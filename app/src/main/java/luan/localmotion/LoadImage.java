package luan.localmotion;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by luann on 2016-06-30.
 */
public class LoadImage extends AsyncTask<String, String, Bitmap> {
    Bitmap bitmap;
    ImageView img;

    public LoadImage(ImageView img) {
        super();
        this.img = img;
        // do stuff
    }

    protected Bitmap doInBackground(String... args) {
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap image) {

        if (image != null) {
            img.setImageBitmap(image);

        } else {


        }
    }
}
