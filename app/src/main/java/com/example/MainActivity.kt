package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.bank.BankStatementScreen
import com.example.ui.bills.BillsScreen
import com.example.ui.charge.AnnualChargeTableScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.face.FaceSelectionScreen
import com.example.ui.ledger.LedgerScreen
import com.example.ui.report.DepositSummaryScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.KiwiBuildingTheme
import com.example.ui.units.UnitsScreen

enum class MainTab(val title: String, val icon: ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Dashboard),
    CHARGES("جدول شارژ", Icons.Default.TableChart),
    DEPOSITS("خلاصه واریز", Icons.Default.Summarize),
    BANK("بانک و تطبیق", Icons.Default.AccountBalance),
    LEDGERS("دفاتر معین", Icons.Default.ReceiptLong),
    BILLS("قبوض", Icons.Default.Bolt),
    UNITS("واحدها", Icons.Default.Apartment),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var currentTab by remember { mutableStateOf(MainTab.DASHBOARD) }
            var forceShowFaceSelection by remember { mutableStateOf(false) }

            KiwiBuildingTheme(
                faceId = uiState.settings.selectedFace,
                themeMode = uiState.settings.themeMode,
                accentColorHex = uiState.settings.accentColorHex
            ) {
                if (uiState.isLoading || uiState.building == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "در حال آماده‌سازی مدیر ساختمان کیوی...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else if (!uiState.settings.isFaceSelectedOnboarded || forceShowFaceSelection) {
                    // Part 1: Four Visual Faces Selection Screen on first launch!
                    FaceSelectionScreen(
                        currentSelectedId = uiState.settings.selectedFace,
                        onConfirmSelection = { chosenFaceId ->
                            viewModel.selectFace(chosenFaceId)
                            forceShowFaceSelection = false
                        }
                    )
                } else {
                    // Handle system back navigation
                    BackHandler(enabled = currentTab != MainTab.DASHBOARD) {
                        currentTab = MainTab.DASHBOARD
                    }

                    val building = uiState.building!!

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                val tabs = listOf(
                                    MainTab.DASHBOARD,
                                    MainTab.CHARGES,
                                    MainTab.DEPOSITS,
                                    MainTab.BANK,
                                    MainTab.LEDGERS,
                                    MainTab.SETTINGS
                                )
                                tabs.forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = { currentTab = tab },
                                        icon = {
                                            Icon(
                                                tab.icon,
                                                contentDescription = tab.title,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                tab.title,
                                                fontSize = 10.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentTab) {
                                MainTab.DASHBOARD -> DashboardScreen(
                                    building = building,
                                    units = uiState.units,
                                    charges = uiState.charges,
                                    bankAccounts = uiState.bankAccounts,
                                    transactions = uiState.transactions,
                                    bills = uiState.bills,
                                    ledgers = uiState.ledgers,
                                    onNavigateToCharges = { currentTab = MainTab.CHARGES },
                                    onNavigateToBankStatement = { currentTab = MainTab.BANK },
                                    onNavigateToDepositSummary = { currentTab = MainTab.DEPOSITS },
                                    onNavigateToLedgers = { currentTab = MainTab.LEDGERS },
                                    onNavigateToBills = { currentTab = MainTab.BILLS },
                                    onNavigateToUnits = { currentTab = MainTab.UNITS }
                                )
                                MainTab.CHARGES -> AnnualChargeTableScreen(
                                    building = building,
                                    units = uiState.units,
                                    charges = uiState.charges,
                                    annualNotes = uiState.annualNotes,
                                    monthNotes = uiState.monthNotes,
                                    onSaveChargeRecord = { viewModel.saveChargeRecord(it) },
                                    onSaveAnnualNote = { uid, yr, note -> viewModel.saveAnnualNote(uid, yr, note) },
                                    onSaveMonthNote = { uid, yr, m, note -> viewModel.saveMonthNote(uid, yr, m, note) }
                                )
                                MainTab.DEPOSITS -> DepositSummaryScreen(
                                    building = building,
                                    units = uiState.units,
                                    charges = uiState.charges
                                )
                                MainTab.BANK -> BankStatementScreen(
                                    building = building,
                                    bankAccounts = uiState.bankAccounts,
                                    transactions = uiState.transactions,
                                    units = uiState.units,
                                    ledgers = uiState.ledgers,
                                    onAddTransaction = { viewModel.addTransaction(it) },
                                    onConfirmTransaction = { viewModel.confirmTransaction(it) },
                                    onAddAccount = {}
                                )
                                MainTab.LEDGERS -> LedgerScreen(
                                    building = building,
                                    ledgers = uiState.ledgers,
                                    onAddLedger = { viewModel.addLedger(it) }
                                )
                                MainTab.BILLS -> BillsScreen(
                                    building = building,
                                    bills = uiState.bills,
                                    onAddBill = { viewModel.addBill(it) },
                                    onUpdateBill = { viewModel.updateBill(it) }
                                )
                                MainTab.UNITS -> UnitsScreen(
                                    building = building,
                                    units = uiState.units,
                                    charges = uiState.charges,
                                    onAddUnit = { viewModel.addUnit(it) },
                                    onUpdateUnit = { viewModel.updateUnit(it) },
                                    onDeleteUnit = { viewModel.deleteUnit(it) }
                                )
                                MainTab.SETTINGS -> SettingsScreen(
                                    building = building,
                                    settings = uiState.settings,
                                    onUpdateSettings = { viewModel.updateSettings(it) },
                                    onOpenFaceSelection = { forceShowFaceSelection = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
