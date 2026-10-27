package com.example.data

import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SampleDataLoader {
    suspend fun seedDatabase(database: AppDatabase) = withContext(Dispatchers.IO) {
        // Check if building already exists
        val buildingDao = database.buildingDao()
        val existingBuildings = database.buildingDao().getAllBuildings()
        // If empty, insert
        val building = Building(
            id = 1L,
            name = "ساختمان بهارستان (۱۲ واحدی)",
            address = "تهران، خیابان شریعتی، کوچه بهار، پلاک ۱۲",
            phone = "۰۲۱-۲۲۳۳۴۴۵۵",
            managerName = "مهندس علیرضا رادپور",
            floorCount = 4,
            unitCount = 12,
            baseMonthlyCharge = 650_000L,
            chargeCalcMethod = "FIXED",
            fiscalYear = 1403,
            currency = "TOMAN",
            notes = "ساختمان مسکونی ۴ طبقه هر طبقه ۳ واحد. آسانسور و موتورخانه مرکزی دارد."
        )
        buildingDao.insertBuilding(building)

        // 12 Units
        val unitDao = database.unitDao()
        val units = listOf(
            BuildingUnit(
                id = 1L, buildingId = 1L, unitNumber = "۱", floor = 1, area = 95.0,
                ownerName = "احمد سعیدی", residentName = "احمد سعیدی",
                mobile = "09121111101", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "603799182341", bankName = "ملی", initialDebt = 0L,
                notes = "ساکن دائمی - همیشه سر موقع پرداخت می‌کند."
            ),
            BuildingUnit(
                id = 2L, buildingId = 1L, unitNumber = "۲", floor = 1, area = 105.0,
                ownerName = "جواد کاظمی", tenantName = "مریم رضایی", residentName = "مریم رضایی",
                mobile = "09122222102", residencyStatus = "TENANT_OCCUPIED",
                cardNumber = "589210118832", bankName = "سپه", initialDebt = 650_000L,
                notes = "مستأجر از خرداد ماه سکونت دارد."
            ),
            BuildingUnit(
                id = 3L, buildingId = 1L, unitNumber = "۳", floor = 1, area = 88.0,
                ownerName = "مرتضی یوسفی", residentName = "مرتضی یوسفی",
                mobile = "09123333103", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "610433789012", bankName = "ملت", initialDebt = 0L
            ),
            BuildingUnit(
                id = 4L, buildingId = 1L, unitNumber = "۴", floor = 2, area = 95.0,
                ownerName = "رضا حسینی", residentName = "رضا حسینی",
                mobile = "09124444104", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "603769194455", bankName = "صادرات", initialDebt = 1_300_000L,
                notes = "توافق پرداخت اقساطی بدهی معوق با مدیر انجام شده است."
            ),
            BuildingUnit(
                id = 5L, buildingId = 1L, unitNumber = "۵", floor = 2, area = 105.0,
                ownerName = "بهروز تهرانی", tenantName = "سارا امینی", residentName = "سارا امینی",
                mobile = "09125555105", residencyStatus = "TENANT_OCCUPIED",
                cardNumber = "502229107741", bankName = "پاسارگاد", initialDebt = 0L
            ),
            BuildingUnit(
                id = 6L, buildingId = 1L, unitNumber = "۶", floor = 2, area = 88.0,
                ownerName = "پیمان صادقی", residentName = "پیمان صادقی",
                mobile = "09126666106", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "627412113322", bankName = "اقتصاد نوین", initialDebt = 0L
            ),
            BuildingUnit(
                id = 7L, buildingId = 1L, unitNumber = "۷", floor = 3, area = 95.0,
                ownerName = "داود فرهمند", residentName = "داود فرهمند",
                mobile = "09127777107", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "621986105566", bankName = "سامان", initialDebt = 0L
            ),
            BuildingUnit(
                id = 8L, buildingId = 1L, unitNumber = "۸", floor = 3, area = 105.0,
                ownerName = "مجید کیانی", residentName = "مجید کیانی",
                mobile = "09128888108", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "502229119900", bankName = "پاسارگاد", initialDebt = 650_000L
            ),
            BuildingUnit(
                id = 9L, buildingId = 1L, unitNumber = "۹", floor = 3, area = 88.0,
                ownerName = "علی رسولی", tenantName = "وحید نادری", residentName = "وحید نادری",
                mobile = "09129999109", residencyStatus = "TENANT_OCCUPIED",
                cardNumber = "603770114488", bankName = "کشاورزی", initialDebt = 0L
            ),
            BuildingUnit(
                id = 10L, buildingId = 1L, unitNumber = "۱۰", floor = 4, area = 95.0,
                ownerName = "حسین پورمحمد", residentName = "حسین پورمحمد",
                mobile = "09131111110", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "585983102233", bankName = "تجارت", initialDebt = 0L
            ),
            BuildingUnit(
                id = 11L, buildingId = 1L, unitNumber = "۱۱", floor = 4, area = 105.0,
                ownerName = "مهدی خسروی", residentName = "مهدی خسروی",
                mobile = "09132222111", residencyStatus = "VACANT",
                cardNumber = "", bankName = "", initialDebt = 0L,
                notes = "واحد خالی است، طبق مصوبه ۵۰٪ شارژ برای مشاعات منظور می‌شود."
            ),
            BuildingUnit(
                id = 12L, buildingId = 1L, unitNumber = "۱۲", floor = 4, area = 120.0,
                ownerName = "علیرضا رادپور (مدیر)", residentName = "علیرضا رادپور",
                mobile = "09120000112", residencyStatus = "OWNER_OCCUPIED",
                cardNumber = "610433701234", bankName = "ملت", initialDebt = 0L,
                notes = "واحد مدیر ساختمان"
            )
        )
        unitDao.insertUnits(units)

        // General Notes
        val notesDao = database.notesDao()
        notesDao.insertOrUpdateAnnualNote(
            UnitAnnualNote(
                unitId = 4L, year = 1403,
                generalNote = "توافق شده بدهی معوق سال قبل تا پایان شهریور در ۳ قسط پرداخت شود."
            )
        )
        notesDao.insertOrUpdateAnnualNote(
            UnitAnnualNote(
                unitId = 11L, year = 1403,
                generalNote = "واحد خالی از سکنه است، فقط سهم هزینه تعمیرات و نظافت منظور می‌شود."
            )
        )

        // Monthly Notes
        notesDao.insertOrUpdateMonthNote(
            MonthChargeNote(
                unitId = 2L, year = 1403, monthIndex = 3,
                note = "پرداخت با ۵ روز تأخیر با هماهنگی انجام شد."
            )
        )
        notesDao.insertOrUpdateMonthNote(
            MonthChargeNote(
                unitId = 4L, year = 1403, monthIndex = 4,
                note = "مبلغ ۳۰۰,۰۰۰ علی‌الحساب واریز شد، مابقی ماه بعد تسویه می‌شود."
            )
        )

        // Charge Records for 1403 (Months 1 to 12)
        val chargeDao = database.chargeDao()
        val chargeList = mutableListOf<ChargeRecord>()
        val baseCharge = 650_000L

        for (u in units) {
            for (m in 1..12) {
                val status: String
                val paid: Long
                val date: String
                when {
                    m <= 3 -> {
                        // Past months (Farvardin, Ordibehesht, Khordad) mostly paid
                        if (u.id == 4L && m == 3) {
                            status = "PARTIAL"
                            paid = 350_000L
                            date = "1403/03/25"
                        } else if (u.id == 11L) {
                            status = "EXEMPT"
                            paid = 325_000L
                            date = "1403/03/01"
                        } else {
                            status = "PAID"
                            paid = baseCharge
                            date = "1403/0${m}/10"
                        }
                    }
                    m in 4..5 -> {
                        // Tir, Mordad
                        if (u.id in listOf(1L, 3L, 5L, 6L, 7L, 10L, 12L)) {
                            status = "PAID"
                            paid = baseCharge
                            date = "1403/0${m}/12"
                        } else if (u.id == 4L && m == 4) {
                            status = "PARTIAL"
                            paid = 300_000L
                            date = "1403/04/20"
                        } else {
                            status = "UNPAID"
                            paid = 0L
                            date = ""
                        }
                    }
                    m == 6 -> {
                        // Shahrivar (Current month)
                        if (u.id in listOf(1L, 3L, 12L)) {
                            status = "PAID"
                            paid = baseCharge
                            date = "1403/06/05"
                        } else {
                            status = "UNPAID"
                            paid = 0L
                            date = ""
                        }
                    }
                    else -> {
                        // Future months
                        status = "UNPAID"
                        paid = 0L
                        date = ""
                    }
                }

                chargeList.add(
                    ChargeRecord(
                        unitId = u.id,
                        year = 1403,
                        monthIndex = m,
                        status = status,
                        targetAmount = if (u.id == 11L) 325_000L else baseCharge,
                        paidAmount = paid,
                        paymentDate = date,
                        trackingCode = if (paid > 0) "TRK-${140300 + m}-${u.id}" else ""
                    )
                )
            }
        }
        chargeDao.insertOrUpdateCharges(chargeList)

        // Custom Ledgers
        val ledgerDao = database.ledgerDao()
        val ledgers = listOf(
            Ledger(
                id = 1L, buildingId = 1L, name = "دفتر معین گاز", code = "5101",
                category = "UTILITY", keywords = "فیش گاز,قبض گاز,پرداخت گاز,شرکت ملی گاز",
                colorHex = "#EF4444", iconName = "local_fire_department",
                description = "هزینه گاز موتورخانه و سیستم گرمایش مرکزی"
            ),
            Ledger(
                id = 2L, buildingId = 1L, name = "دفتر معین آب", code = "5102",
                category = "UTILITY", keywords = "فیش آب,قبض آب,پرداخت آب,آب و فاضلاب",
                colorHex = "#0EA5E9", iconName = "water_drop",
                description = "هزینه آب مصرفی مشترک و مشاعات ساختمان"
            ),
            Ledger(
                id = 3L, buildingId = 1L, name = "دفتر معین برق", code = "5103",
                category = "UTILITY", keywords = "فیش برق,قبض برق,پرداخت برق,شرکت توزیع برق",
                colorHex = "#F59E0B", iconName = "bolt",
                description = "هزینه برق راه‌پله، آسانسور و موتورخانه"
            ),
            Ledger(
                id = 4L, buildingId = 1L, name = "دفتر معین آسانسور", code = "5201",
                category = "MAINTENANCE", keywords = "آسانسور,سرویس آسانسور,بیمه آسانسور,تعمیر آسانسور",
                colorHex = "#8B5CF6", iconName = "elevator",
                description = "سرویس ماهیانه، استاندارد و تعمیرات آسانسور"
            ),
            Ledger(
                id = 5L, buildingId = 1L, name = "دفتر معین نظافت و سرایداری", code = "5202",
                category = "EXPENSE", keywords = "نظافت,سرایداری,شستشو,مواد شوینده",
                colorHex = "#10B981", iconName = "cleaning_services",
                description = "حق‌الزحمه نظافت هفتگی و وسایل شوینده"
            ),
            Ledger(
                id = 6L, buildingId = 1L, name = "دفتر معین تعمیرات و تأسیسات", code = "5203",
                category = "MAINTENANCE", keywords = "تعمیرات,پمپ آب,موتورخانه,لوله کشی,تاسیسات",
                colorHex = "#EC4899", iconName = "build",
                description = "هزینه‌های پمپ آب، مخزن، مشعل و لوله‌کشی"
            ),
            Ledger(
                id = 7L, buildingId = 1L, name = "دفتر معین باغبانی و محوطه", code = "5204",
                category = "EXPENSE", keywords = "باغبانی,فضای سبز,گلکاری,کود",
                colorHex = "#84CC16", iconName = "yard",
                description = "رسیدگی به حیاط و باغچه ورودی"
            )
        )
        ledgerDao.insertLedgers(ledgers)

        // Bank & Cash accounts
        val bankDao = database.bankDao()
        val bankAccounts = listOf(
            BankAccount(
                id = 1L, buildingId = 1L, title = "حساب جاری شارژ ساختمان",
                bankName = "بانک ملت", accountNumber = "1234567890",
                cardNumber = "6104337890123456", shebaNumber = "IR120120000000001234567890",
                currentBalance = 18_450_000L, isCashDesk = false
            ),
            BankAccount(
                id = 2L, buildingId = 1L, title = "حساب ذخیره اندوخته و اضطراری",
                bankName = "بانک پاسارگاد", accountNumber = "9876543210",
                cardNumber = "5022291098765432", shebaNumber = "IR980570000000009876543210",
                currentBalance = 42_000_000L, isCashDesk = false
            ),
            BankAccount(
                id = 3L, buildingId = 1L, title = "صندوق نقد و تنخواه مدیر",
                bankName = "صندوق ساختمان", accountNumber = "CASH-01",
                cardNumber = "", shebaNumber = "",
                currentBalance = 3_200_000L, isCashDesk = true
            )
        )
        bankDao.insertAccount(bankAccounts[0])
        bankDao.insertAccount(bankAccounts[1])
        bankDao.insertAccount(bankAccounts[2])

        // Bank Statement Transactions (with auto-detectable phrases)
        val sampleTransactions = listOf(
            BankTransaction(
                id = 1L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/06/05", time = "10:15", docNumber = "882101",
                trackingNumber = "449102",
                description = "واریز شارژ شهریور واحد ۱ سعیدی ۶۰۳۷",
                depositAmount = 650_000L, withdrawalAmount = 0L, balance = 18_450_000L,
                matchedType = "RESIDENT_CHARGE", matchedUnitId = 1L, isConfirmed = true
            ),
            BankTransaction(
                id = 2L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/06/04", time = "14:30", docNumber = "882098",
                trackingNumber = "771822",
                description = "فیش گاز دوره مرداد ساختمان بهارستان پلاک ۱۲",
                depositAmount = 0L, withdrawalAmount = 2_850_000L, balance = 17_800_000L,
                matchedType = "UTILITY_BILL", matchedLedgerId = 1L, isConfirmed = true
            ),
            BankTransaction(
                id = 3L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/06/02", time = "11:05", docNumber = "882045",
                trackingNumber = "319022",
                description = "فیش برق مشاعات و آسانسور دوره تیر",
                depositAmount = 0L, withdrawalAmount = 1_420_000L, balance = 20_650_000L,
                matchedType = "UTILITY_BILL", matchedLedgerId = 3L, isConfirmed = true
            ),
            BankTransaction(
                id = 4L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/06/01", time = "09:20", docNumber = "881990",
                trackingNumber = "990184",
                description = "فیش آب مصرفی دوماهه ساختمان بهارستان",
                depositAmount = 0L, withdrawalAmount = 3_150_000L, balance = 22_070_000L,
                matchedType = "UTILITY_BILL", matchedLedgerId = 2L, isConfirmed = true
            ),
            BankTransaction(
                id = 5L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/05/28", time = "16:40", docNumber = "881850",
                trackingNumber = "552190",
                description = "واریز اینترنتی واحد ۴ رضا حسینی کارت ۶۰۳۷۶۹",
                depositAmount = 300_000L, withdrawalAmount = 0L, balance = 25_220_000L,
                matchedType = "RESIDENT_CHARGE", matchedUnitId = 4L, isConfirmed = true
            ),
            BankTransaction(
                id = 6L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/05/25", time = "18:10", docNumber = "881799",
                trackingNumber = "220194",
                description = "سرویس ماهیانه آسانسور مهندس افشار",
                depositAmount = 0L, withdrawalAmount = 1_200_000L, balance = 24_920_000L,
                matchedType = "CUSTOM_EXPENSE", matchedLedgerId = 4L, isConfirmed = true
            ),
            BankTransaction(
                id = 7L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/05/20", time = "12:00", docNumber = "881640",
                trackingNumber = "882310",
                description = "واریز پایا واحد ۸ مجید کیانی کارت ۵۰۲۲۲۹",
                depositAmount = 650_000L, withdrawalAmount = 0L, balance = 26_120_000L,
                matchedType = "RESIDENT_CHARGE", matchedUnitId = 8L, isConfirmed = false
            ),
            BankTransaction(
                id = 8L, buildingId = 1L, bankAccountId = 1L,
                jalaliDate = "1403/05/18", time = "15:20", docNumber = "881512",
                trackingNumber = "774120",
                description = "قبض گاز دوره جدید شرکت گاز استان تهران",
                depositAmount = 0L, withdrawalAmount = 2_100_000L, balance = 25_470_000L,
                matchedType = "UTILITY_BILL", matchedLedgerId = 1L, isConfirmed = false
            )
        )
        database.bankDao().insertTransactions(sampleTransactions)

        // Utility Bills
        val billDao = database.billDao()
        val sampleBills = listOf(
            UtilityBill(
                id = 1L, buildingId = 1L, billType = "گاز",
                billId = "982341001", paymentId = "48102390", period = "مرداد ۱۴۰۳",
                issueDate = "1403/05/20", dueDate = "1403/06/10",
                amount = 2_850_000L, paidAmount = 2_850_000L, paidDate = "1403/06/04",
                status = "PAID", ledgerId = 1L, description = "گاز مشاعات و موتورخانه"
            ),
            UtilityBill(
                id = 2L, buildingId = 1L, billType = "برق",
                billId = "551209341", paymentId = "19230184", period = "تیر ۱۴۰۳",
                issueDate = "1403/05/15", dueDate = "1403/06/05",
                amount = 1_420_000L, paidAmount = 1_420_000L, paidDate = "1403/06/02",
                status = "PAID", ledgerId = 3L, description = "برق راه پله و آسانسور"
            ),
            UtilityBill(
                id = 3L, buildingId = 1L, billType = "آب",
                billId = "771829004", paymentId = "33910245", period = "تیر و مرداد ۱۴۰۳",
                issueDate = "1403/05/25", dueDate = "1403/06/15",
                amount = 3_150_000L, paidAmount = 3_150_000L, paidDate = "1403/06/01",
                status = "PAID", ledgerId = 2L, description = "آب مشترک ۱۲ واحد"
            ),
            UtilityBill(
                id = 4L, buildingId = 1L, billType = "گاز",
                billId = "982341002", paymentId = "48102391", period = "شهریور ۱۴۰۳",
                issueDate = "1403/06/01", dueDate = "1403/06/25",
                amount = 2_100_000L, paidAmount = 0L, paidDate = "",
                status = "UNPAID", ledgerId = 1L, description = "قبض گاز دوره شهریور (در انتظار پرداخت)"
            )
        )
        billDao.insertBills(sampleBills)

        // Settings - default to face not selected initially so user sees the 4 faces screen on first run
        val settingsDao = database.settingsDao()
        settingsDao.saveSettings(
            AppSettings(
                id = 1,
                selectedFace = 1,
                isFaceSelectedOnboarded = false, // Set false to show Face Selection Screen first!
                activeBuildingId = 1L,
                themeMode = "SYSTEM",
                accentColorHex = "#1E3A8A",
                fontSizeScale = 1.0f,
                tableDensity = "COMFORTABLE",
                currency = "TOMAN"
            )
        )
    }
}
