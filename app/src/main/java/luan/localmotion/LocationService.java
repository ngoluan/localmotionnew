package luan.localmotion;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

public class LocationService extends Service implements LocationListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    LocationServiceListener mListener=null;
    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mListener != null)
            mListener.onLocationChanged(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mListener != null)
            mListener.OnConnected(bundle);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    public void setCustomObjectListener(LocationServiceListener listener) {
        this.mListener = listener;
    }
}
interface LocationServiceListener {
    public void OnConnected(@Nullable Bundle bundle);
    public void onLocationChanged(Location location);
}