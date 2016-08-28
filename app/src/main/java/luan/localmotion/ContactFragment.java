package luan.localmotion;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;

import luan.localmotion.Content.ContactItem;
import me.everything.providers.android.contacts.Contact;
import me.everything.providers.android.contacts.ContactsProvider;

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
public class ContactFragment extends BaseFragment<ContactItem>  implements SearchView.OnQueryTextListener {

    public ContactRecyclerViewAdapter recyclerViewAdapter;

    private ArrayList<ContactItem> originalContacts=new ArrayList<ContactItem>();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public Activity activity;
    public ContactFragment() {

            models=new ArrayList<ContactItem>();
    }

    public static ContactFragment newInstance(int columnCount) {
        return new ContactFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        activity=getActivity();
        recyclerViewAdapter = new ContactRecyclerViewAdapter(getContext(), mListListener);

        listColumns=3;
        createRecyclerViews(R.id.contactRecyleriew);
        setRecyclerViewAdapter(recyclerViewAdapter);

        final EditText search = (EditText) view.findViewById( R.id.contactSearchView);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(MainActivity.TAG, "Luan-afterTextChanged: "+s.toString());
                String query = search.getText().toString().toLowerCase();
                ArrayList<ContactItem> filteredList = new ArrayList<ContactItem>();
                models= new ArrayList<ContactItem>(originalContacts);
                for (int i = 0; i < models.size(); i++) {

                    final String text = models.get(i).name.toLowerCase();
                    if (text.contains(query)) {

                        filteredList.add(models.get(i));
                    }
                }

                recyclerViewAdapter.animateTo(filteredList);
                recyclerView.scrollToPosition(0);
            }
        });


        return view;
    }


    public void getAllContacts(){
        Thread thread = new Thread() {
            @Override
            public void run() {

                ContactsProvider contactsProvider = new ContactsProvider(getContext());

                List<Contact> contactsList =  contactsProvider.getContacts().getList();
                Log.d(MainActivity.TAG, "Luan-run: "+contactsList.size());
                HashMap<String, Contact> contactsMap= new HashMap<String, Contact>();
                for (Contact contact:contactsList) {

                    if (!contactsMap.containsKey(contact.normilizedPhone)) {
                        contactsMap.put(contact.normilizedPhone, contact);
                        //Bitmap profilePic= Contacts.retrieveContactPhoto(getContext(),contact.phone);
                        Bitmap profilePic=null;
                        models.add(new ContactItem(String.valueOf(contact.id), contact.displayName, contact.phone,profilePic, contact.uriPhoto));
                    }

                }
                originalContacts= new ArrayList<ContactItem>(models);
                Collections.sort(models, new Comparator<ContactItem>() {
                    @Override
                    public int compare(ContactItem lhs, ContactItem rhs) {
                        return  lhs.name.compareTo(rhs.name);
                    }
                });

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerViewAdapter.animateTo(models);
                    }
                });

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
        models= new ArrayList<ContactItem>(originalContacts);
        for (int i = 0; i < models.size(); i++) {

            final String text = models.get(i).name.toLowerCase();
            if (text.contains(query)) {

                filteredList.add(models.get(i));
            }
        }

        recyclerViewAdapter.animateTo(filteredList);
        recyclerView.scrollToPosition(0);

        return true;
    }

    @Override
    public void fragmentBecameVisible() {
        getAllContacts();
    }

    @Override
    public void fragmentBecameInvisible() {

    }
    @Override
    public void OnClick(ContactItem item, View view, int position) {
        Intent scheduleIntent = new Intent(getContext(), ScheduleActvity.class);
        scheduleIntent.putExtra("contactPhone", item.phoneNumber);
        startActivity(scheduleIntent);
    }

}
