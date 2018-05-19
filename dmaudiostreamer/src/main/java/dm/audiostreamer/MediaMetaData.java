/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamer;


import android.os.Parcel;
import android.os.Parcelable;

public class MediaMetaData implements Parcelable {

    private String mediaId;
    private String mediaUrl;
    private String mediaTitle;
    private String mediaArtist;
    private String mediaAlbum;
    private String mediaComposer;
    private String mediaDuration;
    private String mediaArt;
    private int playState;
    private boolean isCache = false;
    private String cache;
    private TypeAudio typeAudio;
    String hash;
    String hash2;

    public String getHash2() {
        return hash2;
    }

    public void setHash2(String hash2) {
        this.hash2 = hash2;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public TypeAudio getTypeAudio() {
        return typeAudio;
    }

    public void setTypeAudio(TypeAudio typeAudio) {
        this.typeAudio = typeAudio;
    }

    public boolean isCache() {
        return isCache;
    }

    public void setCacheBoool(boolean cache) {
        isCache = cache;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public MediaMetaData() {
    }

    protected MediaMetaData(Parcel in) {
        mediaId = in.readString();
        mediaUrl = in.readString();
        mediaTitle = in.readString();
        mediaArtist = in.readString();
        mediaAlbum = in.readString();
        mediaComposer = in.readString();
        mediaDuration = in.readString();
        mediaArt = in.readString();
        playState = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mediaId);
        dest.writeString(mediaUrl);
        dest.writeString(mediaTitle);
        dest.writeString(mediaArtist);
        dest.writeString(mediaAlbum);
        dest.writeString(mediaComposer);
        dest.writeString(mediaDuration);
        dest.writeString(mediaArt);
        dest.writeInt(playState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaMetaData> CREATOR = new Creator<MediaMetaData>() {
        @Override
        public MediaMetaData createFromParcel(Parcel in) {
            return new MediaMetaData(in);
        }

        @Override
        public MediaMetaData[] newArray(int size) {
            return new MediaMetaData[size];
        }
    };

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaTitle() {
        return mediaTitle;
    }

    public void setMediaTitle(String mediaTitle) {
        this.mediaTitle = mediaTitle;
    }

    public String getMediaArtist() {
        return mediaArtist;
    }

    public void setMediaArtist(String mediaArtist) {
        this.mediaArtist = mediaArtist;
    }

    public String getMediaAlbum() {
        return mediaAlbum;
    }

    public void setMediaAlbum(String mediaAlbum) {
        this.mediaAlbum = mediaAlbum;
    }

    public String getMediaComposer() {
        return mediaComposer;
    }

    public void setMediaComposer(String mediaComposer) {
        this.mediaComposer = mediaComposer;
    }

    public String getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(String mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public String getMediaArt() {
        return mediaArt;
    }

    public void setMediaArt(String mediaArt) {
        this.mediaArt = mediaArt;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public String getHashAdd(){
        try{
            String addHash = this.hash.split("/")[0];
            return addHash;

        }catch (Exception e){
            return "";
        }
    }
    public String getHashRemove(){
        try{
            String addHash = this.hash.split("/")[3];
            return addHash;

        }catch (Exception e){
            return "";
        }
    }
}
