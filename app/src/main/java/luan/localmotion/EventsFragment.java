package luan.localmotion;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventsFragment extends BaseFragment<EventbriteEvent,EventsRecyclerViewAdapter> implements FragmentInterface,BaseListener<EventbriteEvent>{

    MainActivity mainActivity;
    EventsRecyclerViewAdapter recyclerViewAdapter;

    public EventsFragment() {
        models=new ArrayList<EventbriteEvent>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_event_list, container, false);
        Log.d(MainActivity.TAG, "Luan-onCreateView: eventFragment ");
        mainActivity = (MainActivity) getActivity();
        recyclerViewAdapter = new EventsRecyclerViewAdapter(getContext(), mListListener);

        listColumns=4;
        createViews(R.id.events_list);
        setRecyclerViewAdapter(recyclerViewAdapter);

        return view;
    }
    public static EventsFragment newInstance(String type) {
        return new EventsFragment();
    }
    public void getEventbrite(){
        Location mCurrentLocation = Utils.getLocationFromHistory(getContext());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", EventbriteService.TOKEN);
        data.put("location.latitude", String.valueOf(mCurrentLocation.getLatitude()));
        data.put("location.longitude",String.valueOf(mCurrentLocation.getLongitude()));

        Call<EventbriteEvents> eventbriteEvents = eventbriteService.listEvents(data);

        eventbriteEvents.enqueue(new Callback<EventbriteEvents>() {
            @Override
            public void onResponse(Call<EventbriteEvents> call, Response<EventbriteEvents> response) {
                Log.d(MainActivity.TAG, "Luan-onResponse: "+response.body().getEvents());

                models.addAll(response.body().getEvents());
                recyclerViewAdapter.animateTo(models);
            }

            @Override
            public void onFailure(Call<EventbriteEvents> call, Throwable t) {
                Log.d(MainActivity.TAG, "Luan-onFailure: "+t.toString());
            }
        });
        Log.d(MainActivity.TAG, "Luan-onCreate: "+eventbriteEvents.toString());
    }
    
    @Override
    public void onResume() {
        super.onResume();

    }
    @Override
    public void onPause() {
        super.onPause();
    }
    
    @Override
    public void fragmentBecameVisible() {
        getEventbrite();
    }

    @Override
    public void fragmentBecameInvisible() {

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void OnClick(EventbriteEvent item) {
        Intent scheduleIntent = new Intent(getContext(), ScheduleActvity.class);
        scheduleIntent.putExtra("eventUniqueId", item.getId().toString());
        startActivity(scheduleIntent);
    }

    @Override
    public void OnClick(EventbriteEvent item, View view) {

    }

    @Override
    public void OnLongClick(EventbriteEvent item) {
        final FrameLayout layout = (FrameLayout) view.findViewById(R.id.eventLayout);
        Snackbar snackbar = Snackbar
                .make(layout, "Delete event?", Snackbar.LENGTH_LONG)
                .setAction("DELETE", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
/*                        item.delete();
                        List<EventbriteEvent> events = CalendarEvent.listAll(CalendarEvent.class);
                        recyclerViewAdapter.animateTo(events);*/
                    }
                });
        snackbar.show();
    }
}
