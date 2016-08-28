package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class ActivityPickEvent extends AppCompatActivity implements BaseListener<EventbriteEvent>{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_events);
        EventsFragment eventsFragment = (EventsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.eventsActivityFragment);
        //placesFragment.events=new Places(this);
        eventsFragment.fragmentBecameVisible();
        eventsFragment.recyclerViewAdapter.setClickListener(this);
    }


    @Override
    public void OnClick(EventbriteEvent item, View view, int position) {
        Intent intent = new Intent();

        intent.putExtra(EventBrite.ID_TAG,item.getId());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void OnLongClick(EventbriteEvent item, View view, int position) {

    }
}
