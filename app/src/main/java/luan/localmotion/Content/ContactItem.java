package luan.localmotion.Content;

import android.graphics.Bitmap;

import com.orm.SugarRecord;

/**
 * Created by luann on 2016-06-25.
 */
public class ContactItem extends SugarRecord{
    static public String UNIQUE_ID = "contactPhone";
    public String id;
    public String name;
    public String phoneNumber;
    public Bitmap profilePic;
    public String profilePicURI;
    public boolean isMember =false;

    public ContactItem(String id, String name, String phoneNumber, Bitmap profilePic, String profilePicURI) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profilePic=profilePic;
        this.profilePicURI=profilePicURI;
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", profilePic=" + profilePic +
                ", profilePicURI='" + profilePicURI + '\'' +
                '}';
    }
}
