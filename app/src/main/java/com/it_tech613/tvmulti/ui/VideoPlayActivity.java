package com.it_tech613.tvmulti.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.Gson;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.DataModel;
import com.it_tech613.tvmulti.models.MovieModel;
import com.it_tech613.tvmulti.ui.movies.PackageDlg;
import com.it_tech613.tvmulti.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener, IVLCVout.Callback,View.OnClickListener, IVLCVout.OnNewVideoLayoutListener {
    private static final String TAG = "JavaActivity";
    private MediaPlayer mMediaPlayer = null;
    public int mHeight;
    public int mWidth;
    LibVLC libvlc;
    private SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay,ly_play,ly_resolution,ly_audio,ly_subtitle,ly_fav;
    ImageView img_play;

    MediaPlayer.TrackDescription[] tracks;
    MediaPlayer.TrackDescription[] subTracks;
    String ratio;
    String[] resolutions;
    int current_resolution = 0;
    boolean first = true;

    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,rss="";
    int duration_time = 0,selected_item = 0,position,fav_pos = 0,msg_time = 0;
    List<MovieModel> vod_favList;
    boolean is_create = true,is_long=false;
    boolean is_exit = false,is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    private FrameLayout mVideoSurfaceFrame = null;
    private View.OnLayoutChangeListener mOnLayoutChangeListener = null;

    private int mVideoHeight = 0;
    private int mVideoWidth = 0;
    private int mVideoVisibleHeight = 0;
    private int mVideoVisibleWidth = 0;
    private int mVideoSarNum = 0;
    private int mVideoSarDen = 0;
    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_SCREEN = 1;
    private static final int SURFACE_FILL = 2;
    private static final int SURFACE_16_9 = 3;
    private static final int SURFACE_4_3 = 4;
    private static final int SURFACE_ORIGINAL = 5;
    private static int CURRENT_SIZE = SURFACE_BEST_FIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_player);
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
                        updateVideoSurfaces();
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
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.setOnClickListener(view -> {
            if(bottom_lay.getVisibility()== View.VISIBLE){
                bottom_lay.setVisibility(View.GONE);
            }else {
                bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
            }
        });
        holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.RGBX_8888);
        def_lay = (LinearLayout) findViewById(R.id.def_lay);
        bottom_lay = (LinearLayout) findViewById(R.id.vod_bottom_lay);
        ly_fav = findViewById(R.id.ly_fav);
        ly_fav.setOnClickListener(this);
        title_txt = (TextView) findViewById(R.id.vod_channel_title);
        imageView = (ImageView) findViewById(R.id.vod_channel_img);
        start_txt = (TextView) findViewById(R.id.vod_start_time);
        end_txt = (TextView) findViewById(R.id.vod_end_time);
        seekBar = (SeekBar) findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);
        remote_subtitles_surface = findViewById(R.id.remote_subtitles_surface);
        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        ly_audio = findViewById(R.id.ly_audio);

        ly_play = findViewById(R.id.ly_play);
        ly_resolution = findViewById(R.id.ly_resolution);
        ly_subtitle = findViewById(R.id.ly_subtitle);

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

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        mHeight = displayMetrics.heightPixels;
        mWidth = displayMetrics.widthPixels;

        ratio = MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT;
        resolutions =  new String[]{"16:9", "4:3", ratio};

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
                        Animation bottomToTop = AnimationUtils.loadAnimation(VideoPlayActivity.this, R.anim.bottom_to_top);
                        txt_rss.clearAnimation();
                        txt_rss.startAnimation(bottomToTop);
                    }else {
                        lay_header.setVisibility(View.GONE);
                    }
                    rssTimer();
                } else {
                    Toast.makeText(VideoPlayActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
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

    private void changeMediaPlayerLayout(int displayW, int displayH) {
        /* Change the video placement using the MediaPlayer API */
        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_FIT_SCREEN:
            case SURFACE_FILL: {
                Media.VideoTrack vtrack = mMediaPlayer.getCurrentVideoTrack();
                if (vtrack == null)
                    return;
                final boolean videoSwapped = vtrack.orientation == Media.VideoTrack.Orientation.LeftBottom
                        || vtrack.orientation == Media.VideoTrack.Orientation.RightTop;
                if (CURRENT_SIZE == SURFACE_FIT_SCREEN) {
                    int videoW = vtrack.width;
                    int videoH = vtrack.height;

                    if (videoSwapped) {
                        int swap = videoW;
                        videoW = videoH;
                        videoH = swap;
                    }
                    if (vtrack.sarNum != vtrack.sarDen)
                        videoW = videoW * vtrack.sarNum / vtrack.sarDen;

                    float ar = videoW / (float) videoH;
                    float dar = displayW / (float) displayH;

                    float scale;
                    if (dar >= ar)
                        scale = displayW / (float) videoW; /* horizontal */
                    else
                        scale = displayH / (float) videoH; /* vertical */
                    mMediaPlayer.setScale(scale);
                    mMediaPlayer.setAspectRatio(null);
                } else {
                    mMediaPlayer.setScale(0);
                    mMediaPlayer.setAspectRatio(!videoSwapped ? "" + displayW + ":" + displayH
                            : "" + displayH + ":" + displayW);
                }
                break;
            }
            case SURFACE_16_9:
                mMediaPlayer.setAspectRatio("16:9");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_4_3:
                mMediaPlayer.setAspectRatio("4:3");
                mMediaPlayer.setScale(0);
                break;
            case SURFACE_ORIGINAL:
                mMediaPlayer.setAspectRatio(null);
                mMediaPlayer.setScale(1);
                break;
        }
    }

    private void updateVideoSurfaces() {
        int sw = getWindow().getDecorView().getWidth();
        int sh = getWindow().getDecorView().getHeight();

        // sanity check
        if (sw * sh == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }

        mMediaPlayer.getVLCVout().setWindowSize(sw, sh);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (mVideoWidth * mVideoHeight == 0) {
            /* Case of OpenGL vouts: handles the placement of the video using MediaPlayer API */
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceView.setLayoutParams(lp);
            lp = mVideoSurfaceFrame.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoSurfaceFrame.setLayoutParams(lp);
            changeMediaPlayerLayout(sw, sh);
            return;
        }

        if (lp.width == lp.height && lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            /* We handle the placement of the video using Android View LayoutParams */
            mMediaPlayer.setAspectRatio(null);
            mMediaPlayer.setScale(0);
        }

        double dw = sw, dh = sh;
        final boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (sw > sh && isPortrait || sw < sh && !isPortrait) {
            dw = sh;
            dh = sw;
        }

        // compute the aspect ratio
        double ar, vw;
        if (mVideoSarDen == mVideoSarNum) {
            /* No indication about the density, assuming 1:1 */
            vw = mVideoVisibleWidth;
            ar = (double) mVideoVisibleWidth / (double) mVideoVisibleHeight;
        } else {
            /* Use the specified aspect ratio */
            vw = mVideoVisibleWidth * (double) mVideoSarNum / mVideoSarDen;
            ar = vw / mVideoVisibleHeight;
        }

        // compute the display aspect ratio
        double dar = dw / dh;

        switch (CURRENT_SIZE) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_FIT_SCREEN:
                if (dar >= ar)
                    dh = dw / ar; /* horizontal */
                else
                    dw = dh * ar; /* vertical */
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = dw / ar;
                else
                    dw = dh * ar;
                break;
            case SURFACE_ORIGINAL:
                dh = mVideoVisibleHeight;
                dw = vw;
                break;
        }

        // set display size
        lp.width = (int) Math.ceil(dw * mVideoWidth / mVideoVisibleWidth);
        lp.height = (int) Math.ceil(dh * mVideoHeight / mVideoVisibleHeight);
        surfaceView.setLayoutParams(lp);
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.setLayoutParams(lp);

        // set frame size (crop if necessary)
        lp = mVideoSurfaceFrame.getLayoutParams();
        lp.width = (int) Math.floor(dw);
        lp.height = (int) Math.floor(dh);
        mVideoSurfaceFrame.setLayoutParams(lp);

        surfaceView.invalidate();
        if (remote_subtitles_surface != null)
            remote_subtitles_surface.invalidate();
    }

    private void playVideo(String path) {
        if(def_lay.getVisibility()==View.VISIBLE)def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
//        Log.e("url",path);
        toggleFullscreen(true);
        try {

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("0");//this option is used to show the first subtitle track
            options.add("--subsdec-encoding");

            libvlc = new LibVLC(this, options);

            // Creating media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);


            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);

            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.setWindowSize(mWidth, mHeight);
            vout.addCallback(this);
            vout.attachViews(this);
//            vout.setSubtitlesView(tv_subtitle);


            Log.e("VideoPlay",path);
            Media m = new Media(libvlc, Uri.parse(path));
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();

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
            if (mMediaPlayer != null) {
                if (tracks == null && subTracks == null) {
                    first = false;
                    tracks = mMediaPlayer.getAudioTracks();
                    subTracks = mMediaPlayer.getSpuTracks();
                }
                long totalDuration = mMediaPlayer.getLength();
                long currentDuration = mMediaPlayer.getTime();
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
            if (libvlc != null) {
                releaseMediaPlayer();
                surfaceView = null;
            }
            surfaceView = (SurfaceView) findViewById(R.id.surface_view);
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mHeight = displayMetrics.heightPixels;
            mWidth = displayMetrics.widthPixels;
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
        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AUDIO_TRACK", 0);
        editor.commit();

        SharedPreferences pref2 = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = pref2.edit();
        editor1.putInt("SUB_TRACK", 0);
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
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
//        mMediaPlayer.release();
        holder = null;
        libvlc.release();
        libvlc = null;

        mWidth = 0;
        mHeight = 0;
    }
    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        mVideoWidth = width;
        mVideoHeight = height;
        mVideoVisibleWidth = visibleWidth;
        mVideoVisibleHeight = visibleHeight;
        mVideoSarNum = sarNum;
        mVideoSarDen = sarDen;
        updateVideoSurfaces();
    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoPlayActivity> mOwner;

        public MediaPlayerListener(VideoPlayActivity owner) {
            mOwner = new WeakReference<VideoPlayActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoPlayActivity player = mOwner.get();
            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    //동영상 끝까지 재생되었다면..
                    player.releaseMediaPlayer();
                    player.is_create = false;
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
//                    Toast.makeText(player, "Playing", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
//                    Toast.makeText(player, "Stop", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.Buffering:
//                    Toast.makeText(player, "Buffering", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayer.Event.EncounteredError:
//                    Toast.makeText(player, "Error", Toast.LENGTH_SHORT).show();
                    player.def_lay.setVisibility(View.VISIBLE);
                    break;

                //아래 두 이벤트는 계속 발생됨
                case MediaPlayer.Event.TimeChanged: //재생 시간 변화
                    break;
                case MediaPlayer.Event.PositionChanged: //동영상 재생 구간 변화시
                    //Log.d(TAG, "PositionChanged");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                if (tracks != null) {
                    if (tracks.length > 0) {
                        showAudioTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                }

                break;

            case R.id.ly_subtitle:
                if (subTracks != null) {
                    if (subTracks.length > 0) {
                        showSubTracksList();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.ly_resolution:
                current_resolution++;
                if (current_resolution == resolutions.length)
                    current_resolution = 0;

                mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                break;

            case R.id.ly_play:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    mMediaPlayer.play();
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
        long totalDuration = mMediaPlayer.getLength();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        mMediaPlayer.setTime(currentPosition);
        updateProgressBar();
    }


    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }


    private void updateTimer() {
        handler.removeCallbacks(mTicker);
        startTimer();
    }

    int maxTime;
    private void startTimer() {
        maxTime = 10;
        mTicker = () -> {
            if (maxTime < 1) {
                if (bottom_lay.getVisibility() == View.VISIBLE)
                    bottom_lay.setVisibility(View.GONE);
                return;
            }
            runNextTicker();
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
            long curr_pos = mMediaPlayer.getTime();
            long max_pos = mMediaPlayer.getLength();
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.pause();
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            mMediaPlayer.play();
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        duration_time += 30;
                        if (curr_pos < duration_time * 1000)
                            mMediaPlayer.setTime(1);
                        else {
                            long st = (long) (curr_pos - (long) duration_time * 1000);
                            mMediaPlayer.setTime(st);
                        }
                        duration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        duration_time += 30;
                        if (max_pos < duration_time * 1000)
                            mMediaPlayer.setTime((long) (max_pos - 10));
                        else mMediaPlayer.setTime((long) (curr_pos + (long) duration_time * 1000));
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
                        PackageDlg packageDlg = new PackageDlg(VideoPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
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
                                        if (subTracks != null) {
                                            if (subTracks.length > 0) {
                                                showSubTracksList();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No subtitle or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case 2:
                                        if (tracks != null) {
                                            if (tracks.length > 0) {
                                                showAudioTracksList();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "No audio tracks or not loading yet", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case 3:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
                                        builder.setTitle("Select Screen Mode");
                                        String[] screen_mode_list = {"16:9", "4:3", "Full Screen"};
                                        builder.setSingleChoiceItems(screen_mode_list, current_resolution,
                                                (dialog1, which) -> current_resolution=which);
                                        builder.setPositiveButton("OK", (dialog12, which) -> {
//                                                current_resolution=which;
                                            mMediaPlayer.setAspectRatio(resolutions[current_resolution]);
                                        });
                                        builder.setNegativeButton("Cancel", null);

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();
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

    private void showAudioTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
        builder.setTitle("Audio track");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < tracks.length; i++) {
            names.add(tracks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("AUDIO_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                (dialog, which) -> selected_item = which);

        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedPreferences pref1 = getSharedPreferences("PREF_AUDIO_TRACK", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref1.edit();
            editor.putInt("AUDIO_TRACK", selected_item);
            editor.commit();

            mMediaPlayer.setAudioTrack(tracks[selected_item].id);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSubTracksList() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayActivity.this);
        builder.setTitle("Subtitle");

        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < subTracks.length; i++) {
            names.add(subTracks[i].name);
        }
        String[] audioTracks = names.toArray(new String[0]);

        SharedPreferences pref = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
        int checkedItem = pref.getInt("SUB_TRACK", 0);
        builder.setSingleChoiceItems(audioTracks, checkedItem,
                (dialog, which) -> selected_item = which);

        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedPreferences pref1 = getSharedPreferences("PREF_SUB_TRACK", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref1.edit();
            editor.putInt("SUB_TRACK", selected_item);
            editor.commit();
            mMediaPlayer.setSpuTrack(subTracks[selected_item].id);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
