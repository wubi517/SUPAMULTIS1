package com.it_tech613.tvmulti.ui.home;

import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.models.MovieModel;
import com.it_tech613.tvmulti.models.SeriesModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class HomeCategoryListAdapter extends RecyclerView.Adapter<HomeCategoryListAdapter.HomeListViewHolder> {

    private int type;
    private Function4<Integer, EPGChannel, MovieModel, SeriesModel, Unit> clickListenerFunction;
    private List<EPGChannel> epgChannels = new ArrayList<>();
    private List<MovieModel> movieModels = new ArrayList<>();
    private List<SeriesModel> seriesModels = new ArrayList<>();

    HomeCategoryListAdapter(int type, Function4<Integer, EPGChannel, MovieModel, SeriesModel, Unit> function) {
        clickListenerFunction = function;
        this.type = type;
        int sorting_id;
        if (MyApp.instance.getPreference().get(Constants.getCurrentSorting())!=null)
            sorting_id = (int) MyApp.instance.getPreference().get(Constants.getCurrentSorting());
        else sorting_id = 0;
        switch (sorting_id){
            case 0://last added
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getAllFullModel(MyApp.fullModels).getChannels());
                        Log.e(getClass().getSimpleName(),"all full model: "+epgChannels.size());
                        Collections.sort(epgChannels, (o1, o2) -> {
                            try {
                                o2.getAdded().compareTo(o1.getAdded());
                            }catch (Exception ignored){
                            }
                            return 0;
                        });
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        Collections.sort(movieModels, (o1, o2) -> {
                            try {
                                o2.getAdded().compareTo(o1.getAdded());
                            }catch (Exception ignored){
                            }
                            return 0;
                        });
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        Collections.sort(seriesModels, (o1, o2) -> {
                            try {
                                o2.getLast_modified().compareTo(o1.getLast_modified());
                            }catch (Exception ignored){
                            }
                            return 0;
                        });
                        break;
                }
                break;
            case 1://top to bottom
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getAllFullModel(MyApp.fullModels).getChannels());
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        break;
                }
                break;
            case 2://bottom to top
                switch (type){
                    case 0:
                        epgChannels = new ArrayList<>(Constants.getAllFullModel(MyApp.fullModels).getChannels());
                        Collections.reverse(epgChannels);
                        break;
                    case 1:
                        movieModels = new ArrayList<>(MyApp.movieModels);
                        Collections.reverse(movieModels);
                        break;
                    case 2:
                        seriesModels = new ArrayList<>(MyApp.seriesModels);
                        Collections.reverse(seriesModels);
                        break;
                }
                break;
        }
    }

    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.home_category_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder holder, final int position) {
        switch (type){
            case 0:
                final EPGChannel epgChannel = epgChannels.get(position);
                if (epgChannel.getImageURL()!=null && !epgChannel.getImageURL().equals("")){
                    holder.image.setImageURI(Uri.parse(epgChannel.getImageURL()));
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText(epgChannel.getName());
                    holder.title.setVisibility(View.GONE);
                }else {
                    holder.image.setImageResource(R.drawable.pkg_dlg_title_bg);
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(epgChannel.getName());
                    holder.name.setVisibility(View.GONE);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position,epgChannel, null, null);
                    }
                });
                break;
            case 1:
                final MovieModel movieModel = movieModels.get(position);
                if (movieModel.getStream_icon()!=null && !movieModel.getStream_icon().equals("")){
                    holder.image.setImageURI(Uri.parse(movieModel.getStream_icon()));
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText(movieModel.getName());
                    holder.title.setVisibility(View.GONE);
                }else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(movieModel.getName());
                    holder.name.setVisibility(View.GONE);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position, null, movieModel, null);
                    }
                });
                break;
            case 2:
                final SeriesModel seriesModel = seriesModels.get(position);
                if (seriesModel.getStream_icon()!=null && !seriesModel.getStream_icon().equals("")){
                    holder.image.setImageURI(Uri.parse(seriesModel.getStream_icon()));
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText(seriesModel.getName());
                    holder.title.setVisibility(View.GONE);
                }else {
                    holder.title.setVisibility(View.VISIBLE);
                    holder.title.setText(seriesModel.getName());
                    holder.name.setVisibility(View.GONE);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListenerFunction.invoke(position, null, null,seriesModel);
                    }
                });
                break;
        }
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    holder.card.setCardElevation(10f);
                    holder.card.setCardBackgroundColor(Color.parseColor("#FFD600"));
                    holder.itemView.setScaleX(1f);
                    holder.itemView.setScaleY(1f);
                    holder.name.setTextColor(Color.parseColor("#212121"));
                    holder.title.setTextColor(Color.parseColor("#eeeeee"));
                }else{
                    holder.card.setCardElevation(1f);
                    holder.card.setCardBackgroundColor(Color.parseColor("#25ffffff"));
                    holder.itemView.setScaleX(0.85f);
                    holder.itemView.setScaleY(0.85f);
                    holder.name.setTextColor(Color.parseColor("#eeeeee"));
                    holder.title.setTextColor(Color.parseColor("#eeeeee"));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        switch (type){
            case 0:
                return epgChannels.size();
            case 1:
                return movieModels.size();
            case 2:
                return seriesModels.size();
        }
        return 0;
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder{
        SimpleDraweeView image;
        CardView card;
        TextView title, name;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            card = itemView.findViewById(R.id.card);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
        }
    }
}