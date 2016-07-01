package luan.localmotion;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import luan.localmotion.Content.ContactItem;

public class SchedulerActivity2 extends AppCompatActivity implements ContactFragment2.OnListFragmentInteractionListener{
    Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler2);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SchedulerActivity2Fragment fragment = new SchedulerActivity2Fragment();
        fragmentTransaction.add(R.id.container, fragment);
        fragmentTransaction.addToBackStack("schedule");
        fragmentTransaction.commit();

        Intent intent = getIntent();

        String type = intent.getStringExtra("type");
        String id = intent.getStringExtra("id");
        mLocation = new Location("dummy");
        Log.i(MainActivity.TAG,"Location schedule"+intent.getStringExtra("lat"));
        mLocation.setLatitude(Double.valueOf(intent.getStringExtra("lat")));
        mLocation.setLongitude(Double.valueOf(intent.getStringExtra("lng")));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    public void getContacts(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ContactFragment2 fragment = new ContactFragment2();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack("contact");
        fragmentTransaction.commit();
    }

    @Override
    public void onContactFragmentInteraction(ContactItem item) {

    }
}
