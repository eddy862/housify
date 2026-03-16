package com.example.housify.di

import android.content.Context
import androidx.room.Room
import com.example.housify.data.local.AppDatabase
import com.example.housify.data.local.dao.GroupDao
import com.example.housify.data.local.dao.RatingDao
import com.example.housify.data.local.dao.TaskDao
import com.example.housify.data.local.dao.UserDao
import com.example.housify.data.local.dao.UserStatDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // 1. Tells Hilt how to create an instance of the Room Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: AppDatabase): GroupDao {
        return database.groupDao()
    }

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideRatingDao(database: AppDatabase): RatingDao {
        return database.ratingDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideUserStatDao(database: AppDatabase): UserStatDao {
        return database.userStatDao()
    }
}
