package luan.localmotion;

import android.content.Context;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
    public ArrayList<PlacesItem> places = new ArrayList<PlacesItem>();
    ExpandableLinearLayout expandableLayout;

    public GoogleMap mMap;
    public Marker locMarker;
    ArrayList<YelpCategoryItem> yelpCategoryItems;
    View view;
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
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();

            recyclerView.setItemAnimator(new SlideInLeftAnimator());
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mListener.OnPlacesStart();
            recycleViewAdapter=new PlacesRecyclerViewAdapter(places, mListener, getActivity());
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recycleViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /*
        if(MainActivity.mLocation!=null){
            LatLng loc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
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

