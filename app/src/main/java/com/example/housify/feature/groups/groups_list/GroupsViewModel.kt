package com.example.housify.feature.groups.groups_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.model.Group
import com.example.housify.domain.repository_interfaces.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupUiState(
    val groups: List<Group> = emptyList(),
    val getGroupsLoading: Boolean = false,
    val getGroupsError: String? = null,
    val createGroupLoading: Boolean = false,
    val createGroupError: String? = null,
    val joinGroupLoading: Boolean = false,
    val joinGroupError: String? = null
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GroupUiState())
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        fetchLatestData()
    }

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    private val _closeDialogEvent = MutableSharedFlow<Unit>()
    val closeDialogEvent = _closeDialogEvent.asSharedFlow()

    fun createGroup(name: String) {
        if (name.isBlank()) {
            _uiState.update {
                it.copy(
                    createGroupError = "Group name cannot be empty"
                )
            }
            return
        }

        viewModelScope.launch {
            groupRepository.createGroup(name)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    createGroupError = null,
                                    createGroupLoading = false
                                )
                            }

                            _successMessage.emit("Successfully created group '$name'")
                            _closeDialogEvent.emit(Unit)
                            fetchLatestData()
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = resource.message
                                        ?: "An unknown error occurred",
                                    joinGroupLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupLoading = true,
                                    joinGroupError = null
                                )
                            }
                        }
                    }
                }
        }
    }

    fun joinGroup(invitationCode: String) {
        if (invitationCode.isBlank()) {
            _uiState.update {
                it.copy(
                    joinGroupError = "Invitation code cannot be empty"
                )
            }
            return
        }

        if (invitationCode.length < 7) {
            _uiState.update {
                it.copy(
                    joinGroupError = "Invitation code should be 7 characters long"
                )
            }
            return
        }

        viewModelScope.launch {
            groupRepository.joinGroup(invitationCode)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = null,
                                    joinGroupLoading = false
                                )
                            }

                            _successMessage.emit("Successfully joined group")
                            _closeDialogEvent.emit(Unit)
                            fetchLatestData()
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupError = resource.message
                                        ?: "An unknown error occurred",
                                    joinGroupLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    joinGroupLoading = true,
                                    joinGroupError = null
                                )
                            }
                        }
                    }
                }
        }
    }

    fun refreshData() {
        fetchLatestData()
    }

    private fun fetchLatestData() {
        viewModelScope.launch {
            groupRepository.getGroups()
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    groups = resource.data ?: emptyList(),
                                    getGroupsLoading = false,
                                    getGroupsError = null
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    getGroupsError = resource.message
                                        ?: "An unknown error occurred",
                                    getGroupsLoading = false,
                                    groups = resource.data ?: it.groups
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    getGroupsLoading = true,
                                    getGroupsError = null
                                )
                            }
                        }
                    }
                }
        }
    }
}