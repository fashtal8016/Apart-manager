package com.example.data

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class BuildingRepository(private val database: AppDatabase) {
    private val buildingDao = database.buildingDao()
    private val unitDao = database.unitDao()
    private val chargeDao = database.chargeDao()
    private val notesDao = database.notesDao()
    private val ledgerDao = database.ledgerDao()
    private val bankDao = database.bankDao()
    private val billDao = database.billDao()
    private val accountingDao = database.accountingDao()
    private val settingsDao = database.settingsDao()

    val allBuildings: Flow<List<Building>> = buildingDao.getAllBuildings()
    val settingsFlow: Flow<AppSettings?> = settingsDao.getSettings()

    fun getUnits(buildingId: Long): Flow<List<BuildingUnit>> = unitDao.getUnitsByBuilding(buildingId)
    suspend fun getUnitsList(buildingId: Long): List<BuildingUnit> = unitDao.getUnitsListByBuilding(buildingId)

    fun getCharges(buildingId: Long, year: Int): Flow<List<ChargeRecord>> =
        chargeDao.getChargesForBuilding(buildingId, year)

    suspend fun getChargesList(buildingId: Long, year: Int): List<ChargeRecord> =
        chargeDao.getChargesListForBuilding(buildingId, year)

    fun getAnnualNotes(buildingId: Long, year: Int): Flow<List<UnitAnnualNote>> =
        notesDao.getAnnualNotesForBuilding(buildingId, year)

    suspend fun getAnnualNotesList(buildingId: Long, year: Int): List<UnitAnnualNote> =
        notesDao.getAnnualNotesListForBuilding(buildingId, year)

    fun getMonthNotes(buildingId: Long, year: Int): Flow<List<MonthChargeNote>> =
        notesDao.getMonthNotesForBuilding(buildingId, year)

    suspend fun getMonthNotesList(buildingId: Long, year: Int): List<MonthChargeNote> =
        notesDao.getMonthNotesListForBuilding(buildingId, year)

    fun getLedgers(buildingId: Long): Flow<List<Ledger>> = ledgerDao.getLedgers(buildingId)
    suspend fun getLedgersList(buildingId: Long): List<Ledger> = ledgerDao.getLedgersList(buildingId)

    fun getBankAccounts(buildingId: Long): Flow<List<BankAccount>> = bankDao.getAccounts(buildingId)
    suspend fun getBankAccountsList(buildingId: Long): List<BankAccount> = bankDao.getAccountsList(buildingId)

    fun getTransactions(buildingId: Long): Flow<List<BankTransaction>> = bankDao.getTransactions(buildingId)
    suspend fun getTransactionsList(buildingId: Long): List<BankTransaction> = bankDao.getTransactionsList(buildingId)

    fun getBills(buildingId: Long): Flow<List<UtilityBill>> = billDao.getBills(buildingId)
    suspend fun getBillsList(buildingId: Long): List<UtilityBill> = billDao.getBillsList(buildingId)

    fun getEntries(buildingId: Long): Flow<List<AccountingEntry>> = accountingDao.getEntries(buildingId)

    // Mutations
    suspend fun saveSettings(settings: AppSettings) = settingsDao.saveSettings(settings)

    suspend fun updateCharge(charge: ChargeRecord) = chargeDao.insertOrUpdateCharge(charge)

    suspend fun saveAnnualNote(unitId: Long, year: Int, noteText: String) {
        if (noteText.isBlank()) {
            notesDao.deleteAnnualNote(unitId, year)
        } else {
            notesDao.insertOrUpdateAnnualNote(
                UnitAnnualNote(unitId = unitId, year = year, generalNote = noteText)
            )
        }
    }

    suspend fun saveMonthNote(unitId: Long, year: Int, monthIndex: Int, noteText: String) {
        if (noteText.isBlank()) {
            notesDao.deleteMonthNote(unitId, year, monthIndex)
        } else {
            notesDao.insertOrUpdateMonthNote(
                MonthChargeNote(unitId = unitId, year = year, monthIndex = monthIndex, note = noteText)
            )
        }
    }

    suspend fun insertUnit(unit: BuildingUnit) = unitDao.insertUnit(unit)
    suspend fun updateUnit(unit: BuildingUnit) = unitDao.updateUnit(unit)
    suspend fun deleteUnit(unit: BuildingUnit) = unitDao.deleteUnit(unit)

    suspend fun insertBuilding(building: Building): Long = buildingDao.insertBuilding(building)

    suspend fun insertLedger(ledger: Ledger): Long = ledgerDao.insertLedger(ledger)

    suspend fun insertTransaction(tx: BankTransaction): Long = bankDao.insertTransaction(tx)
    suspend fun updateTransaction(tx: BankTransaction) = bankDao.updateTransaction(tx)

    suspend fun insertBill(bill: UtilityBill): Long = billDao.insertBill(bill)
    suspend fun updateBill(bill: UtilityBill) = billDao.updateBill(bill)

    suspend fun insertAccount(account: BankAccount): Long = bankDao.insertAccount(account)

    suspend fun confirmTransactionAndPost(tx: BankTransaction, buildingId: Long) {
        // Mark transaction confirmed
        bankDao.updateTransaction(tx.copy(isConfirmed = true))

        // If matched to a unit charge, update corresponding charge record or add entry
        if (tx.matchedType == "RESIDENT_CHARGE" && tx.matchedUnitId != null) {
            val unit = unitDao.getUnitById(tx.matchedUnitId)
            if (unit != null) {
                // Post double-entry accounting
                accountingDao.insertEntry(
                    AccountingEntry(
                        buildingId = buildingId,
                        docNumber = System.currentTimeMillis() % 1000000,
                        jalaliDate = tx.jalaliDate,
                        description = "وصول شارژ واحد ${unit.unitNumber} (${unit.residentName}) - رهگیری: ${tx.trackingNumber}",
                        debitAccount = "بانک",
                        creditAccount = "درآمد شارژ",
                        amount = tx.depositAmount,
                        unitId = unit.id,
                        trackingCode = tx.trackingNumber
                    )
                )
            }
        } else if (tx.matchedType == "UTILITY_BILL" && tx.matchedLedgerId != null) {
            val ledger = ledgerDao.getLedgerById(tx.matchedLedgerId)
            val ledgerName = ledger?.name ?: "قبوض خدماتی"
            accountingDao.insertEntry(
                AccountingEntry(
                    buildingId = buildingId,
                    docNumber = System.currentTimeMillis() % 1000000,
                    jalaliDate = tx.jalaliDate,
                    description = "پرداخت هزینه $ledgerName - شرح: ${tx.description}",
                    debitAccount = ledgerName,
                    creditAccount = "بانک",
                    amount = tx.withdrawalAmount,
                    ledgerId = tx.matchedLedgerId,
                    trackingCode = tx.trackingNumber
                )
            )
        }
    }
}
