package luan.localmotion;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by luann on 2016-07-07.
 */
public class TimeListViewAdapter extends ArrayAdapter<String> {
    public List<Boolean>  mSelectedItemsIds;
    private LayoutInflater inflater;
    private Context mContext;
    public List<String> list;

    public TimeListViewAdapter(Context context, int resourceId, List<String> list) {
        super(context, resourceId, list);
        mSelectedItemsIds = new ArrayList<>(24);
        mContext = context;
        inflater = LayoutInflater.from(mContext);
        this.list = list;

        Boolean[] booleens = new Boolean[24];
        Arrays.fill(booleens, false);

        mSelectedItemsIds = Arrays.asList(booleens);
    }

    private static class ViewHolder {
        TextView itemName;
    }

    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.view_time, null);
            holder.itemName = (TextView) view.findViewById(R.id.custom_tv);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.itemName.setText(list.get(position));
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.custom_layout);
        if(mSelectedItemsIds.get(position)==true){

            layout.setBackgroundColor(parent.getResources().getColor(R.color.colorPrimary));
        }
        else{

            layout.setBackgroundColor(Color.TRANSPARENT);
        }
        return view;
    }
    public void resetSelected(){
        Boolean[] booleens = new Boolean[24];
        Arrays.fill(booleens, false);
        mSelectedItemsIds = Arrays.asList(booleens);
    }
    @Override
    public void remove(String string) {
        list.remove(string);
        notifyDataSetChanged();
    }

    public void toggleSelection(int position) {
        //selectView(position, !mSelectedItemsIds.get(position));
        Boolean value = !mSelectedItemsIds.get(position);
        mSelectedItemsIds.set(position,value);

        notifyDataSetChanged();
    }

    public void selectView(int position, boolean value) {

    }

    public List<Boolean> getSelectedIds() {
        return mSelectedItemsIds;
    }
}