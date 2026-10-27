package com.example.ui.charge

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.util.ExcelExporter
import com.example.util.JalaliCalendar
import com.example.util.PdfExporter
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnualChargeTableScreen(
    building: Building,
    units: List<BuildingUnit>,
    charges: List<ChargeRecord>,
    annualNotes: List<UnitAnnualNote>,
    monthNotes: List<MonthChargeNote>,
    onSaveChargeRecord: (ChargeRecord) -> Unit,
    onSaveAnnualNote: (Long, Int, String) -> Unit,
    onSaveMonthNote: (Long, Int, Int, String) -> Unit
) {
    val context = LocalContext.current
    var selectedYear by remember { mutableIntStateOf(building.fiscalYear) }
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, DEBTORS, PAID_ONLY
    var showNotesColumn by remember { mutableStateOf(true) }

    // Row Edit Lock State: map of unitId -> editing draft
    var editingUnitId by remember { mutableStateOf<Long?>(null) }
    var draftGeneralNote by remember { mutableStateOf("") }
    val draftCharges = remember { mutableStateMapOf<Int, ChargeRecord>() }

    // Month Note dialog state
    var monthNoteTarget by remember { mutableStateOf<Triple<BuildingUnit, Int, String>?>(null) } // Unit, monthIndex, currentNoteText
    var draftMonthNoteText by remember { mutableStateOf("") }

    val annualNotesMap = remember(annualNotes, selectedYear) {
        annualNotes.filter { it.year == selectedYear }.associate { it.unitId to it.generalNote }
    }
    val monthNotesMap = remember(monthNotes, selectedYear) {
        monthNotes.filter { it.year == selectedYear }.associateBy { "${it.unitId}_${it.monthIndex}" }
    }
    val chargeMap = remember(charges, selectedYear) {
        charges.filter { it.year == selectedYear }.groupBy { it.unitId }
    }

    // Filter units
    val filteredUnits = units.filter { u ->
        val uCharges = chargeMap[u.id] ?: emptyList()
        val totalPaid = uCharges.sumOf { it.paidAmount }
        val totalTarget = uCharges.sumOf { it.targetAmount }
        val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + u.initialDebt
        val generalNote = annualNotesMap[u.id] ?: u.notes

        val matchesFilter = when (filterType) {
            "DEBTORS" -> debt > 0
            "PAID_ONLY" -> debt == 0L
            else -> true
        }

        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
                u.residentName.lowercase().contains(q) ||
                u.unitNumber.lowercase().contains(q) ||
                generalNote.lowercase().contains(q)

        matchesFilter && matchesQuery
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("جدول سالانه شارژ و وضعیت پرداخت‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "سال مالی: $selectedYear | قفل ویرایش فعال است",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Export PDF button
                    IconButton(
                        onClick = {
                            val pdfFile = PdfExporter.generateAnnualChargePdf(
                                context, building, units, charges, annualNotesMap, selectedYear, showNotesColumn
                            )
                            ShareHelper.shareFile(context, pdfFile, "application/pdf", "اشتراک‌گذاری جدول سالانه شارژ PDF")
                        },
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "خروجی PDF", tint = Color(0xFFDC2626))
                    }

                    // Export Excel/CSV button
                    IconButton(
                        onClick = {
                            val csvFile = ExcelExporter.exportAnnualChargeCsv(
                                context, building, units, charges, annualNotesMap, monthNotes, selectedYear
                            )
                            ShareHelper.shareFile(context, csvFile, "text/csv", "اشتراک‌گذاری جدول سالانه شارژ Excel")
                        },
                        modifier = Modifier.testTag("export_excel_button")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "خروجی Excel", tint = Color(0xFF16A34A))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search & Filter controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("charge_search_input"),
                        placeholder = { Text("جست‌وجو در نام ساکن، شماره واحد یا متن توضیحات...", fontSize = 12.5.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filter chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = filterType == "ALL",
                                onClick = { filterType = "ALL" },
                                label = { Text("همه واحدها (${units.size})", fontSize = 11.5.sp) }
                            )
                            FilterChip(
                                selected = filterType == "DEBTORS",
                                onClick = { filterType = "DEBTORS" },
                                label = { Text("بدهکاران", fontSize = 11.5.sp) }
                            )
                            FilterChip(
                                selected = filterType == "PAID_ONLY",
                                onClick = { filterType = "PAID_ONLY" },
                                label = { Text("تسویه‌شده", fontSize = 11.5.sp) }
                            )
                        }

                        // Toggle Notes column
                        IconButton(onClick = { showNotesColumn = !showNotesColumn }) {
                            Icon(
                                if (showNotesColumn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "تغییر نمایش ستون توضیحات",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Info helper banner about Edit Lock
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "قفل امنیتی فعال است: برای تغییر وضعیت تیک‌ها یا درج توضیحات، دکمه «ویرایش» ردیف را بزنید.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Horizontal Scrollable Matrix Table
            val horizontalScrollState = rememberScrollState()

            Box(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    // Table Header
                    AnnualTableHeader(showNotesColumn = showNotesColumn)

                    // Table Body Rows
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredUnits.size) { index ->
                            val unit = filteredUnits[index]
                            val isEditingThisRow = (editingUnitId == unit.id)

                            val uCharges = chargeMap[unit.id] ?: emptyList()
                            val totalPaid = uCharges.sumOf { it.paidAmount }
                            val totalTarget = uCharges.sumOf { it.targetAmount }
                            val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt
                            val generalNote = annualNotesMap[unit.id] ?: unit.notes

                            AnnualTableRow(
                                rowIndex = index + 1,
                                unit = unit,
                                charges = uCharges,
                                draftCharges = if (isEditingThisRow) draftCharges else null,
                                generalNote = if (isEditingThisRow) draftGeneralNote else generalNote,
                                isEditing = isEditingThisRow,
                                showNotesColumn = showNotesColumn,
                                currency = building.currency,
                                monthNotesMap = monthNotesMap,
                                onEditClick = {
                                    // Start editing this row
                                    editingUnitId = unit.id
                                    draftGeneralNote = generalNote
                                    draftCharges.clear()
                                    for (m in 1..12) {
                                        val existing = uCharges.firstOrNull { it.monthIndex == m }
                                        val rec = existing ?: ChargeRecord(
                                            unitId = unit.id,
                                            year = selectedYear,
                                            monthIndex = m,
                                            status = "UNPAID",
                                            targetAmount = building.baseMonthlyCharge,
                                            paidAmount = 0L
                                        )
                                        draftCharges[m] = rec
                                    }
                                },
                                onSaveClick = {
                                    // Commit drafts to database
                                    draftCharges.values.forEach { rec ->
                                        onSaveChargeRecord(rec)
                                    }
                                    onSaveAnnualNote(unit.id, selectedYear, draftGeneralNote)
                                    editingUnitId = null
                                    Toast.makeText(context, "اطلاعات واحد ${unit.unitNumber} ذخیره شد", Toast.LENGTH_SHORT).show()
                                },
                                onCancelClick = {
                                    // Cancel editing
                                    editingUnitId = null
                                    draftCharges.clear()
                                },
                                onGeneralNoteChange = { draftGeneralNote = it },
                                onToggleMonthStatus = { monthIndex ->
                                    if (isEditingThisRow) {
                                        val currentRec = draftCharges[monthIndex] ?: ChargeRecord(
                                            unitId = unit.id,
                                            year = selectedYear,
                                            monthIndex = monthIndex,
                                            status = "UNPAID",
                                            targetAmount = building.baseMonthlyCharge,
                                            paidAmount = 0L
                                        )
                                        // Cycle through statuses: UNPAID -> PAID -> PARTIAL -> EXEMPT -> REVIEW -> UNPAID
                                        val nextStatus = when (currentRec.status) {
                                            "UNPAID" -> "PAID"
                                            "PAID" -> "PARTIAL"
                                            "PARTIAL" -> "EXEMPT"
                                            "EXEMPT" -> "REVIEW"
                                            "REVIEW" -> "UNPAID"
                                            else -> "PAID"
                                        }
                                        val nextPaid = when (nextStatus) {
                                            "PAID" -> currentRec.targetAmount
                                            "PARTIAL" -> currentRec.targetAmount / 2
                                            else -> 0L
                                        }
                                        draftCharges[monthIndex] = currentRec.copy(
                                            status = nextStatus,
                                            paidAmount = nextPaid,
                                            paymentDate = if (nextPaid > 0) JalaliCalendar.getCurrentJalaliDate().toFormattedString() else ""
                                        )
                                    }
                                },
                                onMonthNoteClick = { monthIndex ->
                                    val existingNote = monthNotesMap["${unit.id}_$monthIndex"]?.note ?: ""
                                    draftMonthNoteText = existingNote
                                    monthNoteTarget = Triple(unit, monthIndex, existingNote)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Monthly Note Dialog
    monthNoteTarget?.let { (unit, monthIdx, currentText) ->
        val monthName = JalaliCalendar.MONTH_NAMES[monthIdx - 1]
        Dialog(onDismissRequest = { monthNoteTarget = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "یادداشت ماه $monthName - واحد ${unit.unitNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(onClick = { monthNoteTarget = null }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Text(
                        "ساکن: ${unit.residentName} | ثبت یادداشت اختصاصی برای این ماه (مثلاً پرداخت علی‌الحساب، مهلت یا علت تأخیر)",
                        fontSize = 11.5.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = draftMonthNoteText,
                        onValueChange = { draftMonthNoteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("متن یادداشت شارژ این ماه را وارد کنید...") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onSaveMonthNote(unit.id, selectedYear, monthIdx, "")
                                monthNoteTarget = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حذف یادداشت", color = Color(0xFFDC2626), fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                onSaveMonthNote(unit.id, selectedYear, monthIdx, draftMonthNoteText)
                                monthNoteTarget = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ذخیره یادداشت", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnualTableHeader(showNotesColumn: Boolean) {
    Surface(
        modifier = Modifier.height(44.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TableCell(text = "ردیف", width = 36.dp, isHeader = true)
            TableCell(text = "واحد", width = 48.dp, isHeader = true)
            TableCell(text = "نام ساکن", width = 110.dp, isHeader = true)

            // 12 Shamsi Months
            val months = listOf("فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند")
            months.forEach { m ->
                TableCell(text = m, width = 58.dp, isHeader = true)
            }

            TableCell(text = "جمع واریز", width = 90.dp, isHeader = true)
            TableCell(text = "مانده بدهی", width = 90.dp, isHeader = true)

            if (showNotesColumn) {
                TableCell(text = "توضیحات کلی واحد", width = 160.dp, isHeader = true)
            }

            TableCell(text = "عملیات", width = 120.dp, isHeader = true)
        }
    }
}

@Composable
fun AnnualTableRow(
    rowIndex: Int,
    unit: BuildingUnit,
    charges: List<ChargeRecord>,
    draftCharges: Map<Int, ChargeRecord>?,
    generalNote: String,
    isEditing: Boolean,
    showNotesColumn: Boolean,
    currency: String,
    monthNotesMap: Map<String, MonthChargeNote>,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onGeneralNoteChange: (String) -> Unit,
    onToggleMonthStatus: (Int) -> Unit,
    onMonthNoteClick: (Int) -> Unit
) {
    val effectiveCharges = draftCharges ?: charges.associateBy { it.monthIndex }
    val totalPaid = effectiveCharges.values.sumOf { it.paidAmount }
    val totalTarget = effectiveCharges.values.sumOf { it.targetAmount }
    val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

    Surface(
        modifier = Modifier
            .height(52.dp)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant),
        color = if (isEditing) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Row index
            TableCell(text = "$rowIndex", width = 36.dp)

            // Unit Number
            TableCell(text = unit.unitNumber, width = 48.dp, isBold = true)

            // Resident Name
            TableCell(text = unit.residentName, width = 110.dp)

            // 12 Months Status Cells
            for (m in 1..12) {
                val cr = effectiveCharges[m]
                val status = cr?.status ?: "UNPAID"
                val hasMonthNote = (monthNotesMap["${unit.id}_$m"]?.note?.isNotBlank() == true)

                MonthStatusCell(
                    status = status,
                    isEditing = isEditing,
                    hasNote = hasMonthNote,
                    onClick = { onToggleMonthStatus(m) },
                    onNoteClick = { onMonthNoteClick(m) }
                )
            }

            // Total Paid
            TableCell(
                text = JalaliCalendar.formatAmount(totalPaid, currency, true).replace(" تومان", "").replace(" ریال", ""),
                width = 90.dp
            )

            // Debt Balance
            TableCell(
                text = JalaliCalendar.formatAmount(debt, currency, true).replace(" تومان", "").replace(" ریال", ""),
                width = 90.dp,
                textColor = if (debt > 0) Color(0xFFDC2626) else Color(0xFF16A34A),
                isBold = debt > 0
            )

            // General Note Column
            if (showNotesColumn) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = generalNote,
                            onValueChange = onGeneralNoteChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("general_note_input_${unit.id}"),
                            placeholder = { Text("توضیحات کلی واحد...", fontSize = 10.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp)
                        )
                    } else {
                        Text(
                            text = if (generalNote.isNotBlank()) generalNote else "—",
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = if (generalNote.isNotBlank()) MaterialTheme.colorScheme.onSurface else Color.LightGray
                        )
                    }
                }
            }

            // Actions: Edit Lock Controls
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Button(
                            onClick = onSaveClick,
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("save_row_${unit.id}"),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("ذخیره", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier.height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("لغو", fontSize = 11.sp)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("edit_row_${unit.id}"),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ویرایش", fontSize = 11.5.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthStatusCell(
    status: String,
    isEditing: Boolean,
    hasNote: Boolean,
    onClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    val (label, bgColor, textColor) = when (status) {
        "PAID" -> Triple("✓", Color(0xFFDCFCE7), Color(0xFF16A34A))
        "PARTIAL" -> Triple("جزئی", Color(0xFFFEF3C7), Color(0xFFD97706))
        "EXEMPT" -> Triple("معاف", Color(0xFFE0E7FF), Color(0xFF4F46E5))
        "REVIEW" -> Triple("بررسی", Color(0xFFFFE4E6), Color(0xFFE11D48))
        else -> Triple("—", Color(0xFFF1F5F9), Color(0xFF94A3B8))
    }

    Box(
        modifier = Modifier
            .width(58.dp)
            .fillMaxHeight()
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .clickable(enabled = isEditing) { onClick() }
                .border(
                    width = if (isEditing) 1.dp else 0.dp,
                    color = if (isEditing) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            // Month Note Icon Indicator
            if (hasNote || isEditing) {
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (hasNote) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onNoteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.StickyNote2,
                        contentDescription = "یادداشت ماه",
                        tint = if (hasNote) Color.White else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = if (isHeader) 11.5.sp else 11.sp,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) MaterialTheme.colorScheme.onPrimaryContainer else textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
