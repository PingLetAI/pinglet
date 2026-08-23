package com.linger.app.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.linger.app.`data`.local.entity.CategoryEntity
import com.linger.app.`data`.local.entity.ContentEntity
import com.linger.app.`data`.local.entity.DisplayHistoryEntity
import com.linger.app.`data`.local.entity.PendingActionEntity
import com.linger.app.`data`.local.entity.QueueItemEntity
import com.linger.app.`data`.local.entity.UserContentEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ContentDao_Impl(
  __db: RoomDatabase,
) : ContentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfContentEntity: EntityInsertAdapter<ContentEntity>

  private val __insertAdapterOfCategoryEntity: EntityInsertAdapter<CategoryEntity>

  private val __insertAdapterOfQueueItemEntity: EntityInsertAdapter<QueueItemEntity>

  private val __insertAdapterOfPendingActionEntity: EntityInsertAdapter<PendingActionEntity>

  private val __insertAdapterOfDisplayHistoryEntity: EntityInsertAdapter<DisplayHistoryEntity>

  private val __insertAdapterOfUserContentEntity: EntityInsertAdapter<UserContentEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfContentEntity = object : EntityInsertAdapter<ContentEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `content` (`id`,`text`,`type`,`author`,`source`,`sourceUrl`,`visibility`,`ownerUserId`,`status`,`language`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ContentEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.text)
        statement.bindText(3, entity.type)
        val _tmpAuthor: String? = entity.author
        if (_tmpAuthor == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpAuthor)
        }
        statement.bindText(5, entity.source)
        val _tmpSourceUrl: String? = entity.sourceUrl
        if (_tmpSourceUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpSourceUrl)
        }
        statement.bindText(7, entity.visibility)
        val _tmpOwnerUserId: String? = entity.ownerUserId
        if (_tmpOwnerUserId == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpOwnerUserId)
        }
        statement.bindText(9, entity.status)
        statement.bindText(10, entity.language)
      }
    }
    this.__insertAdapterOfCategoryEntity = object : EntityInsertAdapter<CategoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `categories` (`id`,`slug`,`name`,`description`,`isActive`,`sortOrder`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CategoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.slug)
        statement.bindText(3, entity.name)
        val _tmpDescription: String? = entity.description
        if (_tmpDescription == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpDescription)
        }
        val _tmp: Int = if (entity.isActive) 1 else 0
        statement.bindLong(5, _tmp.toLong())
        statement.bindLong(6, entity.sortOrder.toLong())
      }
    }
    this.__insertAdapterOfQueueItemEntity = object : EntityInsertAdapter<QueueItemEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `queue_items` (`id`,`contentItemId`,`slotIndex`,`source`,`validFrom`,`validUntil`,`displayed`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: QueueItemEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.contentItemId)
        statement.bindLong(3, entity.slotIndex.toLong())
        statement.bindText(4, entity.source)
        val _tmpValidFrom: Long? = entity.validFrom
        if (_tmpValidFrom == null) {
          statement.bindNull(5)
        } else {
          statement.bindLong(5, _tmpValidFrom)
        }
        val _tmpValidUntil: Long? = entity.validUntil
        if (_tmpValidUntil == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpValidUntil)
        }
        val _tmp: Int = if (entity.displayed) 1 else 0
        statement.bindLong(7, _tmp.toLong())
      }
    }
    this.__insertAdapterOfPendingActionEntity = object : EntityInsertAdapter<PendingActionEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `pending_actions` (`id`,`actionType`,`payload`,`attempts`,`createdAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PendingActionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.actionType)
        statement.bindText(3, entity.payload)
        statement.bindLong(4, entity.attempts.toLong())
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__insertAdapterOfDisplayHistoryEntity = object :
        EntityInsertAdapter<DisplayHistoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `display_history` (`id`,`contentItemId`,`shownAt`,`surface`,`reaction`,`synced`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DisplayHistoryEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.contentItemId)
        statement.bindLong(3, entity.shownAt)
        statement.bindText(4, entity.surface)
        statement.bindText(5, entity.reaction)
        val _tmp: Int = if (entity.synced) 1 else 0
        statement.bindLong(6, _tmp.toLong())
      }
    }
    this.__insertAdapterOfUserContentEntity = object : EntityInsertAdapter<UserContentEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `user_content` (`id`,`userId`,`contentItemId`,`favorite`,`archived`,`priority`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserContentEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.userId)
        statement.bindText(3, entity.contentItemId)
        val _tmp: Int = if (entity.favorite) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.archived) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindDouble(6, entity.priority.toDouble())
        statement.bindLong(7, entity.createdAt)
        statement.bindLong(8, entity.updatedAt)
      }
    }
  }

  public override suspend fun upsertContent(content: List<ContentEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfContentEntity.insert(_connection, content)
  }

  public override suspend fun upsertCategory(categories: List<CategoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCategoryEntity.insert(_connection, categories)
  }

  public override suspend fun upsertQueue(items: List<QueueItemEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfQueueItemEntity.insert(_connection, items)
  }

  public override suspend fun upsertPendingActions(actions: List<PendingActionEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPendingActionEntity.insert(_connection, actions)
  }

  public override suspend fun insertHistory(entries: List<DisplayHistoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDisplayHistoryEntity.insert(_connection, entries)
  }

  public override suspend fun upsertUserContent(items: List<UserContentEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserContentEntity.insert(_connection, items)
  }

  public override suspend fun replaceFeed(content: List<ContentEntity>,
      queue: List<QueueItemEntity>): Unit = performInTransactionSuspending(__db) {
    super@ContentDao_Impl.replaceFeed(content, queue)
  }

  public override suspend fun allContent(): List<ContentEntity> {
    val _sql: String = "SELECT * FROM content"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceUrl: Int = getColumnIndexOrThrow(_stmt, "sourceUrl")
        val _columnIndexOfVisibility: Int = getColumnIndexOrThrow(_stmt, "visibility")
        val _columnIndexOfOwnerUserId: Int = getColumnIndexOrThrow(_stmt, "ownerUserId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: MutableList<ContentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ContentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceUrl: String?
          if (_stmt.isNull(_columnIndexOfSourceUrl)) {
            _tmpSourceUrl = null
          } else {
            _tmpSourceUrl = _stmt.getText(_columnIndexOfSourceUrl)
          }
          val _tmpVisibility: String
          _tmpVisibility = _stmt.getText(_columnIndexOfVisibility)
          val _tmpOwnerUserId: String?
          if (_stmt.isNull(_columnIndexOfOwnerUserId)) {
            _tmpOwnerUserId = null
          } else {
            _tmpOwnerUserId = _stmt.getText(_columnIndexOfOwnerUserId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _item =
              ContentEntity(_tmpId,_tmpText,_tmpType,_tmpAuthor,_tmpSource,_tmpSourceUrl,_tmpVisibility,_tmpOwnerUserId,_tmpStatus,_tmpLanguage)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun contentById(id: String): ContentEntity? {
    val _sql: String = "SELECT * FROM content WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfAuthor: Int = getColumnIndexOrThrow(_stmt, "author")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceUrl: Int = getColumnIndexOrThrow(_stmt, "sourceUrl")
        val _columnIndexOfVisibility: Int = getColumnIndexOrThrow(_stmt, "visibility")
        val _columnIndexOfOwnerUserId: Int = getColumnIndexOrThrow(_stmt, "ownerUserId")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfLanguage: Int = getColumnIndexOrThrow(_stmt, "language")
        val _result: ContentEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpAuthor: String?
          if (_stmt.isNull(_columnIndexOfAuthor)) {
            _tmpAuthor = null
          } else {
            _tmpAuthor = _stmt.getText(_columnIndexOfAuthor)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceUrl: String?
          if (_stmt.isNull(_columnIndexOfSourceUrl)) {
            _tmpSourceUrl = null
          } else {
            _tmpSourceUrl = _stmt.getText(_columnIndexOfSourceUrl)
          }
          val _tmpVisibility: String
          _tmpVisibility = _stmt.getText(_columnIndexOfVisibility)
          val _tmpOwnerUserId: String?
          if (_stmt.isNull(_columnIndexOfOwnerUserId)) {
            _tmpOwnerUserId = null
          } else {
            _tmpOwnerUserId = _stmt.getText(_columnIndexOfOwnerUserId)
          }
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpLanguage: String
          _tmpLanguage = _stmt.getText(_columnIndexOfLanguage)
          _result =
              ContentEntity(_tmpId,_tmpText,_tmpType,_tmpAuthor,_tmpSource,_tmpSourceUrl,_tmpVisibility,_tmpOwnerUserId,_tmpStatus,_tmpLanguage)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun queue(limit: Int): List<QueueItemEntity> {
    val _sql: String = "SELECT * FROM queue_items ORDER BY slotIndex LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentItemId: Int = getColumnIndexOrThrow(_stmt, "contentItemId")
        val _columnIndexOfSlotIndex: Int = getColumnIndexOrThrow(_stmt, "slotIndex")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfValidFrom: Int = getColumnIndexOrThrow(_stmt, "validFrom")
        val _columnIndexOfValidUntil: Int = getColumnIndexOrThrow(_stmt, "validUntil")
        val _columnIndexOfDisplayed: Int = getColumnIndexOrThrow(_stmt, "displayed")
        val _result: MutableList<QueueItemEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: QueueItemEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpContentItemId: String
          _tmpContentItemId = _stmt.getText(_columnIndexOfContentItemId)
          val _tmpSlotIndex: Int
          _tmpSlotIndex = _stmt.getLong(_columnIndexOfSlotIndex).toInt()
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpValidFrom: Long?
          if (_stmt.isNull(_columnIndexOfValidFrom)) {
            _tmpValidFrom = null
          } else {
            _tmpValidFrom = _stmt.getLong(_columnIndexOfValidFrom)
          }
          val _tmpValidUntil: Long?
          if (_stmt.isNull(_columnIndexOfValidUntil)) {
            _tmpValidUntil = null
          } else {
            _tmpValidUntil = _stmt.getLong(_columnIndexOfValidUntil)
          }
          val _tmpDisplayed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDisplayed).toInt()
          _tmpDisplayed = _tmp != 0
          _item =
              QueueItemEntity(_tmpId,_tmpContentItemId,_tmpSlotIndex,_tmpSource,_tmpValidFrom,_tmpValidUntil,_tmpDisplayed)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun pendingActions(): List<PendingActionEntity> {
    val _sql: String = "SELECT * FROM pending_actions"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfActionType: Int = getColumnIndexOrThrow(_stmt, "actionType")
        val _columnIndexOfPayload: Int = getColumnIndexOrThrow(_stmt, "payload")
        val _columnIndexOfAttempts: Int = getColumnIndexOrThrow(_stmt, "attempts")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<PendingActionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PendingActionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpActionType: String
          _tmpActionType = _stmt.getText(_columnIndexOfActionType)
          val _tmpPayload: String
          _tmpPayload = _stmt.getText(_columnIndexOfPayload)
          val _tmpAttempts: Int
          _tmpAttempts = _stmt.getLong(_columnIndexOfAttempts).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = PendingActionEntity(_tmpId,_tmpActionType,_tmpPayload,_tmpAttempts,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recentHistory(limit: Int): List<DisplayHistoryEntity> {
    val _sql: String = "SELECT * FROM display_history ORDER BY shownAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfContentItemId: Int = getColumnIndexOrThrow(_stmt, "contentItemId")
        val _columnIndexOfShownAt: Int = getColumnIndexOrThrow(_stmt, "shownAt")
        val _columnIndexOfSurface: Int = getColumnIndexOrThrow(_stmt, "surface")
        val _columnIndexOfReaction: Int = getColumnIndexOrThrow(_stmt, "reaction")
        val _columnIndexOfSynced: Int = getColumnIndexOrThrow(_stmt, "synced")
        val _result: MutableList<DisplayHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DisplayHistoryEntity
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpContentItemId: String
          _tmpContentItemId = _stmt.getText(_columnIndexOfContentItemId)
          val _tmpShownAt: Long
          _tmpShownAt = _stmt.getLong(_columnIndexOfShownAt)
          val _tmpSurface: String
          _tmpSurface = _stmt.getText(_columnIndexOfSurface)
          val _tmpReaction: String
          _tmpReaction = _stmt.getText(_columnIndexOfReaction)
          val _tmpSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfSynced).toInt()
          _tmpSynced = _tmp != 0
          _item =
              DisplayHistoryEntity(_tmpId,_tmpContentItemId,_tmpShownAt,_tmpSurface,_tmpReaction,_tmpSynced)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun userLibrary(userId: String): List<UserContentEntity> {
    val _sql: String = "SELECT * FROM user_content WHERE userId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, userId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserId: Int = getColumnIndexOrThrow(_stmt, "userId")
        val _columnIndexOfContentItemId: Int = getColumnIndexOrThrow(_stmt, "contentItemId")
        val _columnIndexOfFavorite: Int = getColumnIndexOrThrow(_stmt, "favorite")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<UserContentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: UserContentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpUserId: String
          _tmpUserId = _stmt.getText(_columnIndexOfUserId)
          val _tmpContentItemId: String
          _tmpContentItemId = _stmt.getText(_columnIndexOfContentItemId)
          val _tmpFavorite: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfFavorite).toInt()
          _tmpFavorite = _tmp != 0
          val _tmpArchived: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp_1 != 0
          val _tmpPriority: Float
          _tmpPriority = _stmt.getDouble(_columnIndexOfPriority).toFloat()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              UserContentEntity(_tmpId,_tmpUserId,_tmpContentItemId,_tmpFavorite,_tmpArchived,_tmpPriority,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearQueue() {
    val _sql: String = "DELETE FROM queue_items"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun markQueueItemDisplayed(id: String) {
    val _sql: String = "UPDATE queue_items SET displayed = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePendingAction(id: String) {
    val _sql: String = "DELETE FROM pending_actions WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setUserContentFavorite(
    userId: String,
    contentItemId: String,
    favorite: Boolean,
    updatedAt: Long,
  ) {
    val _sql: String =
        "UPDATE user_content SET favorite = ?, updatedAt = ? WHERE userId = ? AND contentItemId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (favorite) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, userId)
        _argIndex = 4
        _stmt.bindText(_argIndex, contentItemId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
