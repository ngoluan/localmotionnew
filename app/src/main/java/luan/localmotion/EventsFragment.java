package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import luan.localmotion.Content.PlacesItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EventsFragment extends BaseFragment<EventbriteEvent> implements OnMapReadyCallback {

    Activity activity;
    EventsRecyclerViewAdapter recyclerViewAdapter;
    ExpandableLinearLayout expandableLayout;

    public GoogleMap mMap;
    ArrayList<Marker> markers;

    public ArrayList<EventBriteCategory> eventBriteCategories = new ArrayList<>();

    public EventsFragment() {
        //models = new ArrayList<EventbriteEvent>();
        markers = new ArrayList<>();
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

        listColumns = 1;
        createRecyclerViews(R.id.events_list);
        setRecyclerViewAdapter(recyclerViewAdapter);

        recyclerView.addItemDecoration(new ItemDivider(getContext(),LinearLayoutManager.VERTICAL));
        recyclerViewAdapter.setClickListener(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                addToMap();
            }
        });
/*        expandableLayout = (ExpandableLinearLayout) view.findViewById(R.id.eventExpandableLayout);

        final ImageView expandMapButton = (ImageView) view.findViewById(R.id.eventsExpandMapButton);
        expandMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandableLinearLayout expandableMapLayout = (ExpandableLinearLayout) view.findViewById(R.id.eventMapExpandableLayout);
                expandableMapLayout.toggle();
                if (expandableMapLayout.isExpanded() == true) {
                    Picasso.with(getContext()).load(R.drawable.downicon)
                            .into(expandMapButton);
                } else {
                    Picasso.with(getContext()).load(R.drawable.upicon)
                            .into(expandMapButton);
                }
            }
        });*/

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.eventMap);
        mapFragment.getMapAsync(this);

        return view;
    }
    public void toggleMap(){
        ExpandableLinearLayout expandableMapLayout= (ExpandableLinearLayout) view.findViewById(R.id.expandableLayoutMap);
        expandableMapLayout.toggle();
    }
    public void getEventbrite(String term, String category_filter, String date_start) {
        Location mCurrentLocation = Utils.getLocationFromHistory(getContext());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", EventbriteService.TOKEN);
        data.put("location.latitude", String.valueOf(mCurrentLocation.getLatitude()));
        data.put("location.longitude", String.valueOf(mCurrentLocation.getLongitude()));
        data.put("expand", "venue,category");
        if (!category_filter.equals("")) data.put("categories", category_filter);
        if (!term.equals("")) data.put("q", term);
        if (!date_start.equals("")) data.put("start_date.range_start", date_start);

        Call<EventbriteEvents> eventbriteEvents = eventbriteService.listEvents(data);

        eventbriteEvents.enqueue(new Callback<EventbriteEvents>() {
            @Override
            public void onResponse(Call<EventbriteEvents> call, Response<EventbriteEvents> response) {
                models.clear();
                models.addAll(response.body().getEvents());
                recyclerViewAdapter.animateTo(models);
                addToMap();
            }

            @Override
            public void onFailure(Call<EventbriteEvents> call, Throwable t) {
                Log.d(MainActivity.TAG, "Luan-onFailure: " + t.toString());
            }
        });
    }
    void addToMap(){

        int first = linearLayoutManager.findFirstVisibleItemPosition();
        if(first==-1){

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    addToMap();
                }
            }, 1000);

            return;
        }

        mMap.clear();

        markers.clear();

        Location mCurrentLocation= Utils.getLocationFromHistory(getContext());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        builder.include(loc);

        IconGenerator iconFactory = new IconGenerator(getContext());
        EventbriteEvent event = models.get(first);
        if(event.venue!=null){
            LatLng placeLoc = new LatLng(event.venue.address.latitude,event.venue.address.longitude);

            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon())).
                    position(placeLoc).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

            markers.add(mMap.addMarker(markerOptions));
            builder.include(placeLoc);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 13);
            mMap.animateCamera(cameraUpdate);

        }

    }
    void getEventBriteCategories() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Call<EventbriteCategories> eventbriteServiceCategories = eventbriteService.getCategories();

        eventbriteServiceCategories.enqueue(new Callback<EventbriteCategories>() {

            @Override
            public void onResponse(Call<EventbriteCategories> call, Response<EventbriteCategories> response) {
                eventBriteCategories.addAll(response.body().getCategories());
                //fillCategories();
            }

            @Override
            public void onFailure(Call<EventbriteCategories> call, Throwable t) {
                Log.d(MainActivity.TAG, "Luan-onFailure: " + t.toString());
            }
        });
    }

    public void fillCategories() {
        Spinner categorySpinner = (Spinner) view.findViewById(R.id.eventCategorySpinner);
        List<String> list = new ArrayList<String>();
        for (EventBriteCategory category : eventBriteCategories
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
        getEventbrite("","","");
        getEventBriteCategories();
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

    @Override
    public void onMapReady(GoogleMap mMap) {
        this.mMap=mMap;
        Location mCurrentLocation = Utils.getLocationFromHistory(getContext());
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
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
