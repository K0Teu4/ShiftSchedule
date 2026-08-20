package com.shiftschedule.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shiftschedule.app.data.model.CycleShiftsConverter
import com.shiftschedule.app.data.model.ExceptionsConverter
import com.shiftschedule.app.data.model.Schedule
import com.shiftschedule.app.data.model.Template
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Schedule::class, Template::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(ExceptionsConverter::class, CycleShiftsConverter::class)
abstract class ShiftDatabase : RoomDatabase() {
    abstract fun shiftDao(): ShiftDao

    companion object {
        @Volatile
        private var INSTANCE: ShiftDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE templates ADD COLUMN sortIndex INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE schedules ADD COLUMN hour_rate INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE schedules ADD COLUMN day_hours INTEGER NOT NULL DEFAULT 8")
                database.execSQL("ALTER TABLE schedules ADD COLUMN night_hours INTEGER NOT NULL DEFAULT 16")
            }
        }

        fun getDatabase(context: Context): ShiftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShiftDatabase::class.java,
                    "shift_schedule_database"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.shiftDao()?.let { dao ->
                                Template.getBuiltInTemplates().forEach { dao.insertTemplate(it) }
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
