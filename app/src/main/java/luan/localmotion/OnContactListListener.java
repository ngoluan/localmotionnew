package luan.localmotion;

import android.net.Uri;

import java.util.Map;

import luan.localmotion.Content.ContactItem;

public interface OnContactListListener {
    void OnContactClickListener(String TAG, ContactItem item);
}
