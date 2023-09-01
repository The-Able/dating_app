package com.libra.jobs;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.annotation.WorkerThread;

import com.libra.helpers.LogoutHelper;
import com.libra.support.PrefHelper;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.TaskParams;

public class LogOutJob extends GcmTaskService {

    private static final String TAG = LogOutJob.class.getName();


    @WorkerThread
    @Override
    public int onRunTask(final TaskParams taskParams) {
        LogoutHelper.logoutApp(this);
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    @UiThread
    @Override
    public void onInitializeTasks() {
        super.onInitializeTasks();
        reschedule(this);
    }

    public static void reschedule(@NonNull Context context) {
        if (PrefHelper.isLoginUser(context)) {
            return;
        }
        final GcmNetworkManager gcmNetworkManager = GcmNetworkManager.getInstance(context);
        gcmNetworkManager.cancelTask(TAG, LogOutJob.class);

        ;

        OneoffTask task = new OneoffTask.Builder()
                .setService(LogOutJob.class)
                .setTag(TAG)
                .setExecutionWindow(21600, 43200) //6 to 12 h window
                .setUpdateCurrent(true)
                .setPersisted(true)
                .build();
        gcmNetworkManager.schedule(task);
    }
}
