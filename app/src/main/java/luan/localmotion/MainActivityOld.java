package luan.localmotion;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.Category;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivityOld extends AppCompatActivity implements OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    static String TAG = "luan.localmotion";
    private GoogleMap mMap;
    private GridLayout grid;
    private GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private NextBus nextBus=null;
    private Yelp yelp;
    public int squareSize;
    public String[][] dashGrid= new String[5][3];
    int currentRow=0;
    int currentCol=0;


    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        setContentView(R.layout.activity_main);

        nextBus= new NextBus(this);
        yelp= new Yelp(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        Point dSize = getDisplaySize();
        squareSize = dSize.x / 3;



        grid = (GridLayout) findViewById(R.id.grid);
        View mapView = findViewById(R.id.map);
        ViewGroup.LayoutParams mapLayout = mapView.getLayoutParams();
        mapLayout.height = squareSize*2;
        mapLayout.width = squareSize*2;
        mapView.setLayoutParams(mapLayout);
        dashGrid[1][0]="map";
        dashGrid[1][1]="map";

        View view1 = findViewById(R.id.square1);
        ViewGroup.LayoutParams lpGl1 = (ViewGroup.LayoutParams) view1.getLayoutParams();
        lpGl1.height = squareSize;
        lpGl1.width = squareSize*2;

        view1.setLayoutParams(lpGl1);
        view1.setId(R.id.transitView);
        dashGrid[0][0]="transit";
        dashGrid[0][1]="transit";


/*        View view2 = getLayoutInflater().inflate(R.layout.square, null);
        view2.setBackgroundColor(Color.parseColor("#3366ff"));
        GridLayout.LayoutParams lpGl2 = new GridLayout.LayoutParams();
        lpGl2.height = squareSize;
        lpGl2.width = squareSize*2;
        lpGl2.columnSpec = GridLayout.spec(1,0);
        lpGl2.rowSpec = GridLayout.spec(0);

        view2.setLayoutParams(lpGl2);
        grid.addView(view2);*/

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        setBottomBar();
        //getContacts();
        getMessages();


/*
        View view3 = getLayoutInflater().inflate(R.layout.squareSize,null);
        lpGl.columnSpec = GridLayout.spec(2, 1);
        lpGl.rowSpec = GridLayout.spec(0);
        view3.setLayoutParams(lpGl);
        grid.addView(view3);*/

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void setBottomBar(){
        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

// Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Friends", R.drawable.friendsicon,  Color.parseColor("#F63D2B"));
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Places", R.drawable.placesicon, Color.parseColor("#3366ff"));
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Map", R.drawable.mapicon,Color.parseColor("#33ffcc"));

// Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

// Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#FEFEFE"));

// Disable the translation inside the CoordinatorLayout
        bottomNavigation.setBehaviorTranslationEnabled(false);

// Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));

// Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(true);

// Force the titles to be displayed (against Material Design guidelines!)
        bottomNavigation.setForceTitlesDisplay(true);

// Use colored navigation with circle reveal effect
        bottomNavigation.setColored(true);

// Set current item programmatically
        bottomNavigation.setCurrentItem(1);

// Customize notification (title, background, typeface)
        bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

// Add or remove notification for each item
        bottomNavigation.setNotification("4", 1);
        bottomNavigation.setNotification("", 1);

// Set listener
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected) {
                // Do something cool here...
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(43.6532, -79.3832);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLngBounds curScreen = mMap.getProjection()
                        .getVisibleRegion().latLngBounds;
                ArrayList<VehicleData> currentVehicles= new ArrayList<VehicleData>();
                Log.i("luan.localmotion", "zoom"+ cameraPosition.zoom);
                for (VehicleData vehicle:nextBus.nextBusData) {

                    if(vehicle.lat<curScreen.northeast.latitude && vehicle.lat>curScreen.southwest.latitude &&
                            vehicle.lng<curScreen.northeast.longitude&& vehicle.lng>curScreen.southwest.longitude){
                        currentVehicles.add(vehicle);
                    }
                }

                nextBus.drawMarkers(currentVehicles, mMap);
            }
        });
    }

    public Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        String displayName = display.getName();  // minSdkVersion=17+
        Log.i(TAG, "displayName  = " + displayName);

// display size in pixels
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i(TAG, "width        = " + width);
        Log.i(TAG, "height       = " + height);
        return new Point(width, height);
    }

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more phoneNumber.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            LatLng loc = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));

            getTransit(mLastLocation);
            drawTransitMap();
            getYelp(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    private void getTransit(Location loc){
        nextBus.setNextBusListener(new NextBus.NextBusListener(){

            @Override
            public void OnGetPredictions(ArrayList<ArrayList<NextBusDashItem>> routesArr) {
                RelativeLayout transitView = (RelativeLayout) findViewById(R.id.transitView);
                LinearLayout innerLayoutRow1 = new LinearLayout(getApplicationContext());
                innerLayoutRow1.setOrientation(LinearLayout.HORIZONTAL);
                transitView.addView(innerLayoutRow1);

                LinearLayout innerLayoutRow2 = new LinearLayout(getApplicationContext());
                innerLayoutRow2.setOrientation(LinearLayout.HORIZONTAL);
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                innerLayoutRow2.setLayoutParams(param);
                transitView.addView(innerLayoutRow2);


                int width = transitView.getWidth()/4;
                int x=0;
                for (int i = 0; i < routesArr.size(); i++) {

                        for (int j = 0; j < routesArr.get(i).size(); j++) {
                            routesArr.get(i).get(j);
                            Log.i(TAG, "Predictions" + routesArr.size());

                            //getting the direction heading
                            int c = routesArr.get(i).get(j).dirTitle.indexOf("-");
                            int d=routesArr.get(i).get(0).routeTitle.length();
                            String dirTitle = routesArr.get(i).get(j).dirTitle.substring(0, c) + " to " + routesArr.get(i).get(j).dirTitle.substring(d + 8);
                            String dirShortTitle= routesArr.get(i).get(j).dirTitle.substring(0, c);


                            //getting only the route name
                            c = routesArr.get(i).get(0).routeTitle.indexOf("-")+1;
                            d = routesArr.get(i).get(0).routeTitle.indexOf(" ",c);
                            if(d ==-1){d=routesArr.get(i).get(0).routeTitle.length();}
                            String routeTitle =routesArr.get(i).get(0).routeTitle.substring(c, d);

                            View icon = getLayoutInflater().inflate(R.layout.dash_transit,null);
                            LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(width,width);
                            icon.setLayoutParams(param2);
                            TextView route = (TextView) icon.findViewById(R.id.route);
                            route.setText(routeTitle + "-"+dirShortTitle);
                            TextView eta = (TextView) icon.findViewById(R.id.eta);
                            eta.setText(String.valueOf(routesArr.get(i).get(j).eta) + " mins");

                            if(x<=3){
                                innerLayoutRow1.addView(icon);
                            }
                            else if(x>=3){
                                innerLayoutRow2.addView(icon);
                            }
                            else if(x>7){
                                return;
                            }
                                x++;
                        }

                }

            }

            @Override
            public void OnGetVehicles(ArrayList<VehicleData> vehicleData) {
                LatLngBounds curScreen = mMap.getProjection()
                        .getVisibleRegion().latLngBounds;
                ArrayList<VehicleData>  currentVehicles= new ArrayList<VehicleData>();
                Log.i("luan.localmotion", "vLat" + String.valueOf(vehicleData.size()));
                for (VehicleData vehicle:vehicleData) {

                    if(vehicle.lat<curScreen.northeast.latitude && vehicle.lat>curScreen.southwest.latitude &&
                            vehicle.lng<curScreen.northeast.longitude&& vehicle.lng>curScreen.southwest.longitude){
                        currentVehicles.add(vehicle);
                    }
                }

                nextBus.drawMarkers(currentVehicles, mMap);
            }
        });
        nextBus.getPredictionLocation(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()));
    }
    private void getYelp(Location loc){

        Map<String, String> params = new HashMap<>();
        params.put("term", "food");
        params.put("category", "restaurant");

        View view2 = getLayoutInflater().inflate(R.layout.dash_places, null);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.square2);
        ViewGroup.LayoutParams lpGl2 = (ViewGroup.LayoutParams) layout.getLayoutParams();
        lpGl2.height = squareSize;
        lpGl2.width = squareSize;
        dashGrid[0][2]="yelpRestaurant";
        layout.setLayoutParams(lpGl2);

        layout.addView(view2);

        yelp.searchNearby(loc.getLatitude(),loc.getLongitude(), params,view2);
        yelp.setYelpListener(new Yelp.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {




                String businessName = businesses.get(0).name();  // "JapaCurry Truck"
                ArrayList<Category> businessCategory = businesses.get(0).categories();  // 4.0

                TextView name = (TextView) view.findViewById(R.id.type);
                name.setText(businessName);
                TextView category = (TextView) view.findViewById(R.id.category);
                category.setText(String.valueOf(businessCategory.get(0).name()) );

                Log.i(MainActivityOld.TAG, "Total yelp results (painting):" + String.valueOf(businesses.size()));

                String businessImg= businesses.get(0).imageUrl();
                ImageView img = (ImageView) view.findViewById(R.id.imageView);
                new LoadImage(img).execute(businessImg);
            }
        });

        Map<String, String> params2 = new HashMap<>();
        params2.put("term", "bars");
        params2.put("category", "bars");
        View view3 = getLayoutInflater().inflate(R.layout.dash_places, null);
        RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.square3);
        ViewGroup.LayoutParams lpGl3 = (ViewGroup.LayoutParams) layout2.getLayoutParams();
        lpGl3.height = squareSize;
        lpGl3.width = squareSize;
        dashGrid[1][2]="yelpBar";
        layout2.setLayoutParams(lpGl3);

        layout2.addView(view3);
        yelp.searchNearby(loc.getLatitude(),loc.getLongitude(), params2,view3);
    }
    private void drawTransitMap(){
        nextBus.getVehicleLocations();
    }
    /*private int[] getGridLocation(){


    }*/

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        Bitmap bitmap;
        ImageView img;
        public LoadImage(ImageView img) {
            super();
            this.img = img;
            // do stuff
        }
        protected Bitmap doInBackground(String... args) {
            try {
                bitmap = BitmapFactory.decodeStream((InputStream)new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap image) {

            if(image != null){
                img.setImageBitmap(image);

            }else{

                Toast.makeText(MainActivityOld.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();

            }
        }
    }
    /*public void getContacts(){
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
*//*                        Toast.makeText(this, "Name: " + name
                                + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();*//*
                        *//*Log.i(TAG, "Contact:" + name + " - " + phoneNo);*//*
                    }
                    pCur.close();
                }
            }
        }
    }*/
    public void getMessages(){
        ArrayList<String> conversation = new ArrayList<>();

        Uri    uri    = Uri.parse( "name://sms/inbox" );
        String[] projection= { "DISTINCT address"};
        Cursor cursor = getContentResolver().query( uri, projection, null ,null, "date desc LIMIT 4" );

        if( cursor.getCount() > 0 ) {
            String count = Integer.toString( cursor.getCount() );
            int j=0;
            int row=2;
            int col=1;
            while( cursor.moveToNext() ) {
                String result = "";

                for( int i = 0; i < cursor.getColumnCount(); i++ ) {
                    result = result + "\nindex " + i + "\n column is "
                            + cursor.getColumnName( i ) + "\nvalue is " + cursor.getString( i );
                }

                result = result + "\n new conversation";
                int SENDER_ADDRESS = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS);

                String contactName = getContactName(getApplicationContext(), cursor.getString(SENDER_ADDRESS));
                int contactId= getContactIDFromNumber(String.valueOf(SENDER_ADDRESS), this);

                //InputStream photoIS = openPhoto(id);


                conversation.add( result );

                View view4 = getLayoutInflater().inflate(R.layout.dash_people, null);
                int squareNum = j +4;
                int resID = getResources().getIdentifier(String.valueOf("square"+squareNum), "id", "luan.localmotion");
                Log.i(TAG, "Res:" + resID);
                RelativeLayout layout = (RelativeLayout) findViewById(resID);
                ViewGroup.LayoutParams lpGl4 = (ViewGroup.LayoutParams) layout.getLayoutParams();
                lpGl4.height = squareSize;
                lpGl4.width = squareSize;

/*                if(j==1){
                    row=3;
                    col=0;
                }
                else if(j>1 &&j<4){
                    col++;
                }
                else if (j>=4){
                    break;
                }

                lpGl4.columnSpec = GridLayout.spec(col);
                lpGl4.rowSpec = GridLayout.spec(row);*/
                dashGrid[row][col]="person";
                view4.setLayoutParams(lpGl4);

                InputStream photoIS=retrieveContactPhoto(this,cursor.getString(SENDER_ADDRESS));
                if(photoIS!=null){
                    Bitmap photo = BitmapFactory.decodeStream(photoIS);
                    ImageView img = (ImageView) view4.findViewById(R.id.imageView);
                    img.setImageBitmap(photo);
                }

                TextView name = (TextView) view4.findViewById(R.id.type);
                name.setText(contactName);

                layout.addView(view4);

                Log.i(TAG, "Contact:" +contactName+" Row: "+ row + " Col:" + col + " Children:"+grid.getChildCount());
                j++;
            }
        }

        cursor.close();
    }
    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        if (contactName != null) {
            return contactName;
        } else {
            return phoneNumber;
        }
    }

    public  int getContactIDFromNumber(String contactNumber,Context context)
    {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,contactNumber),new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while(contactLookupCursor.moveToNext()){
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }

/*    public InputStream openPhoto(long id) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }*/
public InputStream retrieveContactPhoto(Context context, String number) {
    InputStream inputStream=null;
    ContentResolver contentResolver = context.getContentResolver();
    String contactId = null;



    //Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
    Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,number);
    String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

    Cursor cursor2 =
            contentResolver.query(
                    uri,
                    projection,
                    null,
                    null,
                    null);

    if (cursor2 != null) {
        while (cursor2.moveToNext()) {
            contactId = cursor2.getString(cursor2.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }



    }
    cursor2.close();
    if(contactId!=null){
        Log.i(TAG, "ContactId: "+ contactId);
        inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));
    }


/*        if (inputStream != null) {
            photo = BitmapFactory.decodeStream(inputStream);
        }*/
    return inputStream;
}
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_tab_test, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
