package com.it_tech613.tvmulti.ui.catchup;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.CategoryModel;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.models.FullModel;
import com.it_tech613.tvmulti.ui.MainActivity;
import com.it_tech613.tvmulti.ui.liveTv.CategoryAdapter;
import com.it_tech613.tvmulti.utils.MyFragment;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.functions.Function3;

import java.util.ArrayList;
import java.util.List;


public class FragmentCatchupCategory extends MyFragment {
    private RecyclerView channel_recycler_view;
    private List<FullModel> catchUpFullModels = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_catchup_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        channel_recycler_view = view.findViewById(R.id.channel_recyclerview);
        channel_recycler_view.setLayoutManager(new GridLayoutManager(requireContext(),requireContext().getResources().getInteger(R.integer.num_span)));
        List<EPGChannel> epgChannels;
        for (FullModel fullModel:MyApp.fullModels_filter){
            epgChannels = new ArrayList<>();
            for (EPGChannel epgChannel: fullModel.getChannels()){
                if (epgChannel.getTv_archive()==1)
                    epgChannels.add(epgChannel);
            }
            catchUpFullModels.add(new FullModel(fullModel.getCategory_id(),epgChannels,fullModel.getCategory_name(),fullModel.getCatchable_count()));
        }
        channel_recycler_view.setAdapter(new ChannelAdapter(catchUpFullModels.get(0).getChannels(), new Function2<Integer, EPGChannel, Unit>() {
            @Override
            public Unit invoke(Integer integer, EPGChannel epgChannel) {
                goToDetailPage(integer, epgChannel);
                return null;
            }
        }));

        RecyclerView category_recycler_view = view.findViewById(R.id.category_recyclerview);
        category_recycler_view.setLayoutManager(new LinearLayoutManager(requireContext()));
        category_recycler_view.setAdapter(new CategoryAdapter(MyApp.live_categories_filter,  new Function3<CategoryModel,Integer,Boolean, Unit>() {
            @Override
            public Unit invoke(CategoryModel categoryModel, Integer position,Boolean is_clicked) {
                if (!is_clicked) return null;
                channel_recycler_view.setAdapter(new ChannelAdapter(catchUpFullModels.get(position).getChannels(), new Function2<Integer, EPGChannel, Unit>() {
                    @Override
                    public Unit invoke(Integer integer, EPGChannel epgChannel) {
                        goToDetailPage(integer, epgChannel);
                        return null;
                    }
                }));
                return null;
            }
        }));
    }

    private void goToDetailPage(Integer integer, EPGChannel epgChannel) {
        if (epgChannel.getTv_archive()==0) return;
        MyApp.selectedChannel = epgChannel;
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(((MainActivity)requireActivity()).fragmentList.size()-1))//FragmentCatchupDetail
                .addToBackStack(null).commit();
    }


    @Override
    public boolean myOnKeyDown(KeyEvent event){
        //do whatever you want here
        if (event.getAction()==KeyEvent.ACTION_UP){
            switch (event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container,((MainActivity)requireActivity()).fragmentList.get(0))//FragmentCatchupDetail
                            .addToBackStack(null).commit();
                    return false;
            }
        }
        return super.myOnKeyDown(event);
    }

}
