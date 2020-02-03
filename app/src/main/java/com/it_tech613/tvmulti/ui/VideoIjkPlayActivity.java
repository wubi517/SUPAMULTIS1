package com.it_tech613.tvmulti.ui;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.*;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.ijklib.widget.media.AndroidMediaController;
import com.it_tech613.tvmulti.ijklib.widget.media.IjkVideoView;
import com.it_tech613.tvmulti.models.DataModel;
import com.it_tech613.tvmulti.models.MovieModel;
import com.it_tech613.tvmulti.ui.movies.PackageDlg;
import com.it_tech613.tvmulti.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoIjkPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener,View.OnClickListener, IMediaPlayer.OnErrorListener,IMediaPlayer.OnCompletionListener {
    private static final String TAG = "JavaActivity";
    private IjkVideoView surfaceView;
    private AndroidMediaController mMediaController;
    private TextView txt_num;
    private TableLayout mHudView;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay,ly_play,ly_resolution,ly_audio,ly_subtitle,ly_fav;
    ImageView img_play;

    boolean first = true;

    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,rss="";
    int duration_time = 0,position,fav_pos = 0,msg_time = 0;
    List<MovieModel> vod_favList;
    boolean is_create = true,is_long=false;
    boolean is_exit = false,is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    private FrameLayout mVideoSurfaceFrame = null;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_ijk_player);
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
        mMediaController = new AndroidMediaController(this, false);
        txt_num = findViewById(R.id.toast_text_view);
        mHudView = findViewById(R.id.hud_view);
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        pkg_datas = new ArrayList<>();
        pkg_datas.addAll(Arrays.asList(getResources().getStringArray(R.array.package_list1)));
        if(MyApp.instance.getPreference().get(Constants.getMOVIE_FAV())==null){
            vod_favList = new ArrayList<>();
        }else {
            vod_favList = (List<MovieModel>) MyApp.instance.getPreference().get(Constants.getMOVIE_FAV());
        }
        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        lay_header = findViewById(R.id.lay_header);
        if (mOnLayoutChangeListener == null) {
            mOnLayoutChangeListener = new View.OnLayoutChangeListener() {
                private final Runnable mRunnable = new Runnable() {
                    @Override
                    public void run() {
                    }
                };

                @Override
                public void onLayoutChange(View v, int left, int top, int right,
                                           int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.post(mRunnable);
                    }
                }
            };
        }
        mVideoSurfaceFrame.addOnLayoutChangeListener(mOnLayoutChangeListener);
        surfaceView = findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(view -> {
            if(bottom_lay.getVisibility()== View.VISIBLE){
                bottom_lay.setVisibility(View.GONE);
            }else {
                bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
            }
        });

        def_lay = findViewById(R.id.def_lay);
        bottom_lay = findViewById(R.id.vod_bottom_lay);
        ly_fav = findViewById(R.id.ly_fav);
        ly_fav.setOnClickListener(this);
        title_txt = findViewById(R.id.vod_channel_title);
        imageView = findViewById(R.id.vod_channel_img);
        start_txt = findViewById(R.id.vod_start_time);
        end_txt = findViewById(R.id.vod_end_time);
        seekBar = findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);

        ly_audio = findViewById(R.id.ly_audio);
        ly_audio.setVisibility(View.GONE);

        ly_play = findViewById(R.id.ly_play);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);
        ly_subtitle.setVisibility(View.GONE);

        ly_subtitle.setOnClickListener(this);
        ly_play.setOnClickListener(this);
        ly_resolution.setOnClickListener(this);
        ly_subtitle.setOnClickListener(this);
        ly_audio.setOnClickListener(this);

        img_play = findViewById(R.id.img_play);

        txt_rss = findViewById(R.id.txt_rss);
        txt_rss.setSingleLine(true);

        image_icon = findViewById(R.id.image_icon);
        Glide.with(this).load(Constants.GetIcon(this))
                .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon_default).signature(new ObjectKey("myKey9")))
                .into(image_icon);

        title_txt.setText(getIntent().getStringExtra("title"));
        cont_url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        is_live = getIntent().getBooleanExtra("is_live",false);
        try {
            Glide.with(this).load(getIntent().getStringExtra("img"))
                    .apply(new RequestOptions().error(R.drawable.icon).placeholder(R.drawable.icon_default).signature(new ObjectKey("myKey10")))
                    .into(imageView);

        }catch (Exception e){
            Glide.with(this).load(R.drawable.icon_default).into(imageView);
        }


        playVideo(cont_url);
        FullScreencall();
        getRespond();
    }

    private void getRespond(){
        if (!MyApp.is_announce_enabled) return;
        try {
            String string = MyApp.instance.getIptvclient().login(Constants.GetKey(this));
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
                        Animation bottomToTop = AnimationUtils.loadAnimation(VideoIjkPlayActivity.this, R.anim.bottom_to_top);
                        txt_rss.clearAnimation();
                        txt_rss.startAnimation(bottomToTop);
                    }else {
                        lay_header.setVisibility(View.GONE);
                    }
                    rssTimer();
                } else {
                    Toast.makeText(VideoIjkPlayActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    int rss_time;
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


    private void playVideo(String path) {
        if(def_lay.getVisibility()==View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
        toggleFullscreen(true);
        try {
            surfaceView.setMediaController(mMediaController);
            surfaceView.setHudView(mHudView);
            mMediaController.hide();
            surfaceView.setVideoPath(path);
            surfaceView.setOnCompletionListener(this);
            surfaceView.setOnErrorListener(this);
            surfaceView.start();

            updateProgressBar();
            updateTimer();
//            mediaSeekTo();

        } catch (Exception e) {
            Toast.makeText(this, "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }

    }

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (surfaceView != null) {
                long totalDuration = surfaceView.getDuration();
                long currentDuration = surfaceView.getCurrentPosition();
                end_txt.setText("" + Utils.milliSecondsToTimer(totalDuration));
                start_txt.setText("" + Utils.milliSecondsToTimer(currentDuration));
                int progress = (int) (Utils.getProgressPercentage(currentDuration, totalDuration));
                seekBar.setProgress(progress);
                mHandler.postDelayed(this, 500);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        if (!is_create) {
            if (surfaceView != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            playVideo(cont_url);
        } else {
            is_create = false;
        }
    }
    private void toggleFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (fullscreen) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mOnLayoutChangeListener != null) {
            mVideoSurfaceFrame.removeOnLayoutChangeListener(mOnLayoutChangeListener);
            mOnLayoutChangeListener = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayer();
    }
    @Override
    protected void onUserLeaveHint()
    {
        releaseMediaPlayer();
        finish();
        super.onUserLeaveHint();
    }
    private void releaseMediaPlayer() {
        if (surfaceView == null)
            return;
        surfaceView.release(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:

                break;
            case R.id.ly_subtitle:
                break;
            case R.id.ly_resolution:
                surfaceView.toggleAspectRatio();
                break;

            case R.id.ly_play:
                if (surfaceView.isPlaying()) {
                    surfaceView.pause();
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    surfaceView.start();
                    img_play.setImageResource(R.drawable.exo_pause);
                }
                break;
            case R.id.ly_fav:
                for(int i = 0; i< vod_favList.size(); i++){
                    if(vod_favList.get(i).getStream_id().equalsIgnoreCase(MyApp.vod_model.getStream_id())){
                        pkg_datas.set(0,"Remove from Fav");
                        is_exit = true;
                        fav_pos = i;
                        break;
                    }else {
                        pkg_datas.set(0,"Add to Fav");
                    }
                }
                if(is_exit){
                    vod_favList.remove(fav_pos);
                    Toast.makeText(this,"This movie has been removed from favorites.",Toast.LENGTH_SHORT).show();
                } else{
                    vod_favList.add(MyApp.vod_model);
                    Toast.makeText(this,"This movie has been added to favorites.",Toast.LENGTH_SHORT).show();
                }
                MyApp.instance.getPreference().put(Constants.getMOVIE_FAV(), vod_favList);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        long totalDuration = surfaceView.getDuration();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        surfaceView.seekTo(currentPosition);
        updateProgressBar();
    }



    private void updateTimer() {
        handler.removeCallbacks(mTicker);
        startTimer();
    }

    int maxTime;
    private void startTimer() {
        maxTime = 10;
        mTicker = new Runnable() {
            public void run() {
                if (maxTime < 1) {
                    if (bottom_lay.getVisibility() == View.VISIBLE)
                        bottom_lay.setVisibility(View.GONE);
                    return;
                }
                runNextTicker();
            }
        };
        mTicker.run();
    }

    private void runNextTicker() {
        maxTime --;
        long next = SystemClock.uptimeMillis() + 1000;
        handler.postAtTime(mTicker, next);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            long curr_pos = surfaceView.getCurrentPosition();
            long max_pos = surfaceView.getDuration();
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (surfaceView.isPlaying()) {
                            surfaceView.pause();
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            surfaceView.start();
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        duration_time += 30;
                        if (curr_pos < duration_time * 1000)
                            surfaceView.seekTo(1);
                        else {
                            int st = (int) (curr_pos - (long) duration_time * 1000);
                            surfaceView.seekTo(st);
                        }
                        duration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        duration_time += 30;
                        if (max_pos < duration_time * 1000)
                            surfaceView.seekTo((int) (max_pos - 10));
                        else surfaceView.seekTo((int) (curr_pos + (long) duration_time * 1000));
                        duration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        if(bottom_lay.getVisibility()==View.VISIBLE){
                            bottom_lay.setVisibility(View.GONE);
                            return true;
                        }
                        releaseMediaPlayer();
                        finish();
                        break;
                    case KeyEvent.KEYCODE_MENU:
                        for(int i = 0; i< vod_favList.size(); i++){
                            if(vod_favList.get(i).getStream_id().equalsIgnoreCase(MyApp.vod_model.getStream_id())){
                                pkg_datas.set(0,"Remove from Fav");
                                is_exit = true;
                                fav_pos = i;
                                break;
                            }else {
                                pkg_datas.set(0,"Add to Fav");
                            }
                        }
                        PackageDlg packageDlg = new PackageDlg(VideoIjkPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
                            @Override
                            public void OnItemClick(Dialog dialog, int position) {
                                dialog.dismiss();
                                is_long = false;
                                switch (position) {
                                    case 0:
                                        if(is_exit)
                                            vod_favList.remove(fav_pos);
                                        else
                                            vod_favList.add(MyApp.vod_model);
                                        MyApp.instance.getPreference().put(Constants.getMOVIE_FAV(), vod_favList);
                                        break;
                                    case 1:
                                        surfaceView.toggleAspectRatio();
                                        break;
                                }
                            }
                        });
                        packageDlg.show();
                        break;
                }
            }
        }catch (Exception e){

        }
        return super.dispatchKeyEvent(event);
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else  {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
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
        def_lay.setVisibility(View.VISIBLE);
        return false;
    }
}
