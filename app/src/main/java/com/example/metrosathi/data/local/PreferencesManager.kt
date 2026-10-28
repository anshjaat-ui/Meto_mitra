package com.example.metrosathi.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "metrosathi_prefs")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

data class AppSettings(
    val selectedCityId: String = "delhi",
    val selectedLanguage: String = "en", // "en" or "hi"
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val alarmAheadStations: Int = 1,
    val isPremiumUser: Boolean = false,
    val dataVersion: Int = 1
)

class PreferencesManager(private val context: Context) {
    companion object {
        val KEY_SELECTED_CITY = stringPreferencesKey("selected_city_id")
        val KEY_LANGUAGE = stringPreferencesKey("selected_language")
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        val KEY_ALARM_AHEAD_STATIONS = intPreferencesKey("alarm_ahead_stations")
        val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        val KEY_DATA_VERSION = intPreferencesKey("data_version")
    }

    val appSettings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val themeString = prefs[KEY_THEME] ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeString)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        AppSettings(
            selectedCityId = prefs[KEY_SELECTED_CITY] ?: "delhi",
            selectedLanguage = prefs[KEY_LANGUAGE] ?: "en",
            themeMode = themeMode,
            fontScale = prefs[KEY_FONT_SCALE] ?: 1.0f,
            alarmAheadStations = prefs[KEY_ALARM_AHEAD_STATIONS] ?: 1,
            isPremiumUser = prefs[KEY_IS_PREMIUM] ?: false,
            dataVersion = prefs[KEY_DATA_VERSION] ?: 1
        )
    }

    suspend fun setSelectedCity(cityId: String) {
        context.dataStore.edit { it[KEY_SELECTED_CITY] = cityId }
    }

    suspend fun setLanguage(langCode: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = langCode }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[KEY_FONT_SCALE] = scale }
    }

    suspend fun setAlarmAheadStations(count: Int) {
        context.dataStore.edit { it[KEY_ALARM_AHEAD_STATIONS] = count }
    }

    suspend fun setPremium(isPremium: Boolean) {
        context.dataStore.edit { it[KEY_IS_PREMIUM] = isPremium }
    }

    suspend fun setDataVersion(version: Int) {
        context.dataStore.edit { it[KEY_DATA_VERSION] = version }
    }
}
