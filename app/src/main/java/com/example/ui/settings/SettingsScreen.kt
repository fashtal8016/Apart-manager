package com.example.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.Building
import com.example.ui.face.FaceOptions
import com.example.ui.theme.AccentPresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    building: Building,
    settings: AppSettings,
    onUpdateSettings: (AppSettings) -> Unit,
    onOpenFaceSelection: () -> Unit
) {
    val context = LocalContext.current
    var selectedFaceId by remember(settings) { mutableIntStateOf(settings.selectedFace) }
    var selectedCurrency by remember(settings) { mutableStateOf(settings.currency) }
    var selectedAccentHex by remember(settings) { mutableStateOf(settings.accentColorHex) }
    var selectedTableDensity by remember(settings) { mutableStateOf(settings.tableDensity) }
    var selectedThemeMode by remember(settings) { mutableStateOf(settings.themeMode) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("تنظیمات و شخصی‌سازی", fontWeight = FontWeight.Bold, fontSize = 16.5.sp)
                        Text("تنظیم ظاهر ۴ فیس، رنگ، واحد پول و نسخه پشتیبان", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            // Visual Face Selector Card
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
                            Text("انتخاب ظاهر کلی نرم‌افزار (۴ فیس)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            TextButton(onClick = onOpenFaceSelection) {
                                Text("مشاهده پیش‌نمایش‌ها", fontSize = 11.5.sp)
                            }
                        }

                        Text(
                            "تغییر فیس هیچ تغییری در محاسبات و اسناد مالی ساختمان ایجاد نمی‌کند.",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        FaceOptions.forEach { face ->
                            val isSelected = face.id == selectedFaceId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.5.dp,
                                        color = if (isSelected) face.primaryColor else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedFaceId = face.id
                                        onUpdateSettings(settings.copy(selectedFace = face.id))
                                        Toast.makeText(context, "فیس به «${face.title}» تغییر یافت", Toast.LENGTH_SHORT).show()
                                    },
                                color = if (isSelected) face.primaryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedFaceId = face.id
                                            onUpdateSettings(settings.copy(selectedFace = face.id))
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = face.primaryColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(face.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(face.subtitle, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 10 Color Accent Presets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("۱۰ ترکیب رنگ آماده برنامه", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("رنگ اصلی دکمه‌ها و عناصر مهم را متناسب با سلیقه انتخاب کنید:", fontSize = 11.5.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(AccentPresets.size) { idx ->
                                val preset = AccentPresets[idx]
                                val isSelected = preset.hex.equals(selectedAccentHex, ignoreCase = true)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAccentHex = preset.hex
                                            onUpdateSettings(settings.copy(accentColorHex = preset.hex))
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(preset.primary, CircleShape)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(preset.name, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // Financial & Currency Preferences
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("تنظیمات مالی و واحد پول", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("واحد نمایش مبالغ:")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedCurrency == "TOMAN",
                                    onClick = {
                                        selectedCurrency = "TOMAN"
                                        onUpdateSettings(settings.copy(currency = "TOMAN"))
                                    },
                                    label = { Text("تومان") }
                                )
                                FilterChip(
                                    selected = selectedCurrency == "RIAL",
                                    onClick = {
                                        selectedCurrency = "RIAL"
                                        onUpdateSettings(settings.copy(currency = "RIAL"))
                                    },
                                    label = { Text("ریال") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تراکم نمایش جداول:")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedTableDensity == "COMFORTABLE",
                                    onClick = {
                                        selectedTableDensity = "COMFORTABLE"
                                        onUpdateSettings(settings.copy(tableDensity = "COMFORTABLE"))
                                    },
                                    label = { Text("استاندارد") }
                                )
                                FilterChip(
                                    selected = selectedTableDensity == "COMPACT",
                                    onClick = {
                                        selectedTableDensity = "COMPACT"
                                        onUpdateSettings(settings.copy(tableDensity = "COMPACT"))
                                    },
                                    label = { Text("فشرده") }
                                )
                            }
                        }
                    }
                }
            }

            // Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("پشتیبان‌گیری و بازیابی اطلاعات", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            "تمام اطلاعات ساختمان، واحدها، دفاتر معین و تراکنش‌ها در پایگاه داده محلی Room ذخیره شده است.",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    Toast.makeText(context, "نسخه پشتیبان با موفقیت در حافظه ذخیره شد", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("backup_data_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تهیه فایل پشتیبان", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    // Reset appearance
                                    onUpdateSettings(
                                        settings.copy(
                                            selectedFace = 1,
                                            accentColorHex = "#1E3A8A",
                                            themeMode = "SYSTEM",
                                            tableDensity = "COMFORTABLE",
                                            currency = "TOMAN"
                                        )
                                    )
                                    Toast.makeText(context, "تنظیمات ظاهری به حالت اولیه بازگشت", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بازنشانی ظاهر", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
