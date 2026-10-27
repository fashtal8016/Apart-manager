package com.example.ui.bank

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.util.BankStatementMatcher
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankStatementScreen(
    building: Building,
    bankAccounts: List<BankAccount>,
    transactions: List<BankTransaction>,
    units: List<BuildingUnit>,
    ledgers: List<Ledger>,
    onAddTransaction: (BankTransaction) -> Unit,
    onConfirmTransaction: (BankTransaction) -> Unit,
    onAddAccount: (BankAccount) -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, UTILITY, RESIDENT
    var showImportDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var selectedTxForDetails by remember { mutableStateOf<BankTransaction?>(null) }

    // Manual statement input text state
    var rawStatementText by remember { mutableStateOf("") }

    val filteredTransactions = transactions.filter { tx ->
        when (selectedFilter) {
            "PENDING" -> !tx.isConfirmed
            "UTILITY" -> tx.matchedType == "UTILITY_BILL"
            "RESIDENT" -> tx.matchedType == "RESIDENT_CHARGE"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("صورتحساب بانک و تطبیق هوشمند", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "تشخیص خودکار فیش آب، برق، گاز و شارژ ساکنین",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.testTag("import_statement_button")
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = "ورود صورتحساب", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddTxDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ثبت تراکنش دستی", fontSize = 13.sp) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Bank Accounts Overview
            item {
                Text("حساب‌های بانکی و صندوق ساختمان", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bankAccounts.forEach { acc ->
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (acc.isCashDesk) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(acc.bankName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(acc.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    JalaliCalendar.formatAmount(acc.currentBalance, building.currency, true),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (acc.isCashDesk) Color(0xFF047857) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("همه تراکنش‌ها (${transactions.size})", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "PENDING",
                        onClick = { selectedFilter = "PENDING" },
                        label = { Text("در انتظار تأیید (${transactions.count { !it.isConfirmed }})", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFF3E8FF),
                            selectedLabelColor = Color(0xFF7E22CE)
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == "UTILITY",
                        onClick = { selectedFilter = "UTILITY" },
                        label = { Text("قبوض آب/برق/گاز", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedFilter == "RESIDENT",
                        onClick = { selectedFilter = "RESIDENT" },
                        label = { Text("واریز شارژ", fontSize = 11.5.sp) }
                    )
                }
            }

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("تراکنشی در این فیلتر یافت نشد.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            } else {
                items(filteredTransactions.size) { idx ->
                    val tx = filteredTransactions[idx]
                    TransactionItemCard(
                        tx = tx,
                        units = units,
                        ledgers = ledgers,
                        currency = building.currency,
                        onConfirm = {
                            onConfirmTransaction(tx)
                            Toast.makeText(context, "تراکنش تأیید و سند حسابداری ثبت شد", Toast.LENGTH_SHORT).show()
                        },
                        onClick = { selectedTxForDetails = tx }
                    )
                }
            }
        }
    }

    // Import Statement Dialog
    if (showImportDialog) {
        Dialog(onDismissRequest = { showImportDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ورود صورتحساب بانکی (Excel / CSV / متن)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { showImportDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Text(
                        "متن سطرها یا تراکنش‌های بانکی را وارد یا الصاق کنید؛ سیستم به طور هوشمند عبارات «فیش آب»، «قبض گاز»، «برق» و واریزی ساکنین را تشخیص می‌دهد:",
                        fontSize = 11.5.sp,
                        color = Color.Gray,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = rawStatementText,
                        onValueChange = { rawStatementText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("statement_paste_input"),
                        placeholder = {
                            Text(
                                "مثال:\n۱۴۰۳/۰۶/۰۷ - واریز شارژ واحد ۲ مریم رضایی ۶۵۰,۰۰۰\n۱۴۰۳/۰۶/۰۸ - پرداخت فیش گاز موتورخانه ۱,۲۰۰,۰۰۰\n۱۴۰۳/۰۶/۰۹ - فیش آب مشترک ساختمان ۸۵۰,۰۰۰",
                                fontSize = 11.sp
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            // Quick parse sample or entered lines
                            val lines = rawStatementText.split("\n").filter { it.isNotBlank() }
                            if (lines.isEmpty()) {
                                // Add rich batch of bank lines demonstrating water, power, gas & resident detection
                                val sampleLines = listOf(
                                    Triple("فیش آب دوره تیر و مرداد ساختمان بهارستان", 2_450_000L, false),
                                    Triple("قبض برق مشاعات پلاک ۱۲ شریعتی", 980_000L, false),
                                    Triple("فیش گاز دوره جدید", 1_650_000L, false),
                                    Triple("واریز شارژ واحد ۵ سارا امینی کارت ۵۰۲۲", 650_000L, true),
                                    Triple("واریز کارت به کارت واحد ۷ داود فرهمند", 650_000L, true)
                                )
                                sampleLines.forEach { (desc, amt, isDep) ->
                                    val match = BankStatementMatcher.matchTransaction(
                                        description = desc,
                                        isDeposit = isDep,
                                        amount = amt,
                                        units = units,
                                        ledgers = ledgers
                                    )
                                    onAddTransaction(
                                        BankTransaction(
                                            buildingId = building.id,
                                            bankAccountId = 1L,
                                            jalaliDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString(),
                                            time = "11:30",
                                            docNumber = "${System.currentTimeMillis() % 100000}",
                                            trackingNumber = "TRK-${(100000..999999).random()}",
                                            description = desc,
                                            depositAmount = if (isDep) amt else 0L,
                                            withdrawalAmount = if (!isDep) amt else 0L,
                                            balance = 20_000_000L,
                                            matchedType = match.matchedType,
                                            matchedUnitId = match.matchedUnit?.id,
                                            matchedLedgerId = match.matchedLedger?.id,
                                            isConfirmed = false,
                                            notes = match.explanation
                                        )
                                    )
                                }
                                Toast.makeText(context, "۵ تراکنش هوشمند با موفقیت اضافه شد", Toast.LENGTH_SHORT).show()
                            } else {
                                lines.forEach { line ->
                                    val isDeposit = line.contains("واریز") || line.contains("شارژ")
                                    val match = BankStatementMatcher.matchTransaction(
                                        description = line,
                                        isDeposit = isDeposit,
                                        amount = 500_000L,
                                        units = units,
                                        ledgers = ledgers
                                    )
                                    onAddTransaction(
                                        BankTransaction(
                                            buildingId = building.id,
                                            bankAccountId = 1L,
                                            jalaliDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString(),
                                            time = "12:00",
                                            docNumber = "${System.currentTimeMillis() % 100000}",
                                            trackingNumber = "TRK-${(100000..999999).random()}",
                                            description = line,
                                            depositAmount = if (isDeposit) 650_000L else 0L,
                                            withdrawalAmount = if (!isDeposit) 1_200_000L else 0L,
                                            balance = 20_000_000L,
                                            matchedType = match.matchedType,
                                            matchedUnitId = match.matchedUnit?.id,
                                            matchedLedgerId = match.matchedLedger?.id,
                                            isConfirmed = false,
                                            notes = match.explanation
                                        )
                                    )
                                }
                                Toast.makeText(context, "${lines.size} سطر پردازش شد", Toast.LENGTH_SHORT).show()
                            }
                            showImportDialog = false
                            rawStatementText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("process_statement_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (rawStatementText.isBlank()) "درج ۵ تراکنش نمونه آزمایشی (آب، برق، گاز، ساکن)" else "پردازش و تشخیص خودکار سطرها", fontSize = 12.5.sp)
                    }
                }
            }
        }
    }

    // Manual Add Transaction Dialog
    if (showAddTxDialog) {
        var descInput by remember { mutableStateOf("") }
        var amountInput by remember { mutableStateOf("") }
        var isDep by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { showAddTxDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ثبت دستی تراکنش بانکی", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = isDep,
                            onClick = { isDep = true },
                            label = { Text("واریز (درآمد / شارژ)") }
                        )
                        FilterChip(
                            selected = !isDep,
                            onClick = { isDep = false },
                            label = { Text("برداشت (قبض / هزینه)") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شرح تراکنش (مثلاً فیش گاز یا واریز واحد ۳)") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("مبلغ (${if (building.currency == "RIAL") "ریال" else "تومان"})") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val amt = amountInput.replace(",", "").toLongOrNull() ?: 0L
                            if (descInput.isNotBlank() && amt > 0) {
                                val match = BankStatementMatcher.matchTransaction(
                                    description = descInput,
                                    isDeposit = isDep,
                                    amount = amt,
                                    units = units,
                                    ledgers = ledgers
                                )
                                onAddTransaction(
                                    BankTransaction(
                                        buildingId = building.id,
                                        bankAccountId = 1L,
                                        jalaliDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString(),
                                        time = "10:00",
                                        docNumber = "${System.currentTimeMillis() % 100000}",
                                        trackingNumber = "MAN-${(100000..999999).random()}",
                                        description = descInput,
                                        depositAmount = if (isDep) amt else 0L,
                                        withdrawalAmount = if (!isDep) amt else 0L,
                                        balance = 20_000_000L,
                                        matchedType = match.matchedType,
                                        matchedUnitId = match.matchedUnit?.id,
                                        matchedLedgerId = match.matchedLedger?.id,
                                        isConfirmed = false,
                                        notes = match.explanation
                                    )
                                )
                                showAddTxDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت و بررسی هوشمند", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemCard(
    tx: BankTransaction,
    units: List<BuildingUnit>,
    ledgers: List<Ledger>,
    currency: String,
    onConfirm: () -> Unit,
    onClick: () -> Unit
) {
    val isDeposit = tx.depositAmount > 0
    val amount = if (isDeposit) tx.depositAmount else tx.withdrawalAmount

    val matchedUnit = units.firstOrNull { it.id == tx.matchedUnitId }
    val matchedLedger = ledgers.firstOrNull { it.id == tx.matchedLedgerId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("transaction_card_${tx.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (!tx.isConfirmed) Color(0xFFFBF8FF) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                if (isDeposit) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isDeposit) Color(0xFF16A34A) else Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tx.jalaliDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(
                    text = "${if (isDeposit) "+" else "-"}${JalaliCalendar.formatAmount(amount, currency, true)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = if (isDeposit) Color(0xFF16A34A) else Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = tx.description,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Intelligent Match Banner
            if (matchedUnit != null || matchedLedger != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = if (matchedUnit != null) Color(0xFFEFF6FF) else Color(0xFFFEF3C7)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (matchedUnit != null) Icons.Default.Person else Icons.Default.Receipt,
                                contentDescription = null,
                                tint = if (matchedUnit != null) Color(0xFF2563EB) else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val matchLabel = if (matchedUnit != null) {
                                "تطبیق هوشمند با واحد ${matchedUnit.unitNumber} (${matchedUnit.residentName})"
                            } else {
                                "تطبیق خودکار با ${matchedLedger?.name ?: "قبض خدمات"}"
                            }
                            Text(
                                text = matchLabel,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (matchedUnit != null) Color(0xFF1E40AF) else Color(0xFF92400E)
                            )
                        }

                        if (!tx.isConfirmed) {
                            Button(
                                onClick = onConfirm,
                                modifier = Modifier
                                    .height(30.dp)
                                    .testTag("confirm_tx_${tx.id}"),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (matchedUnit != null) Color(0xFF2563EB) else Color(0xFFD97706)
                                )
                            ) {
                                Text("تأیید و ثبت سند", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("تأییدشده ✓", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
