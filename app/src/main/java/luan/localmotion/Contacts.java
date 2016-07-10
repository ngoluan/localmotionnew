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
import java.util.Random;

import luan.localmotion.Content.ContactItem;

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
    public ContactItem getContactItem(Context context, String phoneNumber){
        ContactItem contact=null;
        String contactName = null;
        String contactId=null;
        String contactPicUri=null;
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
            contactPicUri = cursor
                    .getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
/*
            if(contactId!=null){
                inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactId)));
            }


            if(inputStream!=null){
                profilePic = BitmapFactory.decodeStream(inputStream);
            }
*/
            profilePic=null;

            contact=new ContactItem(contactId, contactName,phoneNumber,profilePic, contactPicUri);
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contact;
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

    public  int getContactIDFromNumber(String contactNumber,Context context)
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
}
