package luan.localmotion.Content;

/**
 * Created by luann on 2016-06-25.
 */
public class PlacesItem {
    public final String contactId;
    public final String name;
    public final String type;
    public final String imgUrl;

    public PlacesItem(String contactId, String name, String type, String imgUrl) {
        this.contactId =contactId;
        this.name = name;
        this.type= type;
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return name;
    }
}
