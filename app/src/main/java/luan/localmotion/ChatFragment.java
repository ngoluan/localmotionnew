package luan.localmotion;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.activeandroid.query.Select;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import me.everything.providers.android.contacts.ContactsProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    View view;
    ScheduleActvity2 scheduleActvity2;
    private ArrayList<Message> messages = new ArrayList<Message>();
    RecyclerView recyclerView;
    ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ChatFragment newInstance() {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        scheduleActvity2 = (ScheduleActvity2) getActivity();
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView.setItemAnimator(new FadeInRightAnimator());
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, 1));
            }
            chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(messages, mListener);
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(chatRecyclerViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);

            //recyclerView.setAdapter(new ChatRecyclerViewAdapter(Messages.ITEMS, mListener));
        }


        View sendButtonView= view.findViewById(R.id.sendButton);
        sendButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageEditText = (EditText) view.findViewById(R.id.messageEditText);
                luan.localmotion.Message message = new luan.localmotion.Message();
                message.message = messageEditText.getText().toString();
                message.event = scheduleActvity2.event;
                message.time =  Calendar.getInstance();
                message.save();
                messages.add(message);
                Log.d(MainActivity.TAG, "Luan-onClick size: "+messages.size());
                //chatRecyclerViewAdapter.notifyDataSetChanged();
                chatRecyclerViewAdapter.notifyItemInserted(messages.size()-1);
                TelephonyManager tMgr = (TelephonyManager)  getContext().getSystemService(Context.TELEPHONY_SERVICE);
                final String mPhoneNumber = tMgr.getLine1Number();

                HashMap<String,String> sendData = new HashMap<String, String>();
                sendData.put("type","message");
                sendData.put("toPhone",message.event.contactsPhone);
                sendData.put("message",message.message);
                /*sendData.put("senderName",senderName);*/
                sendData.put("senderPhone",mPhoneNumber);
                sendData.put("dateTime",String.valueOf(message.time.getTimeInMillis()));
                Utils.sendMessage(sendData, getContext());
            }
        });
        return view;
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser)
            retrieveContacts();
        else
            Log.d("MyFragment", "Fragment is not visible.");
    }
    public void retrieveContacts(){
        ScheduleActvity2 activity = (ScheduleActvity2) getActivity();
        long eventId = activity.eventId;
        if(scheduleActvity2.event==null){
            return;
        }

        Log.d(MainActivity.TAG, "Luan-retrieveContacts: "+eventId);
        List<Event> event= new Select()
                .from(Event.class)
                .where("id  = ?", scheduleActvity2.event.getId())
                .orderBy("PhoneNumber ASC")
                .execute();
        try{

            ContactsProvider contactsProvider = new ContactsProvider(getContext());

            LinearLayout layout=(LinearLayout)view.findViewById(R.id.contactImgList);
            for (String contactPhone:event.get(0).getPhones()) {
                if(contactPhone.equals(""))
                    continue;
                Log.d(MainActivity.TAG, "Luan-retrieveContacts: "+contactPhone);
                int id = Contacts.getContactIDFromNumber(contactPhone, getContext());
                CircularImageView img=new CircularImageView(getContext());
                int size = (int) Utils.getPixelfromDP(48, getContext());
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(size,size);
                img.setLayoutParams(layoutParams);
                img.setBorderColor(getResources().getColor(R.color.colorSecondary));
                img.setBorderWidth(10);
                //Picasso.with(getContext()).load(contactsProvider.getPhotoUri(getContext(),String.valueOf(id))).into(img);
                Log.d(MainActivity.TAG, "Luan-retrieveContacts: "+contactsProvider.getPhotoUri(getContext(),String.valueOf(id)));
                Picasso.with(getContext())
                        .load(contactsProvider.getPhotoUri(getContext(),String.valueOf(id)))
                        .placeholder(R.drawable.personicon)
                        .error(R.drawable.personicon).into(img);
                layout.addView(img);
            }
        }
        catch (SQLiteException e){
            Log.d(MainActivity.TAG, "Luan-retrieveContacts: " + e.fillInStackTrace());

        }



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Message item);
        void OnChatFragmentListener(String TAG, Message item);
    }
}
