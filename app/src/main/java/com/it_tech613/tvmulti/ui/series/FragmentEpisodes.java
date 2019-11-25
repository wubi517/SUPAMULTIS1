package com.it_tech613.tvmulti.ui.series;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.EpisodeModel;
import com.it_tech613.tvmulti.models.SeriesModel;
import com.it_tech613.tvmulti.ui.VideoExoPlayActivity;
import com.it_tech613.tvmulti.ui.VideoIjkPlayActivity;
import com.it_tech613.tvmulti.ui.VideoPlayActivity;
import com.it_tech613.tvmulti.utils.MyFragment;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FragmentEpisodes extends MyFragment {
    private SimpleDraweeView image;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_episodes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        image = view.findViewById(R.id.image);
        final TextView desc = view.findViewById(R.id.desc);
//        RecyclerView categoryRecyclerView = view.findViewById(R.id.movies_recyclerview);
//        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        categoryRecyclerView.setAdapter(new CategoryAdapter(MyApp.series_categories_filter, new Function2<CategoryModel,Integer, Unit>() {
//            @Override
//            public Unit invoke(CategoryModel categoryModel, Integer position) {
//                startGetSeries(categoryModel.getId());
//                return null;
//            }
//        }));
        RecyclerView seriesRecyclerView = view.findViewById(R.id.video_list_recyclerview);
        seriesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        //onclicklistener
        //onFocusListener
        EpisodeListAdapter episodeListAdapter = new EpisodeListAdapter(MyApp.selectedSeasonModel.getEpisodeModels(), new Function2<Integer, EpisodeModel, Unit>() {
            @Override
            public Unit invoke(Integer integer, EpisodeModel episodeModel) {
                //add recent series
                checkAddedRecent(MyApp.selectedSeriesModel);
                Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels().add(0,MyApp.selectedSeriesModel);
                //get recent series names list
                List<String> recent_series_names = new ArrayList<>();
                for (SeriesModel seriesModel:Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels()){
                    recent_series_names.add(seriesModel.getName());
                }
                //set
                MyApp.instance.getPreference().put(Constants.getRecentSeries(), recent_series_names);
                Log.e(getClass().getSimpleName(),"added");

                //onclicklistener
                String episode_url = MyApp.instance.getIptvclient().buildSeriesStreamURL(MyApp.user,MyApp.pass,episodeModel.getStream_id(),episodeModel.getContainer_extension());
                Log.e(getClass().getSimpleName(),episode_url);
                int current_player = (int) MyApp.instance.getPreference().get(Constants.getCurrentPlayer());
                Intent intent;
                switch (current_player){
                    case 0:
                        intent = new Intent(requireContext(), VideoPlayActivity.class);
                         break;
                    case 1:
                        intent = new Intent(requireContext(), VideoIjkPlayActivity.class);
                        break;
                    case 2:
                        intent = new Intent(requireContext(), VideoExoPlayActivity.class);
                        break;
                    default:
                        intent = new Intent(requireContext(), VideoPlayActivity.class);
                        break;
                }
                intent.putExtra("title",episodeModel.getTitle());
                intent.putExtra("img",episodeModel.getEpisodeInfoModel().getMovie_image());
                intent.putExtra("url",episode_url);
                startActivity(intent);
                return null;
            }
        }, (integer, episodeInfoModel) -> {
            //onFocusListener
            if (episodeInfoModel.getEpisodeInfoModel().getMovie_image()!=null && !episodeInfoModel.getEpisodeInfoModel().getMovie_image().equals("")) image.setImageURI(Uri.parse(episodeInfoModel.getEpisodeInfoModel().getMovie_image()));
            else image.setImageResource(R.drawable.icon);
            desc.setText(episodeInfoModel.getTitle());
            seriesRecyclerView.scrollToPosition(integer);
            return null;
        });
        seriesRecyclerView.setAdapter(episodeListAdapter);
        image.setImageURI(Uri.parse(MyApp.selectedSeriesModel.getStream_icon()));
    }
    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here

        return super.myOnKeyDown(event);
    }

    private void checkAddedRecent(SeriesModel showModel) {
        Iterator<SeriesModel> iter = Constants.getRecentCatetory(MyApp.series_categories).getSeriesModels().iterator();
        while(iter.hasNext()){
            SeriesModel seriesModel = iter.next();
            if (seriesModel.getName().equals(showModel.getName()))
                iter.remove();
        }
    }
}
