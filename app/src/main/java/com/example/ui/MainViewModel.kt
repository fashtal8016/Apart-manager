package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BuildingRepository
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = true,
    val building: Building? = null,
    val units: List<BuildingUnit> = emptyList(),
    val charges: List<ChargeRecord> = emptyList(),
    val annualNotes: List<UnitAnnualNote> = emptyList(),
    val monthNotes: List<MonthChargeNote> = emptyList(),
    val ledgers: List<Ledger> = emptyList(),
    val bankAccounts: List<BankAccount> = emptyList(),
    val transactions: List<BankTransaction> = emptyList(),
    val bills: List<UtilityBill> = emptyList(),
    val settings: AppSettings = AppSettings()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = BuildingRepository(database)
    val preferences = com.example.util.KiwiPreferences(application)

    private val _uiState = MutableStateFlow(
        MainUiState(
            settings = AppSettings(
                selectedFace = preferences.getSelectedFace(),
                isFaceSelectedOnboarded = preferences.isFaceOnboarded()
            )
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if DB needs seed
            val existing = repository.getUnitsList(1L)
            if (existing.isEmpty()) {
                AppDatabase.populateInitialData(database)
            }

            // Combine buildings & settings
            combine(
                repository.allBuildings,
                repository.settingsFlow
            ) { buildings, settings ->
                val activeBld = buildings.firstOrNull { it.id == (settings?.activeBuildingId ?: 1L) }
                    ?: buildings.firstOrNull()
                Pair(activeBld, settings ?: AppSettings())
            }.collectLatest { (bld, sett) ->
                if (bld != null) {
                    val bldId = bld.id
                    val year = bld.fiscalYear

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            building = bld,
                            settings = sett
                        )
                    }

                    // Launch listeners for each reactive collection
                    launch {
                        repository.getUnits(bldId).collectLatest { u ->
                            _uiState.update { it.copy(units = u) }
                        }
                    }
                    launch {
                        repository.getCharges(bldId, year).collectLatest { c ->
                            _uiState.update { it.copy(charges = c) }
                        }
                    }
                    launch {
                        repository.getAnnualNotes(bldId, year).collectLatest { an ->
                            _uiState.update { it.copy(annualNotes = an) }
                        }
                    }
                    launch {
                        repository.getMonthNotes(bldId, year).collectLatest { mn ->
                            _uiState.update { it.copy(monthNotes = mn) }
                        }
                    }
                    launch {
                        repository.getLedgers(bldId).collectLatest { l ->
                            _uiState.update { it.copy(ledgers = l) }
                        }
                    }
                    launch {
                        repository.getBankAccounts(bldId).collectLatest { ba ->
                            _uiState.update { it.copy(bankAccounts = ba) }
                        }
                    }
                    launch {
                        repository.getTransactions(bldId).collectLatest { t ->
                            _uiState.update { it.copy(transactions = t) }
                        }
                    }
                    launch {
                        repository.getBills(bldId).collectLatest { bi ->
                            _uiState.update { it.copy(bills = bi) }
                        }
                    }
                }
            }
        }
    }

    fun selectFace(faceId: Int) {
        preferences.saveSelectedFace(faceId)
        viewModelScope.launch(Dispatchers.IO) {
            val current = _uiState.value.settings
            val updated = current.copy(
                selectedFace = faceId,
                isFaceSelectedOnboarded = true
            )
            repository.saveSettings(updated)
            _uiState.update { it.copy(settings = updated) }
        }
    }

    fun updateSettings(settings: AppSettings) {
        preferences.saveSelectedFace(settings.selectedFace)
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings)
            _uiState.update { it.copy(settings = settings) }
        }
    }

    fun saveChargeRecord(charge: ChargeRecord) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCharge(charge)
        }
    }

    fun saveAnnualNote(unitId: Long, year: Int, noteText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveAnnualNote(unitId, year, noteText)
        }
    }

    fun saveMonthNote(unitId: Long, year: Int, monthIndex: Int, noteText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMonthNote(unitId, year, monthIndex, noteText)
        }
    }

    fun addTransaction(tx: BankTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(tx)
        }
    }

    fun confirmTransaction(tx: BankTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            val buildingId = _uiState.value.building?.id ?: 1L
            repository.confirmTransactionAndPost(tx, buildingId)
        }
    }

    fun addLedger(ledger: Ledger) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertLedger(ledger)
        }
    }

    fun addBill(bill: UtilityBill) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBill(bill)
        }
    }

    fun updateBill(bill: UtilityBill) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateBill(bill)
        }
    }

    fun addUnit(unit: BuildingUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertUnit(unit)
        }
    }

    fun updateUnit(unit: BuildingUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUnit(unit)
        }
    }

    fun deleteUnit(unit: BuildingUnit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUnit(unit)
        }
    }
}
