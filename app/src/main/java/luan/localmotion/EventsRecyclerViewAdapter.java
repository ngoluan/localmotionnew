package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class EventsRecyclerViewAdapter extends BaseRecyclerViewAdapter<EventbriteEvent>  {
    int height=196;
    public EventsRecyclerViewAdapter(Context context, BaseListener<EventbriteEvent> listener) {
        super(context, listener);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_event, viewGroup, false);

        float sizeDp= Utils.getPixelfromDP(height, view.getContext());
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height=Math.round(sizeDp);
        view.setLayoutParams(layoutParams);

        return view;
    }

    @Override
    protected void bindView(EventbriteEvent item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            RelativeLayout layout = (RelativeLayout) viewHolder.getView(R.id.eventContainer);
            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(Utils.getPixelfromDP(300, getContext()))));

            TextView eventName = (TextView) viewHolder.getView(R.id.eventName);
            TextView eventCategory = (TextView) viewHolder.getView(R.id.eventCategory);
            TextView eventDescription = (TextView) viewHolder.getView(R.id.eventDescription);
            TextView eventAddress= (TextView) viewHolder.getView(R.id.eventAddress);
            TextView eventTime = (TextView) viewHolder.getView(R.id.eventTime);
            ImageView eventImgView = (ImageView) viewHolder.getView(R.id.eventImgView);

            eventName.setText(item.name.text);
            if(item.category!=null) eventCategory.setText(item.category.name);
            eventDescription.setText(item.description.text);
            eventAddress.setText(item.venue.address.address_1);

            SimpleDateFormat newDate = new SimpleDateFormat("EEEE, MMM d',' h:mm a");
            Calendar beginTime = Calendar.getInstance();
            Date parsedDate = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                parsedDate = dateFormat.parse(item.start.local);
                beginTime.setTime(parsedDate);

            } catch (ParseException e) {
                e.printStackTrace();

            }
            eventTime.setText(newDate.format(parsedDate));
            if (item.logo != null) {
                if (!item.logo.url.equals(""))
                    Picasso.with(getContext()).load(item.logo.url)
                            .error(R.drawable.calendaricon)
                            .placeholder(R.drawable.calendaricon)
                            .into(eventImgView);
            }

            eventName.setText(item.name.text);


        }
    }

}
