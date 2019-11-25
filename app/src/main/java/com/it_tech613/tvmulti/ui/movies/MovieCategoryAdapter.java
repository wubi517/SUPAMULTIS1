package com.it_tech613.tvmulti.ui.movies;

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
import com.it_tech613.tvmulti.models.MovieModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MovieCategoryAdapter extends RecyclerView.Adapter<MovieCategoryAdapter.HomeListViewHolder> {
    private List<CategoryModel> list=new ArrayList<>();
    private List<CategoryModel> fullList=new ArrayList<>();

    private Context context;
    private Function3<Integer, CategoryModel, List<MovieModel>, Unit> clickListenerFunction;

    MovieCategoryAdapter(Context context, Function3<Integer, CategoryModel, List<MovieModel>, Unit> clickListenerFunction) {
        this.context = context;
        this.clickListenerFunction = clickListenerFunction;
    }

    public abstract void setBlocked(boolean isBlocked);

    void setList(List<CategoryModel> list, boolean firstLoad){
        this.list = list;
        notifyDataSetChanged();
        if (firstLoad){
            fullList.clear();
            fullList.addAll(list);
        }
    }

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
//        if (position == 0) holder.itemView.requestFocus();
        holder.name.setText(list.get(position).getName());
        holder.items_recyclerview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        List<MovieModel> movieModels = new ArrayList<>(list.get(position).getMovieModels());
        int sorting_id;
        if (MyApp.instance.getPreference().get(Constants.getCurrentSorting())!=null)
            sorting_id = (int) MyApp.instance.getPreference().get(Constants.getCurrentSorting());
        else sorting_id = 0;
        switch (sorting_id){
            case 0://last added
                Collections.sort(movieModels, (o1, o2) -> o2.getNum().compareTo(o1.getNum()));
                break;
            case 1://top to bottom
                break;
            case 2://bottom to top
                Collections.reverse(movieModels);
                break;
        }
        holder.items_recyclerview.setAdapter(new MovieAdapter(movieModels, (pos, items) -> {
            clickListenerFunction.invoke(pos,list.get(position), items);
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
            List<MovieModel> movieModels = new ArrayList<>();
            for (MovieModel movieModel: categoryModel.getMovieModels()){
                if (movieModel.getName().toLowerCase().contains(query.toLowerCase()))
                    movieModels.add(movieModel);
            }
            if (movieModels.size()==0) continue;
            CategoryModel categoryObject = new CategoryModel(categoryModel.getId(),categoryModel.getName(),categoryModel.getUrl());
            categoryObject.setMovieModels(movieModels);
            categoryModels.add(categoryObject);
        }
        setList(categoryModels,false);
        setBlocked(false);
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder {
        RecyclerView items_recyclerview;
        TextView name;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            items_recyclerview = itemView.findViewById(R.id.items_recyclerview);
            name = itemView.findViewById(R.id.name);
        }
    }
}