package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class ActivityPickContact extends AppCompatActivity implements BaseListener<ContactItem> {
    Toolbar myToolbar;
    ContactFragment contactFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
        contactFragment = (ContactFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contactActivityFragment);
        contactFragment.fragmentBecameVisible();
        contactFragment.recyclerViewAdapter.setClickListener(this);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setTitle("Contacts");
    }


    @Override
    public void OnClick(ContactItem item, View view, int position) {
        Intent intent = new Intent();
        intent.putExtra("id",item.id  );
        intent.putExtra("name",item.name);
        intent.putExtra(ContactItem.UNIQUE_ID,item.phoneNumber );
        intent.putExtra("profilePic",item.profilePic);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void OnLongClick(ContactItem item, View view, int position) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

       if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
