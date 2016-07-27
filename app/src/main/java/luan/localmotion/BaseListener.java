package luan.localmotion;

import android.view.View;

/**
 * Created by luann on 2016-07-24.
 */
public interface BaseListener<T> {
    public void OnClick(T item);
    public void OnClick(T item, View view);
    public void OnLongClick(T item);
}
