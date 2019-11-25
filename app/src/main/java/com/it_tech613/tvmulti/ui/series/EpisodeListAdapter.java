package com.it_tech613.tvmulti.ui.series;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.models.EpisodeModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.ArrayList;
import java.util.List;

public class EpisodeListAdapter extends RecyclerView.Adapter<EpisodeListAdapter.VideoViewHolder> {

    private List<EpisodeModel> episodeModels =new ArrayList<>();
    private Function2<Integer,EpisodeModel, Unit> onClickListener;
    private Function2<Integer,EpisodeModel, Unit> onFocusListener;
    private int selected = 0;
    EpisodeListAdapter(List<EpisodeModel> videoItems, Function2<Integer,EpisodeModel, Unit> onClickListener, Function2<Integer,EpisodeModel, Unit> onFocusListener) {
        episodeModels =videoItems;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }

    void setEpisodeModels(List<EpisodeModel> episodeModels){
        this.episodeModels = episodeModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder( LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, final int position) {
        final EpisodeModel episodeModel = episodeModels.get(position);
        if (episodeModel.getEpisodeInfoModel().getMovie_image()!=null && !episodeModel.getEpisodeInfoModel().getMovie_image().equals(""))
            holder.image.setImageURI(Uri.parse(episodeModel.getEpisodeInfoModel().getMovie_image()));
        else holder.image.setImageResource(R.drawable.icon);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));
                    onFocusListener.invoke(position,episodeModel);
                    selected = position;
                }else{
                    holder.itemView.setBackgroundColor(Color.parseColor("#000096a6"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.invoke(position,episodeModel);
            }
        });
        holder.title.setText(episodeModel.getTitle());
        holder.description.setText(episodeModel.getEpisodeInfoModel().getPlot());
        holder.duration.setText(episodeModel.getEpisodeInfoModel().getDuration());
        if(position==selected) holder.itemView.requestFocus();
    }

    @Override
    public int getItemCount() {
        return episodeModels.size();
    }

    class VideoViewHolder extends ViewHolder{
        SimpleDraweeView image;
        TextView title, duration, description;
        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.textView);
            description = itemView.findViewById(R.id.textView2);
            duration = itemView.findViewById(R.id.textView3);
        }
    }

}