package luan.localmotion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import luan.localmotion.Content.ContactItem;

public class ChatRecyclerViewAdapter extends BaseRecyclerViewAdapter<Chat> {


    public ChatRecyclerViewAdapter(Context context, BaseListener<Chat> listener) {
        super(context, listener);
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_chat, viewGroup, false);

        return view;
    }

    @Override
    protected void bindView(Chat item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            ContactItem contactItem=Contacts.getContactItem(getContext(), item.sendersPhone);
            TextView chatDate = (TextView) viewHolder.getView(R.id.chatDate);
            TextView chatName = (TextView) viewHolder.getView(R.id.chatName);
            TextView chatContent = (TextView) viewHolder.getView(R.id.chatContent);
            ImageView chatProfilePic = (ImageView) viewHolder.getView(R.id.chatProfilePic);

            chatName.setText(contactItem.name);
            chatContent.setText(item.message);

            SimpleDateFormat newDate = new SimpleDateFormat("EEEE, MMM d',' h:mm a");
            Calendar beginTime = Calendar.getInstance();
            Date parsedDate = null;
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                parsedDate = dateFormat.parse(item.dateTime.toString());
                beginTime.setTime(parsedDate);
                chatDate.setText(newDate.format(parsedDate));

            } catch (ParseException e) {
                e.printStackTrace();

            }

            Picasso.with(mContext).load(contactItem.profilePicURI)
                    .error(R.drawable.personicon)
                    .placeholder(R.drawable.personicon)
                    .into(chatProfilePic);

        }
    }
}
