package luan.localmotion.Content;

/**
 * Created by luann on 2016-06-25.
 */
public class PlacesItem {
    public final String placeId;
    public final String name;
    public final String type;
    public final String imgUrl;

    public PlacesItem(String placeId, String name, String type, String imgUrl) {
        this.placeId = placeId;
        this.name = name;
        this.type= type;
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return name;
    }
}
