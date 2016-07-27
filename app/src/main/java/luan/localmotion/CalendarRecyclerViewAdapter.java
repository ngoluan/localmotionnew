package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class CalendarRecyclerViewAdapter extends BaseRecyclerViewAdapter<CalendarEvent>  {

    private ViewGroup parent;

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
            TextView calendarBeginView = (TextView) viewHolder.getView(R.id.calendarBeginView);
            TextView calendarEndView = (TextView) viewHolder.getView(R.id.calendarEndView);
            LinearLayout contactsLayout= (LinearLayout) viewHolder.getView(R.id.contactsLayout);
            ImageView calendarImgView = (ImageView) viewHolder.getView(R.id.calendarImgView);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd G 'at' HH:mm:ss z");

            if(item.beginTime!=0) {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(item.beginTime);
                calendarBeginView.setText(dateFormat.format(item.beginTime));

            }
            if (item.endTime!=0){
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(item.endTime);
                calendarEndView.setText(dateFormat.format(item.endTime));
            }

            if(!item.yelpImageUrl.equals("")){
                Picasso.with(parent.getContext()).load(item.yelpImageUrl).placeholder(R.drawable.placesicon).into(calendarImgView);
            }
            for (String contact : item.getPhones()) {
                if(contact.equals("")) continue;
                Contacts.fillView(parent.getContext(), Contacts.getContactItem(parent.getContext(), contact),contactsLayout);
            }
        }

    }
    public void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(int position, CalendarEvent item) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final CalendarEvent item = mValues.remove(fromPosition);
        mValues.add(toPosition, item);
        notifyItemChanged(fromPosition, toPosition);
    }
    public void animateTo(List<CalendarEvent> directionObjects) {
        applyAndAnimateRemovals(directionObjects);
        applyAndAnimateAdditions(directionObjects);
        applyAndAnimateMovedItems(directionObjects);
    }
    private void applyAndAnimateRemovals(List<CalendarEvent> newModels) {
        for (int i = mValues.size() - 1; i >= 0; i--) {
            final CalendarEvent model = mValues.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(List<CalendarEvent> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final CalendarEvent model = newModels.get(i);
            if (!mValues.contains(model)) {
                addItem(i, model);
            }
        }
    }
    private void applyAndAnimateMovedItems(List<CalendarEvent> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final CalendarEvent model = newModels.get(toPosition);
            final int fromPosition = mValues.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
    @Override
    public int getItemCount() {
        return mValues.size();
    }


}
