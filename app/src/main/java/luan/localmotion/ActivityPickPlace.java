package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class ActivityPickPlace extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_places);
        PlacesFragment placesFragment = (PlacesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.placesActivityFragment);
        placesFragment.places=new Places(this);
        placesFragment.fragmentBecameVisible();
    }

    @Override
    public void onContactFragmentInteraction(String TAG, ContactItem item) {

    }

    @Override
    public void OnPlacesFragmentListener(String TAG, PlacesItem item) {
        Intent intent = new Intent();

        intent.putExtra("placeId",item.placeId  );
        intent.putExtra("name",item.name);
        intent.putExtra("type",item.type );
        intent.putExtra("imgUrl",item.imgUrl);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }




}
