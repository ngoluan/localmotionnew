package luan.localmotion;

import com.orm.SugarRecord;

/**
 * Created by luann on 2016-07-21.
 */
public class Chat extends SugarRecord {
    public String senderPhone;
    Long dateTime = 0l;
    String message = "";
    String eventUniqueId;
    public Chat(){}
    public Chat(String senderPhone, Long dateTime, String message, String eventUniqueId) {
        this.senderPhone = senderPhone;
        this.dateTime = dateTime;
        this.message = message;
        this.eventUniqueId = eventUniqueId;
    }


    @Override
    public String toString() {
        return "Chat{" +
                "senderPhone='" + senderPhone + '\'' +
                ", dateTime=" + dateTime +
                ", message='" + message + '\'' +
                ", eventUniqueId='" + eventUniqueId + '\'' +
                '}';
    }
}
