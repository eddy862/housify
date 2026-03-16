package com.example.housify.feature.hostIp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.data.local.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateHostIpViewModel @Inject constructor(
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {
    val hostIp = userPreferences.getHostIp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val error = MutableStateFlow<String?>(null)

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    fun updateHostIp(newIp: String) {
        if (newIp.isEmpty()) {
            error.value = "Host IP cannot be empty"
            return
        }

        viewModelScope.launch {
            userPreferences.setHostIp(newIp)
            error.value = null
            _successMessage.emit("Successfully updated host IP to $newIp")
        }
    }
}