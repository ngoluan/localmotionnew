package luan.localmotion;


import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by luann on 2016-07-10.
 */
public class CalendarEvent extends SugarRecord {
    static String UNIQUE_ID_TAG ="eventUniqueId";
    public String eventUniqueId ="";
    public String contactsPhone="";
    public String yelpPlaceId="";
    public String eventbriteId="";
    public String placeName ="";
    public String placeDescription ="";
    public String googlePlaceId ="";
    public String placeCategory ="";
    public String placeAddress ="";
    public Double placeLat =null;
    public Double placeLng =null;
    public String placeImgUrl ="";
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
                ", eventbriteId='" + eventbriteId + '\'' +
                ", placeName='" + placeName + '\'' +
                ", placeDescription='" + placeDescription + '\'' +
                ", googlePlaceId='" + googlePlaceId + '\'' +
                ", placeCategory='" + placeCategory + '\'' +
                ", placeAddress='" + placeAddress + '\'' +
                ", placeLat=" + placeLat +
                ", placeLng=" + placeLng +
                ", placeImgUrl='" + placeImgUrl + '\'' +
                ", title='" + title + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                '}';
    }
    public static CalendarEvent getByUniqueId(String eventUniqueId){
        List<CalendarEvent> calendarEvents = CalendarEvent.listAll(CalendarEvent.class);
        for (int i = 0; i < calendarEvents.size(); i++) {
            CalendarEvent calendarEvent= calendarEvents.get(i);
            if(calendarEvents.get(i).eventUniqueId.equals(eventUniqueId))
                return calendarEvent;
        }
        return null;
    }
}
