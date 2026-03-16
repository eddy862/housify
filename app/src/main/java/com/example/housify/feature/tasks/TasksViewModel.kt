package com.example.housify.feature.tasks

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.data.remote.dto.FutureTask
import com.example.housify.data.remote.dto.TodayTask
import com.example.housify.data.remote.dto.UncompletedRating
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.data.repository.TaskRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepositoryImpl,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    // Stores the currently selected date as String yyyy-MM-dd
    @RequiresApi(Build.VERSION_CODES.O)
    private val _selectedDate = MutableStateFlow(LocalDate.now().toString())

    @RequiresApi(Build.VERSION_CODES.O)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _todayTasks = MutableStateFlow<List<TodayTask>>(emptyList())
    val todayTasks = _todayTasks.asStateFlow()

    private val _futureTasks = MutableStateFlow<List<FutureTask>>(emptyList())
    val futureTasks = _futureTasks.asStateFlow()

    private val _ratings = MutableStateFlow<List<UncompletedRating>>(emptyList())
    val ratings = _ratings.asStateFlow()

    private val _isTasksLoading = MutableStateFlow(false)
    val isTasksLoading: StateFlow<Boolean> = _isTasksLoading.asStateFlow()

    private val _isRatingsLoading = MutableStateFlow(false)
    val isRatingsLoading: StateFlow<Boolean> = _isRatingsLoading.asStateFlow()

    // Update selected date
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun getTodayTasks(
    ) {
        viewModelScope.launch {
            _futureTasks.value = emptyList()
            _isTasksLoading.value = true

            taskRepository.getTodayTasks().collect {
                when (it) {
                    is Resource.Error -> {
                        _todayTasks.value = it.data ?: emptyList()
                        _isTasksLoading.value = false
                    }

                    is Resource.Loading -> {
                        // already true, but harmless to set again
                        _isTasksLoading.value = true
                    }

                    is Resource.Success -> {
                        _todayTasks.value = it.data ?: emptyList()
                        _isTasksLoading.value = false
                    }
                }
            }

        }
    }

    fun getFutureTasks(
        dateString: String
    ) {
        viewModelScope.launch {
            _isTasksLoading.value = true
            _todayTasks.value = emptyList()

            try {
                val futureTasks = taskRepository.getFutureTasks(dateString)
                _futureTasks.value = futureTasks

            } catch (e: Exception) {
                e.printStackTrace()
                _futureTasks.value = emptyList()
            } finally {
                _isTasksLoading.value = false
            }
        }
    }

    fun getUncompletedTask(
    ) {
        viewModelScope.launch {
            reviewRepository.getUncompletedTask().collect {
                when (it) {
                    is Resource.Loading -> {
                        _isRatingsLoading.value = true
                        _ratings.value = emptyList()
                    }

                    is Resource.Success -> {
                        _isRatingsLoading.value = false
                        _ratings.value = it.data ?: emptyList()
                    }

                    is Resource.Error -> {
                        _isRatingsLoading.value = false
                        _ratings.value = it.data ?: emptyList()
                    }
                }
            }
        }
    }
}