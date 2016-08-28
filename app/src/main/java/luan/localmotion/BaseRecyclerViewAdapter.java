package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by luann on 2016-07-24.
 */
public abstract class  BaseRecyclerViewAdapter<T>  extends RecyclerView.Adapter<BaseRecyclerViewAdapter.BaseViewHolder> {
    public ArrayList<T> mValues = new ArrayList<T>();
    Context mContext;
    private BaseListener<T> mListener;

    protected abstract View createView(Context context, ViewGroup viewGroup, int viewType);

    protected abstract void bindView(T item, BaseViewHolder baseViewHolder);

    public BaseRecyclerViewAdapter(Context context) {
        this(context, null);
    }

    public BaseRecyclerViewAdapter(Context context, BaseListener<T>  listener) {
        super();
        mContext = context;
        mListener = listener;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return new BaseViewHolder(createView(mContext, viewGroup, viewType), mListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder baseViewHolder, int position) {
        baseViewHolder.setItem(getItem(position));
        bindView(getItem(position), baseViewHolder);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public T getItem(int index) {
        return ((mValues != null && index < mValues.size()) ? mValues.get(index) : null);
    }

    public Context getContext() {
        return mContext;
    }

    public void setList(ArrayList<T> list) {
        mValues = list;
    }

    public List<T> getList() {
        return mValues;
    }

    public void setClickListener(BaseListener<T>  listener) {
        mListener = listener;
    }

    public void removeItem(int position) {
        mValues.remove(position);
        notifyItemRemoved(position);
    }

    public void addItem(int position, T item) {
        mValues.add(position, item);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final T item = mValues.remove(fromPosition);
        mValues.add(toPosition, item);
        notifyItemChanged(fromPosition, toPosition);
    }
    public void animateTo(List<T> directionObjects) {
        applyAndAnimateRemovals(directionObjects);
        applyAndAnimateAdditions(directionObjects);
        applyAndAnimateMovedItems(directionObjects);
    }
    private void applyAndAnimateRemovals(List<T> newModels) {
        for (int i = mValues.size() - 1; i >= 0; i--) {
            final T model = mValues.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }
    private void applyAndAnimateAdditions(List<T> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final T model = newModels.get(i);
            if (!mValues.contains(model)) {
                addItem(i, model);
            }
        }
    }
    private void applyAndAnimateMovedItems(List<T> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final T model = newModels.get(toPosition);
            final int fromPosition = mValues.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
    public class BaseViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private Map<Integer, View> mMapView;
        private BaseListener<T> mListener;
        private T mItem;

        public BaseViewHolder(View view, BaseListener<T> listener) {
            super(view);
            mMapView = new HashMap<>();
            mMapView.put(0, view);
            mListener = listener;

            if (mListener != null)
                view.setOnClickListener(this);
                view.setOnLongClickListener(this);
        }
        public void setItem(T item){
            this.mItem=item;
        }
        @Override
        public void onClick(View view) {
            if (mListener != null)
                mListener.OnClick(mItem, view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            if (mListener != null)
                mListener.OnLongClick(mItem, v,getAdapterPosition());
            return true;
        }

        public void initViewList(int[] idList) {
            for (int id : idList)
                initViewById(id);
        }

        public void initViewById(int id) {
            View view = (getView() != null ? getView().findViewById(id) : null);

            if (view != null)
                mMapView.put(id, view);
        }

        public View getView() {
            return getView(0);
        }

        public View getView(int id) {
            if (mMapView.containsKey(id))
                return mMapView.get(id);
            else
                initViewById(id);

            return mMapView.get(id);
        }
    }
}
