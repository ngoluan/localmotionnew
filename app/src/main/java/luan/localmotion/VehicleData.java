package luan.localmotion;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class VehicleData implements ClusterItem {
    public String vehicleId;
    public String routeTag;
    public String dirTag;
    public float lat;
    public float lng;
    public Integer heading;
    public LatLng mPosition;
    String date;
    public int index;
    public String type=null;
    public Boolean visible;
    public VehicleData(String vehicleId, String routeTag, String dirTag,
                       float lat, float lng, Integer heading, String date, int index, Boolean visible) {
    	this.vehicleId = vehicleId;
    	this.routeTag = routeTag;
        this.dirTag = dirTag;
        this.lat = lat;
        this.lng = lng;
    	this.heading = heading;
    	mPosition = new LatLng(lat, lng);
    	this.date = date;
    	this.index = index;
    	this.type="nextBusVehicleInfo";
        this.visible=visible;
    }
    public MapMarker createMapMarker(){
    	MapMarker currentMarker = new MapMarker(this.index, this.lat, this.lng, this.type);
  		return currentMarker;
    	
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
