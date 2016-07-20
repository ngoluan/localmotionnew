package luan.localmotion;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.googledirection.constant.TransportMode;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import luan.localmotion.ScheduleFragment.DirectionsObject;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DirectionsRecyclerViewAdapter extends RecyclerView.Adapter<DirectionsRecyclerViewAdapter.ViewHolder> {

    public ArrayList<DirectionsObject>  mValues;
    private final OnDirectionsListener mListener;
    private ViewGroup parent;
    public DirectionsRecyclerViewAdapter(ArrayList<DirectionsObject> DirectionsObjects, OnDirectionsListener listener) {
        mValues = DirectionsObjects;

        mListener = listener;
    }
    public void setData(ArrayList<DirectionsObject> DirectionsObjects){
        mValues=DirectionsObjects;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_directions, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //holder.mItem = mValues.get(position);
        holder.typeView.setText(mValues.get(position).type);
        holder.etaView.setText(mValues.get(position).ETA);


        if(mValues.get(position).type.equals(TransportMode.DRIVING)){
            Picasso.with(parent.getContext()).load(R.drawable.driveicon)
                    .into(holder.imgView);
        }
        else if(mValues.get(position).type.equals(TransportMode.TRANSIT)) {
            Picasso.with(parent.getContext()).load(R.drawable.busicon)
                    .into(holder.imgView);
        }
        else if(mValues.get(position).type.equals(TransportMode.BICYCLING)){
            Picasso.with(parent.getContext()).load(R.drawable.bikeshareicon)
                    .into(holder.imgView);
        }
        else if(mValues.get(position).type.equals(TransportMode.WALKING)){
            Picasso.with(parent.getContext()).load(R.drawable.walkicon)
                    .into(holder.imgView);
        }
        /*if(mValues.get(position).profilePic!=null){
            Picasso.with(parent.getContext()).load(mValues.get(position).profilePicURI).into(holder.profilePicView);
            //holder.profilePicView.setImageBitmap((mValues.get(position).profilePic));
        }else{

*//*            Drawable res = holder.getResources().getDrawable(R.drawable.personicon);
            holder.profilePicView.setImageDrawable(res);*//*
            Picasso.with(parent.getContext()).load(R.drawable.personicon).into(holder.profilePicView);
        }*/
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    ViewHolder holder = (ViewHolder) v.getTag();
                    int position = holder.getPosition();

                    mListener.OnDirectionsClickListener( mValues.get(position));
                }
            }
        });
    }
    public void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(int position, DirectionsObject item) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final DirectionsObject item = mValues.remove(fromPosition);
        mValues.add(toPosition, item);
        notifyItemChanged(fromPosition, toPosition);
    }
    public void animateTo(ArrayList<DirectionsObject> DirectionsObjects) {
        applyAndAnimateRemovals(DirectionsObjects);
        applyAndAnimateAdditions(DirectionsObjects);
        applyAndAnimateMovedItems(DirectionsObjects);
    }
    private void applyAndAnimateRemovals(ArrayList<DirectionsObject> newModels) {
        for (int i = mValues.size() - 1; i >= 0; i--) {
            final DirectionsObject model = mValues.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(ArrayList<DirectionsObject> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final DirectionsObject model = newModels.get(i);
            if (!mValues.contains(model)) {
                addItem(i, model);
            }
        }
    }
    private void applyAndAnimateMovedItems(ArrayList<DirectionsObject> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final DirectionsObject model = newModels.get(toPosition);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected View mView;

        protected TextView typeView;
        protected ImageView imgView;
        protected TextView etaView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            etaView = (TextView) view.findViewById(R.id.etaView);
            typeView = (TextView) view.findViewById(R.id.typeView);
            imgView = (ImageView) view.findViewById(R.id.imgView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + etaView.getText() + "'";
        }
    }
    public interface OnDirectionsListener {
        void OnDirectionsClickListener(DirectionsObject item);
    }

}
