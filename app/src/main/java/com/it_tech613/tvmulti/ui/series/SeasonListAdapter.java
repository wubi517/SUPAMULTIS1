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
import com.it_tech613.tvmulti.models.SeasonModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.ArrayList;
import java.util.List;

public class SeasonListAdapter extends RecyclerView.Adapter<SeasonListAdapter.VideoViewHolder> {

    private List<SeasonModel> seasonModels =new ArrayList<>();
    private Function2<Integer,SeasonModel, Unit> onClickListener;
    private Function2<Integer,SeasonModel, Unit> onFocusListener;
    private int selected_i=0;
    SeasonListAdapter(List<SeasonModel> videoItems, Function2<Integer,SeasonModel, Unit> onClickListener, Function2<Integer,SeasonModel, Unit> onFocusListener) {
        seasonModels = videoItems;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }

    void setSeasonModels(List<SeasonModel> seasonModels){
        this.seasonModels = seasonModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder( LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final VideoViewHolder holder, final int position) {
        final SeasonModel seasonModel = seasonModels.get(position);
        if (seasonModel.getIcon()!=null && seasonModel.getIcon().contains("http"))
            holder.image.setImageURI(Uri.parse(seasonModel.getIcon()));
        else holder.image.setImageResource(R.drawable.icon);
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));
                    onFocusListener.invoke(position,seasonModel);
                    selected_i = position;
                }else{
                    holder.itemView.setBackgroundColor(Color.parseColor("#000096a6"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.invoke(position,seasonModel);
            }
        });
        holder.title.setText(seasonModel.getName());
        holder.description.setText(seasonModel.getOverview());
        holder.duration.setText(seasonModel.getAir_date());
        if(position==selected_i) holder.itemView.requestFocus();
    }

    @Override
    public int getItemCount() {
        return seasonModels.size();
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