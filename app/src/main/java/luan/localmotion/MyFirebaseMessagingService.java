/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package luan.localmotion;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        sendNotification(remoteMessage.getData());
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     *  FCM message body received.
     */
    private void sendNotification(Map<String,String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        Bitmap image=null;
        String urldisplay = "https://maps.googleapis.com/maps/api/staticmap?center=2+bloor+west+Toronto&markers=2+bloor+west+Toronto&zoom=13.5&size=100x100&key=AIzaSyDanfrkNLdf5vDKb861Z3Et-z2BiLzZPc0";
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            image = null;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ttcinner)
                .setContentTitle(data.get("title"))
                .setContentText(data.get("message"))
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_EVENT)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if(image!=null){
/*            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .setBigContentTitle(data.get("message"))
                    .setSummaryText(data.get("message")));*/
            notificationBuilder.setLargeIcon(image);
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(data.get("message")));
        }
        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction("EVENT_INTENT");
        Bundle yesBundle = new Bundle();
        yesBundle.putInt("userAnswer", 1);//This is the value I want to pass
        yesReceive.putExtras(yesBundle);


        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.doneicon, "Yes", pendingIntentYes);

        Intent changeReceive = new Intent(getApplicationContext(), SchedulerActivity.class);
        changeReceive.setAction("EVENT_EDIT");
        changeReceive.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle changeBundle = new Bundle();
        changeBundle.putString("place", data.get("place"));
        changeBundle.putString("placeId", data.get("placeId"));
        changeBundle.putString("message", data.get("message"));
        changeBundle.putString("address", data.get("address"));
        changeBundle.putString("contactName", data.get("senderName"));
        changeBundle.putString("contactPhone", data.get("senderPhone"));
        changeBundle.putString("dateTime", data.get("dateTime"));
        changeReceive.putExtras(changeBundle);
        PendingIntent pendingIntentChange = PendingIntent.getActivity(getBaseContext(), 0,
                changeReceive, 0);
        notificationBuilder.addAction(R.drawable.editicon, "Edit", pendingIntentChange);

        Intent rejectReceive = new Intent();
        rejectReceive.setAction("EVENT_INTENT");
        Bundle rejectBundle = new Bundle();
        rejectBundle.putInt("userAnswer", 1);//This is the value I want to pass
        rejectReceive.putExtras(rejectBundle);
        PendingIntent pendingIntentreject= PendingIntent.getBroadcast(this, 12345, rejectReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.clearicon, "Reject", pendingIntentreject);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}