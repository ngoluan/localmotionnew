package luan.localmotion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.yelp.clientlib.entities.Business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.NextBusDashItem;
import luan.localmotion.Content.PlacesItem;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {DashFragment.OnMapInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DashFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashFragment extends Fragment implements OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener, YourFragmentInterface {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public View view;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public GoogleMap mMap;
    public Marker locMarker;

    public ScrollView mScrollView;

    private boolean isSpeakButtonLongPressed = false;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public int squareSize;
    private GridLayout grid;
    private OnDashFragmentInteractionListener mListener;

    public MainActivity activity;
    public NextBus nextBus;
    public BikeShare bikeShare;
    public Contacts contacts;
    Location mCurrentLocation;
    public int mapMarkerType;
    public int NEXTBUS=0;
    public int BIKESHARE=0;

    private ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
    RecyclerView contactRecyclerView;
    ContactRecyclerViewAdapter contactRecyclerViewAdapter;

    RecyclerView placesRecyclerView;
    PlacesRecyclerViewAdapter placesRecyclerViewAdapter;

    OnContactListListener onContactListListerner;

    public ArrayList<PlacesItem> placesItems = new ArrayList<PlacesItem>();
    public DashFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DashFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DashFragment newInstance(String param1, String param2) {
        DashFragment fragment = new DashFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        activity = (MainActivity) getActivity();


        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));
    }
    BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mCurrentLocation = new Location(intent.getExtras().getString("provider"));
            mCurrentLocation.setLongitude(intent.getExtras().getDouble("lng"));
            mCurrentLocation.setLatitude(intent.getExtras().getDouble("lat"));
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dash, container, false);

        Point dSize = getDisplaySize();
        squareSize = (dSize.x - 24)/ 3 ;

        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        grid = (GridLayout) view.findViewById(R.id.grid);

        if (mMap == null) {
            View mapView = view.findViewById(R.id.dashMap);
            ViewGroup.LayoutParams mapLayout = mapView.getLayoutParams();
            mapLayout.height = squareSize * 2;
            mapLayout.width = squareSize * 2;
            mapView.setLayoutParams(mapLayout);

            CustomMapView mapFragment = (CustomMapView) getChildFragmentManager()
                    .findFragmentById(R.id.dashMap);
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new CustomMapView.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        View view1 = view.findViewById(R.id.square1);
        ViewGroup.LayoutParams lpGl1 = view1.getLayoutParams();
        lpGl1.height = squareSize;
        lpGl1.width = squareSize * 2;

        view1.setLayoutParams(lpGl1);
        view1.setId(R.id.transitView);
        view1.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(MainActivity.TAG, "Luan-onClick: viewTransit");
                drawNextBus();
            }
        });
        RelativeLayout viewUber = (RelativeLayout) view.findViewById(R.id.square2);
        ViewGroup.LayoutParams lpGlUber = viewUber.getLayoutParams();
        lpGlUber.height = squareSize;
        lpGlUber.width = squareSize;
        viewUber.setLayoutParams(lpGlUber);

        RelativeLayout viewBike = (RelativeLayout) view.findViewById(R.id.square3);
        ViewGroup.LayoutParams lpGlBike = viewBike.getLayoutParams();
        lpGlBike.height = squareSize;
        lpGlBike.width = squareSize;
        viewBike.setLayoutParams(lpGlBike);
        viewBike.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Log.d(MainActivity.TAG, "Luan-onClick: viewBike");
                    drawBikeShare();
                }
            });

        nextBus = new NextBus(getActivity());
        bikeShare = new BikeShare(getActivity());
        contacts = new Contacts(getActivity());

        getContacts();
        getBikeshare();
        if (mCurrentLocation != null) {
            setupDash(mCurrentLocation);
        }

        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));

        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("AGHkbAzvvw5i0dxzJw75Tdv2ZA8iN6L0") //This is necessary
                .setRedirectUri("EYFoUMRpm4eBXmhQ6HrBBXU1inOJv9LU8kDA83Lk") //This is necessary if you'll be using implicit grant
                .setEnvironment(SessionConfiguration.Environment.SANDBOX) //Useful for testing your app in the sandbox environment
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                .build();

//This is a convenience method and will set the default config to be used in other components without passing it directly.
        UberSdk.initialize(config);

/*UBER
        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("YOUR_CLIENT_ID")
                .setServerToken("YOUR_SERVER_TOKEN")
                .build();

        ServerTokenSession session = new ServerTokenSession(config);
        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("YOUR_CLIENT_ID")
                .setClientSecret("YOUR_CLIENT_SECRET")
                .setScopes(yourScopes)
                .setRedirectUri(redirectUri)
                .build();

        OAuth2Credentials credentials = new OAuth2Credentials.Builder()
                .setSessionConfiguration(config)
                .build();
*/


        onContactListListerner= new OnContactListListener() {
            @Override
            public void OnContactClickListener(String TAG, ContactItem item) {

            }
        };


        return view;
    }
    public void drawNextBus(){
        mapMarkerType = NEXTBUS;
        ArrayList<VehicleData> currentVehicles= lookupOnScreenVehicles();
        mMap.clear();
        nextBus.drawMarkers(currentVehicles, mMap);
    }
    public void drawBikeShare(){
        mapMarkerType = BIKESHARE;
        mMap.clear();
        ArrayList<BikeShareItem> currenStations= lookupOnScreenBikess();
        //bikeShare.findNearestBikeShare(mCurrentLocation,mMap);
        bikeShare.drawMarkers(currenStations, mMap);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        MainActivity activity = (MainActivity) getActivity();
        Location currentLocation = activity.getCurrentLocation();
        if (currentLocation != null) {
            LatLng loc = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));


        }
        nextBus.getVehicleLocations();
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(mapMarkerType==NEXTBUS)
                    drawNextBus();
                else if(mapMarkerType==BIKESHARE)
                    drawBikeShare();
            }
        });
    }
    public ArrayList<VehicleData> lookupOnScreenVehicles(){
        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;
        ArrayList<VehicleData> currentVehicles= new ArrayList<VehicleData>();

        for (VehicleData vehicle:nextBus.nextBusData) {

            if(vehicle.lat<curScreen.northeast.latitude && vehicle.lat>curScreen.southwest.latitude &&
                    vehicle.lng<curScreen.northeast.longitude&& vehicle.lng>curScreen.southwest.longitude){
                currentVehicles.add(vehicle);
            }
        }
        return currentVehicles;
    }
    public ArrayList<BikeShareItem> lookupOnScreenBikess(){
        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;
        ArrayList<BikeShareItem> currentStations= new ArrayList<BikeShareItem>();

        for (BikeShareItem station:bikeShare.bikeShareItems) {

            if(station.lat<curScreen.northeast.latitude && station.lat>curScreen.southwest.latitude &&
                    station.lng<curScreen.northeast.longitude&& station.lng>curScreen.southwest.longitude){
                currentStations.add(station);
            }
        }
        return currentStations;
    }
    public void setupDash(Location mCurrentLocation){
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
            /*locMarker = mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));*/

        }


        getPlaces(mCurrentLocation);
        getTransit(mCurrentLocation);
    }

    public Point getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        String displayName = display.getName();  // minSdkVersion=17+


// display size in pixels
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        return new Point(width, height);
    }
    @Override
    public void onRefresh() {

    }

    @Override
    public void fragmentBecameVisible() {

    }

    @Override
    public void fragmentBecameInvisible() {
        if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}
        try{
            SupportMapFragment f = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.dashMap);
            if (f != null){
                getChildFragmentManager().beginTransaction().remove(f).commit();
                mMap=null;
            }
            if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}

        }catch(Exception e){
        }
    }

    public class PredictionDraw implements Runnable {
        ArrayList<ArrayList<NextBusDashItem>> routesArr;
        public PredictionDraw(ArrayList<ArrayList<NextBusDashItem>> routesArr){
            this.routesArr=routesArr;
        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

            final RelativeLayout transitView = (RelativeLayout) view.findViewById(R.id.transitView);
            final LinearLayout innerLayoutRow1 = new LinearLayout(getContext());
            innerLayoutRow1.setOrientation(LinearLayout.HORIZONTAL);


            final LinearLayout innerLayoutRow2 = new LinearLayout(getContext());
            innerLayoutRow2.setOrientation(LinearLayout.HORIZONTAL);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            param.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            innerLayoutRow2.setLayoutParams(param);



            int width = transitView.getWidth()/4;
            int x=0;
            for (int i = 0; i < routesArr.size(); i++) {

                for (int j = 0; j < routesArr.get(i).size(); j++) {
                    routesArr.get(i).get(j);


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

                    View icon = getActivity().getLayoutInflater().inflate(R.layout.view_transit,null);
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

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    transitView.addView(innerLayoutRow1);
                    transitView.addView(innerLayoutRow2);

                    YoYo.with(Techniques.SlideInDown)
                            .duration(700)
                            .playOn(innerLayoutRow1);
                    YoYo.with(Techniques.SlideInDown)
                            .duration(700)
                            .playOn(innerLayoutRow2);
                }
            });
        }

    }
    public void getTransit(Location loc){
        nextBus.setNextBusListener(new NextBus.NextBusListener(){

            @Override
            public void OnGetPredictions(ArrayList<ArrayList<NextBusDashItem>> routesArr) {

                Thread t = new Thread(new PredictionDraw(routesArr));
                t.start();


            }

            @Override
            public void OnGetVehicles(ArrayList<VehicleData> vehicleData) {

                drawNextBus();
            }
        });
        nextBus.getPredictionLocation(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()));
    }
    public void getBikeshare(){
        bikeShare.getStations();
        bikeShare.setBikeShareListener(new BikeShare.BikeShareListener(){


            @Override
            public void OnGetBikes(ArrayList<BikeShareItem> bikeData) {
/*
                mMap.clear();
                bikeShare.drawMarkers(bikeData, mMap);*/
            }


        });
    }
    public void getPlaces(Location loc){
        //getPlaces_v2(loc);
        MainActivity caller = (MainActivity) getActivity();

        Map<String, String> params = new HashMap<>();
        params.put("term", "food");
        params.put("category", "restaurant");

        View view2 = getActivity().getLayoutInflater().inflate(R.layout.dash_places, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.square4);
        ViewGroup.LayoutParams lpGl2 = layout.getLayoutParams();
        lpGl2.height = squareSize;
        lpGl2.width = squareSize;
        layout.setLayoutParams(lpGl2);

        layout.addView(view2);

        ImageView img = (ImageView) view2.findViewById(R.id.imageView);
        img.setImageDrawable(getResources().getDrawable(R.drawable.placesicon));

        final TextView name = (TextView) view2.findViewById(R.id.name);
        final TextView category = (TextView) view2.findViewById(R.id.category);

/*        (new Thread(new Runnable() {

            @Override
            public void run() {

                Looper.prepare();
                YoYo.with(Techniques.FadeOutLeft)
                        .duration(700)
                        .playOn(name);
                Looper.loop();
            }
        })).start();*/

        final Handler handler = new Handler();
        final int[] count = {1};
        handler.post(new Runnable() {
            @Override
            public void run() {


                YoYo.with(Techniques.FadeOutLeft)
                        .duration(700)
                        .playOn(name);
                YoYo.with(Techniques.FadeInRight)
                        .duration(700)
                        .playOn(category);

                if(count[0] %2==0)  { //trigger on alternate counts }
                    YoYo.with(Techniques.FadeInRight)
                            .duration(700)
                            .playOn(name);
                    YoYo.with(Techniques.FadeOutLeft)
                            .duration(700)
                            .playOn(category);

                }
                count[0]++;
                handler.postDelayed(this,5000);
            }
        });

        caller.places.searchNearby(loc.getLatitude(),loc.getLongitude(), params,view2);
        caller.places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(final ArrayList<Business> businesses, View view) {

                String businessName = businesses.get(0).name();  // "JapaCurry Truck"
                String categoryName= businesses.get(0).categories().get(0).name();  // "JapaCurry Truck"

                TextView name = (TextView) view.findViewById(R.id.name);
                name.setText(businessName);
                TextView category = (TextView) view.findViewById(R.id.category);
                category.setText(categoryName);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.
                            Map<String, String> viewParams= new HashMap<>();
                            viewParams.put("type","places");
                            viewParams.put("placeId", businesses.get(0).id());
                            mListener.onDashFragmentInteraction(viewParams);
                        }
                    }
                });
            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });

        Map<String, String> params2 = new HashMap<>();
        params2.put("term", "bars");
        params2.put("category", "bars");
        View view3 = getActivity().getLayoutInflater().inflate(R.layout.dash_places, null);
        RelativeLayout layout2 = (RelativeLayout) view.findViewById(R.id.square5);
        ViewGroup.LayoutParams lpGl3 = layout2.getLayoutParams();
        lpGl3.height = squareSize;
        lpGl3.width = squareSize;
        layout2.setLayoutParams(lpGl3);
        final TextView name2 = (TextView) view3.findViewById(R.id.name);
        final TextView category2 = (TextView) view3.findViewById(R.id.category);

        final Handler handler2 = new Handler();
        final int[] count2= {1};
        handler2.post(new Runnable() {
            @Override
            public void run() {


                YoYo.with(Techniques.FadeOutLeft)
                        .duration(700)
                        .playOn(name2);
                YoYo.with(Techniques.FadeInRight)
                        .duration(700)
                        .playOn(category2);

                if(count2[0] %2==0)  { //trigger on alternate counts }
                    YoYo.with(Techniques.FadeInRight)
                            .duration(700)
                            .playOn(name2);
                    YoYo.with(Techniques.FadeOutLeft)
                            .duration(700)
                            .playOn(category2);

                }
                count2[0]++;
                handler2.postDelayed(this,5000);
            }
        });


        layout2.addView(view3);
        img = (ImageView) view3.findViewById(R.id.imageView);
        img.setImageDrawable(getResources().getDrawable(R.drawable.drinksicon));

        caller.places.searchNearby(loc.getLatitude(),loc.getLongitude(), params2,view3);
    }
    public void getContacts_v2(){


        HashMap<String, String> contactsMap= new HashMap<String, String>();
        String[] projection= {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.TIMES_CONTACTED,
                ContactsContract.Contacts.LAST_TIME_CONTACTED,
                ContactsContract.Contacts.STARRED};

        Cursor cursor = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, null ,null, ContactsContract.Contacts.TIMES_CONTACTED+" desc LIMIT 6"  );
        cursor.moveToFirst();


        if( cursor.getCount() > 0 ) {
            while( cursor.moveToNext() ) {
                String id = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(projection[1]));
                String uriPhoto = cursor.getString(cursor.getColumnIndex(projection[2]));
                String timesContacted = cursor.getString(cursor.getColumnIndex(projection[3]));
                String lastTimeContacted = cursor.getString(cursor.getColumnIndex(projection[4]));
                String starred = cursor.getString(cursor.getColumnIndex(projection[5]));


                Cursor pCur =  getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{id}, null);
                while (pCur.moveToNext()) {
                    String phoneNo = pCur.getString(pCur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER ));
                    Log.d(MainActivity.TAG, "Luan-getContacts_v2: "+phoneNo+","+timesContacted+","+lastTimeContacted+","+starred+","+uriPhoto);


                    if (!contactsMap.containsKey(phoneNo)) {
                        contactsMap.put(phoneNo, id);
                        contactList.add(new ContactItem(id, displayName, phoneNo,null, uriPhoto));
                    }

                }
                pCur.close();

            }
            cursor.close();
            contactRecyclerView = (RecyclerView) view.findViewById(R.id.contactList);
            // Set the adapter
            if (contactRecyclerView instanceof RecyclerView) {
                Context context = view.getContext();

                contactRecyclerView.setItemAnimator(new SlideInLeftAnimator());
                contactRecyclerView.setLayoutManager(new GridLayoutManager(context, 3));

                contactRecyclerViewAdapter= new ContactRecyclerViewAdapter(contactList, onContactListListerner);
                AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(contactRecyclerViewAdapter);
                contactRecyclerView.setAdapter(alphaInAnimationAdapter);

            }
        }

    }
    public void getContacts(){

        getContacts_v2();

        ArrayList<String> conversation = new ArrayList<>();


        Uri uri    = Uri.parse( "content://sms/inbox" );
        String[] projection= { "DISTINCT Replace(Address, '+', '') AS ADDRESS"};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null ,null, "DATE desc"  );

        cursor.moveToFirst();
        if( cursor.getCount() > 0 ) {
            String count = Integer.toString( cursor.getCount() );
            int j=0;
            int row=2;
            int col=1;
            while( cursor.moveToNext() ) {
                if(j>1){break;}
                String result = "";

                for( int i = 0; i < cursor.getColumnCount(); i++ ) {
                    result = result + "\nindex " + i + "\n column is "
                            + cursor.getColumnName( i ) + "\nvalue is " + cursor.getString( i );
                }

                result = result + "\n new conversation";


                int SENDER_ADDRESS = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS);
                final String address = cursor.getString(SENDER_ADDRESS);
                String contactName = contacts.getContactName(getContext(), cursor.getString(SENDER_ADDRESS));
                int contactId= contacts.getContactIDFromNumber(String.valueOf(SENDER_ADDRESS), getContext());

                //InputStream photoIS = openPhoto(id);

                if(isInteger(contactName)){continue;}
                conversation.add( result );

                View view4 = getActivity().getLayoutInflater().inflate(R.layout.dash_people, null);
                int squareNum = j +6;
                int resID = getResources().getIdentifier(String.valueOf("square"+squareNum), "id", "luan.localmotion");
                Log.d(MainActivity.TAG, "Luan-getContacts: square"+squareNum);
                RelativeLayout layout = (RelativeLayout) view.findViewById(resID);
                ViewGroup.LayoutParams lpGl4 = layout.getLayoutParams();
                lpGl4.height = squareSize;
                lpGl4.width = squareSize;


                view4.setLayoutParams(lpGl4);

                Bitmap profilePic= Contacts.retrieveContactPhoto(getContext(),cursor.getString(SENDER_ADDRESS));
                ImageView img = (ImageView) view4.findViewById(R.id.imageView);
                if(profilePic!=null){
                    img.setImageBitmap(profilePic);
                }else{
                    Drawable res = getResources().getDrawable(R.drawable.personicon);
                    img.setImageDrawable(res);
                }

                TextView name = (TextView) view4.findViewById(R.id.category);
                name.setText(contactName);
                view4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(MainActivity.TAG, "onClick: ");
                        if (null != mListener) {
                            // Notify the active callbacks interface (the activity, if the
                            // fragment is attached to one) that an item has been selected.

                            Map<String, String> viewParams= new HashMap<>();
                            viewParams.put("type","contact");
                            viewParams.put("contactPhone", address);
                            mListener.onDashFragmentInteraction(viewParams);
                        }
                    }
                });
                layout.addView(view4);

                j++;
            }
        }

        cursor.close();
    }
    public void getPlaces_v2(Location loc){
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(6));
        fillPlacesList(loc, params);
    }
    public void fillPlacesList(Location loc, Map<String, String> params) {
        MainActivity caller = (MainActivity) getActivity();
        Places places = new Places(getActivity());
        places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {
                placesRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });
        places.searchNearby(loc.getLatitude(), loc.getLongitude(), params, null);


    }
    public static boolean isInteger(String str) {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDashFragmentInteractionListener) {
            mListener = (OnDashFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDashFragmentInteractionListener");
        }
        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
        if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}
    }


    public interface OnDashFragmentInteractionListener {
        // TODO: Update argument type and name
        void onDashFragmentInteraction(Map<String, String> param);
        void onDashFragmentInteraction(Uri uri);
    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }
}
