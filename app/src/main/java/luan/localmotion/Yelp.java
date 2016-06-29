package luan.localmotion;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import luan.localmotion.Content.PlacesItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by luann on 2016-06-03.
 */
public class Yelp {
    YelpAPI yelpAPI;
    private Activity caller;
    private YelpListener listener = null;


    public Yelp(Activity caller){
        YelpAPIFactory apiFactory = new YelpAPIFactory("X320v-hKa-_-hkzoQFlo4Q", "SzhtYQTfDQRbSHW70p_ou419Ylw", "MrAZgZ-GhsjOHNs4N6psi5VSknn-SoRz", "SxLw_leUV8yHUTceulflVtMjNog");
        yelpAPI = apiFactory.createAPI();
        this.caller = caller;

    }
    public void searchNearby(Double lat, Double lng,  Map<String, String> params, final View view ){

        /*Map<String, String> params = new HashMap<>();*/

// general params
        /*params.put("term", "food");
        params.put("limit", "3");
        params.put("category","restaurants");*/

// locale params
        /*params.put("lang", "fr");*/

        // coordinates
        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(lat)
                .longitude(lng).build();

        Call<SearchResponse> call = yelpAPI.search(coordinate, params);


        /*Callback<SearchResponse> callback = ;*/


        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();

                int totalNumberOfResult = searchResponse.total();  // 3

                ArrayList<Business> businesses = searchResponse.businesses();

                if (listener != null)
                    listener.OnGetSearch(businesses, view);


            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                // HTTP error happened, do something to handle it.
            }
        });

/*        try {
            Response<SearchResponse> response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void setYelpListener (YelpListener listener) {
        this.listener = listener;
    }

    public void fillPlacesFragment(Location loc, Map<String, String> params, final MainActivity mainActivity){


        searchNearby(loc.getLatitude(),loc.getLongitude(), params,null);
        setYelpListener(new YelpListener() {
            @Override
            public void OnGetSearch(ArrayList<Business> businesses, View view) {
                PlacesFragment placesFragment = (PlacesFragment) mainActivity.mSectionsPagerAdapter.getActiveFragment(mainActivity.mViewPager, 2);
                Log.i(MainActivity.TAG, "yelp start" + businesses.size());

                for (Business business :
                        businesses) {
                    placesFragment.places.add(new PlacesItem(business.id(),business.name(), business.categories().get(0).name(),business.imageUrl()));
                }
                placesFragment.recycleViewAdapter.notifyDataSetChanged();


            }
        });


    }

    public interface YelpListener {

        public void OnGetSearch( ArrayList<Business> businesses, View view);
    }
    interface CallbackExt extends Callback{


    }
}
