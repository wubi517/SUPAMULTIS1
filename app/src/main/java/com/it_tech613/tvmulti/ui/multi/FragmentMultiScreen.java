package com.it_tech613.tvmulti.ui.multi;

import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.liveTv.MulteScreenMenuDialog;
import com.it_tech613.tvmulti.utils.MyFragment;
import com.it_tech613.tvmulti.utils.Utils;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WINDOW_SERVICE;
import static com.it_tech613.tvmulti.apps.MyApp.num_screen;

public class FragmentMultiScreen extends MyFragment implements View.OnClickListener, SurfaceHolder.Callback, IVLCVout.Callback {

    private String TAG=this.getClass().getSimpleName();
    private List<SurfaceHolder> surfaceHolderList=new ArrayList<>(4);
    private List<SurfaceView> surfaceList=new ArrayList<>(4);
    private List<String> contentUriList=new ArrayList<>(4);
    private List<ImageView> imageViewList=new ArrayList<>(4);
    private List<Boolean> muteList=new ArrayList<>(4);
    private List<LibVLC> libVlcList=new ArrayList<>(4);
    private List<MediaPlayer> mMediaPlayerList = new ArrayList<>(4);
    private List<Integer> categoryPosList=new ArrayList<>(4);
    private List<Integer> channelPosList=new ArrayList<>(4);
    private List<RelativeLayout> laySurfaceList =new ArrayList<>(4);
    private boolean is_create= true;
    private List<MediaPlayer.EventListener> mPlayerListenerList = new ArrayList<>(4);
    private int mVideoWidth,mVideoHeight;
    private boolean is_full=false;
    private int[] coordinates=new int[2];
    private int[] size = new int[2];
    private int selected_screen_id=-1;
    private ConstraintLayout rootview;
    private LinearLayout linearLayout1, linearLayout2;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)requireActivity()).toggleFullScreen(true);
        surfaceHolderList=new ArrayList<>(4);
        surfaceList=new ArrayList<>(4);
        contentUriList=new ArrayList<>(4);
        imageViewList=new ArrayList<>(4);
        muteList=new ArrayList<>(4);
        libVlcList=new ArrayList<>(4);
        mMediaPlayerList = new ArrayList<>(4);
        categoryPosList=new ArrayList<>(4);
        channelPosList=new ArrayList<>(4);
        laySurfaceList =new ArrayList<>(4);
        mPlayerListenerList = new ArrayList<>(4);
        switch (num_screen){
            case 4:
                return inflater.inflate(R.layout.fragment_multi_screen_four, container, false);
            case 3:
                return inflater.inflate(R.layout.fragment_multi_screen_three, container, false);
            case 2:
                return inflater.inflate(R.layout.fragment_multi_screen_two, container, false);
        }
        return inflater.inflate(R.layout.fragment_multi_screen_four, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SurfaceView surfaceView1, surfaceView2, surfaceView3, surfaceView4;
        SurfaceHolder holder1,holder2, holder3, holder4;
        for (int i = 0; i< num_screen; i++){
            categoryPosList.add(0);
            channelPosList.add(0);
            contentUriList.add("");
            libVlcList.add(null);
            mMediaPlayerList.add(null);
            mPlayerListenerList.add(new MediaPlayerListener(this));
            muteList.add(true);
        }
        if (num_screen==2 || num_screen==3) rootview=view.findViewById(R.id.rootview);
        else {
            linearLayout1=view.findViewById(R.id.linearLayout1);
            linearLayout2=view.findViewById(R.id.linearLayout2);
        }

        view.findViewById(R.id.lay1).setOnClickListener(this);
        imageViewList.add(0,(ImageView) view.findViewById(R.id.mute1));
        view.findViewById(R.id.mute1).setOnClickListener(this);
        surfaceView1=view.findViewById(R.id.surface_view1);
        surfaceList.add(0, surfaceView1);
        holder1=surfaceView1.getHolder();
        holder1.addCallback(this);
        holder1.setFormat(PixelFormat.RGBX_8888);
        laySurfaceList.add(0,(RelativeLayout) view.findViewById(R.id.lay1));
        surfaceHolderList.add(0,holder1);

        view.findViewById(R.id.lay2).setOnClickListener(this);
        imageViewList.add(1,(ImageView) view.findViewById(R.id.mute2));
        view.findViewById(R.id.mute2).setOnClickListener(this);
        surfaceView2=view.findViewById(R.id.surface_view2);
        surfaceList.add(1, surfaceView2);
        holder2=surfaceView2.getHolder();
        holder2.addCallback(this);
        holder2.setFormat(PixelFormat.RGBX_8888);
        laySurfaceList.add(1,(RelativeLayout) view.findViewById(R.id.lay2));
        surfaceHolderList.add(1,holder2);

        if (num_screen==4){
            view.findViewById(R.id.lay3).setOnClickListener(this);
            view.findViewById(R.id.mute3).setOnClickListener(this);
            imageViewList.add(2,(ImageView) view.findViewById(R.id.mute3));
            surfaceView3=view.findViewById(R.id.surface_view3);
            surfaceList.add(2, surfaceView3);
            holder3=surfaceView3.getHolder();
            holder3.addCallback(this);
            holder3.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add(2,(RelativeLayout) view.findViewById(R.id.lay3));
            surfaceHolderList.add(2,holder3);

            view.findViewById(R.id.lay4).setOnClickListener(this);
            view.findViewById(R.id.mute4).setOnClickListener(this);
            imageViewList.add(3,(ImageView) view.findViewById(R.id.mute4));
            surfaceView4=view.findViewById(R.id.surface_view4);
            surfaceList.add(3, surfaceView4);
            holder4=surfaceView4.getHolder();
            holder4.addCallback(this);
            holder4.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add(3,(RelativeLayout) view.findViewById(R.id.lay4));
            surfaceHolderList.add(3,holder4);
        }else if (num_screen==3){
            view.findViewById(R.id.lay3).setOnClickListener(this);
            view.findViewById(R.id.mute3).setOnClickListener(this);
            imageViewList.add(2,(ImageView) view.findViewById(R.id.mute3));
            surfaceView3=view.findViewById(R.id.surface_view3);
            surfaceList.add(2, surfaceView3);
            holder3=surfaceView3.getHolder();
            holder3.addCallback(this);
            holder3.setFormat(PixelFormat.RGBX_8888);
            laySurfaceList.add(2,(RelativeLayout) view.findViewById(R.id.lay3));
            surfaceHolderList.add(2,holder3);
        }
        for (int i = 0; i< num_screen; i++){
            setSize(surfaceHolderList.get(i));
            final int finalI = i;
            laySurfaceList.get(finalI).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b)
                        for (int j=0;j<num_screen;j++) {
                            if (finalI !=j) mute(j);
                            else unMute(j);
                        }
                }
            });
        }
        //For initial video
        setChannel(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.deleteCache(requireContext());
    }

    private void setChannel(final int i) {
//        if ( mMediaPlayerList.get(i)!=null) mMediaPlayerList.get(i).pause();
        Log.e(TAG,"setchannel clicked "+i);
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        assert fm != null;
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("fragment_alert");
        if (prev != null) {
            ft.remove(prev);
            ft.addToBackStack(null);
            return;
        }
        SelectChannelDialog selectChannelDialog = SelectChannelDialog.newInstance(i,categoryPosList.get(i),channelPosList.get(i));
        selectChannelDialog.setSelectChannelListener(new SelectChannelDialog.SelectChannel() {
            @Override
            public void onSelected(int category_pos, int channel_pos, EPGChannel channelModel) {
                releaseMediaPlayer(i);
                categoryPosList.set(i,category_pos);
                channelPosList.set(i,channel_pos);
                contentUriList.set(i, MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user,MyApp.pass,channelModel.getStream_id()+"","ts"));
                playVideo(i);
                laySurfaceList.get(i).requestFocus();
            }
        });
        selectChannelDialog.show(fm, "fragment_alert");
        laySurfaceList.get(i).requestFocus();
    }

    private void releaseMediaPlayer(int i) {
        Log.e(TAG,"release player "+i);
        if (libVlcList.size()!=num_screen || libVlcList.get(i) == null)
            return;
        if (mMediaPlayerList.get(i) != null) {
            mMediaPlayerList.get(i).stop();
            final IVLCVout vout = mMediaPlayerList.get(i).getVLCVout();
            vout.removeCallback(this);
            vout.detachViews();
        }

        if (surfaceHolderList.size()!=num_screen)surfaceHolderList.add(i,null);
        else surfaceHolderList.set(i,null);
        libVlcList.get(i).release();
        if (libVlcList.size()!=num_screen) libVlcList.add(i,null);
        else libVlcList.set(i,null);
    }

    @Override
    public void onPause() {
        super.onPause();
        for (int i=0;i<num_screen;i++) {
            releaseMediaPlayer(i);
        }
    }

    private void playVideo(int i){
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

            libVlcList.set(i,new LibVLC(requireContext(), options));

            mMediaPlayerList.set(i,new MediaPlayer(libVlcList.get(i)));
            mMediaPlayerList.get(i).setEventListener(mPlayerListenerList.get(i));
            mMediaPlayerList.get(i).setAspectRatio(MyApp.SCREEN_WIDTH+":"+ MyApp.SCREEN_HEIGHT);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerList.get(i).getVLCVout();
            vout.setVideoView(surfaceList.get(i));
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVlcList.get(i), Uri.parse(contentUriList.get(i)));
            mMediaPlayerList.get(i).setMedia(m);
            mMediaPlayerList.get(i).play();
            Log.e(TAG,"started video "+i);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private void setSize(SurfaceHolder holder){
        WindowManager wm = (WindowManager) requireContext().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        mVideoHeight = displayMetrics.heightPixels/2;
        mVideoWidth = displayMetrics.widthPixels/2;
        Log.e(TAG,"size "+mVideoWidth+" : "+mVideoHeight);
        holder.setFixedSize(mVideoWidth, mVideoHeight);
    }
    
    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        View view = requireActivity().getCurrentFocus();
        if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MENU:
                    switch (view.getId()){
                        case R.id.lay1:
                            showMenu(0);
                            break;
                        case R.id.lay2:
                            showMenu(1);
                            break;
                        case R.id.lay3:
                            showMenu(2);
                            break;
                        case R.id.lay4:
                            showMenu(3);
                            break;
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                    if (!is_full){
                        for (int i=0;i<num_screen;i++) {
                            releaseMediaPlayer(i);
                        }
                        ((MainActivity)requireActivity()).toggleFullScreen(false);
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(0))//FragmentCatchupDetail
                                .addToBackStack(null).commit();
                        return false;
                    }else {
                        for (int i=0;i<num_screen;i++) {
                            if (i!=selected_screen_id) releaseMediaPlayer(i);
                        }
                        toggleFullScreen(selected_screen_id);
                        return false;
                    }
            }
        }
        return super.myOnKeyDown(event);
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

    private void showMenu(final int i) {
        selected_screen_id=i;
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
        if (mMediaPlayerList.get(i)!=null) isPlaying=mMediaPlayerList.get(i).isPlaying();
        else isPlaying=true;
        MulteScreenMenuDialog selectChannelDialog = MulteScreenMenuDialog.newInstance(isPlaying, muteList.get(i));
        selectChannelDialog.setMultiScreenMenuListenerListener(new MulteScreenMenuDialog.MultiScreenMenuListener() {
            @Override
            public void onPlus() {
                setChannel(i);
            }

            @Override
            public void onFullScreen() {
                toggleFullScreen(selected_screen_id);
                laySurfaceList.get(i).requestFocus();
            }

            @Override
            public void onPlayPause(boolean is_playing) {
                if (is_playing) mMediaPlayerList.get(i).pause();
                else mMediaPlayerList.get(i).play();
            }

            @Override
            public void onSoundMute(boolean is_mute) {
                toggleMute(i);
            }
        });
        selectChannelDialog.show(fm, "fragment_menu");
    }

    private void toggleMute(int i) {
        Log.e(TAG,"mute clicked "+i);
        ImageView imageView=imageViewList.get(i);
        if (muteList.get(i)){
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,false);
                imageView.setImageResource(R.drawable.sound_mute);
                mMediaPlayerList.get(i).setVolume(0);
            }
        }else {
            if (mMediaPlayerList.get(i)!=null) {
                muteList.set(i,true);
                imageView.setImageResource(R.drawable.sound);
                mMediaPlayerList.get(i).setVolume(100);
            }
        }
    }

    private void unMute(int i){
        Log.e(TAG,"unMute "+i);
        ImageView imageView=imageViewList.get(i);
        if (mMediaPlayerList.get(i)!=null) {
            muteList.set(i,true);
            imageView.setImageResource(R.drawable.sound);
            mMediaPlayerList.get(i).setVolume(100);
        }
    }

    private void mute(int i){
        Log.e(TAG,"mute "+i);
        ImageView imageView=imageViewList.get(i);
        if (mMediaPlayerList.get(i)!=null) {
            muteList.set(i,false);
            imageView.setImageResource(R.drawable.sound_mute);
            mMediaPlayerList.get(i).setVolume(0);
        }
    }

    private void toggleFullScreen(int i) {
        if (!is_full){
            is_full = true;
            for (int j=0;j<num_screen;j++) {
                if (i==j) {
//                    playVideo(i);
                    laySurfaceList.get(j).setVisibility(View.VISIBLE);
                    surfaceList.get(j).setVisibility(View.VISIBLE);
                }
                else {
                    releaseMediaPlayer(j);
                    laySurfaceList.get(j).setVisibility(View.GONE);
                    surfaceList.get(j).setVisibility(View.GONE);
                }
            }
            switch (num_screen){
                case 2:
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    if (i==0){
                        constraintSet.setGuidelinePercent(R.id.guideline1, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                    }else {
                        constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                    }
                    TransitionManager.beginDelayedTransition(rootview);
                    constraintSet.applyTo(rootview);
                    break;
                case 3:
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    if (i==0){
                        constraintSet.setGuidelinePercent(R.id.guideline1, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline3, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 1.0f);
                    }else if (i==1){
                        constraintSet.setGuidelinePercent(R.id.guideline2, 1.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 0.0f);
                    }else {
                        constraintSet.setGuidelinePercent(R.id.guideline2, 0.0f);
                        constraintSet.setGuidelinePercent(R.id.guideline4, 0.0f);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 4:
                    linearLayout1.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    for (int j=0;j<4;j++) {
                        if (i==j) {
                            if (i<2) {
                                linearLayout2.setVisibility(View.GONE);
                            }else {
                                linearLayout1.setVisibility(View.GONE);
                            }
                        }
                    }
                    break;
            }
        }else {
            is_full = false;
            for (int j=0;j<num_screen;j++) {
                if (i!=j) {
                    laySurfaceList.get(j).setVisibility(View.VISIBLE);
                    surfaceList.get(j).setVisibility(View.VISIBLE);
                    playVideo(j);
                    mute(j);
                }
            }
            switch (num_screen){
                case 2:
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    constraintSet.setGuidelinePercent(R.id.guideline1, 0.5f);
                    constraintSet.setGuidelinePercent(R.id.guideline2, 0.25f);
                    constraintSet.setGuidelinePercent(R.id.guideline3, 0.75f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 3:
                    constraintSet = new ConstraintSet();
                    constraintSet.clone(rootview);
                    constraintSet.setGuidelinePercent(R.id.guideline1, 0.25f);
                    constraintSet.setGuidelinePercent(R.id.guideline2, 0.5f);
                    constraintSet.setGuidelinePercent(R.id.guideline3, 0.75f);
                    constraintSet.setGuidelinePercent(R.id.guideline4, 0.5f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        TransitionManager.beginDelayedTransition(rootview);
                    }
                    constraintSet.applyTo(rootview);
                    break;
                case 4:
                    linearLayout1.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mute1:
                showMenu(0);
                break;
            case R.id.mute2:
                showMenu(1);
                break;
            case R.id.mute3:
                showMenu(2);
                break;
            case R.id.mute4:
                showMenu(3);
                break;
            case R.id.lay1:
                setChannel(0);
                break;
            case R.id.lay2:
                setChannel(1);
                break;
            case R.id.lay3:
                setChannel(2);
                break;
            case R.id.lay4:
                setChannel(3);
                break;
        }
    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<FragmentMultiScreen> mOwner;

        public MediaPlayerListener(FragmentMultiScreen owner) {
            mOwner = new WeakReference<FragmentMultiScreen>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            FragmentMultiScreen player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
//                    player.releaseMediaPlayer(0);
                    player.is_create = false;
//                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
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
