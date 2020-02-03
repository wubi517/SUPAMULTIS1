package com.it_tech613.tvmulti.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.*;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.drm.*;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.*;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsMasterPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylist;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.*;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.ParsingLoadable;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
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

import java.lang.reflect.Constructor;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class VideoExoPlayActivity extends AppCompatActivity implements  SeekBar.OnSeekBarChangeListener,View.OnClickListener,PlaybackPreparer {
    private static final String TAG = "JavaActivity";

    public static final String DRM_SCHEME_EXTRA = "drm_scheme";
    public static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    public static final String DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties";
    public static final String DRM_MULTI_SESSION_EXTRA = "drm_multi_session";
    public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";

    public static final String AD_TAG_URI_EXTRA = "ad_tag_uri";

    public static final String ABR_ALGORITHM_EXTRA = "abr_algorithm";
    private static final String ABR_ALGORITHM_DEFAULT = "default";
    private static final String ABR_ALGORITHM_RANDOM = "random";

    private static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";

    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final CookieManager DEFAULT_COOKIE_MANAGER;
    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private PlayerView playerView;
    private FrameLayout frameLayout;
    private TextView debugTextView;
    private LinearLayout debugRootView;
    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private FrameworkMediaDrm mediaDrm;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private DebugTextViewHelper debugViewHelper;
    private TrackGroupArray lastSeenTrackGroupArray;

    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private AdsLoader adsLoader;
    private Uri loadedAdTagUri;
    private ViewGroup adUiViewGroup;

    SeekBar seekBar;
    LinearLayout bottom_lay, def_lay,ly_play,ly_resolution,ly_audio,ly_subtitle,ly_fav;
    ImageView img_play;


    TextView title_txt, start_txt, end_txt;
    ImageView imageView,image_icon;
    Handler mHandler = new Handler();
    Handler handler = new Handler();
    Runnable mTicker,rssTicker;
    String cont_url,title,rss="";
    int duration_time = 0,fav_pos = 0,msg_time = 0;

    List<MovieModel> vod_favList;
    boolean is_create = true,is_long=false;
    boolean is_exit = false,is_rss = false,is_msg = false, is_live = false;
    List<String>  pkg_datas;
    Handler rssHandler = new Handler();
    TextView txt_rss;
    private RelativeLayout lay_header;
    private FrameLayout mVideoSurfaceFrame = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_exo_player);
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

        mediaDataSourceFactory = buildDataSourceFactory(true);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mVideoSurfaceFrame = findViewById(R.id.video_surface_frame);
        lay_header = findViewById(R.id.lay_header);

        debugRootView = findViewById(R.id.controls_root);
        debugRootView.setVisibility(View.GONE);

        frameLayout = findViewById(R.id.rootVideoPlayerMovieActivity);
        playerView = findViewById(R.id.player_view);
        playerView.setUseController(false);
        playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        playerView.requestFocus();

        debugTextView = findViewById(R.id.debug_text_view);

        changeVideoViewSize();
        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
            clearStartPosition();
        }

        def_lay =  findViewById(R.id.def_lay);
        bottom_lay =  findViewById(R.id.vod_bottom_lay);
        ly_fav = findViewById(R.id.ly_fav);
        ly_fav.setOnClickListener(this);
        title_txt =  findViewById(R.id.vod_channel_title);
        imageView =  findViewById(R.id.vod_channel_img);
        start_txt = findViewById(R.id.vod_start_time);
        end_txt =  findViewById(R.id.vod_end_time);
        seekBar =  findViewById(R.id.vod_seekbar);
        seekBar.setOnSeekBarChangeListener(this);


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


        playVideo(cont_url);
        FullScreencall();
        getRespond();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            playVideo(cont_url);
        }
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
                        Animation bottomToTop = AnimationUtils.loadAnimation(VideoExoPlayActivity.this, R.anim.bottom_to_top);
                        txt_rss.clearAnimation();
                        txt_rss.startAnimation(bottomToTop);
                    }else {
                        lay_header.setVisibility(View.GONE);
                    }
                    rssTimer();
                } else {
                    Toast.makeText(VideoExoPlayActivity.this, "Server Error!", Toast.LENGTH_SHORT).show();
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
//        Log.e("url",path);
        toggleFullscreen(true);
        try {

            if (player == null) {
                Intent intent = this.getIntent();

                Uri[] uris = new Uri[1];
                String[] extensions = new String[1];

                uris[0] = Uri.parse(path);

                DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;
                if (intent.hasExtra(DRM_SCHEME_EXTRA) || intent.hasExtra(DRM_SCHEME_UUID_EXTRA)) {
                    String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL_EXTRA);
                    String[] keyRequestPropertiesArray =
                            intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA);
                    boolean multiSession = intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA, false);
                    int errorStringId = R.string.error_drm_unknown;
                    if (Util.SDK_INT < 18) {
                        errorStringId = R.string.error_drm_not_supported;
                    } else {
                        try {
                            String drmSchemeExtra = intent.hasExtra(DRM_SCHEME_EXTRA) ? DRM_SCHEME_EXTRA
                                    : DRM_SCHEME_UUID_EXTRA;
                            UUID drmSchemeUuid = Util.getDrmUuid(intent.getStringExtra(drmSchemeExtra));
                            if (drmSchemeUuid == null) {
                                errorStringId = R.string.error_drm_unsupported_scheme;
                            } else {
                                drmSessionManager =
                                        buildDrmSessionManagerV18(
                                                drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, multiSession);
                            }
                        } catch (UnsupportedDrmException e) {
                            errorStringId = e.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                                    ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                        }
                    }
                    if (drmSessionManager == null) {
                        showToast(errorStringId);
                        releaseMediaPlayer();
                        return;
                    }
                }

                TrackSelection.Factory trackSelectionFactory;
                String abrAlgorithm = intent.getStringExtra(ABR_ALGORITHM_DEFAULT);
                if (abrAlgorithm == null || ABR_ALGORITHM_DEFAULT.equals(abrAlgorithm)) {
                    trackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
                } else if (ABR_ALGORITHM_RANDOM.equals(abrAlgorithm)) {
                    trackSelectionFactory = new RandomTrackSelection.Factory();
                } else {
                    showToast(R.string.error_unrecognized_abr_algorithm);
                    releaseMediaPlayer();
                    return;
                }

                boolean preferExtensionDecoders =
                        intent.getBooleanExtra(PREFER_EXTENSION_DECODERS_EXTRA, false);
                @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode =
                        MyApp.instance.useExtensionRenderers()
                                ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                                : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
                DefaultRenderersFactory renderersFactory =
                        new DefaultRenderersFactory(this, extensionRendererMode);

                trackSelector = new DefaultTrackSelector(trackSelectionFactory);
                trackSelector.setParameters(trackSelectorParameters);
                lastSeenTrackGroupArray = null;

                player = ExoPlayerFactory.newSimpleInstance(this, renderersFactory, trackSelector, drmSessionManager);
                player.addListener(new PlayerEventListener());
                player.setPlayWhenReady(startAutoPlay);
                player.addAnalyticsListener(new EventLogger(trackSelector));
                playerView.setPlayer(player);
                playerView.setPlaybackPreparer(this);

                debugViewHelper = new DebugTextViewHelper(player, debugTextView);
                debugViewHelper.start();

                MediaSource[] mediaSources = new MediaSource[uris.length];
                for (int i = 0; i < uris.length; i++) {
                    mediaSources[i] = buildMediaSource(uris[i], extensions[i]);
                }
                mediaSource =
                        mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
                String adTagUriString = intent.getStringExtra(AD_TAG_URI_EXTRA);
                if (adTagUriString != null) {
                    Uri adTagUri = Uri.parse(adTagUriString);
                    if (!adTagUri.equals(loadedAdTagUri)) {
                        releaseAdsLoader();
                        loadedAdTagUri = adTagUri;
                    }
                    MediaSource adsMediaSource = createAdsMediaSource(mediaSource, Uri.parse(adTagUriString));
                    if (adsMediaSource != null) {
                        mediaSource = adsMediaSource;
                    } else {
                        showToast(R.string.ima_not_loaded);
                    }
                } else {
                    releaseAdsLoader();
                }
            }
            boolean haveStartPosition = startWindow != C.INDEX_UNSET;
            if (haveStartPosition) {
                player.seekTo(startWindow, startPosition);
            }
            player.prepare(mediaSource, !haveStartPosition, false);

            updateProgressBar();
            updateTimer();
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
            if (player != null) {
                long totalDuration = player.getDuration();
                long currentDuration = player.getCurrentPosition();
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
            if (Util.SDK_INT <= 23 || player == null) {
                playVideo(cont_url);
            }
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
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releaseMediaPlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releaseMediaPlayer();
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
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            debugViewHelper.stop();
            debugViewHelper = null;
            player.release();
            player = null;
            mediaSource = null;
            trackSelector = null;
        }
        releaseMediaDrm();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ly_audio:
                showAudioOptions();
                break;

            case R.id.ly_subtitle:
                showSubtitlesOptions();
                break;
            case R.id.ly_resolution:

                break;

            case R.id.ly_play:
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                    img_play.setImageResource(R.drawable.exo_play);
                } else {
                    player.setPlayWhenReady(true);
                    img_play.setImageResource(R.drawable.exo_pause);
                }
                if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                updateTimer();
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
        long totalDuration = player.getDuration();
        int currentPosition = Utils.progressToTimer(seekBar.getProgress(), totalDuration);
        player.seekTo(currentPosition);
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
            long curr_pos = player.getContentPosition();
            long max_pos = player.getDuration();
            if (event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (player.getPlayWhenReady()) {
                            player.setPlayWhenReady(false);
                            img_play.setImageResource(R.drawable.exo_play);
                        } else {
                            player.setPlayWhenReady(true);
                            img_play.setImageResource(R.drawable.exo_pause);
                        }
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        updateTimer();
                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        duration_time += 30;
                        if (curr_pos < duration_time * 1000)
                            player.seekTo(1);
                        else {
                            long st = (long) (curr_pos - (long) duration_time * 1000);
                            player.seekTo(st);
                        }
                        duration_time = 0;
                        updateProgressBar();
                        updateTimer();
                        if (bottom_lay.getVisibility() == View.GONE) bottom_lay.setVisibility(View.VISIBLE);
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        duration_time += 30;
                        if (max_pos < duration_time * 1000)
                            player.seekTo((long) (max_pos - 10));
                        else player.seekTo((long) (curr_pos + (long) duration_time * 1000));
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
                        PackageDlg packageDlg = new PackageDlg(VideoExoPlayActivity.this, pkg_datas, new PackageDlg.DialogPackageListener() {
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
                                        showSubtitlesOptions();
                                        break;
                                    case 2:
                                        showAudioOptions();
                                        break;
                                    case 3:

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

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return MyApp.instance.buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    @Override
    public void preparePlayback() {

    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.decoderName);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
    }

    private void changeVideoViewSize(){
        WindowManager wm = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            ViewGroup.LayoutParams paramsPlayerView=frameLayout.getLayoutParams();
            paramsPlayerView.height = MyApp.SCREEN_HEIGHT;
            paramsPlayerView.width = MyApp.SCREEN_WIDTH;
            frameLayout.setLayoutParams(paramsPlayerView);
        }
    }

    private void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private void releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader.release();
            adsLoader = null;
            loadedAdTagUri = null;
            playerView.getOverlayFrameLayout().removeAllViews();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            releaseMediaPlayer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);

        super.onSaveInstanceState(outState);
    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void releaseMediaDrm() {
        if (mediaDrm != null) {
            mediaDrm.release();
            mediaDrm = null;
        }
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
        return MyApp.instance.getDownloadTracker().getOfflineStreamKeys(uri);
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
            UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
            throws UnsupportedDrmException {
        HttpDataSource.Factory licenseDataSourceFactory =
                MyApp.instance.buildHttpDataSourceFactory(/* listener= */ null);
        HttpMediaDrmCallback drmCallback =
                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        releaseMediaDrm();
        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
        return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
    }

    private class PlayerEventListener extends Player.DefaultEventListener {

        private  final String LOG_TAG =  PlayerEventListener.class.getSimpleName();

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            super.onPlaybackParametersChanged(playbackParameters);
            Log.d(LOG_TAG,"onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            Log.d(LOG_TAG,"onSeekProcessed");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
        }

        @Override
        public void onPositionDiscontinuity(@Player.DiscontinuityReason int reason) {
            if (player.getPlaybackError() != null) {
                updateStartPosition();
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            if (isBehindLiveWindow(e)) {
                clearStartPosition();
                playVideo(cont_url);
            } else {
                updateStartPosition();
                showControls();
            }
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }

    private void showControls() {
        bottom_lay.setVisibility(View.VISIBLE);
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParserFactory(
                                new HlsPlaylistParserFactory() {
                                    @Override
                                    public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser() {
                                        return null;
                                    }

                                    @Override
                                    public ParsingLoadable.Parser<HlsPlaylist> createPlaylistParser(HlsMasterPlaylist masterPlaylist) {
                                        return null;
                                    }
                                })
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private @Nullable
    MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) {
        try {
            Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
            if (adsLoader == null) {
                Constructor<? extends AdsLoader> loaderConstructor =
                        loaderClass
                                .asSubclass(AdsLoader.class)
                                .getConstructor(android.content.Context.class, Uri.class);
                adsLoader = loaderConstructor.newInstance(this, adTagUri);
                adUiViewGroup = new FrameLayout(this);
                playerView.getOverlayFrameLayout().addView(adUiViewGroup);
            }
            AdsMediaSource.MediaSourceFactory adMediaSourceFactory =
                    new AdsMediaSource.MediaSourceFactory() {
                        @Override
                        public MediaSource createMediaSource(Uri uri) {
                            return buildMediaSource(uri);
                        }

                        @Override
                        public int[] getSupportedTypes() {
                            return new int[] {C.TYPE_DASH, C.TYPE_SS, C.TYPE_HLS, C.TYPE_OTHER};
                        }
                    };
            return new AdsMediaSource(mediaSource, adMediaSourceFactory, adsLoader, (AdsLoader.AdViewProvider) adUiViewGroup);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showAudioOptions(){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = "Audio";
            int rendererIndex = 1;
            /*int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
            boolean allowAdaptiveSelections =
                    rendererType == C.TRACK_TYPE_VIDEO
                            || (rendererType == C.TRACK_TYPE_AUDIO
                            && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);*/
            Pair<android.app.AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog(this, title, trackSelector, rendererIndex);
            dialogPair.second.setShowDisableOption(false);
            dialogPair.second.setAllowAdaptiveSelections(false);
            dialogPair.first.show();
        }
    }

    private void showSubtitlesOptions(){
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            CharSequence title = "Subtitles";
            int rendererIndex = 2;
            /*int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
            boolean allowAdaptiveSelections =
                    rendererType == C.TRACK_TYPE_VIDEO
                            || (rendererType == C.TRACK_TYPE_AUDIO
                            && mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS);*/
            Pair<AlertDialog, TrackSelectionView> dialogPair =
                    TrackSelectionView.getDialog(this, title, trackSelector, rendererIndex);
            dialogPair.second.setShowDisableOption(true);
            dialogPair.second.setAllowAdaptiveSelections(false);
//            dialogPair.second.removeViewAt(4);
            dialogPair.first.show();
        }
    }


}
