package luan.localmotion;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by luann on 2016-07-24.
 */
/*Client secret
        KEY:GJKWMYZIASNQN3NGMQ
        PNR6WPLEZMPM4NLT6CQOKB2GSUTUKGPYPGQWQAFDHLMTWFAJ5H
        Your personal OAuth token
        C2IZSW45UMSH5LK4TWRL
        Anonymous access OAuth token
        XV3WW7DYC2TEOF75WDO6*/
public interface NextbusService {
    @GET("locations/{lat},{lng}/predictions")
    Call<List<NextBusPrediction>> getPredictions(@Path("lat") String lat, @Path("lng") String lng);
    Call<EventbriteCategories> getCategories();
}
class NextBusPredictions {
    List<NextBusPrediction> nextBusPredictions;

    public NextBusPredictions() {
    }

    public List<NextBusPrediction> getNextBusPredictions() {
        return nextBusPredictions;
    }


}
class NextBusPrediction {
    @SerializedName("agency")
    Agency agency;
    @SerializedName("route")
    Route route;
    @SerializedName("stop")
    Stop stop;
    @SerializedName("values")
    List<Value> values;

    public NextBusPrediction() {
    }

    public Agency getAgency() {
        return agency;
    }

    public Route getRoute() {
        return route;
    }

    public Stop getStop() {
        return stop;
    }

    public List<Value> getValues() {
        return values;
    }


    class Agency {
        String id;
        String title;
        String logoUrl;
    }
    class Route {
        String id;
        String title;
    }
    class Stop {
        String id;
        String title;
        String distance;
    }
    class Value {
        String minutes;
        String branch;
        String isDeparture;
        String affectedByLayover;
        String isScheduleBased;
        Direction direction;
        class Direction {
            String id;
            String title;
        }
    }

}