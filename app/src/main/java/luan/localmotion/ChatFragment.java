package luan.localmotion;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;
import me.everything.providers.android.contacts.ContactsProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ChatFragment extends Fragment implements FragmentInterface {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    View view;
    ScheduleActvity scheduleActvity;
    private ArrayList<Chat> chats = new ArrayList<Chat>();
    RecyclerView recyclerView;
    ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    static String TYPE_MESSAGE="chat";
    static String TYPE_EVENT="calendarEvent";
    LinearLayoutManager linearLayoutManager;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    MessageReceiver messagingReceiver= new MessageReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(MainActivity.TAG, "Luan-onReceiveFragment: "+intent.getExtras().toString());
            String type = intent.getExtras().getString("type");
            String id = intent.getExtras().getString("id");
            Chat chat = Chat.findById(Chat.class,Integer.parseInt(id));
            chats.add(chat);
            chatRecyclerViewAdapter.animateTo(chats);
            linearLayoutManager.scrollToPosition(chats.size());
        }
    };
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

        getActivity().registerReceiver(messagingReceiver, new IntentFilter(ChatFragment.TYPE_MESSAGE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        scheduleActvity = (ScheduleActvity) getActivity();

        if(scheduleActvity.calendarEvent.getId()!=null){
            List<Chat> chatList = Chat.find(Chat.class, "event_unique_id=?", scheduleActvity.calendarEvent.eventUniqueId.toString());
            if(chatList!=null) chats.addAll(chatList);
        }

        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView.setItemAnimator(new FadeInRightAnimator());
            linearLayoutManager = new LinearLayoutManager(context);
            linearLayoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(linearLayoutManager);
            chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(chats, mListener);
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(chatRecyclerViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);
            linearLayoutManager.scrollToPosition(chats.size());
            //recyclerView.setAdapter(new ChatRecyclerViewAdapter(Messages.ITEMS, mListener));
        }


        View sendButtonView= view.findViewById(R.id.sendButton);
        sendButtonView.setOnClickListener(onSendButtonClick);
        return view;
    }

    android.view.View.OnClickListener onSendButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EditText messageEditText = (EditText) view.findViewById(R.id.messageEditText);
            Chat chat = new Chat(
                    Utils.getPhoneNumber(getContext()),
                    Calendar.getInstance().getTimeInMillis(),messageEditText.getText().toString(),
                    scheduleActvity.calendarEvent.eventUniqueId);
            long chatId= chat.save();
            chats.add(chat);
            chatRecyclerViewAdapter.notifyItemInserted(chats.size()-1);
            TelephonyManager tMgr = (TelephonyManager)  getContext().getSystemService(Context.TELEPHONY_SERVICE);

            HashMap<String,String> sendData = new HashMap<String, String>();

            scheduleActvity.calendarEvent.save();


            sendData.put("type",TYPE_MESSAGE);
            sendData.put("contactsPhone",scheduleActvity.calendarEvent.contactsPhone);
            sendData.put("message",chat.message);
            sendData.put("senderPhone",chat.senderPhone);
            sendData.put("dateTime",String.valueOf(chat.dateTime));
            Utils.sendMessage(sendData, getContext());
            scheduleActvity.calendarEvent.save();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(messagingReceiver, new IntentFilter(ChatFragment.TYPE_MESSAGE));

    }
    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        getActivity().unregisterReceiver(messagingReceiver);
        super.onPause();
    }
    public void retrieveContacts(){
        ScheduleActvity activity = (ScheduleActvity) getActivity();
        long eventId = activity.eventId;
        if(scheduleActvity.calendarEvent ==null){
            return;
        }

        Log.d(MainActivity.TAG, "Luan-retrieveContacts: "+eventId);

/*        List<CalendarEvent> calendarEvent= new Select()
                .from(CalendarEvent.class)
                .where("id  = ?", scheduleActvity.calendarEvent.getId())
                .orderBy("PhoneNumber ASC")
                .execute();*/
        try{

            ContactsProvider contactsProvider = new ContactsProvider(getContext());

            LinearLayout layout=(LinearLayout)view.findViewById(R.id.contactImgList);
            for (String contactPhone:scheduleActvity.calendarEvent.getPhones()) {
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
    public void fragmentBecameVisible() {
        retrieveContacts();
        List<Chat> chatList = Chat.find(Chat.class, "event_unique_id=?", scheduleActvity.calendarEvent.eventUniqueId.toString());
        if(chatList!=null) {
            chats.clear();
            chats.addAll(chatList);
            chatRecyclerViewAdapter.animateTo(chats);
            linearLayoutManager.scrollToPosition(chats.size());
        }
    }

    @Override
    public void fragmentBecameInvisible() {
        //if(messagingReceiver!=null){getActivity().unregisterReceiver(messagingReceiver);}

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
        //if(messagingReceiver!=null){getActivity().unregisterReceiver(messagingReceiver);}
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
        void onListFragmentInteraction(Chat item);
        void OnChatFragmentListener(String TAG, Chat item);
    }
}
