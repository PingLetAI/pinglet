package com.linger.app.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    @Volatile
    private var instance: AppApiService? = null
    @Volatile
    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        authToken = token?.takeIf { it.isNotBlank() }
    }

    fun build(baseUrl: String): AppApiService {
        return instance ?: synchronized(this) {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .apply {
                            authToken?.takeIf { it.isNotBlank() }?.let { header("Authorization", "Bearer $it") }
                        }
                        .build()

                    chain.proceed(request)
                }
                .addInterceptor(logging)
                .build()

            val created = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AppApiService::class.java)

            instance = created
            created
        }
    }
}
