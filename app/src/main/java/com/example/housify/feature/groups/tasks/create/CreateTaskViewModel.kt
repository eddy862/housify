package com.example.housify.feature.groups.tasks.create

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.model.PredefinedSpace
import com.example.housify.domain.model.Space
import com.example.housify.domain.model.User
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.domain.repository_interfaces.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateTaskUiState(
    val availableSpaces: List<Space> = emptyList(),
    val availableUsers: List<User> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val createTaskError: String? = null,
    val createTaskLoading: Boolean = false
)

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val groupRepository: GroupRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    var uiState by mutableStateOf(CreateTaskUiState())
        private set

    init {
        loadInitialData()
    }

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    private val _onBackEvent = MutableSharedFlow<Unit>()
    val onBackEvent = _onBackEvent.asSharedFlow()

    fun createTask(
        title: String,
        assignees: List<User>,
        space: Space?,
        repeatSetting: Repeat,
        rotationEnabled: Boolean,
        startFrom: Long?
    ) {
        uiState = uiState.copy(
            createTaskError = null,
            createTaskLoading = true
        )

        if (title.isBlank()) {
            viewModelScope.launch {
                uiState = uiState.copy(
                    createTaskError = "Task title cannot be empty.",
                    createTaskLoading = false
                )
            }
            return
        }

        if (startFrom == null) {
            viewModelScope.launch {
                uiState = uiState.copy(
                    createTaskError = "Please select a start date.",
                    createTaskLoading = false
                )
            }
            return
        }

        if (startFrom < System.currentTimeMillis()) {
            viewModelScope.launch {
                uiState = uiState.copy(
                    createTaskError = "Start date must be in the future.",
                    createTaskLoading = false
                )
            }
            return
        }

        if (assignees.isEmpty()) {
            viewModelScope.launch {
                uiState =
                    uiState.copy(
                        createTaskError = "Task must have at least one assignee.",
                        createTaskLoading = false
                    )
            }
            return
        }

        if (rotationEnabled && repeatSetting == Repeat.NONE) {
            viewModelScope.launch {
                uiState = uiState.copy(
                    createTaskError = "Rotation must be enabled when repeat is set.",
                    createTaskLoading = false
                )
            }
            return
        }

        if (assignees.size < 2 && rotationEnabled) {
            viewModelScope.launch {
                uiState =
                    uiState.copy(
                        createTaskError = "Rotation must be disabled when there are less than 2 assignees.",
                        createTaskLoading = false
                    )
            }
            return
        }

        if (space == null) {
            viewModelScope.launch {
                uiState = uiState.copy(
                    createTaskError = "Please select a space.",
                    createTaskLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            taskRepository.createTask(
                groupId = groupId,
                title = title,
                place = space.name,
                assigneeIds = assignees.map { it.id },
                startDate = startFrom,
                isRotational = rotationEnabled,
                repeat = repeatSetting
            ).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        uiState = uiState.copy(
                            createTaskLoading = false,
                            createTaskError = null
                        )
                        _successMessage.emit("Task created successfully")
                        _onBackEvent.emit(Unit)
                    }

                    is Resource.Error -> {
                        uiState = uiState.copy(
                            createTaskLoading = false,
                            createTaskError = resource.message ?: "An unknown error occurred"
                        )
                    }

                    is Resource.Loading -> {
                        uiState = uiState.copy(
                            createTaskLoading = true,
                            createTaskError = null
                        )
                    }
                }
            }
        }
    }

    fun refreshData() {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            groupRepository.getGroupAllUsers(groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            uiState = uiState.copy(
                                availableSpaces = PredefinedSpace.getSortedList(),
                                availableUsers = resource.data ?: emptyList(),
                                loading = false,
                                error = null
                            )
                        }

                        is Resource.Error -> {
                            uiState = uiState.copy(
                                loading = false,
                                error = resource.message ?: "An unknown error occurred"
                            )
                        }

                        is Resource.Loading -> {
                            uiState = uiState.copy(
                                loading = true,
                                error = null
                            )
                        }
                    }
                }
        }
    }
}
