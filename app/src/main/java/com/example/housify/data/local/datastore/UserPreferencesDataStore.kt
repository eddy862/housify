package com.example.housify.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DEFAULT_HOST_IP = "10.0.2.2"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Define a key for the user ID
    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val HOST_IP = stringPreferencesKey("host_ip")
    }

    // Function to save the user ID
    suspend fun saveUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun setHostIp(hostIp: String) {
        context.dataStore.edit { preferences ->
            preferences[HOST_IP] = hostIp
        }
    }

    val getHostIp: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[HOST_IP] ?: DEFAULT_HOST_IP
        }

    // Function to clear the user ID (on sign-out)
    suspend fun clearUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }

    // A Flow to observe the user ID, so other parts of the app can react to changes
    val getUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }
}