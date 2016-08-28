package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CalendarRecyclerViewAdapter extends BaseRecyclerViewAdapter<CalendarEvent>  {

    public CalendarRecyclerViewAdapter(Context context, BaseListener listener) {
        super(context, listener);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_calendar, viewGroup, false);



        return view;
    }

    @Override
    protected void bindView(CalendarEvent item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            TextView calendarPlace = (TextView) viewHolder.getView(R.id.calendarPlaceView);
            TextView calendarTime = (TextView) viewHolder.getView(R.id.calendarTimeView);
            LinearLayout contactsLayout= (LinearLayout) viewHolder.getView(R.id.contactsLayout);
            ImageView calendarImgView = (ImageView) viewHolder.getView(R.id.calendarImgView);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd 'at' HH:mm");

            String timeText="";
            if(item.beginTime!=0) {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(item.beginTime);
                timeText=dateFormat.format(item.beginTime);


                if (item.endTime!=0){
                    calendar.setTimeInMillis(item.endTime);
                    timeText += " to "+dateFormat.format(item.endTime);
                }

            }
            else{
                timeText="No time specified.";
            }
            calendarTime.setText(timeText);

            if(!item.placeImgUrl.equals("")){
                Picasso.with(getContext()).load(item.placeImgUrl).placeholder(R.drawable.placesicon).into(calendarImgView);
            }
            if(!item.placeName.equals("")){
                calendarPlace.setText(item.placeName);
            }
            Log.d(MainActivity.TAG, "Luan-bindView: "+item.getPhones());
            for (String contact : item.getPhones()) {
                if(contact.equals("")) continue;
                Contacts.fillView(getContext(), Contacts.getContactItem(getContext(), contact),contactsLayout, 72, null);
            }
        }

    }
}
