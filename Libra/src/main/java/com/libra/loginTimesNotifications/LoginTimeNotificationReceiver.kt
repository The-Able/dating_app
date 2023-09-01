package com.libra.loginTimesNotifications

import android.app.AlarmManager
import android.app.Notification.BigTextStyle
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.libra.BuildConfig
import com.libra.Const
import com.libra.LibraApplication
import com.libra.R
import com.libra.activitys.SplashActivity
import com.libra.tools.LoginTimesHelper
import com.libra.ui.home.cafeslist.viewmodels.CafeListViewModel

class LoginTimeNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent) {
//        val isLoginTime = intent.getBooleanExtra(Const.LOGINTIME_ALARM, false)
//        if(!isLoginTime) {
//            showNotification(context!!)
//        } else {
//            showNotificationWhenLoginTime(context!!)
//        }
//        scheduleNextNotification()
    }

    private fun showNotification(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channelId = context.getString(R.string.notification_channel_id)
        val activityIntent = SplashActivity.launchIntent(context)
        val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        val message = context.getString(R.string.notification_log_in_time)

        val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(pendingIntent)
                .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun showNotificationWhenLoginTime(context: Context) {
        val nm = NotificationManagerCompat.from(context)
        val channelId = context.getString(R.string.notification_channel_id)
        val activityIntent = SplashActivity.launchIntent(context)
        val pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        val cafeName1 = "Landwer Café לנדוור"
        val cafeName2 = "Cafeteria קפיטריה"
        val messageMale =  context.getString(R.string.notification_someone_logged_in_man, cafeName1)
        val messageFemale = context.getString(R.string.notification_someone_logged_in_woman, cafeName2)

        val notification1 = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(messageMale)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageMale))
                .setContentIntent(pendingIntent)
                .build()

        val notification2 = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(ContextCompat.getColor(context, R.color.color_accent))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(context.getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentText(messageFemale)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageFemale))
                .setContentIntent(pendingIntent)
                .build()

        nm.notify(1001, notification1)
        nm.notify(1002, notification2)
    }

    companion object {
        private const val INTENT_FILTER = "com.dor.libra.LOGIN_TIME_NOTIFICATION"
        private val NOTIFICATION_ID = INTENT_FILTER.hashCode()

        fun cancelNotification() {
            val pendingIntent = getPendingIntent()
            val pendingLoginTimeIntent = getLoginTimePendingIntent()
            getAlarmManager().cancel(pendingIntent)
            getAlarmManager().cancel(pendingLoginTimeIntent)
        }

        fun scheduleNextNotification() {
            val scheduleDate = LoginTimesHelper.findNextLoginHourForNotificationFromNow().time
            val scheduleLoginDate = LoginTimesHelper.findNextLoginHourForFromNow().time
            val pendingIntent = getPendingIntent()
            val pendingLoginTimeIntent = getLoginTimePendingIntent()

            val alarmManager = getAlarmManager()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleDate.time, pendingIntent)
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleLoginDate.time, pendingLoginTimeIntent)

//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduleDate.time, pendingIntent)
//                alarmManager.setExact(AlarmManager.RTC_WAKEUP, scheduleLoginDate.time, pendingLoginTimeIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleDate.time, pendingIntent)
                alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleLoginDate.time, pendingLoginTimeIntent)
            }
            if (BuildConfig.DEBUG) {
                Log.d("Scheduler", "Schedule next login notification for date: $scheduleDate")
            }
        }

        private fun getPendingIntent(): PendingIntent {
            val intent = getIntent(LibraApplication.appContext)
            return PendingIntent.getBroadcast(LibraApplication.appContext,
                        NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        }
        private fun getLoginTimePendingIntent(): PendingIntent {
            val intent = getIntent(LibraApplication.appContext)
            intent.putExtra(Const.LOGINTIME_ALARM, true);
            return PendingIntent.getBroadcast(LibraApplication.appContext,
                        NOTIFICATION_ID, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        }

        private fun getIntent(context: Context): Intent {
            val intent = Intent(context, LoginTimeNotificationReceiver::class.java)
            intent.action = INTENT_FILTER
            return intent
        }

        private fun getAlarmManager(): AlarmManager {
            return LibraApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }
}