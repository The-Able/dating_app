package com.libra.loginTimesNotifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy.REPLACE
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.libra.BuildConfig
import com.libra.LibraApplication
import java.util.concurrent.TimeUnit.DAYS

class RescheduleWorker(
        context: Context,
        workerParameters: WorkerParameters
): Worker(context, workerParameters) {

    override fun doWork(): Result {
        if (BuildConfig.DEBUG) Log.d("Scheduler", "Reschedule worker reschedule all notifications")
        LoginTimeNotificationReceiver.cancelNotification()
        LoginTimeNotificationReceiver.scheduleNextNotification()
        return Result.success()
    }

    companion object {
        private const val TAG = "RESCHEDULE_WORKER"

        fun schedule() {
            WorkManager.getInstance(LibraApplication.appContext)
                    .cancelAllWorkByTag(TAG)

            val request = PeriodicWorkRequest
                    .Builder(RescheduleWorker::class.java, 1, DAYS)
                    .addTag(TAG)
                    .build()
            WorkManager.getInstance(LibraApplication.appContext)
                    .enqueueUniquePeriodicWork(TAG, REPLACE, request)
        }
    }
}