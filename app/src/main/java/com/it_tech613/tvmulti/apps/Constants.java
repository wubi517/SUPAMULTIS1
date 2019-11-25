package com.it_tech613.tvmulti.apps;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.it_tech613.tvmulti.BuildConfig;
import com.it_tech613.tvmulti.models.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Constants {
    static final String APP_INFO = "app_info";
    private static final String DIRECT_VPN_PIN_CODE = "direct_vpn_pin_code";
    public static final String MAC_ADDRESS = "mac_addr";
    public static int xxx_category_id =-1;

    public static String Recently_Viewed = "Recently Viewed";
    public static String All="All";
    public static String Favorites="Favorites";
    public static String No_Name_Category="No Name Category";

    public static int recent_id = 10000;
    public static int all_id = 1000;
    public static int fav_id = 2000;
    public static int no_name_id = 3000;

    public static int unCount_number = 3;

    private static final String CHANNEL_POS = "channel_pos";
    private static final String CATEGORY_POS = "channel_pos";

    private static final String LOGIN_INFO = "login_info";
    private static final String FAV_INFO = "user_info";
    private static final String MEDIA_POSITION = "media_position";
    private static final String SERIES_POSITION = "seris_position";
    private static final String MOVIE_FAV = "movie_app";

    private static final String PIN_CODE = "pin_code";
    private static final String OSD_TIME = "osd_time";
    private static final String INVISIBLE_VOD_CATEGORIES = "invisible_vod_categories";
    private static final String INVISIBLE_LIVE_CATEGORIES = "invisible_live_categories";
    private static final String INVISIBLE_SERIES_CATEGORIES = "invisible_series_categories";

    private static final String FAV_LIVE_CHANNELS = "live_fav";
    private static final String FAV_VOD_MOVIES = "vod_fav";
    private static final String FAV_SERIES = "series_fav";

    private static final String RECENT_CHANNELS="RECENT_CHANNELS";
    private static final String RECENT_MOVIES="RECENT_MOVIES";
    private static final String RECENT_SERIES="RECENT_SERIES";

    private static final String CURRENT_PLAYER = "current_player";
    private static final String CURRENT_SORTING = "CURRENT_SORTING";
    public static SimpleDateFormat catchupFormat = new SimpleDateFormat("yyyy-MM-dd:HH-mm");
    public static SimpleDateFormat stampFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat clockFormat=new SimpleDateFormat("HH:mm");
    public static SimpleDateFormat clock12Format=new SimpleDateFormat("hh:mm");
    public static SimpleDateFormat epgFormat = new SimpleDateFormat("yyyyMMddHHmmss Z");
    public static long SEVER_OFFSET;
    public static DataModel userDataModel;

    public static String getCurrentPlayer(){
        return CURRENT_PLAYER+MyApp.firstServer.getValue();
    }

    public static String getCurrentSorting(){
        return CURRENT_SORTING+MyApp.firstServer.getValue();
    }

    public static String GetPin4(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("four_way_screen","");
//        return  app_icon;
        return userDataModel.getPin_4();
    }

    public static String GetPin3(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("tri_screen","");
//        return  app_icon;
        return userDataModel.getPin_3();
    }

    public static String GetPin2(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("dual_screen","");
//        return  app_icon;
        return userDataModel.getPin_2();
    }

    public static String GetVPN_IP(Context mContext){
//        SharedPreferences serveripdetails = mContext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        return serveripdetails.getString("vpn_ip","");
        return userDataModel.getVpn_ip();
    }

    public static int GetSlideTime(Context mcontext) {
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        return serveripdetails.getInt("slider_time",5);
        return Integer.parseInt(userDataModel.getSlider_time());
    }

    public static String getCHANNEL_POS() {
        return CHANNEL_POS +MyApp.firstServer.getValue();
    }

    public static String getCATEGORY_POS() {
        return CATEGORY_POS +MyApp.firstServer.getValue();
    }

    public static String getDIRECT_VPN_PIN_CODE() {
        return DIRECT_VPN_PIN_CODE +MyApp.firstServer.getValue();
    }

    public static String getSERIES_POSITION() {
        return SERIES_POSITION +MyApp.firstServer.getValue();
    }

    public static String getFAV_SERIES() {
        return FAV_SERIES+MyApp.firstServer.getValue();
    }

    public static String getRecentChannels(){
        return RECENT_CHANNELS + MyApp.firstServer.getValue();
    }

    public static String getRecentMovies(){
        return RECENT_MOVIES + MyApp.firstServer.getValue();
    }

    public static String getRecentSeries(){
        return RECENT_SERIES + MyApp.firstServer.getValue();
    }

    public static String getFAV_VOD_MOVIES() {
        return FAV_VOD_MOVIES+MyApp.firstServer.getValue();
    }

    public static String getFAV_LIVE_CHANNELS() {
        return FAV_LIVE_CHANNELS+MyApp.firstServer.getValue();
    }

    public static String getINVISIBLE_SERIES_CATEGORIES() {
        return INVISIBLE_SERIES_CATEGORIES+MyApp.firstServer.getValue();
    }

    public static String getINVISIBLE_LIVE_CATEGORIES() {
        return INVISIBLE_LIVE_CATEGORIES+MyApp.firstServer.getValue();
    }

    public static String getINVISIBLE_VOD_CATEGORIES() {
        return INVISIBLE_VOD_CATEGORIES+MyApp.firstServer.getValue();
    }

    public static String getPIN_CODE() {
        return PIN_CODE+MyApp.firstServer.getValue();
    }

    public static String getOSD_TIME() {
        return OSD_TIME+MyApp.firstServer.getValue();
    }

    public static String getMEDIA_POSITION() {
        return MEDIA_POSITION+MyApp.firstServer.getValue();
    }

    public static String getMOVIE_FAV() {
        return MOVIE_FAV+MyApp.firstServer.getValue();
    }

    public static void setServerTimeOffset(String my_timestamp,String server_timestamp) {
        Log.e("server_timestamp",server_timestamp);
        try {
            long my_time= Long.parseLong(my_timestamp);
            Date date_server= stampFormat.parse(server_timestamp);
            my_time=my_time*1000;
            SEVER_OFFSET=my_time-date_server.getTime();
            Log.e("offset",String.valueOf(SEVER_OFFSET));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String Offset(boolean is_12,String string){
        try {
            Date that_date=stampFormat.parse(string);
            that_date.setTime(that_date.getTime()+ Constants.SEVER_OFFSET);
            if (is_12)return clock12Format.format(that_date);
            else return clockFormat.format(that_date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static CategoryModel getFavoriteCatetory(List<CategoryModel> categoryModels){
        for (CategoryModel categoryModel:categoryModels){
            if (categoryModel.getId()==Constants.fav_id)
                return categoryModel;
        }
        return null;
    }

    public static CategoryModel getRecentCatetory(List<CategoryModel> categoryModels){
        for (CategoryModel categoryModel:categoryModels){
            if (categoryModel.getId()==Constants.recent_id)
                return categoryModel;
        }
        return null;
    }

    public static CategoryModel getAllCategory(List<CategoryModel> categoryModels){
        for (CategoryModel categoryModel:categoryModels){
            if (categoryModel.getId()==Constants.all_id)
                return categoryModel;
        }
        return null;
    }

    public static FullModel getAllFullModel(List<FullModel> fullModels){
        for (FullModel fullModel:fullModels){
            if (fullModel.getCategory_id()==Constants.all_id)
                return fullModel;
        }
        return null;
    }

    public static FullModel getRecentFullModel(List<FullModel> fullModels){
        for (FullModel fullModel:fullModels){
            if (fullModel.getCategory_id()==Constants.recent_id)
                return fullModel;
        }
        return null;
    }

    public static FullModel getFavFullModel(List<FullModel> fullModels){
        for (FullModel fullModel:fullModels){
            if (fullModel.getCategory_id()==Constants.fav_id)
                return fullModel;
        }
        return null;
    }

    public static void putMovies(List<MovieModel> movieModels) {
        for (int i=unCount_number;i<MyApp.vod_categories.size();i++){
            MyApp.vod_categories.get(i).setMovieModels(filterMovies(movieModels,MyApp.vod_categories.get(i).getId()));
        }
    }

    private static List<MovieModel> filterMovies(List<MovieModel> movieModels, int categoryId){
        List<MovieModel> movieModelList=new ArrayList<>();
        for (MovieModel movieModel:movieModels){
            if (movieModel.getCategory_id() != null && movieModel.getCategory_id().equals(categoryId+""))
                movieModelList.add(movieModel);
            else if (movieModel.getCategory_id()==null && categoryId==Constants.no_name_id)
                movieModelList.add(movieModel);
        }
        return movieModelList;
    }

    public static void putSeries(List<SeriesModel> seriesModels) {
        for (int i=unCount_number;i<MyApp.series_categories.size();i++){
            MyApp.series_categories.get(i).setSeriesModels(filterSeries(seriesModels,MyApp.series_categories.get(i).getId()));
        }
    }

    private static List<SeriesModel> filterSeries(List<SeriesModel> seriesModels, int categoryId){
        List<SeriesModel> seriesModelList=new ArrayList<>();
        for (SeriesModel seriesModel:seriesModels){
            if (seriesModel.getCategory_id() != null && seriesModel.getCategory_id().equals(categoryId+""))
                seriesModelList.add(seriesModel);
            else if (seriesModel.getCategory_id()==null && categoryId==Constants.no_name_id)
                seriesModelList.add(seriesModel);
        }
        return seriesModelList;
    }

    public static void getVodFilter() {
        List<Integer> selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_VOD_CATEGORIES());
        MyApp.vod_categories_filter=new ArrayList<>();
        MyApp.vod_categories_filter.addAll(MyApp.vod_categories);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (CategoryModel categoryModel: MyApp.vod_categories){
                for (int string:selectedIds){
                    if (string == categoryModel.getId())
                        MyApp.vod_categories_filter.remove(categoryModel);
                }
            }
        }
    }

    public static void getLiveFilter() {
        List<Integer> selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_LIVE_CATEGORIES());
        MyApp.live_categories_filter = new ArrayList<>();
        MyApp.live_categories_filter.addAll(MyApp.live_categories);
        MyApp.fullModels_filter=new ArrayList<>();
        MyApp.fullModels_filter.addAll(MyApp.fullModels);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (int i = 0; i< MyApp.live_categories.size(); i++){
                CategoryModel categoryModel= MyApp.live_categories.get(i);
                for (int string:selectedIds){
                    if (string == categoryModel.getId()){
                        MyApp.live_categories_filter.remove(categoryModel);
                        MyApp.fullModels_filter.remove(MyApp.fullModels.get(i));
                    }
                }
            }
        }
        Log.e("live filter",MyApp.live_categories_filter.size()+"");
    }

    public static void getSeriesFilter() {
        List<Integer> selectedIds=(List<Integer>) MyApp.instance.getPreference().get(Constants.getINVISIBLE_SERIES_CATEGORIES());
        MyApp.series_categories_filter=new ArrayList<>();
        MyApp.series_categories_filter.addAll(MyApp.series_categories);
        if (selectedIds!=null && selectedIds.size()!=0) {
            for (CategoryModel categoryModel: MyApp.series_categories){
                for (int string:selectedIds){
                    if (string == categoryModel.getId())
                        MyApp.series_categories_filter.remove(categoryModel);
                }
            }
        }
    }

    public static int findNowEvent(List<EPGEvent> epgEvents){
        Date now = new Date();
        now.setTime(now.getTime()-SEVER_OFFSET);
        for (int i=0;i<epgEvents.size();i++){
            EPGEvent epgEvent=epgEvents.get(i);
            if (now.after(epgEvent.getStartTime()) && now.before(epgEvent.getEndTime())) {
                return i;
            }
        }
        return -1;
    }

    public static String GetBaseURL(Context mcontext)
    {
        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        String base_url=serveripdetails.getString("ip","");
        base_url=base_url+"player_api.php?";
        return  base_url;
    }

    public static String GetXmlURL(Context mcontext){
        String base_url=GetAppDomain(mcontext);
        base_url=base_url+"xmltv.php?";
        return  base_url;
    }

    public static String GetAppDomain(Context mcontext){
//        String base_url="";
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        base_url=serveripdetails.getString("ip","");
//        return  base_url;
        return userDataModel.getUrl();
    }

    public static String GetIcon(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("icon_image","");
//        return  app_icon;
        return userDataModel.getImage_urls().get(0);
    }

    public static String GetLoginImage(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("login_image","");
//        return  app_icon;
        return userDataModel.getImage_urls().get(2);
    }

    public static String GetMainImage(Context context){
//        String app_icon="";
//        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        app_icon=serveripdetails.getString("main_bg","");
//        return  app_icon;
        return userDataModel.getImage_urls().get(1);
    }

    public static String GetAd1(Context mcontext)
    {
//        String base_url="";
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        base_url=serveripdetails.getString("ad1","");
//        return  base_url;
        return userDataModel.getImage_urls().get(3);
    }

    public static String GetAd2(Context mcontext)
    {
//        String base_url="";
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        base_url=serveripdetails.getString("ad2","");
//        return  base_url;
        return userDataModel.getImage_urls().get(4);
    }


    public static String GetAd3(Context mcontext)
    {
//        String base_url="";
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        base_url=serveripdetails.getString("ad3","");
//        return  base_url;
        return userDataModel.getImage_urls().get(5);
    }

    public static String GetAd4(Context mcontext)
    {
//        String base_url="";
//        SharedPreferences serveripdetails = mcontext.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
//        base_url=serveripdetails.getString("ad4","");
//        return  base_url;
        return userDataModel.getImage_urls().get(6);
    }

    public static String GetAutho1(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("autho1","");
        return  app_icon;
    }
    public static String GetAutho2(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("autho2","");
        return  app_icon;
    }

    public static String GetUrl(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("url","");
        return  app_icon+"="+GetKey(context);
    }

    public static String GetKey(Context context){
        String app_icon="";
        SharedPreferences serveripdetails = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        app_icon=serveripdetails.getString("key"+MyApp.firstServer.getValue(),"");
        return  app_icon;
    }

    public static String GetCurrentTimeByTimeZone(String pattern,String time_zone) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone(time_zone)); //
        return df.format(c.getTime());
    }

    public static String GetCurrentDateByTimeZone(String pattern,String time_zone) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(TimeZone.getTimeZone(time_zone)); //
        return df.format(c.getTime());
    }


    public static String GetCurrentTime(String pattern) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(c.getTime());
    }

    public static String GetCurrentDate(String pattern) {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(c.getTime());
    }

    public static long getTimeDiffMinutes(String start_time, String end_time, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date d1 = null;
        Date d2 = null;
        long diffMinutes = 0;
        try {
            d1 = format.parse(start_time);
            d2 = format.parse(end_time);

            long diff = d2.getTime() - d1.getTime();
            diffMinutes = diff / (60 * 1000);
            return diffMinutes;

        } catch (Exception e) {
            e.printStackTrace();
            return diffMinutes;
        }
    }

    public static boolean checktimings(String current_time, String endtime, String pattern) {

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date date1 = sdf.parse(current_time);
            Date date2 = sdf.parse(endtime);

            if (date1.before(date2)) {
                return true;
            } else {

                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String GetDecodedString(String text) {
        return text;
    }

    public static String getLoginInfo() {
        return LOGIN_INFO+MyApp.firstServer.getValue();
    }

    public static String getFavInfo() {
        return FAV_INFO+MyApp.firstServer.getValue();
    }
}
