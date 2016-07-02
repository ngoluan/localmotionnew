package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class PickContact extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
    }


    @Override
    public void onContactFragmentInteraction(String TAG, ContactItem item) {
        Intent intent = new Intent();

        intent.putExtra("id",item.id  );
        intent.putExtra("name",item.name);
        intent.putExtra("phoneNumber",item.phoneNumber );
        intent.putExtra("profilePic",item.profilePic);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void OnPlacesFragmentListener(String TAG, PlacesItem item) {

    }

    @Override
    public void OnPlacesStart() {

    }
}
