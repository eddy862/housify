package com.example.housify.feature.groups.tasks.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.domain.model.TaskDetails
import com.example.housify.domain.repository_interfaces.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailsUiState(
    val taskDetails: TaskDetails? = null,
    val getTaskLoading: Boolean = false,
    val getTaskError: String? = null,
    val deleteTaskLoading: Boolean = false,
    val deleteTaskError: String? = null
)

@HiltViewModel
class TaskDetailsViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(TaskDetailsUiState())
    val uiState: StateFlow<TaskDetailsUiState> = _uiState.asStateFlow()

    private val _successMessage = MutableSharedFlow<String>()
    val successMessage = _successMessage.asSharedFlow()

    private val _onBackEvent = MutableSharedFlow<Unit>()
    val onBackEvent = _onBackEvent.asSharedFlow()

    private val _closeDialogEvent = MutableSharedFlow<Unit>()
    val closeDialogEvent = _closeDialogEvent.asSharedFlow()

    init {
        fetchLatestData()
    }

    fun deleteTask() {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId, groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    deleteTaskError = null,
                                    deleteTaskLoading = false,
                                )
                            }
                            _successMessage.emit("Successfully deleted task '${_uiState.value.taskDetails!!.task.title}'")
                            _closeDialogEvent.emit(Unit)
                            _onBackEvent.emit(Unit)
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    deleteTaskError = resource.message
                                        ?: "An unknown error occurred",
                                    deleteTaskLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    deleteTaskError = null,
                                    deleteTaskLoading = true
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
            taskRepository.getTaskDetails(taskId, groupId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    getTaskError = null,
                                    getTaskLoading = false,
                                    taskDetails = resource.data
                                )
                            }
                        }

                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    getTaskError = resource.message
                                        ?: "An unknown error occurred",
                                    getTaskLoading = false
                                )
                            }
                        }

                        is Resource.Loading -> {
                            _uiState.update {
                                it.copy(
                                    getTaskError = null,
                                    getTaskLoading = true
                                )
                            }
                        }
                    }
                }
        }
    }
}