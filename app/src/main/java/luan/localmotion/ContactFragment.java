package luan.localmotion;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;
import luan.localmotion.Content.ContactItem;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;
import me.everything.providers.core.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ContactFragment extends Fragment implements SearchView.OnQueryTextListener{

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 4;
    private OnFragmentInteractionListener mListener;
    private ArrayList<ContactItem> contacts = new ArrayList<ContactItem>();
    private ArrayList<ContactItem> originalContacts=new ArrayList<ContactItem>();
    ContactRecyclerViewAdapter contactRecyclerViewAdapter;
    RecyclerView recyclerView;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Activity activity;
    public ContactFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ContactFragment newInstance(int columnCount) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        activity=getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        // Set the adapter
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();

            recyclerView.setItemAnimator(new SlideInLeftAnimator());
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            contactRecyclerViewAdapter= new ContactRecyclerViewAdapter(contacts, mListener);
            AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(contactRecyclerViewAdapter);
            recyclerView.setAdapter(alphaInAnimationAdapter);
            getAllContacts();
        }

/*
        SearchView search = (SearchView) view.findViewById( R.id.searchView);
        search.setOnQueryTextListener(this); // call the QuerytextListner.
*/

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void getAllContacts(){
        Thread thread = new Thread() {
            @Override
            public void run() {

                ContactsProvider contactsProvider = new ContactsProvider(getContext());
                List<Contact> contactsList =  contactsProvider.getContacts().getList();
                HashMap<String, Contact> contactsMap= new HashMap<String, Contact>();
                for (Contact contact:contactsList) {

                    if (!contactsMap.containsKey(contact.normilizedPhone)) {
                        contactsMap.put(contact.normilizedPhone, contact);
                        Bitmap profilePic= Contacts.retrieveContactPhoto(getContext(),contact.phone);
                        contacts.add(new ContactItem(String.valueOf(contact.id), contact.displayName, contact.phone,profilePic));
                    }

                }
                originalContacts= new ArrayList<ContactItem>(contacts);
                Collections.sort(contacts, new Comparator<ContactItem>() {
                    @Override
                    public int compare(ContactItem lhs, ContactItem rhs) {
                        return  lhs.name.compareTo(rhs.name);
                    }


                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactRecyclerViewAdapter.notifyDataSetChanged();
                    }
                });



                //calendarProvider.getEvents(calendarId);
            }
        };

        thread.start();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        query = query.toLowerCase();
        ArrayList<ContactItem> filteredList = new ArrayList<ContactItem>();
        contacts= new ArrayList<ContactItem>(originalContacts);
        for (int i = 0; i < contacts.size(); i++) {

            final String text = contacts.get(i).name.toLowerCase();
            if (text.contains(query)) {

                filteredList.add(contacts.get(i));
            }
        }

        contactRecyclerViewAdapter.animateTo(filteredList);
        recyclerView.scrollToPosition(0);

        return true;
    }

}
