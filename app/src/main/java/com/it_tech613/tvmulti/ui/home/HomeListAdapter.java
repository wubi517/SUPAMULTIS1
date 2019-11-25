package com.it_tech613.tvmulti.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.models.FirstServer;
import com.it_tech613.tvmulti.models.MovieModel;
import com.it_tech613.tvmulti.models.SeriesModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.functions.Function5;

public class HomeListAdapter extends RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder> {
    private Context context;
    private Function5<Integer, Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction;

    HomeListAdapter(Context context, Function5<Integer,Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction) {
        this.context = context;
        this.clickListenerFunction = clickListenerFunction;
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeListViewHolder holder, final int position) {
        holder.items_recyclerview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        switch (position){
            case 0:
                if (MyApp.firstServer == FirstServer.first) holder.name.setText(context.getResources().getString(R.string.featured_live_tv));
                else holder.name.setVisibility(View.GONE);
                break;
            case 1:
                holder.name.setText(context.getResources().getString(R.string.featured_movies));
                break;
            case 2:
                holder.name.setText(context.getResources().getString(R.string.featured_series));
                break;
        }
        if (MyApp.firstServer == FirstServer.first || position != 0)
        holder.items_recyclerview.setAdapter(new HomeCategoryListAdapter(position, new Function4<Integer, EPGChannel, MovieModel, SeriesModel, Unit>() {
            @Override
            public Unit invoke(Integer pos, EPGChannel epgChannel, MovieModel movieModel, SeriesModel seriesModel) {
                clickListenerFunction.invoke(position,pos,epgChannel,movieModel,seriesModel);
                return null;
            }
        }));
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    class HomeListViewHolder extends  RecyclerView.ViewHolder{
        TextView name;
        RecyclerView items_recyclerview;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            items_recyclerview = itemView.findViewById(R.id.items_recyclerview);
        }
    }
}