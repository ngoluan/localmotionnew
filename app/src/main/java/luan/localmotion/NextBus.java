package luan.localmotion;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import luan.localmotion.Content.NextBusDashItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by luann on 2016-05-28.
 */
public class NextBus {
    //TODO move to http://restbus.info/
    private NextBusListener listener = null;
    private Activity caller;
    public  ArrayList<Bitmap> nextBusIcons = new ArrayList<Bitmap>();
    public  ArrayList<VehicleData> nextBusData = new ArrayList<VehicleData>();

    public Collection<MapMarker> nextBusMapMarkers = new HashSet();
    public  ArrayList<Marker> googleMapMarkers = new ArrayList<Marker>();

    public NextBus(Activity caller){
        this.caller = caller;
        createMarkers();
    }
    public void getPredictionLocation(String lat, String lng) {
        new AsyncTask<String, Void, String>() {
            String type = null;

            @Override
            protected String doInBackground(String... params) {
                type = params[2];
                String result = postData(params[0], params[1], params[2], params[3]);
                return result;
            }

            protected void onPostExecute(final String input) {






                Runnable runnable = new Runnable() {
                    public void run() {

                        JSONObject data;
                        JSONObject route;
                        JSONArray routes;
                        JSONObject prediction;
                        ArrayList<JSONObject> predictionsArr = null;
                        ArrayList<JSONObject> directionsArr = null;
                        ArrayList<String> stopsArr = new ArrayList<String>();
                        ArrayList<String> dirArr = new ArrayList<String>();
                        ArrayList<NextBusDashItem> nextBusItems = new ArrayList<NextBusDashItem>();
                        ArrayList<DashItem> rowItemLocal = new ArrayList<DashItem>();
                        try {
                            data = new JSONObject(input);

                            routes = data.getJSONObject("result").getJSONArray("predictions");

                            for (int i = 0; i < routes.length(); i++) {
                                route = routes.getJSONObject(i);
                                if (route.has("direction") == true) {
                                    directionsArr = new ArrayList<JSONObject>();
                                    if (route.get("direction") instanceof JSONObject) {
                                        directionsArr.add(route.getJSONObject("direction"));
                                    } else {
                                        JSONArray tempArray = route.getJSONArray("direction");
                                        for (int x = 0; x < tempArray.length(); x++) {
                                            directionsArr.add(tempArray.getJSONObject(x));
                                        }
                                    }

                                    for (int j = 0; j < directionsArr.size(); j++) {
                                        JSONObject direction = directionsArr.get(j);
                                        if (dirArr.indexOf(direction.getString("title")) != -1) {
                                            break;
                                        }
                                        stopsArr.add(route.getString("stopTitle"));
                                        dirArr.add(direction.getString("title"));

                                        predictionsArr = new ArrayList<JSONObject>();
                                        if (direction.get("prediction") instanceof JSONObject) {
                                            predictionsArr.add(direction.getJSONObject("prediction"));
                                        } else {
                                            JSONArray tempArray2 = direction.getJSONArray("prediction");
                                            for (int x = 0; x < tempArray2.length(); x++) {
                                                predictionsArr.add(tempArray2.getJSONObject(x));
                                            }
                                        }

                                        for (int k = 0; k < predictionsArr.size(); k++) {
                                            prediction = predictionsArr.get(k);

                                            nextBusItems.add(
                                                    new NextBusDashItem(
                                                            route.getString("routeTag"),
                                                            route.getString("routeTitle"),
                                                            direction.getString("title"),
                                                            route.getString("stopTitle"),
                                                            prediction.getInt("minutes")
                                                    )
                                            );
                                            if (k == 1) {
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            Collections.sort(nextBusItems, new Comparator<NextBusDashItem>() {
                                @Override
                                public int compare(NextBusDashItem nextBusItem1, NextBusDashItem nextBusItem2) {

                                    return nextBusItem1.routeTag.compareTo(nextBusItem2.routeTag);
                                }
                            });
                            ArrayList<ArrayList<NextBusDashItem>> routesArr = new ArrayList<ArrayList<NextBusDashItem>>();
                            ArrayList<NextBusDashItem> tempArr = new ArrayList<NextBusDashItem>();
                            for (int i = 0; i < nextBusItems.size(); i++) {
                                if (tempArr.size() > 0 && !tempArr.get(tempArr.size() - 1).routeTag.equals(nextBusItems.get(i).routeTag)) {
                                    Collections.sort(tempArr, new Comparator<NextBusDashItem>() {
                                        @Override
                                        public int compare(NextBusDashItem nextBusItem1, NextBusDashItem nextBusItem2) {

                                            return nextBusItem1.eta.compareTo(nextBusItem2.eta);
                                        }
                                    });
                                    routesArr.add(tempArr);
                                    tempArr = new ArrayList<NextBusDashItem>();
                                    tempArr.add(nextBusItems.get(i));


                                } else {

                                    tempArr.add(nextBusItems.get(i));
                                }
                                if (i == (nextBusItems.size() - 1)) {
                                    Collections.sort(tempArr, new Comparator<NextBusDashItem>() {
                                        @Override
                                        public int compare(NextBusDashItem nextBusItem1, NextBusDashItem nextBusItem2) {

                                            return nextBusItem1.eta.compareTo(nextBusItem2.eta);
                                        }
                                    });
                                    routesArr.add(tempArr);
                                }

                            }

                            //not needed
                            for (int i = 0; i < routesArr.size(); i++) {
                                rowItemLocal.add(
                                        new DashItem(
                                                routesArr.get(i).get(0).routeTitle,
                                                "",
                                                "",
                                                "header",
                                                "",
                                                ""
                                        )
                                );

                            }
                            if (listener != null)
                                listener.OnGetPredictions(routesArr);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                };
                Thread mythread = new Thread(runnable);
                mythread.start();


            }

            public String postData(String email, String service, String lat, String lng) {

                String url = "http://local-motion.ca/server/nextbus.php?command=getPredictionsFromLocation&email=" +
                        email + "&cityTag=" + service + "&lat=" + lat + "&lng=" + lng;


                try{

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

        }.execute("", "ttc", lat, lng);
    }
    void createMarkers() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap backBitmap = BitmapFactory.decodeResource(caller.getApplicationContext().getResources(),
                        R.drawable.ttcback);
                Bitmap innerBitmap = BitmapFactory.decodeResource(caller.getApplicationContext().getResources(),
                        R.drawable.ttcinner);

                for (int i = 0; i <= 360; i = i + 10) {
                    Matrix matrix = new Matrix();

                    float rotate = i;
                    matrix.postScale(0.25f, 0.25f);
                    matrix.postRotate(rotate, backBitmap.getWidth() / 2, backBitmap.getHeight() / 2);
                    Bitmap ttcback2 = Bitmap.createBitmap(backBitmap, 0, 0, backBitmap.getWidth(), backBitmap.getHeight(), matrix, true);

                    rotate = i;
                    matrix = new Matrix();
                    matrix.postScale(0.2f, 0.2f);
                    Bitmap vehicle2 = Bitmap.createBitmap(innerBitmap, 0, 0, innerBitmap.getWidth(), innerBitmap.getHeight(), matrix, true);

                    Canvas canvasMain = new Canvas(ttcback2);

                    int leftOffset = (ttcback2.getWidth() - vehicle2.getWidth()) / 2;
                    int topOffset = (ttcback2.getHeight() - vehicle2.getHeight()) / 2;
                    Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
                    canvasMain.drawBitmap(vehicle2, leftOffset, topOffset, paint);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    nextBusIcons.add(ttcback2);
                    vehicle2.recycle();
                }
            }
        };

        thread.start();


    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }
    public void getVehicleLocations() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }
            @Override
            protected String doInBackground(Void... params) {

                String result = postData();

                XmlPullParserFactory pullParserFactory;
                try {
                    pullParserFactory = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = pullParserFactory.newPullParser();

                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(new StringReader(result));

                    parseXML(parser);

                } catch (XmlPullParserException e) {

                    e.printStackTrace();
                } catch (IOException e) {

                    e.printStackTrace();
                }

                return "";
            }
            public String postData()  {

                try{
                    String url =
                            "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=ttc";

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
                if (listener != null)
                    listener.OnGetVehicles(nextBusData);
            }

            void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
                int x = 0;

                int eventType = parser.getEventType();
                VehicleData currentVehicle = null;
                nextBusData.clear();
                nextBusMapMarkers.clear();


                while (eventType != XmlPullParser.END_DOCUMENT) {

                    String name = null;
                    Boolean visible = true;

                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            name = parser.getName();

                            if (name.equals("vehicle")) {

                                currentVehicle = new VehicleData(
                                        parser.getAttributeValue(null, "id"),
                                        parser.getAttributeValue(null, "routeTag"),
                                        parser.getAttributeValue(null, "dirTag"),
                                        Float.parseFloat(parser.getAttributeValue(null, "lat")),
                                        Float.parseFloat(parser.getAttributeValue(null, "lon")),
                                        Integer.parseInt(parser.getAttributeValue(null, "heading")),
                                        "",
                                        x,
                                        true);
                            }
                            break;
                        case XmlPullParser.END_TAG:

                            name = parser.getName();
                            if (name.equalsIgnoreCase("vehicle") && currentVehicle != null && currentVehicle.visible!=false) {
                                nextBusMapMarkers.add(currentVehicle.createMapMarker());
                                nextBusData.add(currentVehicle);
                                currentVehicle=null;
                                x++;
                            }
                            break;
                    }
                    eventType = parser.next();
                }
            }
        }.execute();
    }
    void drawMarkers(ArrayList<VehicleData>  vehicles, GoogleMap mMap){

        for (Marker marker:googleMapMarkers) {
            marker.remove();
        }
        googleMapMarkers.clear();
        for (VehicleData vehicle:vehicles) {
            final LatLng loc = new LatLng(vehicle.lat, vehicle.lng);
            if (mMap.getCameraPosition().zoom>=14){
                int heading = Math.round(vehicle.heading/10);
                googleMapMarkers.add(mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .anchor(0.5f,0.5f)
                        .icon(BitmapDescriptorFactory.fromBitmap(nextBusIcons.get(heading)))));

            }
            else if (mMap.getCameraPosition().zoom<=14 ||mMap.getCameraPosition().zoom>=12  ){
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ttcircle);
                googleMapMarkers.add(mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .anchor(0.5f,0.5f)
                        .icon(icon)));
            }


        }
    }
    // Assign the listener implementing events interface that will receive the events
    public void setNextBusListener(NextBusListener listener) {
        this.listener = listener;
    }
    public interface NextBusListener {

        void OnGetPredictions(ArrayList<ArrayList<NextBusDashItem>> routesArr);
        void OnGetVehicles(ArrayList<VehicleData> vehicleData);
    }
}
