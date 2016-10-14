package luan.localmotion;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import luan.localmotion.Content.ContactItem;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by luann on 2016-06-29.
 */
public class Contacts {
    private Activity caller;
    public Contacts(Activity caller){
        this.caller = caller;

    }

    public static Bitmap retrieveContactPhoto(Context context, String number) {
        InputStream inputStream=null;
        Bitmap photo=null;
        ContentResolver contentResolver = context.getContentResolver();
        String contactId = null;


        //Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID};

        Cursor cursor2 =
                contentResolver.query(
                        uri,
                        projection,
                        null,
                        null,
                        null);

        if (cursor2 != null) {
            while (cursor2.moveToNext()) {
                contactId = cursor2.getString(cursor2.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            }



        }
        cursor2.close();
        if(contactId!=null){
            inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));
        }


/*        if (inputStream != null) {
            photo = BitmapFactory.decodeStream(inputStream);
        }*/

        if(inputStream!=null){
            photo = BitmapFactory.decodeStream(inputStream);
        }

        return photo;
    }
    static Uri getPhotoUri(long contactId, Context mContext) {
        ContentResolver contentResolver = mContext.getContentResolver();

        try {
            Cursor cursor = contentResolver
                    .query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID
                                    + "="
                                    + contactId
                                    + " AND "

                                    + ContactsContract.Data.MIMETYPE
                                    + "='"
                                    + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                    + "'", null, null);

            if (cursor != null) {
                if (!cursor.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Uri person = ContentUris.withAppendedId(
                ContactsContract.Contacts.CONTENT_URI, contactId);
        return Uri.withAppendedPath(person,
                ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
    public static ContactItem getContactItem(Context context, String phoneNumber){
        ContactItem contact=null;
        String contactName = null;
        String contactId=null;
        Uri contactPicUri=null;
        InputStream inputStream=null;
        Bitmap profilePic=null;
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        if (cursor == null) {
            return null;
        }

        if(cursor.moveToFirst()) {
            contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            contactPicUri = getPhotoUri(Long.parseLong(contactId), context);
            profilePic=null;

            contact=new ContactItem(contactId, contactName,phoneNumber,profilePic, contactPicUri);
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contact;
    }
    public static void isMember(final ContactItem contactItem, final Context context, final ContactListener listener){
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
                            .add("command", "isUser")
                            .add("phoneNumber", contactItem.phoneNumber)
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
                    Log.d(MainActivity.TAG, "Luan-checkin: "+exception.getMessage());
                    return null;
                }

            }
            @Override
            protected void onPostExecute(String msg) {
                Log.d(MainActivity.TAG, "Luan-checkin: "+msg);

                JSONObject data = null;
                try {
                    data = new JSONObject(msg);
                    if(data.getInt("success")==1){

                        listener.OnReceiveIsMember(contactItem, true);
                    }
                    else{
                        Toast.makeText(context, contactItem.name + "does not have ViaVie. Communication limited to SMS.", Toast.LENGTH_LONG).show();
                        listener.OnReceiveIsMember(contactItem, false);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }


        }.execute();
    }

    public String getContactName(Context context, String phoneNumber) {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        if (contactName != null) {
            return contactName;
        } else {
            return phoneNumber;
        }
    }

    public static int getContactIDFromNumber(String contactNumber,Context context)
    {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,contactNumber),new String[] {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while(contactLookupCursor.moveToNext()){
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }
    public static void fillView(Context context, ContactItem contact, ViewGroup view, int size, Integer position, int background){
        LayoutInflater layoutInflater  = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contactView = layoutInflater.inflate(R.layout.view_contact, null);
        if(contact==null)
            return;
        contactView.setTag(contact.phoneNumber);
        float sizeDp= Utils.getPixelfromDP(size, view.getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(Math.round(sizeDp), Math.round(sizeDp));
        contactView.setLayoutParams(layoutParams);

        CircularImageView circularImageView = (CircularImageView) contactView.findViewById(R.id.contactProfilePic);
        Picasso.with(context).load(contact.profilePicURI)
                .error(R.drawable.personicon)
                .placeholder(R.drawable.personicon)
                .into(circularImageView);
        circularImageView.setBorderColor(context.getResources().getColor(background));

        TextView name = (TextView) contactView.findViewById(R.id.contactNameView);
        name.setText(contact.name);
        contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        if(position!=null){
            view.addView(contactView, position);
        }
        else{
            view.addView(contactView);
        }

    }
    public interface ContactListener {
        void OnReceiveIsMember(ContactItem contact, Boolean result);
    }
}
