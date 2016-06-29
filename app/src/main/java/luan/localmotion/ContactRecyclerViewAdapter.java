package luan.localmotion;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import luan.localmotion.ContactFragment2.OnListFragmentInteractionListener;
import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.DummyContent.DummyItem;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<ContactItem>  mValues;
    private final OnListFragmentInteractionListener mListener;
    private ViewGroup parent;
    public ContactRecyclerViewAdapter(ArrayList<ContactItem> contactItems, OnListFragmentInteractionListener listener) {
        mValues = contactItems;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.nameView.setText(mValues.get(position).name);
        holder.phoneNumberView.setText(mValues.get(position).phoneNumber);
        ViewGroup.LayoutParams param = (ViewGroup.LayoutParams) holder.profilePicView.getLayoutParams();
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
                    mListener.onContactFragmentInteraction(holder.mItem);
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
        public final TextView phoneNumberView;
        public final ImageView profilePicView;
        public ContactItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            nameView = (TextView) view.findViewById(R.id.name);
            phoneNumberView = (TextView) view.findViewById(R.id.type);
            profilePicView = (ImageView) view.findViewById(R.id.profilePic);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + phoneNumberView.getText() + "'";
        }
    }
}
