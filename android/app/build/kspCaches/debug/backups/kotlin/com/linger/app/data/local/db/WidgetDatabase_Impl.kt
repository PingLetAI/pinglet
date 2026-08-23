package com.linger.app.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.linger.app.`data`.local.dao.ContentDao
import com.linger.app.`data`.local.dao.ContentDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class WidgetDatabase_Impl : WidgetDatabase() {
  private val _contentDao: Lazy<ContentDao> = lazy {
    ContentDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "e218d9992aa01d5ae2bc0112817c4751", "603e5ca2ff7b3c0174c437a23d3ca845") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `content` (`id` TEXT NOT NULL, `text` TEXT NOT NULL, `type` TEXT NOT NULL, `author` TEXT, `source` TEXT NOT NULL, `sourceUrl` TEXT, `visibility` TEXT NOT NULL, `ownerUserId` TEXT, `status` TEXT NOT NULL, `language` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` TEXT NOT NULL, `slug` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT, `isActive` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `content_categories` (`contentItemId` TEXT NOT NULL, `categoryId` TEXT NOT NULL, PRIMARY KEY(`contentItemId`, `categoryId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `queue_items` (`id` TEXT NOT NULL, `contentItemId` TEXT NOT NULL, `slotIndex` INTEGER NOT NULL, `source` TEXT NOT NULL, `validFrom` INTEGER, `validUntil` INTEGER, `displayed` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `preferences` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `pending_actions` (`id` TEXT NOT NULL, `actionType` TEXT NOT NULL, `payload` TEXT NOT NULL, `attempts` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `display_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contentItemId` TEXT NOT NULL, `shownAt` INTEGER NOT NULL, `surface` TEXT NOT NULL, `reaction` TEXT NOT NULL, `synced` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_content` (`id` TEXT NOT NULL, `userId` TEXT NOT NULL, `contentItemId` TEXT NOT NULL, `favorite` INTEGER NOT NULL, `archived` INTEGER NOT NULL, `priority` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e218d9992aa01d5ae2bc0112817c4751')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `content`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `content_categories`")
        connection.execSQL("DROP TABLE IF EXISTS `queue_items`")
        connection.execSQL("DROP TABLE IF EXISTS `preferences`")
        connection.execSQL("DROP TABLE IF EXISTS `pending_actions`")
        connection.execSQL("DROP TABLE IF EXISTS `display_history`")
        connection.execSQL("DROP TABLE IF EXISTS `user_content`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsContent: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsContent.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("text", TableInfo.Column("text", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("author", TableInfo.Column("author", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("sourceUrl", TableInfo.Column("sourceUrl", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("visibility", TableInfo.Column("visibility", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("ownerUserId", TableInfo.Column("ownerUserId", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContent.put("language", TableInfo.Column("language", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysContent: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesContent: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoContent: TableInfo = TableInfo("content", _columnsContent, _foreignKeysContent,
            _indicesContent)
        val _existingContent: TableInfo = read(connection, "content")
        if (!_infoContent.equals(_existingContent)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |content(com.linger.app.data.local.entity.ContentEntity).
              | Expected:
              |""".trimMargin() + _infoContent + """
              |
              | Found:
              |""".trimMargin() + _existingContent)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("slug", TableInfo.Column("slug", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("description", TableInfo.Column("description", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("sortOrder", TableInfo.Column("sortOrder", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories,
            _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.linger.app.data.local.entity.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsContentCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsContentCategories.put("contentItemId", TableInfo.Column("contentItemId", "TEXT",
            true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContentCategories.put("categoryId", TableInfo.Column("categoryId", "TEXT", true, 2,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysContentCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesContentCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoContentCategories: TableInfo = TableInfo("content_categories",
            _columnsContentCategories, _foreignKeysContentCategories, _indicesContentCategories)
        val _existingContentCategories: TableInfo = read(connection, "content_categories")
        if (!_infoContentCategories.equals(_existingContentCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |content_categories(com.linger.app.data.local.entity.ContentCategoryEntity).
              | Expected:
              |""".trimMargin() + _infoContentCategories + """
              |
              | Found:
              |""".trimMargin() + _existingContentCategories)
        }
        val _columnsQueueItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsQueueItems.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("contentItemId", TableInfo.Column("contentItemId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("slotIndex", TableInfo.Column("slotIndex", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("validFrom", TableInfo.Column("validFrom", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("validUntil", TableInfo.Column("validUntil", "INTEGER", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsQueueItems.put("displayed", TableInfo.Column("displayed", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysQueueItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesQueueItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoQueueItems: TableInfo = TableInfo("queue_items", _columnsQueueItems,
            _foreignKeysQueueItems, _indicesQueueItems)
        val _existingQueueItems: TableInfo = read(connection, "queue_items")
        if (!_infoQueueItems.equals(_existingQueueItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |queue_items(com.linger.app.data.local.entity.QueueItemEntity).
              | Expected:
              |""".trimMargin() + _infoQueueItems + """
              |
              | Found:
              |""".trimMargin() + _existingQueueItems)
        }
        val _columnsPreferences: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPreferences.put("key", TableInfo.Column("key", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPreferences.put("value", TableInfo.Column("value", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPreferences: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPreferences: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPreferences: TableInfo = TableInfo("preferences", _columnsPreferences,
            _foreignKeysPreferences, _indicesPreferences)
        val _existingPreferences: TableInfo = read(connection, "preferences")
        if (!_infoPreferences.equals(_existingPreferences)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |preferences(com.linger.app.data.local.entity.PreferenceEntity).
              | Expected:
              |""".trimMargin() + _infoPreferences + """
              |
              | Found:
              |""".trimMargin() + _existingPreferences)
        }
        val _columnsPendingActions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPendingActions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingActions.put("actionType", TableInfo.Column("actionType", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingActions.put("payload", TableInfo.Column("payload", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingActions.put("attempts", TableInfo.Column("attempts", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPendingActions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPendingActions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPendingActions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPendingActions: TableInfo = TableInfo("pending_actions", _columnsPendingActions,
            _foreignKeysPendingActions, _indicesPendingActions)
        val _existingPendingActions: TableInfo = read(connection, "pending_actions")
        if (!_infoPendingActions.equals(_existingPendingActions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |pending_actions(com.linger.app.data.local.entity.PendingActionEntity).
              | Expected:
              |""".trimMargin() + _infoPendingActions + """
              |
              | Found:
              |""".trimMargin() + _existingPendingActions)
        }
        val _columnsDisplayHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDisplayHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDisplayHistory.put("contentItemId", TableInfo.Column("contentItemId", "TEXT", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDisplayHistory.put("shownAt", TableInfo.Column("shownAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDisplayHistory.put("surface", TableInfo.Column("surface", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDisplayHistory.put("reaction", TableInfo.Column("reaction", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDisplayHistory.put("synced", TableInfo.Column("synced", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDisplayHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDisplayHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDisplayHistory: TableInfo = TableInfo("display_history", _columnsDisplayHistory,
            _foreignKeysDisplayHistory, _indicesDisplayHistory)
        val _existingDisplayHistory: TableInfo = read(connection, "display_history")
        if (!_infoDisplayHistory.equals(_existingDisplayHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |display_history(com.linger.app.data.local.entity.DisplayHistoryEntity).
              | Expected:
              |""".trimMargin() + _infoDisplayHistory + """
              |
              | Found:
              |""".trimMargin() + _existingDisplayHistory)
        }
        val _columnsUserContent: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserContent.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("userId", TableInfo.Column("userId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("contentItemId", TableInfo.Column("contentItemId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("favorite", TableInfo.Column("favorite", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("priority", TableInfo.Column("priority", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsUserContent.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserContent: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserContent: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserContent: TableInfo = TableInfo("user_content", _columnsUserContent,
            _foreignKeysUserContent, _indicesUserContent)
        val _existingUserContent: TableInfo = read(connection, "user_content")
        if (!_infoUserContent.equals(_existingUserContent)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_content(com.linger.app.data.local.entity.UserContentEntity).
              | Expected:
              |""".trimMargin() + _infoUserContent + """
              |
              | Found:
              |""".trimMargin() + _existingUserContent)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "content", "categories",
        "content_categories", "queue_items", "preferences", "pending_actions", "display_history",
        "user_content")
  }

  public override fun clearAllTables() {
    super.performClear(false, "content", "categories", "content_categories", "queue_items",
        "preferences", "pending_actions", "display_history", "user_content")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ContentDao::class, ContentDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun contentDao(): ContentDao = _contentDao.value
}
