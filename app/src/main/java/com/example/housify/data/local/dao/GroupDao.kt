package com.example.housify.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.housify.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM `groups` WHERE userId = :userId")
    fun getGroupsByUserId(userId: String): Flow<List<GroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(groups: List<GroupEntity>)

    @Query("DELETE FROM `groups` WHERE userId = :userId")
    fun deleteAllByUserId(userId: String)
}
