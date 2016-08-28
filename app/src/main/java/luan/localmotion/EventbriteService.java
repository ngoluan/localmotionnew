package luan.localmotion;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

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
public interface EventbriteService {
    static String TOKEN="XV3WW7DYC2TEOF75WDO6";
    @GET("events/search/")
    Call<EventbriteEvents> listEvents(@QueryMap Map<String, String> options);
    @GET("events/{id}/")
    Call<EventbriteEvent> getEvent(@Path("id") long id,@QueryMap Map<String, String> options);
    @GET("categories/")
    Call<EventbriteCategories> getCategories();
}