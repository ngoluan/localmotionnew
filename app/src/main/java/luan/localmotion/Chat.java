package luan.localmotion;

import com.orm.SugarRecord;

/**
 * Created by luann on 2016-07-21.
 */
public class Chat extends SugarRecord {
    public String sendersPhone;
    Long dateTime = 0l;
    String message = "";
    String eventUniqueId;
    public Chat(){}
    public Chat(String sendersPhone, Long dateTime, String message, String eventUniqueId) {
        this.sendersPhone = sendersPhone;
        this.dateTime = dateTime;
        this.message = message;
        this.eventUniqueId = eventUniqueId;
    }


    @Override
    public String toString() {
        return "Chat{" +
                "sendersPhone='" + sendersPhone + '\'' +
                ", dateTime=" + dateTime +
                ", message='" + message + '\'' +
                ", eventUniqueId='" + eventUniqueId + '\'' +
                '}';
    }
}
