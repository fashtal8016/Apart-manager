package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Building::class,
        BuildingUnit::class,
        UnitAnnualNote::class,
        MonthChargeNote::class,
        ChargeRecord::class,
        Ledger::class,
        BankAccount::class,
        BankTransaction::class,
        UtilityBill::class,
        AccountingEntry::class,
        AppSettings::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun buildingDao(): BuildingDao
    abstract fun unitDao(): UnitDao
    abstract fun chargeDao(): ChargeDao
    abstract fun notesDao(): NotesDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun bankDao(): BankDao
    abstract fun billDao(): BillDao
    abstract fun accountingDao(): AccountingDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kiwi_building_manager.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            SampleDataLoader.seedDatabase(database)
        }
    }
}
