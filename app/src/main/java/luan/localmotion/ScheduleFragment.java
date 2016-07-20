package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
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

import com.activeandroid.query.Select;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.yelp.clientlib.entities.Business;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import luan.localmotion.Content.ContactItem;
import me.everything.providers.android.calendar.CalendarProvider;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;
    Location mCurrentLocation;
    Bundle extras;
    public GoogleMap mMap;
    public Marker locMarker;
    public ScrollView mScrollView;
    ScheduleActvity2 scheduleActvity2;
    private OnFragmentInteractionListener mListener;
    public static final int PICK_CONTACT_REQUEST = 1;
    public static final int PICK_PLACE_REQUEST = 2;
    /** Standard activity result: operation canceled. */
    public static final int RESULT_CANCELED    = 0;
    /** Standard activity result: operation succeeded. */
    public static final int RESULT_OK           = -1;

/*    public long eventId;*/
            String placeName=null;
    String placeAddress="";

    MaterialCalendarView calendarView;
    ContactItem contact=null;

    ListView timeListView;
    RecyclerView directionsRecyclerView;
    TimeListViewAdapter mTimeAdapter;
    DirectionsRecyclerViewAdapter mDirectionsAdapter;
    private DirectionsRecyclerViewAdapter.OnDirectionsListener onDirectionsListener;
    public ScheduleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ScheduleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScheduleFragment newInstance() {
        ScheduleFragment fragment = new ScheduleFragment();
        Bundle args = new Bundle();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_schedule, container, false);
        scheduleActvity2 = (ScheduleActvity2) getActivity();
        this.extras = scheduleActvity2.extras;
        scheduleActvity2.event= new Event();
        scheduleActvity2.event.uniqueId=String.valueOf(System.currentTimeMillis())+"-"+Utils.getPhoneNumber(getContext());
        if(mCurrentLocation==null){

            SharedPreferences prefs =getActivity().getSharedPreferences(
                    "luan.localmotion", Context.MODE_PRIVATE);
            mCurrentLocation=new Location(prefs.getString("lastProvider",""));
            mCurrentLocation.setLongitude(Double.valueOf(prefs.getString("lastLng","")));
            mCurrentLocation.setLatitude(Double.valueOf(prefs.getString("lastLat","")));
        }
        //mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        if (mMap == null) {
            CustomMapView mapFragment = (CustomMapView)  getChildFragmentManager().findFragmentById(R.id.placesMap);
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new CustomMapView.OnTouchListener() {
                @Override
                public void onTouch() {
                    //mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
        Log.d(MainActivity.TAG, "onCreateView: "+extras.toString());

        if(extras.getString("contactPhone")!=null){

            fillContact(extras.getString("contactPhone"));
        }
        if(extras.getString("placeId")!=null){

            fillYelpPlace(extras.getString("placeId"));
        }

        View profilePicView= view.findViewById(R.id.profilePicView);
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
                Intent placesIntent = new Intent(getContext(), PickPlace.class);

                startActivityForResult(placesIntent,PICK_PLACE_REQUEST);
            }
        });

        FloatingActionButton sendFab = (FloatingActionButton) view.findViewById(R.id.send);
        FloatingActionButton doneFab= (FloatingActionButton) view.findViewById(R.id.done);
        FloatingActionButton rejectFab= (FloatingActionButton) view.findViewById(R.id.reject);
        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //final ContentValues event = new ContentValues();
                //event.put(CalendarContract.Events.CALENDAR_ID, 1);
                String title = "Hangout";
                if(contact!=null){
                    title+= " with " +contact.name;
                }
                /*event.put(CalendarContract.Events.TITLE, title);
                //event.put(CalendarContract.Events.DESCRIPTION, description);


                event.put(CalendarContract.Events.ALL_DAY, 0);   // 0 for false, 1 for true
                event.put(CalendarContract.Events.HAS_ALARM, 1); // 0 for false, 1 for true

                String timeZone = TimeZone.getDefault().getID();
                event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

                Uri baseUri;
                if (Build.VERSION.SDK_INT >= 8) {
                    baseUri = Uri.parse("content://com.android.calendar/events");
                } else {
                    baseUri = Uri.parse("content://calendar/events");
                }*/
                //getActivity().getContentResolver().insert(baseUri, event);


                Calendar beginTime = getTimestamp(getSelectedDateTime("first"));
                Calendar endTime = getTimestamp(getSelectedDateTime("last"));

                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, title)
                        .putExtra(CalendarContract.Events.DESCRIPTION, title)
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                if(placeName!=null){
                    intent.putExtra(CalendarContract.Events.EVENT_LOCATION, placeName);
                }

                Log.d(MainActivity.TAG, "event"+intent.getExtras().toString());
                scheduleActvity2.event.beginTime = beginTime;
                scheduleActvity2.event.endTime = endTime;
                scheduleActvity2.event.title=title;
                scheduleActvity2.eventId=scheduleActvity2.event.save();
                Log.d(MainActivity.TAG, "Luan-onClick: "+scheduleActvity2.event.toString());
                startActivity(intent);
            }
        });

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

        setupListview();
        setupDirectionsListview();


        Category restaurants = new Category();
        restaurants.name = "Restaurants";
        restaurants.save();

        Item item = new Item();
        item.category = restaurants;
        item.name = "Outback Steakhouse";
        item.save();
        getAll(restaurants);
        //Log.d(MainActivity.TAG, "Luan-onCreateView: "+getAll(restaurants).get(0));
        getCalender();

        return  view;
    }
    public void getCalender(){
        CalendarProvider calendarProvider = new CalendarProvider(getContext());
        List<me.everything.providers.android.calendar.Calendar> calendars = calendarProvider.getCalendars().getList();
        Log.d(MainActivity.TAG, "Luan-getCalender: "+calendars.toString());
        for (me.everything.providers.android.calendar.Calendar calendar:calendars
                ) {

        }
    }
    public static List<Event> getAllEvents() {
        return new Select()
                .from(Event.class)
                .orderBy("PhoneNumber ASC")
                .execute();
    }
    public static List<Item> getAll(Category category) {
        return new Select()
                .from(Item.class)
                .where("Category = ?", category.getId())
                .orderBy("Name ASC")
                .execute();
    }
    public String getSelectedDateTime(String firstOrLast){
        CalendarDay date=  calendarView.getSelectedDate();
        DateFormat FORMATTER = SimpleDateFormat.getDateInstance();
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
        //Toast.makeText(getContext(), dateStr, Toast.LENGTH_SHORT).show();
    }
    class TimeObject{
        String time;
        Boolean selected;
        TimeObject(String time){
            this.time = time;
            selected=false;
        }
    }
    public class DirectionsObject{
        String type="";
        String ETA="";
        String description="";
        Direction direction=null;
        DirectionsObject(String type, String ETA){
            this.type = type;
            this.ETA = ETA;
        }
    }
    public void setupDirectionsListview(){

        directionsRecyclerView = (RecyclerView) view.findViewById(R.id.schedulerDirectionsRecyclerView);
        //ArrayList<TimeObject> timeList = new ArrayList<TimeObject>();
        ArrayList<DirectionsObject> list = new ArrayList<DirectionsObject>();
        directionsRecyclerView.setItemAnimator(new SlideInLeftAnimator());
        directionsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        onDirectionsListener = new DirectionsRecyclerViewAdapter.OnDirectionsListener() {
            @Override
            public void OnDirectionsClickListener(DirectionsObject item) {

            }
        };
        mDirectionsAdapter = new DirectionsRecyclerViewAdapter(list,onDirectionsListener );

        directionsRecyclerView.setAdapter(mDirectionsAdapter);



    }
    public void setupListview(){
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

        timeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String string = (String) parent.getAdapter().getItem(position);
                //Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();
                mTimeAdapter.toggleSelection(position);
            }
        });
        setListViewHeightBasedOnChildren(timeListView);
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
                     * @return true if this callback handled the event,
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
    Calendar getTimestamp(String time){
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2016, 7,9, 12,0, 0);
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

    public void fillContact(String phoneNumber){
        scheduleActvity2.event.contactsPhone.add(phoneNumber);
        scheduleActvity2.eventId=scheduleActvity2.event.save();
        Log.d(MainActivity.TAG, "Luan-onClick: "+scheduleActvity2.event.toString());
        contact = scheduleActvity2.contacts.getContactItem(getActivity(),phoneNumber);

        TextView nameView = (TextView) view.findViewById(R.id.nameView);
        nameView.setText(contact.name);

        TextView phoneNumberView = (TextView) view.findViewById(R.id.phoneNumberView);
        phoneNumberView.setText(contact.phoneNumber);

        ImageView img = (ImageView) view.findViewById(R.id.profilePicView);
        if(contact.profilePic!=null){
            img.setImageBitmap(contact.profilePic);
        }
/*        List<Event> events = getAllEvents();
        for (Event event:events
             ) {

            Log.d(MainActivity.TAG, "Luan-onCreateView: "+event.contactsPhone);
        }*/
    }
    public void fillYelpPlace(String id){
        scheduleActvity2.event.yelpPlaceId=id;
        scheduleActvity2.eventId=scheduleActvity2.event.save();
        Log.d(MainActivity.TAG, "Luan-onClick: "+scheduleActvity2.event.toString());
        scheduleActvity2.places.searchBusiness(getActivity(), id);
        scheduleActvity2.places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {

            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {
                TextView placeNameView = (TextView) view.findViewById(R.id.placeName);
                placeNameView.setText(placeName);

                TextView placesAddressView = (TextView) view.findViewById(R.id.placeAddress);
                placeAddress=business.location().address().get(0);
                placesAddressView.setText(placeAddress);

                ImageView placesPic = (ImageView) view.findViewById(R.id.placePic);
                Log.i(MainActivity.TAG, "Get business pic: " + business.imageUrl());
                new LoadImage(placesPic).execute(business.imageUrl());
                LatLng loc = new LatLng(business.location().coordinate().latitude(), business.location().coordinate().longitude());
                locMarker = mMap.addMarker(new MarkerOptions()
                        .position(loc));
                LatLng currentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(loc).include(currentLocation);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);

// Set the camera to the greatest possible zoom level that includes the
// bounds
                mMap.moveCamera(cameraUpdate);

                Utils.OnGetDirections onGetDirections = new Utils.OnGetDirections() {
                    @Override
                    public void onGetDirections(Direction direction, String type) {
                        Route route = direction.getRouteList().get(0);
                        Leg leg = route.getLegList().get(0);

                        DirectionsObject directionObj = new DirectionsObject(type,leg.getDuration().getText() );

                        mDirectionsAdapter.addItem(mDirectionsAdapter.mValues.size(), directionObj);

                    }
                };

                Utils.getDirections(null, loc, TransportMode.TRANSIT, getContext(), onGetDirections);
                Utils.getDirections(null, loc, TransportMode.DRIVING, getContext(), onGetDirections);
                Utils.getDirections(null, loc, TransportMode.WALKING, getContext(), onGetDirections);
                Utils.getDirections(null, loc, TransportMode.BICYCLING, getContext(), onGetDirections);

            }
        });
    }
    // TODO: Rename method, update argument and hook method into UI event
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
                fillContact(data.getStringExtra("contactPhoneNumber"));
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
        Fragment f = getChildFragmentManager().findFragmentById(R.id.placesMap);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();

            mMap = null;
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

        if(mCurrentLocation !=null){
            LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
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

