package com.example.ui.ledger

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
import com.example.data.model.Building
import com.example.data.model.Ledger
import com.example.util.JalaliCalendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(
    building: Building,
    ledgers: List<Ledger>,
    onAddLedger: (Ledger) -> Unit
) {
    val context = LocalContext.current
    var showAddLedgerDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") } // ALL, UTILITY, EXPENSE, MAINTENANCE
    var selectedLedgerForDetail by remember { mutableStateOf<Ledger?>(null) }

    val filteredLedgers = ledgers.filter {
        if (selectedCategory == "ALL") true else it.category == selectedCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("دفاتر معین و هزینه‌های ساختمان", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "${ledgers.size} دفتر معین فعال با قابلیت تشخیص کلمات کلیدی",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddLedgerDialog = true },
                        modifier = Modifier.testTag("add_ledger_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "افزودن دفتر معین جدید", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddLedgerDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("افزودن دفتر معین جدید", fontSize = 12.5.sp) },
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
            // Category Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "ALL",
                        onClick = { selectedCategory = "ALL" },
                        label = { Text("همه دفاتر (${ledgers.size})", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "UTILITY",
                        onClick = { selectedCategory = "UTILITY" },
                        label = { Text("قبوض (آب/برق/گاز)", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "MAINTENANCE",
                        onClick = { selectedCategory = "MAINTENANCE" },
                        label = { Text("نگهداری و آسانسور", fontSize = 11.5.sp) }
                    )
                    FilterChip(
                        selected = selectedCategory == "EXPENSE",
                        onClick = { selectedCategory = "EXPENSE" },
                        label = { Text("هزینه‌های جاری", fontSize = 11.5.sp) }
                    )
                }
            }

            // Ledgers List
            items(filteredLedgers.size) { idx ->
                val ledger = filteredLedgers[idx]
                LedgerItemCard(
                    ledger = ledger,
                    onClick = { selectedLedgerForDetail = ledger }
                )
            }
        }
    }

    // Add Ledger Dialog
    if (showAddLedgerDialog) {
        var nameInput by remember { mutableStateOf("") }
        var codeInput by remember { mutableStateOf("${5000 + ledgers.size + 1}") }
        var categoryInput by remember { mutableStateOf("EXPENSE") }
        var keywordsInput by remember { mutableStateOf("") }
        var descInput by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddLedgerDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("افزودن دفتر معین جدید", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("نام دفتر معین (مثلاً بیمه، سرویس پمپ، نگهبانی)") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { codeInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("کد حساب") }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = keywordsInput,
                        onValueChange = { keywordsInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("کلمات کلیدی برای تطبیق خودکار (با ویرگول جدا کنید)") },
                        placeholder = { Text("مثلاً: بیمه آتش‌سوزی,بیمه مسئولیت,رازی") }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("توضیحات و نحوه تسهیم") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onAddLedger(
                                    Ledger(
                                        buildingId = building.id,
                                        name = nameInput,
                                        code = codeInput,
                                        category = categoryInput,
                                        keywords = keywordsInput,
                                        colorHex = "#2563EB",
                                        description = descInput
                                    )
                                )
                                showAddLedgerDialog = false
                                Toast.makeText(context, "دفتر معین «$nameInput» با موفقیت افزوده شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("ایجاد دفتر معین", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Ledger Detail Dialog
    selectedLedgerForDetail?.let { ledger ->
        Dialog(onDismissRequest = { selectedLedgerForDetail = null }) {
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
                        Text(ledger.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { selectedLedgerForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Text("کد حساب معین: ${ledger.code} | رده: ${ledger.category}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("کلمات کلیدی تطبیق خودکار:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = ledger.keywords.ifBlank { "تعریف‌نشده" },
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("توضیحات دفتر:", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    Text(
                        text = ledger.description.ifBlank { "توضیحاتی برای این دفتر ثبت نشده است." },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedLedgerForDetail = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("بستن")
                    }
                }
            }
        }
    }
}

@Composable
fun LedgerItemCard(
    ledger: Ledger,
    onClick: () -> Unit
) {
    val ledgerColor = try {
        Color(android.graphics.Color.parseColor(ledger.colorHex))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("ledger_card_${ledger.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ledgerColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (ledger.category) {
                        "UTILITY" -> Icons.Default.Bolt
                        "MAINTENANCE" -> Icons.Default.Build
                        else -> Icons.Default.ReceiptLong
                    },
                    contentDescription = null,
                    tint = ledgerColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(ledger.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            ledger.code,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                if (ledger.keywords.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "کلمات کلیدی: ${ledger.keywords}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                }
            }

            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
        }
    }
}
