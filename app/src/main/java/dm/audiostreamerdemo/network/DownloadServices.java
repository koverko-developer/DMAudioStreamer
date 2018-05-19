package dm.audiostreamerdemo.network;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import dm.audiostreamer.App;
import dm.audiostreamer.AudioStreamingService;
import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.NotificationManager;
import dm.audiostreamer.NotificationTask;
import dm.audiostreamerdemo.AudioStreamerApplication;
import dm.audiostreamerdemo.R;
import dm.audiostreamerdemo.data.Prefs;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DownloadServices {

    private DownloadManager mgr=null;
    private long lastDownload=-1L;
    Context context;
    Prefs prefs;

    public DownloadServices(Context _context){
        this.context = _context;
        prefs = new Prefs(context);
        mgr=
                (DownloadManager)_context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public void startDownload( final String name, final int type, final MediaMetaData metaData){

        MediaMetaData currentAudio = metaData;

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
                        startDownload(name, type, metaData);
                    }else {
                        path = decode(response.substring(response.indexOf("https"), response.indexOf("\",\"")).replace("\\", ""), Integer.parseInt(prefs.getID()));
                        try {
                            Uri uri = Uri.parse(path);
                            File root = null;
                            String folder = "";
                            // 1 - cache  2-memory
                            if(type == 1){
                                folder = String.valueOf(Environment.getDownloadCacheDirectory()+"/vkmusic");
                            }else {
                                if(prefs.getPath().length()>2) folder = prefs.getPath();
                                else folder = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                            }
                            Uri pathe = null;
                            root = new File( folder+"/");
                            pathe = Uri.withAppendedPath(Uri.fromFile(root), name+".mp3");
                            DownloadManager.Request req = new DownloadManager.Request(uri);

                            req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                                    | DownloadManager.Request.NETWORK_MOBILE)
                                    .setAllowedOverRoaming(false)
                                    .setTitle(name)
                                    .setDescription(context.getResources().getString(R.string.download_loading))
                                    .setDestinationUri(pathe);
                            lastDownload = mgr.enqueue(req);

                            Toast.makeText(context, context.getResources().getString(R.string.download_start)+"\n"+
                                    context.getResources().getString(R.string.download_in)+" "+folder, Toast.LENGTH_SHORT).show();

                        }catch (Exception e){
                            Toast.makeText(context, context.getResources().getString(R.string.download_error), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
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




    }

    private static String STR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=";


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
