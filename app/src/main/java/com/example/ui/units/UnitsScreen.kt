package com.example.ui.units

import android.widget.Toast
import androidx.compose.foundation.background
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
import com.example.data.model.Building
import com.example.data.model.BuildingUnit
import com.example.data.model.ChargeRecord
import com.example.util.JalaliCalendar
import com.example.util.ShareHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitsScreen(
    building: Building,
    units: List<BuildingUnit>,
    charges: List<ChargeRecord>,
    onAddUnit: (BuildingUnit) -> Unit,
    onUpdateUnit: (BuildingUnit) -> Unit,
    onDeleteUnit: (BuildingUnit) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUnitForDetail by remember { mutableStateOf<BuildingUnit?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val chargeMap = charges.groupBy { it.unitId }

    val filteredUnits = units.filter { u ->
        val q = searchQuery.trim().lowercase()
        q.isEmpty() || u.residentName.lowercase().contains(q) || u.unitNumber.lowercase().contains(q) || u.mobile.contains(q)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("مدیریت واحدها و پرونده ساکنین", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${units.size} واحد ثبت‌شده | پشتیبانی از تعداد نامحدود", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_unit_button")
                    ) {
                        Icon(Icons.Default.AddHome, contentDescription = "افزودن واحد جدید", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن واحد جدید", fontSize = 12.5.sp) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("جست‌وجو با نام ساکن، شماره واحد یا موبایل...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            items(filteredUnits.size) { idx ->
                val unit = filteredUnits[idx]
                val uCharges = chargeMap[unit.id] ?: emptyList()
                val totalPaid = uCharges.sumOf { it.paidAmount }
                val totalTarget = uCharges.sumOf { it.targetAmount }
                val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

                UnitItemCard(
                    unit = unit,
                    debt = debt,
                    currency = building.currency,
                    onClick = { selectedUnitForDetail = unit },
                    onSendSms = {
                        val currentDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString()
                        val debtStr = JalaliCalendar.formatAmount(debt, building.currency, true)
                        val msg = "ساکن محترم واحد ${unit.unitNumber} (${unit.residentName})،\nبا سلام؛ مانده بدهی شارژ ساختمان ${building.name} تا تاریخ $currentDate برابر $debtStr می‌باشد.\nلطفاً نسبت به تسویه حساب اقدام فرمایید.\nمدیریت ساختمان"
                        ShareHelper.shareSms(context, unit.mobile, msg)
                    }
                )
            }
        }
    }

    // Add Unit Dialog
    if (showAddDialog) {
        var unitNumber by remember { mutableStateOf("${units.size + 1}") }
        var floor by remember { mutableStateOf("1") }
        var area by remember { mutableStateOf("90") }
        var residentName by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var cardNumber by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("تعریف واحد جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = unitNumber,
                            onValueChange = { unitNumber = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("شماره واحد") }
                        )
                        OutlinedTextField(
                            value = floor,
                            onValueChange = { floor = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("طبقه") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = residentName,
                        onValueChange = { residentName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام و نام خانوادگی ساکن") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شماره موبایل ساکن") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardNumber,
                        onValueChange = { cardNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("شماره کارت بانکی (جهت تطبیق خودکار)") },
                        placeholder = { Text("مثلاً 60379918...") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (residentName.isNotBlank()) {
                                onAddUnit(
                                    BuildingUnit(
                                        buildingId = building.id,
                                        unitNumber = unitNumber,
                                        floor = floor.toIntOrNull() ?: 1,
                                        area = area.toDoubleOrNull() ?: 90.0,
                                        ownerName = residentName,
                                        residentName = residentName,
                                        mobile = mobile,
                                        cardNumber = cardNumber
                                    )
                                )
                                showAddDialog = false
                                Toast.makeText(context, "واحد $unitNumber با موفقیت افزوده شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت واحد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Resident Profile Details Dialog
    selectedUnitForDetail?.let { unit ->
        val uCharges = chargeMap[unit.id] ?: emptyList()
        val totalPaid = uCharges.sumOf { it.paidAmount }
        val totalTarget = uCharges.sumOf { it.targetAmount }
        val debt = (totalTarget - totalPaid).coerceAtLeast(0L) + unit.initialDebt

        Dialog(onDismissRequest = { selectedUnitForDetail = null }) {
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
                        Text("پرونده ساکن - واحد ${unit.unitNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { selectedUnitForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    DetailRow(label = "نام ساکن:", value = unit.residentName)
                    DetailRow(label = "نام مالک:", value = unit.ownerName)
                    if (unit.tenantName.isNotBlank()) {
                        DetailRow(label = "نام مستأجر:", value = unit.tenantName)
                    }
                    DetailRow(label = "طبقه / متراژ:", value = "طبقه ${unit.floor} | ${unit.area} مترمربع")
                    DetailRow(label = "شماره تماس:", value = unit.mobile)
                    DetailRow(label = "شماره کارت بانکی:", value = unit.cardNumber.ifBlank { "ثبت‌نشده" })
                    DetailRow(label = "وضعیت سکونت:", value = if (unit.residencyStatus == "VACANT") "واحد خالی" else "دارای سکنه")
                    DetailRow(
                        label = "مانده بدهی شارژ:",
                        value = JalaliCalendar.formatAmount(debt, building.currency, true),
                        isDebt = debt > 0
                    )

                    if (unit.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("یادداشت واحد:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(unit.notes, fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val currentDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString()
                                val debtStr = JalaliCalendar.formatAmount(debt, building.currency, true)
                                val msg = "ساکن محترم واحد ${unit.unitNumber} (${unit.residentName})،\nبا سلام؛ مانده بدهی شارژ شما تا تاریخ $currentDate مبلغ $debtStr است."
                                ShareHelper.shareSms(context, unit.mobile, msg)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ارسال پیامک بدهی", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isDebt: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDebt) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun UnitItemCard(
    unit: BuildingUnit,
    debt: Long,
    currency: String,
    onClick: () -> Unit,
    onSendSms: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("unit_card_${unit.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            unit.unitNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(unit.residentName, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    Text("طبقه ${unit.floor} | ${unit.mobile}", fontSize = 11.5.sp, color = Color.Gray)
                    if (unit.cardNumber.isNotBlank()) {
                        Text("کارت: ${unit.cardNumber.takeLast(4)}****", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    JalaliCalendar.formatAmount(debt, currency, true),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = if (debt > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                )

                if (debt > 0) {
                    IconButton(
                        onClick = onSendSms,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.SendToMobile,
                            contentDescription = "ارسال پیامک یادآوری",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("تسویه ✓", color = Color(0xFF16A34A), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}
