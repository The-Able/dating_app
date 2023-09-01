package com.android.libramanage.data

import android.util.Log
import com.android.libramanage.BuildConfig
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author Alex on 24.01.2019.
 */
object ServiceGenerator {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/"
    private var retrofit: Retrofit? = null

    inline fun <reified S> createService(): S {
        return getRetrofit().create(S::class.java)
    }

    fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                    .client(createOkHttp())
                    .build()
        }

        return retrofit!!
    }

    private fun createOkHttp(): OkHttpClient {
        return OkHttpClient.Builder()
                .addInterceptor(createLoggingInterceptor())
                .build()
    }

    private fun createLoggingInterceptor(): LoggingInterceptor {
        val logLevel = if (BuildConfig.DEBUG) Level.BASIC else Level.NONE
        return LoggingInterceptor.Builder()
                .setLevel(logLevel)
                .log(Log.INFO)
                .request("")
                .response("")
                .enableAndroidStudioV3LogsHack(true)
                .build()
    }
}