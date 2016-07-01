package luan.localmotion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import luan.localmotion.Content.ContactItem;

public class ChooseContact extends AppCompatActivity implements ContactFragment2.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contact);
    }

    @Override
    public void onContactFragmentInteraction(ContactItem item) {

    }
}
