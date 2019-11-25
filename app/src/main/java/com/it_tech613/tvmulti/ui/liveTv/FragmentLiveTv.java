package com.it_tech613.tvmulti.ui.liveTv;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
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
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.WebViewActivity;
import com.it_tech613.tvmulti.ui.movies.PackageDlg;
import com.it_tech613.tvmulti.utils.MyFragment;
import com.it_tech613.tvmulti.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WINDOW_SERVICE;

public class FragmentLiveTv extends MyFragment implements SurfaceHolder.Callback, IVLCVout.Callback, View.OnClickListener {

    private LibVLC libvlc;
    private SurfaceView surfaceView;
//    private SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private String contentUri;
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);
    private boolean is_full=false, is_mute=false;
    private int categoryPos=0, channelPos=0, playChanelPos = 0;
    private LiveTvProgramsAdapter liveTvProgramsAdapter;
    private ConstraintLayout rootView;
//    private ImageView mute;
    private Handler mEpgHandler = new Handler();
    private Runnable mEpgTicker,mTicker;
//    private int epg_time;
//    private int i = 0;
    private Button addFav;
    private CategoryAdapter categoryAdapter;
    private ImageView fav_icon, image_icon;
    private boolean is_focus_in_channel_recyclerview=false;
    private ChannelAdapter channelAdapter;
    private RecyclerView category_recyclerview, channel_recyclerview;
    private Handler mHandler = new Handler();
    private LinearLayout ly_bottom,ly_info,ly_resolution,ly_audio,ly_subtitle,ly_fav,ly_tv_schedule;
    private TextView txt_title,txt_dec, txt_channel, txt_time_passed, txt_remain_time, txt_last_time, txt_current_dec, txt_next_dec, txt_date, channel_name;
    private ImageView channel_logo, image_clock, image_star;
    private SeekBar seekbar;
    private MediaPlayer.TrackDescription[] traks;
    private MediaPlayer.TrackDescription[] subtraks;
    private String ratio;
    private String[] resolutions ;
    private int current_resolution = 0;
    private int pro,osd_time,selected_item = 0;
    boolean first = true;
    private List<EPGEvent> epgModelList;
    private List<String> pkg_datas;

    private boolean is_rss = false,is_msg = false;
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
        return inflater.inflate(R.layout.fragment_live_tv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pkg_datas = new ArrayList<>();
        for (int i = 0; i < getResources().getStringArray(R.array.package_list).length; i++) {
            pkg_datas.add(getResources().getStringArray(R.array.package_list)[i]);
        }
        if (Constants.getRecentFullModel(MyApp.fullModels_filter).getChannels().size()==0) {
            lastPlayingCategoryPos=1;
            categoryPos=1;
        }
        category_recyclerview = view.findViewById(R.id.category_recyclerview);
        channel_recyclerview = view.findViewById(R.id.subcategory_recyclerview);
        lay_header = view.findViewById(R.id.lay_header);
        RecyclerView programs_recyclerview = view.findViewById(R.id.programs_recyclerview);
//        mute = view.findViewById(R.id.mute);
//        mute.setOnClickListener(this);
        category_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        channel_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        programs_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));
        liveTvProgramsAdapter=new LiveTvProgramsAdapter(new ArrayList<EPGEvent>());
        addFav = (Button)view.findViewById(R.id.button4);
        fav_icon = (ImageView)view.findViewById(R.id.fav_icon);
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
        ly_subtitle=view.findViewById(R.id.ly_subtitle);
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

        ly_subtitle.setOnClickListener(v -> {
            if (subtraks != null) {
                if (subtraks.length > 0) {
                    showSubTracksList();
                } else {
                    Toast.makeText(requireContext(),
                            "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(),
                        "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
            }
        });
        ly_resolution.setOnClickListener(v -> {
            current_resolution++;
            if (current_resolution == resolutions.length)
                current_resolution = 0;

            mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
        });
        ly_fav.setOnClickListener(v -> addToFav());
        ly_tv_schedule.setOnClickListener(v -> startActivity(new Intent(requireContext(), WebViewActivity.class)));
        ly_audio.setOnClickListener(v -> {
            if (traks != null) {
                if (traks.length > 0) {
                    showAudioTracksList();
                } else {
                    Toast.makeText(requireContext(),
                            "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(),
                        "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
            }
        });
        addFav.setOnClickListener(v -> addToFav());
        
        setAddFavText();
        osd_time = (int) MyApp.instance.getPreference().get(Constants.getOSD_TIME());
        channelAdapter =new ChannelAdapter(MyApp.fullModels_filter.get(categoryPos).getChannels(), (channelModel, integer) -> {
            if (playChanelPos == integer){
                toggleFullScreen();
            }else {
                playChannel(channelModel,integer);
            }
            return null;
        }, (epgChannel, integer) -> {
            channelPos = integer;
            setAddFavText();
            is_focus_in_channel_recyclerview=true;
            EpgTimer();
            return null;
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
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBX_8888);

//        remote_subtitles_surface = view.findViewById(R.id.remote_subtitles_surface);
//        remote_subtitles_surface.setZOrderMediaOverlay(true);
//        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        WindowManager wm = (WindowManager) requireContext().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;
        ratio = mVideoWidth + ":"+ mVideoHeight;
        resolutions =  new String[]{"16:9", "4:3", ratio};
        EpgTimer();
        playChannel(MyApp.fullModels_filter.get(lastPlayingCategoryPos).getChannels().get(lastPlayingChannelPos),lastPlayingChannelPos);
    }

    private void doWork(boolean is_clicked, int position) {
        is_focus_in_channel_recyclerview=false;
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
                        is_rss = false;
                    }else {
                        rss =rss_feed;
                        is_rss = true;
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
        rssTicker = () -> {
            if (rss_time < 1) {
                lay_header.setVisibility(View.GONE);
                return;
            }
            runRssTicker();
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
        is_focus_in_channel_recyclerview=true;
        EpgTimer();
        channelAdapter.setSelected(playChanelPos);
    }

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < traks.length; i++) {
            names.add(traks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = requireContext().getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("AUDIO_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = requireContext().getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("AUDIO_TRACK", selected_item);
                editor.apply();

                mMediaPlayer.setAudioTrack(traks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (MediaPlayer.TrackDescription subtrak : subtraks) {
            names.add(subtrak.name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = requireContext().getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("SUB_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selected_item = which;
                    }
                });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = requireContext().getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("SUB_TRACK", selected_item);
                editor.commit();
                mMediaPlayer.setSpuTrack(subtraks[selected_item].id);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
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
            if (mMediaPlayer != null) {
                if (traks == null && subtraks == null) {
                    first = false;
                    traks = mMediaPlayer.getAudioTracks();
                    subtraks = mMediaPlayer.getSpuTracks();
                }
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
        if (!MyApp.fullModels_filter.get(categoryPos).getChannels().isEmpty() && MyApp.fullModels_filter.get(categoryPos).getChannels().size()>channelPos) {
            channel_name.setText(MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).getName());
            if (MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).is_favorite()) {
                addFav.setText(getResources().getString(R.string.remove_favorites));
                fav_icon.setImageResource(R.drawable.heart_filled);
            } else {
                addFav.setText(getResources().getString(R.string.add_to_favorite));
                fav_icon.setImageResource(R.drawable.heart_unfilled);
            }
        }else {
            channel_name.setText("");
            addFav.setText("");
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
        if (categoryPos==1) {
            channelAdapter.notifyDataSetChanged();
            categoryAdapter.setSelected(categoryPos);
        }
        setAddFavText();
    }

    private void EpgTimer(){
        mEpgHandler.removeCallbacks(mEpgTicker);
//        epg_time = 1;
        mEpgTicker = () -> {
            new Thread(()->getEpg()).start();
            runNextEpgTicker();
        };
        mEpgTicker.run();
    }

    private void runNextEpgTicker() {
//        epg_time--;
        long next = SystemClock.uptimeMillis() + 60000;
        mEpgHandler.postAtTime(mEpgTicker, next);
    }

    private void releaseMediaPlayer() {
        if (libvlc == null)
            return;
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        }
        holder = null;
        libvlc.release();
        libvlc = null;
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
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                TransitionManager.beginDelayedTransition(rootView);
//            }
            constraintSet.applyTo(rootView);
            ((MainActivity) requireActivity()).toggleFullScreen(is_full);
            ly_bottom.setVisibility(View.GONE);
            lay_header.setVisibility(View.GONE);
        }else {
            Log.e(TAG,"Full screen");
            is_full=true;
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(rootView);
            constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
            constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
            constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                TransitionManager.beginDelayedTransition(rootView);
//            }
            constraintSet.applyTo(rootView);
            ((MainActivity) requireActivity()).toggleFullScreen(is_full);
            showInfoBar();
        }
    }

    private void toggleMute(){
        if (mMediaPlayer==null) return;
        if (is_mute){
            is_mute=false;
//            mute.setImageResource(R.drawable.sound_mute);
            mMediaPlayer.setVolume(0);
        }else {
            is_mute=true;
//            mute.setImageResource(R.drawable.sound);
            mMediaPlayer.setVolume(100);
        }
    }

    private void showPackageDlg(){
        if (MyApp.fullModels_filter.get(categoryPos).getChannels().get(channelPos).is_favorite()) {
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
                        if (subtraks != null) {
                            if (subtraks.length > 0) {
                                showSubTracksList();
                            } else {
                                Toast.makeText(requireContext(),
                                        "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 3:
                        if (traks != null) {
                            if (traks.length > 0) {
                                showAudioTracksList();
                            } else {
                                Toast.makeText(requireContext(),
                                        "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(requireContext(),
                                    "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case 4:
                        current_resolution++;
                        if (current_resolution == resolutions.length)
                            current_resolution = 0;

                        mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                        break;
                    case 5:
                        startActivity(new Intent(requireContext(),WebViewActivity.class));
                        break;
                }
            }
        });
        packageDlg.show();
    }

    private void showMenu(){
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("fragment_menu");
        if (prev != null) {
            ft.remove(prev);
            ft.addToBackStack(null);
            return;
        }
        boolean isPlaying;
        if (mMediaPlayer!=null) isPlaying=mMediaPlayer.isPlaying();
        else isPlaying=true;
        MulteScreenMenuDialog selectChannelDialog = MulteScreenMenuDialog.newInstance(isPlaying, is_mute);
        selectChannelDialog.setMultiScreenMenuListenerListener(new MulteScreenMenuDialog.MultiScreenMenuListener() {
            @Override
            public void onPlus() {
                surfaceView.requestFocus();
            }

            @Override
            public void onFullScreen() {
                toggleFullScreen();
                surfaceView.requestFocus();
            }

            @Override
            public void onPlayPause(boolean is_playing) {
                if (is_playing) mMediaPlayer.pause();
                else mMediaPlayer.play();
                surfaceView.requestFocus();
            }

            @Override
            public void onSoundMute(boolean is_mute) {
                toggleMute();
                surfaceView.requestFocus();
            }
        });
        selectChannelDialog.show(fm, "fragment_menu");
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
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libvlc = new LibVLC(requireContext(), options);

            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
//            if (remote_subtitles_surface != null)
//                vout.setSubtitlesView(remote_subtitles_surface);
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();


            Media m = new Media(libvlc, Uri.parse(contentUri));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
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
                    epgModelList.addAll(gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>(){}.getType()));
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
        handler.postDelayed(() -> {
            updateProgressBar();
            ly_bottom.setVisibility(View.VISIBLE);
            listTimer();
        }, 100);
        getRespond();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.surface_view:
//                showMenu();
                toggleFullScreen();
                surfaceView.requestFocus();
                break;
        }
    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<FragmentLiveTv> mOwner;

        public MediaPlayerListener(FragmentLiveTv owner) {
            mOwner = new WeakReference<FragmentLiveTv>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            FragmentLiveTv player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
//                    player.releaseMediaPlayer();
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
                    break;
                case MediaPlayer.Event.Stopped:
                    break;
                case MediaPlayer.Event.Buffering:
                    break;
                case MediaPlayer.Event.EncounteredError:
                    break;
                case MediaPlayer.Event.TimeChanged:
                    break;
                case MediaPlayer.Event.PositionChanged:
                    break;
                default:
                    break;
            }
        }
    }
}
