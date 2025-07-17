package com.utkarsh.chatapp.utilities

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

// Extension property for DataStore on Context.
val Context.dataStore by preferencesDataStore(name = "settings")

// Global key for "COMPLETED_SETUP" preference.
val HAS_COMPLETED_SETUP_KEY = booleanPreferencesKey("COMPLETED_SETUP")

@Composable
fun rememberHasCompletedSetup(): Boolean {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val prefsFlow = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[HAS_COMPLETED_SETUP_KEY] ?: false
        }
    val hasCompletedSetup by prefsFlow.collectAsState(initial = false)
    return hasCompletedSetup
}
