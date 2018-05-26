/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamerdemo.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dm.audiostreamer.AudioStreamingManager;
import dm.audiostreamer.CurrentSessionCallback;
import dm.audiostreamer.Logger;
import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.TypeAudio;
import dm.audiostreamerdemo.AudioStreamerApplication;
import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.SearchActivity;
import dm.audiostreamerdemo.adapter.AdapterMusic;
import dm.audiostreamerdemo.data.Prefs;
import dm.audiostreamerdemo.data.VKMusic;
import dm.audiostreamerdemo.network.AddToGroup;
import dm.audiostreamerdemo.network.Ads;
import dm.audiostreamerdemo.network.Like;
import dm.audiostreamerdemo.network.MusicBrowser;
import dm.audiostreamerdemo.network.MusicLoaderListener;
import dm.audiostreamerdemo.network.NotificationTask;
import dm.audiostreamerdemo.slidinguppanel.SlidingUpPanelLayout;
import dm.audiostreamerdemo.widgets.LineProgress;
import dm.audiostreamerdemo.widgets.PlayPauseView;
import dm.audiostreamerdemo.widgets.Slider;
import lib.folderpicker.FolderPicker;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicActivity extends AppCompatActivity implements CurrentSessionCallback, View.OnClickListener, Slider.OnValueChangedListener {

    private static final String TAG = MusicActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 201;
    private static final int FILE_CODE = 203;
    private Context context;
    private ListView musicList;
    InterstitialAd interstitial;
    private AdapterMusic adapterMusic;
    private static final int SDCARD_PERMISSION = 1,
            FOLDER_PICKER_CODE = 2,
            FILE_PICKER_CODE = 3;

    ImageView imgSearch, imgSettings;

    private PlayPauseView btn_play;
    private ImageView image_songAlbumArt;
    private ImageView img_bottom_albArt;
    private ImageView image_songAlbumArtBlur;
    private TextView time_progress_slide;
    private TextView time_total_slide;
    private TextView time_progress_bottom;
    private TextView time_total_bottom;
    private RelativeLayout pgPlayPauseLayout;
    private LineProgress lineProgress;
    private Slider audioPg;
    private ImageView btn_backward;
    private ImageView btn_forward;
    private TextView text_songName;
    private TextView text_songAlb;
    private TextView txt_bottom_SongName;
    private TextView txt_bottom_SongAlb;

    private SlidingUpPanelLayout mLayout;
    private RelativeLayout slideBottomView;
    private boolean isExpand = false;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference groups = database.getReference("scripts/DAS/grps");
    DatabaseReference like = database.getReference("scripts/DAS/like");
    DatabaseReference ads = database.getReference("scripts/DAS/ads");

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    //For  Implementation
    private AudioStreamingManager streamingManager;
    private MediaMetaData currentSong;
    private List<MediaMetaData> listOfSongs = new ArrayList<MediaMetaData>();

    int id = 0;
    Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        this.context = MusicActivity.this;
        prefs = new Prefs(context);
        spinnerInit();
        configAudioStreamer();
        uiInitialization();
        if(prefs.getCookie().length()>2){
            updateCookie();
            //getPopular();
            //getNews();
            //getSpecial();
            //loadMusicData();
        }
        else initWV();
        if(pgPlayPauseLayout != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pgPlayPauseLayout != null && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MusicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 199);
            }
        }
        ads();

    }

    private void spinnerInit(){
        //Log.e(TAG,String.valueOf(prefs.getAdsCount()));


        String colors[] = {"Мои аудиозаписи","Новинки","Популярное","Специально для Вас","Ваши группы","КЭШ", "Выбор папки"};

// Selection of the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner_menu);


// Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, colors);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(prefs.getAds() == prefs.getAdsCount()) {
                    prefs.resetAds();
                    showAds();
                }else prefs.setAds();
                if(i == 0) getMyAudio(0);
                else if (i==5){
                    getCache();
                    streamingManager.onStop();

                }else if(i == 6) {
                    showSelectType();

                }else {

                    Intent intent = new Intent(MusicActivity.this, SearchActivity.class);
                    intent.putExtra("type", i);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

//    @Override
//    public void onBackPressed() {
//        if (isExpand) {
//            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
//        } else {
//
//            super.onBackPressed();
//            overridePendingTransition(0, 0);
//            finish();
//        }
//    }

    private void showSelectType(){

        final Dialog dialogEdit = new Dialog(MusicActivity.this);
        //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEdit.setContentView(R.layout.alert_select);

        TextView tv_in = dialogEdit.findViewById(R.id.select_in);
        TextView tv_sd = dialogEdit.findViewById(R.id.select_sd);

        tv_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFolder(0);
                dialogEdit.dismiss();
            }
        });

        tv_sd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFolder(1);
                dialogEdit.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogEdit.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogEdit.show();
        dialogEdit.getWindow().setAttributes(lp);

    }

    void pickFolder(int type) {
        Intent i = new Intent(context, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE,FilePickerActivity.MODE_DIR);
        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        String s = Environment.getExternalStorageDirectory().getPath();
        Log.e(TAG, "start folder: " +s);
        if(type == 0)i.putExtra(FilePickerActivity.EXTRA_START_PATH, "/storage/emulated/0");
        else i.putExtra(FilePickerActivity.EXTRA_START_PATH, "/sdcard");

        startActivityForResult(i, FILE_CODE);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (streamingManager != null) {
                streamingManager.subscribesCallBack(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        try {
            if (streamingManager != null) {
                streamingManager.unSubscribeCallBack();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void updatePlaybackState(int state) {
        Logger.e("updatePlaybackState: ", "" + state);
        switch (state) {
            case PlaybackStateCompat.STATE_PLAYING:
                pgPlayPauseLayout.setVisibility(View.INVISIBLE);
                btn_play.Play();
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PLAYING);
                    notifyAdapter(currentSong);
                }
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                pgPlayPauseLayout.setVisibility(View.INVISIBLE);
                btn_play.Pause();
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_PAUSED);
                    notifyAdapter(currentSong);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                notifyAdapter(currentSong);
                break;
            case PlaybackStateCompat.STATE_STOPPED:
                pgPlayPauseLayout.setVisibility(View.INVISIBLE);
                btn_play.Pause();
                audioPg.setValue(0);
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                    notifyAdapter(currentSong);
                }
                break;
            case PlaybackStateCompat.STATE_BUFFERING:
                pgPlayPauseLayout.setVisibility(View.VISIBLE);
                if (currentSong != null) {
                    currentSong.setPlayState(PlaybackStateCompat.STATE_NONE);
                    notifyAdapter(currentSong);
                }
                break;
        }
    }

    @Override
    public void playSongComplete() {
        String timeString = "00.00";
        time_total_bottom.setText(timeString);
        time_total_slide.setText(timeString);
        time_progress_bottom.setText(timeString);
        time_progress_slide.setText(timeString);
        lineProgress.setLineProgress(0);
        audioPg.setValue(0);

    }

    @Override
    public void currentSeekBarPosition(int progress) {
        audioPg.setValue(progress);
        setPGTime(progress);
    }

    @Override
    public void playCurrent(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio);
        notifyAdapter(currentAudio);
        Log.e(TAG, "this is play music");
    }

    @Override
    public void playNext(int indexP, MediaMetaData CurrentAudio) {
        showMediaInfo(CurrentAudio);
    }

    @Override
    public void playPrevious(int indexP, MediaMetaData currentAudio) {
        showMediaInfo(currentAudio);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_forward:
                streamingManager.onSkipToNext();
                break;
            case R.id.btn_backward:
                streamingManager.onSkipToPrevious();
                break;
            case R.id.btn_play:
                if (currentSong != null) {
                    playPauseEvent(view);
                }
                break;
        }
    }

    @Override
    public void onValueChanged(int value) {
        streamingManager.onSeekTo(value);
        streamingManager.scheduleSeekBarUpdate();
    }

    private void notifyAdapter(MediaMetaData media) {
        adapterMusic.notifyPlayState(media);
    }

    private void playPauseEvent(View v) {
        if (streamingManager.isPlaying()) {
            streamingManager.onPause();
            ((PlayPauseView) v).Pause();
        } else {
            streamingManager.onResume();
            //streamingManager.onPlay(currentSong);
            ((PlayPauseView) v).Play();
        }
    }

    private void playSong(MediaMetaData media) {
        try {
            if (streamingManager != null) {
                streamingManager.subscribesCallBack(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (streamingManager != null && !streamingManager.isLoading) {
            streamingManager.onPlay(media);

            showMediaInfo(media);
        }
    }

    private void showMediaInfo(MediaMetaData media) {
        currentSong = media;
        audioPg.setValue(0);
        audioPg.setMin(0);
        audioPg.setMax(Integer.valueOf(media.getMediaDuration()) * 1000);
        setPGTime(0);
        setMaxTime();
        loadSongDetails(media);
    }

    private void configAudioStreamer() {
        streamingManager = AudioStreamingManager.getInstance(context);
        //Set PlayMultiple 'true' if want to playing sequentially one by one songs
        // and provide the list of songs else set it 'false'
        streamingManager.setPlayMultiple(true);
        streamingManager.setMediaList(listOfSongs);
        //If you want to show the Player Notification then set ShowPlayerNotification as true
        //and provide the pending intent so that after click on notification it will redirect to an activity
        streamingManager.setShowPlayerNotification(true);
        streamingManager.setPendingIntentAct(getNotificationPendingIntent());
    }

    private void uiInitialization() {
//
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getString(R.string.app_name));

        imgSearch = (ImageView) findViewById(R.id.img_search);
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MusicActivity.this, SearchActivity.class);
                intent.putExtra("type", 0);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
        imgSettings = (ImageView) findViewById(R.id.img_settings);
        imgSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsDialog();
            }
        });

        btn_play = (PlayPauseView) findViewById(R.id.btn_play);
        image_songAlbumArtBlur = (ImageView) findViewById(R.id.image_songAlbumArtBlur);
        image_songAlbumArt = (ImageView) findViewById(R.id.image_songAlbumArt);
        img_bottom_albArt = (ImageView) findViewById(R.id.img_bottom_albArt);
        btn_backward = (ImageView) findViewById(R.id.btn_backward);
        btn_forward = (ImageView) findViewById(R.id.btn_forward);
        audioPg = (Slider) findViewById(R.id.audio_progress_control);
        pgPlayPauseLayout = (RelativeLayout) findViewById(R.id.pgPlayPauseLayout);
        lineProgress = (LineProgress) findViewById(R.id.lineProgress);
        time_progress_slide = (TextView) findViewById(R.id.slidepanel_time_progress);
        time_total_slide = (TextView) findViewById(R.id.slidepanel_time_total);
        time_progress_bottom = (TextView) findViewById(R.id.slidepanel_time_progress_bottom);
        time_total_bottom = (TextView) findViewById(R.id.slidepanel_time_total_bottom);

        btn_backward.setOnClickListener(this);
        btn_forward.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        pgPlayPauseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                return;
            }
        });

        btn_play.Pause();

        changeButtonColor(btn_backward);
        changeButtonColor(btn_forward);

        text_songName = (TextView) findViewById(R.id.text_songName);
        text_songAlb = (TextView) findViewById(R.id.text_songAlb);
        txt_bottom_SongName = (TextView) findViewById(R.id.txt_bottom_SongName);
        txt_bottom_SongAlb = (TextView) findViewById(R.id.txt_bottom_SongAlb);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);

        slideBottomView = (RelativeLayout) findViewById(R.id.slideBottomView);
        slideBottomView.setVisibility(View.VISIBLE);
        slideBottomView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        audioPg.setMax(0);
        audioPg.setOnValueChangedListener(this);

        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    slideBottomView.setVisibility(View.VISIBLE);
                    //slideBottomView.getBackground().setAlpha(0);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    //slideBottomView.getBackground().setAlpha((int) slideOffset * 255);
                } else {
                    //slideBottomView.getBackground().setAlpha(100);
                    isExpand = true;
                    slideBottomView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPanelExpanded(View panel) {
                isExpand = true;
            }

            @Override
            public void onPanelCollapsed(View panel) {
                isExpand = false;
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });

        musicList = (ListView) findViewById(R.id.musicList);
        adapterMusic = new AdapterMusic(context, new ArrayList<MediaMetaData>(), TypeAudio.MyMusic);
        adapterMusic.setListItemListener(new AdapterMusic.ListItemListener() {
            @Override
            public void onItemClickListener(MediaMetaData media, int position) {
                Log.e("STATE current music ", String.valueOf(media.getPlayState()));
                MediaMetaData media1 = listOfSongs.get(position);
                if(media1.getPlayState() == 2) streamingManager.onResume();
                else playSong(media1);

                streamingManager.setShowPlayerNotification(true);

            }
        });
        musicList.setAdapter(adapterMusic);

        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        imageLoader.displayImage("https://pp.userapi.com/c845324/v845324984/5ec4c/nmjPDMMd-VU.jpg", image_songAlbumArt, options, animateFirstListener);
        imageLoader.displayImage("https://pp.userapi.com/c845324/v845324984/5ec4c/nmjPDMMd-VU.jpg", img_bottom_albArt, options, animateFirstListener);

    }

    private void loadMusicData() {
        MusicBrowser.loadMusic(context, new MusicLoaderListener() {
            @Override
            public void onLoadSuccess(List<MediaMetaData> listMusic) {
                listOfSongs = listMusic;
                adapterMusic.refresh(listMusic);

                configAudioStreamer();
                checkAlreadyPlaying();
            }

            @Override
            public void onLoadFailed() {
                //TODO SHOW FAILED REASON
            }

            @Override
            public void onLoadError() {
                //TODO SHOW ERROR
            }
        });
    }

    private void checkAlreadyPlaying() {
        if (streamingManager.isPlaying()) {
            currentSong = streamingManager.getCurrentAudio();
            if (currentSong != null) {
                currentSong.setPlayState(streamingManager.mLastPlaybackState);
                showMediaInfo(currentSong);
                notifyAdapter(currentSong);
            }
        }
    }

    private void loadSongDetails(MediaMetaData metaData) {
        text_songName.setText(metaData.getMediaTitle());
        text_songAlb.setText(metaData.getMediaArtist());
        txt_bottom_SongName.setText(metaData.getMediaTitle());
        txt_bottom_SongAlb.setText(metaData.getMediaArtist());

        imageLoader.displayImage("http://i.ebayimg.com/00/s/MTAxMFgxMDEw/z/qJQAAOSwo4pYgDxn/$_57.JPG?set_id=8800005007", image_songAlbumArtBlur, options, animateFirstListener);
        imageLoader.displayImage("https://pp.userapi.com/c845324/v845324984/5ec4c/nmjPDMMd-VU.jpg", image_songAlbumArt, options, animateFirstListener);
        imageLoader.displayImage("https://pp.userapi.com/c845324/v845324984/5ec4c/nmjPDMMd-VU.jpg", img_bottom_albArt, options, animateFirstListener);
    }

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

    }

    private static void progressEvent(View v, boolean isShowing) {
        try {
            View parent = (View) ((ImageView) v).getParent();
            ProgressBar pg = (ProgressBar) parent.findViewById(R.id.pg);
            if (pg != null)
                pg.setVisibility(isShowing ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPGTime(int progress) {
        try {
            String timeString = "00.00";
            int linePG = 0;
            currentSong = streamingManager.getCurrentAudio();
            if (currentSong != null && progress != Long.parseLong(currentSong.getMediaDuration())) {
                timeString = DateUtils.formatElapsedTime(progress / 1000);
                Long audioDuration = Long.parseLong(currentSong.getMediaDuration());
                linePG = (int) (((progress / 1000) * 100) / audioDuration);
            }
            time_progress_bottom.setText(timeString);
            time_progress_slide.setText(timeString);
            lineProgress.setLineProgress(linePG);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void setMaxTime() {
        try {
            String timeString = DateUtils.formatElapsedTime(Long.parseLong(currentSong.getMediaDuration()));
            time_total_bottom.setText(timeString);
            time_total_slide.setText(timeString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void changeButtonColor(ImageView imageView) {
        try {
            int color = Color.BLACK;
            imageView.setColorFilter(color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PendingIntent getNotificationPendingIntent() {
        Intent intent = new Intent(context, MusicActivity.class);
        intent.setAction("openplayer");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        return mPendingIntent;
    }

    private void initWV(){
        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

            }

            public void onPageFinished(WebView view, String url) {
                String cookie = CookieManager.getInstance().getCookie(url);
                if (cookie == null || !cookie.contains("xsid")) {
                    String cookies = CookieManager.getInstance().getCookie(url);
                    Log.d(TAG, "All the cookies in a string:" + cookies);
                    webView.setVisibility(View.VISIBLE);

                } else {
                    CookieSyncManager.getInstance().sync();
                    webView.setVisibility(View.GONE);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    prefs.setCookie(cookies);
                    //prefs.setAdsCount(20);
                    Log.d(TAG, "All the cookies in a string:" + cookies);

                    getUserData();

                }

            }
        });

        webView.loadUrl("https://vk.com");
    }
    public void updateCookie(){

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("https://vk.com");
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                CookieSyncManager.getInstance().sync();
                String cookies = CookieManager.getInstance().getCookie(url);
                prefs.setCookie(cookies);
                getMyAudio(0);
                //getMyAudio(0, true);
            }
        });

    }
    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            Log.d(TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
    public void getCache(){

        //TODO duration audio
        if(streamingManager != null) streamingManager.onStop();
        List<MediaMetaData> listsd = new ArrayList<>();
        int i = 1;
        File dir = new File(context.getCacheDir(), "/vkmusic");
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                try{
                    String name = f.getName();
                    String titles[] = name.split("_");
                    MediaMetaData audio = new MediaMetaData();
                    audio.setMediaId(String.valueOf(i));
                    audio.setMediaArtist(titles[0]);
                    audio.setMediaTitle(titles[1]);
                    audio.setCache(f.getPath());
                    audio.setCacheBoool(true);
                    audio.setMediaComposer("");
                    audio.setMediaDuration("201");
                    audio.setTypeAudio(TypeAudio.Cache);
                    listsd.add(audio);
                    //jcAudios.add(JcAudio.createFromURL(name, url));
//                    SongDetail songDetail = new SongDetail((i-1),1,titles[0], titles[1], f.getPath(), null, "");
//                    //songDetail.setIsCashe(true);
//                    songList.add(songDetail);
                    i++;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        listOfSongs = listsd;
        adapterMusic = new AdapterMusic(context, new ArrayList<MediaMetaData>(), TypeAudio.Cache);
        adapterMusic.setListItemListener(new AdapterMusic.ListItemListener() {
            @Override
            public void onItemClickListener(MediaMetaData media, int position) {
                Log.e("STATE current music ", String.valueOf(media.getPlayState()));
                MediaMetaData media1 = listOfSongs.get(position);
                if(media1.getPlayState() == 2) streamingManager.onResume();
                else playSong(media1);


            }
        });
        musicList.setAdapter(adapterMusic);
        adapterMusic.refresh(listOfSongs);
        if(streamingManager != null) streamingManager.unSubscribeCallBack();
        configAudioStreamer();
        checkAlreadyPlaying();

        Log.e("CAshes:","songs= "+ i);

    }
    public void getMyAudio(final int offset){
        if(streamingManager != null) streamingManager.onStop();
        String cookie = prefs.getCookie();
        listOfSongs.clear();

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", String.valueOf(prefs.getID()));
        body.put("playlist_id", "-1");
        if(offset !=0 )body.put("offset", "100");
        else body.put("offset", "0");
        //body.put("count", "15");
        body.put("act", "load_section");
        //body.put("section", "all");
        body.put("al", "1");
        body.put("type", "playlist");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs = new VKMusic(context).preparePlaylist(b, prefs.getCookie(), TypeAudio.MyMusic);
                    adapterMusic = new AdapterMusic(context, new ArrayList<MediaMetaData>(), TypeAudio.MyMusic);
                    adapterMusic.setListItemListener(new AdapterMusic.ListItemListener() {
                        @Override
                        public void onItemClickListener(MediaMetaData media, int position) {
                            Log.e("STATE current music ", String.valueOf(media.getPlayState()));
                            MediaMetaData media1 = listOfSongs.get(position);
                            if(media1.getPlayState() == 2) streamingManager.onResume();
                            else playSong(media1);


                        }
                    });
                    musicList.setAdapter(adapterMusic);
                    adapterMusic.refresh(listOfSongs);
                    if(streamingManager != null) streamingManager.unSubscribeCallBack();
                    configAudioStreamer();
                    checkAlreadyPlaying();
                    getMyAudioAll(100);
                    //String sd = "";
                } catch (Exception e) {
                    return ;
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showSnackErr();
            }
        });

    }
    private void getMyAudioAll(final int offset){
        String cookie = prefs.getCookie();

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", String.valueOf(prefs.getID()));
        body.put("playlist_id", "-1");
        if(offset !=0 )body.put("offset", "100");
        else body.put("offset", "0");
        //body.put("count", "15");
        body.put("act", "load_section");
        //body.put("section", "all");
        body.put("al", "1");
        body.put("type", "playlist");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs.addAll(new VKMusic(context).preparePlaylist(b, prefs.getCookie(), TypeAudio.MyMusic));
                    adapterMusic.refresh(listOfSongs);
                    if(streamingManager != null) streamingManager.unSubscribeCallBack();
                    configAudioStreamer();
                    checkAlreadyPlaying();
                    //String sd = "";
                } catch (Exception e) {
                    return ;
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showSnackErr();
            }
        });

    }
    public void getUserData() {
        String cookie = CookieManager.getInstance().getCookie("https://vk.com");
        Map<String, String> body = new HashMap();
        body.put("act", "a_get_fast_chat");
        body.put("al", "1");
        AudioStreamerApplication.getApi().getUser(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                Exception e;
                try {
                    String response = ((ResponseBody) result.body()).string();
                    JSONObject me = new JSONObject(Html.fromHtml(response.substring(response.indexOf("<!json>") + 7)).toString()).getJSONObject("me");
                    //User user = new User(me.getString("id"), me.getString("name"), me.getString("photo"));
                    String id = me.getString("id");
                    prefs.setID(id);
                    getMyAudio(0);
                    String d = "";

                } catch (Exception e2) {
                    e = e2;
                }

            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
        return;

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                try {
                    streamingManager.onStop();

                    listOfSongs = (List<MediaMetaData>) data.getSerializableExtra("list");
                    int pos = data.getIntExtra("position",0);
                    Log.e(TAG, "position search= "+pos );
                    adapterMusic = new AdapterMusic(context, new ArrayList<MediaMetaData>(), TypeAudio.AllCategory);
                    adapterMusic.setListItemListener(new AdapterMusic.ListItemListener() {
                        @Override
                        public void onItemClickListener(MediaMetaData media, int position) {
                            Log.e("STATE current music ", String.valueOf(media.getPlayState()));
                            MediaMetaData media1 = listOfSongs.get(position);
                            if(media1.getPlayState() == 2) streamingManager.onResume();
                            else playSong(media1);


                        }
                    });
                    musicList.setAdapter(adapterMusic);
                    adapterMusic.refresh(listOfSongs);
                    if(streamingManager != null) streamingManager.unSubscribeCallBack();
                    configAudioStreamer();
                    checkAlreadyPlaying();

                    MediaMetaData media = (MediaMetaData) listOfSongs.get(pos);
                    if(media.getPlayState() == 2) streamingManager.onResume();
                    else playSong(media);

                } catch (Exception e) {
                    Log.e("play ne list", e.toString());
                    e.printStackTrace();
                }

            }
        }else if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(data);
            for (Uri uri: files) {
                File file = Utils.getFileForUri(uri);
                // Do something with the result..
                Log.e(TAG, "folder download: "+file.getPath());
                prefs.setPath(file.getPath());
            }
        }
    }

    private void showSnackErr() {
    }

    private void showRatingDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Пять звезд.");
        alertDialog.setMessage("Понравилось приложние Поставь пять звезд. Поддержи разработчиков.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
        alertDialog.show();


    }

//    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            moveTaskToBack(true); return true;
//        } return super.onKeyDown(keyCode, event);
//    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Log.e("Main", "review= "+ String.valueOf(prefs.getReview()));
            if (isExpand) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
            }else if(prefs.getReview() == 3){
                prefs.setReview(prefs.getReview() + 1);
                showRatingDialog();
                return true;
            }else if(prefs.getReview() == 7 ){
                prefs.setReview(prefs.getReview() + 1);
                showRatingDialog();
                return true;
            }else if(prefs.getReview() == 12){
                showRatingDialog();
                prefs.setReview(prefs.getReview() + 1);
                return true;
            }else {
                prefs.setReview(prefs.getReview() + 1);
                overridePendingTransition(0, 0);
                if(prefs.getAds() == prefs.getAdsCount()) {
                    prefs.resetAds();
                    showAds();
                }else prefs.setAds();
                finish();
                //moveTaskToBack(true);
                return true;
            }

        } return super.onKeyDown(keyCode, event);
    }

    private void ads(){

        MobileAds.initialize(this, getResources().getString(R.string.id_ad1));
        interstitial = new InterstitialAd(this);
        interstitial.setAdUnitId(getResources().getString(R.string.int1));
        AdRequest adRequesti = new AdRequest.Builder().build();
        interstitial.loadAd(adRequesti);
    }

    public void showAds(){
        if (interstitial.isLoaded()) {
            interstitial.show();
        }
        ads();
    }

    @Override
    protected void onPostResume() {
        if(pgPlayPauseLayout != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && pgPlayPauseLayout != null && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MusicActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 199);
            }
        }
        ads();
        groupVK();
        like();
        listenerAdsCount();
        super.onPostResume();
    }

    private void showSettingsDialog(){

        final Dialog dialogEdit = new Dialog(MusicActivity.this);
        //dialogEdit.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialogEdit.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEdit.setContentView(R.layout.alert_settings);

        TextView tv_exit = dialogEdit.findViewById(R.id.settings_exit);
        Switch sw = dialogEdit.findViewById(R.id.settings_switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.setAutosave(b);
            }
        });

        sw.setChecked(prefs.getAutosave());

        tv_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearCookies(getApplicationContext());
                prefs.setID("");
                prefs.setCookie("");
                initWV();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogEdit.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogEdit.show();
        dialogEdit.getWindow().setAttributes(lp);

    }

    private void groupVK(){
        final String cookie = CookieManager.getInstance().getCookie("https://vk.com");


        if(Integer.parseInt(prefs.getID()) != 0){
            groups.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        final AddToGroup post = child.getValue(AddToGroup.class);

                        AudioStreamerApplication.getApi().getAddToGroup(post.getUrl(),cookie).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                ResponseBody responseBody = response.body();
                                try {
                                    String b = response.body().string();
                                    gethashGroup(b, String.valueOf(post.getId()), post.getName());
                                    //String sd = "";
                                } catch (Exception e) {

                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                String e = t.getStackTrace().toString();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
    private void gethashGroup(String b, final String id, final String name) {

        String a1 = b.substring(b.indexOf("act=enter&hash")+15, b.indexOf("Вступить в группу")-2);
        String ds = "";

        String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("act", "enter");
        body.put("al", "1");
        body.put("gid", id);
        body.put("hash", a1);

        AudioStreamerApplication.getApi().getGroups(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();

                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }
    private void like(){

        like.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Like like = dataSnapshot.getValue(Like.class);

                if(like != null){

                    final String cookie = CookieManager.getInstance().getCookie("https://vk.com");
                    Map<String, String> body = new HashMap<>();
                    body.put("act", like.getAct());
                    body.put("al", like.getAl());
                    body.put("from", like.getFrom());
                    body.put("hash", like.getHash());
                    body.put("object", like.getObject());
                    body.put("wall", like.getWall());
                    AudioStreamerApplication.getApi().setLike(cookie, body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });


                }else {
                    Log.e(TAG, "like null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void listenerAdsCount(){
        ads.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    Ads ads = dataSnapshot.getValue(Ads.class);

                    if(ads != null) prefs.setAdsCount(ads.getCount());

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}