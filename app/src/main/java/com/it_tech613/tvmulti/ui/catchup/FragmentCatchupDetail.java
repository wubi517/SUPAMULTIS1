package com.it_tech613.tvmulti.ui.catchup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.CatchupModel;
import com.it_tech613.tvmulti.models.EPGEvent;
import com.it_tech613.tvmulti.ui.LiveExoPlayActivity;
import com.it_tech613.tvmulti.ui.LiveIjkPlayActivity;
import com.it_tech613.tvmulti.ui.LivePlayActivity;
import com.it_tech613.tvmulti.utils.MyFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;


public class FragmentCatchupDetail extends MyFragment {

    private String TAG = getClass().getSimpleName();
    private TextView duration, title, content;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM, yyyy");
    private ProgramsCatchUpAdapter liveTvProgramsAdapter;
    private DateAdapter dateAdapter;
    private Disposable bookSubscription;

    @Override
    public void onStop() {
        super.onStop();
        if (bookSubscription!=null && !bookSubscription.isDisposed()) {
            bookSubscription.dispose();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bookSubscription != null && !bookSubscription.isDisposed()) {
            bookSubscription.dispose();
        }
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catchup_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView dateRecyclerView = view.findViewById(R.id.dateRecyclerView);
        RecyclerView epg_recyclerView = view.findViewById(R.id.epg_recyclerview);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        epg_recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        SimpleDraweeView current_channel_image = view.findViewById(R.id.current_channel_image);
        current_channel_image.setImageURI(Uri.parse(MyApp.selectedChannel.getImageURL()));
        duration = view.findViewById(R.id.textView4);
        title = view.findViewById(R.id.textView7);
        content = view.findViewById(R.id.textView8);
        liveTvProgramsAdapter = new ProgramsCatchUpAdapter(new ArrayList<EPGEvent>(), new Function2<Integer, EPGEvent, Unit>() {
            @Override
            public Unit invoke(Integer integer, EPGEvent epgEvent) {
                //onClickListener
                String url = MyApp.instance.getIptvclient().buildCatchupStreamURL(MyApp.user,MyApp.pass,
                        MyApp.selectedChannel.getStream_id()+"",Constants.catchupFormat.format(epgEvent.getStartTime()),
                        (epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/1000/60);
                int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
                Intent intent;
                switch (current_player){
                    default:
                        intent = new Intent(requireContext(), LivePlayActivity.class);
                        break;
                    case 1:
                        intent = new Intent(requireContext(), LiveIjkPlayActivity.class);
                        break;
                    case 2:
                        intent = new Intent(requireContext(), LiveExoPlayActivity.class);
                        break;
                }
                int duration = (int) ((epgEvent.getEndTime().getTime()-epgEvent.getStartTime().getTime())/1000);
                long start_mil = epgEvent.getStartTime().getTime();
                long now_mil = System.currentTimeMillis();
                intent.putExtra("title",MyApp.selectedChannel.getName());
                intent.putExtra("stream_id",MyApp.selectedChannel.getStream_id());
//                        intent.putExtra("img",MyApp.selectedChannel.getImageURL());
                intent.putExtra("url",url);
                intent.putExtra("duration",duration);
                intent.putExtra("start_mil",start_mil);
                intent.putExtra("now_mil",now_mil);
                intent.putExtra("is_live",false);
                intent.putExtra("dec",epgEvent.getDec());
                intent.putExtra("current_dec",epgEvent.getTitle());
                if(epgEvent.getNextEvent()!=null){
                    intent.putExtra("next_dec",epgEvent.getNextEvent().getTitle());
                }else {
                    intent.putExtra("next_dec","No Information");
                }
                startActivity(intent);

                return null;
            }
        }, new Function2<Integer, EPGEvent, Unit>() {
            @Override
            public Unit invoke(Integer integer, EPGEvent epgEvent) {
                //onFocusListener
                setDescription(epgEvent);
                return null;
            }
        });
        dateAdapter = new DateAdapter(new ArrayList<CatchupModel>(), new Function2<CatchupModel, Integer, Unit>() {
            @Override
            public Unit invoke(CatchupModel catchupModel, Integer integer) {
                liveTvProgramsAdapter.setEpgModels(catchupModel.getEpgEvents());
                return null;
            }
        });
        dateRecyclerView.setAdapter(dateAdapter);
        epg_recyclerView.setAdapter(liveTvProgramsAdapter);
        io.reactivex.Observable<List<EPGEvent>> booksObservable =
                Observable.fromCallable(this::getEpg);
        bookSubscription = booksObservable.
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe(epgModelList -> {
                    MyApp.selectedChannel.setEvents(epgModelList);
                    List<CatchupModel> catchupModels = getCatchupModels(MyApp.selectedChannel.getEvents());
                    liveTvProgramsAdapter.setEpgModels(catchupModels.get(0).getEpgEvents());
                    dateAdapter.setList(catchupModels);
                    setDescription(MyApp.selectedChannel.getEvents().get(0));
                });
    }

    private List<EPGEvent> getEpg() {
        try {
            String map = MyApp.instance.getIptvclient().getAllEPGOfStream(MyApp.user,MyApp.pass,MyApp.selectedChannel.getStream_id()+"");
            Log.e(getClass().getSimpleName(),map);
            Gson gson=new Gson();
            map=map.replaceAll("[^\\x00-\\x7F]", "");
            if (!map.contains("null_error_response")){
                Log.e("response",map);
                try {
                    JSONObject jsonObject= new JSONObject(map);
                    JSONArray jsonArray=jsonObject.getJSONArray("epg_listings");
                    return new ArrayList<>(gson.fromJson(jsonArray.toString(), new TypeToken<List<EPGEvent>>() {}.getType()));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            } else return new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<CatchupModel> getCatchupModels(List<EPGEvent> events) {
        Collections.sort(events, (o1, o2) -> o1.getStart_timestamp().compareTo(o2.getStart_timestamp()));
        List<CatchupModel> catchupModels = new ArrayList<>();
        String date, nowDate=null;
        List<EPGEvent> epgEvents=new ArrayList<>();
        Date now = new Date();
        for (EPGEvent epgEvent:events){
            Date that_date = new Date();
            that_date.setTime(epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET);
            date =simpleDateFormat.format(that_date);
            if (nowDate==null) {
                nowDate = date;
                epgEvents = new ArrayList<>();
                Log.e("FragmentCatchupDetail","initialize");
            }
            if (!date.equals(nowDate)){
                CatchupModel catchupModel = new CatchupModel();
                catchupModel.setName(nowDate);
                catchupModel.setEpgEvents(epgEvents);
                Log.e("FragmentCatchupDetail",nowDate+" "+epgEvents.size());
                catchupModels.add(catchupModel);
                nowDate=date;
                epgEvents = new ArrayList<>();
            }
            if (now.before(that_date))
                break;
            epgEvents.add(epgEvent);
        }
        return catchupModels;
    }


    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here

        return super.myOnKeyDown(event);
    }

    @SuppressLint("SetTextI18n")
    private void setDescription(EPGEvent epgEvent) {
        Log.e(TAG,"initialize header by changing program");
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("MMM d, HH:mm");
        if (epgEvent!=null){
            Log.e(TAG,epgEvent.getStart_timestamp());
            Date that_date = new Date();
            that_date.setTime(epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET);
            Date end_date = new Date();
            end_date.setTime(epgEvent.getEndTime().getTime()+ Constants.SEVER_OFFSET);
            duration.setText(dateFormat1.format(that_date)+" - "+dateFormat.format(end_date));
            title.setText(new String (Base64.decode(epgEvent.getTitle(), Base64.DEFAULT)));
            content.setText(new String (Base64.decode(epgEvent.getDec(), Base64.DEFAULT)));
        }else {
            duration.setText("-");
            title.setText(requireContext().getString(R.string.no_information));
            content.setText("");
        }
    }
}
