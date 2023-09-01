package com.libra.helpers;


import android.content.Context;

import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.support.CloudHelper;
import com.libra.support.NotificationMsgHelper;
import com.libra.support.PrefHelper;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;



public class LogoutHelper {

    public static void logoutApp(Context context) {
        final User user = PrefHelper.getUser(context);
        PrefHelper.logoutUser(context);
        NotificationMsgHelper.cancelAll(context);
        PrefHelper.setNotifyNewUser(context, false);

        if (user != null) {
            //String destroyResult = "";
            try {
                CloudHelper.destroyImage(context, user.getImageUrl());
                /*if (!TextUtils.equals(destroyResult, CloudHelper.RESULT_OK)) {
                    LOG.e(new Exception("LOGOUT, not removed image from cloud, destroy result - " + destroyResult));
                }*/
            } catch (IOException e) {
                e.printStackTrace();
               /* LOG.e(e);
                LOG.e(new Exception("LOGOUT, not removed image from cloud, destroy result - " + destroyResult));
            */}
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        if (user != null) {
            FBHelper.getUserById(user.getCafeId(), user.getId()).removeValue();
            FBHelper.getChats().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.getKey().contains(user.getId())) {
                            FBHelper.getChats().child(data.getKey()).removeValue();
                        }
                    }
                   // LOG.i("data logout is success");
                    countDownLatch.countDown();
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                   // LOG.e(new Exception(firebaseError.getMessage()));
                    countDownLatch.countDown();
                }
            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
