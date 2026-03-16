package com.example.housify.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.housify.Resource
import com.example.housify.data.remote.dto.CompletedTask
import com.example.housify.data.remote.dto.DownloadSignedUrl
import com.example.housify.data.remote.dto.MediaItem
import com.example.housify.data.repository.ReviewRepository
import com.example.housify.data.repository.TaskRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedTasksViewModel @Inject constructor(
    private val taskRepository: TaskRepositoryImpl,
    private val reviewRepository: ReviewRepository
) : ViewModel() {
    private val _completedTasks = MutableStateFlow<List<CompletedTask>>(emptyList())
    val completedTasks: StateFlow<List<CompletedTask>> = _completedTasks.asStateFlow()

    private val _downloadSignedUrl = MutableStateFlow<List<DownloadSignedUrl>?>(null)
    val downloadSignedUrl = _downloadSignedUrl

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getCompletedTaskHistory(
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tasks = taskRepository.getCompletedTaskHistory()
                _completedTasks.value = tasks
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDownloadSignedUrlByMedia(mediaItems: List<MediaItem>) {
        viewModelScope.launch {
            try {
                val downloadSignedUrl = reviewRepository.getDownloadSignedUrl(mediaItems)
                _downloadSignedUrl.value = downloadSignedUrl
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}