package com.example.housify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.housify.data.local.entity.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE userId = :userId")
    fun getRatingsByUserId(userId: String): Flow<List<RatingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(ratings: List<RatingEntity>)

    @Query("DELETE FROM ratings WHERE userId = :userId")
    fun deleteAllByUserId(userId: String)
}