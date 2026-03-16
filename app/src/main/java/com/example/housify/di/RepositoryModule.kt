package com.example.housify.di

import com.example.housify.data.repository.GroupRepositoryImpl
import com.example.housify.data.repository.LeaderboardRepositoryImpl
import com.example.housify.data.repository.TaskRepositoryImpl
import com.example.housify.domain.repository_interfaces.GroupRepository
import com.example.housify.domain.repository_interfaces.LeaderboardRepository
import com.example.housify.domain.repository_interfaces.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindLeaderboardRepository(
        leaderboardRepositoryImpl: LeaderboardRepositoryImpl
    ): LeaderboardRepository
}
    