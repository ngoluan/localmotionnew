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
    public String contactsPhone="";
    @Column(name = "YelpId")
    public String yelpPlaceId="";
    @Column(name = "Title")
    public String title="";
    @Column(name = "BeginTime")
    Calendar beginTime=null;
    @Column(name = "EndTime")
    Calendar endTime=null;
    public Event(){}
    public void addPhone(String phone){
        if(phone.equals(""))
            return;
        List<String> phones = getPhones();
        phones.add(phone);
        UtilListSerializer serializer = new UtilListSerializer();
        contactsPhone = serializer.serialize(phones);
    }
    public List<String> getPhones(){
        UtilListSerializer serializer = new UtilListSerializer();
        List<String> phones = serializer.deserialize(contactsPhone);
        return phones;
    }
}
