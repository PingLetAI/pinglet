const { test } = require('node:test');
const assert = require('node:assert/strict');
const { AuthService } = require('../dist/auth/auth.service');
const { UsersService } = require('../dist/users/users.service');
const { IngestionService } = require('../dist/ingestion/ingestion.service');
const { IngestionProcessor } = require('../dist/ingestion/ingestion.processor');
const { CURRENT_TERMS_VERSION } = require('../dist/common/legal/terms.constants');
const { PUBLIC_SOURCE_ANALYSIS_VERSION } = require('../dist/ingestion/analysis-version');

const hasCode = (code) => (error) => error.getResponse?.().code === code;

function authFixture({ user = null, device = null, createError = null } = {}) {
  const mutations = [];
  const prisma = {
    user: { findUnique: async () => user },
    device: { findUnique: async () => device },
    refreshToken: {
      deleteMany: async () => mutations.push('delete tokens'),
      create: async () => mutations.push('create token'),
    },
  };
  const users = { createAnonymous: async () => {
    if (createError) throw createError;
    mutations.push('create user');
    return { id: 'new-guest' };
  } };
  const devices = { findOrCreate: async () => { mutations.push('create device'); return { id: 'new-device' }; } };
  return {
    service: new AuthService(users, devices, prisma, { signAsync: async () => 'signed-token' }, { get: (_, fallback) => fallback }),
    mutations, prisma,
  };
}

for (const isAnonymous of [true, false]) {
  test(`installation ID cannot recover an existing ${isAnonymous ? 'guest' : 'verified'} account`, async () => {
    const { service, mutations } = authFixture({ user: { id: 'existing', isAnonymous } });
    await assert.rejects(service.createAnonymous('known-installation'), hasCode('INSTALLATION_ID_IN_USE'));
    assert.deepEqual(mutations, []);
  });
}

test('bootstrap cannot reassign a device after logout cleared the user installation ID', async () => {
  const { service, mutations } = authFixture({ device: { id: 'existing-device', userId: 'verified-owner' } });
  await assert.rejects(service.createAnonymous('known-device-installation'), hasCode('INSTALLATION_ID_IN_USE'));
  assert.deepEqual(mutations, []);
});

test('concurrent bootstrap collision never issues credentials for the winner', async () => {
  const { service, mutations } = authFixture({ createError: { code: 'P2002' } });
  await assert.rejects(service.createAnonymous('racing-installation'), hasCode('INSTALLATION_ID_IN_USE'));
  assert.deepEqual(mutations, []);
});

test('a new installation still receives its own guest session', async () => {
  const { service, mutations } = authFixture();
  const result = await service.createAnonymous('new-installation');
  assert.equal(result.userId, 'new-guest');
  assert.equal(result.deviceId, 'new-device');
  assert.ok(result.refreshToken);
  assert.deepEqual(mutations, ['create user', 'create device', 'delete tokens', 'create token']);
});

test('refresh rejects tokens whose device now belongs to another user', async () => {
  const { service, prisma } = authFixture();
  prisma.refreshToken.findUnique = async () => ({
    userId: 'old-user', device: { userId: 'new-user' }, expiresAt: new Date(Date.now() + 60000),
  });
  await assert.rejects(service.refreshAccess('stale-token'), /Invalid or expired refresh token/);
});

test('consent rejects empty and obsolete versions without recording acceptance', async () => {
  let writes = 0;
  const service = new UsersService({ user: { update: async () => writes++ } });
  for (const version of [undefined, '', '2026-08-26']) {
    await assert.rejects(service.acceptCurrentTerms('user', version), hasCode('CONSENT_VERSION_REQUIRED'));
  }
  assert.equal(writes, 0);
});

test('explicit current consent is recorded with its version and time', async () => {
  let recorded;
  const service = new UsersService({ user: { update: async (value) => { recorded = value; } } });
  const result = await service.acceptCurrentTerms('user', CURRENT_TERMS_VERSION);
  assert.equal(result.accepted, true);
  assert.equal(recorded.data.termsAcceptedVersion, CURRENT_TERMS_VERSION);
  assert.ok(recorded.data.termsAcceptedAt instanceof Date);
});

test('obsolete consent cannot initiate an import', async () => {
  const service = new IngestionService({ user: { findUnique: async () => ({ termsAcceptedVersion: '2026-08-26' }) } }, {}, {});
  await assert.rejects(service.createUrlIngestion('user', 'https://www.instagram.com/p/test/'), hasCode('TERMS_ACCEPTANCE_REQUIRED'));
});

function ingestionFixture(records) {
  const created = [];
  let queued = 0;
  const prisma = {
    user: { findUnique: async () => ({ termsAcceptedVersion: CURRENT_TERMS_VERSION }) },
    userContent: { upsert: async () => ({}) },
    ingestion: {
      findFirst: async ({ where }) => records.find((row) => {
        if (where.analysis && row.analysis?.[where.analysis.path[0]] !== where.analysis.equals) return false;
        if (where.userId && row.userId !== where.userId) return false;
        return true;
      }) ?? null,
      create: async ({ data }) => { const row = { id: 'new-ingestion', ...data }; created.push(row); return row; },
    },
  };
  prisma.$transaction = (fn) => fn(prisma);
  const service = new IngestionService(prisma, { enqueue: async () => queued++ }, {
    assertCanSave: async () => {}, assertCanStartOriginalImport: async () => {},
    getSummary: async () => ({ plan: 'PLUS' }),
  });
  return { service, created, queued: () => queued };
}

test('legacy analysis is not reused, and extra text from old clients is not stored', async () => {
  const marker = 'PRIVATE_MARKER_NEVER_SHARE';
  const { service, created, queued } = ingestionFixture([{
    userId: 'user-A', contentItemId: 'old-content', status: 'READY',
    sourceDocument: marker, analysis: { summary: { short: marker } },
  }]);
  const result = await service.createUrlIngestion('user-B', 'https://www.instagram.com/p/test/', marker);
  assert.equal(queued(), 1);
  assert.equal(created[0].rawText, null);
  assert.equal(JSON.stringify(result).includes(marker), false);
});

test('generalized public-only analysis remains reusable across accounts', async () => {
  const publicAnalysis = { sourceScope: PUBLIC_SOURCE_ANALYSIS_VERSION, summary: { short: 'Public post summary' } };
  const { service, created, queued } = ingestionFixture([{
    userId: 'user-A', contentItemId: 'public-content', status: 'READY',
    sourceDocument: 'Public caption', analysis: publicAnalysis,
  }]);
  const result = await service.createUrlIngestion('user-B', 'https://www.instagram.com/p/test/');
  assert.equal(queued(), 0);
  assert.equal(created[0].userId, 'user-B');
  assert.equal(created[0].rawText, null);
  assert.equal(result.analysis.summary.short, 'Public post summary');
});

test('worker excludes legacy user input from AI, stored analysis, and catalog classification', async () => {
  const marker = 'PRIVATE_MARKER_NEVER_SHARE';
  const aiInputs = [];
  const updates = [];
  let cleanup = false;
  const prisma = {
    user: { findUnique: async () => ({ termsAcceptedVersion: CURRENT_TERMS_VERSION }) },
    ingestion: {
      findUnique: async () => ({ id: 'import', userId: 'user-A', type: 'URL', status: 'RECEIVED', sourceUrl: 'https://www.instagram.com/p/test/', rawText: marker }),
      update: async ({ data }) => { updates.push(data); return data; },
    },
    catalog: { findMany: async () => [{ id: 'catalog', slug: 'public', name: 'Public', description: '' }] },
  };
  const media = {
    acquire: async () => ({ workspace: '/fake-workspace', caption: 'This is a public caption about learning.', images: [], videos: [], canonicalUrl: 'https://www.instagram.com/p/test/' }),
    ocr: async () => [], cleanup: async () => { cleanup = true; },
  };
  const ai = {
    inspectImages: async () => '',
    moderate: async (input) => { aiInputs.push(input); return { flagged: false }; },
    deriveAnalysis: async (input) => { aiInputs.push(input); return {
      sourceLanguage: 'en', sourceLanguageConfidence: 1,
      summary: { short: 'A public post about learning' },
      takeaways: [{ text: 'Learning takes practice.', type: 'NOTE', confidence: 1 }],
    }; },
    classifyCatalogs: async (input) => { aiInputs.push(input); return []; },
  };
  const processor = new IngestionProcessor(prisma, { createUserContent: async () => ({ contentItemId: 'public-content' }) }, media, ai);
  await processor.process('import');
  assert.ok(aiInputs.length >= 2);
  assert.equal(aiInputs.some((input) => input.includes(marker)), false);
  assert.equal(JSON.stringify(updates).includes(marker), false);
  const ready = updates.find((row) => row.status === 'READY');
  assert.ok(ready, 'processing must succeed');
  assert.equal(ready.analysis.sourceScope, PUBLIC_SOURCE_ANALYSIS_VERSION);
  assert.equal(cleanup, true);
});

test('worker does not acquire media or call AI for a job queued under old consent', async () => {
  let final;
  const prisma = {
    user: { findUnique: async () => ({ termsAcceptedVersion: '2026-08-26' }) },
    ingestion: {
      findUnique: async () => ({ id: 'legacy-job', userId: 'user', status: 'RECEIVED' }),
      update: async ({ data }) => { final = data; },
    },
  };
  // Empty provider mocks make any attempted external processing fail the test.
  await new IngestionProcessor(prisma, {}, {}, {}).process('legacy-job');
  assert.equal(final.status, 'FAILED');
  assert.equal(final.errorCode, 'TERMS_ACCEPTANCE_REQUIRED');
});
