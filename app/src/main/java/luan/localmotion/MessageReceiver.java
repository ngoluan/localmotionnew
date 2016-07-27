package luan.localmotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by luann on 2016-07-20.
 */
public class MessageReceiver extends BroadcastReceiver {
    OnReceiveMessage onReceiveMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.TAG, "New Message received:" + intent.toString());
        if (onReceiveMessage != null) {
            Chat Message = (Chat) intent.getExtras().get("Message");
            onReceiveMessage.onReceiveMessage(Message);
        }
    }

    public void setListener(OnReceiveMessage listener) {
        onReceiveMessage = listener;
    }
    interface OnReceiveMessage{
        void onReceiveMessage(Chat Message);
    }
}
