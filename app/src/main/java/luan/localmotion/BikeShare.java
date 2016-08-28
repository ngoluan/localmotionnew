package luan.localmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
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
    ArrayList<BikeShareItem> bikeShareItems =  new ArrayList<BikeShareItem>();
    ArrayList<Bitmap> icons;
    // Declare a variable for the cluster manager.
    private ClusterManager<BikeShareClulsterItem> mClusterManager;
    BikeShareClusterRenderer bikeShareClusterRenderer;
    private CameraPosition mPreviousCameraPosition;
    public BikeShare(Context context){
        this.context=context;
        createMarkers();

    }
    public void getStations(){
        bikeShareItems.clear();
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

                Log.d(MainActivity.TAG, "Luan-onPostExecute: "+msg);
                JSONObject data;
                JSONArray stations;
                JSONObject station;
                JSONObject prediction;
                ArrayList<JSONObject> predictionsArr = null;
                JSONObject directions;
                ArrayList<JSONObject> directionsArr = null;
                String icon;


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
        Thread thread = new Thread() {
            @Override
            public void run() {

                icons = new ArrayList<Bitmap>();
                Bitmap bikebmp0 = BitmapFactory.decodeResource(context.getResources(), R.drawable.bikeshare0);
                Bitmap bikebmp1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare1);
                Bitmap bikebmp2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare2);
                Bitmap bikebmp3 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare3);
                Bitmap bikebmp4 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare4);
                Bitmap bikebmp5 = BitmapFactory.decodeResource(context.getResources(),R.drawable.bikeshare5);

                Bitmap bikebmpresized0 = Bitmap.createScaledBitmap(bikebmp0,(int)(bikebmp0.getWidth()*.3), (int)(bikebmp0.getHeight()*.3), true);
                Bitmap bikebmpresized1 = Bitmap.createScaledBitmap(bikebmp1,(int)(bikebmp1.getWidth()*.3), (int)(bikebmp1.getHeight()*.3), true);
                Bitmap bikebmpresized2 = Bitmap.createScaledBitmap(bikebmp2,(int)(bikebmp2.getWidth()*.3), (int)(bikebmp2.getHeight()*.3), true);
                Bitmap bikebmpresized3 = Bitmap.createScaledBitmap(bikebmp3,(int)(bikebmp3.getWidth()*.3), (int)(bikebmp3.getHeight()*.3), true);
                Bitmap bikebmpresized4 = Bitmap.createScaledBitmap(bikebmp4,(int)(bikebmp4.getWidth()*.3), (int)(bikebmp4.getHeight()*.3), true);
                Bitmap bikebmpresized5 = Bitmap.createScaledBitmap(bikebmp5,(int)(bikebmp5.getWidth()*.3), (int)(bikebmp5.getHeight()*.3), true);

                icons.add(bikebmpresized0);
                icons.add(bikebmpresized1);
                icons.add(bikebmpresized2);
                icons.add(bikebmpresized3);
                icons.add(bikebmpresized4);
                icons.add(bikebmpresized5);

            }
        };

        thread.start();
    }
    void drawMarkers(ArrayList<BikeShareItem>  stations, GoogleMap mMap){

        mClusterManager.clearItems();
        for (BikeShareItem station:stations) {
            final LatLng loc = new LatLng(station.lat, station.lng);
            if((station.bikes+station.docks)==0)
                continue;
            float ratioDbl =  ((float)station.bikes/((float)station.bikes+(float)station.docks))*5;
            int ratio = Math.round(ratioDbl);

            mClusterManager.addItem(new BikeShareClulsterItem(station.lat, station.lng, ratio));

            /*googleMapMarkers.add(mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromBitmap(icons.get(ratio)))));*/


        }
        mClusterManager.cluster();
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
    void setUpClusterer(GoogleMap map, Context context) {

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<BikeShareClulsterItem>(context, map);
        bikeShareClusterRenderer=new BikeShareClusterRenderer(context,map,mClusterManager,icons);
        mClusterManager.setRenderer(bikeShareClusterRenderer);

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        //map.setOnCameraChangeListener(mClusterManager);


    }

    void onCameraChange(CameraPosition cameraPosition,GoogleMap map){
        if(bikeShareClusterRenderer!=null)
        ((GoogleMap.OnCameraChangeListener)bikeShareClusterRenderer).onCameraChange(cameraPosition);
        if(this.mPreviousCameraPosition == null || this.mPreviousCameraPosition.zoom != cameraPosition.zoom) {
            this.mPreviousCameraPosition = cameraPosition;
            if(mClusterManager!=null)
                mClusterManager.cluster();
        }

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
        //Location.distanceBetween(HomeScreen.placeLat, HomeScreen.placeLng, this.placeLat, this.placeLng, dist);
        this.distance = 0;
    }

}
class BikeShareClulsterItem implements ClusterItem {
    public String id;
    private final LatLng mPosition;

    public int getmRatio() {
        return mRatio;
    }

    private int mRatio;

    public BikeShareClulsterItem(double lat, double lng, int ratio){
        mPosition = new LatLng(lat, lng);
        mRatio=ratio;
    }
    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
class BikeShareClusterRenderer extends DefaultClusterRenderer<BikeShareClulsterItem> implements GoogleMap.OnCameraChangeListener {

    private final IconGenerator mIconGenerator;
    private final IconGenerator mClusterIconGenerator;
/*    private final ImageView mImageView;
    private final ImageView mClusterImageView;
    private final int mDimension;*/
    private Context mContext;
    private ArrayList<Bitmap> mIcons;

    public BikeShareClusterRenderer(Context context, GoogleMap map, ClusterManager<BikeShareClulsterItem> clusterManager,ArrayList<Bitmap> icons ) {
        super(context, map, clusterManager);
        mContext=context;
        mIconGenerator = new IconGenerator(mContext);
        mClusterIconGenerator = new IconGenerator(mContext);
        mIcons = icons;
        /*View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
        mClusterIconGenerator.setContentView(multiProfile);
        mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

        mImageView = new ImageView(getApplicationContext());
        mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
        mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);*/
    }

    @Override
    protected void onBeforeClusterItemRendered(BikeShareClulsterItem bikeShareClulsterItem, MarkerOptions markerOptions) {
        // Draw a single person.
        // Set the info window to show their name.
/*        mImageView.setImageResource(person.profilePhoto);
        Bitmap icon = mIconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));*/
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mIcons.get(bikeShareClulsterItem.getmRatio())));
    }

    /*@Override
    protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
        // Draw multiple people.
        // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
        List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
        int width = mDimension;
        int height = mDimension;

        for (Person p : cluster.getItems()) {
            // Draw 4 at most.
            if (profilePhotos.size() == 4) break;
            Drawable drawable = getResources().getDrawable(p.profilePhoto);
            drawable.setBounds(0, 0, width, height);
            profilePhotos.add(drawable);
        }
        MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
        multiDrawable.setBounds(0, 0, width, height);

        mClusterImageView.setImageDrawable(multiDrawable);
        Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }*/

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }
}