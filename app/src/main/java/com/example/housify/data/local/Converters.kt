package com.example.housify.data.local

import androidx.room.TypeConverter
import com.example.housify.domain.model.User // <-- IMPORTANT: Import your actual data class
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    // Assuming you are converting a list of 'User' objects.
    // CHANGE 'User' to your actual class name if it's different.
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val userListType = Types.newParameterizedType(List::class.java, User::class.java)
    private val jsonAdapterUserList = moshi.adapter<List<User>>(userListType)
    private val jsonAdapterUser = moshi.adapter(User::class.java)

    @TypeConverter
    fun fromUserList(users: List<User>?): String? {
        if (users == null) {
            return null
        }
        return jsonAdapterUserList.toJson(users)
    }

    @TypeConverter
    fun toUserList(json: String?): List<User>? {
        if (json == null) {
            return null
        }
        return jsonAdapterUserList.fromJson(json)
    }

    @TypeConverter
    fun fromUser(user: User?): String? {
        // Convert a User object to a JSON String
        if (user == null) {
            return null
        }
        return jsonAdapterUser.toJson(user)
    }

    @TypeConverter
    fun toUser(json: String?): User? {
        // Convert a JSON String back to a User object
        if (json == null) {
            return null
        }
        return jsonAdapterUser.fromJson(json)
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) {
            return null
        }

        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String?): List<String>? {
        if (json == null) {
            return null
        }

        val listType = Types.newParameterizedType(List::class.java, String::class.java)
        val adapter = moshi.adapter<List<String>>(listType)
        return adapter.fromJson(json)
    }
}
