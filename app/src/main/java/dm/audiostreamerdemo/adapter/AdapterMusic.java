/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamerdemo.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.thin.downloadmanager.util.Log;

import java.io.File;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dm.audiostreamer.AudioStreamingService;
import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.NotificationManager;
import dm.audiostreamer.TypeAudio;
import dm.audiostreamerdemo.AudioStreamerApplication;
import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.SearchActivity;
import dm.audiostreamerdemo.activity.MusicActivity;
import dm.audiostreamerdemo.data.Prefs;
import dm.audiostreamerdemo.network.DownloadServices;
import dm.audiostreamerdemo.network.NotificationTask;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdapterMusic extends BaseAdapter {
    private static String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=";

    private List<MediaMetaData> musicList;
    private Context mContext;
    private LayoutInflater inflate;

    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private ColorStateList colorPlay;
    private ColorStateList colorPause;
    private TypeAudio type;
    MusicActivity activity;
    SearchActivity searchActivity;

    public AdapterMusic(Context context, List<MediaMetaData> authors, TypeAudio type) {
        this.musicList = authors;
        this.mContext = context;
        this.type = type;
        this.inflate = LayoutInflater.from(context);
        this.colorPlay = ColorStateList.valueOf(context.getResources().getColor(R.color.md_black_1000));
        this.colorPause = ColorStateList.valueOf(context.getResources().getColor(R.color.md_blue_grey_500_75));
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    public void refresh(List<MediaMetaData> musicList) {
        if (this.musicList != null) {
            this.musicList.clear();
        }
        this.musicList.addAll(musicList);
        notifyDataSetChanged();
    }

    public void notifyPlayState(MediaMetaData metaData) {
        if (this.musicList != null && metaData != null) {
            int index = Integer.parseInt(metaData.getMediaId());
//            int index = this.musicList.indexOf(metaData);
//            //TODO SOMETIME INDEX RETURN -1 THOUGH THE OBJECT PRESENT IN THIS LIST
//            if (index == -1) {
//                for (int i = 0; i < this.musicList.size(); i++) {
//                    if (this.musicList.get(i).getMediaId().equalsIgnoreCase(metaData.getMediaId())) {
//                        index = i;
//                        break;
//                    }
//                }
//            }
            for (MediaMetaData m: this.musicList
                 ) {
                m.setPlayState(PlaybackStateCompat.STATE_NONE);
            }
            if (index > 0 && index < this.musicList.size()) {
                if(!(this.musicList.get(index).getPlayState() == PlaybackStateCompat.ACTION_PLAY))
                    this.musicList.get(index).setPlayState((int) PlaybackStateCompat.ACTION_PLAY);
                else this.musicList.get(index).setPlayState((int) PlaybackStateCompat.ACTION_PAUSE);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return musicList.size();
    }

    @Override
    public Object getItem(int i) {
        return musicList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        try{
            activity = (MusicActivity) mContext;
        }catch (Exception e){
            searchActivity = (SearchActivity) mContext;
        }
        ViewHolder mViewHolder;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = inflate.inflate(R.layout.inflate_allsongsitem, null);
            mViewHolder.mediaArt = (ImageView) convertView.findViewById(R.id.img_mediaArt);
            mViewHolder.playState = (ImageView) convertView.findViewById(R.id.img_playState);
            mViewHolder.mediaTitle = (TextView) convertView.findViewById(R.id.text_mediaTitle);
            mViewHolder.MediaDesc = (TextView) convertView.findViewById(R.id.text_mediaDesc);
            mViewHolder.img_more = (ImageView) convertView.findViewById(R.id.img_moreicon);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        if(mViewHolder.img_more != null){
            mViewHolder.img_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (type){
                        case Cache:
                            showPopupMenuCache(view, position);
                            Log.e("adapter type audio: ", "cashe");
                            break;
                        case MyMusic:
                            showPopupMenuMy(view, position);
                            Log.e("adapter type audio: ", "mymusic");
                            break;
                        case AllCategory:
                            showPopupMenuAll(view, position);
                            Log.e("adapter type audio: ", "all");
                            break;
                    }
                }
            });
        }

        MediaMetaData media = musicList.get(position);

        mViewHolder.mediaTitle.setText(media.getMediaTitle());
        mViewHolder.MediaDesc.setText(media.getMediaArtist());
        mViewHolder.playState.setImageDrawable(getDrawableByState(mContext, media.getPlayState()));
        String mediaArt = media.getMediaArt();
        imageLoader.displayImage(mediaArt, mViewHolder.mediaArt, options, animateFirstListener);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listItemListener != null) {
                    listItemListener.onItemClickListener(musicList.get(position), position);
                }
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        public ImageView mediaArt;
        public ImageView playState;
        public TextView mediaTitle;
        public TextView MediaDesc;
        public ImageView img_more;
    }


    private Drawable getDrawableByState(Context context, int state) {
        switch (state) {
            case PlaybackStateCompat.STATE_NONE:
                Drawable pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play);
                DrawableCompat.setTintList(pauseDrawable, colorPlay);
                return pauseDrawable;
            case PlaybackStateCompat.STATE_PLAYING:
                AnimationDrawable animation = (AnimationDrawable) ContextCompat.getDrawable(context, R.drawable.equalizer);
                DrawableCompat.setTintList(animation, colorPlay);
                animation.start();
                return animation;
            case PlaybackStateCompat.STATE_PAUSED:
                Drawable playDrawable = ContextCompat.getDrawable(context, R.drawable.equalizer);
                DrawableCompat.setTintList(playDrawable, colorPause);
                return playDrawable;
            default:
                Drawable noneDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play);
                DrawableCompat.setTintList(noneDrawable, colorPlay);
                return noneDrawable;
        }
    }


    private class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

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
                    FadeInBitmapDisplayer.animate(imageView, 200);
                    displayedImages.add(imageUri);
                }
            }
            progressEvent(view, true);
        }

    }

    private static void progressEvent(View v, boolean isShowing) {
        try {
            RelativeLayout rl = (RelativeLayout) ((ImageView) v).getParent();
            ProgressBar pg = (ProgressBar) rl.findViewById(R.id.pg);
            pg.setVisibility(isShowing ? View.GONE : View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setListItemListener(ListItemListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    public ListItemListener listItemListener;

    public interface ListItemListener {
        void onItemClickListener(MediaMetaData media, int position);
    }

    private void showPopupMenuCache(View v, int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_cache); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());
        final MediaMetaData metaData = (MediaMetaData) getItem(position) ;

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        switch (item.getItemId()) {

                            case R.id.menu1:
                                Log.e("click menu:", "delete from cache");
                                deleteFromCache(metaData);
                                final Prefs prefs = new Prefs(mContext);

                                if(prefs.getAds() == prefs.getAdsCount()) {
                                    prefs.resetAds();
                                    activity.showAds();
                                }else prefs.setAds();
                                return true;
//                            case R.id.menu2:
//                                Log.e("click menu:", "download from cache");
//                                return true;
//                            case R.id.menu3:
//                                Log.e("click menu:", "add to playlist");
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }

    private void showPopupMenuMy(View v, int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_my); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        final MediaMetaData metaData = (MediaMetaData) getItem(position) ;

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        final Prefs prefs = new Prefs(mContext);

                        if(prefs.getAds() == prefs.getAdsCount()) {
                            prefs.resetAds();
                            activity.showAds();
                        }else prefs.setAds();
                        switch (item.getItemId()) {

                            case R.id.menu1:

                                Log.e("click menu:", "save to cache");
                                //new DownloadServices(mContext).startDownload(metaData.getMediaArtist()+"_"+metaData.getMediaTitle(),
//                                        1, metaData);
//                                Random random = new Random();
//                                int i = random.nextInt(1000);
//                                new dm.audiostreamer.NotificationTask(mContext, metaData.getMediaArtist()+"_"+metaData.getMediaTitle(), i, metaData)
//                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 10);
                                dToCache(metaData);
                                return true;
                            case R.id.menu2:

                                Log.e("click menu:", "download");
                                new DownloadServices(mContext).startDownload(metaData.getMediaArtist()+"_"+metaData.getMediaTitle(),
                                        2, metaData);
                                return true;
                            case R.id.menu3:

                                Log.e("click menu:", "delete from playlist");
                                removeFromPlaylist(metaData);
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }
    private void showPopupMenuAll(View v , int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_all); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());
        final MediaMetaData metaData = (MediaMetaData) getItem(position) ;
        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        final Prefs prefs = new Prefs(mContext);

                        if(prefs.getAds() == prefs.getAdsCount()) {
                            prefs.resetAds();
                            activity.showAds();
                        }else prefs.setAds();
                        switch (item.getItemId()) {

                            case R.id.menu1:

                                Log.e("click menu:", "save to cache");
                                dToCache(metaData);
                                return true;
                            case R.id.menu2:
                                new DownloadServices(mContext).startDownload(metaData.getMediaArtist()+"_"+metaData.getMediaTitle(),
                                        2, metaData);
                                Log.e("click menu:", "download");
                                return true;
                            case R.id.menu3:
                                Log.e("click menu:", "add to playlist");
                                addToPlaylist(metaData);
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }

    private void dToCache(final MediaMetaData currentAudio){
        final Prefs prefs = new Prefs(mContext);
        String cookie = currentAudio.getMediaComposer();
        Map<String, String> body = new HashMap();
        body.put("act", "reload_audio");
        body.put("al", "1");
        body.put("ids", currentAudio.getMediaUrl());

        Call<ResponseBody> call = AudioStreamerApplication.getApi().alAudio(cookie, body);
        call.enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                try {
                    String lastPath = "";
                    String path = "";
                    String response = ((ResponseBody) res.body()).string();
                    if (response.length() < 100) {
                        dToCache(currentAudio);
                    }else {
                        path = decode(response.substring(response.indexOf("https"), response.indexOf("\",\"")).replace("\\", ""), Integer.parseInt(prefs.getID()));
                        currentAudio.setMediaUrl(path);

                        Random random = new Random();
                        int id = random.nextInt(1000);
                        new NotificationTask(mContext, currentAudio.getMediaArtist()+"_"+currentAudio.getMediaTitle(), id, currentAudio)
                                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 10);
                    }

                } catch (Exception e) {
                    String er = e.toString();
                    //ThrowableExtension.printStackTrace(e);
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private void deleteFromCache(MediaMetaData data){
        List<MediaMetaData> listsd = new ArrayList<>();
        int i = 1;
        File dir = new File(mContext.getCacheDir(), "/vkmusic");
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                try{
                    String name = f.getName();

                    if(name.contains(data.getMediaArtist()+"_"+data.getMediaTitle())) {
                        f.delete();
                        Log.e("adapter", "delete"+name);
                        Toast.makeText(mContext, "Удалено из КЭШа", Toast.LENGTH_SHORT).show();

                        MusicActivity activity = (MusicActivity) mContext;
                        activity.getCache();

                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void addToPlaylist(final MediaMetaData currentAudio){

        try {
            final Prefs prefs = new Prefs(mContext);
            String cookie = currentAudio.getMediaComposer();
            String audio_id = currentAudio.getMediaUrl().split("_")[1];
            String audio_owner_id = currentAudio.getMediaUrl().split("_")[0];
            Map<String, String> body = new HashMap();
            body.put("act", "add");
            body.put("al", "1");
            body.put("audio_id", audio_id);
            body.put("audio_owner_id", audio_owner_id);
            body.put("group_id", "0");
            body.put("hash", currentAudio.getHashAdd());
            body.put("from", "recoms_recoms");

            Call<ResponseBody> call = AudioStreamerApplication.getApi().alAudio(cookie, body);
            call.enqueue(new Callback<ResponseBody>() {
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                    try {

                        if(res.body() != null) Toast.makeText(mContext, "Добавлено в аудиозаписи", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        String er = e.toString();
                        //ThrowableExtension.printStackTrace(e);
                    }
                }

                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void removeFromPlaylist(final MediaMetaData currentAudio){

        try {
            final Prefs prefs = new Prefs(mContext);
            String cookie = currentAudio.getMediaComposer();
            String audio_id = currentAudio.getMediaUrl().split("_")[1];
            String audio_owner_id = currentAudio.getMediaUrl().split("_")[0];
            Map<String, String> body = new HashMap();
            body.put("act", "delete_audio");
            body.put("al", "1");
            body.put("aid", audio_id);
            body.put("oid", audio_owner_id);
            body.put("hash", currentAudio.getHashRemove());
            body.put("restore", "1");

            Call<ResponseBody> call = AudioStreamerApplication.getApi().alAudio(cookie, body);
            call.enqueue(new Callback<ResponseBody>() {
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                    try {

                        if(res.body() != null) Toast.makeText(mContext, "Удалено из плейлиста", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        String er = e.toString();
                        //ThrowableExtension.printStackTrace(e);
                    }
                }

                public void onFailure(Call<ResponseBody> call, Throwable t) {
                }
            });

            MusicActivity activity = (MusicActivity) mContext;
            activity.getMyAudio(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String decode(String url, int userId) {
        try {
            String[] vals = url.split("/?extra=")[1].split("#");
            url = vk_o(vals[0]);
            String[] opsArr = vk_o(vals[1]).split(String.valueOf('\t'));
            for (int i = opsArr.length - 1; i >= 0; i--) {
                String[] argsArr = opsArr[i].split(String.valueOf('\u000b'));
                String opInd = shiftArray(argsArr);
                int i2 = -1;
                url = vk_i(url, Integer.parseInt(argsArr[0]), userId);
                String s ="";
                //            switch (i2) {
                //                case uk.co.samuelwall.materialtaptargetprompt.R.styleable.PromptView_mttp_autoDismiss /*0*/:
                //                    url = vk_i(url, Integer.parseInt(argsArr[0]), userId);
                //                    break;
                //                case uk.co.samuelwall.materialtaptargetprompt.R.styleable.PromptView_mttp_autoFinish /*1*/:
                //                    url = vk_v(url);
                //                    break;
                //                case uk.co.samuelwall.materialtaptargetprompt.R.styleable.PromptView_mttp_backgroundColour /*2*/:
                //                    url = vk_r(url, Integer.parseInt(argsArr[0]));
                //                    break;
                //                case uk.co.samuelwall.materialtaptargetprompt.R.styleable.PromptView_mttp_captureTouchEventOnFocal /*3*/:
                //                    url = vk_x(url, argsArr[0]);
                //                    break;
                //                case uk.co.samuelwall.materialtaptargetprompt.R.styleable.PromptView_mttp_captureTouchEventOutsidePrompt /*4*/:
                //                    url = vk_s(url, Integer.parseInt(argsArr[0]));
                //                    break;
                //                default:
                //                    break;
                //            }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return url.substring(0, url.indexOf("?extra="));
    }

    private static String vk_i(String str, int e, int userID) {
        return vk_s(str, e ^ userID);
    }
    private static String vk_s(String str, int start) {
        StringBuilder result = null;
        try {
            result = new StringBuilder(str);
            int len = str.length();
            int e = start;
            if (len > 0) {
                int i;
                Integer[] shufflePos = new Integer[len];
                for (i = len - 1; i >= 0; i--) {
                    e = Math.abs((((i + 1) * len) ^ (e + i)) % len);
                    shufflePos[i] = Integer.valueOf(e);
                }
                for (i = 1; i < len; i++) {
                    int offset = shufflePos[(len - i) - 1].intValue();
                    String prev = result.substring(i, i + 1);
                    result.replace(i, i + 1, result.substring(offset, offset + 1));
                    result.replace(offset, offset + 1, prev);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return result.toString();
    }

    private static String vk_o(String str) {
        StringBuilder b = null;
        try {
            int len = str.length();
            int i = 0;
            b = new StringBuilder();
            int index2 = 0;
            for (int s = 0; s < len; s++) {
                int symIndex = STR.indexOf(str.substring(s, s + 1));
                if (symIndex >= 0) {
                    if (index2 % 4 != 0) {
                        i = (i << 6) + symIndex;
                    } else {
                        i = symIndex;
                    }
                    if (index2 % 4 != 0) {
                        index2++;
                        b.append((char) ((i >> ((index2 * -2) & 6)) & 255));
                    } else {
                        index2++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b.toString();
    }

    private static String shiftArray(String[] array) {
        String result = array[0];
        System.arraycopy(array, 1, array, 0, array.length - 1);
        return result;
    }



}
