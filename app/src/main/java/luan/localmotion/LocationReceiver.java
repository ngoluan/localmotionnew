package luan.localmotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by luann on 2016-07-02.
 */
public class LocationReceiver extends BroadcastReceiver {
    OnReceiveLocation onReceiveLocation;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.TAG, "New Location received:" +intent.toString());
        if(onReceiveLocation!=null)
        {
            Location location = (Location) intent.getExtras().get("location");
            onReceiveLocation.onReceiveLocation(location);
        }
    }
    public void setListener(OnReceiveLocation listener) {
        onReceiveLocation= listener;
    }
}
interface OnReceiveLocation{
    void onReceiveLocation(Location location);
}
