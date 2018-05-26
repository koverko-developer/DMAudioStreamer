/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamerdemo.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.network.Version;
import dm.audiostreamerdemo.widgets.CircleImageView;

public class SplashActivity extends AppCompatActivity {

    private static final long delayTime = 1000;
    private static final String TAG = "SplashActivity";
    Handler handler = new Handler();
    private Context context;
    CircleImageView img;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference version = database.getReference("scripts/DAS/version");

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.context = SplashActivity.this;
        img = (CircleImageView) findViewById(R.id.image_songAlbumArt) ;
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        imageLoader.displayImage("https://pp.userapi.com/c845324/v845324984/5ec4c/nmjPDMMd-VU.jpg", img, options, animateFirstListener);

        handler.postDelayed(postTask, delayTime);



    }

    private void showNewVersion(final String new_package) {


        try {
            final Dialog dialogEdit = new Dialog(SplashActivity.this);
            //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogEdit.setContentView(R.layout.alert_new_version);

            Button btn_new = (Button) dialogEdit.findViewById(R.id.button2);
            btn_new.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + new_package)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + new_package)));
                    }
                }
            });

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogEdit.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialogEdit.show();
            dialogEdit.getWindow().setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(postTask);
        super.onDestroy();
    }

    Runnable postTask = new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(context, MusicActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    };
    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            progressEvent(view, false);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            progressEvent(view, true);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 1000);
                    displayedImages.add(imageUri);
                }
            }
            progressEvent(view, true);
        }

        private static void progressEvent(View v, boolean isShowing) {
            try {
                View parent = (View) ((ImageView) v).getParent();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
