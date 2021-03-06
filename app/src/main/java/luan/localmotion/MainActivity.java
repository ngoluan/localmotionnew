package luan.localmotion;

import android.*;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

/*import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;*/
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.iid.FirebaseInstanceId;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class MainActivity extends AppCompatActivity implements
        OnFragmentInteractionListener,
        DashFragment.OnDashFragmentInteractionListener,
        OnContactListListener,
        MapFragment.OnMapInteractionListener {
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    static String TAG = "luan.localmotion";
    static String EVENT_ACCEPT= "EVENT_ACCEPT";
    static String EVENT_CHANGE= "EVENT_CHANGE";
    static String EVENT_REJECT= "EVENT_REJECT";

    Bundle extras=null;

    public Location mCurrentLocation;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout drawer;
    Toolbar myToolbar;
    private ActionBarDrawerToggle mDrawerToggle;

    public Places places;
    public Contacts contacts;

    public SectionsPagerAdapter mSectionsPagerAdapter;


    public ViewPager mViewPager;

    LocationService mService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.main_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(onPageChangeListener);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);





        //setBottomBar();

        updateValuesFromBundle(savedInstanceState);

        contacts = new Contacts(this);
        places = new Places(this);



        processExtras();

        // [END handle_data_extras]
/*        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);*/
        SharedPreferences prefs = this.getSharedPreferences(
                "luan.localmotion", Context.MODE_PRIVATE);
        if (prefs.getString("lastLat", "").equals("")) {
            prefs.edit().putString("lastLat", "43.6532").apply();
            prefs.edit().putString("lastLng", "-79.3832").apply();
            prefs.edit().putString("lastProvider", "provider").apply();
        }
        Dexter.checkPermissions(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                Utils.serverUserCheckIn(FirebaseInstanceId.getInstance().getToken(), getApplicationContext());
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }


        }, android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);



        SessionConfiguration config = new SessionConfiguration.Builder()
                .setClientId("AGHkbAzvvw5i0dxzJw75Tdv2ZA8iN6L0") //This is necessary
                .setRedirectUri("YOUR_REDIRECT_URI") //This is necessary if you'll be using implicit grant
                .setEnvironment(SessionConfiguration.Environment.SANDBOX) //Useful for testing your app in the sandbox environment
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                .build();

//This is a convenience method and will set the default config to be used in other components without passing it directly.
        UberSdk.initialize(config);

        setDrawer();
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    void processExtras(){
        Intent intent = getIntent();
        extras = intent.getExtras();
        String action = intent.getAction();
        if(action!=null){
            if(action.equals(EVENT_ACCEPT)){

            }
            else if(action.equals(EVENT_CHANGE)){
                //TODO is this a common function???
                Intent scheduleIntent = new Intent(this, ScheduleActvity.class);
                if (!extras.get(CalendarEvent.UNIQUE_ID_TAG).equals("")) {
                    scheduleIntent.putExtra(CalendarEvent.UNIQUE_ID_TAG, extras.getString(CalendarEvent.UNIQUE_ID_TAG));
                }
                scheduleIntent.putExtra("placeLat", String.valueOf(mCurrentLocation.getLatitude()));
                scheduleIntent.putExtra("placeLng", String.valueOf(mCurrentLocation.getLongitude()));
                startActivity(scheduleIntent);
            }
            else if(action.equals(EVENT_REJECT)){
                CalendarEvent calendarEvent =CalendarEvent.getByUniqueId(extras.getString(CalendarEvent.UNIQUE_ID_TAG));
                calendarEvent.delete();
            }
        }

    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        int lastPosition;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            FragmentInterface fragmentIn = (FragmentInterface) mSectionsPagerAdapter.instantiateItem(mViewPager, position);
            if (fragmentIn != null) {
                fragmentIn.fragmentBecameVisible();
            }
            FragmentInterface fragmentOut = (FragmentInterface) mSectionsPagerAdapter.instantiateItem(mViewPager, lastPosition);
            if (fragmentOut != null) {
                fragmentOut.fragmentBecameInvisible();
            }
            lastPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {


            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            //updateUI();
        }


    }

    void setDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = (RelativeLayout) findViewById(R.id.drawer);

        ImageView dashButton = (ImageView) drawer.findViewById(R.id.dashButton);
        dashButton.setColorFilter(Color.argb(255, 255, 255, 255));
        dashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openMainActivity= new Intent(getApplicationContext(), MainActivity.class);
                openMainActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(openMainActivity);

            }
        });

        ImageView contactsButton = (ImageView) drawer.findViewById(R.id.contactsButton);
        contactsButton.setColorFilter(Color.argb(255, 255, 255, 255));
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getApplicationContext(), ActivityPickContact.class);
                startActivityForResult(contactIntent, Utils.PICK_CONTACT_REQUEST);
            }
        });
        ImageView placeButton = (ImageView) drawer.findViewById(R.id.placesButton);
        placeButton.setColorFilter(Color.argb(255, 255, 255, 255));
        placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent placeIntent = new Intent(getApplicationContext(), ActivityPickPlace.class);
                startActivityForResult(placeIntent, Utils.PICK_PLACE_REQUEST);
            }
        });

        ImageView eventsButton = (ImageView) drawer.findViewById(R.id.eventsButton);
        eventsButton.setColorFilter(Color.argb(255, 255, 255, 255));
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventIntent = new Intent(getApplicationContext(), ActivityPickEvent.class);
                startActivityForResult(eventIntent, Utils.PICK_EVENT_REQUEST);
            }
        });
        
        ImageView calendarButton = (ImageView) drawer.findViewById(R.id.calendarButton);
        calendarButton.setColorFilter(Color.argb(255, 255, 255, 255));
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent calendarIntent = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(calendarIntent, Utils.PICK_CALENDAR_REQUEST);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                myToolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                String mTitle = "ViaVie";
                getSupportActionBar().setTitle(mTitle);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                String mDrawerTitle = "ViaVie";
                getSupportActionBar().setTitle(mDrawerTitle);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Utils.RESULT_OK) {
            Intent scheduleIntent = new Intent(this, ScheduleActvity.class);

            // Check which request we're responding to
            if (requestCode == Utils.PICK_CONTACT_REQUEST) {
                // Make sure the request was successful

                scheduleIntent.putExtra(ContactItem.UNIQUE_ID, data.getStringExtra(ContactItem.UNIQUE_ID));

            } else if (requestCode == Utils.PICK_PLACE_REQUEST) {
                scheduleIntent.putExtra("yelpPlaceId", data.getStringExtra("yelpPlaceId"));
            } else if (requestCode == Utils.PICK_EVENT_REQUEST) {
                scheduleIntent.putExtra(EventBrite.ID_TAG, data.getStringExtra(EventBrite.ID_TAG));
            }
            scheduleIntent.putExtra("placeLat", String.valueOf(mCurrentLocation.getLatitude()));
            scheduleIntent.putExtra("placeLng", String.valueOf(mCurrentLocation.getLongitude()));
            startActivity(scheduleIntent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, LocationService.class);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_dash, menu);
        return true;
    }

    public boolean onCreateContextMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.action_location) {
            if (mViewPager.getCurrentItem() == 0) {
                DashFragment dashFragment = (DashFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                if(dashFragment!=null){
                    LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                    dashFragment.mMap.animateCamera(cameraUpdate);
                }

            }
        }
        else if (id == R.id.action_transit) {
            if (mViewPager.getCurrentItem() == 0) {
                DashFragment dashFragment = (DashFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                if(dashFragment!=null){
                    dashFragment.dashTransitExpandableLayout.toggle();
                    //dashFragment.dashTransitExpandableLayout.move(1000);
                    //dashFragment.dashTransitExpandableLayout.expand();
                }

            }
        }
        return super.onOptionsItemSelected(item);
    }


    LocationServiceListener locationServiceListener = new LocationServiceListener() {
        @Override
        public void OnConnected(@Nullable Bundle bundle, Location location) {
            if(location==null) return;
            mCurrentLocation = location;
            SharedPreferences prefs = getSharedPreferences(
                    "luan.localmotion", Context.MODE_PRIVATE);
            Log.d(TAG, "Putting location shared preferences" + mCurrentLocation.toString());
            prefs.edit().putString("lastLat", String.valueOf(mCurrentLocation.getLatitude())).apply();
            prefs.edit().putString("lastLng", String.valueOf(mCurrentLocation.getLongitude())).apply();
            prefs.edit().putString("lastProvider", String.valueOf(mCurrentLocation.getProvider())).apply();

            if (mViewPager.getCurrentItem() == 0) {
                DashFragment dashFragment = (DashFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                if(dashFragment!=null){
                    dashFragment.setupDash(mCurrentLocation);
                }



            }


        }

        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;

            SharedPreferences prefs = getSharedPreferences(
                    "luan.localmotion", Context.MODE_PRIVATE);
            prefs.edit().putString("lastLat", String.valueOf(location.getLatitude())).apply();
            prefs.edit().putString("lastLng", String.valueOf(location.getLongitude())).apply();
            prefs.edit().putString("lastProvider", String.valueOf(location.getProvider())).apply();
        }
    };





    public void onSaveInstanceState(Bundle savedInstanceState) {
        try {
            super.onSaveInstanceState(savedInstanceState);
        } catch (IllegalStateException e) {
            Log.d(MainActivity.TAG, "Luan-onSaveInstanceState: " + e.toString());
        }

    }

    @Override
    public void onContactFragmentInteraction(String TAG, ContactItem item) {
        Toast.makeText(MainActivity.this, item.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnPlacesFragmentListener(String TAG, PlacesItem item) {

    }


    @Override
    public void onDashFragmentInteraction(Map<String, String> param) {
        Intent scheduleIntent = new Intent(this, ScheduleActvity.class);
        scheduleIntent.putExtra("type", param.get("type"));
        if (param.get("type").equals("contact")) {
            scheduleIntent.putExtra(ContactItem.UNIQUE_ID, param.get(ContactItem.UNIQUE_ID));
        } else if (param.get("type").equals("places")) {
            scheduleIntent.putExtra("yelpPlaceId", param.get("yelpPlaceId"));
        } else if (param.get("type").equals("events")) {
            scheduleIntent.putExtra(EventBrite.ID_TAG, param.get(EventBrite.ID_TAG));
        }
        scheduleIntent.putExtra("placeLat", String.valueOf(mCurrentLocation.getLatitude()));
        scheduleIntent.putExtra("placeLng", String.valueOf(mCurrentLocation.getLongitude()));
        startActivity(scheduleIntent);
    }

    @Override
    public void onDashFragmentInteraction(Uri uri) {

    }

    /*public void setBottomBar() {
        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

// Create items
        AHBottomNavigationItem item0 = new AHBottomNavigationItem("Dash", R.drawable.dashicon, ContextCompat.getColor(getBaseContext(), R.color.colorPrimary));
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Friends", R.drawable.friendsicon, ContextCompat.getColor(getBaseContext(), R.color.colorSecondary));
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Places", R.drawable.placesicon, ContextCompat.getColor(getBaseContext(), R.color.colorTertiary));
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Events", R.drawable.calendaricon, ContextCompat.getColor(getBaseContext(), R.color.colorDark));
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("More", R.drawable.moreicon, ContextCompat.getColor(getBaseContext(), R.color.colorAccent));

// Add items
        bottomNavigation.addItem(item0);
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);

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
        bottomNavigation.setCurrentItem(0);

// Set listener
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Log.d(MainActivity.TAG, "onTabSelected: " + position);
                if (position == 4) {
                    mDrawerLayout.openDrawer(drawer);
                } else {
                    mViewPager.setCurrentItem(position);
                }
                return true;
            }
        });
    }*/

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnContactClickListener(String TAG, ContactItem item) {

    }


    class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Log.d(MainActivity.TAG, "Luan-getItem: " + position);
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return DashFragment.newInstance("0", "Page # 1");
                case 1:

                    return ContactFragment.newInstance(3);
                case 2:

                    return PlacesFragment.newInstance(places);
                case 3:
                    return EventsFragment.newInstance("EventsFragment");
                default:
                    return DashFragment.newInstance("0", "Page # 1");
            }

        }

        public Fragment getActiveFragment(ViewPager container, int position) {
            //String name = makeFragmentName(container.getId(), position);
            String name = "android:switcher:" + container.getId() + ":" + position;

            return mFragmentManager.findFragmentByTag(name);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
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
                case 3:
                    return "SECTION 4";
            }
            return null;
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    public ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            mService = binder.getService();
            mService.setCustomObjectListener(locationServiceListener);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
