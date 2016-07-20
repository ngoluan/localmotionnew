package luan.localmotion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.orm.SugarContext;

public class ScheduleActvity2 extends AppCompatActivity implements ScheduleFragment.OnFragmentInteractionListener, ChatFragment.OnListFragmentInteractionListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    Contacts contacts;
    Places places;
    Bundle extras=null;
    public long eventId;
    public Event event=null;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_actvity);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        Intent intent = getIntent();

        contacts = new Contacts(this);
        places = new Places(this);
        extras = intent.getExtras();
        SugarContext.init(this);

        Configuration.Builder config = new Configuration.Builder(this);
        config.addModelClasses(Category.class, Item.class, Event.class, Message.class);
        ActiveAndroid.initialize(config.create());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_schedule_actvity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    @Override
    public void onListFragmentInteraction(Message item) {

    }

    @Override
    public void OnChatFragmentListener(String TAG, Message item) {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        Log.d(MainActivity.TAG, "Luan-onNewIntent: "+extras.toString());
    }


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

