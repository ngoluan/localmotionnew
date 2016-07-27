package luan.localmotion;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by luann on 2016-06-29.
 */
public class Utils {
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {

            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                //Read byte from input stream

                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;

                //Write byte from output stream
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
    public static String normalizeNumber(String phoneNumber) {
        if (TextUtils.isEmpty(phoneNumber)) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        int len = phoneNumber.length();
        for (int i = 0; i < len; i++) {
            char c = phoneNumber.charAt(i);
            // Character.digit() supports ASCII and Unicode digits (fullwidth, Arabic-Indic, etc.)
            int digit = Character.digit(c, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (sb.length() == 0 && c == '+') {
                sb.append(c);
            } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(phoneNumber));
            }
        }
        String number = sb.toString();
        number=number.replace("+","");
        number=number.replace("+","");
        return number;
    }
    public static String  getPhoneNumber(Context context){
        TelephonyManager tMgr = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        final String normalizePhoneNumber = normalizeNumber(mPhoneNumber);
        return normalizePhoneNumber;
    }
    public static void serverUserCheckIn( final String regIDFCM, Context context){
        TelephonyManager tMgr = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        final String normalizePhoneNumber = normalizeNumber(mPhoneNumber);

        //Used in case regIDFCM not available, like during first installation.
        if(regIDFCM==null){
            return;
        }
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
                            "http://www.local-motion.ca/server/user.php";
                    RequestBody formBody = new FormBody.Builder()
                            .add("command", "checkin")
                            .add("phoneNumber", normalizePhoneNumber)
                            .add("regIDFCM", regIDFCM)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();
                    Log.d(MainActivity.TAG, "Luan-checkin: "+url);
                    Response response = client.newCall(request).execute();
                    return response.body().string();
                }catch(IOException exception){
                    exception.printStackTrace();
                    Log.d(MainActivity.TAG, "Luan-checkin: "+exception.getMessage());
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String msg) {
                Log.d(MainActivity.TAG, "Luan-checkin: "+msg);
            }


        }.execute();
    }
    public static void sendMessage(final HashMap<String, String> data, Context context){
        TelephonyManager tMgr = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        final String mPhoneNumber = tMgr.getLine1Number();
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
                            "http://www.local-motion.ca/server/sendFCM.php";
                    FormBody.Builder builder = new FormBody.Builder();

                    for (Map.Entry<String, String> entry :data.entrySet()
                         ) {
                        builder.add(entry.getKey(), entry.getValue());
                    }
                    RequestBody formBody = builder.build();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();


                    Response response = client.newCall(request).execute();
                    return response.body().string();
                }catch(IOException exception){
                    Log.d(MainActivity.TAG, "Luan-msg send: "+exception.getMessage());
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String msg) {
                Log.d(MainActivity.TAG, "Luan-msg send: "+msg);
            }


        }.execute();
    }
    public static float getPixelfromDP(int size, Context context){
/*        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (250 * scale + 0.5f);*/
        Resources r = context.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, r.getDisplayMetrics());
        return px;
    }
    public static Location getLocationFromHistory(Context context){
        Location mCurrentLocation;
        SharedPreferences prefs = context.getSharedPreferences(
                "luan.localmotion", Context.MODE_PRIVATE);
        if(!prefs.getString("lastLng","").equals("")){
            mCurrentLocation=new Location(prefs.getString("lastProvider",""));
            mCurrentLocation.setLongitude(Double.valueOf(prefs.getString("lastLng","")));
            mCurrentLocation.setLatitude(Double.valueOf(prefs.getString("lastLat","")));
        }
        else{
            mCurrentLocation=null;
        }
        return mCurrentLocation;
    }
    public static int getColor(int i, Context context){
        int color=0;
        switch (i){
            case 0: color = context.getResources().getColor(R.color.colorPrimary);
            case 1: color = context.getResources().getColor(R.color.colorSecondary);
            case 2: color = context.getResources().getColor(R.color.colorTertiary);
            case 3: color = context.getResources().getColor(R.color.colorAccent);
        }

        return color;
    }
    public static void getDirections(LatLng from, LatLng to, final String type, Context context, final OnGetDirections mListener){

        if(from==null){
            Location mCurrentLocation = getLocationFromHistory(context);
            from = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }
        GoogleDirection.withServerKey("AIzaSyDFaWQ80f68t72jnT4QVC3_Q-fiiYprAr4")
                .from(from)
                .to(to)
                .transportMode(type)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if(direction.isOK()) {
                            if (mListener != null)
                                mListener.onGetDirections(direction, type);
                        } else {
                            // Do something
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });
    }
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
    public static Point getDisplaySize(Context context) {
/*
        Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
        String displayName = display.getName();  // minSdkVersion=17+
*/

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;


// display size in pixels
/*        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;*/

        return new Point(width, height);
    }
    static int getResFromInt (Context context, String basename, int index){
        Resources r = context.getResources();
        String name = context.getPackageName();
        return r.getIdentifier(basename + index, "id", name);
    }
    public interface OnGetDirections {

        void onGetDirections(Direction direction, String type);
    }
}
