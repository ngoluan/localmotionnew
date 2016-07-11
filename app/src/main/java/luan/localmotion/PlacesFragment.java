package luan.localmotion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.entities.Business;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import luan.localmotion.Content.PlacesItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PlacesFragment extends Fragment implements OnMapReadyCallback {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    private OnFragmentInteractionListener mListener;
    public PlacesRecyclerViewAdapter recycleViewAdapter;
    public ArrayList<PlacesItem> placesItems = new ArrayList<PlacesItem>();
    ExpandableLinearLayout expandableLayout;

    public GoogleMap mMap;
    public Marker locMarker;
    ArrayList<YelpCategoryItem> yelpCategoryItems;
    View view;
    private boolean loading;
    int offset = 0;
    int numberofItems = 9;
    Places places;
    Location mCurrentLocation=null;
    ArrayList<Marker> markers;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlacesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PlacesFragment newInstance(int columnCount) {
        PlacesFragment fragment = new PlacesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));
    }

    BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mCurrentLocation = new Location(intent.getExtras().getString("provider"));
            mCurrentLocation.setLongitude(intent.getExtras().getDouble("lng"));
            mCurrentLocation.setLatitude(intent.getExtras().getDouble("lat"));
        }
    };
//test
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_places_list, container, false);

        if(mCurrentLocation==null){

            SharedPreferences prefs = getActivity().getSharedPreferences(
                    "luan.localmotion", Context.MODE_PRIVATE);
            mCurrentLocation=new Location(prefs.getString("lastProvider",""));
            mCurrentLocation.setLongitude(Double.valueOf(prefs.getString("lastLng","")));
            mCurrentLocation.setLatitude(Double.valueOf(prefs.getString("lastLat","")));
        }
        markers=new ArrayList<Marker>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        final GridLayoutManager mLayoutManager;
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();

            recyclerView.setItemAnimator(new SlideInLeftAnimator());
            if (mColumnCount <= 1) {
                mLayoutManager = new GridLayoutManager(getContext(), mColumnCount);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mLayoutManager = new GridLayoutManager(getContext(), mColumnCount);
                recyclerView.setLayoutManager(mLayoutManager);
            }
            if (mMap == null) {
                CustomMapView mapFragment = (CustomMapView) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
                mapFragment.setListener(new CustomMapView.OnTouchListener() {
                    @Override
                    public void onTouch() {
                    }
                });
            }
            places = new Places(getActivity());

            recycleViewAdapter = new PlacesRecyclerViewAdapter(placesItems, mListener, getActivity());
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recycleViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);


            recyclerView.addOnScrollListener(new EndlessOnScrollListener(mLayoutManager) {

                @Override
                public void onScrolledToEnd() {
                    if (!loading) {
                        loading = true;
                        Log.i(MainActivity.TAG, "Places layout offset:" + String.valueOf(mLayoutManager.getItemCount()));
                        offset = mLayoutManager.getItemCount() + numberofItems;
                        getPlaces();
                    }
                    loading = false;
                }
            });



        }
        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));
        expandableLayout
                = (ExpandableLinearLayout) view.findViewById(R.id.expandableLayout);

        SearchView search = (SearchView) view.findViewById(R.id.searchView);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                expandableLayout.expand();
            }
        });

        View searchButton = (View) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.collapse();

            }
        });


        return view;
    }

    public void getPlaces() {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(numberofItems));
        params.put("offset", String.valueOf(offset));

        Log.d(MainActivity.TAG,"Place location"+mCurrentLocation.toString());
        fillPlacesFragment(mCurrentLocation, params);
    }

    public void fillPlacesFragment(Location loc, Map<String, String> params) {
        places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                builder.include(loc);
                for (Business business :
                        businesses) {
                    placesItems.add(new PlacesItem(business.id(), business.name(), business.categories().get(0).name(), business.imageUrl()));
                    LatLng placeLoc = new LatLng(business.location().coordinate().latitude(),business.location().coordinate().longitude());
                    markers.add(mMap.addMarker(new MarkerOptions()
                            .title(business.categories().get(0).name())
                            .position(placeLoc)));
                    builder.include(placeLoc);
                }

                recycleViewAdapter.notifyDataSetChanged();


                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
                mMap.moveCamera(cameraUpdate);


            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });
        places.searchNearby(loc.getLatitude(), loc.getLongitude(), params, null);


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlacesFragmentListener");
        }
        getActivity().registerReceiver(locationReceiver, new IntentFilter("NEW_LOCATION"));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        getActivity().unregisterReceiver(locationReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try{
            Fragment f = getChildFragmentManager().findFragmentById(R.id.map);
            if (f != null) {
                getFragmentManager().beginTransaction().remove(f).commit();

                mMap = null;
            }
        }
        catch (RuntimeException e){
            
        }


        getActivity().unregisterReceiver(locationReceiver);
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            getPlaces();
            getYelpCategories();
        }
        else
            Log.d("MyFragment", "Fragment is not visible.");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }
    public void getYelpCategories(){
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }
            @Override
            protected String doInBackground(Void... params) {

                String result = postData();

                  return result;
            }
            public String postData()  {

                try{
                    String url =
                            "https://www.yelp.ca/developers/documentation/v2/all_category_list/categories.json";

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = client.newCall(request).execute();
                    return response.body().string();
                }catch(IOException exception){
                    exception.printStackTrace();
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String msg) {
                JSONArray categories;
                JSONObject category;
                ArrayList<JSONObject> categoryArr = null;
                yelpCategoryItems = new ArrayList<YelpCategoryItem>();

                try {
                    categories =  new JSONArray(msg);
                    for (int i = 0; i < categories.length(); i++) {
                        category = categories.getJSONObject(i);
                        yelpCategoryItems.add(new YelpCategoryItem(category.getString("alias"),category.getJSONArray("parents"),category.getString("title")));
                    }
                    fillCategories();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }.execute();
    }
    public void fillCategories(){
        Spinner categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
        List<String> list = new ArrayList<String>();
        for (YelpCategoryItem category: yelpCategoryItems
             ) {
            if(category.parents.length()==0){
                list.add(category.title);
            }


        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(dataAdapter);
    }
}

class YelpCategoryItem {
    public String alias;
    public JSONArray parents;
    public String title;

    public YelpCategoryItem(String alias, JSONArray parents,String title) {
        this.alias = alias;
        this.parents = parents;
        this.title = title;
    }
}
abstract class EndlessOnScrollListener extends RecyclerView.OnScrollListener {

    public static String TAG = EndlessOnScrollListener.class.getSimpleName();

    // use your LayoutManager instead
    private LinearLayoutManager lm;

    public EndlessOnScrollListener(GridLayoutManager sglm) {
        this.lm = sglm;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (!recyclerView.canScrollVertically(1)) {
            onScrolledToEnd();
        }
    }

    public abstract void onScrolledToEnd();
}

