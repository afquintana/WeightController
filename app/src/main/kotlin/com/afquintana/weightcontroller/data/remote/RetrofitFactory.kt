package com.afquintana.weightcontroller.data.remote

import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Singleton
class RetrofitFactory @Inject constructor() {

    private val networkJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            LoggingInterceptor.Builder()
                .setLevel(Level.BASIC)
                .log(android.util.Log.VERBOSE)
                .request("Retrofit")
                .response("Retrofit")
                .build()
        )
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .client(okHttpClient)
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()
}
