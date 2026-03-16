package com.example.housify.di

import com.example.housify.data.local.datastore.UserPreferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

const val PORT = 8080

@Singleton // The provider itself can be a singleton
class RetrofitProvider @Inject constructor(
    private val userPreferences: UserPreferencesDataStore,
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    // This function will build a new Retrofit instance with the latest IP
    suspend fun get(): Retrofit {
        val hostIp = userPreferences.getHostIp.first()
        val baseUrl = "http://$hostIp:$PORT/v1/"

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}