package luan.localmotion;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.googledirection.constant.TransportMode;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by luann on 2016-07-07.
 */
public class DirectionsListViewAdapter extends ArrayAdapter<ScheduleFragment.DirectionsObject> {
    public SparseBooleanArray mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    public List<ScheduleFragment.DirectionsObject> list;

    public DirectionsListViewAdapter(Context context, int resourceId, List<ScheduleFragment.DirectionsObject> list) {
        super(context, resourceId, list);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;
    }

    private static class ViewHolder {
        TextView typeView;
        ImageView imgView;
        TextView etaView;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.view_directions, null);
            holder.typeView = (TextView) view.findViewById(R.id.typeView);
            holder.etaView = (TextView) view.findViewById(R.id.etaView);
            holder.imgView = (ImageView) view.findViewById(R.id.imgView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.typeView.setText(list.get(position).type);
        holder.etaView.setText(list.get(position).ETA);
        if(list.get(position).type.equals(TransportMode.DRIVING)){
            Picasso.with(parent.getContext()).load(R.drawable.driveicon)
                    .into(holder.imgView);
        }
        else if(list.get(position).type.equals(TransportMode.TRANSIT)) {
            Picasso.with(parent.getContext()).load(R.drawable.busicon)
                    .into(holder.imgView);
        }
        else if(list.get(position).type.equals(TransportMode.BICYCLING)){
            Picasso.with(parent.getContext()).load(R.drawable.bikeshareicon)
                    .into(holder.imgView);
        }
        else if(list.get(position).type.equals(TransportMode.WALKING)){
            Picasso.with(parent.getContext()).load(R.drawable.walkicon)
                    .into(holder.imgView);
        }
        return view;
    }




}