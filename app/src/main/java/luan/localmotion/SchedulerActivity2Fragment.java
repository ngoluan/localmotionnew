package luan.localmotion;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;

import luan.localmotion.Content.ContactItem;

/**
 * A placeholder fragment containing a simple view.
 */
public class SchedulerActivity2Fragment extends Fragment implements OnMapReadyCallback {
    Contacts contacts;
    Places places;

    View view;
    public GoogleMap mMap;
    public Marker locMarker;
    public ScrollView mScrollView;
    public SchedulerActivity2Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.content_schedule_view, container, false);
        contacts = new Contacts(getActivity());
        places = new Places(getActivity());
        mScrollView = (ScrollView) container.findViewById(R.id.scrollView);

        if (mMap == null) {
            CustomMapView mapFragment = (CustomMapView) getChildFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new CustomMapView.OnTouchListener() {
                @Override
                public void onTouch() {
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }

        View profilePicView= view.findViewById(R.id.profilePicView);
        profilePicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getActivity(), ChooseContact.class);

                startActivity(contactIntent);
            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Fragment f = getChildFragmentManager().findFragmentById(R.id.map);
        if (f != null){
            getFragmentManager().beginTransaction().remove(f).commit();
            Log.i(MainActivity.TAG, "Destroying map");
            mMap=null;
        }
    }

}
