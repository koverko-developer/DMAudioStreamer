package dm.audiostreamerdemo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import dm.audiostreamerdemo.SearchActivity;
import dm.audiostreamerdemo.network.Group;

/**
 * Created by root on 5.5.18.
 */

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsAdapterViewHolder> {
    public static final String TAG = GroupsAdapter.class.getSimpleName();

    private List<Group> groupList;

    private static OnItemClickListener mListener ;
    public SearchActivity activity;
    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    // Define the mListener interface
    public interface OnItemClickListener {
        void onItemClick(int position);

        void onSongItemDeleteClicked(int position);
    }

    // Define the method that allows the parent activity or fragment to define the listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }



    public GroupsAdapter(List<Group> groupList, Context context) {
        this.groupList = groupList;
        activity = (SearchActivity) context;
        setHasStableIds(true);
        this.options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.bg_default_album_art)
                .showImageForEmptyUri(R.drawable.bg_default_album_art)
                .showImageOnFail(R.drawable.bg_default_album_art).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(groupList.get(position).getId());
    }

    @Override
    public GroupsAdapter.GroupsAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friends, parent, false);
        return new GroupsAdapterViewHolder(view);
//        audiosViewHolder.itemView.setOnClickListener(this);
//        return audiosViewHolder;
    }

    @Override
    public void onBindViewHolder(GroupsAdapterViewHolder holder, final int position) {

        String title = groupList.get(position).getName();
        holder.name.setText(title);
        imageLoader.displayImage(groupList.get(position).getImg(), holder.icon, options, animateFirstListener);
    }


    @Override
    public int getItemCount() {
        return groupList == null ? 0 : groupList.size();
    }


    static class GroupsAdapterViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private ImageView  icon;

        public GroupsAdapterViewHolder(View view){
            super(view);
            this.name = (TextView) view.findViewById(R.id.friend_name);
            this.icon = (ImageView) view.findViewById(R.id.header_avatar);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (mListener != null) mListener.onItemClick(getAdapterPosition());
                }
            });


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
}