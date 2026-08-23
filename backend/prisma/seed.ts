import {
  ContentStatus,
  ContentType,
  ContentVisibility,
  DevicePlatform,
  PersonalSystemMix,
  PrismaClient,
} from '@prisma/client';
import { createHash } from 'crypto';

const prisma = new PrismaClient();

const categories = [
  'motivation',
  'discipline',
  'drive',
  'focus',
  'business',
  'fitness',
  'confidence',
  'mindset',
  'life',
  'gratitude',
  'relationships',
  'faith',
];

const catalogSeed = [
  { slug: 'daily-motivation', name: 'Daily Motivation', description: 'General daily mindset material' },
  { slug: 'discipline', name: 'Discipline', description: 'Focus on consistency and follow-through' },
  { slug: 'drive', name: 'Drive', description: 'Intentional motivation for movement' },
  { slug: 'business-mindset', name: 'Business Mindset', description: 'Thoughts for work and ownership' },
  { slug: 'fitness-motivation', name: 'Fitness Motivation', description: 'Performance and habits for physical activity' },
  { slug: 'confidence', name: 'Confidence', description: 'Self-assurance and calm action' },
  { slug: 'morning-focus', name: 'Morning Focus', description: 'Start-of-day reminders and prompts' },
];

const baseTexts = [
  'Discipline is choosing what matters over what is easy.',
  'Small progress compounds into momentum.',
  'Do the hard thing first.',
  'Start before you feel ready.',
  'Saying yes to your future sometimes means saying no today.',
  'You do not need motivation to begin, you need action.',
  'Show up, even when it is imperfect.',
  'Momentum is built from tiny, repeated choices.',
  'Clarity begins where excuses end.',
  'Your future self is watching.',
];

function normalizeText(value: string) {
  return value
    .normalize('NFKC')
    .toLowerCase()
    .replace(/[\u2018\u2019]/g, "'")
    .replace(/[\u201C\u201D]/g, '"')
    .replace(/[\s]+/g, ' ')
    .replace(/^"|"$|^'|'$/g, '')
    .trim();
}

function makeHash(value: string) {
  return createHash('sha256').update(normalizeText(value)).digest('hex');
}

async function seedUsers() {
  const user = await prisma.user.upsert({
    where: { installationId: 'seed-anon-user' },
    update: {},
    create: {
      installationId: 'seed-anon-user',
      isAnonymous: false,
      preferences: {
        create: {
          refreshMinutes: 30,
          personalSystemMix: PersonalSystemMix.BALANCED,
          theme: 'system',
        },
      },
    },
  });

  const device = await prisma.device.upsert({
    where: { installationId: 'seed-device' },
    update: {},
    create: {
      userId: user.id,
      installationId: 'seed-device',
      platform: DevicePlatform.ANDROID,
      timezone: 'UTC',
      locale: 'en',
      appVersion: '0.1.0',
    },
  });

  await prisma.refreshToken.upsert({
    where: { token: 'seed-refresh-token' },
    update: {},
    create: {
      token: 'seed-refresh-token',
      userId: user.id,
      deviceId: device.id,
      expiresAt: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000),
    },
  });

  return user;
}

async function ensureCategories() {
  const categoryModels: { id: string; slug: string }[] = [];
  for (let i = 0; i < categories.length; i += 1) {
    const slug = categories[i];
    const category = await prisma.category.upsert({
      where: { slug },
      update: {},
      create: {
        slug,
        name: slug.replace(/\b\w/g, (m) => m.toUpperCase()),
        sortOrder: i,
        isActive: true,
      },
    });
    categoryModels.push({ id: category.id, slug });
  }
  return categoryModels;
}

async function ensureCatalogs() {
  const map = new Map<string, any>();

  for (const [index, catalog] of catalogSeed.entries()) {
    const row = await prisma.catalog.upsert({
      where: { slug: catalog.slug },
      update: { name: catalog.name, description: catalog.description },
      create: {
        slug: catalog.slug,
        name: catalog.name,
        description: catalog.description,
        isActive: true,
      },
    });
    map.set(catalog.slug, row);
  }

  return map;
}

async function seedSystemContent(categoriesBySlug: { id: string; slug: string }[], catalogMap: Map<string, any>) {
  const statements: string[] = [];
  let idx = 0;

  for (let i = 0; i < 200; i += 1) {
    const text = `${baseTexts[idx % baseTexts.length]} ${i + 1}`;
    idx += 1;
    const textNorm = normalizeText(text);
    const hash = makeHash(text);
    const catalog = catalogSeed[i % catalogSeed.length];
    const category = categoriesBySlug[i % categoriesBySlug.length];

    const types: ContentType[] = [
      ContentType.QUOTE,
      ContentType.REMINDER,
      ContentType.AFFIRMATION,
      ContentType.GOAL,
      ContentType.MESSAGE,
      ContentType.NOTE,
      ContentType.PASSAGE,
    ];

    const created = await prisma.contentItem.upsert({
      where: { contentHash: hash },
      update: {},
      create: {
        text,
        type: types[i % types.length],
        visibility: ContentVisibility.SYSTEM,
        ownerUserId: null,
        language: 'en',
        status: ContentStatus.ACTIVE,
        normalizedText: textNorm,
        contentHash: hash,
        categories: {
          create: {
            category: {
              connect: { slug: category.slug },
            },
          },
        },
        catalogItems: {
          create: {
            catalog: {
              connect: { id: catalogMap.get(catalog.slug).id },
            },
            priority: 1,
          },
        },
      },
    });

    statements.push(created.id);

    if (i % 20 === 0) {
      const catalogIds = Array.from(catalogMap.values()).map((c) => c.id);
      await prisma.catalogItem.createMany({
        data: catalogIds.map((catalogId, cIdx) => ({
          catalogId,
          contentItemId: created.id,
          priority: 1,
        })),
        skipDuplicates: true,
      });
    }
  }

  return statements.length;
}

async function main() {
  await prisma.$connect();

  await seedUsers();
  const categoryModels = await ensureCategories();
  const catalogMap = await ensureCatalogs();
  const totalContent = await seedSystemContent(categoryModels, catalogMap);

  // Ensure all users read their catalogs by default.
  const users = await prisma.user.findMany({ where: { isAnonymous: true } });
  const catalogs = await prisma.catalog.findMany({ where: { isActive: true } });

  for (const user of users) {
    for (const catalog of catalogs) {
      await prisma.userCatalogPreference.upsert({
        where: { userId_catalogId: { userId: user.id, catalogId: catalog.id } },
        update: { enabled: true },
        create: {
          userId: user.id,
          catalogId: catalog.id,
          enabled: true,
          weight: 1,
        },
      });
    }
  }

  // eslint-disable-next-line no-console
  console.log(`Seed complete. Created ${totalContent} system content rows.`);
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
