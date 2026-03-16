package com.example.housify.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.housify.data.local.dao.GroupDao
import com.example.housify.data.local.dao.RatingDao
import com.example.housify.data.local.dao.TaskDao
import com.example.housify.data.local.dao.UserDao
import com.example.housify.data.local.dao.UserStatDao
import com.example.housify.data.local.entity.GroupEntity
import com.example.housify.data.local.entity.RatingEntity
import com.example.housify.data.local.entity.TaskEntity
import com.example.housify.data.local.entity.UserEntity
import com.example.housify.data.local.entity.UserStatEntity

@Database(
    entities = [GroupEntity::class, TaskEntity::class, RatingEntity::class, UserEntity::class, UserStatEntity::class],
    version = 5
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDao
    abstract fun taskDao(): TaskDao
    abstract fun ratingDao(): RatingDao
    abstract fun userDao(): UserDao
    abstract fun userStatDao(): UserStatDao
}
