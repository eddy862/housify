package com.example.housify.di

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.housify.data.remote.GroupApiService
import com.example.housify.data.remote.ReviewApiService
import com.example.housify.data.remote.LeaderboardApiService
import com.example.housify.data.remote.NotificationApiService
import com.example.housify.data.remote.TaskApiService
import com.example.housify.data.remote.UserApiService
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Invocation
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideTokenAuthenticator(firebaseAuth: FirebaseAuth): Authenticator =
        TokenAuthenticator(firebaseAuth)

    @Provides
    @Singleton
    fun provideAuthInterceptor(firebaseAuth: FirebaseAuth): AuthInterceptor =
        AuthInterceptor(firebaseAuth)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authenticator: Authenticator, // Inject the Authenticator
        authInterceptor: AuthInterceptor // Inject the Interceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            // The authenticator is called ONLY on 401 responses.
            .authenticator(authenticator)
            // The interceptor is called for EVERY request.
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .setLenient()
            .create()
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    @Provides
//    @Singleton
//    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
//        // Create a Gson instance with our custom adapter
//        val gson = GsonBuilder()
//            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
//            .setLenient()
//            .create()
//
//        return Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .build()
//    }

    @Provides
    @Singleton
    fun provideGroupApi(provider: RetrofitProvider): GroupApiService =
        runBlocking {
            provider.get().create(GroupApiService::class.java)
        }

    @Provides
    @Singleton
    fun provideTaskApi(provider: RetrofitProvider): TaskApiService = runBlocking {
        provider.get().create(TaskApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(provider: RetrofitProvider): UserApiService = runBlocking {
        provider.get().create(UserApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideLeaderboardApiService(provider: RetrofitProvider): LeaderboardApiService = runBlocking {
        provider.get().create(LeaderboardApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReviewApiService(provider: RetrofitProvider): ReviewApiService = runBlocking {
        provider.get().create(ReviewApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApiService(provider: RetrofitProvider): NotificationApiService = runBlocking {
        provider.get().create(NotificationApiService::class.java)
    }
}

class AuthInterceptor(private val firebaseAuth: FirebaseAuth) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Check if the endpoint requires authentication
        val isAuthenticated = originalRequest.tag(Invocation::class.java)
            ?.method()
            ?.isAnnotationPresent(Authenticated::class.java)
            ?: false

        if (!isAuthenticated) {
            return chain.proceed(originalRequest)
        }

        // Get the current user's token. This is a blocking call.
        // It's acceptable here because this runs on a background I/O thread.
        val token = try {
            Tasks.await(firebaseAuth.currentUser?.getIdToken(false)!!).token
        } catch (e: Exception) {
            // Handle cases where token retrieval fails (e.g., user is signed out)
            null
        }

        // If the token is null, you might want to log out the user or handle the error
        if (token == null) {
            // Proceeding with the original request which will likely fail with a 401
            // You could also throw an IOException to prevent the network call
            return chain.proceed(originalRequest)
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}

// to retry a request with refreshed token
class TokenAuthenticator(private val firebaseAuth: FirebaseAuth) : okhttp3.Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // We need to check if the user is still signed in.
        val currentUser =
            firebaseAuth.currentUser ?: return null // User is signed out, can't refresh.

        // Check if we've already tried to refresh and failed, to avoid an infinite loop.
        if (isSameRequest(response.request, response.priorResponse?.request)) {
            return null // Authentication has failed. Give up.
        }

        // Force a token refresh. This is a blocking call.
        val newToken = try {
            Tasks.await(currentUser.getIdToken(true)).token
        } catch (e: Exception) {
            // If refresh fails, we can't do anything.
            // You should probably log the user out here.
            null
        }

        // If we failed to get a new token, give up.
        if (newToken == null) {
            return null
        }

        // Retry the original request with the new token.
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    // Helper to check if we're in a retry loop
    private fun isSameRequest(first: Request?, second: Request?): Boolean {
        return first?.url == second?.url
    }
}