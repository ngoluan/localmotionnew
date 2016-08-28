package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventsFragment extends BaseFragment<EventbriteEvent>{

    Activity activity;
    EventsRecyclerViewAdapter recyclerViewAdapter;
    ExpandableLinearLayout expandableLayout;

    String term="";
    String category_filter="";

    ArrayList<EventBriteCategory> eventBriteCategories = new ArrayList<>();
    public EventsFragment() {
        models=new ArrayList<EventbriteEvent>();
    }

    public static EventsFragment newInstance(String type) {
        return new EventsFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_event_list, container, false);
        activity = getActivity();
        recyclerViewAdapter = new EventsRecyclerViewAdapter(getContext(), mListListener);

        listColumns=1;
        createRecyclerViews(R.id.events_list);
        setRecyclerViewAdapter(recyclerViewAdapter);

        recyclerViewAdapter.setClickListener(this);
        expandableLayout= (ExpandableLinearLayout) view.findViewById(R.id.eventExpandableLayout);

        final SearchView search = (SearchView) view.findViewById(R.id.eventSearchView);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                expandableLayout.expand();
            }
        });
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!search.getQuery().toString().equals("")){
                    term=search.getQuery().toString();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        View searchButton = (View) view.findViewById(R.id.eventSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.collapse();
                Spinner categorySpinner = (Spinner) view.findViewById(R.id.eventCategorySpinner);


                if(!categorySpinner.getSelectedItem().toString().equals("All")){
                    int pos=categorySpinner.getSelectedItemPosition();
                    category_filter = eventBriteCategories.get(pos).id;
                }
                if(!search.getQuery().toString().equals("")){
                    term=search.getQuery().toString();
                }
                getEventbrite();
            }
        });
        final ImageView expandMapButton = (ImageView) view.findViewById(R.id.eventsExpandMapButton);
        expandMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandableLinearLayout expandableMapLayout= (ExpandableLinearLayout) view.findViewById(R.id.eventMapExpandableLayout);
                expandableMapLayout.toggle();
                if(expandableMapLayout.isExpanded()==true){
                    Picasso.with(getContext()).load(R.drawable.downicon)
                            .into(expandMapButton);
                }
                else{
                    Picasso.with(getContext()).load(R.drawable.upicon)
                            .into(expandMapButton);
                }
            }
        });
        return view;
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
        data.put("expand","venue");
        data.put("expand","venue");
        if(!category_filter.equals("")) data.put("categories",category_filter);
        if(!term.equals("")) data.put("q",term);

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
    void getEventBriteCategories(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Call<EventbriteCategories> eventbriteServiceCategories= eventbriteService.getCategories();

        eventbriteServiceCategories.enqueue(new Callback<EventbriteCategories>(){

            @Override
            public void onResponse(Call<EventbriteCategories> call, Response<EventbriteCategories> response) {
                eventBriteCategories.addAll(response.body().getCategories());
                fillCategories();
            }

            @Override
            public void onFailure(Call<EventbriteCategories> call, Throwable t) {
                Log.d(MainActivity.TAG, "Luan-onFailure: "+t.toString());
            }
        });
    }
    public void fillCategories(){
        Spinner categorySpinner = (Spinner) view.findViewById(R.id.eventCategorySpinner);
        List<String> list = new ArrayList<String>();
        for (EventBriteCategory category: eventBriteCategories
                ) {
            list.add(category.shortName);


        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dataAdapter);
    }

    
    @Override
    public void fragmentBecameVisible() {
        getEventbrite();getEventBriteCategories();
    }

    @Override
    public void fragmentBecameInvisible() {

    }



    @Override
    public void OnClick(EventbriteEvent item, View view, int position) {
        Intent scheduleIntent = new Intent(getContext(), ScheduleActvity.class);
        scheduleIntent.putExtra(EventBrite.ID_TAG, item.getId());
        startActivity(scheduleIntent);
    }

    @Override
    public void OnLongClick(EventbriteEvent item, View view, int position) {
    }
    abstract class EndlessOnScrollListener extends RecyclerView.OnScrollListener {

        public String TAG = EndlessOnScrollListener.class.getSimpleName();

        // use your LayoutManager instead
        private LinearLayoutManager lm;

        public EndlessOnScrollListener(GridLayoutManager sglm) {
            this.lm = sglm;
        }
        public void onScrollStateChanged(RecyclerView recyclerView, int newState){}
        public abstract void onScrolledToEnd();


    }
}
