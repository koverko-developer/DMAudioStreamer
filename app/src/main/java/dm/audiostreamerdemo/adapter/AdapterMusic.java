/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamerdemo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
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

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.thin.downloadmanager.util.Log;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.TypeAudio;
import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.SearchActivity;
import dm.audiostreamerdemo.activity.MusicActivity;

public class AdapterMusic extends BaseAdapter {
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
                            showPopupMenuCache(view);
                            Log.e("adapter type audio: ", "cashe");
                            break;
                        case MyMusic:
                            showPopupMenuMy(view);
                            Log.e("adapter type audio: ", "mymusic");
                            break;
                        case AllCategory:
                            showPopupMenuAll(view);
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

    private void showPopupMenuCache(View v) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_cache); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

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
                                return true;
                            case R.id.menu2:
                                Log.e("click menu:", "download from cache");
                                return true;
//                            case R.id.menu3:
//                                Log.e("click menu:", "add to playlist");
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }

    private void showPopupMenuMy(View v) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_my); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        switch (item.getItemId()) {

                            case R.id.menu1:
                                Log.e("click menu:", "save to cache");
                                return true;
                            case R.id.menu2:
                                Log.e("click menu:", "download");
                                return true;
                            case R.id.menu3:
                                Log.e("click menu:", "delete from playlist");
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }
    private void showPopupMenuAll(View v) {
        PopupMenu popupMenu = new PopupMenu(mContext, v);
        popupMenu.inflate(R.menu.popup_all); // Для Android 4.0
        // для версии Android 3.0 нужно использовать длинный вариант
        // popupMenu.getMenuInflater().inflate(R.menu.popupmenu,
        // popupMenu.getMenu());

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // Toast.makeText(PopupMenuDemoActivity.this,
                        // item.toString(), Toast.LENGTH_LONG).show();
                        // return true;
                        switch (item.getItemId()) {

                            case R.id.menu1:
                                Log.e("click menu:", "save to cache");
                                return true;
                            case R.id.menu2:
                                Log.e("click menu:", "download");
                                return true;
                            case R.id.menu3:
                                Log.e("click menu:", "add to playlist");
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }

}
