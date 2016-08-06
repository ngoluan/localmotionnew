package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
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
import com.google.android.gms.vision.text.Line;
import com.squareup.picasso.Picasso;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;
import com.yelp.clientlib.entities.Business;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import luan.localmotion.Content.NextBusDashItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashFragment extends Fragment implements OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener, FragmentInterface {
    public View view;
    public ScrollView mScrollView;
    public int squareSize;

    public GoogleMap mMap;
    public int mapMarkerType;
    public List<Integer> mapViews=new ArrayList<Integer>();
    public BikeShare bikeShare;
    public MainActivity activity;
    public NextBus nextBus;

    public Contacts contacts;

    static int NEXTBUS=0;
    static int  BIKESHARE=1;


    private OnDashFragmentInteractionListener mListener;

    public DashFragment() {}

    public static DashFragment newInstance(String param1, String param2) {
        DashFragment fragment = new DashFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        nextBus = new NextBus(getActivity());
        bikeShare = new BikeShare(getActivity());
        contacts = new Contacts(getActivity());

        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("AGHkbAzvvw5i0dxzJw75Tdv2ZA8iN6L0") //This is necessary
                .setRedirectUri("EYFoUMRpm4eBXmhQ6HrBBXU1inOJv9LU8kDA83Lk") //This is necessary if you'll be using implicit grant
                .setEnvironment(SessionConfiguration.Environment.SANDBOX) //Useful for testing your app in the sandbox environment
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                .build();
        mapViews.add(NEXTBUS);
        mapViews.add(BIKESHARE);
        UberSdk.initialize(config);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_dash, container, false);

        setSquareHeights();

        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        View transitView = view.findViewById(R.id.square1);
        transitView.setId(R.id.transitView);
        transitView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

            }
        });

        RelativeLayout viewBike = (RelativeLayout) view.findViewById(R.id.square3);
        viewBike.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    drawBikeShare();
                }
            });

        CustomMapView mapFragment = (CustomMapView) getChildFragmentManager()
                .findFragmentById(R.id.dashMap);
        mapFragment.getMapAsync(this);
        mapFragment.setListener(new CustomMapView.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });
        return view;
    }
    void setSquareHeights(){
        Point dSize = Utils.getDisplaySize(getContext());
        squareSize = (dSize.x - 24)/ 4 ;

        for (int i = 1; i <=11; i++) {
            Log.d(MainActivity.TAG, "Luan-setSquareHeights: "+i);
            int resId=Utils.getResFromInt(getContext(),"square",i);
            View thisView = view.findViewById(resId);
            ViewGroup.LayoutParams layoutParams = thisView.getLayoutParams();
            layoutParams.height = squareSize;
            thisView.setLayoutParams(layoutParams);
        }

        View mapView = view.findViewById(R.id.dashMap);
        ViewGroup.LayoutParams mapLayout = mapView.getLayoutParams();
        mapLayout.height = squareSize * 2;
        mapView.setLayoutParams(mapLayout);

        View eventView = view.findViewById(R.id.square11);
        ViewGroup.LayoutParams eventLayout = mapView.getLayoutParams();
        eventLayout.height = squareSize * 2;
        eventView.setLayoutParams(mapLayout);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.clear();
                for (int i = 0; i < mapViews.size(); i++) {
                    if(mapViews.get(i)==NEXTBUS)
                        drawNextBus();
                    else if(mapViews.get(i)==BIKESHARE)
                        drawBikeShare();
                }

            }
        });
    }
    public void setupDash(Location mCurrentLocation){


        getContacts();
        getBikeshare();
        
        setupMap(mCurrentLocation);
        getPlaces(mCurrentLocation);
        getTransit(mCurrentLocation);
        getEvents();

    }
    void setupMap(final Location mCurrentLocation){

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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

            nextBus.getVehicleLocations();

        }
        else{
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupMap(activity.mCurrentLocation);
                                }
                            });

                        }
                    },
                    1000
            );
        }
    }
    void drawNextBus(){
        //mapMarkerType = NEXTBUS;
        ArrayList<VehicleData> currentVehicles= lookupOnScreenVehicles();

        nextBus.drawMarkers(currentVehicles, mMap);
    }
    void drawBikeShare(){
        //mapMarkerType = BIKESHARE;

        ArrayList<BikeShareItem> currenStations= lookupOnScreenBikes();
        bikeShare.drawMarkers(currenStations, mMap);
    }
    void getEvents(){
        EventBrite.getEventbrite(getContext(), new Callback<EventbriteEvents>() {
            @Override
            public void onResponse(Call<EventbriteEvents> call, Response<EventbriteEvents> response) {
                ArrayList<EventbriteEvent> models = new ArrayList<EventbriteEvent>();
                models.addAll(response.body().getEvents());

                HorizontalScrollView layout = (HorizontalScrollView) view.findViewById(R.id.square11);
                layout.removeAllViews();
                LinearLayout ll = new LinearLayout(getContext());
                ll.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ll.setLayoutParams(llParams);
                layout.addView(ll);

                for (int i = 0; i < models.size(); i++) {
                    if (i > 5) {
                        break;
                    }
                    EventbriteEvent item = models.get(i);
                    View eventView = getActivity().getLayoutInflater().inflate(R.layout.view_event, null);

                    eventView.setLayoutParams(new ViewGroup.LayoutParams(squareSize * 3, ViewGroup.LayoutParams.MATCH_PARENT));

                    Calendar beginTime = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM--dd'T'HH:mm:ss");
                    Date parsedDate = null;
                    try {
                        parsedDate = dateFormat.parse(item.start.local);
                        beginTime.setTime(parsedDate);

                    } catch (ParseException e) {
                        e.printStackTrace();

                    }

                    TextView eventName = (TextView) eventView.findViewById(R.id.eventName);
                    TextView eventTime = (TextView) eventView.findViewById(R.id.eventTime);
                    ImageView eventImgView = (ImageView) eventView.findViewById(R.id.eventImgView);

                    eventName.setText(item.name.text);
                    //eventTime.setText(beginTime);
                    if (item.logo.url != null) {
                        if (!item.logo.url.equals(""))
                            Picasso.with(getContext()).load(item.logo.url)
                                    .error(R.drawable.placesicon)
                                    .placeholder(R.drawable.placesicon)
                                    .into(eventImgView);
                    }

                    ll.addView(eventView);
                }
            }

            @Override
            public void onFailure(Call<EventbriteEvents> call, Throwable t) {

            }


        });




    }
    //TODO can probably combine this
    ArrayList<VehicleData> lookupOnScreenVehicles(){
        LatLngBounds curScreen = mMap.getProjection()
                .getVisibleRegion().latLngBounds;
        ArrayList<VehicleData> currentVehicles= new ArrayList<VehicleData>();

        for (Iterator<VehicleData> it = nextBus.nextBusData.iterator(); it.hasNext(); ) {
            VehicleData vehicle = it.next();
            if(vehicle.lat<curScreen.northeast.latitude && vehicle.lat>curScreen.southwest.latitude &&
                    vehicle.lng<curScreen.northeast.longitude&& vehicle.lng>curScreen.southwest.longitude){
                currentVehicles.add(vehicle);
            }
        }

        return currentVehicles;
    }
    ArrayList<BikeShareItem> lookupOnScreenBikes(){
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
        setupDash(activity.mCurrentLocation);
    }

    @Override
    public void fragmentBecameInvisible() {

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
                drawBikeShare();
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
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.square5);

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
        RelativeLayout layout2 = (RelativeLayout) view.findViewById(R.id.square6);
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

    public void getContacts(){

        ArrayList<String> conversation = new ArrayList<>();


        Uri uri    = Uri.parse( "content://sms/inbox" );
        String[] projection= { "DISTINCT Replace(Address, '+', '') AS ADDRESS"};
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null ,null, "DATE desc"  );

        cursor.moveToFirst();
        View[] contactView=new View[4];
        if( cursor.getCount() > 0 ) {
            int j=0;
            while( cursor.moveToNext() ) {
                if(j>3){break;}

                int SENDER_ADDRESS = cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS);
                final String address = cursor.getString(SENDER_ADDRESS);
                String contactName = contacts.getContactName(getContext(), cursor.getString(SENDER_ADDRESS));

                if(isInteger(contactName)){continue;}

                contactView[j] =  getActivity().getLayoutInflater().inflate(R.layout.dash_people, null);
                //TODO switch to other way of getting int
                int squareNum = j +7;
                int resID = getResources().getIdentifier(String.valueOf("square"+squareNum), "id", "luan.localmotion");
                Log.d(MainActivity.TAG, "Luan-getContacts: square"+squareNum);
                RelativeLayout layout = (RelativeLayout) view.findViewById(resID);

                Bitmap profilePic= Contacts.retrieveContactPhoto(getContext(),cursor.getString(SENDER_ADDRESS));
                ImageView img = (ImageView) contactView[j].findViewById(R.id.imageView);
                if(profilePic!=null){
                    img.setImageBitmap(profilePic);
                }else{
                    Drawable res = getResources().getDrawable(R.drawable.personicon);
                    img.setImageDrawable(res);
                }

                TextView name = (TextView) contactView[j].findViewById(R.id.category);
                name.setText(contactName);
                layout.setOnClickListener(new View.OnClickListener() {
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
                layout.addView(contactView[j]);

                j++;
            }
        }

        cursor.close();
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
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
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
        try{
            SupportMapFragment f = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.dashMap);
            if (f != null){
                getChildFragmentManager().beginTransaction().remove(f).commit();
                mMap=null;
            }

        }catch(Exception e){
        }
    }
}
