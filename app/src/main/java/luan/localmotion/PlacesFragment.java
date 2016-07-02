package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
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
    int numberofItems=18;

    Places places;
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_places_list, container, false);
        RecyclerView recyclerView =(RecyclerView) view.findViewById(R.id.list);
        final GridLayoutManager mLayoutManager;
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();

            recyclerView.setItemAnimator(new SlideInLeftAnimator());
            if (mColumnCount <= 1) {
                mLayoutManager = new GridLayoutManager(getContext(),mColumnCount);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                mLayoutManager = new GridLayoutManager(getContext(),mColumnCount);
                recyclerView.setLayoutManager(mLayoutManager);
            }
            places=new Places(getActivity());
            getPlaces();
            recycleViewAdapter=new PlacesRecyclerViewAdapter(placesItems, mListener, getActivity());
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recycleViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);


            recyclerView.addOnScrollListener(new EndlessOnScrollListener(mLayoutManager) {

                @Override
                public void onScrolledToEnd() {
                    if (!loading) {
                        loading = true;
                        Log.i(MainActivity.TAG, "Places layout offset:"+String.valueOf(mLayoutManager.getItemCount()));
                        offset=mLayoutManager.getItemCount()+numberofItems;
                        getPlaces();
                    }
                    loading = false;
                }
            });
            if (mMap == null) {
                CustomMapView mapFragment = (CustomMapView) getChildFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

            }
        }
        expandableLayout
                = (ExpandableLinearLayout) view.findViewById(R.id.expandableLayout);

        SearchView search = (SearchView) view.findViewById( R.id.searchView);
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

        getYelpCategories();

        return view;
    }
    public void getPlaces(){
        Location mCurrentLocation=null;
        Map<String, String> params = new HashMap<>();
        params.put("limit",String.valueOf(numberofItems));
        params.put("offset",String.valueOf(offset));

        if(getActivity() instanceof MainActivity){
            MainActivity activity = (MainActivity) getActivity();
            mCurrentLocation=activity.mCurrentLocation;
        }
        else if(getActivity() instanceof SchedulerActivity){
            SchedulerActivity activity = (SchedulerActivity) getActivity();
            mCurrentLocation=activity.mCurrentLocation;
        }

        fillPlacesFragment(mCurrentLocation,params);
    }
    public void fillPlacesFragment(Location loc, Map<String, String> params){
        places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {
                for (Business business :
                        businesses) {
                    placesItems.add(new PlacesItem(business.id(),business.name(), business.categories().get(0).name(),business.imageUrl()));
                }

                recycleViewAdapter.notifyDataSetChanged();

            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });
        places.searchNearby(loc.getLatitude(),loc.getLongitude(), params,null);


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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fragment f = getChildFragmentManager().findFragmentById(R.id.map);
        if (f != null){
            getFragmentManager().beginTransaction().remove(f).commit();

            mMap=null;
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*
        if(MainActivity.mCurrentLocation!=null){
            LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            locMarker = mMap.addMarker(new MarkerOptions()
                    .position(loc)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.locationicon)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,16));


        }*/
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

