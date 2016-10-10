package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

public class ActivityPickPlace extends AppCompatActivity implements BaseListener<PlacesItem> {
    Toolbar myToolbar;
    PlacesFragment placesFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_places);
         placesFragment = (PlacesFragment) getSupportFragmentManager()
                .findFragmentById(R.id.placesActivityFragment);
        placesFragment.places=new Places(this);
        placesFragment.fragmentBecameVisible();
        placesFragment.recyclerViewAdapter.setClickListener(this);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle("Yelp Places");
    }


    @Override
    public void OnClick(PlacesItem item, View view, int position) {
        Intent intent = new Intent();
        intent.putExtra("yelpPlaceId",item.placeId  );
        intent.putExtra("name",item.name);
        intent.putExtra("type",item.type );
        intent.putExtra("placeImgUrl",item.imgUrl);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void OnLongClick(PlacesItem item, View view, int position) {

    }

    public boolean onCreateOptionsMenu( Menu menu ) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pick, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            LinearLayout filterLayout= (LinearLayout) getLayoutInflater().inflate(R.layout.filter_yelp,null);
            Spinner categorySpinner = (Spinner) filterLayout.findViewById(R.id.yelpCategorySpinner);

            final List<String> categoryList = new ArrayList<String>();
            for (YelpCategoryItem category: placesFragment.yelpCategoryItems) {
                if(category.parents.length()==0)
                    categoryList.add(category.title);
            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
                    android.R.layout.simple_spinner_item, categoryList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(dataAdapter);

            Spinner priceSpinner = (Spinner) filterLayout.findViewById(R.id.yelpPriceSpinner);

            List<String> priceList = new ArrayList<String>();
            for (String price: placesFragment.yelpPriceItems) {
                priceList.add(price);
            }
            ArrayAdapter<String> priceDataAdapter = new ArrayAdapter<String>(getBaseContext(),
                    android.R.layout.simple_spinner_item, priceList);
            priceDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            priceSpinner.setAdapter(priceDataAdapter);
            
            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogPlus dialog, View view) {
                            LinearLayout filterLayout = (LinearLayout) dialog.getHolderView();

                            switch (view.getId()){
                                case R.id.yelpSearchButton:


                                    Spinner categorySpinner = (Spinner) filterLayout.findViewById(R.id.yelpCategorySpinner);
                                    Spinner priceSpinner = (Spinner) filterLayout.findViewById(R.id.yelpPriceSpinner);
                                    Switch openSwitch= (Switch) filterLayout.findViewById(R.id.yelpOpenSwitch);
                                    Switch hotSwitch= (Switch) filterLayout.findViewById(R.id.yelpHotSwitch);
                                    EditText search = (EditText) filterLayout.findViewById(R.id.yelpSearchView);

                                    if(!categorySpinner.getSelectedItem().toString().equals("All")){
                                        int pos=categorySpinner.getSelectedItemPosition();
                                        String alias = "";

                                        for (int i = 0; i < placesFragment.yelpCategoryItems.size(); i++) {
                                            if(placesFragment.yelpCategoryItems.get(i).title.equals(categoryList.get(pos))){
                                                placesFragment.category_filter = placesFragment.yelpCategoryItems.get(i).alias;
                                                break;
                                            }
                                        }

                                    }
                                    else{
                                        placesFragment.category_filter="";
                                    }

                                    if(!priceSpinner.getSelectedItem().toString().equals("Any")){
                                        int pos=priceSpinner.getSelectedItemPosition();
                                        placesFragment.price = placesFragment.yelpPriceItems.get(pos);
                                    }
                                    else{
                                        placesFragment.price="";
                                    }

                                    if(openSwitch.isChecked()==true){
                                        placesFragment.open="true";
                                    }
                                    else{
                                        placesFragment.open="false";
                                    }

                                    if(hotSwitch.isChecked()==true){
                                        placesFragment.hotAndNew="hot_and_new";
                                    }
                                    else{
                                        placesFragment.hotAndNew="";
                                    }

                                    placesFragment.term=search.getText().toString();


                                    Map<String, String> params = new HashMap<>();
                                    params.put("term", placesFragment.term);
                                    params.put("category_filter", placesFragment.category_filter);
                                    params.put("price", placesFragment.price);
                                    params.put("open_now", placesFragment.open);
                                    params.put("attributes", placesFragment.hotAndNew);
                                    params.put("limit", String.valueOf(placesFragment.numberofItems));
                                    params.put("offset", String.valueOf(placesFragment.offset));

                                    placesFragment.models.clear();

                                    placesFragment.fillPlacesFragment(null);
                                    dialog.dismiss();

                            }
                        }


                    })
                    .setContentHolder(new ViewHolder(filterLayout))
                    .setContentHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                    /*.setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)*/
                    .create();

            dialog.show();
        }

        else if (id == R.id.action_map  ) {
            placesFragment.toggleMap();
        }
        else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
