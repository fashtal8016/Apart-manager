package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val phone: String = "",
    val managerName: String = "",
    val floorCount: Int = 4,
    val unitCount: Int = 12,
    val baseMonthlyCharge: Long = 500_000L,
    val chargeCalcMethod: String = "FIXED", // FIXED, METRAGE, RESIDENTS, COEFFICIENT, EQUAL_SPLIT
    val fiscalYear: Int = 1403,
    val currency: String = "TOMAN", // TOMAN, RIAL
    val notes: String = ""
)

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = Building::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class BuildingUnit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val unitNumber: String,
    val floor: Int,
    val area: Double, // Metrage
    val ownerName: String,
    val tenantName: String = "",
    val residentName: String,
    val mobile: String,
    val phone2: String = "",
    val residentCount: Int = 2,
    val chargeCoefficient: Double = 1.0,
    val residencyStatus: String = "OWNER_OCCUPIED", // OWNER_OCCUPIED, TENANT_OCCUPIED, VACANT
    val parking: String = "دارد",
    val storage: String = "دارد",
    val cardNumber: String = "", // e.g. 6037... for bank auto-matching
    val bankName: String = "",
    val notes: String = "",
    val initialDebt: Long = 0L
)

@Entity(
    tableName = "unit_annual_notes",
    indices = [Index(value = ["unitId", "year"], unique = true)]
)
data class UnitAnnualNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val year: Int,
    val generalNote: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "month_charge_notes",
    indices = [Index(value = ["unitId", "year", "monthIndex"], unique = true)]
)
data class MonthChargeNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val year: Int,
    val monthIndex: Int, // 1 to 12 (Farvardin to Esfand)
    val note: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "charge_records",
    indices = [Index(value = ["unitId", "year", "monthIndex"], unique = true)]
)
data class ChargeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: Long,
    val year: Int,
    val monthIndex: Int, // 1 = فروردین ... 12 = اسفند
    val status: String = "UNPAID", // PAID (پرداخت کامل), PARTIAL (پرداخت جزئی), UNPAID (پرداخت‌نشده), EXEMPT (معاف), REVIEW (نیازمند بررسی)
    val targetAmount: Long = 0L,
    val paidAmount: Long = 0L,
    val paymentDate: String = "",
    val trackingCode: String = "",
    val paymentMethod: String = "بانک", // بانک, کارت‌به‌کارت, نقدی
    val isAutoMatched: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "ledgers",
    foreignKeys = [
        ForeignKey(
            entity = Building::class,
            parentColumns = ["id"],
            childColumns = ["buildingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("buildingId")]
)
data class Ledger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val name: String,
    val code: String,
    val category: String, // EXPENSE, UTILITY, INCOME, MAINTENANCE
    val distributionMethod: String = "EQUAL", // EQUAL, METRAGE, RESIDENTS, FIXED
    val keywords: String = "", // Comma-separated for auto-detection (e.g. "فیش گاز,قبض گاز,پرداخت گاز")
    val colorHex: String = "#3B82F6",
    val iconName: String = "receipt",
    val isActive: Boolean = true,
    val description: String = ""
)

@Entity(tableName = "bank_accounts")
data class BankAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val title: String,
    val bankName: String,
    val accountNumber: String,
    val cardNumber: String = "",
    val shebaNumber: String = "",
    val currentBalance: Long = 0L,
    val isCashDesk: Boolean = false // صندوق / تنخواه
)

@Entity(tableName = "bank_transactions")
data class BankTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val bankAccountId: Long,
    val jalaliDate: String,
    val time: String = "",
    val docNumber: String = "",
    val trackingNumber: String = "",
    val description: String,
    val depositAmount: Long = 0L,
    val withdrawalAmount: Long = 0L,
    val balance: Long = 0L,
    val matchedType: String = "NONE", // RESIDENT_CHARGE, UTILITY_BILL, CUSTOM_EXPENSE, NONE
    val matchedUnitId: Long? = null,
    val matchedLedgerId: Long? = null,
    val isConfirmed: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "utility_bills")
data class UtilityBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val billType: String, // آب, برق, گاز, نظافت, آسانسور, سایر
    val billId: String = "",
    val paymentId: String = "",
    val period: String = "",
    val issueDate: String = "",
    val dueDate: String = "",
    val amount: Long = 0L,
    val paidAmount: Long = 0L,
    val paidDate: String = "",
    val bankAccountId: Long? = null,
    val status: String = "UNPAID", // UNPAID, PAID, PARTIAL
    val ledgerId: Long? = null,
    val description: String = ""
)

@Entity(tableName = "accounting_entries")
data class AccountingEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingId: Long,
    val docNumber: Long,
    val jalaliDate: String,
    val description: String,
    val debitAccount: String, // نام حساب بدهکار
    val creditAccount: String, // نام حساب بستانکار
    val amount: Long,
    val unitId: Long? = null,
    val ledgerId: Long? = null,
    val trackingCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val selectedFace: Int = 1, // 1 = Classic, 2 = Luxury, 3 = Minimal, 4 = Financial
    val isFaceSelectedOnboarded: Boolean = false,
    val activeBuildingId: Long = 1L,
    val themeMode: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val accentColorHex: String = "#1E3A8A",
    val fontSizeScale: Float = 1.0f,
    val tableDensity: String = "COMFORTABLE", // COMPACT, COMFORTABLE
    val currency: String = "TOMAN" // TOMAN, RIAL
)
