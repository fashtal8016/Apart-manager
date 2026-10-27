package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings ORDER BY id ASC")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM buildings WHERE id = :id LIMIT 1")
    suspend fun getBuildingById(id: Long): Building?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: Building): Long

    @Update
    suspend fun updateBuilding(building: Building)

    @Delete
    suspend fun deleteBuilding(building: Building)
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE buildingId = :buildingId ORDER BY id ASC")
    fun getUnitsByBuilding(buildingId: Long): Flow<List<BuildingUnit>>

    @Query("SELECT * FROM units WHERE buildingId = :buildingId ORDER BY id ASC")
    suspend fun getUnitsListByBuilding(buildingId: Long): List<BuildingUnit>

    @Query("SELECT * FROM units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Long): BuildingUnit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: BuildingUnit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<BuildingUnit>)

    @Update
    suspend fun updateUnit(unit: BuildingUnit)

    @Delete
    suspend fun deleteUnit(unit: BuildingUnit)
}

@Dao
interface ChargeDao {
    @Query("""
        SELECT cr.* FROM charge_records cr
        INNER JOIN units u ON cr.unitId = u.id
        WHERE u.buildingId = :buildingId AND cr.year = :year
    """)
    fun getChargesForBuilding(buildingId: Long, year: Int): Flow<List<ChargeRecord>>

    @Query("""
        SELECT cr.* FROM charge_records cr
        INNER JOIN units u ON cr.unitId = u.id
        WHERE u.buildingId = :buildingId AND cr.year = :year
    """)
    suspend fun getChargesListForBuilding(buildingId: Long, year: Int): List<ChargeRecord>

    @Query("SELECT * FROM charge_records WHERE unitId = :unitId AND year = :year ORDER BY monthIndex ASC")
    fun getChargesForUnit(unitId: Long, year: Int): Flow<List<ChargeRecord>>

    @Query("SELECT * FROM charge_records WHERE unitId = :unitId AND year = :year AND monthIndex = :monthIndex LIMIT 1")
    suspend fun getCharge(unitId: Long, year: Int, monthIndex: Int): ChargeRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCharge(charge: ChargeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCharges(charges: List<ChargeRecord>)
}

@Dao
interface NotesDao {
    @Query("""
        SELECT n.* FROM unit_annual_notes n
        INNER JOIN units u ON n.unitId = u.id
        WHERE u.buildingId = :buildingId AND n.year = :year
    """)
    fun getAnnualNotesForBuilding(buildingId: Long, year: Int): Flow<List<UnitAnnualNote>>

    @Query("""
        SELECT n.* FROM unit_annual_notes n
        INNER JOIN units u ON n.unitId = u.id
        WHERE u.buildingId = :buildingId AND n.year = :year
    """)
    suspend fun getAnnualNotesListForBuilding(buildingId: Long, year: Int): List<UnitAnnualNote>

    @Query("SELECT * FROM unit_annual_notes WHERE unitId = :unitId AND year = :year LIMIT 1")
    suspend fun getAnnualNote(unitId: Long, year: Int): UnitAnnualNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAnnualNote(note: UnitAnnualNote)

    @Query("DELETE FROM unit_annual_notes WHERE unitId = :unitId AND year = :year")
    suspend fun deleteAnnualNote(unitId: Long, year: Int)

    // Monthly notes
    @Query("""
        SELECT mn.* FROM month_charge_notes mn
        INNER JOIN units u ON mn.unitId = u.id
        WHERE u.buildingId = :buildingId AND mn.year = :year
    """)
    fun getMonthNotesForBuilding(buildingId: Long, year: Int): Flow<List<MonthChargeNote>>

    @Query("""
        SELECT mn.* FROM month_charge_notes mn
        INNER JOIN units u ON mn.unitId = u.id
        WHERE u.buildingId = :buildingId AND mn.year = :year
    """)
    suspend fun getMonthNotesListForBuilding(buildingId: Long, year: Int): List<MonthChargeNote>

    @Query("SELECT * FROM month_charge_notes WHERE unitId = :unitId AND year = :year AND monthIndex = :monthIndex LIMIT 1")
    suspend fun getMonthNote(unitId: Long, year: Int, monthIndex: Int): MonthChargeNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMonthNote(note: MonthChargeNote)

    @Query("DELETE FROM month_charge_notes WHERE unitId = :unitId AND year = :year AND monthIndex = :monthIndex")
    suspend fun deleteMonthNote(unitId: Long, year: Int, monthIndex: Int)
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM ledgers WHERE buildingId = :buildingId ORDER BY id ASC")
    fun getLedgers(buildingId: Long): Flow<List<Ledger>>

    @Query("SELECT * FROM ledgers WHERE buildingId = :buildingId ORDER BY id ASC")
    suspend fun getLedgersList(buildingId: Long): List<Ledger>

    @Query("SELECT * FROM ledgers WHERE id = :id LIMIT 1")
    suspend fun getLedgerById(id: Long): Ledger?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedger(ledger: Ledger): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgers(ledgers: List<Ledger>)

    @Update
    suspend fun updateLedger(ledger: Ledger)

    @Delete
    suspend fun deleteLedger(ledger: Ledger)
}

@Dao
interface BankDao {
    @Query("SELECT * FROM bank_accounts WHERE buildingId = :buildingId ORDER BY isCashDesk ASC, id ASC")
    fun getAccounts(buildingId: Long): Flow<List<BankAccount>>

    @Query("SELECT * FROM bank_accounts WHERE buildingId = :buildingId ORDER BY isCashDesk ASC, id ASC")
    suspend fun getAccountsList(buildingId: Long): List<BankAccount>

    @Query("SELECT * FROM bank_accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: Long): BankAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: BankAccount): Long

    @Update
    suspend fun updateAccount(account: BankAccount)

    @Delete
    suspend fun deleteAccount(account: BankAccount)

    // Transactions
    @Query("SELECT * FROM bank_transactions WHERE buildingId = :buildingId ORDER BY id DESC")
    fun getTransactions(buildingId: Long): Flow<List<BankTransaction>>

    @Query("SELECT * FROM bank_transactions WHERE buildingId = :buildingId ORDER BY id DESC")
    suspend fun getTransactionsList(buildingId: Long): List<BankTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: BankTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<BankTransaction>)

    @Update
    suspend fun updateTransaction(transaction: BankTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: BankTransaction)
}

@Dao
interface BillDao {
    @Query("SELECT * FROM utility_bills WHERE buildingId = :buildingId ORDER BY id DESC")
    fun getBills(buildingId: Long): Flow<List<UtilityBill>>

    @Query("SELECT * FROM utility_bills WHERE buildingId = :buildingId ORDER BY id DESC")
    suspend fun getBillsList(buildingId: Long): List<UtilityBill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: UtilityBill): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<UtilityBill>)

    @Update
    suspend fun updateBill(bill: UtilityBill)

    @Delete
    suspend fun deleteBill(bill: UtilityBill)
}

@Dao
interface AccountingDao {
    @Query("SELECT * FROM accounting_entries WHERE buildingId = :buildingId ORDER BY id DESC")
    fun getEntries(buildingId: Long): Flow<List<AccountingEntry>>

    @Query("SELECT * FROM accounting_entries WHERE buildingId = :buildingId ORDER BY id DESC")
    suspend fun getEntriesList(buildingId: Long): List<AccountingEntry>

    @Query("SELECT * FROM accounting_entries WHERE buildingId = :buildingId AND unitId = :unitId ORDER BY id ASC")
    fun getEntriesForUnit(buildingId: Long, unitId: Long): Flow<List<AccountingEntry>>

    @Query("SELECT * FROM accounting_entries WHERE buildingId = :buildingId AND ledgerId = :ledgerId ORDER BY id ASC")
    fun getEntriesForLedger(buildingId: Long, ledgerId: Long): Flow<List<AccountingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AccountingEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<AccountingEntry>)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: AppSettings)
}
