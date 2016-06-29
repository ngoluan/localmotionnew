package luan.localmotion;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MapMarker implements ClusterItem {
    public int index;
    public float lat;
    public float lng;
    public LatLng mPosition;
    public String type;
    public int heading;

    public MapMarker(int index, float lat, float lng, String type) {
    	this.lat = lat;
        this.lng = lng;
    	mPosition = new LatLng(lat, lng);
    	this.index = index;
    	this.type=type;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
