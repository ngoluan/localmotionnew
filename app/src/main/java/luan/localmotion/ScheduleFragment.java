package luan.localmotion;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import android.widget.Toast;

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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import luan.localmotion.Content.ContactItem;


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

    String placeName="";
    String placeAddress="";

    MaterialCalendarView calendarView;
    ContactItem contact=null;

    ListView listView;
    CustomAdapter mAdapter;
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
        if(mCurrentLocation==null){

            SharedPreferences prefs =getActivity().getSharedPreferences(
                    "luan.localmotion", Context.MODE_PRIVATE);
            mCurrentLocation=new Location(prefs.getString("lastProvider",""));
            mCurrentLocation.setLongitude(Double.valueOf(prefs.getString("lastLng","")));
            mCurrentLocation.setLatitude(Double.valueOf(prefs.getString("lastLat","")));
        }
        //mScrollView = (ScrollView) view.findViewById(R.id.scrollView);

        if (mMap == null) {
            CustomMapView mapFragment = (CustomMapView)  getChildFragmentManager().findFragmentById(R.id.map);
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
        View placePicView= view.findViewById(R.id.placePic);
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
        rejectFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ContentValues event = new ContentValues();
                event.put(CalendarContract.Events.CALENDAR_ID, 1);

                event.put(CalendarContract.Events.TITLE, "Hangout with" + contact.name );
                //event.put(CalendarContract.Events.DESCRIPTION, description);

                event.put(CalendarContract.Events.EVENT_LOCATION, placeName);

                event.put(CalendarContract.Events.DTSTART, String.valueOf(getTimestamp("")));
                event.put(CalendarContract.Events.DTEND, String.valueOf(getTimestamp("")));
                event.put(CalendarContract.Events.ALL_DAY, 0);   // 0 for false, 1 for true
                event.put(CalendarContract.Events.HAS_ALARM, 1); // 0 for false, 1 for true

                String timeZone = TimeZone.getDefault().getID();
                event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

                Uri baseUri;
                if (Build.VERSION.SDK_INT >= 8) {
                    baseUri = Uri.parse("content://com.android.calendar/events");
                } else {
                    baseUri = Uri.parse("content://calendar/events");
                }

                getActivity().getContentResolver().insert(baseUri, event);
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

        return  view;
    }
    public void getSelectedDateTime(){
        CalendarDay date=  calendarView.getSelectedDate();
        String dateStr = date.getYear()+"-"+date.getMonth()+"-"+date.getDay();
        if(listView.getCheckedItemCount()>0){
            SparseBooleanArray items = listView.getCheckedItemPositions();
            dateStr += " " + items.get(0);
        }
        else{
            dateStr += " 00:00";
        }
        Toast.makeText(getContext(), dateStr, Toast.LENGTH_SHORT).show();
    }
    public void setupListview(){
        listView = (ListView)view.findViewById(R.id.timeListView);
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

        mAdapter = new CustomAdapter(getContext(),R.layout.custom_textview, list);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String string = (String) parent.getAdapter().getItem(position);
                Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();

            }
        });
        setListViewHeightBasedOnChildren(listView);
        listView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                // Capture ListView item click
                listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

                    @Override
                    public void onItemCheckedStateChanged(ActionMode mode,
                                                          int position, long id, boolean checked) {

                        // Prints the count of selected Items in title
                        mode.setTitle(listView.getCheckedItemCount() + " Selected");

                        // Toggle the state of item after every click on it
                        mAdapter.toggleSelection(position);
                    }

                    /**
                     * Called to report a user click on an action button.
                     * @return true if this callback handled the event,
                     *          false if the standard MenuItem invocation should continue.
                     */
                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                       /* if (item.getItemId() == R.id.delete){
                            SparseBooleanArray selected = mAdapter.getSelectedIds();
                            short size = (short)selected.size();
                            for (byte I = 0; I<size; I++){
                                if (selected.valueAt(I)) {
                                    String selectedItem = mAdapter.getItem(selected.keyAt(I));
                                    mAdapter.remove(selectedItem);
                                }
                            }

                            // Close CAB (Contextual Action Bar)
                            mode.finish();
                            return true;
                        }
                        */
                        getSelectedDateTime();
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
                        //  mAdapter.removeSelection();
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
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (250 * scale + 0.5f);
        params.height = pixels;
        listView.setLayoutParams(params);
    }
    Timestamp getTimestamp(String time){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = dateFormat.parse("");
            Timestamp startTime = new java.sql.Timestamp(parsedDate.getTime());
            return startTime;
        }catch(Exception e){
        }
        return null;
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
         contact = scheduleActvity2.contacts.getContactItem(getActivity(),phoneNumber);

        TextView nameView = (TextView) view.findViewById(R.id.nameView);
        nameView.setText(contact.name);

        TextView phoneNumberView = (TextView) view.findViewById(R.id.phoneNumberView);
        phoneNumberView.setText(contact.phoneNumber);

        ImageView img = (ImageView) view.findViewById(R.id.profilePicView);
        if(contact.profilePic!=null){
            img.setImageBitmap(contact.profilePic);
        }
    }
    public void fillYelpPlace(String id){
        Log.i(MainActivity.TAG, "Got business " + id);
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
        Fragment f = getChildFragmentManager().findFragmentById(R.id.map);
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