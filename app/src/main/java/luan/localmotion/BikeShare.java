package luan.localmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.entities.Business;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import luan.localmotion.Content.PlacesItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by luann on 2016-07-08.
 */
public class BikeShare {

    public  ArrayList<Marker> googleMapMarkers = new ArrayList<Marker>();
    private BikeShareListener listener = null;
    public Context context;
    ArrayList<BikeShareItem> bikeShareItems;
    ArrayList<Bitmap> icons;
    public BikeShare(Context context){
        this.context=context;
    }
    public void getStations(){
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {


                try{
                    String url = "http://local-motion.ca/server/bixi.php?command=getStations_v5&cityTag=toronto" ;

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    return response.body().string();
                }catch(IOException exception){
                    exception.printStackTrace();
                    return null;
                }

            }

            @Override
            protected void onPostExecute(String msg) {

                Log.i(getClass().getSimpleName(), msg);
                JSONObject data;
                JSONArray stations;
                JSONObject station;
                JSONObject prediction;
                ArrayList<JSONObject> predictionsArr = null;
                JSONObject directions;
                ArrayList<JSONObject> directionsArr = null;
                String icon;
                bikeShareItems =  new ArrayList<BikeShareItem>();

                try {
                    data = new JSONObject(msg);

                    stations = data.getJSONArray("result");

                    for (int i = 0; i < stations.length(); i++) {
                        station = stations.getJSONObject(i);

                        bikeShareItems.add(
                                new BikeShareItem(
                                        station.getString("name"),
                                        station.getDouble("lat"),
                                        station.getDouble("lng"),
                                        station.getInt("bikes"),
                                        station.getInt("docks"),
                                        station.getString("id")
                                )
                        );
                    }
                    createMarkers();
                    if (listener != null)
                        listener.OnGetBikes(bikeShareItems);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.execute();
    }
    void createMarkers(){
        for (BikeShareItem station:bikeShareItems) {



            icons = new ArrayList<Bitmap>();
            Bitmap bikebmp0 = BitmapFactory.decodeResource(context.getResources(), R.drawable.bikeshare0);
            Bitmap bikebmp1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare1);
            Bitmap bikebmp2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare2);
            Bitmap bikebmp3 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare3);
            Bitmap bikebmp4 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare4);
            Bitmap bikebmp5 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare5);

            Bitmap bikebmpresized0 = Bitmap.createScaledBitmap(bikebmp0,(int)(bikebmp0.getWidth()*0.4), (int)(bikebmp0.getHeight()*0.4), true);
            Bitmap bikebmpresized1 = Bitmap.createScaledBitmap(bikebmp1,(int)(bikebmp1.getWidth()*0.4), (int)(bikebmp1.getHeight()*0.4), true);
            Bitmap bikebmpresized2 = Bitmap.createScaledBitmap(bikebmp2,(int)(bikebmp2.getWidth()*0.4), (int)(bikebmp2.getHeight()*0.4), true);
            Bitmap bikebmpresized3 = Bitmap.createScaledBitmap(bikebmp3,(int)(bikebmp3.getWidth()*0.4), (int)(bikebmp3.getHeight()*0.4), true);
            Bitmap bikebmpresized4 = Bitmap.createScaledBitmap(bikebmp4,(int)(bikebmp4.getWidth()*0.4), (int)(bikebmp4.getHeight()*0.4), true);
            Bitmap bikebmpresized5 = Bitmap.createScaledBitmap(bikebmp5,(int)(bikebmp5.getWidth()*0.4), (int)(bikebmp5.getHeight()*0.4), true);

            icons.add(bikebmpresized0);
            icons.add(bikebmpresized1);
            icons.add(bikebmpresized2);
            icons.add(bikebmpresized3);
            icons.add(bikebmpresized4);
            icons.add(bikebmpresized5);


        }
    }
    void drawMarkers(ArrayList<BikeShareItem>  stations, GoogleMap mMap){
        for (Marker marker:googleMapMarkers) {
            marker.remove();
        }
        googleMapMarkers.clear();
        for (BikeShareItem station:stations) {
            final LatLng loc = new LatLng(station.lat, station.lng);
            if((station.bikes+station.docks)==0)
                continue;
            float ratioDbl =  ((float)station.bikes/((float)station.bikes+(float)station.docks))*5;
            int ratio = Math.round(ratioDbl);
            googleMapMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromBitmap(icons.get(ratio)))));


        }
    }
    public void findNearestBikeShare(Location loc, GoogleMap mMap){
        for (BikeShareItem station:bikeShareItems) {
            float [] dist = new float[1];
            Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), station.lat, station.lng, dist);
            station.distance =(double) dist[0];
        }
        Collections.sort(bikeShareItems, new Comparator<BikeShareItem>() {
            @Override
            public int compare(BikeShareItem bixiItem1, BikeShareItem bixiItem2) {

                return (int) Math.round(bixiItem1.distance - bixiItem2.distance);
            }
        });

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        LatLng currentLtLng = new LatLng(loc.getLatitude(),loc.getLongitude());
        builder.include(currentLtLng);
        for (int i = 0; i < bikeShareItems.size(); i++) {
            if(i==2)
                break;
            LatLng placeLoc = new LatLng(bikeShareItems.get(i).lat,bikeShareItems.get(i).lng);
            builder.include(placeLoc);
        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);

        mMap.moveCamera(cameraUpdate);
    }
    // Assign the listener implementing events interface that will receive the events
    public void setBikeShareListener(BikeShareListener listener) {
        this.listener = listener;
    }
    public interface BikeShareListener {

        void OnGetBikes(ArrayList<BikeShareItem> bikeData);
    }
}
class BikeShareItem {
    public String name;
    public Double lat;
    public Double lng;
    public int bikes;
    public int docks;
    public String id;
    public double distance;

    public BikeShareItem(String name, Double lat, Double lng, int bikes, int docks, String id) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.bikes = bikes;
        this.docks = docks;
        this.id = id;

        float [] dist = new float[1];
        //Location.distanceBetween(HomeScreen.lat, HomeScreen.lng, this.lat, this.lng, dist);
        this.distance = 0;
    }

}
