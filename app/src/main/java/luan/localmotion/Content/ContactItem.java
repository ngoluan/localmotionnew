package luan.localmotion.Content;

import android.graphics.Bitmap;

/**
 * Created by luann on 2016-06-25.
 */
public class ContactItem {
    public final String id;
    public final String name;
    public final String phoneNumber;
    public final Bitmap profilePic;

    public ContactItem(String id, String name, String phoneNumber, Bitmap profilePic) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePic=profilePic;
    }

    @Override
    public String toString() {
        return name;
    }
}
