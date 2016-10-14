package luan.localmotion;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import luan.localmotion.Content.ContactItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * {@link RecyclerView.Adapter} that can display a  and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ContactRecyclerViewAdapter extends BaseRecyclerViewAdapter<ContactItem> {
    int width=96;
    public ContactRecyclerViewAdapter(Context context, BaseListener<ContactItem> listener) {
        super(context, listener);
        width= Utils.getDisplaySize(getContext()).x/3;
    }


    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_contact, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width=Math.round(width);
        layoutParams.height=(int) (width*1.);
        view.setLayoutParams(layoutParams);

        return view;
    }
    @Override
    protected void bindView(ContactItem item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            TextView contactName = (TextView) viewHolder.getView(R.id.contactNameView);
            ImageView contactImgView = (ImageView) viewHolder.getView(R.id.contactProfilePic);

            contactName.setText(item.name);
            Picasso.with(mContext).load(item.profilePicURI)
                    .error(R.drawable.personicon)
                    .placeholder(R.drawable.personicon)
                    .into(contactImgView);

        }
    }

}
