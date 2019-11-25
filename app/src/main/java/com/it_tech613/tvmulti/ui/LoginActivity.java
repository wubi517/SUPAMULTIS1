package com.it_tech613.tvmulti.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.tvmulti.BuildConfig;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class LoginActivity extends AppCompatActivity  implements View.OnClickListener{

    private EditText name_txt, pass_txt;
    private ProgressBar progressBar;
    private String user;
    private String password;
    private CheckBox checkBox;
    boolean is_remember = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .penaltyLog()
                .detectAll()
                .build());

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (MyApp.instance.getPreference().get(Constants.MAC_ADDRESS) == null) {
            MyApp.mac_address = Utils.getPhoneMac(LoginActivity.this);
            MyApp.instance.getPreference().put(Constants.MAC_ADDRESS, MyApp.mac_address.toUpperCase());
        } else
            MyApp.mac_address = (String) MyApp.instance.getPreference().get(Constants.MAC_ADDRESS);

        RelativeLayout main_lay = findViewById(R.id.main_lay);
        Bitmap myImage = getBitmapFromURL(Constants.GetLoginImage(LoginActivity.this));
        Drawable dr = new BitmapDrawable(myImage);
        main_lay.setBackgroundDrawable(dr);
        progressBar = findViewById(R.id.login_progress);
        name_txt = findViewById(R.id.login_name);
        pass_txt =  findViewById(R.id.login_pass);
        TextView phone = findViewById(R.id.phone);
        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnClickListener(this);

        if(MyApp.instance.getPreference().get(Constants.getLoginInfo())!=null){
            LoginModel loginModel = (LoginModel) MyApp.instance.getPreference().get(Constants.getLoginInfo());
            user = loginModel.getUser_name();
            password = loginModel.getPassword();
            name_txt.setText(user);
            pass_txt.setText(password);
            checkBox.setChecked(true);
            new Thread(this::callLogin).start();
        }
        if (BuildConfig.DEBUG){
            name_txt.setText("Franco62");
            pass_txt.setText("62Franco");
        }

        TextView mac_txt = findViewById(R.id.login_mac_address);
        mac_txt.setText(MyApp.mac_address);
        TextView version_txt =  findViewById(R.id.app_version_code);
        MyApp.version_str = "v " + MyApp.version_name;
        version_txt.setText(MyApp.version_str);
        findViewById(R.id.login_btn).setOnClickListener(this);
        ImageView logo = (ImageView) findViewById(R.id.logo);
        Glide.with(LoginActivity.this)
                .load(Constants.GetIcon(LoginActivity.this))
                .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon_default).signature(new ObjectKey("myKey5")))
                .into(logo);
        logo.setOnClickListener(this);
    }

    private void callLogin() {
        try {
            runOnUiThread(()->progressBar.setVisibility(View.VISIBLE));
            long startTime = System.nanoTime();
            String responseBody = MyApp.instance.getIptvclient().authenticate(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e(getClass().getSimpleName(),"responseBody: " + responseBody);
            Log.e("BugCheck","authenticate success "+MethodeDuration);
            JSONObject map = new JSONObject(responseBody);
            MyApp.user = user;
            MyApp.pass = password;
            JSONObject u_m;
            try {
                u_m = map.getJSONObject("user_info");
                if (!u_m.has("username")) {
                    runOnUiThread(()->{
                        Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.GONE);
                    });
                } else {
                    MyApp.created_at = u_m.getString("created_at");
                    MyApp.status = u_m.getString("status");
                    if(!MyApp.status.equalsIgnoreCase("Active")){
                        runOnUiThread(()->{
                            Intent intent =new Intent(LoginActivity.this,EmptyActivity.class);
                            intent.putExtra("msg","Your account is Expired");
                            startActivity(intent);
                        });
                        return;
                    }
                    MyApp.is_trail = u_m.getString("is_trial");
                    MyApp.active_cons = u_m.getString("active_cons");
                    MyApp.max_cons = u_m.getString("max_connections");
                    String exp_date;
                    try{
                        exp_date = u_m.getString("exp_date");
                    }catch (Exception e){
                        exp_date = "unlimited";
                    }
                    LoginModel loginModel = new LoginModel();
                    loginModel.setUser_name(MyApp.user);
                    loginModel.setPassword(MyApp.pass);
                    try{
                        loginModel.setExp_date(exp_date);
                    }catch (Exception e){
                        loginModel.setExp_date("unlimited");
                    }
                    MyApp.loginModel = loginModel;
                    Log.e("remember",String.valueOf(is_remember));
                    if(checkBox.isChecked()){
                        MyApp.instance.getPreference().put(Constants.getLoginInfo(), loginModel);
                    }
                    JSONObject serverInfo= map.getJSONObject("server_info");
                    String  my_timestamp= serverInfo.getString("timestamp_now");
                    String server_timestamp= serverInfo.getString("time_now");
                    Constants.setServerTimeOffset(my_timestamp,server_timestamp);
                    callVodCategory();
                }
            } catch (JSONException e) {
                runOnUiThread(()->{
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Username is incorrect", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLogin()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN UNSUCCESSFUL PLEASE CHECK YOUR LOGIN DETAILS OR CONTACT YOUR PROVIDER");
                connectionDlg.show();
            });
        }
    }

    private void callVodCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getMovieCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovieCategories success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<CategoryModel> categories;
                categories = new ArrayList<>();
                categories.add(getRecentMovies());
                categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
                categories.add(getFavoriteCategory());
                categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
                MyApp.vod_categories = categories;
            }catch (Exception e){
                e.printStackTrace();
            }
            callLiveCategory();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callVodCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private CategoryModel getFavoriteCategory() {
        CategoryModel categoryModel=new CategoryModel(Constants.fav_id,Constants.Favorites,"");
        List<MovieModel> favMovies = (List<MovieModel>) MyApp.instance.getPreference().get(Constants.getFAV_VOD_MOVIES());
        if (favMovies!=null){
            MyApp.favMovieModels=favMovies;
            categoryModel.setMovieModels(favMovies);
        }
        else {
            MyApp.favMovieModels=new ArrayList<>();
            categoryModel.setMovieModels(new ArrayList<>());
        }
        return categoryModel;
    }

    private CategoryModel getRecentMovies() {
        CategoryModel recentCategory = new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,"");
        List<MovieModel> recentMovies=(List<MovieModel>) MyApp.instance.getPreference().get(Constants.getRecentMovies());
        if (recentMovies!=null){
            MyApp.recentMovieModels=recentMovies;
            recentCategory.setMovieModels(recentMovies);
        }
        else {
            MyApp.recentMovieModels=new ArrayList<>();
            recentCategory.setMovieModels(new ArrayList<>());
        }
        return recentCategory;
    }

    private void callLiveCategory(){
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveCategories(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveCategories success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<CategoryModel> categories;
                categories = new ArrayList<>();
                categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
                categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
                categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
                MyApp.live_categories = categories;
                for (CategoryModel categoryModel: categories){
                    String category_name = categoryModel.getName().toLowerCase();
                    if(category_name.contains("adult")||category_name.contains("xxx")){
                        Constants.xxx_category_id = categoryModel.getId();
                        Log.e("LoginActivity","xxx_category_id: "+Constants.xxx_category_id);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            callSeriesCategory();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private void callSeriesCategory(){
        try {
            long startTime = System.nanoTime();
            String map = MyApp.instance.getIptvclient().getSeriesCategories(user,password);
            long endTime = System.nanoTime();
            long MethodeDuration = (endTime - startTime);
            //Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeriesCategories success "+MethodeDuration);
            Gson gson=new Gson();
            map = map.replaceAll("[^\\x00-\\x7F]", "");
            List<CategoryModel> categories;
            categories = new ArrayList<>();
            categories.add(new CategoryModel(Constants.recent_id,Constants.Recently_Viewed,""));
            categories.add(new CategoryModel(Constants.all_id,Constants.All,""));
            categories.add(new CategoryModel(Constants.fav_id,Constants.Favorites,""));
            categories.add(new CategoryModel(Constants.no_name_id,Constants.No_Name_Category,""));
            try {
                categories.addAll(gson.fromJson(map, new TypeToken<List<CategoryModel>>(){}.getType()));
            }catch (Exception e){

            }
            MyApp.series_categories = categories;
            callLiveStreams();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeriesCategory()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }

    }

    private void callLiveStreams(){
        try{
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getLiveStreams(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
           // Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getLiveStreams success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<EPGChannel> epgChannels=new ArrayList<>();
                try{
                    epgChannels = new ArrayList<>(gson.fromJson(map, new TypeToken<List<EPGChannel>>() {}.getType()));
                }catch (Exception e){
                    JSONArray response;
                    try {
                        response=new JSONArray(map);
                        for (int i=0;i<response.length();i++){
                            JSONObject jsonObject=response.getJSONObject(i);
                            EPGChannel epgChannel=new EPGChannel();
                            try{
                                epgChannel.setNumber(jsonObject.getString("num"));
                            }catch (JSONException e1){
                                epgChannel.setNumber("");
                            }
                            try{
                                epgChannel.setName(jsonObject.getString("name"));
                            }catch (JSONException e2){
                                epgChannel.setName("");
                            }
                            try{
                                epgChannel.setStream_type(jsonObject.getString("stream_type"));
                            }catch (JSONException e3){
                                epgChannel.setStream_type("");
                            }
                            try{
                                epgChannel.setStream_id(jsonObject.getInt("stream_id"));
                            }catch (JSONException e4){
                                epgChannel.setStream_id(-1);
                            }
                            try{
                                epgChannel.setImageURL(jsonObject.getString("stream_icon"));
                            }catch (JSONException e5){
                                epgChannel.setImageURL("");
                            }
                            try{
                                epgChannel.setChannelID(jsonObject.getInt("epg_channel_id"));
                            }catch (JSONException e1){
                                epgChannel.setChannelID(-1);
                            }
                            try{
                                epgChannel.setAdded(jsonObject.getString("added"));
                            }catch (JSONException e1){
                                epgChannel.setAdded("");
                            }
                            try{
                                epgChannel.setCategory_id(jsonObject.getInt("category_id"));
                                if (epgChannel.getCategory_id()==Constants.xxx_category_id)
                                    epgChannel.setIs_locked(true);
                                else epgChannel.setIs_locked(false);
                            }catch (JSONException e1){
                                epgChannel.setCategory_id(-1);
                            }
                            try{
                                epgChannel.setCustom_sid(jsonObject.getString("custom_sid"));
                            }catch (JSONException e1){
                                epgChannel.setCustom_sid("");
                            }
                            try{
                                epgChannel.setTv_archive(jsonObject.getInt("tv_archive"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive(0);
                            }
                            try{
                                epgChannel.setDirect_source(jsonObject.getString("direct_source"));
                            }catch (JSONException e1){
                                epgChannel.setDirect_source("");
                            }
                            try{
                                epgChannel.setTv_archive_duration(jsonObject.getString("tv_archive_duration"));
                            }catch (JSONException e1){
                                epgChannel.setTv_archive_duration("0");
                            }
                            epgChannels.add(epgChannel);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
                MyApp.channel_size = epgChannels.size();
                Map<String, List<EPGChannel>> back_map = new HashMap<String, List<EPGChannel>>();
                back_map.put("channels", epgChannels);
                MyApp.backup_map = back_map;
                List<FullModel> fullModels = new ArrayList<>();

                fullModels.add(new FullModel(Constants.recent_id, getRecentChannels(epgChannels), Constants.Recently_Viewed,1));
                fullModels.add(new FullModel(Constants.all_id, epgChannels,Constants.All,0));
                fullModels.add(new FullModel(Constants.fav_id,getFavoriteChannels(epgChannels),Constants.Favorites,0));
                List<String> datas = new ArrayList<>();
                datas.add("All Channel");
                datas.add(Constants.Favorites);
                for(int i = Constants.unCount_number; i< MyApp.live_categories.size(); i++){
                    int category_id = MyApp.live_categories.get(i).getId();
                    String category_name = MyApp.live_categories.get(i).getName();
                    int count =0;
                    List<EPGChannel> chModels = new ArrayList<>();
                    for(int j = 0; j< epgChannels.size(); j++){
                        EPGChannel chModel = epgChannels.get(j);
                        if(category_id==chModel.getCategory_id()){
                            chModels.add(chModel);
                            if (chModel.getTv_archive()==1) count+=1;
                        }
                    }
//                    if(chModels.size()==0){
//                        continue;
//                    }
                    datas.add(MyApp.live_categories.get(i).getName());
                    fullModels.add(new FullModel(category_id,chModels,category_name,count));
                    Log.e("catchable_count",count+"");
                }
                MyApp.fullModels = fullModels;
                MyApp.maindatas = datas;
                int count_catchable=0;
                for (int i=Constants.unCount_number;i<MyApp.fullModels.size();i++){
                    count_catchable+=MyApp.fullModels.get(i).getCatchable_count();
                }
                Constants.getAllFullModel(MyApp.fullModels).setCatchable_count(count_catchable);
                Log.e("total catchable_count",count_catchable+"");
            }catch (Exception e){

            }
            callSeries();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callLiveStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }

    }

    private void callSeries() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getSeries(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getSeries success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<SeriesModel> allSeriesModels = new ArrayList<>();
                try {
                    JSONArray jsonArray = new JSONArray(map);
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        try {
                            SeriesModel seriesModel = gson.fromJson(jsonObject.toString(),SeriesModel.class);
                            allSeriesModels.add(seriesModel);
                        }catch (Exception ignored){}
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//            allSeriesModels.addAll((Collection<? extends SeriesModel>) gson.fromJson(map, new TypeToken<List<SeriesModel>>(){}.getType()));
                MyApp.seriesModels = allSeriesModels;
                putRecentSeriesModels(allSeriesModels);
                putFavSeriesModels(allSeriesModels);
                Constants.getRecentCatetory(MyApp.series_categories).setSeriesModels(MyApp.recentSeriesModels);
                Constants.getAllCategory(MyApp.series_categories).setSeriesModels(allSeriesModels);
                Constants.getFavoriteCatetory(MyApp.series_categories).setSeriesModels(MyApp.favSeriesModels);
                Constants.putSeries(allSeriesModels);
            }catch (Exception e){

            }

            callMovieStreams();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callSeries()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }

    }

    private void putRecentSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentSeries());
        MyApp.recentSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.recentSeriesModels.add(seriesModel);
                            break;
                        }
                    }
                }
            Log.e("recent_series_models",MyApp.recentSeriesModels.size()+" seriesRecent "+fav_series_names.size()+" names");
        }
    }

    private void putFavSeriesModels(List<SeriesModel> allSeriesModels) {
        List<String> fav_series_names=(List<String>) MyApp.instance.getPreference().get(Constants.getFAV_SERIES());
        MyApp.favSeriesModels=new ArrayList<>();
        if (fav_series_names!=null){
            if (fav_series_names.size()!=0)
                for (SeriesModel seriesModel:allSeriesModels){
                    for (String name:fav_series_names){
                        if (name.equals(seriesModel.getName())) {
                            MyApp.favSeriesModels.add(seriesModel);
                            seriesModel.setIs_favorite(true);
                            break;
                        }
                    }
                }
            Log.e("fav_series_models",MyApp.favSeriesModels.size()+" seriesFav "+fav_series_names.size()+" names");
        }
    }

    private void callMovieStreams() {
        try {
            long startTime = System.nanoTime();
//api call here
            String map = MyApp.instance.getIptvclient().getMovies(user,password);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
//            Log.e(getClass().getSimpleName(),map);
            Log.e("BugCheck","getMovies success "+MethodeDuration);
            try {
                Gson gson=new Gson();
                map = map.replaceAll("[^\\x00-\\x7F]", "");
                List<MovieModel> movieModels = new ArrayList<>(gson.fromJson(map, new TypeToken<List<MovieModel>>() {}.getType()));
                MyApp.movieModels = movieModels;
                Constants.getAllCategory(MyApp.vod_categories).setMovieModels(movieModels);
                Constants.putMovies(movieModels);
            }catch (Exception e){

            }
            getAuthorization();
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(LoginActivity.this, new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnRetryClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callMovieStreams()).start();
                    }

                    @Override
                    public void OnHelpClick(Dialog dialog) {
                        startActivity(new Intent(LoginActivity.this, ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA");
                connectionDlg.show();
            });
        }
    }

    private List<EPGChannel> getRecentChannels(List<EPGChannel> epgChannels){
        List<EPGChannel> recentChannels=new ArrayList<>();

        if(MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS())!=null){
            List<String> recent_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getRecentChannels());
            for(int j=0;j< recent_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(recent_channel_names.get(j))){
                        recentChannels.add(epgChannels.get(i));
                    }
                }
            }
        }
        return recentChannels;
    }

    private List<EPGChannel> getFavoriteChannels(List<EPGChannel> epgChannels){
        List<EPGChannel> favChannels=new ArrayList<>();

        if(MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS())!=null){
            List<String> fav_channel_names=(List<String>) MyApp.instance.getPreference().get(Constants.getFAV_LIVE_CHANNELS());
            for(int j=0;j< fav_channel_names.size();j++){
                for(int i = 0;i<epgChannels.size();i++){
                    if(epgChannels.get(i).getName().equals(fav_channel_names.get(j))){
                        epgChannels.get(i).setIs_favorite(true);
                        favChannels.add(epgChannels.get(i));
                    }else {
                        epgChannels.get(i).setIs_favorite(false);
                    }
                }
            }
        }
        Log.e("fav_live_streams_models",favChannels.size()+"");
        return favChannels;
    }
    public Bitmap getBitmapFromURL(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getAuthorization(){
        try {
            long startTime = System.nanoTime();
//api call here
            String response = MyApp.instance.getIptvclient().auth1(Constants.GetKey(this));
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            Log.e("BugCheck","auth1 success "+MethodeDuration);
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("status")) {
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(LoginActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_btn:
                if (name_txt.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "User name cannot be blank.", Toast.LENGTH_LONG).show();
                    return;
                } else if (pass_txt.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Password cannot be blank.", Toast.LENGTH_LONG).show();
                    return;
                }
                user = name_txt.getText().toString();
                password = pass_txt.getText().toString();
                new Thread(this::callLogin).start();
                break;
            case R.id.checkbox:
                is_remember = checkBox.isChecked();
                break;
            case R.id.logo:
                showVpnPinDlg();
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        View view = getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    showVpnPinDlg();
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private void showVpnPinDlg() {
        VPNPinDlg pinDlg = new VPNPinDlg(LoginActivity.this, new VPNPinDlg.DlgPinListener() {
            @Override
            public void OnYesClick(Dialog dialog, String pin_code) {
                String pin = (String )MyApp.instance.getPreference().get(Constants.getDIRECT_VPN_PIN_CODE());
                if(pin_code.equalsIgnoreCase(pin)){
                    dialog.dismiss();
                    startActivity(new Intent(LoginActivity.this,VpnActivity.class));
                }else {
                    Toast.makeText(LoginActivity.this, "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void OnCancelClick(Dialog dialog, String pin_code) {
                dialog.dismiss();
            }
        });
        pinDlg.show();
    }
}
