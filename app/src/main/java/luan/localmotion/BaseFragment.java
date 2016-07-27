package luan.localmotion;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInRightAnimator;

/**
 * Created by luann on 2016-07-24.
 */
public class BaseFragment<T,A> extends Fragment implements FragmentInterface {
    View view;

    RecyclerView recyclerView;
    int listColumns=1;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    BaseListener<T> mListListener;
    /*BaseRecyclerViewAdapter<T> recyclerViewAdapter;*/
    ArrayList<T> models;

    public BaseFragment(){

    }

    public void createViews(int viewId){
        
        recyclerView = (RecyclerView) view.findViewById(viewId);
        Log.d(MainActivity.TAG, "Luan-createViews: basefragment");
        if (recyclerView instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView.setItemAnimator(new FadeInRightAnimator());

            if(listColumns==1){
                linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
            }
            else{
                gridLayoutManager = new GridLayoutManager(context,listColumns);
                recyclerView.setLayoutManager(gridLayoutManager);
            }

        }
    }
    public void setRecyclerViewAdapter(EventsRecyclerViewAdapter recyclerViewAdapter){
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(recyclerViewAdapter);
        recyclerView.setAdapter(alphaInAnimationAdapter);
    }


    @Override
    public void fragmentBecameVisible() {

    }

    @Override
    public void fragmentBecameInvisible() {

    }

}
