package luan.localmotion;

import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.model.OAuth1RequestToken;

/**
 * Created by luann on 2016-07-24.
 */
/*
Client secret
KEY:GJKWMYZIASNQN3NGMQ
        PNR6WPLEZMPM4NLT6CQOKB2GSUTUKGPYPGQWQAFDHLMTWFAJ5H
        Your personal OAuth token
        C2IZSW45UMSH5LK4TWRL
        Anonymous access OAuth token
        XV3WW7DYC2TEOF75WDO6*/
public class EventbriteAPI extends DefaultApi10a {
    private static final String AUTHORIZE_URL = "https://www.eventbrite.com/oauth/authorize";

    protected EventbriteAPI() {
    }

    private static class InstanceHolder {
        private static final EventbriteAPI INSTANCE = new EventbriteAPI();
    }

    public static EventbriteAPI instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint(){
        return "https://www.eventbrite.com/oauth/token";
    }

    @Override
    public String getRequestTokenEndpoint() {
        return "http://jimbo.com/oauth/request_token";
    }

    @Override
    public String getAuthorizationUrl(OAuth1RequestToken requestToken) {
        return String.format(AUTHORIZE_URL, requestToken.getToken());
    }
}