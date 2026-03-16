package com.example.housify.feature.joinGroup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.repository_interfaces.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinGroupUiState(
    val groupName: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isJoinGroupLoading: Boolean = false,
    val joinGroupError: String? = null
)

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val invitationCode: String = checkNotNull(savedStateHandle["invitationCode"])

    private val _uiState = MutableStateFlow(JoinGroupUiState())
    val uiState: StateFlow<JoinGroupUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    private val _onBackEvent = MutableSharedFlow<Unit>()
    val onBackEvent = _onBackEvent.asSharedFlow()

    init {
        fetchLatestData()
    }

    fun refreshData() {
        fetchLatestData()
    }

    fun joinGroup() {
        viewModelScope.launch {
            groupRepository.joinGroup(invitationCode)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = null,
                                    isJoinGroupLoading = false,
                                )
                            }
                            _successMessage.emit("Successfully join group '${_uiState.value.groupName}'")
                            _onBackEvent.emit(Unit)
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = resource.message
                                        ?: "An unknown error occurred",
                                    isJoinGroupLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = null,
                                    isJoinGroupLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun fetchLatestData() {
        viewModelScope.launch {
            groupRepository.getGroupNameByInvitationCode(invitationCode)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    error = null,
                                    isLoading = false,
                                    groupName = resource.data
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    error = resource.message
                                        ?: "An unknown error occurred",
                                    isLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    error = null,
                                    isLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }
}