package luan.localmotion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.activeandroid.serializer.TypeSerializer;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    public static void serverUserCheckIn( final String regIDFCM, Context context){
        TelephonyManager tMgr = (TelephonyManager)  context.getSystemService(Context.TELEPHONY_SERVICE);
        final String mPhoneNumber = tMgr.getLine1Number();
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }
            @Override
            protected String doInBackground(Void... params) {

                String result = postData();



                return "";
            }
            public String postData()  {

                try{
                    String url =
                            "http://www.local-motion.ca/server/users.php?command=checkin";
                    RequestBody formBody = new FormBody.Builder()
                            .add("phoneNumber", mPhoneNumber)
                            .add("regIDFCM", regIDFCM)
                            .build();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
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
                Log.d(MainActivity.TAG, "Luan-onPostExecute: "+msg);
            }


        }.execute();
    }
    public static int getPixelfromDP(int size, Context context){
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (250 * scale + 0.5f);
        return pixels;
    }


}
