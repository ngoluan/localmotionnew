package luan.localmotion;

import android.app.Activity;
import android.util.Log;

import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import java.util.ArrayList;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by luann on 2016-06-03.
 */
public class Places {
    YelpAPI yelpAPI;
    private Activity caller;
    private YelpListener listener = null;


    public Places(Activity caller){
        YelpAPIFactory apiFactory = new YelpAPIFactory("X320v-hKa-_-hkzoQFlo4Q", "SzhtYQTfDQRbSHW70p_ou419Ylw", "MrAZgZ-GhsjOHNs4N6psi5VSknn-SoRz", "SxLw_leUV8yHUTceulflVtMjNog");
        yelpAPI = apiFactory.createAPI();
        this.caller = caller;

    }
    public void searchBusiness(final Activity caller, String id){
        Call<Business> call = yelpAPI.getBusiness(id);

        call.enqueue(new Callback<Business>() {
            @Override
            public void onResponse(Call<Business> call, Response<Business> response) {
                Business business = response.body();


                    if (listener != null)
                    listener.OnGetBusiness(caller, business);


            }
            @Override
            public void onFailure(Call<Business> call, Throwable t) {
                // HTTP error happened, do something to handle it.
            }
        });
        // Response<Business> response = call.execute();
    }
    public void searchNearby(Double lat, Double lng, Map<String, String> params){

        CoordinateOptions coordinate = CoordinateOptions.builder()
                .latitude(lat)
                .longitude(lng).build();

        Call<SearchResponse> call = yelpAPI.search(coordinate, params);

        Log.d(MainActivity.TAG, "HTTP request yelp");
        /*Callback<SearchResponse> callback = ;*/


        call.enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                SearchResponse searchResponse = response.body();

                int totalNumberOfResult = searchResponse.total();  // 3

                ArrayList<Business> businesses = searchResponse.businesses();
                if (listener != null)
                    listener.OnGetSearch(businesses);


            }
            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e(MainActivity.TAG, "Yelp fail:"+t.getMessage());
            }
        });

/*        try {
            Response<SearchResponse> response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    public void setYelpListener (YelpListener listener) {
        Log.d(MainActivity.TAG, "Setting yelp listener");
        this.listener = listener;
    }



    public interface YelpListener {

        void OnGetSearch(ArrayList<Business> businesses);
        void OnGetBusiness(Activity caller, Business business);
    }
    interface CallbackExt extends Callback{


    }
}
