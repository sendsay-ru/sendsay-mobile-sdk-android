package com.sendsay.sdk.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sendsay.sdk.models.ExportedEvent
import kotlin.collections.forEach
import com.sendsay.sdk.util.Logger
import kotlin.text.clear
import kotlin.text.get

@Database(entities = [ExportedEvent::class], version = 2)
@TypeConverters(Converters::class)
internal abstract class SendsayDatabase : RoomDatabase() {

    abstract fun exportedEventDao(): ExportedEventDao

    fun all(): List<ExportedEvent> {
        return exportedEventDao().all()
    }
    fun count(): Int {
        return exportedEventDao().count()
    }
    fun add(item: ExportedEvent) {
        exportedEventDao().add(item)
    }

    fun update(item: ExportedEvent) {
        exportedEventDao().update(item)
    }

    fun get(id: String): ExportedEvent? {
        return exportedEventDao().get(id)
    }
    fun remove(id: String) {
        exportedEventDao().delete(id)
    }
    fun clear() {
        exportedEventDao().clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: SendsayDatabase? = null

        fun getInstance(context: Context): SendsayDatabase {
            if (INSTANCE == null || !INSTANCE!!.isOpen) {
                synchronized(this) {
                    if (INSTANCE == null || !INSTANCE!!.isOpen) {
                        INSTANCE = buildDatabase(context)
                    }
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): SendsayDatabase {
            val databaseBuilder = Room.databaseBuilder(
                context,
                SendsayDatabase::class.java,
                "SendsayEventDatabase"
            )
            databaseBuilder.enableMultiInstanceInvalidation()
            databaseBuilder.allowMainThreadQueries()
            databaseMigrations().forEach { migration ->
                databaseBuilder.addMigrations(migration)
            }
            val database = databaseBuilder.build()
            try {
                database.count()
            } catch (e: Exception) {
                Logger.e(this, "Error occurred while init-opening database", e)
            }
            return database
        }

        private fun databaseMigrations(): List<Migration> {
            val migration1to2 = object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE exported_event ADD COLUMN sdk_event_type TEXT")
                }
            }
            return listOf(migration1to2)
        }
    }
}
