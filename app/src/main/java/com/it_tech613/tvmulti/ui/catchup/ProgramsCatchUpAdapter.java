package com.it_tech613.tvmulti.ui.catchup;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.models.EPGEvent;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProgramsCatchUpAdapter extends RecyclerView.Adapter<ProgramsCatchUpAdapter.CategoryViewHolder> {

    private List<EPGEvent> epgModels=new ArrayList<>();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
    private Function2<Integer, EPGEvent, Unit> onClickListener;
    private Function2<Integer, EPGEvent, Unit> onFocusListener;
    ProgramsCatchUpAdapter(List<EPGEvent> epgModels, Function2<Integer, EPGEvent, Unit> onClickListener, Function2<Integer, EPGEvent, Unit> onFocusListener){
        this.epgModels=epgModels;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }
    void setEpgModels(List<EPGEvent> epgModels){
        this.epgModels=epgModels;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.livetv_program_item_catchup,parent,false));
    }

    @Override
    public int getItemCount() {
        return epgModels.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        final EPGEvent epgEvent=epgModels.get(position);
//        holder.name.setText(epgEvent.getTitle());
//        holder.time.setText(dateFormat.format(epgEvent.getStartTime()));
        holder.name.setText(new String (Base64.decode(epgEvent.getTitle(), Base64.DEFAULT)));
        Date that_date = new Date();
        that_date.setTime(epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET);
        holder.time.setText(dateFormat.format(that_date));
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.name.setTextColor(Color.parseColor("#ffaa3f"));
                    holder.time.setTextColor(Color.parseColor("#ffaa3f"));
                    onFocusListener.invoke(position,epgEvent);
                }else {
                    holder.name.setTextColor(Color.parseColor("#ffffff"));
                    holder.time.setTextColor(Color.parseColor("#ffffff"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.invoke(position, epgEvent);
            }
        });
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView name,time;
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
        }
    }
}