package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    building: Building,
    units: List<BuildingUnit>,
    charges: List<ChargeRecord>,
    bankAccounts: List<BankAccount>,
    transactions: List<BankTransaction>,
    bills: List<UtilityBill>,
    ledgers: List<Ledger>,
    onNavigateToCharges: () -> Unit,
    onNavigateToBankStatement: () -> Unit,
    onNavigateToDepositSummary: () -> Unit,
    onNavigateToLedgers: () -> Unit,
    onNavigateToBills: () -> Unit,
    onNavigateToUnits: () -> Unit
) {
    val currentJalali = JalaliCalendar.getCurrentJalaliDate()
    val currentMonthIdx = currentJalali.month.coerceIn(1, 12)
    val currentMonthName = JalaliCalendar.MONTH_NAMES[currentMonthIdx - 1]

    // Calculations
    val totalUnits = units.size
    val occupiedUnits = units.count { it.residencyStatus != "VACANT" }
    val vacantUnits = totalUnits - occupiedUnits

    val currentMonthCharges = charges.filter { it.monthIndex == currentMonthIdx }
    val approvedTargetMonth = currentMonthCharges.sumOf { it.targetAmount }
    val collectedChargeMonth = currentMonthCharges.sumOf { it.paidAmount }

    // Total debt
    val chargeMap = charges.groupBy { it.unitId }
    val totalDebtAll = units.sumOf { u ->
        val uCharges = chargeMap[u.id] ?: emptyList()
        val totalPaid = uCharges.sumOf { it.paidAmount }
        val totalTarget = uCharges.sumOf { it.targetAmount }
        (totalTarget - totalPaid).coerceAtLeast(0L) + u.initialDebt
    }

    val bankTotalBalance = bankAccounts.filter { !it.isCashDesk }.sumOf { it.currentBalance }
    val cashBalance = bankAccounts.filter { it.isCashDesk }.sumOf { it.currentBalance }

    val unpaidBills = bills.filter { it.status == "UNPAID" }
    val unpaidBillsSum = unpaidBills.sumOf { it.amount }

    val unconfirmedTransactions = transactions.filter { !it.isConfirmed }

    val debtorUnits = units.filter { u ->
        val uCharges = chargeMap[u.id] ?: emptyList()
        val totalPaid = uCharges.sumOf { it.paidAmount }
        val totalTarget = uCharges.sumOf { it.targetAmount }
        ((totalTarget - totalPaid).coerceAtLeast(0L) + u.initialDebt) > 0L
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(building.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "${currentJalali.year} $currentMonthName | ${building.managerName}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToUnits) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("$totalUnits", color = Color.White, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.Apartment, contentDescription = "واحدها")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // Quick Action Buttons Row
            item {
                Text("دسترسی سریع عملیات روزمره", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionButton(
                            title = "جدول سالانه شارژ",
                            icon = Icons.Default.TableChart,
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToCharges
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "خلاصه واریز واحدها",
                            icon = Icons.Default.Summarize,
                            color = Color(0xFF0D9488),
                            onClick = onNavigateToDepositSummary
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "ورود صورتحساب بانک",
                            icon = Icons.Default.AccountBalance,
                            color = Color(0xFF7C3AED),
                            onClick = onNavigateToBankStatement
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "دفاتر معین و آب/برق/گاز",
                            icon = Icons.Default.ReceiptLong,
                            color = Color(0xFFEA580C),
                            onClick = onNavigateToLedgers
                        )
                    }
                    item {
                        QuickActionButton(
                            title = "قبوض و هزینه‌ها",
                            icon = Icons.Default.Bolt,
                            color = Color(0xFFDC2626),
                            onClick = onNavigateToBills
                        )
                    }
                }
            }

            // Top Stat Cards: Bank & Cash
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "موجودی حساب‌های بانکی",
                        value = JalaliCalendar.formatAmount(bankTotalBalance, building.currency, true),
                        icon = Icons.Default.AccountBalance,
                        iconTint = MaterialTheme.colorScheme.primary,
                        bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        onClick = onNavigateToBankStatement
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "موجودی صندوق / تنخواه",
                        value = JalaliCalendar.formatAmount(cashBalance, building.currency, true),
                        icon = Icons.Default.Savings,
                        iconTint = Color(0xFF059669),
                        bgColor = Color(0xFFDCFCE7).copy(alpha = 0.5f),
                        onClick = onNavigateToBankStatement
                    )
                }
            }

            // Current Month Charge & Debt
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "وصول شارژ $currentMonthName",
                        value = JalaliCalendar.formatAmount(collectedChargeMonth, building.currency, true),
                        subtitle = "از کل مصوب: ${JalaliCalendar.formatAmount(approvedTargetMonth, building.currency, true)}",
                        icon = Icons.Default.CheckCircle,
                        iconTint = Color(0xFF16A34A),
                        bgColor = Color(0xFFECFDF5),
                        onClick = onNavigateToCharges
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "مجموع بدهی ساکنین",
                        value = JalaliCalendar.formatAmount(totalDebtAll, building.currency, true),
                        subtitle = "${debtorUnits.size} واحد بدهکار",
                        icon = Icons.Default.Warning,
                        iconTint = Color(0xFFDC2626),
                        bgColor = Color(0xFFFEF2F2),
                        onClick = onNavigateToCharges
                    )
                }
            }

            // Units occupancy & Pending Alerts
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت واحدها و هشدارهای مدیریتی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("کل واحدها: $totalUnits", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            OccupancyItem(label = "دارای سکنه", count = occupiedUnits, color = Color(0xFF16A34A))
                            OccupancyItem(label = "واحد خالی", count = vacantUnits, color = Color(0xFFD97706))
                            OccupancyItem(label = "قبوض معوق", count = unpaidBills.size, color = Color(0xFFDC2626))
                            OccupancyItem(label = "تراکنش تأییدنشد", count = unconfirmedTransactions.size, color = Color(0xFF9333EA))
                        }
                    }
                }
            }

            // Visual Progress Chart Card: Month Charge Collection
            item {
                val percent = if (approvedTargetMonth > 0) ((collectedChargeMonth.toDouble() / approvedTargetMonth) * 100).toInt() else 0
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("نمودار پیشرفت وصول شارژ ماه $currentMonthName", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("$percent٪", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (percent / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("وصول‌شده: ${JalaliCalendar.formatAmount(collectedChargeMonth, building.currency, true)}", fontSize = 11.5.sp, color = Color(0xFF16A34A))
                            Text("باقیمانده: ${JalaliCalendar.formatAmount((approvedTargetMonth - collectedChargeMonth).coerceAtLeast(0L), building.currency, true)}", fontSize = 11.5.sp, color = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // Unconfirmed Bank Transactions Notice
            if (unconfirmedTransactions.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToBankStatement() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SyncProblem, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "تطبیق‌های خودکار در انتظار تأیید مدیر",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF581C87)
                                )
                                Text(
                                    "${unconfirmedTransactions.size} تراکنش بانکی (فیش آب/برق/گاز یا واریزی ساکنین) شناسایی شده و منتظر تأیید نهایی شماست.",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF6B21A8)
                                )
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color(0xFF9333EA))
                        }
                    }
                }
            }

            // Top Debtors List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("واحدهای دارای مانده بدهی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = onNavigateToCharges) {
                        Text("مشاهده جدول کامل", fontSize = 12.sp)
                    }
                }
            }

            if (debtorUnits.isEmpty()) {
                item {
                    Text("خوشبختانه تمامی ساکنین شارژ خود را تسویه کرده‌اند.", fontSize = 12.5.sp, color = Color.Gray)
                }
            } else {
                items(debtorUnits.take(5).size) { idx ->
                    val unit = debtorUnits[idx]
                    val uCharges = chargeMap[unit.id] ?: emptyList()
                    val totalPaid = uCharges.sumOf { it.paidAmount }
                    val totalTarget = uCharges.sumOf { it.targetAmount }
                    val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

                    DebtorUnitCard(
                        unit = unit,
                        debt = debt,
                        currency = building.currency,
                        onClick = onNavigateToCharges
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(115.dp)
            .height(85.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(bgColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun OccupancyItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
        Text(text = label, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DebtorUnitCard(
    unit: BuildingUnit,
    debt: Long,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        "واحد ${unit.unitNumber}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(unit.residentName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Text("طبقه ${unit.floor} - ${unit.mobile}", fontSize = 11.sp, color = Color.Gray)
                }
            }
            Text(
                JalaliCalendar.formatAmount(debt, currency, true),
                color = Color(0xFFDC2626),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
