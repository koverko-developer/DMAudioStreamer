/*
 * This is the source code of DMAudioStreaming for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry(dibakar.ece@gmail.com), 2017.
 */
package dm.audiostreamer;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AudioStreamingManager extends StreamingManager {
    private static final String TAG = Logger.makeLogTag(AudioStreamingManager.class);

    private AudioPlaybackListener audioPlayback;
    private CurrentSessionCallback currentSessionCallback;
    private static volatile AudioStreamingManager instance = null;
    private static String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=";
    private Context context;
    private int index = 0;
    private boolean playMultiple = false;
    private boolean showPlayerNotification = false;
    public PendingIntent pendingIntent;
    private MediaMetaData currentAudio;
    private List<MediaMetaData> mediaList = new ArrayList<>();
    public static volatile Handler applicationHandler = null;

    public Prefs prefs;
    public boolean isLoading = false;

    int id = 0;

    public static AudioStreamingManager getInstance(Context context) {

        if (instance == null) {
            synchronized (AudioStreamingManager.class) {
                instance = new AudioStreamingManager();
                instance.context = context;
                instance.audioPlayback = new AudioPlaybackListener(context);
                instance.audioPlayback.setCallback(new MyStatusCallback());
                applicationHandler = new Handler(context.getMainLooper());

            }

        }
        return instance;
    }

    public void subscribesCallBack(CurrentSessionCallback callback) {
        this.currentSessionCallback = callback;
    }

    public int getCurrentIndex() {
        return this.index;
    }

    public void unSubscribeCallBack() {
        this.currentSessionCallback = null;
    }

    public MediaMetaData getCurrentAudio() {
        return currentAudio;
    }

    public String getCurrentAudioId() {
        return currentAudio != null ? currentAudio.getMediaId() : "";
    }

    public boolean isPlayMultiple() {
        return playMultiple;
    }

    public void setPlayMultiple(boolean playMultiple) {
        this.playMultiple = playMultiple;
    }

    public boolean isPlaying() {
        return instance.audioPlayback.isPlaying();
    }

    public void setPendingIntentAct(PendingIntent mPendingIntent) {
        this.pendingIntent = mPendingIntent;
    }

    public void setShowPlayerNotification(boolean showPlayerNotification) {
        this.showPlayerNotification = showPlayerNotification;
    }

    public void setMediaList(List<MediaMetaData> currentAudioList) {
        if (this.mediaList != null) {
            this.mediaList.clear();
            this.mediaList.addAll(currentAudioList);
        }
    }

    public void clearList() {
        if (this.mediaList != null && mediaList.size() > 0) {
            this.mediaList.clear();
            this.index = 0;
            this.onPause();
        }
    }

    public void onResume(){
        try {
            audioPlayback.play(currentAudio);
            onSeekTo(lastSeekPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlay(MediaMetaData infoData) {
        Log.e(TAG, "this is play music");
        if (infoData == null) {
            return;
        }
        if (playMultiple && !isMediaListEmpty()) {
            index = mediaList.indexOf(infoData);
        }
        if (this.currentAudio != null && this.currentAudio.getMediaId().equalsIgnoreCase(infoData.getMediaId()) && instance.audioPlayback != null && instance.audioPlayback.isPlaying()) {
            onPause();
            //MusicActivity activity = (MusicActivity) context;
            //onStop();
            //this.currentAudio = null;

        } else {
            this.currentAudio = infoData;
            handlePlayRequest();
//            if (currentSessionCallback != null)
//                currentSessionCallback.playCurrent(index, currentAudio);
        }
    }

    @Override
    public void onPause() {

        handlePauseRequest();
    }

    @Override
    public void onStop() {
        handleStopRequest(null);
    }

    @Override
    public void onSeekTo(long position) {
        audioPlayback.seekTo((int) position);
    }

    @Override
    public int lastSeekPosition() {
        return (audioPlayback == null) ? 0 : (int) audioPlayback.getCurrentStreamPosition();
    }

    @Override
    public void onSkipToNext() {
        Log.e("NOTIFY ","next");
        try {
            //onPause();
            index = index + 1;
            isLoading = false;
            if(index!=this.mediaList.size())currentAudio =this.mediaList.get(index);
            else {
                currentAudio = this.mediaList.get(0);
            }
            handlePlayRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onSkipToPrevious() {
        Log.e("NOTIFY ","pre");
        try {
            //onPause();
            index = index - 1;
            isLoading = false;
            if(index!=-1) currentAudio = this.mediaList.get(index);
            else {
                currentAudio = this.mediaList.get(this.mediaList.size()-1);
            }
            handlePlayRequest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    public boolean isMediaListEmpty() {
        return (mediaList == null || mediaList.size() == 0);
    }

    /**
     * @param isIncremental
     * @return
     */
    private boolean isValidIndex(boolean isIncremental, int index) {
        if (isIncremental) {
            return (playMultiple && !isMediaListEmpty() && mediaList.size() > index);
        } else {
            return (playMultiple && !isMediaListEmpty() && index >= 0);
        }
    }

    public void handlePlayRequest() {
        Logger.d(TAG, "handlePlayRequest: mState=" + audioPlayback.getState());
        Log.e("handlePlayRequesr", "start");
        prefs = new Prefs(context);

        if(!isLoading){
            isLoading = true;
            if (audioPlayback != null && currentAudio != null) {

                if(!currentAudio.isCache()){
                    try {

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("https://vk.com") //Базовая часть адреса
                                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                                .build();
                        Api vogellaAPI = retrofit.create(Api.class);


                        String cookie = currentAudio.getMediaComposer();
                        Map<String, String> body = new HashMap();
                        body.put("act", "reload_audio");
                        body.put("al", "1");
                        body.put("ids", currentAudio.getMediaUrl());

                        Call<ResponseBody> call = vogellaAPI.alAudio(cookie, body);
                        call.enqueue(new Callback<ResponseBody>() {
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                                try {
                                    String lastPath = "";
                                    String path = "";
                                    String response = ((ResponseBody) res.body()).string();
                                    if (response.length() < 100) {
                                        handlePlayRequest();
                                    }else {
                                        path = decode(response.substring(response.indexOf("https"), response.indexOf("\",\"")).replace("\\", ""), Integer.parseInt(prefs.getID()));
                                        currentAudio.setMediaUrl(path);
                                        isLoading = false;
                                        audioPlayback.play(currentAudio);
                                        try {

                                            if(prefs.getAutosave()){
                                                Log.e(TAG, "autosave true");
                                                new NotificationTask(context, currentAudio.getMediaArtist()+"_"+currentAudio.getMediaTitle(), id, currentAudio)
                                                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 10);
                                                id++;
                                            }else Log.e(TAG, "autosave false");

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        try{System.out.print("PATH"+path);}catch (Exception e){}
                                        if (showPlayerNotification) {
                                            if (context != null) {
                                                Intent intent = new Intent(context, AudioStreamingService.class);
                                                context.startService(intent);
                                            } else {
                                                Intent intent = new Intent(context, AudioStreamingService.class);
                                                context.stopService(intent);
                                            }

                                            NotificationManager.getInstance().postNotificationName(NotificationManager.audioDidStarted, currentAudio);
                                            NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, getCurrentAudio().getMediaId());
                                            setPendingIntent();
                                        }
                                    }

                                } catch (Exception e) {
                                    String er = e.toString();
                                    //ThrowableExtension.printStackTrace(e);
                                }
                            }

                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                            }
                        });
                        //Prefs prefs = new Prefs(getBaseContext());
                        //final int id = prefs.getID();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }else {
                    isLoading = false;
                    currentAudio.setMediaUrl(currentAudio.getCache());
                    audioPlayback.play(currentAudio);

                    if (showPlayerNotification) {
                        if (context != null) {
                            Intent intent = new Intent(context, AudioStreamingService.class);
                            context.startService(intent);
                        } else {
                            Intent intent = new Intent(context, AudioStreamingService.class);
                            context.stopService(intent);
                        }

                        NotificationManager.getInstance().postNotificationName(NotificationManager.audioDidStarted, currentAudio);
                        NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, getCurrentAudio().getMediaId());
                        setPendingIntent();
                    }
                }
                if (currentSessionCallback != null)
                    currentSessionCallback.playCurrent(index, currentAudio);
            }
        }else Log.e("ERROR", "loading ........");


//
    }

    private void setPendingIntent(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pendingIntent != null) {
                    NotificationManager.getInstance().postNotificationName(NotificationManager.setAnyPendingIntent, pendingIntent);
                }
            }
        },400);
    }

    public void handlePauseRequest() {
        Logger.d(TAG, "handlePauseRequest: mState=" + audioPlayback.getState());
        if (audioPlayback != null && audioPlayback.isPlaying()) {
            audioPlayback.pause();
            if (showPlayerNotification) {
                NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, getCurrentAudio().getMediaId());
            }
        }
    }

    public void handleStopRequest(String withError) {
        Logger.d(TAG, "handleStopRequest: mState=" + audioPlayback.getState() + " error=", withError);
        audioPlayback.stop(true);
    }

    static class MyStatusCallback implements PlaybackListener.Callback {
        @Override
        public void onCompletion() {
            if (instance.playMultiple && !instance.isMediaListEmpty()) {
                if (instance.currentSessionCallback != null) {
                    instance.currentSessionCallback.playSongComplete();
                }
                instance.onSkipToNext();
            } else {
                instance.handleStopRequest(null);
            }
        }

        @Override
        public void onPlaybackStatusChanged(int state) {
            try {
                if (state == PlaybackStateCompat.STATE_PLAYING) {
                    instance.scheduleSeekBarUpdate();
                } else {
                    instance.stopSeekBarUpdate();
                }
                if (instance.currentSessionCallback != null) {
                    instance.currentSessionCallback.updatePlaybackState(state);
                }

                instance.mLastPlaybackState = state;
                if(instance.currentAudio!=null){
                    instance.currentAudio.setPlayState(state);
                }
                if (instance.showPlayerNotification) {
                    NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, instance.getCurrentAudio().getMediaId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(String error) {
            //TODO FOR ERROR
        }

        @Override
        public void setCurrentMediaId(String mediaId) {

        }

    }


    public void cleanupPlayer(Context context, boolean notify, boolean stopService) {
        cleanupPlayer(notify, stopService);
    }

    public void cleanupPlayer(boolean notify, boolean stopService) {
        handlePauseRequest();
        audioPlayback.stop(true);
        if (stopService) {
            Intent intent = new Intent(context, AudioStreamingService.class);
            context.stopService(intent);
        }
    }

    private ScheduledFuture<?> mScheduleFuture;
    public int mLastPlaybackState;
    private long currentPosition = 0;
    private final Handler mHandler = new Handler();
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public void scheduleSeekBarUpdate() {
        stopSeekBarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, PROGRESS_UPDATE_INITIAL_INTERVAL,
                    PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    public void stopSeekBarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    private void updateProgress() {
        if (instance.mLastPlaybackState == 0 || instance.mLastPlaybackState < 0) {
            return;
        }
        if (instance.mLastPlaybackState != PlaybackStateCompat.STATE_PAUSED && instance.currentSessionCallback != null) {
            instance.currentSessionCallback.currentSeekBarPosition((int) audioPlayback.getCurrentStreamPosition());
        }
    }

    private static String shiftArray(String[] array) {
        String result = array[0];
        System.arraycopy(array, 1, array, 0, array.length - 1);
        return result;
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

}
