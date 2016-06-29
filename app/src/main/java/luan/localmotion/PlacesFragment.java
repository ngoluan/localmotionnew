package luan.localmotion;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import luan.localmotion.Content.PlacesItem;

/**
 * A fragment representing a list of Items.
 * <p>
 * Activities containing this fragment MUST implement the {@link OnPlacesFragmentInteractionListener}
 * interface.
 */
public class PlacesFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 3;
    private OnPlacesFragmentInteractionListener mListener;
    public PlacesRecyclerViewAdapter recycleViewAdapter;
    public ArrayList<PlacesItem> places = new ArrayList<PlacesItem>();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PlacesFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PlacesFragment newInstance(int columnCount) {
        PlacesFragment fragment = new PlacesFragment();
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            getAllplaces();
            recycleViewAdapter = new PlacesRecyclerViewAdapter(places, mListener, getActivity());
            recyclerView.setAdapter(recycleViewAdapter);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlacesFragmentInteractionListener) {
            mListener = (OnPlacesFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnPlacesFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
    public void getAllplaces(){
        /*Cursor contatCursor = null;
        try {
            contatCursor = getActivity().getContentResolver().query(placesContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, placesContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");
            int contactIdIdx = contatCursor.getColumnIndex(placesContract.CommonDataKinds.Phone._ID);
            int nameIdx = contatCursor.getColumnIndex(placesContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneNumberIdx = contatCursor.getColumnIndex(placesContract.CommonDataKinds.Phone.NUMBER);
            int photoIdIdx = contatCursor.getColumnIndex(placesContract.CommonDataKinds.Phone.PHOTO_ID);
            contatCursor.moveToFirst();

            do {
                String idContact = contatCursor.getString(contactIdIdx);
                String name = contatCursor.getString(nameIdx);
                String phoneNumber = contatCursor.getString(phoneNumberIdx);
                Log.i(MainActivity.TAG,phoneNumber);
                places.acdd(new PlacesItem(idContact, name, phoneNumber));
            } while (contatCursor.moveToNext());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contatCursor != null) {
                contatCursor.close();
            }
        }*/
        mListener.OnPlacesStart();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPlacesFragmentInteractionListener {
        // TODO: Update argument type and name
        void OnPlacesFragmentListener(PlacesItem item);
        void OnPlacesStart();
    }
}
