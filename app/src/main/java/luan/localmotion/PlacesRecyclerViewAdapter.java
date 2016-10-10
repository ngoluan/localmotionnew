package luan.localmotion;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import luan.localmotion.Content.PlacesItem;

/**
 * {@link RecyclerView.Adapter} that can display a {} and makes a call to the
 * specified {@link OnFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlacesRecyclerViewAdapter  extends BaseRecyclerViewAdapter<PlacesItem>  {
    public boolean showCategory = false;
    int width=96;
    public PlacesRecyclerViewAdapter(Context context, BaseListener<PlacesItem> listener) {
        super(context, listener);
        width= Utils.getDisplaySize(getContext()).x/3;
    }

    @Override
    protected View createView(Context context, ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_places, viewGroup, false);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width=Math.round(width);
        layoutParams.height=Math.round(width);
        view.setLayoutParams(layoutParams);

        return view;
    }


    @Override
    protected void bindView(PlacesItem item, BaseRecyclerViewAdapter.BaseViewHolder viewHolder) {
        if (item != null) {
            TextView placesName = (TextView) viewHolder.getView(R.id.placesName);
            TextView placesCategory = (TextView) viewHolder.getView(R.id.placesCategory);
            ImageView placesImgView = (ImageView) viewHolder.getView(R.id.placesImg);

            placesName.setText(item.name);
            placesCategory.setText(item.type);

            Picasso.with(mContext).load(item.imgUrl)
                    .error(R.drawable.placesicon)
                    .placeholder(R.drawable.placesicon)
                    .into(placesImgView);

        }
    }




}
