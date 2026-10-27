package com.example.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.Building
import com.example.data.model.BuildingUnit
import com.example.data.model.ChargeRecord
import com.example.util.ExcelExporter
import com.example.util.JalaliCalendar
import com.example.util.PdfExporter
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositSummaryScreen(
    building: Building,
    units: List<BuildingUnit>,
    charges: List<ChargeRecord>
) {
    val context = LocalContext.current
    var isOnePageSummaryMode by remember { mutableStateOf(true) } // true = One Page Summary, false = Detailed
    var selectedMonthIndex by remember { mutableIntStateOf(6) } // شهریور default
    val selectedMonthName = JalaliCalendar.MONTH_NAMES[selectedMonthIndex - 1]
    val currentDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString()

    val chargeMap = charges.groupBy { it.unitId }

    // Summary calculations
    val unitsWithDeposits = units.count { u ->
        val uCharges = chargeMap[u.id] ?: emptyList()
        uCharges.any { it.paidAmount > 0 }
    }
    val unitsWithoutDeposits = units.size - unitsWithDeposits
    val totalDepositsSum = units.sumOf { u ->
        val uCharges = chargeMap[u.id] ?: emptyList()
        uCharges.sumOf { it.paidAmount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("خلاصه واریز ساکنین", fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
                        Text(
                            "${building.name} | سال ${building.fiscalYear}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // PDF Button
                    IconButton(
                        onClick = {
                            val pdfFile = PdfExporter.generateDepositSummaryPdf(
                                context, building, units, charges, building.fiscalYear, selectedMonthName
                            )
                            ShareHelper.shareFile(context, pdfFile, "application/pdf", "اشتراک‌گذاری خلاصه واریز ساکنین PDF")
                        },
                        modifier = Modifier.testTag("export_deposit_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "خروجی PDF", tint = Color(0xFFDC2626))
                    }

                    // Excel Button
                    IconButton(
                        onClick = {
                            val csvFile = ExcelExporter.exportDepositSummaryCsv(
                                context, building, units, charges, building.fiscalYear
                            )
                            ShareHelper.shareFile(context, csvFile, "text/csv", "اشتراک‌گذاری خلاصه واریز ساکنین Excel")
                        },
                        modifier = Modifier.testTag("export_deposit_excel_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "خروجی Excel", tint = Color(0xFF16A34A))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
        ) {
            // Mode Selector (One-Page vs Detailed)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isOnePageSummaryMode,
                        onClick = { isOnePageSummaryMode = true },
                        label = { Text("خلاصه یک‌صفحه‌ای تمام واحدها", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.LooksOne, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    FilterChip(
                        selected = !isOnePageSummaryMode,
                        onClick = { isOnePageSummaryMode = false },
                        label = { Text("گزارش کامل تمام تراکنش‌ها", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.ListAlt, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سربرگ گزارش مالی واریزی‌ها", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Text("تاریخ گزارش: $currentDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("تعداد کل واحدها: ${units.size}", fontSize = 12.sp)
                                Text("واحدهای دارای پرداخت: $unitsWithDeposits", fontSize = 12.sp, color = Color(0xFF16A34A))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("واحدهای بدون واریزی: $unitsWithoutDeposits", fontSize = 12.sp, color = Color(0xFFDC2626))
                                Text(
                                    "مجموع کل واریزها: ${JalaliCalendar.formatAmount(totalDepositsSum, building.currency, true)}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Table Header
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ردیف", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp))
                        Text("واحد", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                        Text("نام ساکن", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                        Text(if (isOnePageSummaryMode) "آخرین واریز" else "تاریخ سند", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("مبلغ", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("وضعیت", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                    }
                }
            }

            // Table Items
            if (isOnePageSummaryMode) {
                // One-Page Mode: 1 Row per unit
                items(units.size) { idx ->
                    val unit = units[idx]
                    val uCharges = chargeMap[unit.id] ?: emptyList()
                    val totalPaid = uCharges.sumOf { it.paidAmount }
                    val lastPaidRecord = uCharges.filter { it.paidAmount > 0 }.maxByOrNull { it.monthIndex }
                    val lastDate = lastPaidRecord?.paymentDate ?: "—"
                    val isPaid = totalPaid > 0

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
                        color = if (idx % 2 == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${idx + 1}", fontSize = 11.5.sp, modifier = Modifier.width(32.dp))
                            Text(unit.unitNumber, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                            Text(unit.residentName, fontSize = 12.sp, modifier = Modifier.weight(1.2f), maxLines = 1)
                            Text(lastDate, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                            Text(
                                JalaliCalendar.formatAmount(totalPaid, building.currency, true)
                                    .replace(" تومان", "").replace(" ریال", ""),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                modifier = Modifier.width(70.dp)
                            ) {
                                Text(
                                    text = if (isPaid) "تأییدشده" else "بدون پرداخت",
                                    fontSize = 10.sp,
                                    color = if (isPaid) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 3.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            } else {
                // Detailed Mode: Every single paid record
                val allPaidRecords = charges.filter { it.paidAmount > 0 }.sortedByDescending { it.id }
                if (allPaidRecords.isEmpty()) {
                    item {
                        Text("هیچ واریزی در این بازه ثبت نشده است.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                    }
                } else {
                    items(allPaidRecords.size) { idx ->
                        val rec = allPaidRecords[idx]
                        val unit = units.firstOrNull { it.id == rec.unitId }
                        val unitNumber = unit?.unitNumber ?: "—"
                        val residentName = unit?.residentName ?: "—"

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
                            color = if (idx % 2 == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${idx + 1}", fontSize = 11.sp, modifier = Modifier.width(32.dp))
                                Text(unitNumber, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(44.dp))
                                Text(residentName, fontSize = 11.5.sp, modifier = Modifier.weight(1.2f), maxLines = 1)
                                Text(rec.paymentDate.ifEmpty { "—" }, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text(
                                    JalaliCalendar.formatAmount(rec.paidAmount, building.currency, true)
                                        .replace(" تومان", "").replace(" ریال", ""),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("شارژ ${JalaliCalendar.MONTH_NAMES[rec.monthIndex - 1]}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(70.dp))
                            }
                        }
                    }
                }
            }

            // Summary Footer Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("خلاصه گزارش کنترل نهایی:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "۱. مبالغ واریز از تراکنش‌های بانکی و دفاتر معین تأییدشده محاسبه شده است.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "۲. انتقال‌های داخلی بین حساب‌های ساختمان به عنوان واریز ساکن لحاظ نشده است.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "۳. جهت اشتراک‌گذاری یا چاپ در یک صفحه افقی استاندارد، دکمه PDF بالای صفحه را لمس کنید.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
