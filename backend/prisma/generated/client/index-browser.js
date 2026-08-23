
Object.defineProperty(exports, "__esModule", { value: true });

const {
  Decimal,
  objectEnumValues,
  makeStrictEnum,
  Public,
  getRuntime,
  skip
} = require('./runtime/index-browser.js')


const Prisma = {}

exports.Prisma = Prisma
exports.$Enums = {}

/**
 * Prisma Client JS version: 5.22.0
 * Query Engine version: 605197351a3c8bdd595af2d2a9bc3025bca48ea2
 */
Prisma.prismaVersion = {
  client: "5.22.0",
  engine: "605197351a3c8bdd595af2d2a9bc3025bca48ea2"
}

Prisma.PrismaClientKnownRequestError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`PrismaClientKnownRequestError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)};
Prisma.PrismaClientUnknownRequestError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`PrismaClientUnknownRequestError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.PrismaClientRustPanicError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`PrismaClientRustPanicError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.PrismaClientInitializationError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`PrismaClientInitializationError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.PrismaClientValidationError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`PrismaClientValidationError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.NotFoundError = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`NotFoundError is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.Decimal = Decimal

/**
 * Re-export of sql-template-tag
 */
Prisma.sql = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`sqltag is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.empty = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`empty is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.join = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`join is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.raw = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`raw is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.validator = Public.validator

/**
* Extensions
*/
Prisma.getExtensionContext = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`Extensions.getExtensionContext is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}
Prisma.defineExtension = () => {
  const runtimeName = getRuntime().prettyName;
  throw new Error(`Extensions.defineExtension is unable to run in this browser environment, or has been bundled for the browser (running in ${runtimeName}).
In case this error is unexpected for you, please report it in https://pris.ly/prisma-prisma-bug-report`,
)}

/**
 * Shorthand utilities for JSON filtering
 */
Prisma.DbNull = objectEnumValues.instances.DbNull
Prisma.JsonNull = objectEnumValues.instances.JsonNull
Prisma.AnyNull = objectEnumValues.instances.AnyNull

Prisma.NullTypes = {
  DbNull: objectEnumValues.classes.DbNull,
  JsonNull: objectEnumValues.classes.JsonNull,
  AnyNull: objectEnumValues.classes.AnyNull
}



/**
 * Enums
 */

exports.Prisma.TransactionIsolationLevel = makeStrictEnum({
  ReadUncommitted: 'ReadUncommitted',
  ReadCommitted: 'ReadCommitted',
  RepeatableRead: 'RepeatableRead',
  Serializable: 'Serializable'
});

exports.Prisma.UserScalarFieldEnum = {
  id: 'id',
  installationId: 'installationId',
  isAnonymous: 'isAnonymous',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.DeviceScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  installationId: 'installationId',
  platform: 'platform',
  timezone: 'timezone',
  locale: 'locale',
  appVersion: 'appVersion',
  lastSyncAt: 'lastSyncAt',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.RefreshTokenScalarFieldEnum = {
  id: 'id',
  token: 'token',
  userId: 'userId',
  deviceId: 'deviceId',
  expiresAt: 'expiresAt',
  revokedAt: 'revokedAt',
  createdAt: 'createdAt'
};

exports.Prisma.ContentItemScalarFieldEnum = {
  id: 'id',
  text: 'text',
  type: 'type',
  author: 'author',
  sourceUrl: 'sourceUrl',
  sourcePlatform: 'sourcePlatform',
  language: 'language',
  visibility: 'visibility',
  ownerUserId: 'ownerUserId',
  status: 'status',
  normalizedText: 'normalizedText',
  contentHash: 'contentHash',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.CategoryScalarFieldEnum = {
  id: 'id',
  slug: 'slug',
  name: 'name',
  description: 'description',
  isActive: 'isActive',
  sortOrder: 'sortOrder'
};

exports.Prisma.ContentItemCategoryScalarFieldEnum = {
  contentItemId: 'contentItemId',
  categoryId: 'categoryId'
};

exports.Prisma.CatalogScalarFieldEnum = {
  id: 'id',
  slug: 'slug',
  name: 'name',
  description: 'description',
  isActive: 'isActive',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.CatalogItemScalarFieldEnum = {
  catalogId: 'catalogId',
  contentItemId: 'contentItemId',
  priority: 'priority'
};

exports.Prisma.UserContentScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  contentItemId: 'contentItemId',
  favorite: 'favorite',
  archived: 'archived',
  priority: 'priority',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.UserCatalogPreferenceScalarFieldEnum = {
  userId: 'userId',
  catalogId: 'catalogId',
  enabled: 'enabled',
  weight: 'weight'
};

exports.Prisma.UserPreferenceScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  refreshMinutes: 'refreshMinutes',
  personalSystemMix: 'personalSystemMix',
  theme: 'theme',
  updatedAt: 'updatedAt',
  createdAt: 'createdAt'
};

exports.Prisma.IngestionScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  type: 'type',
  rawText: 'rawText',
  sourceUrl: 'sourceUrl',
  assetUrl: 'assetUrl',
  status: 'status',
  createdAt: 'createdAt',
  updatedAt: 'updatedAt'
};

exports.Prisma.EventScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  type: 'type',
  contentItemId: 'contentItemId',
  surface: 'surface',
  timestamp: 'timestamp',
  synced: 'synced',
  metadata: 'metadata'
};

exports.Prisma.FavoriteScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  contentItemId: 'contentItemId',
  createdAt: 'createdAt'
};

exports.Prisma.QueueItemScalarFieldEnum = {
  id: 'id',
  contentItemId: 'contentItemId',
  slotIndex: 'slotIndex',
  source: 'source',
  displayed: 'displayed',
  validFrom: 'validFrom',
  validUntil: 'validUntil',
  createdAt: 'createdAt'
};

exports.Prisma.PendingActionScalarFieldEnum = {
  id: 'id',
  userId: 'userId',
  actionType: 'actionType',
  payload: 'payload',
  attempts: 'attempts',
  createdAt: 'createdAt'
};

exports.Prisma.SortOrder = {
  asc: 'asc',
  desc: 'desc'
};

exports.Prisma.QueryMode = {
  default: 'default',
  insensitive: 'insensitive'
};

exports.Prisma.NullsOrder = {
  first: 'first',
  last: 'last'
};
exports.DevicePlatform = exports.$Enums.DevicePlatform = {
  ANDROID: 'ANDROID',
  IOS: 'IOS'
};

exports.ContentType = exports.$Enums.ContentType = {
  QUOTE: 'QUOTE',
  REMINDER: 'REMINDER',
  AFFIRMATION: 'AFFIRMATION',
  GOAL: 'GOAL',
  MESSAGE: 'MESSAGE',
  NOTE: 'NOTE',
  PASSAGE: 'PASSAGE'
};

exports.ContentVisibility = exports.$Enums.ContentVisibility = {
  SYSTEM: 'SYSTEM',
  PRIVATE: 'PRIVATE',
  COMMUNITY: 'COMMUNITY'
};

exports.ContentStatus = exports.$Enums.ContentStatus = {
  ACTIVE: 'ACTIVE',
  ARCHIVED: 'ARCHIVED',
  PENDING: 'PENDING',
  REJECTED: 'REJECTED'
};

exports.PersonalSystemMix = exports.$Enums.PersonalSystemMix = {
  MOSTLY_MINE: 'MOSTLY_MINE',
  BALANCED: 'BALANCED',
  MORE_DISCOVERY: 'MORE_DISCOVERY'
};

exports.IngestionType = exports.$Enums.IngestionType = {
  TEXT: 'TEXT',
  URL: 'URL',
  IMAGE: 'IMAGE'
};

exports.IngestionStatus = exports.$Enums.IngestionStatus = {
  RECEIVED: 'RECEIVED',
  PROCESSING: 'PROCESSING',
  READY: 'READY',
  FAILED: 'FAILED'
};

exports.EventType = exports.$Enums.EventType = {
  CONTENT_SHOWN: 'CONTENT_SHOWN',
  CONTENT_SKIPPED: 'CONTENT_SKIPPED',
  CONTENT_FAVORITED: 'CONTENT_FAVORITED',
  CONTENT_OPENED: 'CONTENT_OPENED'
};

exports.Surface = exports.$Enums.Surface = {
  WIDGET: 'WIDGET',
  APP: 'APP',
  NOTIFICATION: 'NOTIFICATION'
};

exports.CatalogItemSource = exports.$Enums.CatalogItemSource = {
  PERSONAL: 'PERSONAL',
  SYSTEM: 'SYSTEM'
};

exports.PendingActionType = exports.$Enums.PendingActionType = {
  CREATE_CONTENT: 'CREATE_CONTENT',
  UPDATE_CONTENT: 'UPDATE_CONTENT',
  DELETE_CONTENT: 'DELETE_CONTENT',
  FAVORITE: 'FAVORITE',
  UNFAVORITE: 'UNFAVORITE',
  ARCHIVE: 'ARCHIVE',
  IMPRESSION_BATCH: 'IMPRESSION_BATCH'
};

exports.Prisma.ModelName = {
  User: 'User',
  Device: 'Device',
  RefreshToken: 'RefreshToken',
  ContentItem: 'ContentItem',
  Category: 'Category',
  ContentItemCategory: 'ContentItemCategory',
  Catalog: 'Catalog',
  CatalogItem: 'CatalogItem',
  UserContent: 'UserContent',
  UserCatalogPreference: 'UserCatalogPreference',
  UserPreference: 'UserPreference',
  Ingestion: 'Ingestion',
  Event: 'Event',
  Favorite: 'Favorite',
  QueueItem: 'QueueItem',
  PendingAction: 'PendingAction'
};

/**
 * This is a stub Prisma Client that will error at runtime if called.
 */
class PrismaClient {
  constructor() {
    return new Proxy(this, {
      get(target, prop) {
        let message
        const runtime = getRuntime()
        if (runtime.isEdge) {
          message = `PrismaClient is not configured to run in ${runtime.prettyName}. In order to run Prisma Client on edge runtime, either:
- Use Prisma Accelerate: https://pris.ly/d/accelerate
- Use Driver Adapters: https://pris.ly/d/driver-adapters
`;
        } else {
          message = 'PrismaClient is unable to run in this browser environment, or has been bundled for the browser (running in `' + runtime.prettyName + '`).'
        }
        
        message += `
If this is unexpected, please open an issue: https://pris.ly/prisma-prisma-bug-report`

        throw new Error(message)
      }
    })
  }
}

exports.PrismaClient = PrismaClient

Object.assign(exports, Prisma)
