package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityPickEvent extends AppCompatActivity implements BaseListener<EventbriteEvent>{
    Toolbar myToolbar;
    EventsFragment eventsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_events);
        eventsFragment = (EventsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.eventsActivityFragment);
        //placesFragment.events=new Places(this);
        eventsFragment.fragmentBecameVisible();
        eventsFragment.recyclerViewAdapter.setClickListener(this);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        myToolbar.setTitle("Events");
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

            LinearLayout filterLayout= (LinearLayout) getLayoutInflater().inflate(R.layout.filter_eventbrite,null);
            Spinner categorySpinner = (Spinner) filterLayout.findViewById(R.id.eventCategorySpinner);

            List<String> list = new ArrayList<String>();
            for (EventBriteCategory category: eventsFragment.eventBriteCategories
                    ) {
                list.add(category.shortName);

            }
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getBaseContext(),
                    android.R.layout.simple_spinner_item, list);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(dataAdapter);

            final EditText datePicker = (EditText) filterLayout.findViewById(R.id.eventDatePicker);
            datePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Calendar cal = Calendar.getInstance();
                    CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                            .setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                                @Override
                                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                                    datePicker.setText(year+"/"+monthOfYear+"/"+dayOfMonth);
                                    dialog.dismiss();
                                }
                            })
                            .setFirstDayOfWeek(Calendar.SUNDAY)
                            .setPreselectedDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)-1, cal.get(Calendar.DAY_OF_MONTH))
                            .setDoneText("Yay")
                            .setCancelText("Nop");
                    cdp.show(getSupportFragmentManager(), "datePicker");
                }




            });

            final EditText search = (EditText) filterLayout.findViewById(R.id.eventSearchView);


            Button searchButton = (Button) filterLayout.findViewById(R.id.eventsSearchButton);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            DialogPlus dialog = DialogPlus.newDialog(this)
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(DialogPlus dialog, View view) {
                            LinearLayout filterLayout = (LinearLayout) dialog.getHolderView();
                            switch (view.getId()){
                                case R.id.eventsSearchButton:
                                    String category_filter="";
                                    String term="";
                                    String date_start="";

                                    Spinner categorySpinner = (Spinner) filterLayout.findViewById(R.id.eventCategorySpinner);

                                    if(!categorySpinner.getSelectedItem().toString().equals("All")){
                                        int pos=categorySpinner.getSelectedItemPosition();
                                        category_filter = eventsFragment.eventBriteCategories.get(pos).id;
                                    }
                                    if(!datePicker.getText().toString().equals("Any")){
                                        date_start=datePicker.getText().toString();
                                    }
                                    term=search.getText().toString();
                                    eventsFragment.getEventbrite(term, category_filter, date_start);
                            }
                        }


                    })
                    .setContentHolder(new ViewHolder(filterLayout))
                    .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                    .create();

            dialog.show();
        }
        else if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

}
