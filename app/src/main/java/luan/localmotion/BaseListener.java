package luan.localmotion;

import android.view.View;

/**
 * Created by luann on 2016-07-24.
 */
public interface BaseListener<T> {
    public void OnClick(T item, View view, int position);
    public void OnLongClick(T item, View view, int position);
}
