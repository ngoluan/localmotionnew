package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class ActivityPickPlace extends AppCompatActivity implements BaseListener<PlacesItem> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_places);
        PlacesFragment placesFragment = (PlacesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.placesActivityFragment);
        placesFragment.places=new Places(this);
        placesFragment.fragmentBecameVisible();
        placesFragment.recyclerViewAdapter.setClickListener(this);
    }


    @Override
    public void OnClick(PlacesItem item, View view, int position) {
        Intent intent = new Intent();
        intent.putExtra("placeId",item.placeId  );
        intent.putExtra("name",item.name);
        intent.putExtra("type",item.type );
        intent.putExtra("placeImgUrl",item.imgUrl);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void OnLongClick(PlacesItem item, View view, int position) {

    }
}
