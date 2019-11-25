package com.it_tech613.tvmulti.ui.liveTv;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.ijklib.widget.media.AndroidMediaController;
import com.it_tech613.tvmulti.ijklib.widget.media.IjkVideoView;
import com.it_tech613.tvmulti.models.DataModel;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.models.EPGEvent;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.WebViewActivity;
import com.it_tech613.tvmulti.ui.movies.PackageDlg;
import com.it_tech613.tvmulti.utils.MyFragment;
import com.it_tech613.tvmulti.utils.Utils;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.Context.WINDOW_SERVICE;

public class FragmentIjkLiveTv extends MyFragment implements View.OnClickListener, IMediaPlayer.OnCompletionListener,IMediaPlayer.OnErrorListener {

    private IjkVideoView surfaceView;
    private AndroidMediaController mMediaController;
    private TextView txt_num;
    private TableLayout mHudView;
    private String contentUri;

    private boolean is_full=false;
    private int categoryPos=0, channelPos=0, playChanelPos = 0;
    private LiveTvProgramsAdapter liveTvProgramsAdapter;
    private ConstraintLayout rootView;
    private Handler mEpgHandler = new Handler();
    private Runnable mEpgTicker,mTicker;

    private Button addFav;
    private CategoryAdapter categoryAdapter;
    private ImageView fav_icon, image_icon;
    private ChannelAdapter channelAdapter;
    private RecyclerView category_recyclerview, channel_recyclerview;
    private Handler mHandler = new Handler();
    private LinearLayout ly_bottom,ly_info,ly_resolution,ly_audio,ly_subtitle,ly_fav,ly_tv_schedule;
    private TextView txt_title,txt_dec, txt_channel, txt_time_passed, txt_remain_time, txt_last_time, txt_current_dec, txt_next_dec, txt_date, channel_name;
    private ImageView channel_logo, image_clock, image_star;
    private SeekBar seekbar;

    private int pro,osd_time,width,height;

    private List<EPGEvent> epgModelList;
    private List<String> pkg_datas;

    private boolean is_msg = false;
    private Handler rssHandler = new Handler();
    private TextView txt_rss;
    private RelativeLayout lay_header;
    private int msg_time = 0;
    private String rss="";
    private Runnable rssTicker;
    private int rss_time;

    private int lastPlayingCategoryPos=0, lastPlayingChannelPos=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ijk_live_tv, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMediaController = new AndroidMediaController(requireContext(), false);
        if (Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().size()==0) {
            lastPlayingCategoryPos=1;
            categoryPos=1;
        }
        txt_num = view.findViewById(R.id.toast_text_view);
        mHudView = view.findViewById(R.id.hud_view);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list2)));
        category_recyclerview = view.findViewById(R.id.category_recyclerview);
        channel_recyclerview = view.findViewById(R.id.subcategory_recyclerview);
        lay_header = view.findViewById(R.id.lay_header);
        RecyclerView programs_recyclerview = view.findViewById(R.id.programs_recyclerview);
        category_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        channel_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        programs_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        liveTvProgramsAdapter=new LiveTvProgramsAdapter(new ArrayList<EPGEvent>());
        addFav = view.findViewById(R.id.button4);
        fav_icon = view.findViewById(R.id.fav_icon);
        if (getResources().getBoolean(R.bool.is_phone)) fav_icon.setVisibility(View.GONE);
        image_icon = view.findViewById(R.id.image_icon);
        channel_name = view.findViewById(R.id.channel_name);
        Glide.with(requireContext())
                .load(Constants.GetIcon(requireContext()))
                .apply(new RequestOptions().placeholder(R.drawable.icon).error(R.drawable.icon).signature(new ObjectKey("myKey0")))
                .into(image_icon);
        txt_rss = view.findViewById(R.id.txt_rss);
        //for info bar
        ly_bottom=view.findViewById(R.id.ly_bottom);
        ly_bottom.setVisibility(View.GONE);
        ly_info=view.findViewById(R.id.ly_info);
        ly_resolution=view.findViewById(R.id.ly_resolution);
        ly_audio=view.findViewById(R.id.ly_audio);
        ly_audio.setVisibility(View.INVISIBLE);
        ly_subtitle=view.findViewById(R.id.ly_subtitle);
        ly_subtitle.setVisibility(View.INVISIBLE);
        ly_fav=view.findViewById(R.id.ly_fav);
        ly_tv_schedule=view.findViewById(R.id.ly_tv_schedule);
        txt_title=view.findViewById(R.id.txt_title);
        txt_dec=view.findViewById(R.id.txt_dec);
        txt_channel=view.findViewById(R.id.txt_channel);
        txt_time_passed=view.findViewById(R.id.txt_time_passed);
        txt_remain_time=view.findViewById(R.id.txt_remain_time);
        txt_last_time=view.findViewById(R.id.txt_last_time);
        txt_current_dec=view.findViewById(R.id.txt_current_dec);
        txt_next_dec=view.findViewById(R.id.txt_next_dec);
        channel_logo=view.findViewById(R.id.channel_logo);
        image_clock=view.findViewById(R.id.image_clock);
        image_star=view.findViewById(R.id.image_star);
        seekbar=view.findViewById(R.id.seekbar);
        txt_date = view.findViewById(R.id.txt_date);

        ly_subtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        ly_resolution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                surfaceView.toggleAspectRatio();
            }
        });
        ly_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFav();
            }
        });
        ly_tv_schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), WebViewActivity.class));
            }
        });
        ly_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        addFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToFav();
            }
        });
        
        setAddFavText();
        osd_time = (int) MyApp.instance.getPreference().get(Constants.getOSD_TIME());
        channelAdapter =new ChannelAdapter(MyApp.fullModels_filter.get(categoryPos).getChannels(), new Function2<EPGChannel, Integer, Unit>() {
            @Override
            public Unit invoke(EPGChannel channelModel, Integer integer) {
                if (playChanelPos == integer){
                    toggleFullScreen();
                }else {
                    playChannel(channelModel,integer);
                }
                return null;
            }
        }, new Function2<EPGChannel, Integer, Unit>() {
            @Override
            public Unit invoke(EPGChannel epgChannel, Integer integer) {
                channelPos = integer;
                setAddFavText();
                EpgTimer();
                return null;
            }
        });
        categoryAdapter = new CategoryAdapter(MyApp.live_categories_filter, (categoryModel, position, is_clicked) -> {
            if (categoryModel.getId() == Constants.xxx_category_id && position!=0 && is_clicked) {
                PinDlg pinDlg = new PinDlg(requireContext(), new PinDlg.DlgPinListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                        String pin = (String) MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                        if (pin_code.equalsIgnoreCase(pin)) {
                            dialog.dismiss();
                            doWork(is_clicked, position);
                        } else {
                            dialog.dismiss();
                            Toast.makeText(requireContext(), "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void OnCancelClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                    }
                });
                pinDlg.show();
            } else doWork(is_clicked, position);

            return null;
        });
        category_recyclerview.setAdapter(categoryAdapter);
        channel_recyclerview.setAdapter(channelAdapter);
        programs_recyclerview.setAdapter(liveTvProgramsAdapter);
        rootView =view.findViewById(R.id.rootview);

        surfaceView = view.findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(this);
        surfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                toggleFullScreen();
                return false;
            }
        });
//
        width = (int) (MyApp.SCREEN_WIDTH*0.6*0.86);
        height = (int) (MyApp.SCREEN_HEIGHT*0.6);
//        width = 0;
//        height = 0;
//        changeVideoViewSize(width,height);

        EpgTimer();
        playChannel(MyApp.fullModels_filter.get(lastPlayingCategoryPos).getChannels().get(lastPlayingChannelPos),lastPlayingChannelPos);
    }

    private void doWork(boolean is_clicked, int position) {
        if (!is_clicked) return;
        categoryPos=position;
        channelAdapter.setList(MyApp.fullModels_filter.get(categoryPos).getChannels());
        channelPos=0;
        playChanelPos=-1;
        if (lastPlayingCategoryPos==categoryPos) {
            playChanelPos=lastPlayingChannelPos;
            category_recyclerview.scrollToPosition(lastPlayingChannelPos);
        }
        channelAdapter.setSelected(playChanelPos);
        setAddFavText();
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.deleteCache(requireContext());
    }

    private void getRespond(){
        if (!MyApp.is_announce_enabled) return;
        try {
            String string = MyApp.instance.getIptvclient().login(Constants.GetKey(requireActivity()));
            try {
                JSONObject object = new JSONObject(string);
                if (object.getBoolean("status")) {
                    JSONObject data = object.getJSONObject("data");
                    Gson gson = new Gson();
                    DataModel dataModel = gson.fromJson(data.toString(),DataModel.class);
                    Constants.userDataModel = dataModel;
                    is_msg = !dataModel.getMessage_on_off().equalsIgnoreCase("0");
                    try {
                        msg_time = Integer.parseInt(dataModel.getMessage_time());
                    }catch (Exception e){
                        msg_time = 20;
                    }
                    String rss_feed = "                 "+dataModel.getMessage()+"                 ";
                    if(rss.equalsIgnoreCase(rss_feed)){
                        lay_header.setVisibility(View.GONE);
                    }else {
                        rss =rss_feed;
                        lay_header.setVisibility(View.VISIBLE);
                    }
                    if(is_msg){
                        lay_header.setVisibility(View.VISIBLE);
                        txt_rss.setText(rss);
                        Animation bottomToTop = AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_to_top);
                        txt_rss.clearAnimation();
                        txt_rss.startAnimation(bottomToTop);
                    }else {
                        lay_header.setVisibility(View.GONE);
                    }
                    rssTimer();
                } else {
                    Toast.makeText(requireContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rssTimer() {
        rss_time = msg_time;
        rssTicker = new Runnable() {
            public void run() {
                if (rss_time < 1) {
                    lay_header.setVisibility(View.GONE);
                    return;
                }
                runRssTicker();
            }
        };
        rssTicker.run();
    }

    private void runRssTicker() {
        rss_time --;
        long next = SystemClock.uptimeMillis() + 1000;
        rssHandler.postAtTime(rssTicker, next);
    }

    private void playChannel(EPGChannel channelModel, Integer integer) {
        playChanelPos = integer;
        channelPos = integer;
        if (channelModel.getCategory_id() == Constants.xxx_category_id && categoryPos == 0) {
            PinDlg pinDlg = new PinDlg(requireContext(), new PinDlg.DlgPinListener() {
                @Override
                public void OnYesClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                    String pin = (String) MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                    if (pin_code.equalsIgnoreCase(pin)) {
                        dialog.dismiss();
                        playVideo();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(requireContext(), "Your Pin code was incorrect. Please try again", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void OnCancelClick(Dialog dialog, String pin_code) {
                    dialog.dismiss();
                }
            });
            pinDlg.show();
        } else playVideo();
        setAddFavText();
        EpgTimer();
        channelAdapter.setSelected(playChanelPos);
    }

    int maxTime;
    private void listTimer() {
        maxTime = osd_time;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    ly_bottom.setVisibility(View.GONE);
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }
    private void runNextTicker() {
        maxTime--;
        long next = SystemClock.uptimeMillis() + 1000;
        mHandler.postAtTime(mTicker, next);
    }

    private void updateProgressBar() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 0);
    }
    
    private Runnable mUpdateTimeTask = new Runnable() {
        @SuppressLint("SetTextI18n")
        public void run() {
            if (surfaceView != null) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH.mm a EEE MM/dd");
                long nowLocalTimeStamp = System.currentTimeMillis();
                final EPGChannel playChannel = MyApp.fullModels_filter.get(lastPlayingCategoryPos).getChannels().get(lastPlayingChannelPos);
                if(epgModelList!=null && epgModelList.size()>0){
                    try {
                        EPGEvent epgEvent = epgModelList.get(0);
                        long startStamp =epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET;
                        long endStamp = epgEvent.getEndTime().getTime()+ Constants.SEVER_OFFSET;
                        if(nowLocalTimeStamp>startStamp){
                            txt_title.setText(new String (Base64.decode(epgEvent.getTitle(), Base64.DEFAULT)));
                            txt_dec.setText(new String (Base64.decode(epgEvent.getDec(), Base64.DEFAULT)));
                            try {
                                txt_channel.setText(playChannel.getNumber() + " " + playChannel.getName());
                            }catch (Exception e1){
                                txt_channel.setText("    ");
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void run() {
                                        txt_channel.setText(playChannel.getNumber() + " " + playChannel.getName());
                                    }
                                }, 5000);
                            }
                            txt_date.setText(dateFormat.format(new Date()));
                            int pass_min = (int) ((nowLocalTimeStamp - startStamp)/(1000*60));
                            int remain_min = (int)(endStamp-nowLocalTimeStamp)/(1000*60);
                            int progress = (int) pass_min*100/(int)((epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/60/1000);
                            pro  = progress;
                            seekbar.setProgress(progress);
                            txt_time_passed.setText("Started " + pass_min +" mins ago");
                            txt_remain_time.setText("+"+remain_min+" min");
                            txt_last_time.setText(sdf.format(new Date(endStamp)));
                            Log.e(TAG,epgEvent.getTitle()+" "+epgModelList.get(1).getTitle());
                            txt_current_dec.setText(new String (Base64.decode(epgEvent.getTitle(), Base64.DEFAULT)));
                            txt_next_dec.setText(new String (Base64.decode(epgModelList.get(1).getTitle(), Base64.DEFAULT)));
                            if(playChannel.is_favorite()){
                                image_star.setVisibility(View.VISIBLE);
                            }else {
                                image_star.setVisibility(View.GONE);
                            }
                            if(playChannel.getTv_archive()==1){
                                image_clock.setVisibility(View.VISIBLE);
                            }else {
                                image_clock.setVisibility(View.GONE);
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    txt_title.setText("No Information");
                    txt_dec.setText("No Information");
                    try {
                        txt_channel.setText(playChannel.getNumber() + " " + playChannel.getName());
                    }catch (Exception e2){
                        txt_channel.setText("    ");
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                txt_channel.setText(playChannel.getNumber() + " " + playChannel.getName());
                            }
                        }, 5000);
                    }
                    txt_date.setText(dateFormat.format(new Date()));
                    txt_time_passed.setText("      mins ago");
                    txt_remain_time.setText("      min");
                    txt_last_time.setText("         ");
                    seekbar.setProgress(0);
                    txt_current_dec.setText("No Information");
                    txt_next_dec.setText("No Information");
                }
            }
            mHandler.postDelayed(this, 500);
        }
    };

    private void setAddFavText(){
        if(MyApp.fullModels_filter.size()>0 && MyApp.fullModels_filter.get(categoryPos).getChannels().size()>0){
            channel_name.setText(MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).getName());
            if (MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).is_favorite()) {
                addFav.setText(getResources().getString(R.string.remove_favorites));
                fav_icon.setImageResource(R.drawable.heart_filled);
            }
            else {
                addFav.setText(getResources().getString(R.string.add_to_favorite));
                fav_icon.setImageResource(R.drawable.heart_unfilled);
            }
        }
    }

    private void addToFav() {
        Log.e("OnAddFavClick","received");
        EPGChannel selectedChannel = MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos);
        if (selectedChannel.is_favorite()) {
            pkg_datas.set(0, "Add to Fav");
            selectedChannel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < Constants.getFavFullModel(MyApp.fullModels).getChannels().size(); i++) {
                if (Constants.getFavFullModel(MyApp.fullModels).getChannels().get(i).getName().equals(selectedChannel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist) {
                Constants.getFavFullModel(MyApp.fullModels).getChannels().remove(pp);
            }
            //get favorite channel names list
            List<String> fav_channel_names=new ArrayList<>();
            for (EPGChannel epgChannel:Constants.getFavFullModel(MyApp.fullModels).getChannels()){
                fav_channel_names.add(epgChannel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_LIVE_CHANNELS(), fav_channel_names);
            Log.e("ADD_FAV","removed");
        } else {
            pkg_datas.set(0, "Remove from Fav");
            selectedChannel.setIs_favorite(true);
            Constants.getFavFullModel(MyApp.fullModels).getChannels().add(selectedChannel);
            //get favorite channel names list
            List<String> fav_channel_names=new ArrayList<>();
            for (EPGChannel epgChannel:Constants.getFavFullModel(MyApp.fullModels).getChannels()){
                fav_channel_names.add(epgChannel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_LIVE_CHANNELS(), fav_channel_names);
            Log.e("LIVE_RATIO","added");
        }
        if (categoryPos==1) categoryAdapter.notifyDataSetChanged();
        setAddFavText();
    }

    private void EpgTimer(){
        mEpgHandler.removeCallbacks(mEpgTicker);
//        epg_time = 1;
        mEpgTicker = new Runnable() {
            public void run() {
                new Thread(()->getEpg()).start();
                runNextEpgTicker();
            }
        };
        mEpgTicker.run();
    }

    private void runNextEpgTicker() {
//        epg_time--;
        long next = SystemClock.uptimeMillis() + 60000;
        mEpgHandler.postAtTime(mEpgTicker, next);
    }

    private void releaseMediaPlayer() {
        if(surfaceView!=null){
            surfaceView.release(true);
        }
    }

    private String TAG = this.getClass().getSimpleName();

    private void toggleFullScreen(){
        if (is_full){
            Log.e(TAG,"Small screen");
            is_full=false;
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootView);
            constraintSet.setGuidelinePercent(R.id.guideline1, 0.2f);
            constraintSet.setGuidelinePercent(R.id.guideline2, 0.4f);
            constraintSet.setGuidelinePercent(R.id.guideline3, 0.6f);

            constraintSet.applyTo(rootView);
            ((MainActivity) requireActivity()).toggleFullScreen(is_full);
            ly_bottom.setVisibility(View.GONE);
            lay_header.setVisibility(View.GONE);

            width = (int) (MyApp.SCREEN_WIDTH*0.6*0.86);
            height = (int) (MyApp.SCREEN_HEIGHT*0.6);
//            width = 0;
//            height =0;
//            changeVideoViewSize(width,height);
        }else {
            Log.e(TAG,"Full screen");
            is_full=true;
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootView);
            constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
            constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
            constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);

            constraintSet.applyTo(rootView);
            ((MainActivity) requireActivity()).toggleFullScreen(is_full);

            width = MyApp.SCREEN_WIDTH;
            height =MyApp.SCREEN_HEIGHT;
//            width = 0;
//            height = 0;
//            changeVideoViewSize(width,height);

            showInfoBar();
        }
    }

    private void changeVideoViewSize(int witdth,int height){
        WindowManager wm = (WindowManager) requireActivity().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            ViewGroup.LayoutParams paramsPlayerView=surfaceView.getLayoutParams();
            paramsPlayerView.height = height;
            paramsPlayerView.width = witdth;
            surfaceView.setLayoutParams(paramsPlayerView);
        }
    }

    private void showPackageDlg(){
        if (MyApp.fullModels_filter.get(categoryPos).getChannels().get(playChanelPos).is_favorite()) {
            pkg_datas.set(0,"Remove from Fav");
        }else {
            pkg_datas.set(0,"Add to Fav");
        }
        PackageDlg packageDlg = new PackageDlg(requireContext(), pkg_datas, new PackageDlg.DialogPackageListener() {
            @Override
            public void OnItemClick(Dialog dialog, int position) {
                dialog.dismiss();
                switch (position) {
                    case 0:
                        addToFav();
                        break;
                    case 1:
                        MyApp.instance.getPreference().put(Constants.getCATEGORY_POS(),categoryPos);
                        SearchDlg searchDlg = new SearchDlg(requireContext(), new SearchDlg.DialogSearchListener() {
                            @Override
                            public void OnSearchClick(Dialog dialog, EPGChannel sel_Channel) {
                                dialog.dismiss();
                                for(int i = 0;i<MyApp.fullModels_filter.get(categoryPos).getChannels().size();i++){
                                    if(MyApp.fullModels_filter.get(categoryPos).getChannels().get(i).getName().equalsIgnoreCase(sel_Channel.getName())){
                                        channelPos = i;
                                        playChanelPos = i;
                                        channel_recyclerview.scrollToPosition(channelPos);
                                        channelAdapter.setSelected(playChanelPos);
                                        MyApp.instance.getPreference().put(Constants.getCHANNEL_POS(),channelPos);
                                        mEpgHandler.removeCallbacks(mEpgTicker);
                                        EpgTimer();
                                        Log.e(TAG,"channel pos: "+channelPos+sel_Channel.getName());
                                        break;
                                    }
                                }
                            }
                        });
                        searchDlg.show();
                        break;
                    case 2:
                        surfaceView.toggleAspectRatio();
                        break;
                    case 3:
                        startActivity(new Intent(requireContext(),WebViewActivity.class));
                        break;
                }
            }
        });
        packageDlg.show();
    }


    @Override
    public void onPause() {
        super.onPause();
        releaseMediaPlayer();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mEpgHandler.removeCallbacks(mEpgTicker);
    }

    private void checkAddedRecent(EPGChannel showModel) {
        Iterator<EPGChannel> iter =  Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().iterator();
        while(iter.hasNext()){
            EPGChannel movieModel = iter.next();
            if (movieModel.getName().equals(showModel.getName()))
                iter.remove();
        }
    }

    private void playVideo() {
        lastPlayingCategoryPos = categoryPos;
        lastPlayingChannelPos = playChanelPos;
        releaseMediaPlayer();
        contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                MyApp.fullModels_filter.get(categoryPos).getChannels().get(playChanelPos).getStream_id()+"","ts");
        Log.e("url",contentUri);
        //add recent series
        EPGChannel epgChannel = MyApp.fullModels_filter.get(categoryPos).getChannels().get(playChanelPos);
        checkAddedRecent(epgChannel);
        Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().add(0,epgChannel);
        //get recent series names list
        List<String> recent_series_names = new ArrayList<>();
        for (EPGChannel channel:Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels()){
            recent_series_names.add(channel.getName());
        }
        //set
        MyApp.instance.getPreference().put(Constants.getRecentChannels(), recent_series_names);
        Log.e(getClass().getSimpleName(),"added");
        try {
            surfaceView.setMediaController(mMediaController);
            surfaceView.setHudView(mHudView);
            mMediaController.hide();
            surfaceView.setVideoPath(contentUri);
            surfaceView.setOnCompletionListener(this);
            surfaceView.setOnErrorListener(this);
            surfaceView.start();
            categoryAdapter.setSelected(categoryPos);
            category_recyclerview.scrollToPosition(categoryPos);
            channel_recyclerview.scrollToPosition(playChanelPos);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void getEpg(){
        try {
            String map = MyApp.instance.getIptvclient().getShortEPG(MyApp.user,MyApp.pass,
                    MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).getStream_id()+"",4);
            Log.e(getClass().getSimpleName(),map);
            Gson gson=new Gson();
            map=map.replaceAll("[^\\x00-\\x7F]", "");
            if (!map.contains("null_error_response")){
                Log.e("response",map);
                try {
                    JSONObject jsonObject= new JSONObject(map);
                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
                    epgModelList = new ArrayList<>();
                    epgModelList.addAll((Collection<? extends EPGEvent>) gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>(){}.getType()));
                    requireActivity().runOnUiThread(()->liveTvProgramsAdapter.setEpgModels(epgModelList));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        AudioManager audioManager = (AudioManager) requireActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        View view = requireActivity().getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_UP){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_MENU:
//                    toggleFullScreen();
                        showPackageDlg();
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (is_full) {
                        toggleFullScreen();
                        return false;
                    }else {
                        mHandler.removeCallbacks(mUpdateTimeTask);
                        releaseMediaPlayer();
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(0))//FragmentCatchupDetail
                                .addToBackStack(null).commit();
                        return false;
                    }
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (!is_full){
//                        if (is_focus_in_channel_recyclerview)
//                            addFav.requestFocus();
                    }else {
                        if (audioManager != null) {
                            audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (is_full){
                        if (audioManager != null) {
                            audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                        }
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (is_full && MyApp.fullModels_filter.get(categoryPos).getChannels().size()-1>playChanelPos) {
                        playChannel(MyApp.fullModels_filter.get(categoryPos).getChannels().get(playChanelPos+1),playChanelPos+1);
                        showInfoBar();
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (is_full && 0<playChanelPos) {
                        playChannel(MyApp.fullModels_filter.get(categoryPos).getChannels().get(playChanelPos-1),playChanelPos-1);
                        showInfoBar();
                    }
                    break;
            }
        }
        return super.myOnKeyDown(event);
    }

    private void showInfoBar() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateProgressBar();
                ly_bottom.setVisibility(View.VISIBLE);
                listTimer();
            }
        }, 100);
        getRespond();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.surface_view:
                Log.e("click_event","click");
//                showMenu();
                toggleFullScreen();
                surfaceView.requestFocus();
                break;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        releaseMediaPlayer();
        onResume();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        releaseMediaPlayer();
        return false;
    }
}
