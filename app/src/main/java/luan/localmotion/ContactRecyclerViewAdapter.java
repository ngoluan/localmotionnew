package luan.localmotion;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import luan.localmotion.Content.ContactItem;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ContactItem>  mValues;
    private final OnFragmentInteractionListener mListener;
    private ViewGroup parent;
    public ContactRecyclerViewAdapter(ArrayList<ContactItem> contactItems, OnFragmentInteractionListener listener) {
        mValues = contactItems;

        mListener = listener;
    }
    public void setData(ArrayList<ContactItem> contactItems){
        mValues=contactItems;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.nameView.setText(mValues.get(position).name);/*
        holder.phoneNumberView.setText(mValues.get(position).phoneNumber);*/
        ViewGroup.LayoutParams param = holder.profilePicView.getLayoutParams();
        param.height = holder.profilePicView.getWidth();
        holder.profilePicView.setLayoutParams(param);
        if(mValues.get(position).profilePic!=null){
            holder.profilePicView.setImageBitmap((mValues.get(position).profilePic));
        }else{

            Drawable res = parent.getResources().getDrawable(R.drawable.personicon);
            holder.profilePicView.setImageDrawable(res);
        }
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                    mListener.onContactFragmentInteraction("PICK_CONTACT", holder.mItem);
                }
            }
        });
    }
    public void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(int position, ContactItem item) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final ContactItem item = mValues.remove(fromPosition);
        mValues.add(toPosition, item);
        notifyItemChanged(fromPosition, toPosition);
    }
    public void animateTo(ArrayList<ContactItem> contactItems) {
        applyAndAnimateRemovals(contactItems);
        applyAndAnimateAdditions(contactItems);
        applyAndAnimateMovedItems(contactItems);
    }
    private void applyAndAnimateRemovals(ArrayList<ContactItem> newModels) {
        for (int i = mValues.size() - 1; i >= 0; i--) {
            final ContactItem model = mValues.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(ArrayList<ContactItem> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final ContactItem model = newModels.get(i);
            if (!mValues.contains(model)) {
                addItem(i, model);
            }
        }
    }
    private void applyAndAnimateMovedItems(ArrayList<ContactItem> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final ContactItem model = newModels.get(toPosition);
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
        public final View mView;
        public final TextView nameView;/*
        public final TextView phoneNumberView;*/
        public final ImageView profilePicView;
        public ContactItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameView = (TextView) view.findViewById(R.id.name);/*
            phoneNumberView = (TextView) view.findViewById(R.id.type);*/
            profilePicView = (ImageView) view.findViewById(R.id.profilePic);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nameView.getText() + "'";
        }
    }
}
