package luan.localmotion;


import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Calendar;

/**
 * Created by luann on 2016-07-10.
 */
@Table(name = "Messages")
public class Message extends Model {
    @Column(name = "PhoneNumber")
    public String contactsPhone="";
    @Column(name = "Message")
    public String message="";
    @Column(name = "Event")
    public Event event;
    @Column(name = "Time")
    Calendar time=null;
    public Message(){}
    public Message(String contactsPhone, String message){
        this.contactsPhone=contactsPhone;
        this.message=message;
    };
}
