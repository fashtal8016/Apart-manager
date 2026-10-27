package com.example.ui.bills

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
import com.example.data.model.UtilityBill
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    building: Building,
    bills: List<UtilityBill>,
    onAddBill: (UtilityBill) -> Unit,
    onUpdateBill: (UtilityBill) -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredBills = bills.filter {
        when (filterStatus) {
            "UNPAID" -> it.status == "UNPAID"
            "PAID" -> it.status == "PAID"
            else -> true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("مدیریت قبوض و هزینه‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("قبوض آب، برق، گاز و سایر هزینه‌های مشترک", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ثبت قبض جدید", fontSize = 12.5.sp) },
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
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filterStatus == "ALL",
                        onClick = { filterStatus = "ALL" },
                        label = { Text("همه قبوض (${bills.size})") }
                    )
                    FilterChip(
                        selected = filterStatus == "UNPAID",
                        onClick = { filterStatus = "UNPAID" },
                        label = { Text("پرداخت‌نشده (${bills.count { it.status == "UNPAID" }})") }
                    )
                    FilterChip(
                        selected = filterStatus == "PAID",
                        onClick = { filterStatus = "PAID" },
                        label = { Text("پرداخت‌شده") }
                    )
                }
            }

            if (filteredBills.isEmpty()) {
                item {
                    Text("هیچ قبضی در این بخش وجود ندارد.", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(filteredBills.size) { idx ->
                    val bill = filteredBills[idx]
                    BillCardItem(
                        bill = bill,
                        currency = building.currency,
                        onPayToggle = {
                            val newStatus = if (bill.status == "PAID") "UNPAID" else "PAID"
                            val paidAmt = if (newStatus == "PAID") bill.amount else 0L
                            val paidDate = if (newStatus == "PAID") JalaliCalendar.getCurrentJalaliDate().toFormattedString() else ""
                            onUpdateBill(bill.copy(status = newStatus, paidAmount = paidAmt, paidDate = paidDate))
                            Toast.makeText(context, "وضعیت قبض به‌روزرسانی شد", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        var billType by remember { mutableStateOf("آب") }
        var amountStr by remember { mutableStateOf("") }
        var billIdInput by remember { mutableStateOf("") }
        var paymentIdInput by remember { mutableStateOf("") }
        var periodInput by remember { mutableStateOf("شهریور ۱۴۰۳") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ثبت قبض جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("آب", "برق", "گاز", "شهرداری", "سایر").forEach { type ->
                            FilterChip(
                                selected = billType == type,
                                onClick = { billType = type },
                                label = { Text(type, fontSize = 11.5.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("مبلغ قبض (${if (building.currency == "RIAL") "ریال" else "تومان"})") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = billIdInput,
                            onValueChange = { billIdInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("شناسه قبض") }
                        )
                        OutlinedTextField(
                            value = paymentIdInput,
                            onValueChange = { paymentIdInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("شناسه پرداخت") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = periodInput,
                        onValueChange = { periodInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("دوره مصرف") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val amt = amountStr.replace(",", "").toLongOrNull() ?: 0L
                            if (amt > 0) {
                                onAddBill(
                                    UtilityBill(
                                        buildingId = building.id,
                                        billType = billType,
                                        billId = billIdInput,
                                        paymentId = paymentIdInput,
                                        period = periodInput,
                                        amount = amt,
                                        dueDate = JalaliCalendar.getCurrentJalaliDate().toFormattedString(),
                                        status = "UNPAID"
                                    )
                                )
                                showAddDialog = false
                                Toast.makeText(context, "قبض $billType ثبت شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ثبت قبض", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BillCardItem(
    bill: UtilityBill,
    currency: String,
    onPayToggle: () -> Unit
) {
    val isPaid = bill.status == "PAID"
    val (typeColor, typeIcon) = when (bill.billType) {
        "آب" -> Pair(Color(0xFF0EA5E9), Icons.Default.WaterDrop)
        "برق" -> Pair(Color(0xFFF59E0B), Icons.Default.Bolt)
        "گاز" -> Pair(Color(0xFFEF4444), Icons.Default.LocalFireDepartment)
        else -> Pair(Color(0xFF6B7280), Icons.Default.Receipt)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .testTag("bill_card_${bill.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("قبض ${bill.billType} - ${bill.period}", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text("شناسه: ${bill.billId.ifEmpty { "—" }}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Text(
                    JalaliCalendar.formatAmount(bill.amount, currency, true),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isPaid) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isPaid) "پرداخت‌شده ✓" else "پرداخت‌نشده",
                        color = if (isPaid) Color(0xFF16A34A) else Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                OutlinedButton(
                    onClick = onPayToggle,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(if (isPaid) "تغییر به پرداخت‌نشده" else "ثبت پرداخت قبض", fontSize = 11.5.sp)
                }
            }
        }
    }
}
