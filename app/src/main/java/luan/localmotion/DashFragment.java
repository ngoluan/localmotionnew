package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.NextBusDashItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashFragment extends Fragment implements OnMapReadyCallback, SwipeRefreshLayout.OnRefreshListener, FragmentInterface {
    public View view;
    public ScrollView mScrollView;
    public int squareSize3;
    public int squareSize4;

    public GoogleMap mMap;
    public List<Integer> mapViews=new ArrayList<Integer>();
    public BikeShare bikeShare;
    public MainActivity activity;
    public NextBus nextBus;
    ExpandableLinearLayout dashTransitExpandableLayout;
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

        /*RelativeLayout viewBike = (RelativeLayout) view.findViewById(R.id.square3);
        viewBike.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    drawBikeShare();
                }
            });

        View uberOverlay = (View) view.findViewById(R.id.dashUberOverlay);
        uberOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View uberButton = (View) view.findViewById(R.id.dashUberButton);
                uberButton.callOnClick();
            }
        });*/

        CustomMapView mapFragment = (CustomMapView) getChildFragmentManager()
                .findFragmentById(R.id.dashMap);
        mapFragment.getMapAsync(this);
        mapFragment.setListener(new CustomMapView.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        dashTransitExpandableLayout = (ExpandableLinearLayout) view.findViewById(R.id.dashTransitExpandableLayout);
        //dashTransitExpandableLayout.setClosePosition((int) Math.round(squareSize4*3));
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        bikeShare.setUpClusterer(mMap,getContext());
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {

                for (int i = 0; i < mapViews.size(); i++) {
                    if(mapViews.get(i)==NEXTBUS)
                        drawNextBus();
                    else if(mapViews.get(i)==BIKESHARE)
                        bikeShare.onCameraChange(cameraPosition, mMap);
                }

            }
        });
    }
    public void setupDash(Location mCurrentLocation){

        //getContacts();
        setupMap(mCurrentLocation);
        getBikeshare();
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

        //ArrayList<BikeShareItem> currenStations= lookupOnScreenBikes();
        bikeShare.drawMarkers(bikeShare.bikeShareItems, mMap);

    }
    void getEvents(){
        EventBrite.getEvents(getContext(), "date", new Callback<EventbriteEvents>() {
            @Override
            public void onResponse(Call<EventbriteEvents> call, Response<EventbriteEvents> response) {
                ArrayList<EventbriteEvent> models = new ArrayList<EventbriteEvent>();
                models.addAll(response.body().getEvents());

                HorizontalScrollView layout = (HorizontalScrollView) view.findViewById(R.id.dashEventsHsv);
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
                    final EventbriteEvent item = models.get(i);
                    View eventView = getActivity().getLayoutInflater().inflate(R.layout.view_event, null);

                    eventView.setLayoutParams(new ViewGroup.LayoutParams(squareSize3 * 3, ViewGroup.LayoutParams.MATCH_PARENT));

                    Calendar beginTime = Calendar.getInstance();

                    Date parsedDate = null;
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                        parsedDate = dateFormat.parse(item.start.local);
                        beginTime.setTime(parsedDate);

                    } catch (ParseException e) {
                        e.printStackTrace();

                    }
                    SimpleDateFormat newDate = new SimpleDateFormat("EEEE, MMM d',' h:mm a");
                    TextView eventName = (TextView) eventView.findViewById(R.id.eventName);
                    TextView eventCategory = (TextView) eventView.findViewById(R.id.eventCategory);
                    TextView eventTime = (TextView) eventView.findViewById(R.id.eventTime);
                    TextView eventDescription = (TextView) eventView.findViewById(R.id.eventDescription);
                    TextView eventAddress= (TextView) eventView.findViewById(R.id.eventAddress);
                    ImageView eventImgView = (ImageView) eventView.findViewById(R.id.eventImgView);

                    eventName.setText(item.name.text);
                    if(item.category!=null) eventCategory.setText(item.category.shortName);
                    eventDescription.setText(item.description.text);
                    eventAddress.setText(item.venue.address.address_1);
                    eventTime.setText(newDate.format(parsedDate));
                    if (item.logo != null) {
                        if (!item.logo.url.equals(""))
                            Picasso.with(getContext()).load(item.logo.url)
                                    .error(R.drawable.calendaricon)
                                    .placeholder(R.drawable.calendaricon)
                                    .into(eventImgView);
                    }
                    eventView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Map<String, String> viewParams= new HashMap<>();
                            viewParams.put("type","events");
                            viewParams.put(EventBrite.ID_TAG, item.getId());
                            mListener.onDashFragmentInteraction(viewParams);
                        }
                    });
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
        FlexboxLayout transitView;
        public PredictionDraw(ArrayList<ArrayList<NextBusDashItem>> routesArr,FlexboxLayout transitView){
            this.transitView=transitView;
            this.routesArr=routesArr;

        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            int x=0;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int totalViews = transitView.getChildCount();
                    for (int i = 0; i < totalViews; i++) {
                        View child = (View) transitView.getChildAt(i);
                        if(child.getTag()==String.valueOf(NEXTBUS))
                            transitView.removeView(child);
                    }
                    transitView.removeViews(0,totalViews-2);
                }
            });

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
                    String routeShortTitle =routesArr.get(i).get(0).routeTitle.substring(0,c);

                    View icon = getActivity().getLayoutInflater().inflate(R.layout.view_transit,null);
                    LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(squareSize4, squareSize4);
                    icon.setLayoutParams(param2);
                    TextView route = (TextView) icon.findViewById(R.id.route);
                    route.setText(routeShortTitle + "-"+dirShortTitle);
                    TextView eta = (TextView) icon.findViewById(R.id.eta);
                    eta.setText(String.valueOf(routesArr.get(i).get(j).eta) + " mins");
                    icon.setTag(NEXTBUS);

                    draw(icon, x);
                    if(x>4){
                        return;
                    }
                    x++;

                }

            }

        }
        void draw(final View icon, final int position){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    transitView.addView(icon, position);
                    YoYo.with(Techniques.SlideInRight)
                            .duration(700)
                            .playOn(icon);
                }
            });
        }
    }
    public class PredictionDraw_2 implements Runnable {
        List<NextBusPrediction> routesArr;
        FlexboxLayout transitView;
        public PredictionDraw_2(List<NextBusPrediction> routesArr,FlexboxLayout transitView){
            this.transitView=transitView;
            this.routesArr=routesArr;

        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            int x=0;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int totalViews = transitView.getChildCount();
                    for (int i = 0; i < totalViews; i++) {
                        View child = (View) transitView.getChildAt(i);
                        if(child.getTag()==String.valueOf(NEXTBUS))
                            transitView.removeView(child);
                    }
                    transitView.removeViews(0,totalViews-1);
                }
            });

            //loop through directions
            for (int i = 0; i < routesArr.size(); i++) {
                View item = getActivity().getLayoutInflater().inflate(R.layout.view_transit_long,null);
                TextView routeView = (TextView) item.findViewById(R.id.transitRouteNumber);
                routeView.setText(routesArr.get(i).route.id);

                int c = routesArr.get(i).route.title.indexOf("-")+1;
                String routeShortTitle =routesArr.get(i).route.title.substring(c);

                TextView routeNameView = (TextView) item.findViewById(R.id.transitRouteName);
                routeNameView.setText(routeShortTitle);

                String direction = routesArr.get(i).values.get(0).direction.title.replace("To: ","");
                int start=direction.indexOf("-");
                int end=direction.indexOf("towards");
                if(end>start) //sometimes, it doesn't fit this
                    direction=direction.substring(0, start-1) + " " + direction.substring(end);
                TextView directionView = (TextView) item.findViewById(R.id.transitDirection);
                directionView.setText(direction);
                String eta="";

                TextView etaView = (TextView) item.findViewById(R.id.transitETA);

                for (int j = 0; j < routesArr.get(i).values.size(); j++) {

                    eta +=routesArr.get(i).values.get(j).minutes + " & ";
                    if(j==1) break;

                }

                eta = eta.substring(0,eta.length()-2);
                etaView.setText(eta);

                item.setTag(NEXTBUS);
                draw(item, x);

                x++;

            }

        }
        void draw(final View icon, final int position){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    transitView.addView(icon, position);
/*                    YoYo.with(Techniques.SlideInRight)
                            .duration(700)
                            .playOn(icon);*/
                }
            });
        }
    }
    public class PredictionDraw_3 implements Runnable {
        List<NextBusPrediction> routesArr;
        FlexboxLayout transitView;
        public PredictionDraw_3(List<NextBusPrediction> routesArr, FlexboxLayout transitView){
            this.transitView=transitView;
            this.routesArr=routesArr;

        }
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            int numberOfItems=0;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int totalViews = transitView.getChildCount();
                    for (int i = 0; i < totalViews; i++) {
                        View child = (View) transitView.getChildAt(i);
                        transitView.removeView(child);
                    }
                    transitView.removeViews(0,totalViews);
                }
            });

            //loop through directions
            final List<View > treemap = new ArrayList<View>();
            for (int route = 0; route < routesArr.size(); route++) {
                for (int vehicle = 0; vehicle < routesArr.get(route).values.size(); vehicle++) {
                    View item = getActivity().getLayoutInflater().inflate(R.layout.view_transit_v2,null);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(squareSize4, (int)Math.round(squareSize4*1.5));
                    item.setLayoutParams(layoutParams);
                    CircleProgressView mCircleView = (CircleProgressView) item.findViewById(R.id.transitCircleView);
                    //TextView routeView = (TextView) item.findViewById(R.id.transitRouteNumber);
                    //routeView.setText(routesArr.get(route).route.id);

                    int c = routesArr.get(route).route.title.indexOf("-")+1;
                    String routeShortTitle =routesArr.get(route).route.title.substring(c);

                    //TextView routeNameView = (TextView) item.findViewById(R.id.transitRouteName);
                    //routeNameView.setText(routeShortTitle);

                    String direction = routesArr.get(route).values.get(0).direction.title.replace("To: ","");
                    int start=direction.indexOf("-");
                    int end=direction.indexOf("towards");
                    if(end>start) //sometimes, it doesn't fit this
                    {
                        direction=direction.substring(0, start-1) + " " + direction.substring(end);
                    }
                    TextView directionView = (TextView) item.findViewById(R.id.transitDirection);
                    String sourceString = "<b>"+routesArr.get(route).route.title+"</b>"+ ": "+ direction;
                    directionView.setText(Html.fromHtml(sourceString));

                    TextView etaView = (TextView) item.findViewById(R.id.transitETA);



                    Integer eta =Integer.parseInt(routesArr.get(route).values.get(vehicle).minutes);
                        if(vehicle==2) break;

                    mCircleView.setTextMode(TextMode.VALUE);
                    mCircleView.setValue(eta);
                    mCircleView.setUnit("mins");
                    //mCircleView.setText(routesArr.get(route).route.title);
                    //eta = eta.substring(0,eta.length()-2);
                    //etaView.setText(routesArr.get(route).route.id);

                    item.setTag(eta);
                    treemap.add(item);
                    //draw(item, numberOfItems);

                    numberOfItems++;
                }
            }

            Collections.sort(treemap, new Comparator<View>(){
                public int compare(View emp1, View emp2) {
                    // ## Ascending order
                    if (Integer.parseInt(emp1.getTag().toString()) > Integer.parseInt(emp2.getTag().toString())) {
                        return 1;
                    }
                    else if (Integer.parseInt(emp1.getTag().toString()) < Integer.parseInt(emp2.getTag().toString())) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                    // return Integer.valueOf(emp1.getId()).compareTo(emp2.getId()); // To compare integer values

                    // ## Descending order
                    // return emp2.getFirstName().compareToIgnoreCase(emp1.getFirstName()); // To compare string values
                    // return Integer.valueOf(emp2.getId()).compareTo(emp1.getId()); // To compare integer values
                }
            });
            for (int i = 0; i < treemap.size(); i++) {
                draw(treemap.get(i), i);
            }
            /*getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //transitView.addView(icon, position);
*//*                    YoYo.with(Techniques.SlideInRight)
                            .duration(700)
                            .playOn(icon);*//*
                    for (int i = 0; i < treemap.size(); i++) {
                        transitView.addView(treemap.get(i), i);
                    }
                    //
                }
            });*/


        }
        void draw(final View icon, final int position){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    transitView.addView(icon, position);
/*                    YoYo.with(Techniques.SlideInRight)
                            .duration(700)
                            .playOn(icon);*/
                }
            });
        }
    }
    public void getTransit(Location loc){
        nextBus.setNextBusListener(new NextBus.NextBusListener(){

            @Override
            public void OnGetPredictions(ArrayList<ArrayList<NextBusDashItem>> routesArr) {

            }

            @Override
            public void OnGetVehicles(ArrayList<VehicleData> vehicleData) {

                drawNextBus();
            }
        });

        nextBus.getPredictionLocation_v2(String.valueOf(loc.getLatitude()), String.valueOf(loc.getLongitude()), new Callback<List<NextBusPrediction>>() {
            @Override
            public void onResponse(Call<List<NextBusPrediction>> call, Response<List<NextBusPrediction>> response) {

                FlexboxLayout transitView = (FlexboxLayout) view.findViewById(R.id.transitView);
                Thread t = new Thread(new PredictionDraw_3(response.body(), transitView));
                t.start();
            }

            @Override
            public void onFailure(Call<List<NextBusPrediction>> call, Throwable t) {

            }
        }, getContext());
    }
    public void getBikeshare(){
        bikeShare.setBikeShareListener(new BikeShare.BikeShareListener(){


            @Override
            public void OnGetBikes(ArrayList<BikeShareItem> bikeData) {
                drawBikeShare();
            }


        });
        bikeShare.getStations();
    }
    public void getPlaces(Location loc){
        MainActivity caller = (MainActivity) getActivity();

        Map<String, String> params = new HashMap<>();

        //TODO make this dependent on time of day
        params.put("category_filter", "restaurants,bars,coffee");
        params.put("limit", "12");
        params.put("sort", "2");

        final FlexboxLayout layout = (FlexboxLayout) view.findViewById(R.id.dashPlacesGrid);
        layout.removeAllViews();

        caller.places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(final ArrayList<Business> businesses) {
                for (int i = 0; i < businesses.size(); i++) {
                    String businessName = businesses.get(i).name();
                    String businessId = businesses.get(i).id();
                    String categoryName = businesses.get(i).categories().get(0).name();
                    String img = businesses.get(i).imageUrl();

                    addPlacesChild(businessName, businessId, categoryName, img);


                    if(i==11)break;
                }
            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });
        caller.places.searchNearby(loc.getLatitude(),loc.getLongitude(), params);


    }
    void addPlacesChild(String businessName, final String businessId, String categoryName, String img){
        final FlexboxLayout layout = (FlexboxLayout) view.findViewById(R.id.dashPlacesGrid);
        View placesView = getActivity().getLayoutInflater().inflate(R.layout.dash_places, null);

        ImageView imgView = (ImageView) placesView.findViewById(R.id.placeImageView);
        imgView.setImageDrawable(getResources().getDrawable(R.drawable.placesicon));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(squareSize3, squareSize3);
        placesView.setLayoutParams(params);

        Picasso.with(getContext()).load(img)
                .error(R.drawable.placesicon)
                .placeholder(R.drawable.placesicon)
                .into(imgView);


        final TextView nameView = (TextView) placesView.findViewById(R.id.placeNameView);
        nameView.setText(businessName);

        final TextView categoryView = (TextView) placesView.findViewById(R.id.placeCategoryView);
        categoryView.setText(categoryName);


        placesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    Map<String, String> viewParams= new HashMap<>();
                    viewParams.put("type","places");
                    viewParams.put("yelpPlaceId", businessId);
                    mListener.onDashFragmentInteraction(viewParams);
                }
            }
        });
        layout.addView(placesView);
        YoYo.with(Techniques.ZoomIn)
                .duration(700)
                .playOn(placesView);


        final Handler handler = new Handler();
        final int[] count = {1};
        handler.post(new Runnable() {
            @Override
            public void run() {


                YoYo.with(Techniques.FadeOutLeft)
                        .duration(700)
                        .playOn(nameView);
                YoYo.with(Techniques.FadeInRight)
                        .duration(700)
                        .playOn(categoryView);

                if(count[0] %2==0)  { //trigger on alternate counts }
                    YoYo.with(Techniques.FadeInRight)
                            .duration(700)
                            .playOn(nameView);
                    YoYo.with(Techniques.FadeOutLeft)
                            .duration(700)
                            .playOn(categoryView);

                }
                count[0]++;
                handler.postDelayed(this,5000);
            }
        });

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
                ImageView img = (ImageView) contactView[j].findViewById(R.id.placeImageView);
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
                            viewParams.put(ContactItem.UNIQUE_ID, address);
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
    void setSquareHeights(){
        Point dSize = Utils.getDisplaySize(getContext());
        squareSize3 = (dSize.x - 24)/ 3 ;
        squareSize4 = (dSize.x - 24)/ 4 ;

        /*for (int i = 2; i <=3; i++) {
            int resId=Utils.getResFromInt(getContext(),"square",i);
            View thisView = view.findViewById(resId);
            ViewGroup.LayoutParams layoutParams = thisView.getLayoutParams();
            layoutParams.height = squareSize4;
            layoutParams.width = squareSize4;
            thisView.setLayoutParams(layoutParams);
        }*/

        View mapView = view.findViewById(R.id.dashMap);
        ViewGroup.LayoutParams mapLayout = mapView.getLayoutParams();
        mapLayout.height = squareSize3 * 3;
        mapView.setLayoutParams(mapLayout);

        View eventView = view.findViewById(R.id.dashEventsHsv);
        ViewGroup.LayoutParams eventLayout = eventView.getLayoutParams();
        eventLayout.height = squareSize3 * 3;
        eventView.setLayoutParams(eventLayout);

    }

}
