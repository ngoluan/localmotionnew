package luan.localmotion;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by luann on 2016-06-29.
 */
public class Contacts {
    private Activity caller;
    public Contacts(Activity caller){
        this.caller = caller;

    }
    public Bitmap retrieveContactPhoto(Context context, String number) {
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
}
