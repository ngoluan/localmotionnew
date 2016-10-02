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
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import luan.localmotion.Content.ContactItem;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    CalendarEvent calendarEvent =null;
    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> data=remoteMessage.getData();
        Log.d(MainActivity.TAG, "Luan-onMessageReceived: "+data.toString());
        if(data.get("type").equals(ChatFragment.TYPE_EVENT)){
            String eventId = data.get(CalendarEvent.UNIQUE_ID_TAG);
            List<CalendarEvent> calendarEvents =  CalendarEvent.find(CalendarEvent.class, "event_unique_id=?", eventId);

            if(calendarEvents.size()==0){
                calendarEvent = new CalendarEvent();

            }
            else{
                calendarEvent = calendarEvents.get(0);
            }
            calendarEvent.addPhone(data.get("contactsPhone"));
            calendarEvent.addPhone(data.get("sendersPhone"));
            calendarEvent.beginTime = Long.parseLong(data.get("beginTime"));
            calendarEvent.endTime = Long.parseLong(data.get("endTime"));
            calendarEvent.title = data.get("title");
            calendarEvent.yelpPlaceId = data.get("yelpPlaceId");
            calendarEvent.placeName = data.get("placeName");
            calendarEvent.placeDescription = data.get("placeDescription");
            calendarEvent.placeCategory = data.get("placeCategory");
            calendarEvent.placeAddress = data.get("placeAddress");
            calendarEvent.googlePlaceId = data.get("googlePlaceId");
            calendarEvent.placeImgUrl = data.get("placeImgUrl");
            calendarEvent.eventUniqueId = data.get(CalendarEvent.UNIQUE_ID_TAG);
            if(!data.get("placeLat").equals(""))
            calendarEvent.placeLat = Double.parseDouble(data.get("placeLat"));
            if(!data.get("placeLng").equals(""))
            calendarEvent.placeLng = Double.parseDouble(data.get("placeLng"));
            calendarEvent.save();

            readyBroadcast(data,data.get("type"), calendarEvent.eventUniqueId);
        }
        else if(data.get("type").equals(ChatFragment.TYPE_MESSAGE)){
            Chat chat = new Chat(
                    data.get("senderPhone"),
                    Calendar.getInstance().getTimeInMillis(),
                    data.get("message"),
                    data.get("eventUniqueId")
            );
            chat.save();
            readyBroadcast(data,data.get("type"), String.valueOf(chat.getId()));
        }




    }
    private void readyBroadcast(Map<String, String> data, String type, String id){
        if (!Utils.isAppIsInBackground(getApplicationContext())) {

            Intent pushNotification = new Intent(data.get("type"));
            pushNotification.setAction(data.get("type"));
            pushNotification.putExtra("type", type);
            pushNotification.putExtra("id", id);/*
            pushNotification.setClass(this, MessageReceiver.class);*/
            sendBroadcast(pushNotification);
        } else {

            sendNotification(data);
        }
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     *  FCM message body received.
     */
    private void sendNotification(Map<String,String> data) {


        if(data.get("type").equals("calendarEvent")){
            sendEventNotification(data);
        }
        else if(data.get("type").equals("message")){
            sendMessageNotification(data);
        }


    }
    public void sendMessageNotification(Map<String,String> data){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_EVENT)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if(!data.get("title").equals(""))
            notificationBuilder.setContentTitle(data.get("title"));
        if(!data.get("message").equals(""))
            notificationBuilder.setContentTitle(data.get("message"));


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    public void sendEventNotification(Map<String,String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        ContactItem contactItem = Contacts.getContactItem(getBaseContext(), data.get("sendersPhone"));
        String contactName = contactItem.name;
        String time = Utils.formatTime(calendarEvent.beginTime);
        String title = contactName+" wants to hangout";
        String message = "At: " + calendarEvent.placeName+ " starting " + time;
        NotificationCompat.Builder notificationBuilder = null;

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo_sm)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_EVENT)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


        // BigPictureStyle
        NotificationCompat.BigPictureStyle s = new NotificationCompat.BigPictureStyle();

        String pictureUrl = "https://maps.googleapis.com/maps/api/staticmap?center="+calendarEvent.placeLat+","+calendarEvent.placeLng+"&zoom=13&size=400x400&&markers=color:blue%7C"+calendarEvent.placeLat+","+calendarEvent.placeLng+"&key=AIzaSyD3rvDAJ0HqJZQsDJIRsDbmDQ-r2D2Qvuw";
        Log.d(MainActivity.TAG, "Luan-sendEventNotification: "+pictureUrl);
        try {
            s.setSummaryText(message);
            s.bigLargeIcon(Picasso.with(getBaseContext()).load(contactItem.profilePicURI).get());
            s.bigPicture(Picasso.with(getBaseContext()).load(pictureUrl).get());
        } catch (IOException e) {
            e.printStackTrace();
        }
        notificationBuilder.setStyle(s);


        //Yes intent
        Intent yesReceive = new Intent();
        yesReceive.setAction(MainActivity.EVENT_ACCEPT);
        Bundle yesBundle = new Bundle();
        yesBundle.putString("eventUniqueId",calendarEvent.getId().toString());
        yesReceive.putExtras(yesBundle);


        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 12345, yesReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.doneicon, "Yes", pendingIntentYes);

        Intent changeReceive = new Intent(getApplicationContext(), ScheduleActvity.class);
        changeReceive.setAction(MainActivity.EVENT_CHANGE);
        changeReceive.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle changeBundle = new Bundle();
        changeBundle.putString(CalendarEvent.UNIQUE_ID_TAG,calendarEvent.eventUniqueId);
        changeReceive.putExtras(changeBundle);
        PendingIntent pendingIntentChange = PendingIntent.getActivity(getBaseContext(), 0,
                changeReceive, 0);
        notificationBuilder.addAction(R.drawable.editicon, "Edit", pendingIntentChange);

        Intent rejectReceive = new Intent();
        rejectReceive.setAction(MainActivity.EVENT_REJECT);
        Bundle rejectBundle = new Bundle();
        rejectBundle.putString(CalendarEvent.UNIQUE_ID_TAG,calendarEvent.eventUniqueId);
        rejectReceive.putExtras(rejectBundle);
        PendingIntent pendingIntentreject= PendingIntent.getBroadcast(this, 12345, rejectReceive, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.addAction(R.drawable.clearicon, "Reject", pendingIntentreject);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}