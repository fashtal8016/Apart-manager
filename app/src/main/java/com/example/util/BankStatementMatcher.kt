package com.example.util

import com.example.data.model.BuildingUnit
import com.example.data.model.Ledger

data class MatchResult(
    val matchedType: String, // RESIDENT_CHARGE, UTILITY_BILL, CUSTOM_EXPENSE, NONE
    val matchedUnit: BuildingUnit? = null,
    val matchedLedger: Ledger? = null,
    val confidence: String, // HIGH, MEDIUM, LOW, NONE
    val explanation: String
)

object BankStatementMatcher {

    /**
     * Normalizes Persian/Arabic strings for fuzzy comparison:
     * Removes diacritics, unifies ی / ي, ک / ك, normalizes spaces and digits.
     */
    fun normalizeText(input: String): String {
        return JalaliCalendar.toEnglishDigits(input)
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('ة', 'ه')
            .replace('آ', 'ا')
            .replace("ـ", "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }

    /**
     * Matches a transaction description against residents and ledgers (water, power, gas, custom).
     */
    fun matchTransaction(
        description: String,
        isDeposit: Boolean,
        amount: Long,
        units: List<BuildingUnit>,
        ledgers: List<Ledger>
    ): MatchResult {
        val normDesc = normalizeText(description)

        // 1. Check for utility bills / ledgers keywords first (usually withdrawals or bill payments)
        for (ledger in ledgers) {
            val keywordsList = ledger.keywords.split(",")
                .map { normalizeText(it) }
                .filter { it.isNotBlank() }

            for (kw in keywordsList) {
                if (normDesc.contains(kw)) {
                    return MatchResult(
                        matchedType = if (ledger.category == "UTILITY") "UTILITY_BILL" else "CUSTOM_EXPENSE",
                        matchedLedger = ledger,
                        confidence = "HIGH",
                        explanation = "شناسایی کلمه کلیدی «$kw» متعلق به ${ledger.name}"
                    )
                }
            }
        }

        // Standard water, electricity, gas phrases
        if (normDesc.contains("گاز") && (normDesc.contains("قبض") || normDesc.contains("فیش") || normDesc.contains("پرداخت"))) {
            val gasLedger = ledgers.firstOrNull { it.name.contains("گاز") }
            return MatchResult(
                matchedType = "UTILITY_BILL",
                matchedLedger = gasLedger,
                confidence = "HIGH",
                explanation = "تشخیص خودکار فیش/قبض گاز"
            )
        }
        if (normDesc.contains("آب") && (normDesc.contains("قبض") || normDesc.contains("فیش") || normDesc.contains("پرداخت"))) {
            val waterLedger = ledgers.firstOrNull { it.name.contains("آب") }
            return MatchResult(
                matchedType = "UTILITY_BILL",
                matchedLedger = waterLedger,
                confidence = "HIGH",
                explanation = "تشخیص خودکار فیش/قبض آب"
            )
        }
        if (normDesc.contains("برق") && (normDesc.contains("قبض") || normDesc.contains("فیش") || normDesc.contains("پرداخت"))) {
            val powerLedger = ledgers.firstOrNull { it.name.contains("برق") }
            return MatchResult(
                matchedType = "UTILITY_BILL",
                matchedLedger = powerLedger,
                confidence = "HIGH",
                explanation = "تشخیص خودکار فیش/قبض برق"
            )
        }

        // 2. If it's a deposit (واریز), check for resident matching
        if (isDeposit || normDesc.contains("شارژ") || normDesc.contains("واریز")) {
            // Check Card Number match
            for (unit in units) {
                if (unit.cardNumber.isNotBlank()) {
                    val cleanCard = unit.cardNumber.replace(" ", "").replace("-", "")
                    val last4 = if (cleanCard.length >= 4) cleanCard.takeLast(4) else cleanCard
                    val first6 = if (cleanCard.length >= 6) cleanCard.take(6) else cleanCard

                    if (normDesc.contains(cleanCard) || (last4.length == 4 && normDesc.contains(last4))) {
                        return MatchResult(
                            matchedType = "RESIDENT_CHARGE",
                            matchedUnit = unit,
                            confidence = "HIGH",
                            explanation = "تطبیق شماره کارت واحد ${unit.unitNumber} (${unit.residentName})"
                        )
                    }
                }
            }

            // Check Resident or Owner name match
            for (unit in units) {
                val normResident = normalizeText(unit.residentName)
                val normOwner = normalizeText(unit.ownerName)
                val normTenant = normalizeText(unit.tenantName)

                // Match by full resident name
                if (normResident.isNotBlank() && normDesc.contains(normResident)) {
                    return MatchResult(
                        matchedType = "RESIDENT_CHARGE",
                        matchedUnit = unit,
                        confidence = "HIGH",
                        explanation = "تطبیق نام ساکن: ${unit.residentName} (واحد ${unit.unitNumber})"
                    )
                }

                // Match by owner name
                if (normOwner.isNotBlank() && normDesc.contains(normOwner)) {
                    return MatchResult(
                        matchedType = "RESIDENT_CHARGE",
                        matchedUnit = unit,
                        confidence = "HIGH",
                        explanation = "تطبیق نام مالک: ${unit.ownerName} (واحد ${unit.unitNumber})"
                    )
                }

                // Match by tenant name
                if (normTenant.isNotBlank() && normDesc.contains(normTenant)) {
                    return MatchResult(
                        matchedType = "RESIDENT_CHARGE",
                        matchedUnit = unit,
                        confidence = "HIGH",
                        explanation = "تطبیق نام مستأجر: ${unit.tenantName} (واحد ${unit.unitNumber})"
                    )
                }

                // Check by individual family name if 2+ words
                val parts = unit.residentName.split(" ").filter { it.length > 2 }
                if (parts.size >= 2) {
                    val lastName = normalizeText(parts.last())
                    if (normDesc.contains(lastName) && (normDesc.contains("شارژ") || normDesc.contains("واحد"))) {
                        return MatchResult(
                            matchedType = "RESIDENT_CHARGE",
                            matchedUnit = unit,
                            confidence = "MEDIUM",
                            explanation = "تطبیق نام خانوادگی «$lastName» با واحد ${unit.unitNumber}"
                        )
                    }
                }
            }

            // Check Unit Number pattern (e.g. "واحد ۴", "واحد 4", "w4", "u4")
            for (unit in units) {
                val uNum = unit.unitNumber
                val normUNum = normalizeText(uNum)
                val patterns = listOf(
                    "واحد $normUNum",
                    "واحد$normUNum",
                    "vahed $normUNum",
                    "vahed$normUNum",
                    "w$normUNum",
                    "u$normUNum"
                )
                if (patterns.any { normDesc.contains(it) }) {
                    return MatchResult(
                        matchedType = "RESIDENT_CHARGE",
                        matchedUnit = unit,
                        confidence = "MEDIUM",
                        explanation = "تطبیق شماره واحد $uNum در شرح تراکنش"
                    )
                }
            }
        }

        return MatchResult(
            matchedType = "NONE",
            confidence = "NONE",
            explanation = "نیاز به بررسی و انتخاب دستی توسط مدیر"
        )
    }
}
