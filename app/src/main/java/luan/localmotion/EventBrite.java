package luan.localmotion;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by luann on 2016-07-26.
 */
public class EventBrite {
    static String ID_TAG="eventbriteId";
    public static void getEvents(Context context, String sort_by, Callback<EventbriteEvents> callback){
        Location mCurrentLocation = Utils.getLocationFromHistory(context);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.eventbriteapi.com/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventbriteService eventbriteService = retrofit.create(EventbriteService.class);
        Map<String, String> data = new HashMap<>();
        data.put("token", EventbriteService.TOKEN);
        data.put("location.latitude", String.valueOf(mCurrentLocation.getLatitude()));
        data.put("location.longitude",String.valueOf(mCurrentLocation.getLongitude()));
        data.put("expand","placeCategory,venue");
        if(!sort_by.equals("")) data.put("sort_by",sort_by);

        Call<EventbriteEvents> eventbriteEvents = eventbriteService.listEvents(data);

        eventbriteEvents.enqueue(callback);
    }
}
