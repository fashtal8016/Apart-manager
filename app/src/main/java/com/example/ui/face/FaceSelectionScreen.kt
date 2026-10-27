package com.example.ui.face

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.util.KiwiPreferences
import kotlinx.coroutines.launch

data class FaceOption(
    val id: Int,
    val title: String,
    val shortName: String,
    val subtitle: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val bgPreviewColor: Color,
    val cardColor: Color,
    val textColor: Color,
    val bulletPoints: List<String>
)

val FaceOptions = listOf(
    FaceOption(
        id = 1,
        title = "فیس اول: کلاسیک مدیریتی",
        shortName = "کلاسیک",
        subtitle = "رسمی، خوانا و ساختاریافته اداری",
        description = "مناسب برای مدیرانی که ظاهر حسابداری استاندارد، جداول منظم و ترکیب رنگ رسمی سرمه‌ای و خاکستری را ترجیح می‌دهند.",
        primaryColor = Color(0xFF1E3A8A),
        secondaryColor = Color(0xFF3B82F6),
        bgPreviewColor = Color(0xFFF1F5F9),
        cardColor = Color.White,
        textColor = Color(0xFF1E293B),
        bulletPoints = listOf(
            "رنگ‌بندی سرمه‌ای اداری، خاکستری سنگی و سفید",
            "ستون‌بندی حسابداری رسمی و فونت خوانا",
            "کنتراست بالا و جدول‌بندی منظم برای نظارت بر شارژ"
        )
    ),
    FaceOption(
        id = 2,
        title = "فیس دوم: مدرن و لوکس",
        shortName = "مدرن",
        subtitle = "زمینه تیره متالیک با جلوه طلایی",
        description = "طراحی جذاب ویژه ساختمان‌های لوکس و محیط‌های کم‌نور؛ کارت‌های شناور با نوارهای طلایی نرم و کارت‌های آماری زیبا.",
        primaryColor = Color(0xFFD4AF37),
        secondaryColor = Color(0xFFE5C158),
        bgPreviewColor = Color(0xFF0F172A),
        cardColor = Color(0xFF1E293B),
        textColor = Color(0xFFF8FAFC),
        bulletPoints = listOf(
            "تم دارک متالیک عمیق با خطوط طلایی آرام‌بخش",
            "کارت‌های شناور با کنتراست فوق‌العاده",
            "جلوه‌ای شیک و معاصر برای ساختمان‌های نوساز"
        )
    ),
    FaceOption(
        id = 3,
        title = "فیس سوم: مینیمال و سریع",
        shortName = "مینیمال",
        subtitle = "بسیار سبک، بدون شلوغی و دسترسی فوری",
        description = "بدون گرافیک‌های سنگین؛ برای مدیرانی که می‌خواهند بلافاصله و با کمترین لمس، شارژ واحدها را بررسی و ثبت کنند.",
        primaryColor = Color(0xFF2563EB),
        secondaryColor = Color(0xFF10B981),
        bgPreviewColor = Color(0xFFF8FAFC),
        cardColor = Color.White,
        textColor = Color(0xFF0F172A),
        bulletPoints = listOf(
            "طراحی بسیار سبک، مینیمال و تمیز",
            "دکمه‌ها و فرم‌های سریع بدون المان‌های تزئینی اضافی",
            "بارگذاری سریع صفحات و دسترسی فوری به ثبت پرداختی"
        )
    ),
    FaceOption(
        id = 4,
        title = "فیس چهارم: داشبورد حرفه‌ای مالی",
        shortName = "مالی",
        subtitle = "تحلیلی، نموداری و کنترل تراز نقدی",
        description = "طراحی اختصاصی برای حسابداری دقیق؛ نمودارهای پیشرفت وصول، نمایش نقدینگی بانک، وضعیت صندوق و هشدارهای مالی.",
        primaryColor = Color(0xFF047857),
        secondaryColor = Color(0xFF0D9488),
        bgPreviewColor = Color(0xFFF0FDF4),
        cardColor = Color.White,
        textColor = Color(0xFF064E3B),
        bulletPoints = listOf(
            "پالت سبز زمردی حسابداری و رنگ‌های هشداردهنده هوشمند",
            "نوارهای پیشرفت مالی برای وصول شارژ و کنترل هزینه‌ها",
            "مانیتورینگ همزمان حساب‌های بانکی، صندوق و تراز کل"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceSelectionScreen(
    currentSelectedId: Int = 1,
    onConfirmSelection: (Int) -> Unit
) {
    val context = LocalContext.current
    val kiwiPreferences = remember { KiwiPreferences(context) }
    val initialFace = remember {
        // Read from SharedPreferences first, fallback to parameter
        val saved = kiwiPreferences.getSelectedFace()
        if (saved in 1..4) saved else currentSelectedId
    }

    var selectedId by remember { mutableIntStateOf(initialFace) }
    val pagerState = rememberPagerState(initialPage = (selectedId - 1).coerceIn(0, 3)) { FaceOptions.size }
    val coroutineScope = rememberCoroutineScope()
    var fullPreviewFace by remember { mutableStateOf<FaceOption?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "انتخاب ظاهر برنامه (فیس اولیه)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "۴ صفحه مستقل با پیش‌نمایش زنده | ذخیره دائمی",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Tab Navigation Bar (4 Pages: کلاسیک، مدرن، مینیمال، مالی)
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                FaceOptions.forEachIndexed { index, face ->
                    val isTabSelected = pagerState.currentPage == index
                    Tab(
                        selected = isTabSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (face.id == selectedId) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(face.primaryColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    face.shortName,
                                    fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_face_${face.id}")
                    )
                }
            }

            // Pager for the 4 independent pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val face = FaceOptions[pageIndex]
                val isSelected = (face.id == selectedId)

                FacePageView(
                    face = face,
                    pageIndex = pageIndex,
                    totalPages = FaceOptions.size,
                    isSelected = isSelected,
                    onSelectFace = {
                        selectedId = face.id
                        // Save in SharedPreferences
                        kiwiPreferences.saveSelectedFace(face.id)
                        Toast.makeText(context, "فیس «${face.title}» انتخاب و در SharedPreferences ذخیره شد", Toast.LENGTH_SHORT).show()
                    },
                    onContinue = {
                        // Persist in SharedPreferences and continue
                        selectedId = face.id
                        kiwiPreferences.saveSelectedFace(face.id)
                        onConfirmSelection(face.id)
                    },
                    onOpenFullPreview = { fullPreviewFace = face },
                    onNavigatePrevious = {
                        if (pageIndex > 0) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pageIndex - 1) }
                        }
                    },
                    onNavigateNext = {
                        if (pageIndex < FaceOptions.size - 1) {
                            coroutineScope.launch { pagerState.animateScrollToPage(pageIndex + 1) }
                        }
                    }
                )
            }

            // Bottom Fixed Navigation & Selection Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Page Dots Indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FaceOptions.indices.forEach { idx ->
                            val active = pagerState.currentPage == idx
                            Box(
                                modifier = Modifier
                                    .size(if (active) 10.dp else 8.dp)
                                    .background(
                                        color = if (active) FaceOptions[idx].primaryColor else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Current Selected status text
                    val currentFace = FaceOptions.firstOrNull { it.id == selectedId } ?: FaceOptions[0]
                    Text(
                        text = "فیس فعال: ${currentFace.shortName}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = currentFace.primaryColor
                    )
                }
            }
        }
    }

    // Modal Full Preview Dialog
    fullPreviewFace?.let { face ->
        FacePreviewDialog(face = face, onDismiss = { fullPreviewFace = null })
    }
}

@Composable
fun FacePageView(
    face: FaceOption,
    pageIndex: Int,
    totalPages: Int,
    isSelected: Boolean,
    onSelectFace: () -> Unit,
    onContinue: () -> Unit,
    onOpenFullPreview: () -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Page Stepper & Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = face.primaryColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "صفحه ${pageIndex + 1} از $totalPages",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = face.primaryColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            if (isSelected) {
                Surface(
                    color = face.primaryColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "انتخاب‌شده برای اجرا",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Title and Subtitle
        Column {
            Text(
                text = face.title,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = face.primaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = face.subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Rich Live Preview Card for this specific Face
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 2.5.dp else 1.dp,
                    color = if (isSelected) face.primaryColor else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onOpenFullPreview() }
                .testTag("preview_card_${face.id}"),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(face.bgPreviewColor)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "پیش‌نمایش کارت رابط کاربری",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = face.textColor
                    )
                    Text(
                        "لمس برای مشاهده بزرگتر ↗",
                        fontSize = 11.sp,
                        color = face.primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realistic Simulated UI Component according to Face Type
                when (face.id) {
                    1 -> ClassicFacePreviewContent(face)
                    2 -> ModernFacePreviewContent(face)
                    3 -> MinimalFacePreviewContent(face)
                    4 -> FinancialFacePreviewContent(face)
                }
            }
        }

        // 2. Face Description and Key Features
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = face.description,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                face.bulletPoints.forEach { pt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = face.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(pt, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Action Buttons: Select Button & Continue Button (Requested specifically)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Select Button ("دکمه انتخاب")
            OutlinedButton(
                onClick = onSelectFace,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("select_button_${face.id}"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) face.primaryColor else MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) face.primaryColor.copy(alpha = 0.1f) else Color.Transparent
                )
            ) {
                Icon(
                    if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isSelected) face.primaryColor else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSelected) "انتخاب‌شده ✓" else "انتخاب این فیس",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = if (isSelected) face.primaryColor else MaterialTheme.colorScheme.onSurface
                )
            }

            // Continue Button ("دکمه ادامه")
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("continue_button_${face.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = face.primaryColor)
            ) {
                Text(
                    text = "ادامه و ورود",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (face.id == 2) Color.Black else Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, // RTL arrow points forward
                    contentDescription = null,
                    tint = if (face.id == 2) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // 4. Stepper Navigation Buttons (Previous / Next page)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (pageIndex > 0) {
                TextButton(onClick = onNavigatePrevious) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("فیس قبلی", fontSize = 12.sp)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            if (pageIndex < totalPages - 1) {
                TextButton(onClick = onNavigateNext) {
                    Text("فیس بعدی", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Face 1: Classic Management Preview Component
// -------------------------------------------------------------
@Composable
fun ClassicFacePreviewContent(face: FaceOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = face.cardColor,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Simulated Classic Navbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E3A8A), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("دفتر روزنامه و وضعیت شارژ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("سال ۱۴۰۳", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Classic Accounting Table Rows
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("واحد", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                Text("ساکن", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("شارژ مصوب", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(70.dp))
                Text("تیک واریز", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
            }

            listOf(
                Triple("واحد ۱", "سعیدی", "۶۵۰,۰۰۰"),
                Triple("واحد ۴", "حسینی", "۶۵۰,۰۰۰"),
                Triple("واحد ۸", "کیانی", "۶۵۰,۰۰۰")
            ).forEach { (u, name, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(u, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(40.dp))
                    Text(name, fontSize = 10.5.sp, modifier = Modifier.weight(1f))
                    Text(amount, fontSize = 10.sp, modifier = Modifier.width(70.dp))
                    Text("✓ پرداخت", fontSize = 9.5.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                }
                Divider(color = Color(0xFFF1F5F9))
            }
        }
    }
}

// -------------------------------------------------------------
// Face 2: Modern & Luxury Preview Component
// -------------------------------------------------------------
@Composable
fun ModernFacePreviewContent(face: FaceOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        color = Color(0xFF1E293B)
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
                            .size(20.dp)
                            .background(Color(0xFFD4AF37), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("K", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بهارستان لوکس", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text("اندوخته بانک", color = Color(0xFFD4AF37), fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Floating Stat Cards with Gold Accent
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("موجودی کل", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        Text("۴۲,۰۰۰,۰۰۰ تومان", color = Color(0xFFE5C158), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    color = Color(0xFF0F172A)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("وصول شهریور", color = Color(0xFF94A3B8), fontSize = 9.sp)
                        Text("۸۵٪ کامل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Face 3: Minimal & Fast Preview Component
// -------------------------------------------------------------
@Composable
fun MinimalFacePreviewContent(face: FaceOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("دسترسی سریع روزمره", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ثبت قبض", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E40AF))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تیک شارژ", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Face 4: Professional Financial Preview Component
// -------------------------------------------------------------
@Composable
fun FinancialFacePreviewContent(face: FaceOption) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("تراز درآمد و هزینه شهریور", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                Text("تراز: +۵,۴۰۰,۰۰۰", fontSize = 10.5.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Financial Progress bar
            LinearProgressIndicator(
                progress = { 0.78f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF047857),
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("درآمد شارژ: ۷,۸۰۰,۰۰۰", fontSize = 9.5.sp, color = Color(0xFF16A34A))
                Text("هزینه قبوض: ۲,۴۰۰,۰۰۰", fontSize = 9.5.sp, color = Color(0xFFDC2626))
            }
        }
    }
}

@Composable
fun FacePreviewDialog(face: FaceOption, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp)),
            color = face.bgPreviewColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = face.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = face.textColor
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن", tint = face.textColor)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp)),
                    color = face.cardColor,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("وضعیت مالی ساختمان", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = face.textColor)
                            Surface(
                                color = face.primaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "تراز مثبت",
                                    color = face.primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("شارژ وصول‌شده", fontSize = 11.sp, color = face.textColor.copy(alpha = 0.7f))
                                Text("۷,۸۰۰,۰۰۰ تومان", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = face.textColor)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("موجودی بانک", fontSize = 11.sp, color = face.textColor.copy(alpha = 0.7f))
                                Text("۱۸,۴۵۰,۰۰۰ تومان", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = face.primaryColor)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = face.primaryColor)
                ) {
                    Text(
                        text = "بستن پیش‌نمایش",
                        color = if (face.id == 2) Color.Black else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
