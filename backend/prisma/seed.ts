import { PrismaClient } from '@prisma/client';

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

const catalogs = [
  { slug: 'daily-motivation', name: 'Daily Motivation', description: 'Ideas that encourage constructive action and steady progress' },
  { slug: 'discipline', name: 'Discipline', description: 'Consistency, self-control, habits, and following through' },
  { slug: 'drive', name: 'Drive', description: 'Ambition, perseverance, resilience, and purposeful effort' },
  { slug: 'business-mindset', name: 'Business Mindset', description: 'Entrepreneurship, leadership, ownership, and building useful work' },
  { slug: 'fitness-motivation', name: 'Fitness Motivation', description: 'Training, movement, physical consistency, and healthy performance' },
  { slug: 'confidence', name: 'Confidence', description: 'Self-belief, courage, composure, and taking action despite uncertainty' },
  { slug: 'morning-focus', name: 'Morning Focus', description: 'Intentional starts, priorities, attention, and planning the day' },
];

async function main() {
  await prisma.$connect();

  const removedLegacyContent = await prisma.contentItem.deleteMany({
    where: {
      visibility: 'SYSTEM',
      ownerUserId: null,
      sourceUrl: null,
    },
  });
  await prisma.user.deleteMany({ where: { installationId: 'seed-anon-user' } });

  for (const [index, slug] of categories.entries()) {
    await prisma.category.upsert({
      where: { slug },
      update: { sortOrder: index, isActive: true },
      create: {
        slug,
        name: slug.replace(/\b\w/g, (letter) => letter.toUpperCase()),
        sortOrder: index,
        isActive: true,
      },
    });
  }

  for (const catalog of catalogs) {
    await prisma.catalog.upsert({
      where: { slug: catalog.slug },
      update: { name: catalog.name, description: catalog.description, isActive: true },
      create: { ...catalog, isActive: true },
    });
  }

  // Catalog definitions are bootstrapped, but catalog content is not. Moderated,
  // high-confidence public-link ingestions populate collections over time.
  console.log(`Catalog bootstrap complete. Removed ${removedLegacyContent.count} legacy seeded content rows.`);
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
