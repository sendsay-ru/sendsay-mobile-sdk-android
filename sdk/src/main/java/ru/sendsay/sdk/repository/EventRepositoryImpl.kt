package ru.sendsay.sdk.repository

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.sendsay.sdk.database.SendsayDatabase
import ru.sendsay.sdk.models.ExportedEvent
import ru.sendsay.sdk.util.Logger

internal open class EventRepositoryImpl(
    context: Context
) : EventRepository {

    val database: SendsayDatabase by lazy {
        SendsayDatabase.getInstance(context)
    }

    override fun count(): Int {
        return database.count()
    }

    override fun all(): List<ExportedEvent> {
        return database.all()
    }

    override fun add(item: ExportedEvent) {
        database.add(item)
    }

    override fun update(item: ExportedEvent) {
        database.update(item)
    }

    override fun get(id: String): ExportedEvent? {
        return database.get(id)
    }

    override fun remove(id: String) {
        database.remove(id)
    }

    override fun clear() {
        database.clear()
    }

    override fun onIntegrationStopped() {
        if (!database.isOpen) {
            // after init, first query is required to open connection
            try {
                database.count()
            } catch (e: IllegalStateException) {
                Logger.e(this, "Unable to re-open database, clearing may be incomplete", e)
            }
        }
        if (database.isOpen) {
            try {
                database.clear()
            } catch (e: IllegalStateException) {
                Logger.e(this, "Unable to clear already cleared and closed database")
            }
            database.close()
        }
    }
}
