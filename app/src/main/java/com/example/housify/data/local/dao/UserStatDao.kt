package com.example.housify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.housify.data.local.entity.UserStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatDao {
    @Query("SELECT * FROM userStats WHERE userId = :userId")
    fun getUserStat(userId: String): Flow<UserStatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserStat(userStat: UserStatEntity)
}