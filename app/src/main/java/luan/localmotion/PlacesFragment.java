package luan.localmotion;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
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
public class PlacesFragment extends Fragment implements OnMapReadyCallback,YourFragmentInterface {

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
    GridLayoutManager mLayoutManager;

    String category_filter="";
    String term="";

    CustomMapView mapFragment;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_places_list, container, false);

        markers=new ArrayList<Marker>();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);

        mapFragment = (CustomMapView) getChildFragmentManager().findFragmentById(R.id.placesMap);

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



            recycleViewAdapter = new PlacesRecyclerViewAdapter(placesItems, mListener, getActivity());
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recycleViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);


            recyclerView.addOnScrollListener(new EndlessOnScrollListener(mLayoutManager) {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (!recyclerView.canScrollVertically(1)) {
                        onScrolledToEnd();
                    }

                }


                public void onScrollStateChanged(RecyclerView recyclerView, int newState){
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        Log.i("a", "scrolling stopped...");
                        mapShownBusinesses(placesItems);
                    }
                }
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
        expandableLayout= (ExpandableLinearLayout) view.findViewById(R.id.expandableLayout);

        final SearchView search = (SearchView) view.findViewById(R.id.searchView);
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
                placesItems.clear();
                fillPlacesFragment(null);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        View searchButton = (View) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout.collapse();
                Spinner categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);

                Map<String, String> params = new HashMap<>();
                params.put("limit", String.valueOf(numberofItems));
                params.put("offset", String.valueOf(offset));
                if(!categorySpinner.getSelectedItem().toString().equals("All")){
                    int pos=categorySpinner.getSelectedItemPosition();
                    category_filter = yelpCategoryItems.get(pos).alias;
                }
                if(!search.getQuery().toString().equals("")){
                    term=search.getQuery().toString();
                }
                placesItems.clear();
                fillPlacesFragment(null);
            }
        });
        final ImageView expandMapButton = (ImageView) view.findViewById(R.id.expandMapButton);
        expandMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpandableLinearLayout expandableMapLayout= (ExpandableLinearLayout) view.findViewById(R.id.expandableLayoutMap);
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

    public void getPlaces() {

        LatLng loc=null;
        if(mCurrentLocation==null){
            mCurrentLocation= Utils.getLocationFromHistory(getContext());
            loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        }

        Log.d(MainActivity.TAG,"Place location"+mCurrentLocation.toString());
        if(places==null){
            places = new Places(getActivity());
        }
        fillPlacesFragment(loc);
    }

    public void fillPlacesFragment(LatLng loc) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(numberofItems));
        params.put("offset", String.valueOf(offset));
        params.put("category_filter", category_filter);
        params.put("term", term);

        Log.d(MainActivity.TAG, "Luan-fillPlacesFragment: "+params.toString());
        if(loc==null){
            mCurrentLocation= Utils.getLocationFromHistory(getContext());
            loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }

        places.setYelpListener(new Places.YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {
                int i=0;
                for (Business business :
                        businesses) {
                    Log.d(MainActivity.TAG, "Luan-OnGetSearch: "+(i%4));
                    placesItems.add(new PlacesItem(
                            business.id(),
                            business.name(),
                            business.categories().get(0).name(),
                            business.imageUrl(),
                            business.location().coordinate().latitude(),
                            business.location().coordinate().longitude()));
/*                    LatLng placeLoc = new LatLng(business.location().coordinate().latitude(),business.location().coordinate().longitude());

                    markers.add(mMap.addMarker(new MarkerOptions()
                            .title(business.categories().get(0).name())
                            .position(placeLoc)));*/
                    i++;
                }

                recycleViewAdapter.notifyDataSetChanged();



                mapShownBusinesses(placesItems);


            }

            @Override
            public void OnGetBusiness(Activity caller, Business business) {

            }
        });
        places.searchNearby(loc.latitude, loc.longitude, params, null);


    }
    public void mapShownBusinesses(ArrayList<PlacesItem> places){
        int first = mLayoutManager.findFirstVisibleItemPosition();
        int last = mLayoutManager.findLastVisibleItemPosition();
        if(mMap!=null)
            mMap.clear();

        markers.clear();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        builder.include(loc);

        IconGenerator iconFactory = new IconGenerator(getContext());
        for (int i = first; i < last; i++) {


            PlacesItem place = places.get(i);
            LatLng placeLoc = new LatLng(place.lat,place.lng);

            MarkerOptions markerOptions = new MarkerOptions().
                    icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(String.valueOf(i)))).
                    position(placeLoc).
                    anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

            markers.add(mMap.addMarker(markerOptions));
            /*markers.add(mMap.addMarker(new MarkerOptions()
                    .title(place.name)
                    .position(placeLoc)))*/;
            builder.include(placeLoc);

        }
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 100);
        //mMap.moveCamera(cameraUpdate);
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
        if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}

    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        /*if (isVisibleToUser) {
            if (mMap == null) {

                mapFragment.getMapAsync(this);
                mapFragment.setListener(new CustomMapView.OnTouchListener() {
                    @Override
                    public void onTouch() {
                    }
                });
            }
            getPlaces();
            getYelpCategories();
        }
        else
            Log.d("MyFragment", "Fragment is not visible.");*/
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
        if(mCurrentLocation==null){
            mCurrentLocation= Utils.getLocationFromHistory(getContext());
        }
        LatLng loc = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //cameraPosition.target.latitude
                placesItems.clear();
                fillPlacesFragment(cameraPosition.target);
            }
        });
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
                yelpCategoryItems.add(new YelpCategoryItem("All",new JSONArray(),"All"));
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

    @Override
    public void fragmentBecameVisible() {
        if (mMap == null) {

            mapFragment.getMapAsync(this);
            mapFragment.setListener(new CustomMapView.OnTouchListener() {
                @Override
                public void onTouch() {
                }
            });
        }
        getPlaces();
        getYelpCategories();
    }

    @Override
    public void fragmentBecameInvisible() {
        try{
            Fragment f = getChildFragmentManager().findFragmentById(R.id.placesMap);
            if (f != null) {
                getFragmentManager().beginTransaction().remove(f).commit();

                mMap = null;
            }
        }
        catch (RuntimeException e){

        }


        if(locationReceiver!=null){getActivity().unregisterReceiver(locationReceiver); locationReceiver=null;}
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
    public void onScrollStateChanged(RecyclerView recyclerView, int newState){}
    public abstract void onScrolledToEnd();


}

