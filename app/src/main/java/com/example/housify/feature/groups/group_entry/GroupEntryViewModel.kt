package com.example.housify.feature.groups.group_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.model.GroupEntry
import com.example.housify.domain.model.User
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

data class GroupEntryUiState(
    val groupEntry: GroupEntry? = null,
    val getGroupEntryLoading: Boolean = false,
    val getGroupEntryError: String? = null,
    val editGroupNameLoading: Boolean = false,
    val editGroupNameError: String? = null,
    val deleteGroupLoading: Boolean = false,
    val deleteGroupError: String? = null,
    val removeMemberLoading: Boolean = false,
    val removeMemberError: String? = null,
    val memberLeaveGroupLoading: Boolean = false,
    val memberLeaveGroupError: String? = null
)

@HiltViewModel
class GroupEntryViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val _uiState = MutableStateFlow(GroupEntryUiState())
    val uiState: StateFlow<GroupEntryUiState> = _uiState.asStateFlow()

    init {
        fetchLatestData()
    }

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    private val _closeDialogEvent = MutableSharedFlow<Unit>()
    val closeDialogEvent = _closeDialogEvent.asSharedFlow()

    private val _onBackEvent = MutableSharedFlow<Unit>()
    val onBackEvent = _onBackEvent.asSharedFlow()

    fun getTasksGroupedBySpace() = _uiState.value.groupEntry?.tasks?.groupBy { it.space }

    fun editGroupName(name: String) {
        if (name.isEmpty()) {
            _uiState.update {
                it.copy(
                    editGroupNameError = "Group name cannot be empty",
                    editGroupNameLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            groupRepository.editGroupName(
                groupId = groupId,
                newGroupName = name
            )
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    editGroupNameError = null,
                                    editGroupNameLoading = false
                                )
                            }

                            _successMessage.emit("Successfully edited group name to '$name'")
                            _closeDialogEvent.emit(Unit)
                            fetchLatestData()
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    editGroupNameError = resource.message
                                        ?: "An unknown error occurred",
                                    editGroupNameLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    editGroupNameError = null,
                                    editGroupNameLoading = true
                                )
                            }
                        }
                    }
                }

        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    deleteGroupError = null,
                                    deleteGroupLoading = false
                                )
                            }

                            _successMessage.emit("Successfully deleted group '${_uiState.value.groupEntry!!.name}'")
                            _closeDialogEvent.emit(Unit)
                            _onBackEvent.emit(Unit)
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    deleteGroupError = resource.message
                                        ?: "An unknown error occurred",
                                    deleteGroupLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    deleteGroupError = null,
                                    deleteGroupLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }

    fun removeMember(user: User) {
        viewModelScope.launch {
            groupRepository.removeMember(
                groupId = groupId,
                memberId = user.id
            )
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    removeMemberError = null,
                                    removeMemberLoading = false
                                )
                            }

                            _successMessage.emit("Successfully removed member '${user.name}'")
                            _closeDialogEvent.emit(Unit)
                            fetchLatestData()
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    removeMemberError = resource.message
                                        ?: "An unknown error occurred",
                                    removeMemberLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    removeMemberError = null,
                                    removeMemberLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }

    fun memberLeaveGroup() {
        viewModelScope.launch {
            groupRepository.leaveGroup(groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    memberLeaveGroupError = null,
                                    memberLeaveGroupLoading = false,
                                )
                            }

                            _successMessage.emit("Successfully left group '${_uiState.value.groupEntry!!.name}'")
                            _closeDialogEvent.emit(Unit)
                            _onBackEvent.emit(Unit)
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    memberLeaveGroupError = resource.message
                                        ?: "An unknown error occurred",
                                    memberLeaveGroupLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    memberLeaveGroupError = null,
                                    memberLeaveGroupLoading = true
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
            groupRepository.getGroupEntry(groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    getGroupEntryError = null,
                                    getGroupEntryLoading = false,
                                    groupEntry = resource.data
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    getGroupEntryError = resource.message
                                        ?: "An unknown error occurred",
                                    getGroupEntryLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    getGroupEntryError = null,
                                    getGroupEntryLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }
}