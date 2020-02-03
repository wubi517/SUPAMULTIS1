package com.it_tech613.tvmulti.ui.tvGuide;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.ui.*;
import com.it_tech613.tvmulti.ui.liveTv.CategoryAdapter;
import com.it_tech613.tvmulti.ui.liveTv.PinDlg;
import com.it_tech613.tvmulti.utils.MyFragment;

import kotlin.Unit;
import kotlin.jvm.functions.Function3;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.Context.WINDOW_SERVICE;

public class FragmentTvGuide extends MyFragment implements IVLCVout.Callback, SurfaceHolder.Callback, View.OnClickListener {
    private String TAG=getClass().getSimpleName();
    private EpgAdapter epgAdapter;
    private SimpleDraweeView current_channel_image;
    private RecyclerView category_recycler_view,epg_recyclerview;
    private TextView duration, title, content, channel_name;
    private int categoryPos=0, channelPos=0, programPos;
    private boolean is_data_loaded = false;
    private RelativeLayout ly_surface;
    public static SurfaceView surfaceView;
    SurfaceView remote_subtitles_surface;
    private SurfaceHolder holder;
    MediaPlayer.TrackDescription[] traks;
    MediaPlayer.TrackDescription[] subtraks;
    LinearLayout def_lay;
    String ratio;
    String[] resolutions ;
    int current_resolution = 0;
    private int mVideoWidth;
    private int mVideoHeight;
    private EPGChannel selectedEpgChannel;
    private MediaPlayer mMediaPlayer = null;
    LibVLC libvlc=null;
    private String contentUri;
    SimpleDateFormat time = new SimpleDateFormat("d MMM hh:mm a");
    private TextView txt_time;
    private CategoryAdapter categoryAdapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tv_guide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        epg_recyclerview = view.findViewById(R.id.epg_recyclerview);
        epg_recyclerview.setLayoutManager(new LinearLayoutManager(requireContext()));

        category_recycler_view = view.findViewById(R.id.category_recyclerview);
        category_recycler_view.setLayoutManager(new LinearLayoutManager(requireContext()));
        txt_time = view.findViewById(R.id.txt_time);
        def_lay = view.findViewById(R.id.def_lay);
        surfaceView = view.findViewById(R.id.surface_view);
        ly_surface = view.findViewById(R.id.ly_surface);
        remote_subtitles_surface = view.findViewById(R.id.remote_subtitles_surface);
        current_channel_image = view.findViewById(R.id.current_channel_image);
        duration = view.findViewById(R.id.textView4);
        title = view.findViewById(R.id.textView7);
        content = view.findViewById(R.id.textView8);
        channel_name = view.findViewById(R.id.channel_name);
        if (!is_data_loaded){
            MyApp.instance.getKpHUD().setLabel("Please wait while we download the latest epg data").show();
            new Thread(this::callAllEpg).start();
        }else setUI();
        Thread myThread;
        Runnable runnable = new CountDownRunner();
        myThread = new Thread(runnable);
        myThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseMediaPlayer();
    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    class CountDownRunner implements Runnable {
        // @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    doWork();
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void doWork() {
        requireActivity().runOnUiThread(() -> {
            try {
                txt_time.setText(time.format(new Date()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void callAllEpg() {
        try {
            Log.e("BugCheck","getAllEPG start ");
            long startTime = System.nanoTime();
            //api call here
            String inputStream = MyApp.instance.getIptvclient().getAllEPG(MyApp.user, MyApp.pass);
            long endTime = System.nanoTime();

            long MethodeDuration = (endTime - startTime);
            //            Log.e(getClass().getSimpleName(),inputStream);
            Log.e("BugCheck","getAllEPG success "+MethodeDuration);
            if (inputStream==null || inputStream.length()==0) return;
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = null;
            //        Log.e("xml result","received");
            try {
                parser = parserFactory.newSAXParser();
                DefaultHandler handler = new DefaultHandler(){
                    String currentValue = "";
                    boolean currentElement = false;
                    EPGEvent prevEvent=null;
                    EPGEvent currentEvent=null;
                    String channel="";
                    List<EPGChannel> currentChannelList;
                    ArrayList<EPGEvent> epgModels=new ArrayList<>();
                    public void startElement(String uri, String localName,String qName, Attributes attributes) {
                        currentElement = true;
                        currentValue = "";
//                    Log.e("response","received");
                        if(localName.equals("programme")){
//                        Log.e("response","started programs list");
                            currentEvent = new EPGEvent();
                            String start=attributes.getValue(0);
                            String end=attributes.getValue(1);
                            currentEvent.setStart_timestamp(start);//.split(" ")[0]
                            currentEvent.setStop_timestamp(end);//.split(" ")[0]
                            if (!channel.equals(attributes.getValue(2))) {
                                if (currentChannelList !=null && !currentChannelList.isEmpty()) {
                                    Collections.sort(epgModels, (o1, o2) -> o1.getStart_timestamp().compareTo(o2.getStart_timestamp()));
                                    for (EPGChannel epgChannel:currentChannelList)
                                        epgChannel.setEvents(epgModels);
                                }
                                epgModels=new ArrayList<>();
                                channel=attributes.getValue(2);
                                currentChannelList =findChannelByid(channel);
                            }
                        }
                    }
                    public void endElement(String uri, String localName, String qName) {
                        currentElement = false;
                        if (localName.equalsIgnoreCase("title"))
                            currentEvent.setTitle(currentValue);
                        else if (localName.equalsIgnoreCase("desc"))
                            currentEvent.setDec(currentValue);
                        else if (localName.equalsIgnoreCase("programme")) {
                            if (currentChannelList !=null && !currentChannelList.isEmpty())
                                currentEvent.setChannel(currentChannelList.get(0));
                            if (prevEvent!=null){
                                currentEvent.setPreviousEvent(prevEvent);
                                prevEvent.setNextEvent(currentEvent);
                            }
                            prevEvent=currentEvent;
                            epgModels.add(currentEvent);
                        }
                        else if (localName.equalsIgnoreCase("tv")){
                            //
                            is_data_loaded=true;
                            Constants.getLiveFilter();
                            requireActivity().runOnUiThread(()->setUI());
                        }
                    }
                    @Override
                    public void characters(char[] ch, int start, int length) {
                        if (currentElement) {
                            currentValue = currentValue +  new String(ch, start, length);
                        }
                    }
                };

                parser.parse(new InputSource(new StringReader(inputStream)),handler);
            } catch (ParserConfigurationException | SAXException | IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            MyApp.instance.getKpHUD().dismiss();
            requireActivity().runOnUiThread(() -> {
                ConnectionDlg connectionDlg = new ConnectionDlg(requireContext(), new ConnectionDlg.DialogConnectionListener() {
                    @Override
                    public void OnYesClick(Dialog dialog) {
                        dialog.dismiss();
                        new Thread(() -> callAllEpg()).start();
                    }

                    @Override
                    public void OnNoClick(Dialog dialog) {
                        startActivity(new Intent(requireContext(), ConnectionErrorActivity.class));
                    }
                },"LOGIN SUCCESSFUL LOADING DATA",null, null);
                connectionDlg.show();
            });
        }
    }

    private void setUI() {
        initializeHeader(MyApp.fullModels_filter.get(0));
        epgAdapter = new EpgAdapter(MyApp.fullModels_filter.get(0).getChannels(), requireContext(), (integer, integer2, epgChannel, epgEvent) -> {
            //onclicklistener
            playVideo(epgChannel);
//            ((MainActivity)requireActivity()).toggleFullScreen(false);
            return null;
        }, (i_ch, i_pro, epgChannel, epgEvent) -> {
            //onfocuslistener
//            ((MainActivity)requireActivity()).toggleFullScreen(true);
            channelPos=i_ch;
            programPos=i_pro;
            setDescription(epgChannel,epgEvent);
            return null;
        });
        epg_recyclerview.setAdapter(epgAdapter);

        ly_surface.setOnClickListener(this);
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(PixelFormat.RGBX_8888);

        remote_subtitles_surface.setZOrderMediaOverlay(true);
        remote_subtitles_surface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        ViewGroup.LayoutParams params = ly_surface.getLayoutParams();
        params.height = MyApp.SURFACE_HEIGHT;
        params.width = MyApp.SURFACE_WIDTH;
        WindowManager wm = (WindowManager) requireActivity().getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int SCREEN_HEIGHT = displayMetrics.heightPixels;
        int SCREEN_WIDTH = displayMetrics.widthPixels;
        mVideoHeight = displayMetrics.heightPixels;
        mVideoWidth = displayMetrics.widthPixels;
        holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        ratio = mVideoWidth + ":"+ mVideoHeight;
        resolutions =  new String[]{"16:9", "4:3", ratio};
        Log.e("height", String.valueOf(MyApp.SCREEN_HEIGHT));

        categoryAdapter = new CategoryAdapter(MyApp.live_categories_filter, (categoryModel, position, is_clicked) -> {
            if (!is_clicked) return null;
            if (categoryModel.getId()==Constants.xxx_category_id){
                PinDlg pinDlg = new PinDlg(requireContext(), new PinDlg.DlgPinListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                        String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                        if(pin_code.equalsIgnoreCase(pin)){
                            dialog.dismiss();
                            ((MainActivity)requireActivity()).toggleFullScreen(true);
                            category_recycler_view.setVisibility(View.GONE);
                            categoryPos=position;
                            epgAdapter.setList(MyApp.fullModels_filter.get(position).getChannels());
                            initializeHeader(MyApp.fullModels_filter.get(position));
                            epgAdapter.setChannelPos(0);
                            epg_recyclerview.scrollToPosition(0);
                            epg_recyclerview.performClick();
                        }else {
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
            }else {
                ((MainActivity)requireActivity()).toggleFullScreen(true);
                category_recycler_view.setVisibility(View.GONE);
                categoryPos=position;
                epgAdapter.setList(MyApp.fullModels_filter.get(position).getChannels());
                initializeHeader(MyApp.fullModels_filter.get(position));
                epgAdapter.setChannelPos(0);
                epg_recyclerview.scrollToPosition(0);
                epg_recyclerview.performClick();
            }
            return null;
        });
        category_recycler_view.setAdapter(categoryAdapter);
        if (MyApp.instance.getKpHUD().isShowing())
            MyApp.instance.getKpHUD().dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (selectedEpgChannel!=null){
            if (libvlc != null) {
                releaseMediaPlayer();
            }
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.addCallback(this);
            WindowManager wm = (WindowManager) requireActivity().getSystemService(WINDOW_SERVICE);
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(displayMetrics);
            int SCREEN_HEIGHT = displayMetrics.heightPixels;
            int SCREEN_WIDTH = displayMetrics.widthPixels;
            holder.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
            mVideoHeight = displayMetrics.heightPixels;
            mVideoWidth = displayMetrics.widthPixels;
            if (!mMediaPlayer.isPlaying())playChannel(selectedEpgChannel);
        }
    }

    private List<EPGChannel> findChannelByid(String channel_id){
        List<EPGChannel> channelList = new ArrayList<>();
        List<EPGChannel> entireChannels = Constants.getAllFullModel(MyApp.fullModels).getChannels();
        for (EPGChannel epgChannel : entireChannels) {
            if (epgChannel.getId().equals(channel_id)) {
                channelList.add(epgChannel);
            }
        }
        return channelList;
    }

    private void initializeHeader(FullModel fullModel) {
        Log.e(TAG,"initialize header by changing category");
        if (fullModel.getChannels()==null || fullModel.getChannels().size()==0) {
            setDescription(null,null);
            return;
        }
        playChannel(fullModel.getChannels().get(0));
        if (fullModel.getChannels().get(0).getEvents()==null || fullModel.getChannels().get(0).getEvents().size()==0){
            setDescription(fullModel.getChannels().get(0),null);
            return;
        }
        setDescription(fullModel.getChannels().get(0),fullModel.getChannels().get(0).getEvents().get(Constants.findNowEvent(fullModel.getChannels().get(0).getEvents())));//Constants.findNowEvent(fullModel.getChannels().get(0).getEvents())
    }

    private void playVideo(EPGChannel epgChannel) {
        Log.e(TAG,"Start Video");
        if (selectedEpgChannel!=null && selectedEpgChannel.getName().equals(epgChannel.getName())){
            goVideoActivity(selectedEpgChannel);
        }else {
            if(epgChannel.is_locked() && (categoryPos==1 || categoryPos==0)){
                PinDlg pinDlg = new PinDlg(requireContext(), new PinDlg.DlgPinListener() {
                    @Override
                    public void OnYesClick(Dialog dialog, String pin_code) {
                        dialog.dismiss();
                        String pin = (String)MyApp.instance.getPreference().get(Constants.getPIN_CODE());
                        if(pin_code.equalsIgnoreCase(pin)){
                            dialog.dismiss();
                            playChannel(epgChannel);
                        }else {
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
            }else {
                playChannel(epgChannel);
            }
        }
    }

    private void goVideoActivity(EPGChannel epgChannel){
        Log.e(TAG,"Start Video");
        String url = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                epgChannel.getStream_id()+"","ts");
        Log.e(getClass().getSimpleName(),url);
        int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
        Intent intent;
        switch (current_player){
            case 1:
                intent = new Intent(requireContext(), LiveIjkPlayActivity.class);
                break;
            case 2:
                intent = new Intent(requireContext(), LiveExoPlayActivity.class);
                break;
            default:
                intent = new Intent(requireContext(), LivePlayActivity.class);
                break;
        }
        intent.putExtra("title",epgChannel.getName());
        intent.putExtra("img",epgChannel.getImageURL());
        intent.putExtra("url",url);
        intent.putExtra("stream_id",epgChannel.getStream_id());
        intent.putExtra("is_live",true);
        startActivity(intent);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ly_surface:
                goVideoActivity(selectedEpgChannel);
                break;
        }
    }

    private void playChannel(EPGChannel epgChannel) {
        selectedEpgChannel = epgChannel;
        contentUri = MyApp.instance.getIptvclient().buildLiveStreamURL(MyApp.user, MyApp.pass,
                epgChannel.getStream_id()+"","ts");
        Log.e("url",contentUri);
        if(def_lay.getVisibility()== View.VISIBLE) def_lay.setVisibility(View.GONE);
        releaseMediaPlayer();
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

            libvlc = new LibVLC(requireContext(), options);

            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setAspectRatio(MyApp.SCREEN_WIDTH+":"+MyApp.SCREEN_HEIGHT);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(surfaceView);
            if (remote_subtitles_surface != null)
                vout.setSubtitlesView(remote_subtitles_surface);
            vout.setWindowSize(mVideoWidth, mVideoHeight);
            vout.addCallback(this);
            vout.attachViews();


            Media m = new Media(libvlc, Uri.parse(contentUri));
            mMediaPlayer.setMedia(m);
            m.release();
            mMediaPlayer.play();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error in creating player!", Toast
                    .LENGTH_LONG).show();
        }
    }

    private MediaPlayer.EventListener mPlayerListener = new MediaPlayerListener(this);

    private static class MediaPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<FragmentTvGuide> mOwner;

        MediaPlayerListener(FragmentTvGuide owner) {
            mOwner = new WeakReference<FragmentTvGuide>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            FragmentTvGuide player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    player.releaseMediaPlayer();
                    player.onResume();
                    break;
                case MediaPlayer.Event.Playing:
                    break;
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                    break;
                case MediaPlayer.Event.Buffering:
                    break;
                case MediaPlayer.Event.EncounteredError:
                    player.def_lay.setVisibility(View.VISIBLE);
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

    @SuppressLint("SetTextI18n")
    private void setDescription(EPGChannel epgChannel, EPGEvent epgEvent) {
        Log.e(TAG,"initialize header by changing program");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM d, hh:mm");
        if (epgEvent!=null){
            Date that_date = new Date();
            that_date.setTime(epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET);
            Date end_date = new Date();
            end_date.setTime(epgEvent.getEndTime().getTime()+ Constants.SEVER_OFFSET);
            duration.setText(dateFormat1.format(that_date)+" - "+dateFormat.format(end_date));
            title.setText(epgEvent.getTitle());
            content.setText(epgEvent.getDec());
        }else {
            duration.setText("-");
            title.setText(requireContext().getString(R.string.no_information));
            content.setText("");
        }
        if (epgChannel!=null){
            current_channel_image.setImageURI(Uri.parse(epgChannel.getImageURL()));
            channel_name.setText(epgChannel.getName());
        }else {
            current_channel_image.setImageResource(R.drawable.icon);
            channel_name.setText("");
        }
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        if (event.getAction()==KeyEvent.ACTION_UP){
            if (event.getKeyCode()==KeyEvent.KEYCODE_BACK){
                if (category_recycler_view.getVisibility()==View.GONE) {
                    category_recycler_view.setVisibility(View.VISIBLE);
//                    epgAdapter.setChannelPos(channelPos);
//                    categoryAdapter.setSelected(categoryPos);
                    category_recycler_view.scrollToPosition(categoryPos);
                    category_recycler_view.requestFocus();
                    category_recycler_view.performClick();
                }
                else ((MainActivity)requireActivity()).toggleFullScreen(false);
                return false;
            }
        }
        return super.myOnKeyDown(event);
    }
}
