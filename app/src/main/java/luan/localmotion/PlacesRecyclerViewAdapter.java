package luan.localmotion;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import luan.localmotion.Content.PlacesItem;

/**
 * {@link RecyclerView.Adapter} that can display a {} and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlacesRecyclerViewAdapter extends RecyclerView.Adapter<PlacesRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<PlacesItem> mValues;
    private final OnFragmentInteractionListener mListener;
    public ImageLoader imageLoader;
    Activity activity;

    public PlacesRecyclerViewAdapter(ArrayList<PlacesItem> PlacesItems, OnFragmentInteractionListener listener, Activity activity) {
        mValues = PlacesItems;
        mListener = listener;
        this.activity=activity;
        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_places, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.nameView.setText(mValues.get(position).name);
        holder.typeView.setText(mValues.get(position).type);
        //imageLoader.DisplayImage(mValues.get(position).imgUrl, holder.placesImg);
        Picasso.with(activity).load(mValues.get(position).imgUrl)
                .error(R.drawable.personicon)
                .placeholder(R.drawable.personicon)
                .into(holder.placesImg);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.OnPlacesFragmentListener("PICK_PLACE", holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView nameView;
        public final TextView typeView;
        public final ImageView placesImg;
        public PlacesItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameView = (TextView) view.findViewById(R.id.name);
            typeView = (TextView) view.findViewById(R.id.category);
            placesImg = (ImageView) view.findViewById(R.id.placesImg);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + typeView.getText() + "'";
        }

    }
}
