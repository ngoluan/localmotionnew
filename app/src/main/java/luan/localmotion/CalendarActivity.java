package luan.localmotion;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;

public class CalendarActivity extends AppCompatActivity implements BaseListener<CalendarEvent>{
    RecyclerView recyclerView;
    int listColumns=1;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    CalendarRecyclerViewAdapter recyclerViewAdapter;
    BaseListener<CalendarEvent> mListListener;

    ArrayList<CalendarEvent> models = new ArrayList<CalendarEvent>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        createViews(R.id.event_list);

        setRecyclerViewAdapter(recyclerViewAdapter);

        getEvents();
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", EventbriteService.TOKEN);
        data.put("location.latitude", "Marcus");
        data.put("location.longitude", "Marcus");

        Call<EventbriteEvents> eventbriteEvents = eventbriteService.listEvents(data);
        eventbriteEvents.enqueue(new Callback<EventbriteEvents>() {
            @Override
            public void onResponse(Call<EventbriteEvents> call, Response<EventbriteEvents> response) {
                Log.d(MainActivity.TAG, "Luan-onResponse: "+response.body().getResults());

            }

            @Override
            public void onFailure(Call<EventbriteEvents> call, Throwable t) {

            }
        });*/
        //Log.d(MainActivity.TAG, "Luan-onCreate: "+eventbriteEvents.toString());
    }

    public void createViews(int viewId){

        recyclerView = (RecyclerView) findViewById(viewId);

        if (recyclerView instanceof RecyclerView) {
            Context context = this;
            recyclerView.setItemAnimator(new FadeInRightAnimator());

            if(listColumns==1){
                linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
            }
            else{
                gridLayoutManager = new GridLayoutManager(context,listColumns);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

            recyclerViewAdapter = new CalendarRecyclerViewAdapter(context, mListListener);
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recyclerViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);
        }
    }
    void setRecyclerViewAdapter(CalendarRecyclerViewAdapter recyclerViewAdapter){
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recyclerViewAdapter);
        recyclerView.setAdapter(alphaInAnimationAdapter);
        recyclerViewAdapter.setClickListener(this);
    }
    void  getEvents() {
        List<CalendarEvent> calendarEvents = CalendarEvent.listAll(CalendarEvent.class);
        models.addAll(calendarEvents);

        recyclerViewAdapter.animateTo(models);

    }



    @Override
    public void OnClick(CalendarEvent item, View view, int position) {
        Intent scheduleIntent = new Intent(this, ScheduleActvity.class);
        scheduleIntent.putExtra("eventUniqueId", item.getId().toString());
        startActivity(scheduleIntent);
    }

    @Override
    public void OnLongClick(final CalendarEvent item, View view, final int position) {
        final FrameLayout layout = (FrameLayout) findViewById(R.id.eventLayout);
        Snackbar snackbar = Snackbar
                .make(layout, "Delete calendarEvent?", Snackbar.LENGTH_LONG)
                .setAction("DELETE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item.delete();

                        List<CalendarEvent> calendarEvents = CalendarEvent.listAll(CalendarEvent.class);
                        recyclerViewAdapter.removeItem(position);
                    }
                });
        snackbar.show();
    }
}