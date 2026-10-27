package com.example.util

import android.content.Context
import android.content.SharedPreferences

class KiwiPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "kiwi_building_manager_prefs"
        const val KEY_SELECTED_FACE = "key_selected_face"
        const val KEY_FACE_NAME = "key_face_name"
        const val KEY_FACE_ONBOARDED = "key_face_onboarded"
        const val KEY_ACCENT_COLOR = "key_accent_color"
        const val KEY_CURRENCY = "key_currency"
    }

    fun saveSelectedFace(faceId: Int) {
        val faceName = when (faceId) {
            1 -> "CLASSIC"
            2 -> "MODERN"
            3 -> "MINIMAL"
            4 -> "FINANCIAL"
            else -> "CLASSIC"
        }
        prefs.edit()
            .putInt(KEY_SELECTED_FACE, faceId)
            .putString(KEY_FACE_NAME, faceName)
            .putBoolean(KEY_FACE_ONBOARDED, true)
            .apply()
    }

    fun getSelectedFace(): Int = prefs.getInt(KEY_SELECTED_FACE, 1)

    fun getFaceName(): String = prefs.getString(KEY_FACE_NAME, "CLASSIC") ?: "CLASSIC"

    fun isFaceOnboarded(): Boolean = prefs.getBoolean(KEY_FACE_ONBOARDED, false)

    fun resetFaceOnboarding() {
        prefs.edit()
            .putBoolean(KEY_FACE_ONBOARDED, false)
            .apply()
    }
}
