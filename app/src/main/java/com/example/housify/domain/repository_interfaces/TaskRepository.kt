package com.example.housify.domain.repository_interfaces

import com.example.housify.Resource
import com.example.housify.domain.model.Repeat
import com.example.housify.domain.model.TaskDetails
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun createTask(
        groupId: String,
        title: String,
        place: String,
        assigneeIds: List<String>,
        repeat: Repeat,
        startDate: Long,
        isRotational: Boolean
    ): Flow<Resource<Unit>>

    fun getTaskDetails(taskId: String, groupId: String): Flow<Resource<TaskDetails>>

    fun deleteTask(taskId: String, groupId: String): Flow<Resource<Unit>>
}