package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.entities.Business;

import java.util.ArrayList;

import luan.localmotion.Content.ContactItem;

public class Scheduler extends AppCompatActivity implements OnMapReadyCallback {
    Contacts contacts;
    Places places;
    Location mLocation;

    public GoogleMap mMap;
    public Marker locMarker;
    public ScrollView mScrollView;

    public static final int PICK_CONTACT_REQUEST = 1;
    public static final int PICK_PLACE_REQUEST = 2;
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

        if(type.equals("contacts")){
            fillContact(id);
        }
        if(type.equals("places")){
            fillPlace(id);
        }

        View profilePicView= findViewById(R.id.profilePicView);
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getApplicationContext(), PickContact.class);

                startActivityForResult(contactIntent,PICK_CONTACT_REQUEST);
            }
        });
        View placePicView= findViewById(R.id.placePic);
        placePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent placesIntent = new Intent(getApplicationContext(), PickPlace.class);

                startActivityForResult(placesIntent,PICK_PLACE_REQUEST);
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
    public void fillContact(String phoneNumber){
        ContactItem contact = contacts.getContactItem(this,phoneNumber);

        TextView nameView = (TextView) findViewById(R.id.nameView);
        nameView.setText(contact.name);

        TextView phoneNumberView = (TextView) findViewById(R.id.phoneNumberView);
        phoneNumberView.setText(contact.phoneNumber);

        ImageView img = (ImageView) findViewById(R.id.profilePicView);
        if(contact.profilePic!=null){
            img.setImageBitmap(contact.profilePic);
        }
    }
    public void fillPlace(String id){
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
                placesAddress.setText(business.location().address().get(0));

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                fillContact(data.getStringExtra("phoneNumber"));
            }
        }
        else if(requestCode == PICK_PLACE_REQUEST) {
            if (resultCode == RESULT_OK) {
                fillPlace(data.getStringExtra("placeId"));
            }
        }
    }
}
