package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.orm.SugarRecord;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.yelp.clientlib.entities.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import luan.localmotion.Content.ContactItem;
import me.everything.providers.android.calendar.CalendarProvider;


public class ScheduleFragment extends Fragment implements OnMapReadyCallback ,FragmentInterface {

    ScheduleActvity scheduleActvity;
    private OnFragmentInteractionListener mListener;


    Bundle extras;

    public GoogleMap mMap;
    public Marker locMarker;
    public static final int PICK_CONTACT_REQUEST = 1;

    public static final int PICK_PLACE_REQUEST = 2;
    public static final int RESULT_CANCELED    = 0;
    public static final int RESULT_OK           = -1;
    String placeAddress="";

    View view;
    ScrollView mScrollView;
    MaterialCalendarView calendarView;
    ListView timeListView;
    RecyclerView directionsRecyclerView;
    TimeListViewAdapter mTimeAdapter;
    DirectionsRecyclerViewAdapter mDirectionsAdapter;

    private DirectionsRecyclerViewAdapter.OnDirectionsListener onDirectionsListener;

    public ScheduleFragment() {

    }


    public static ScheduleFragment newInstance() {
        ScheduleFragment fragment = new ScheduleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_schedule, container, false);
        scheduleActvity = (ScheduleActvity) getActivity();

        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);


        if(scheduleActvity.calendarEvent !=null){
            if(!scheduleActvity.calendarEvent.yelpPlaceId.equals("")){
                fillYelpPlace(scheduleActvity.calendarEvent.yelpPlaceId);
            }
        }
        if(scheduleActvity.contactList.size()!=0){
            fillContact_v3(scheduleActvity.contactList);
        }

/*        if(scheduleActvity.extras.getString("placeId")!=null){
            fillYelpPlace(scheduleActvity.extras.getString("placeId"));
        }*/

        View profilePicView= view.findViewById(R.id.contactAdd);
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getContext(), PickContact.class);

                startActivityForResult(contactIntent,PICK_CONTACT_REQUEST);
            }
        });
        final View placePicView= view.findViewById(R.id.placePic);
        placePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent placesIntent = new Intent(getContext(), ActivityPickPlace.class);

                startActivityForResult(placesIntent,PICK_PLACE_REQUEST);
            }
        });

        CustomMapView mapFragment = (CustomMapView) getChildFragmentManager()
                .findFragmentById(R.id.placesMap);
        mapFragment.getMapAsync(this);
        mapFragment.setListener(new CustomMapView.OnTouchListener() {
            @Override
            public void onTouch() {
                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });

        setupPlaceMap();
        setupCalendar();
        setupDirectionsListview();




        return  view;
    }
    void setupPlaceMap(){

        if (mMap != null&&scheduleActvity.mCurrentLocation!=null) {
            Location mCurrentLocation=scheduleActvity.mCurrentLocation;
            LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
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

        }
        else{
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupPlaceMap();
                                }
                            });

                        }
                    },
                    1000
            );
        }
    }
    void setupCalendar(){
        calendarView = (MaterialCalendarView) view.findViewById(R.id.calendarView2);
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                Log.d(MainActivity.TAG, "onDateSelected: "+widget.getSelectedDate());
            }
        });
        Calendar c = Calendar.getInstance();
        CalendarDay today = CalendarDay.from(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        calendarView.setSelectedDate(today);

        //TODO find blocked off spots in calendar
        getCalender();

        setupCalendarListview();

    }
    @Override
    public void onStop() {
        super.onStop();

    }
    public void getCalender(){
        CalendarProvider calendarProvider = new CalendarProvider(getContext());
        List<me.everything.providers.android.calendar.Calendar> calendars = calendarProvider.getCalendars().getList();
        Log.d(MainActivity.TAG, "Luan-getCalender: "+calendars.toString());
        for (me.everything.providers.android.calendar.Calendar calendar:calendars
                ) {

        }
    }

    public String getSelectedDateTime(String firstOrLast){
        String dateStr = calendarView.getSelectedDate().getYear()+"-"+(calendarView.getSelectedDate().getMonth()+1)+"-"+calendarView.getSelectedDate().getDay();
        TimeListViewAdapter adapter = (TimeListViewAdapter) timeListView.getAdapter();
        if(firstOrLast.equals("first")){

            for (int i = 0; i < adapter.list.size(); i++) {
                Log.d(MainActivity.TAG, "adapter " + adapter.mSelectedItemsIds.get(i));
                if (adapter.mSelectedItemsIds.get(i)==true){
                    dateStr += " " + adapter.list.get(i);
                    break;
                }
            }
        }
        else{
            for (int i = adapter.list.size(); i >= 0; i--) {
                if (adapter.mSelectedItemsIds.get(i)==true){
                    dateStr += " " + adapter.list.get(i);
                    break;
                }
            }
        }


        return dateStr;
    }

    @Override
    public void fragmentBecameVisible() {
        setupPlaceMap();
    }

    @Override
    public void fragmentBecameInvisible() {

    }

    class TimeObject{
        String time;
        Boolean selected;
        TimeObject(String time){
            this.time = time;
            selected=false;
        }
    }
    public class DirectionObject {
        String type="";
        String ETA="";
        String description="";
        Direction direction=null;
        DirectionObject(String type, String ETA){
            this.type = type;
            this.ETA = ETA;
        }
    }
    public void setupDirectionsListview(){

        directionsRecyclerView = (RecyclerView) view.findViewById(R.id.schedulerDirectionsRecyclerView);
        //ArrayList<TimeObject> timeList = new ArrayList<TimeObject>();
        ArrayList<DirectionObject> list = new ArrayList<DirectionObject>();
        directionsRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        directionsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        onDirectionsListener = new DirectionsRecyclerViewAdapter.OnDirectionsListener() {
            @Override
            public void OnDirectionsClickListener(DirectionObject item) {

            }
        };
        mDirectionsAdapter = new DirectionsRecyclerViewAdapter(list,onDirectionsListener );

        directionsRecyclerView.setAdapter(mDirectionsAdapter);



    }
    public void setupCalendarListview(){
        timeListView = (ListView)view.findViewById(R.id.timeListView);
        //ArrayList<TimeObject> timeList = new ArrayList<TimeObject>();
        List<String> list = new ArrayList<String>();
        list.add("12:00 am");
        list.add("12:30 am");
        list.add("01:00 am");
        list.add("01:30 am");
        list.add("02:00 am");
        list.add("02:30 am");
        list.add("03:00 am");
        list.add("03:30 am");
        list.add("04:30 am");
        list.add("04:00 am");
        list.add("05:30 am");
        list.add("05:00 am");
        list.add("06:30 am");
        list.add("06:00 am");
        list.add("07:30 am");
        list.add("07:30 am");
        list.add("08:00 am");
        list.add("08:30 am");
        list.add("09:00 am");
        list.add("09:30 am");
        list.add("10:00 am");
        list.add("10:30 am");
        list.add("11:30 am");
        list.add("11:00 am");
        list.add("12:00 pm");
        list.add("12:30 pm");
        list.add("01:00 pm");
        list.add("01:30 pm");
        list.add("02:00 pm");
        list.add("02:30 pm");
        list.add("03:00 pm");
        list.add("03:30 pm");
        list.add("04:30 pm");
        list.add("04:00 pm");
        list.add("05:30 pm");
        list.add("05:00 pm");
        list.add("06:30 pm");
        list.add("06:00 pm");
        list.add("07:30 pm");
        list.add("07:30 pm");
        list.add("08:00 pm");
        list.add("08:30 pm");
        list.add("09:00 pm");
        list.add("09:30 pm");
        list.add("10:00 pm");
        list.add("10:30 pm");
        list.add("11:30 pm");
        list.add("11:00 pm");

        mTimeAdapter = new TimeListViewAdapter(getContext(),R.layout.view_time, list);

        timeListView.setAdapter(mTimeAdapter);
        timeListView.smoothScrollToPosition(12);
        timeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String string = (String) parent.getAdapter().getItem(position);
                //Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                mTimeAdapter.toggleSelection(position);
                Calendar beginTime = getTimestamp(getSelectedDateTime("first"));
                Calendar endTime = getTimestamp(getSelectedDateTime("last"));
                scheduleActvity.calendarEvent.beginTime = beginTime.getTimeInMillis();
                scheduleActvity.calendarEvent.endTime = endTime.getTimeInMillis();
            }
        });
        //setListViewHeightBasedOnChildren(timeListView);
        timeListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        timeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                timeListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                // Capture ListView item click
                timeListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode,
                                                          int position, long id, boolean checked) {

                        // Prints the count of selected Items in title
                        mode.setTitle(timeListView.getCheckedItemCount() + " Selected");

                        // Toggle the state of item after every click on it
                        mTimeAdapter.toggleSelection(position);
                    }

                    /**
                     * Called to report a user click on an action button.
                     * @return true if this callback handled the calendarEvent,
                     *          false if the standard MenuItem invocation should continue.
                     */
                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                       /* if (item.getItemId() == R.id.delete){
                            SparseBooleanArray selected = mTimeAdapter.getSelectedIds();
                            short size = (short)selected.size();
                            for (byte I = 0; I<size; I++){
                                if (selected.valueAt(I)) {
                                    String selectedItem = mTimeAdapter.getItem(selected.keyAt(I));
                                    mTimeAdapter.remove(selectedItem);
                                }
                            }

                            // Close CAB (Contextual Action Bar)
                            mode.finish();
                            return true;
                        }
                        */
                        return false;
                    }

                    /**
                     * Called when action mode is first created.
                     * The menu supplied will be used to generate action buttons for the action mode.
                     * @param mode ActionMode being created
                     * @param menu Menu used to populate action buttons
                     * @return true if the action mode should be created,
                     *          false if entering this mode should be aborted.
                     */
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        //mode.getMenuInflater().inflate(R.menu.menu_main, menu);
                        return true;
                    }

                    /**
                     * Called when an action mode is about to be exited and destroyed.
                     */
                    @Override
                    public void onDestroyActionMode(ActionMode mode) {
                        //  mTimeAdapter.removeSelection();
                    }

                    /**
                     * Called to refresh an action mode's action menu whenever it is invalidated.
                     * @return true if the menu or action mode was updated, false otherwise.
                     */
                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }
                });
                return false;
            }
        });
    }
    public Calendar getTimestamp(String time){
        Calendar beginTime = Calendar.getInstance();
        //beginTime.set(2016, 7,9, 12,0, 0);
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-M-d hh:mm a");
            Date parsedDate = dateFormat.parse(time);
            beginTime.setTime(parsedDate);
            return beginTime;
            //Timestamp startTime = new java.sql.Timestamp(parsedDate.getTime());
            //return startTime;
        }catch(Exception e){
        }
        return beginTime;
    }
    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        Log.d(MainActivity.TAG, "setListViewHeightBasedOnChildren: "+calendarView.getHeight());
        //TODO get pixel from height in utilities
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (250 * scale + 0.5f);
        params.height = pixels;
        listView.setLayoutParams(params);
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.send:
                    break;
                case R.id.done:

                    break;
                case R.id.reject:
                    break;
            }
        }
    };
    public void fillContact_v2(List<String> phones){


        LinearLayout contactsLayout = (LinearLayout) view.findViewById(R.id.scheduleContactsLayout);
        for (String contact : phones) {
            Contacts.fillView(getContext(), Contacts.getContactItem(getContext(), contact),contactsLayout, 96, null);
        }

    }
    public void fillContact_v3(final ArrayList<ContactItem> contacts){


        final LinearLayout contactsLayout = (LinearLayout) view.findViewById(R.id.scheduleContactsLayout);
        int children = contactsLayout.getChildCount();
        for (int i = 0; i < contacts.size(); i++) {
            Contacts.fillView(getContext(), contacts.get(i),contactsLayout, 96, children-1);

            Contacts.isMember(contacts.get(i), getContext(), new Contacts.ContactListener() {
                @Override
                public void OnReceiveIsMember(ContactItem contact, Boolean result) {
                    for (int i1 = 0; i1 < scheduleActvity.contactList.size(); i1++) {
                        if(contact.phoneNumber.equals( scheduleActvity.contactList.get(i1).phoneNumber)){
                            View contactView = (View) contactsLayout.findViewWithTag(contact.phoneNumber);
                            CircularImageView circularImageView = (CircularImageView) contactView.findViewById(R.id.profilePic);
                            circularImageView.setBorderColor(getResources().getColor(R.color.colorDark));
                            scheduleActvity.contactList.get(i1).isMember=result;
                            scheduleActvity.useSMS=true;
                        }
                    }

                }
            });
        }

    }

    public class YelpBusiness extends SugarRecord {
        public String id;
        public String name;
        public String category;
        public String imageUrl;
        public String snippetText;

        public YelpBusiness(String snippetText, String id, String name, String category, String imageUrl) {
            this.snippetText = snippetText;
            this.id = id;
            this.name = name;
            this.category = category;
            this.imageUrl = imageUrl;
        }
    }
    public void fillYelpPlace_v2(CalendarEvent calendarEvent){

        TextView placeNameView = (TextView) view.findViewById(R.id.placeName);
        placeNameView.setText(calendarEvent.businessName);

        TextView placesAddressView = (TextView) view.findViewById(R.id.placeAddress);

        placesAddressView.setText(calendarEvent.address);

        TextView placeSnippetView = (TextView) view.findViewById(R.id.placeSnippet);
        placeSnippetView.setText(calendarEvent.snippetText);

        ImageView placesPic = (ImageView) view.findViewById(R.id.placePic);
        new LoadImage(placesPic).execute(calendarEvent.imgUrl);
        setupPlaceMap(calendarEvent.lat, calendarEvent.lng, mMap);
    }
    public void fillEventbrite(EventbriteEvent event){

        TextView placeNameView = (TextView) view.findViewById(R.id.placeName);
        placeNameView.setText(event.name.text);

        TextView placesAddressView = (TextView) view.findViewById(R.id.placeAddress);

        placesAddressView.setText(event.venue.name+" at "+event.venue.address.address_1);

        TextView placeSnippetView = (TextView) view.findViewById(R.id.placeSnippet);
        placeSnippetView.setText(event.description.text);

        ImageView placesPic = (ImageView) view.findViewById(R.id.placePic);
        new LoadImage(placesPic).execute(event.logo.url);

        setupPlaceMap(Double.parseDouble(event.venue.address.latitude), Double.parseDouble(event.venue.address.longitude), mMap);

    }
    public void fillYelpPlace(String id){

        scheduleActvity.places.searchBusiness(getActivity(), id);
        scheduleActvity.places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {

            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {
                YelpBusiness yelpBusiness=new YelpBusiness(
                    business.snippetText(),
                        business.id(),
                        business.name(),
                        business.categories().get(0).name(),
                        business.imageUrl()
                );
                scheduleActvity.calendarEvent.yelpPlaceId=business.id();
                scheduleActvity.calendarEvent.businessName =business.name();
                scheduleActvity.calendarEvent.category =business.categories().get(0).name();
                scheduleActvity.calendarEvent.imgUrl =business.imageUrl();
                scheduleActvity.calendarEvent.snippetText =business.snippetText();
                scheduleActvity.calendarEvent.save();

                TextView placeNameView = (TextView) view.findViewById(R.id.placeName);
                placeNameView.setText(business.name());

                TextView placesAddressView = (TextView) view.findViewById(R.id.placeAddress);
                placeAddress=business.location().address().get(0);
                placesAddressView.setText(placeAddress);                
                
                TextView placeSnippetView = (TextView) view.findViewById(R.id.placeSnippet);
                placesAddressView.setText(business.snippetText());

                ImageView placesPic = (ImageView) view.findViewById(R.id.placePic);
                Log.i(MainActivity.TAG, "Get business pic: " + business.imageUrl());
                new LoadImage(placesPic).execute(business.imageUrl());
                setupPlaceMap(business.location().coordinate().latitude(), business.location().coordinate().longitude(), mMap);

            }

        });
    }
    void setupPlaceMap(final Double targetLat, final Double targetLng, final GoogleMap map){
        if (scheduleActvity.mCurrentLocation == null) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupPlaceMap(targetLat, targetLng, map);
                                }
                            });


                        }
                    },
                    1000
            );
            return;
        }

        LatLng loc = new LatLng(targetLat, targetLng);
        locMarker = map.addMarker(new MarkerOptions()
                .position(loc));
        LatLng currentLocation = new LatLng(scheduleActvity.mCurrentLocation.getLatitude(), scheduleActvity.mCurrentLocation.getLongitude());
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(loc).include(currentLocation);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);

        map.moveCamera(cameraUpdate);

        Utils.OnGetDirections onGetDirections = new Utils.OnGetDirections() {
            @Override
            public void onGetDirections(Direction direction, String type) {
                Route route = direction.getRouteList().get(0);
                Leg leg = route.getLegList().get(0);

                DirectionObject directionObj = new DirectionObject(type,leg.getDuration().getText() );

                mDirectionsAdapter.addItem(mDirectionsAdapter.mValues.size(), directionObj);

            }
        };

        Utils.getDirections(null, loc, TransportMode.TRANSIT, getContext(), onGetDirections);
        Utils.getDirections(null, loc, TransportMode.DRIVING, getContext(), onGetDirections);
        Utils.getDirections(null, loc, TransportMode.WALKING, getContext(), onGetDirections);
        Utils.getDirections(null, loc, TransportMode.BICYCLING, getContext(), onGetDirections);
    }
    // TODO: Rename method, update argument and hook method into UI calendarEvent
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                List<String> phones= new ArrayList<>();
                String normalizedPhone = Utils.normalizeNumber(data.getStringExtra("contactPhoneNumber"));
                phones.add(normalizedPhone);
                scheduleActvity.calendarEvent.addPhone(normalizedPhone);
                fillContact_v2(phones);
            }
        }
        else if(requestCode == PICK_PLACE_REQUEST) {
            if (resultCode == RESULT_OK) {
                fillYelpPlace(data.getStringExtra("placeId"));
            }
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try{
            SupportMapFragment f = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.placesMap);
            if (f != null){
                getChildFragmentManager().beginTransaction().remove(f).commit();
                mMap=null;
            }

        }catch(Exception e){
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(scheduleActvity.mCurrentLocation !=null){
            LatLng loc = new LatLng(scheduleActvity.mCurrentLocation.getLatitude(), scheduleActvity.mCurrentLocation.getLongitude());
            locMarker = mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,16));


        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

