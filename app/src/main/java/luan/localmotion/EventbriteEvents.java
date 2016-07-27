package luan.localmotion;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EventbriteEvents {
    @SerializedName("pagination")
    Pagination pagination;
    @SerializedName("events")
    List<EventbriteEvent> events;

    public EventbriteEvents() {
    }

    public List<EventbriteEvent> getEvents() {
        return events;
    }

    public void setEvents(List<EventbriteEvent> events) {
        this.events = events;
    }


    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }
    class Pagination{
        @SerializedName("object_count")
        String object_count;
        @SerializedName("page_number")
        String page_number;
        @SerializedName("page_size")
        String page_size;
        @SerializedName("page_count")
        String page_count;
    }
}


class EventbriteEvent {
    @SerializedName("name")
    Name name;
    @SerializedName("description")
    Description description;
    @SerializedName("id")
    String id="";
    @SerializedName("start")
    Start start;
    @SerializedName("end")
    End end;
    @SerializedName("url")
    String url="";
    @SerializedName("category_id")
    String category_id="";
    @SerializedName("venue_id")
    String venue_id="";
    @SerializedName("logo")
    Logo logo;

    public EventbriteEvent(){

    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description description) {
        this.description = description;
    }

    public void setStart(Start start) {
        this.start = start;
    }

    public void setEnd(End end) {
        this.end = end;
    }

    public Start getStart() {
        return start;
    }

    public End getEnd() {
        return end;
    }

    public Logo getLogo() {
        return logo;
    }

    public void setLogo(Logo logo) {
        this.logo = logo;
    }


    class BaseEventbriteField {
        String text;
        String html;
    }
    class Name extends BaseEventbriteField {}
    class Description extends BaseEventbriteField {}
    class Start  {String local;}
    class End extends Start{}
    class Logo {String url;}



    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVenue_id() {
        return venue_id;
    }

    public void setVenue_id(String venue_id) {
        this.venue_id = venue_id;
    }

}
