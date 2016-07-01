package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.entities.Business;

import java.util.ArrayList;

import luan.localmotion.Content.ContactItem;

public class Scheduler extends AppCompatActivity implements OnMapReadyCallback{
    Contacts contacts;
    Places places;
    Location mLocation;

    public GoogleMap mMap;
    public Marker locMarker;
    public ScrollView mScrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_view);
        Intent intent = getIntent();

        contacts = new Contacts(this);
        places = new Places(this);
        String type = intent.getStringExtra("type");
        String id = intent.getStringExtra("id");
        mLocation = new Location("dummy");
        Log.i(MainActivity.TAG,"Location schedule"+intent.getStringExtra("lat"));
        mLocation.setLatitude(Double.valueOf(intent.getStringExtra("lat")));
        mLocation.setLongitude(Double.valueOf(intent.getStringExtra("lng")));
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        if (mMap == null) {
            CustomMapView mapFragment = (CustomMapView) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new CustomMapView.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        Log.i(MainActivity.TAG, "schedule type" + type);
        if(type.equals("contacts")){
            ContactItem contact = contacts.getContactItem(this,id);
            TextView name = (TextView) findViewById(R.id.nameView);
            name.setText(contact.name);
            Log.i(MainActivity.TAG, "name" + contact.name);
            TextView phoneNumber = (TextView) findViewById(R.id.phoneNumberView);
            phoneNumber.setText(contact.phoneNumber);

            ImageView img = (ImageView) findViewById(R.id.profilePicView);
            if(contact.profilePic!=null){
                img.setImageBitmap(contact.profilePic);
            }
        }
        if(type.equals("places")){
            places.searchBusiness(this, id);
            places.setYelpListener(new Places.YelpListener() {
                @Override
                public void OnGetSearch(ArrayList<Business> businesses, View view) {

                }

                @Override
                public void OnGetBusiness(Activity caller, Business business) {
                    Scheduler actvitity = (Scheduler) caller;
                    TextView placesName = (TextView) findViewById(R.id.placeName);
                    placesName.setText(business.name());

                    TextView placesAddress = (TextView) findViewById(R.id.placeAddress);
                    placesAddress.setText((CharSequence) business.location().address().get(0));

                    ImageView placesPic = (ImageView) findViewById(R.id.placePic);

                    new LoadImage(placesPic).execute(business.imageUrl());
                    LatLng loc = new LatLng(business.location().coordinate().latitude(), business.location().coordinate().longitude());
                    locMarker = mMap.addMarker(new MarkerOptions()
                            .position(loc));
                    LatLng currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(loc).include(currentLocation);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);

// Set the camera to the greatest possible zoom level that includes the
// bounds
                    mMap.moveCamera(cameraUpdate);
                }
            });

        }

        View profilePicView= findViewById(R.id.profilePicView);
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mLocation!=null){
            LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            locMarker = mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,16));


        }
    }
}
