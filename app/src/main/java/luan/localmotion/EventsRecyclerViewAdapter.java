package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class EventsRecyclerViewAdapter extends BaseRecyclerViewAdapter<EventbriteEvent>  {

    public EventsRecyclerViewAdapter(Context context, BaseListener<EventbriteEvent> listener) {
        super(context, listener);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_event, viewGroup, false);

        return view;
    }

    @Override
    protected void bindView(EventbriteEvent item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            TextView eventName = (TextView) viewHolder.getView(R.id.eventName);
            TextView eventTime = (TextView) viewHolder.getView(R.id.eventTime);
            ImageView eventImgView = (ImageView) viewHolder.getView(R.id.eventImgView);

            eventName.setText(item.name.text);
            eventTime.setText(item.start.local);
            if (item.logo.url != null) {
                if(!item.logo.url.equals("")) Picasso.with(getContext()).load(item.logo.url)
                        .error(R.drawable.placesicon)
                        .placeholder(R.drawable.placesicon)
                        .into(eventImgView);
            }

        }
    }

}
