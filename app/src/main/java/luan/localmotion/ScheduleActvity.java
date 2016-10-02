package luan.localmotion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;


import com.orm.SugarContext;
import com.yelp.clientlib.entities.Business;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import luan.localmotion.Content.ContactItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ScheduleActvity extends AppCompatActivity implements ScheduleFragment.OnFragmentInteractionListener, ChatFragment.OnListFragmentInteractionListener{
    private SectionsPagerAdapter mSectionsPagerAdapter;


    Contacts contacts;
    Places places;

    ArrayList<ContactItem> contactList=new ArrayList<ContactItem>();
    public Boolean useSMS=false;
    Bundle extras=null;
    public long eventId;
    public CalendarEvent calendarEvent =null;
    private DrawerLayout mDrawerLayout;
    private RelativeLayout drawer;
    Toolbar myToolbar;
    //AHBottomNavigation bottomNavigation;


    Menu menu;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.schedule_drawer_layout);
        drawer= (RelativeLayout) findViewById(R.id.schedule_drawer);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.schedule_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(onPageChangeListener);

        myToolbar = (Toolbar) findViewById(R.id.schedule_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Schedule an Activity");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        contacts = new Contacts(this);

        places = new Places(this);

        SugarContext.init(this);

        //setBottomBar();

        processExtras();

    }

    void processExtras(){
        Intent intent = getIntent();
        extras = intent.getExtras();
        if(extras.getString(CalendarEvent.UNIQUE_ID_TAG)!=null){
            calendarEvent = CalendarEvent.getByUniqueId(extras.getString(CalendarEvent.UNIQUE_ID_TAG));
            //calendarEvent = CalendarEvent.findById(CalendarEvent.class, Integer.parseInt(extras.getString(CalendarEvent.UNIQUE_ID_TAG)));
            if(!calendarEvent.contactsPhone.equals("")){
                List<String> phones = calendarEvent.getPhones();
                getContacts(phones);
            }
            if(!calendarEvent.yelpPlaceId.equals("")){
                getYelpPlace(calendarEvent.yelpPlaceId);
            }
            if(!calendarEvent.eventbriteId.equals("")){
                getEventBrite(calendarEvent.eventbriteId);
            }
        }
        else{
            calendarEvent = new CalendarEvent();
            calendarEvent.eventUniqueId =String.valueOf(System.currentTimeMillis())+"-"+Utils.getPhoneNumber(this);

        }
        if(extras.getString(ContactItem.UNIQUE_ID)!=null){
            String normalizedPhone = Utils.normalizeNumber(extras.getString(ContactItem.UNIQUE_ID), getApplicationContext());
            Log.d(MainActivity.TAG, "Luan-processExtras: "+normalizedPhone);
            calendarEvent.addPhone(normalizedPhone);
            getContacts(calendarEvent.getPhones());
        }
        if(extras.getString("yelpPlaceId")!=null){
            getYelpPlace(extras.getString("yelpPlaceId"));
        }
        if(extras.getString(EventBrite.ID_TAG)!=null){
            getEventBrite(extras.getString(EventBrite.ID_TAG));
        }
    }

    void getContacts(List<String> phones){
        for (int i = 0; i < phones.size(); i++) {
            String normalizedPhone = Utils.normalizeNumber(phones.get(i), getApplicationContext());
            ContactItem contactItem=Contacts.getContactItem(getBaseContext(),normalizedPhone);
            if(contactItem!=null)
            contactList.add(contactItem);

        }
    }
    public void getYelpPlace(String id){
        places.searchBusiness(this, id);
        places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses) {

            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {
                calendarEvent.yelpPlaceId=business.id();
                calendarEvent.placeName =business.name();
                calendarEvent.placeCategory =business.categories().get(0).name();
                calendarEvent.placeImgUrl =business.imageUrl();
                calendarEvent.placeAddress =business.location().address().get(0);
                calendarEvent.placeLat =business.location().coordinate().latitude();
                calendarEvent.placeLng =business.location().coordinate().longitude();
                calendarEvent.placeDescription =business.snippetText();

            if (mViewPager.getCurrentItem() == 0) {
                ScheduleFragment scheduleFragment = (ScheduleFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                if(scheduleFragment!=null){
                    scheduleFragment.fillPlaces(
                            calendarEvent.placeName,
                            calendarEvent.placeAddress,
                            calendarEvent.placeDescription,
                            calendarEvent.placeImgUrl,
                            calendarEvent.placeLat,
                            calendarEvent.placeLng
                    );

                    //scheduleFragment.fillYelpPlace_v2(calendarEvent);
                }

            }

            }
        });
    }
    void getEventBrite(String id){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", EventbriteService.TOKEN);
        data.put("expand","venue");

        Call<EventbriteEvent> eventbriteEvents = eventbriteService.getEvent(Long.parseLong(id),data);
        eventbriteEvents.enqueue(new Callback<EventbriteEvent>() {
            @Override
            public void onResponse(Call<EventbriteEvent> call, Response<EventbriteEvent> response) {
                EventbriteEvent eventbriteEvent=response.body();
                calendarEvent.eventbriteId=eventbriteEvent.getId();
                if (mViewPager.getCurrentItem() == 0) {
                    ScheduleFragment scheduleFragment = (ScheduleFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager, 0);
                    if(scheduleFragment!=null){
                        scheduleFragment.fillPlaces(
                                eventbriteEvent.name.text,
                                eventbriteEvent.venue.name+" at "+eventbriteEvent.venue.address.address_1,
                                eventbriteEvent.description.text,
                                eventbriteEvent.logo.url,
                                Double.parseDouble(String.valueOf(eventbriteEvent.venue.address.latitude)),
                                Double.parseDouble(String.valueOf(eventbriteEvent.venue.address.longitude))
                        );
                        //scheduleFragment.fillEventbrite(eventbriteEvent);
                    }

                }
            }

            @Override
            public void onFailure(Call<EventbriteEvent> call, Throwable t) {
                Log.d(MainActivity.TAG, "Luan-onFailure: "+t.toString());
            }
        });
    }
    public void saveEvent(){
        calendarEvent.title="Hang out";
        calendarEvent.save();
    }
    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce=true;

        final ScheduleFragment scheduleFragment = (ScheduleFragment) mSectionsPagerAdapter.getActiveFragment(mViewPager,0);
        final DrawerLayout layout = (DrawerLayout) findViewById(R.id.schedule_drawer_layout);
        Snackbar snackbar = Snackbar
                .make(layout, "Save event?", Snackbar.LENGTH_LONG)
                .setAction("SAVE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        saveEvent();
                        finish();
                    }
                });
        String title = "Hangout";
/*        if(contact!=null){
            title+= " with " +contact.name;
        }*/
        snackbar.show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 5000);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_schedule, menu);
        this.menu = menu;
        menu.findItem(R.id.action_planner).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_planner) {
            mViewPager.setCurrentItem(0);
            menu.findItem(R.id.action_planner).setVisible(false);
            menu.findItem(R.id.action_chat).setVisible(true);

        }
        else if (id == R.id.action_chat) {
            openConversation();
        }
        else if (id == android.R.id.home) {
                    onBackPressed();
                }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }




    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(MainActivity.TAG, "Luan-onNewIntent: "+extras.toString());
    }



    @Override
    public void onListFragmentInteraction(Chat item) {

    }

    @Override
    public void OnChatFragmentListener(String TAG, Chat item) {

    }
    void openConversation(){
        if(useSMS==true){
            String numbers="";
            for (int i = contactList.size() - 1; i >= 0; i--) {
                numbers+=contactList.get(i).phoneNumber+";";
            }
            numbers = numbers.replaceAll("; $", "");
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", numbers, null)));
        }
        else{
            mViewPager.setCurrentItem(1);
            menu.findItem(R.id.action_planner).setVisible(true);
            menu.findItem(R.id.action_chat).setVisible(false);
        }
    }
    void sendProposal(){
        if(useSMS==true){
            String numbers="";
            for (int i = contactList.size() - 1; i >= 0; i--) {
                numbers+=contactList.get(i).phoneNumber+";";
            }
            numbers = numbers.replaceAll("; $", "");
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", numbers, null)));
        }
        else{
            HashMap<String,String> sendData = new HashMap<String, String>();
            sendData.put("type",ChatFragment.TYPE_EVENT);
            sendData.put("sendersPhone",Utils.getPhoneNumber(getApplicationContext()));
            sendData.put("contactsPhone",calendarEvent.contactsPhone);
            sendData.put("dateTime",String.valueOf(Calendar.getInstance().getTimeInMillis()));
            sendData.put("yelpPlaceId",calendarEvent.yelpPlaceId);
            sendData.put("eventbriteId",calendarEvent.eventbriteId);
            sendData.put("placeName",calendarEvent.placeName);
            sendData.put("placeDescription",calendarEvent.placeDescription);
            sendData.put("googlePlaceId",calendarEvent.googlePlaceId);
            sendData.put("placeCategory",calendarEvent.placeCategory);
            sendData.put("placeAddress",calendarEvent.placeAddress);
            sendData.put("placeLat",calendarEvent.placeLat.toString());
            sendData.put("placeLng",calendarEvent.placeLng.toString());
            sendData.put("placeImgUrl",calendarEvent.placeImgUrl);
            sendData.put("title",calendarEvent.title);
            sendData.put("beginTime",calendarEvent.beginTime.toString());
            sendData.put("endTime",calendarEvent.endTime.toString());
            sendData.put("message","");
            sendData.put("eventUniqueId",calendarEvent.eventUniqueId);

            Utils.sendMessage(sendData, getApplicationContext());
        }
        calendarEvent.save();
    }
    public class MessageReceiver extends BroadcastReceiver {
        OnReceiveMessage onReceiveMessage;
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(MainActivity.TAG, "New Message received:" +intent.toString());
            if(onReceiveMessage!=null)
            {
                Chat chat = (Chat) intent.getExtras().get("Message");
                onReceiveMessage.onReceiveMessage(chat);
            }
        }
        public void setListener(OnReceiveMessage listener) {
            onReceiveMessage= listener;
        }
    }
    interface OnReceiveMessage{
        void onReceiveMessage(Chat chat);
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


    /*public void setBottomBar() {
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.schedule_bottom_navigation);

// Create items

        AHBottomNavigationItem item1 = new AHBottomNavigationItem("Contact", R.drawable.friendsicon, ContextCompat.getColor(getBaseContext(),R.color.colorPrimary));
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Calendar", R.drawable.calendaricon, ContextCompat.getColor(getBaseContext(),R.color.colorSecondary));
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Places", R.drawable.placesicon, ContextCompat.getColor(getBaseContext(),R.color.colorTertiary));
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("Conversation", R.drawable.chaticon, ContextCompat.getColor(getBaseContext(),R.color.colorAccent));
        AHBottomNavigationItem item5 = new AHBottomNavigationItem("More", R.drawable.moreicon, ContextCompat.getColor(getBaseContext(),R.color.colorDark));

// Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);
        bottomNavigation.addItem(item4);
        bottomNavigation.addItem(item5);


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

// Customize notification (title, background, typeface)
        bottomNavigation.setNotificationBackgroundColor(Color.parseColor("#F63D2B"));

// Add or remove notification for each item
        bottomNavigation.setNotification("4", 1);
        bottomNavigation.setNotification("", 1);

// Set listener
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Log.d(MainActivity.TAG, "Luan-onTabSelected: "+position);
                switch (position) {
                    case 3: openConversation();
                        break;
                    case 4: mDrawerLayout.openDrawer(drawer);
                        break;
                    default: break;
                }

                return true;
            }
        });
    }*/

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final FragmentManager mFragmentManager;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return ScheduleFragment.newInstance();
                case 1: // Fragment # 0 - This will show FirstFragment
                    return ChatFragment.newInstance();

                default:
                    return ScheduleFragment.newInstance();
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
            return 2;
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


}

