package com.it_tech613.tvmulti.ui.series;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.*;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.utils.MyFragment;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FragmentSeasons extends MyFragment{

    private SeasonListAdapter seasonListAdapter;
    private Button addFav;
    private ImageView fav_icon;
    private Disposable bookSubscription;
    @NotNull
    public FragmentSeriesHolder parent;
    private SeriesModel seriesModel;
    private boolean is_list = false;

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
        if (MyApp.selectedSeriesModelList!=null && MyApp.selectedSeriesModelList.size()==1) {
            is_list=false;
            return inflater.inflate(R.layout.fragment_seasons, container, false);
        }
        else {
            is_list=true;
            return inflater.inflate(R.layout.fragment_seasons_with_category, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final TextView desc = view.findViewById(R.id.desc);
        seriesModel=MyApp.selectedSeriesModelList.get(0);
        if (is_list){
            RecyclerView categoryRecyclerView = view.findViewById(R.id.recyclerView);
            categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            categoryRecyclerView.setAdapter(new SeriesListAdapter(MyApp.selectedSeriesModelList, (model, position) -> {
                seriesModel=model;
                if (seriesModel.getSeasonModels()==null || seriesModel.getSeasonModels().size()==0) {
                    Observable<List<SeasonModel>> booksObservable =
                            Observable.fromCallable(()->startGetSeries());
                    bookSubscription = booksObservable.
                            subscribeOn(Schedulers.io()).
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribe(seasonModels -> {
                                seriesModel.setSeasonModels(seasonModels);
                                seasonListAdapter.setSeasonModels(seasonModels);
                            });
                }
                else seasonListAdapter.setSeasonModels(seriesModel.getSeasonModels());
                return null;
            }));
        }
        final List<String> slideModels = getSlides(seriesModel.getBackdrop_path());
        SliderLayout mDemoSlider = view.findViewById(R.id.slider_viewpager);
        for(String url : slideModels){
            DefaultSliderView textSliderView = new DefaultSliderView (requireContext());
            // initialize a SliderLayout
            textSliderView
                    //                    .description(name)
                    .image(url)
                    .setScaleType(BaseSliderView.ScaleType.FitCenterCrop);

            //add your extra information
            //            textSliderView.bundle(new Bundle());
            //            textSliderView.getBundle()
            //                    .putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        //        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(slideModels.size()*Constants.GetSlideTime(requireContext()));

        addFav = (Button)view.findViewById(R.id.button4);
        fav_icon = (ImageView)view.findViewById(R.id.fav_icon);
        addFav.setOnClickListener(v -> addToFav());
        setAddFavText();
        RecyclerView seriesRecyclerView = view.findViewById(R.id.video_list_recyclerview);
        seriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        seasonListAdapter =new SeasonListAdapter(new ArrayList<>(), (integer, seasonModel) -> {
            //onclicklistener
            if (seasonModel.getEpisodeModels()==null || seasonModel.getEpisodeModels().isEmpty()) return null;
            MyApp.selectedSeriesModel = seriesModel;
            MyApp.selectedSeasonModel = seasonModel;
            parent.replaceFragment(((MainActivity)requireActivity()).fragmentList.get(((MainActivity)requireActivity()).fragmentList.size()-2));
            return null;
        }, (integer, seasonModel) -> {
            //onFocusListener
            desc.setText(seasonModel.getName());
            seriesRecyclerView.scrollToPosition(integer);
            return null;
        });
        seriesRecyclerView.setAdapter(seasonListAdapter);
        if (seriesModel.getSeasonModels()==null || seriesModel.getSeasonModels().size()==0) {
            Observable<List<SeasonModel>> booksObservable =
                    Observable.fromCallable(this::startGetSeries);
            bookSubscription = booksObservable.
                    subscribeOn(Schedulers.io()).
                    observeOn(AndroidSchedulers.mainThread()).
                    subscribe(seasonModels -> {
                        seriesModel.setSeasonModels(seasonModels);
                        seasonListAdapter.setSeasonModels(seasonModels);
                    });
        }
        else seasonListAdapter.setSeasonModels(seriesModel.getSeasonModels());
    }

    private void setAddFavText(){
        if (seriesModel.isIs_favorite()) {
            addFav.setText(getResources().getString(R.string.remove_favorites));
            fav_icon.setImageResource(R.drawable.heart_filled);
        }
        else {
            addFav.setText(getResources().getString(R.string.add_to_favorite));
            fav_icon.setImageResource(R.drawable.heart_unfilled);
        }
    }
    private void addToFav() {
        Log.e("OnAddFavClick","received");
        if (seriesModel.isIs_favorite()) {
            seriesModel.setIs_favorite(false);
            boolean is_exist = false;
            int pp = 0;
            for (int i = 0; i < MyApp.favSeriesModels.size(); i++) {
                if (MyApp.favSeriesModels.get(i).getName().equals(seriesModel.getName())) {
                    is_exist = true;
                    pp = i;
                }
            }
            if (is_exist)
                MyApp.favSeriesModels.remove(pp);
            //get favorite channel names list
            List<String> fav_series_names=new ArrayList<>();
            for (SeriesModel seriesModel:MyApp.favSeriesModels){
                fav_series_names.add(seriesModel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_SERIES(), fav_series_names);
            Log.e("SERIES_FAV","removed");
        } else {
            seriesModel.setIs_favorite(true);
            //get favorite channel names list
            MyApp.favSeriesModels.add(seriesModel);
            List<String> fav_series_names=new ArrayList<>();
            for (SeriesModel seriesModel:MyApp.favSeriesModels){
                fav_series_names.add(seriesModel.getName());
            }
            //set
            MyApp.instance.getPreference().put(Constants.getFAV_SERIES(), fav_series_names);
            Log.e("SERIES_FAV","added");
        }
        Constants.getFavoriteCatetory(MyApp.series_categories).setSeriesModels(MyApp.favSeriesModels);
        setAddFavText();
    }

    private List<SeasonModel> startGetSeries(){
        try {
            String requestBody = MyApp.instance.getIptvclient().getSeriesInfo(MyApp.user,MyApp.pass,seriesModel.getSeries_id());
            Log.e(getClass().getSimpleName(),seriesModel.getSeries_id() + " "+ requestBody);
            JSONObject map = new JSONObject(requestBody);
            Gson gson=new Gson();
            try {
                JSONArray seasons=map.getJSONArray("seasons");
                List<SeasonModel> seasonModelList = new ArrayList<>();
                List<SeasonModel> seasonModels = new ArrayList<>(gson.fromJson(seasons.toString(), new TypeToken<List<SeasonModel>>() {}.getType()));
//                JSONObject info= map.getJSONObject("info");
//                SeriesModel seriesModel = gson.fromJson(info.toString(),SeriesModel.class);
//                seriesModel.setBackdrop_path(seriesModel.getBackdrop_path());
                try {
                    JSONObject episodes=map.getJSONObject("episodes");
                    Log.e("FragmentSeasons",episodes.toString());
                    Iterator<?> keys = episodes.keys();
                    while (keys.hasNext()){
                        String key = (String) keys.next();
                        SeasonModel seasonModel = getSeasonByKey(seasonModels,key);
                        try {
                            JSONArray i_episodes = episodes.getJSONArray(key);
                            List<EpisodeModel> episodeModels=new ArrayList<>();
                            for (int i=0;i<i_episodes.length();i++){
                                try {
                                    JSONObject object_episode = i_episodes.getJSONObject(i);
                                    EpisodeModel episodeModel = gson.fromJson(object_episode.toString(),EpisodeModel.class);
                                    try {
                                        JSONObject info_object= object_episode.getJSONObject("info");
                                        EpisodeInfoModel episodeInfoModel = gson.fromJson(info_object.toString(),EpisodeInfoModel.class);
                                        episodeModel.setEpisodeInfoModel(episodeInfoModel);
                                        episodeModels.add(episodeModel);
                                    }catch (JSONException ignored){
                                        Log.e("FragmentSeasons","There is an error in getting info model " + seasonModel.getSeason_number());
                                    }
                                }catch (JSONException ignored){
                                    Log.e("FragmentSeasons","There is an error in getting episode model " + seasonModel.getSeason_number());
                                }
                            }
                            seasonModel.setEpisodeModels(episodeModels);
                            seasonModel.setTotal(episodeModels.size());
                        }catch (JSONException ignored){
                            Log.e("FragmentSeasons","There is no episodes in " + seasonModel.getSeason_number());
                        }
                        seasonModelList.add(seasonModel);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        JSONArray episodes=map.getJSONArray("episodes");
                        for (int k=0;k<episodes.length();k++){
                            SeasonModel seasonModel = getSeasonByKey(seasonModels,k+"");
                            try {
                                JSONArray i_episodes = episodes.getJSONArray(k);
                                List<EpisodeModel> episodeModels=new ArrayList<>();
                                for (int i=0;i<i_episodes.length();i++){
                                    try {
                                        JSONObject object_episode = i_episodes.getJSONObject(i);
                                        EpisodeModel episodeModel = gson.fromJson(object_episode.toString(),EpisodeModel.class);
                                        try {
                                            JSONObject info_object= object_episode.getJSONObject("info");
                                            EpisodeInfoModel episodeInfoModel = gson.fromJson(info_object.toString(),EpisodeInfoModel.class);
                                            episodeModel.setEpisodeInfoModel(episodeInfoModel);
                                            episodeModels.add(episodeModel);
                                        }catch (JSONException ignored){
                                            Log.e("FragmentSeasons","There is an error in getting info model " + seasonModel.getSeason_number());
                                        }
                                    }catch (JSONException ignored){
                                        Log.e("FragmentSeasons","There is an error in getting episode model " + seasonModel.getSeason_number());
                                    }
                                }
                                seasonModel.setEpisodeModels(episodeModels);
                                seasonModel.setTotal(episodeModels.size());
                            }catch (JSONException e1) {
                                e1.printStackTrace();
                                Log.e("FragmentSeasons", "There is no episodes "+k);
                            }
                            seasonModelList.add(seasonModel);
                        }
                    }catch (JSONException ignored){
                        Log.e("FragmentSeasons", "There is no episodes at all");
                    }
                }
                Log.e(getClass().getSimpleName(),seasonModelList.size()+"");
                return seasonModelList;
            } catch (JSONException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private SeasonModel getSeasonByKey(List<SeasonModel> seasonModels, String key) {
        for (SeasonModel seasonModel:seasonModels){
            if (key.equals(String.valueOf(seasonModel.getSeason_number())))
                return seasonModel;
        }
        return new SeasonModel(key);
    }

    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        View view = requireActivity().getCurrentFocus();
        switch (event.getKeyCode()){
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (view == null || (view.getId() != R.id.slider_viewpager))
                    addFav.requestFocus();
                break;
        }
        return super.myOnKeyDown(event);
    }

    private List<String> getSlides(List<String> strings) {
        return new ArrayList<>(strings);
    }
}
