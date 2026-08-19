package com.shiftschedule.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.shiftschedule.app.data.model.CycleShiftsConverter;
import com.shiftschedule.app.data.model.ExceptionsConverter;
import com.shiftschedule.app.data.model.Schedule;
import com.shiftschedule.app.data.model.Template;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ShiftDao_Impl implements ShiftDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Schedule> __insertionAdapterOfSchedule;

  private final ExceptionsConverter __exceptionsConverter = new ExceptionsConverter();

  private final CycleShiftsConverter __cycleShiftsConverter = new CycleShiftsConverter();

  private final EntityInsertionAdapter<Template> __insertionAdapterOfTemplate;

  private final EntityDeletionOrUpdateAdapter<Schedule> __deletionAdapterOfSchedule;

  private final EntityDeletionOrUpdateAdapter<Template> __deletionAdapterOfTemplate;

  private final EntityDeletionOrUpdateAdapter<Schedule> __updateAdapterOfSchedule;

  private final EntityDeletionOrUpdateAdapter<Template> __updateAdapterOfTemplate;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllSchedules;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllTemplates;

  public ShiftDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSchedule = new EntityInsertionAdapter<Schedule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `schedules` (`id`,`name`,`color`,`templateId`,`startDate`,`isActive`,`exceptions`,`cycleShifts`,`sortIndex`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Schedule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getColor());
        if (entity.getTemplateId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getTemplateId());
        }
        statement.bindString(5, entity.getStartDate());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final String _tmp_1 = __exceptionsConverter.fromExceptions(entity.getExceptions());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __cycleShiftsConverter.fromCycleShifts(entity.getCycleShifts());
        statement.bindString(8, _tmp_2);
        statement.bindLong(9, entity.getSortIndex());
      }
    };
    this.__insertionAdapterOfTemplate = new EntityInsertionAdapter<Template>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `templates` (`id`,`name`,`description`,`pattern`,`isBuiltIn`,`sortIndex`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Template entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getPattern());
        final int _tmp = entity.isBuiltIn() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getSortIndex());
      }
    };
    this.__deletionAdapterOfSchedule = new EntityDeletionOrUpdateAdapter<Schedule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `schedules` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Schedule entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__deletionAdapterOfTemplate = new EntityDeletionOrUpdateAdapter<Template>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `templates` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Template entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSchedule = new EntityDeletionOrUpdateAdapter<Schedule>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `schedules` SET `id` = ?,`name` = ?,`color` = ?,`templateId` = ?,`startDate` = ?,`isActive` = ?,`exceptions` = ?,`cycleShifts` = ?,`sortIndex` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Schedule entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getColor());
        if (entity.getTemplateId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getTemplateId());
        }
        statement.bindString(5, entity.getStartDate());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(6, _tmp);
        final String _tmp_1 = __exceptionsConverter.fromExceptions(entity.getExceptions());
        statement.bindString(7, _tmp_1);
        final String _tmp_2 = __cycleShiftsConverter.fromCycleShifts(entity.getCycleShifts());
        statement.bindString(8, _tmp_2);
        statement.bindLong(9, entity.getSortIndex());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__updateAdapterOfTemplate = new EntityDeletionOrUpdateAdapter<Template>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `templates` SET `id` = ?,`name` = ?,`description` = ?,`pattern` = ?,`isBuiltIn` = ?,`sortIndex` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Template entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getDescription());
        statement.bindString(4, entity.getPattern());
        final int _tmp = entity.isBuiltIn() ? 1 : 0;
        statement.bindLong(5, _tmp);
        statement.bindLong(6, entity.getSortIndex());
        statement.bindLong(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllSchedules = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM schedules";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllTemplates = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM templates";
        return _query;
      }
    };
  }

  @Override
  public Object insertSchedule(final Schedule schedule,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSchedule.insertAndReturnId(schedule);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertTemplate(final Template template,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfTemplate.insertAndReturnId(template);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteSchedule(final Schedule schedule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfSchedule.handle(schedule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTemplate(final Template template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfTemplate.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSchedule(final Schedule schedule,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfSchedule.handle(schedule);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTemplate(final Template template,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfTemplate.handle(template);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllSchedules(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllSchedules.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllSchedules.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllTemplates(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllTemplates.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAllTemplates.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Schedule>> getAllSchedules() {
    final String _sql = "SELECT * FROM schedules ORDER BY sortIndex ASC, isActive DESC, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"schedules"}, new Callable<List<Schedule>>() {
      @Override
      @NonNull
      public List<Schedule> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTemplateId = CursorUtil.getColumnIndexOrThrow(_cursor, "templateId");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfExceptions = CursorUtil.getColumnIndexOrThrow(_cursor, "exceptions");
          final int _cursorIndexOfCycleShifts = CursorUtil.getColumnIndexOrThrow(_cursor, "cycleShifts");
          final int _cursorIndexOfSortIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "sortIndex");
          final List<Schedule> _result = new ArrayList<Schedule>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Schedule _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final Integer _tmpTemplateId;
            if (_cursor.isNull(_cursorIndexOfTemplateId)) {
              _tmpTemplateId = null;
            } else {
              _tmpTemplateId = _cursor.getInt(_cursorIndexOfTemplateId);
            }
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final Map<String, String> _tmpExceptions;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfExceptions);
            _tmpExceptions = __exceptionsConverter.toExceptions(_tmp_1);
            final Map<String, Integer> _tmpCycleShifts;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfCycleShifts);
            _tmpCycleShifts = __cycleShiftsConverter.toCycleShifts(_tmp_2);
            final int _tmpSortIndex;
            _tmpSortIndex = _cursor.getInt(_cursorIndexOfSortIndex);
            _item = new Schedule(_tmpId,_tmpName,_tmpColor,_tmpTemplateId,_tmpStartDate,_tmpIsActive,_tmpExceptions,_tmpCycleShifts,_tmpSortIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getScheduleById(final int id, final Continuation<? super Schedule> $completion) {
    final String _sql = "SELECT * FROM schedules WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Schedule>() {
      @Override
      @Nullable
      public Schedule call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfColor = CursorUtil.getColumnIndexOrThrow(_cursor, "color");
          final int _cursorIndexOfTemplateId = CursorUtil.getColumnIndexOrThrow(_cursor, "templateId");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfExceptions = CursorUtil.getColumnIndexOrThrow(_cursor, "exceptions");
          final int _cursorIndexOfCycleShifts = CursorUtil.getColumnIndexOrThrow(_cursor, "cycleShifts");
          final int _cursorIndexOfSortIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "sortIndex");
          final Schedule _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpColor;
            _tmpColor = _cursor.getString(_cursorIndexOfColor);
            final Integer _tmpTemplateId;
            if (_cursor.isNull(_cursorIndexOfTemplateId)) {
              _tmpTemplateId = null;
            } else {
              _tmpTemplateId = _cursor.getInt(_cursorIndexOfTemplateId);
            }
            final String _tmpStartDate;
            _tmpStartDate = _cursor.getString(_cursorIndexOfStartDate);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final Map<String, String> _tmpExceptions;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfExceptions);
            _tmpExceptions = __exceptionsConverter.toExceptions(_tmp_1);
            final Map<String, Integer> _tmpCycleShifts;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfCycleShifts);
            _tmpCycleShifts = __cycleShiftsConverter.toCycleShifts(_tmp_2);
            final int _tmpSortIndex;
            _tmpSortIndex = _cursor.getInt(_cursorIndexOfSortIndex);
            _result = new Schedule(_tmpId,_tmpName,_tmpColor,_tmpTemplateId,_tmpStartDate,_tmpIsActive,_tmpExceptions,_tmpCycleShifts,_tmpSortIndex);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMaxScheduleSortIndex(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(MAX(sortIndex), 0) FROM schedules";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMaxTemplateSortIndex(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(MAX(sortIndex), 0) FROM templates";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Template>> getAllTemplates() {
    final String _sql = "SELECT * FROM templates ORDER BY isBuiltIn DESC, sortIndex ASC, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"templates"}, new Callable<List<Template>>() {
      @Override
      @NonNull
      public List<Template> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfIsBuiltIn = CursorUtil.getColumnIndexOrThrow(_cursor, "isBuiltIn");
          final int _cursorIndexOfSortIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "sortIndex");
          final List<Template> _result = new ArrayList<Template>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Template _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final boolean _tmpIsBuiltIn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBuiltIn);
            _tmpIsBuiltIn = _tmp != 0;
            final int _tmpSortIndex;
            _tmpSortIndex = _cursor.getInt(_cursorIndexOfSortIndex);
            _item = new Template(_tmpId,_tmpName,_tmpDescription,_tmpPattern,_tmpIsBuiltIn,_tmpSortIndex);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getTemplateById(final int id, final Continuation<? super Template> $completion) {
    final String _sql = "SELECT * FROM templates WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Template>() {
      @Override
      @Nullable
      public Template call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfPattern = CursorUtil.getColumnIndexOrThrow(_cursor, "pattern");
          final int _cursorIndexOfIsBuiltIn = CursorUtil.getColumnIndexOrThrow(_cursor, "isBuiltIn");
          final int _cursorIndexOfSortIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "sortIndex");
          final Template _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final String _tmpPattern;
            _tmpPattern = _cursor.getString(_cursorIndexOfPattern);
            final boolean _tmpIsBuiltIn;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsBuiltIn);
            _tmpIsBuiltIn = _tmp != 0;
            final int _tmpSortIndex;
            _tmpSortIndex = _cursor.getInt(_cursorIndexOfSortIndex);
            _result = new Template(_tmpId,_tmpName,_tmpDescription,_tmpPattern,_tmpIsBuiltIn,_tmpSortIndex);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
