package com.libra.jobs;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.libra.R;
import com.libra.activitys.SplashActivity;

import java.util.Calendar;

public class JobAt_7_40 extends Worker {
    private static final String TAG = "TAG_JOB_7_40";
    private static final int NOTIFICATION_ID = 519;

    public JobAt_7_40(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // show notification only if the current day is Monday or Thursday

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day == Calendar.MONDAY || day == Calendar.THURSDAY) {
            NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());
            String channelId = getApplicationContext().getString(R.string.notification_channel_id);
            Intent activityIntent = SplashActivity.launchIntent(getApplicationContext());
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            String message = getApplicationContext().getString(R.string.job_7_40_message);


            Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setColor(ContextCompat.getColor(getApplicationContext(), R.color.color_accent))
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentTitle(getApplicationContext().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentIntent(pendingIntent)
                    .build();

            nm.notify(NOTIFICATION_ID, notification);
        }

        // reschedule the Job for next day
        set7_40Job(getApplicationContext());

        return Result.success();
    }

    public static void set7_40Job(Context context) {
        // cancel the job, this is not required
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG);

//        Calendar currentDate = Calendar.getInstance();
//
//        // set execution around 19:40:00
//        Calendar dueDate = Calendar.getInstance();
//        dueDate.set(Calendar.HOUR_OF_DAY, 19);
//        dueDate.set(Calendar.MINUTE, 40);
//        dueDate.set(Calendar.SECOND, 0);
//        dueDate.set(Calendar.MILLISECOND, 0);
//
//        if (dueDate.before(currentDate)) {
//            // due date is passed already
//            // run the same job next day
//            dueDate.add(Calendar.DATE, 1);
//        }
//
//        long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();
//        OneTimeWorkRequest request =
//                new OneTimeWorkRequest.Builder(JobAt_7_40.class)
//                        .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
//                        .addTag(TAG)
//                        .build();
//        WorkManager.getInstance(context)
//                .enqueueUniqueWork(TAG, ExistingWorkPolicy.REPLACE, request);
    }
}
