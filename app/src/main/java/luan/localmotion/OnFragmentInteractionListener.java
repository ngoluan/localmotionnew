package luan.localmotion;

import luan.localmotion.Content.ContactItem;
import luan.localmotion.Content.PlacesItem;

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
public interface OnFragmentInteractionListener<T> {
    // TODO: Update argument type and name
    void onContactFragmentInteraction(String TAG, ContactItem item);
    void OnPlacesFragmentListener(String TAG, PlacesItem item);
    void OnPlacesStart();
}
