package com.libra

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.messaging.FirebaseMessaging
import com.libra.loginTimesNotifications.RescheduleWorker
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class LibraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        appContext = this.applicationContext

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setupNotificationChannel()

        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())

        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.INFO)
        FirebaseMessaging.getInstance().subscribeToTopic("new_user_topic")

        RescheduleWorker.schedule()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun setupNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = getString(R.string.notification_channel_id)
        val channelTitle = getString(R.string.notification_channel_title)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelTitle, importance)
        channel.enableLights(true)
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
