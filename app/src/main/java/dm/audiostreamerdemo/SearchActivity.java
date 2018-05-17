package dm.audiostreamerdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dm.audiostreamer.MediaMetaData;
import dm.audiostreamer.TypeAudio;
import dm.audiostreamerdemo.activity.MusicActivity;
import dm.audiostreamerdemo.adapter.AdapterMusic;
import dm.audiostreamerdemo.adapter.GroupsAdapter;
import dm.audiostreamerdemo.data.Prefs;
import dm.audiostreamerdemo.data.VKMusic;
import dm.audiostreamerdemo.network.Group;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    int typeAudio = 0;// 0 - search  // 1 - Новинки  // 2 - популярное // 3 - Спец // 4 - группы


    private ListView musicList;
    private AdapterMusic adapterMusic;
    List<MediaMetaData> listOfSongs = new ArrayList<>();
    Context context;
    Prefs prefs;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    GroupsAdapter audioAdapter;
    SearchActivity activity;


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        prefs = new Prefs(this);
        activity = this;
        context = this;
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        typeAudio = intent.getIntExtra("type", 0);
        initView();
        if(typeAudio == 0) getSupportActionBar().setTitle("Поиск");
        else if(typeAudio == 1) {
            getSupportActionBar().setTitle("Новинки");
            getNews();
        }
        else if(typeAudio == 2) {
            getSupportActionBar().setTitle("Популярное");
            getPopular();
        }
        else if(typeAudio == 3) {
            getSupportActionBar().setTitle("Специально для Вас");
            getSpecial();
        }
        else if(typeAudio == 4) {
            getSupportActionBar().setTitle("Группы");
            getUserGroups();
        }else if(typeAudio == 5) {
            getSupportActionBar().setTitle("КЭШ");
            //getCache();
        }else if(typeAudio == 6) {
            getSupportActionBar().setTitle("Избранное");
            getMusicFavorite();
        }


    }

    private void getMusicFavorite() {
    }



    public void initView(){

        progressBar = (ProgressBar) findViewById(R.id.search_progress);
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler);

        musicList = (ListView) findViewById(R.id.musicList);
        adapterMusic = new AdapterMusic(SearchActivity.this, new ArrayList<MediaMetaData>(), TypeAudio.AllCategory);
        adapterMusic.setListItemListener(new AdapterMusic.ListItemListener() {
            @Override
            public void onItemClickListener(MediaMetaData media, int position) {
                Log.e("STATE current music ", String.valueOf(media.getPlayState()));
                Intent intent = new Intent();
                ArrayList<MediaMetaData> ds = (ArrayList<MediaMetaData>) listOfSongs;
                intent.putExtra("list", ds);
//                intent.putExtra(MediaMetaData.class.getCanonicalName(), media);
                intent.putExtra("position", position);
                setResult(RESULT_OK, intent);
                finish();

            }
        });
        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        musicList.setAdapter(adapterMusic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        if(typeAudio == 0) myActionMenuItem.setVisible(true);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getAudioSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return true;
            }
        });

        return true;
    }

    private void getAudioSearch(String q){
        //newList.clear();
        progressBar.setVisibility(View.VISIBLE);
        String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", String.valueOf(prefs.getID()));
        body.put("search_q", q);
        body.put("offset", "0");
        //body.put("is_loading_all", "1");
        body.put("act", "load_section");
        //body.put("section", "all");
        body.put("al", "1");
        body.put("type", "search");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs = new VKMusic(SearchActivity.this).preparePlaylist(b, prefs.getCookie(), TypeAudio.AllCategory);
                    adapterMusic.refresh(listOfSongs);
                    progressBar.setVisibility(View.GONE);
                    //String sd = "";
                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    private void getNews(){
        //newList.clear();
        progressBar.setVisibility(View.VISIBLE);
        String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", prefs.getID());
        body.put("offset", "0");
        body.put("al", "1");
        body.put("act", "load_section");
        body.put("type", "recoms");
        body.put("playlist_id", "recomsPUkLGlpXADkvD0tMBABHRDYKDhNqQBIWI0lTVFZVHwcqBA5USA");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs  = new VKMusic(SearchActivity.this).preparePlaylist(b, prefs.getCookie(), TypeAudio.AllCategory);
                    adapterMusic.refresh(listOfSongs);
                    progressBar.setVisibility(View.GONE);
                    String sd = "";
                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void getPopular(){
        //newList.clear();
        progressBar.setVisibility(View.VISIBLE);
        String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", prefs.getID());
        body.put("offset", "0");
        body.put("al", "1");
        body.put("act", "load_section");
        body.put("type", "recoms");
        body.put("playlist_id", "recomsPUkLGlpXADkvD0tMDRhJFicMDClBTRsDZFFLFVRACgopDEsL");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs  = new VKMusic(SearchActivity.this).preparePlaylist(b, prefs.getCookie(), TypeAudio.AllCategory);
                    adapterMusic.refresh(listOfSongs);
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void getSpecial(){
        //newList.clear();
        progressBar.setVisibility(View.VISIBLE);
        String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", prefs.getID());
        body.put("offset", "0");
        body.put("al", "1");
        body.put("act", "load_section");
        body.put("type", "recoms");
        body.put("playlist_id", "recomsPUkLGlpXADkvD0tMBBhJFicMDClBTRsDZFFLFVRACgopDEsL");
        AudioStreamerApplication.getApi().alAudio(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String b = response.body().string();
                    listOfSongs  = new VKMusic(SearchActivity.this).preparePlaylist(b, prefs.getCookie(), TypeAudio.AllCategory);
                    adapterMusic.refresh(listOfSongs);
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void getUserGroups() {

        String cookie = CookieManager.getInstance().getCookie("https://vk.com");
        Map<String, String> body = new HashMap();
        body.put("act", "get_list");
        body.put("mid", String.valueOf(prefs.getID()));
        body.put("al", "1");
        body.put("tab", "groups");
        AudioStreamerApplication.getApi().getGroups(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {


                Exception e;
                JSONArray songJson = new JSONArray();

                try {
                    String response = ((ResponseBody) result.body()).string();
                    String doc = response.substring(response.indexOf("<!json>")+7, response.length());
                    //doc = "{\"list\":"+doc+"}";
                    //String er = doc.substring(1447, 1453);
                    songJson = new JSONArray(doc);
                    //JSONObject me = new JSONObject(Html.fromHtml(doc).toString());
                    //songJson = me.getJSONArray("list");

                    int size = songJson.length();
                    ArrayList<Group> songList = new ArrayList();
                    for (int i = 0; i < size; i++) {
                        JSONArray jsonSong = songJson.getJSONArray(i);
                        Group f = new Group();
                        f.setId(jsonSong.getString(2));
                        f.setImg(jsonSong.getString(4));
                        f.setName(jsonSong.getString(0));
                        String dd = "";

                        songList.add(f);

                    }

                    String d = "";
                    progressBar.setVisibility(View.GONE);
                    setUpRecycler(songList);

                } catch (Exception e2) {
                    progressBar.setVisibility(View.GONE);
                    e = e2;
                }

            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
        return;


    }

    public void getWallAudio(final String id){


        progressBar.setVisibility(View.VISIBLE);
        final String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", "-"+id);
        body.put("offset", "0");
        body.put("act", "get_wall");
        //body.put("section", "all");
        body.put("al", "1");
        body.put("type", "own");
        body.put("wall_start_from", "0");
        AudioStreamerApplication.getApi().getWall(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    List<MediaMetaData> list = new ArrayList<>();
                    String b = response.body().string();
                    String dc = b.substring(b.indexOf("<div"), b.lastIndexOf("/div>"));
                    Document document = Jsoup.parse("<html>"+dc+"</html>");
                    Elements elements = document.select("div.audio_row_with_cover");
                    //Elements elements = document.select("div.audio_row__inner");

                    for(int i =0; i<elements.size(); i++){
                        Element element = elements.get(i);
                        String id_crash  = element.attr("data-audio").toString();
                        String name = element.select("a.audio_row__performer").text().toString()+
                                "-"+element.select("span.audio_row__title_inner").text().toString();
                        String id = id_crash.split(",")[1] + id_crash.split(",")[0];
                        id = id.replace("[","_");
                        String dur_crash = id_crash.split(",")[5];
                        MediaMetaData audio = new MediaMetaData();
                        audio.setMediaId(String.valueOf(i));
                        audio.setMediaArtist(element.select("a.audio_row__performer").text().toString());
                        audio.setMediaTitle(element.select("span.audio_row__title_inner").text().toString());
                        audio.setMediaUrl(id);
                        audio.setTypeAudio(TypeAudio.AllCategory);
                        audio.setMediaComposer(cookie);
                        audio.setMediaDuration(dur_crash);
                        list.add(audio);

                    }

                    if(list.size()==0){
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "На стене группы нет аудиозаписей...",Toast.LENGTH_SHORT).show();
                    }else {
                        getWallAudio2(id, list);
                    }

                    int i = elements.size();
                    String sd = "";
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(MainActivity.this, "Аудиозаписи видны только владельцу страницы...",Toast.LENGTH_SHORT).show();
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
    public void getWallAudio2(String id, final List<MediaMetaData> arr){
        progressBar.setVisibility(View.VISIBLE);
        final String cookie = CookieManager.getInstance().getCookie("https://vk.com");

        Map<String, String> body = new HashMap<>();
        body.put("access_hash", "");
        body.put("owner_id", "-"+id);
        body.put("offset", "10");
        body.put("act", "get_wall");
        //body.put("section", "all");
        body.put("al", "1");
        body.put("type", "own");
        body.put("wall_start_from", "10");
        AudioStreamerApplication.getApi().getWall(cookie, body).enqueue(new Callback<ResponseBody>() {
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    List<MediaMetaData> list = new ArrayList<>();
                    ArrayList<Group> nul = new ArrayList<>();
                    list.addAll(arr);
                    String b = response.body().string();
                    String dc = b.substring(b.indexOf("<div"), b.lastIndexOf("/div>"));
                    Document document = Jsoup.parse("<html>"+dc+"</html>");
                    Elements elements = document.select("div.audio_row_with_cover");
                    //Elements elements = document.select("div.audio_row__inner");

                    for(int i =0; i<elements.size(); i++){
                        Element element = elements.get(i);
                        String id_crash  = element.attr("data-audio").toString();
                        String name = element.select("a.audio_row__performer").text().toString()+
                                "-"+element.select("span.audio_row__title_inner").text().toString();
                        String id = id_crash.split(",")[1] + id_crash.split(",")[0];
                        String dur_crash = id_crash.split(",")[5];
                        id = id.replace("[","_");
                        MediaMetaData audio = new MediaMetaData();
                        audio.setMediaId(String.valueOf(i));
                        audio.setMediaArtist(element.select("a.audio_row__performer").text().toString());
                        audio.setMediaTitle(element.select("span.audio_row__title_inner").text().toString());
                        audio.setMediaUrl(id);
                        audio.setTypeAudio(TypeAudio.AllCategory);
                        audio.setMediaComposer(cookie);
                        audio.setMediaDuration(dur_crash);
                        list.add(audio);

                    }

                    if(list.size()==0){
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SearchActivity.this, "На стене группы нет аудиозаписей...",Toast.LENGTH_SHORT).show();
                    }else {
                        progressBar.setVisibility(View.GONE);
                        listOfSongs  = list;
                        adapterMusic.refresh(listOfSongs);
                    }

                    int i = elements.size();
                    String sd = "";
                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    //Toast.makeText(MainActivity.this, "Аудиозаписи видны только владельцу страницы...",Toast.LENGTH_SHORT).show();
                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }
   

    private void gethashGroup(String b, String id) {

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
                    //String sd = "";
                } catch (Exception e) {

                }
            }

            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });

    }

    private void setUpRecycler(final ArrayList<Group> arrayList){
        recyclerView.setVisibility(View.VISIBLE);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        audioAdapter = new GroupsAdapter(arrayList, activity);
        recyclerView.setAdapter(audioAdapter);

        audioAdapter.setOnItemClickListener(new GroupsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                activity.getWallAudio(arrayList.get(position).getId());
                recyclerView.setVisibility(View.GONE);
            }

            @Override
            public void onSongItemDeleteClicked(int position) {

            }
        });

    }

}
