package luan.localmotion;


import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by luann on 2016-07-10.
 */
public class CalendarEvent extends SugarRecord {
    public String eventUniqueId ="";
    public String contactsPhone="";
    public String yelpPlaceId="";
    public String yelpBusinessName="";
    public String yelpsnippetText="";
    public String yelpcategories ="";
    public String yelpAddress ="";
    public Double yelpLat =null;
    public Double yelpLng =null;
    public String yelpImageUrl ="";
    public String title="";
    Long beginTime=0l;
    Long endTime=0l;
    public CalendarEvent(){}

    public List<String> getPhones(){
        UtilListSerializer serializer = new UtilListSerializer();
        List<String> phones= serializer.deserialize(contactsPhone);
        return phones;
    }
    public void addPhone(String normalizedPhoneNUmber){
        UtilListSerializer serializer = new UtilListSerializer();
        List<String> phones= serializer.deserialize(contactsPhone);
        for (int i = 0; i < phones.size(); i++) {
            if(phones.get(i).equals("")){
                phones.remove(i);
            }
            else{
                if (phones.get(i).equals(normalizedPhoneNUmber)) return;
            }

        }
        phones.add(normalizedPhoneNUmber);
        contactsPhone = serializer.serialize(phones);
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "eventUniqueId='" + eventUniqueId + '\'' +
                ", contactsPhone='" + contactsPhone + '\'' +
                ", yelpPlaceId='" + yelpPlaceId + '\'' +
                ", yelpBusinessName='" + yelpBusinessName + '\'' +
                ", yelpsnippetText='" + yelpsnippetText + '\'' +
                ", yelpcategories='" + yelpcategories + '\'' +
                ", yelpImageUrl='" + yelpImageUrl + '\'' +
                ", title='" + title + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                '}';
    }
}
