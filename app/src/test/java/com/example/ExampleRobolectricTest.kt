package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BuildingUnit
import com.example.data.model.Ledger
import com.example.util.BankStatementMatcher
import com.example.util.JalaliCalendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مدیر ساختمان کیوی", appName)
    }

    @Test
    fun `test Jalali calendar conversions and formatting`() {
        val jalali = JalaliCalendar.gregorianToJalali(2024, 8, 25)
        assertEquals(1403, jalali.year)
        assertEquals(6, jalali.month) // Shahrivar
        assertEquals("شهریور", jalali.monthName)

        val formatted = JalaliCalendar.formatAmount(650000L, "TOMAN", usePersianDigits = false)
        assertEquals("650,000 تومان", formatted)
    }

    @Test
    fun `test bank statement smart matching for water power gas and residents`() {
        val units = listOf(
            BuildingUnit(
                id = 4L, buildingId = 1L, unitNumber = "۴", floor = 2, area = 95.0,
                ownerName = "رضا حسینی", residentName = "رضا حسینی",
                mobile = "09124444104", cardNumber = "603769194455"
            )
        )
        val ledgers = listOf(
            Ledger(id = 1L, buildingId = 1L, name = "دفتر معین گاز", code = "5101", category = "UTILITY", keywords = "فیش گاز,قبض گاز"),
            Ledger(id = 2L, buildingId = 1L, name = "دفتر معین آب", code = "5102", category = "UTILITY", keywords = "فیش آب,قبض آب"),
            Ledger(id = 3L, buildingId = 1L, name = "دفتر معین برق", code = "5103", category = "UTILITY", keywords = "فیش برق,قبض برق")
        )

        // 1. Gas bill match
        val gasMatch = BankStatementMatcher.matchTransaction(
            description = "فیش گاز دوره مرداد پلاک ۱۲",
            isDeposit = false,
            amount = 2850000L,
            units = units,
            ledgers = ledgers
        )
        assertEquals("UTILITY_BILL", gasMatch.matchedType)
        assertNotNull(gasMatch.matchedLedger)
        assertEquals(1L, gasMatch.matchedLedger?.id)

        // 2. Resident charge deposit match
        val residentMatch = BankStatementMatcher.matchTransaction(
            description = "واریز شارژ واحد ۴ رضا حسینی کارت ۶۰۳۷۶۹",
            isDeposit = true,
            amount = 650000L,
            units = units,
            ledgers = ledgers
        )
        assertEquals("RESIDENT_CHARGE", residentMatch.matchedType)
        assertNotNull(residentMatch.matchedUnit)
        assertEquals(4L, residentMatch.matchedUnit?.id)
    }

    @Test
    fun `test SharedPreferences stores and retrieves selected face`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = com.example.util.KiwiPreferences(context)

        // Save Face 2 (Modern)
        prefs.saveSelectedFace(2)
        assertEquals(2, prefs.getSelectedFace())
        assertEquals("MODERN", prefs.getFaceName())
        assertEquals(true, prefs.isFaceOnboarded())

        // Save Face 4 (Financial)
        prefs.saveSelectedFace(4)
        assertEquals(4, prefs.getSelectedFace())
        assertEquals("FINANCIAL", prefs.getFaceName())
        assertEquals(true, prefs.isFaceOnboarded())
    }
}
