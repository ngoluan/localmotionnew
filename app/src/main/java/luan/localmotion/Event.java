package luan.localmotion;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by luann on 2016-07-10.
 */
@Table(name = "Events")
public class Event extends Model {
    @Column(name = "PhoneNumber")
    public List<String> contactsPhone= new ArrayList<String>();
    @Column(name = "YelpId")
    public String yelpPlaceId="";
    @Column(name = "Title")
    public String title="";
    @Column(name = "BeginTime")
    Calendar beginTime=null;
    @Column(name = "EndTime")
    Calendar endTime=null;
    @Column(name = "UniqueId")
    public String uniqueId="";
    public Event(){}

    /*public void addPhone(String phone){
        if(phone.equals(""))
            return;
        List<String> phones = getPhones();
        phones.add(phone);
        UtilListSerializer serializer = new UtilListSerializer();
        contactsPhone = serializer.serialize(phones);
    }*/
/*    public List<String> getPhones(){
        UtilListSerializer serializer = new UtilListSerializer();
        List<String> phones = serializer.deserialize(contactsPhone);
        return phones;
    }    */
    public String getPhones(){
        UtilListSerializer serializer = new UtilListSerializer();
        String phones= serializer.serialize(contactsPhone);
        return phones;
    }
    public String toString(){
        return "UniqueId="+uniqueId+";"+"yelpPlaceId="+yelpPlaceId+";"+"beginTime="+beginTime+";"+"endTime="+endTime+";"+"contactsPhone="+contactsPhone.toString();
    }
}
