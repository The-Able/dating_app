package com.libra.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.support.Logger;
import com.libra.support.PrefHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RushHoursUpdateService extends IntentService {
    String pushToken;

    private final Logger LOG = Logger.getLogger(getClass().getSimpleName(), true);

    public RushHoursUpdateService() {
        super(RushHoursUpdateService.class.getSimpleName());
    }

    public static Intent launchIntent(Context context) {
        return new Intent(context, RushHoursUpdateService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<String> list = PrefHelper.getArrayCafeIdRushHours(this);
        if (list == null || list.isEmpty()) {
            return;
        }

        String appId = PrefHelper.getAppId(this);

//        pushToken = FirebaseInstanceId.getInstance().getToken();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                // Get new Instance ID token
                if (task.getResult() != null) {
                    pushToken = task.getResult();

                }
            }
        });

        for (String cafeId : list) {
            Map<String, Object> map = new HashMap<>();
            map.put(User.PUSH_TOKEN, pushToken);
            FBHelper.getCafeRushHoursByAppId(cafeId, appId).updateChildren(map);
            LOG.i("Update pushToken success");
        }
    }
}
