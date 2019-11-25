package com.it_tech613.tvmulti.ui.series;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.CategoryModel;
import com.it_tech613.tvmulti.models.SeriesModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SeriesCategoryAdapter extends RecyclerView.Adapter<SeriesCategoryAdapter.HomeListViewHolder> {
    private List<CategoryModel> list=new ArrayList<>();
    private List<CategoryModel> fullList=new ArrayList<>();
    private Context context;
    private Function4<Integer, CategoryModel, List<SeriesModel>,Boolean, Unit> clickListenerFunction;
    SeriesCategoryAdapter( Context context, Function4<Integer, CategoryModel, List<SeriesModel>,Boolean, Unit> clickListenerFunction) {
        this.context = context;
        this.clickListenerFunction = clickListenerFunction;
    }

    void setList(List<CategoryModel> list,boolean firstLoad){
        this.list = list;
        notifyDataSetChanged();
        if (firstLoad){
            fullList.clear();
            fullList.addAll(list);
        }
    }

    public abstract void setBlocked(boolean isBlocked);

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeListViewHolder holder, final int position) {
        holder.name.setText(list.get(position).getName());
        holder.itemsRecyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        List<SeriesModel> seriesModels = new ArrayList<>(list.get(position).getSeriesModels());
        int sorting_id;
        if (MyApp.instance.getPreference().get(Constants.getCurrentSorting())!=null)
            sorting_id = (int) MyApp.instance.getPreference().get(Constants.getCurrentSorting());
        else sorting_id = 0;
        switch (sorting_id){
            case 0://last added
                Collections.sort(seriesModels, (o1, o2) -> o2.getNum().compareTo(o1.getNum()));
                break;
            case 1://top to bottom
                break;
            case 2://bottom to top
                Collections.reverse(seriesModels);
                break;
        }
        holder.itemsRecyclerView.setAdapter(new SeriesAdapter(seriesModels, (pos, items, isClicked) -> {
            clickListenerFunction.invoke(position,list.get(position), items, isClicked);
            return null;
        }));
    }

    void search(String query) {
        setBlocked(true);
        if (query.equals("")) {
            setList(fullList,false);
            setBlocked(false);
            return;
        }
        List<CategoryModel> categoryModels = new ArrayList<>();
        for (CategoryModel categoryModel:fullList){
            List<SeriesModel> seriesModels = new ArrayList<>();
            for (SeriesModel seriesModel: categoryModel.getSeriesModels()){
                if (seriesModel.getName().toLowerCase().contains(query.toLowerCase()))
                    seriesModels.add(seriesModel);
            }
            if (seriesModels.size()==0) continue;
            CategoryModel categoryObject = new CategoryModel(categoryModel.getId(),categoryModel.getName(),categoryModel.getUrl());
            categoryObject.setSeriesModels(seriesModels);
            categoryModels.add(categoryObject);
        }
        setList(categoryModels,false);
        setBlocked(false);
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView itemsRecyclerView;
        TextView name;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            itemsRecyclerView = itemView.findViewById(R.id.items_recyclerview);
            name = itemView.findViewById(R.id.name);
        }
    }
}