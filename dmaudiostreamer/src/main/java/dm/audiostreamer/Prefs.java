package dm.audiostreamer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by root on 6.5.18.
 */

public class Prefs {

    Context context;
    private static final String APP_PREFERENCES = "config";
    private static final String APP_PREFERENCES_FIRST = "first_v";
    private static final String APP_PREFERENCES_ID = "id";
    private static final String APP_PREFERENCES_NAME = "name";
    private static final String APP_PREFERENCES_PHOTO = "photo";
    private static final String APP_PREFERENCES_REVIEW = "review";
    private static final String APP_PREFERENCES_AUTOSAVE = "autosave";
    private SharedPreferences mSettings;

    public Prefs(Context context) {
        this.context = context;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String  getCookie(){
        return mSettings.getString(APP_PREFERENCES_FIRST,"0");
    }

    public void setCookie(String cookie){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_FIRST, cookie);
        editor.apply();
    }

    public String getID(){
        return mSettings.getString(APP_PREFERENCES_ID,"0");
    }

    public void setID(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_ID, id);
        editor.apply();
    }
     public void setAutosave(boolean b){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_AUTOSAVE, b);
        editor.apply();
    }

    public boolean getAutosave(){
        //Log.e("PREFS", "ADS count = "+mSettings.getBoolean(APP_PREFERENCES_ADS_COUNT,true));
        return mSettings.getBoolean(APP_PREFERENCES_AUTOSAVE,true);
    }

}
