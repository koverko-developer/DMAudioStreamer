/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamerdemo.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.network.Version;

public class SplashActivity extends AppCompatActivity {

    private static final long delayTime = 1000;
    private static final String TAG = "SplashActivity";
    Handler handler = new Handler();
    private Context context;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference version = database.getReference("scripts/DAS/version");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        this.context = SplashActivity.this;

        version.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Version version = dataSnapshot.getValue(Version.class);

                if(version != null) {

                    if(version.isChek()) {
                        Log.e(TAG, "new version true");
                        showNewVersion(version.getPackages());
                    }else {
                        handler.postDelayed(postTask, delayTime);
                    }

                }else {
                    handler.postDelayed(postTask, delayTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



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


}
