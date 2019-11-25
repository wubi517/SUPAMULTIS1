package com.it_tech613.tvmulti.ui.tvGuide;

import android.graphics.Color;
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

public class EpgProgramsListAdapter extends RecyclerView.Adapter<EpgProgramsListAdapter.HomeListViewHolder> {
    private List<EPGEvent> list = new ArrayList<>();
    private Function2<Integer,EPGEvent, Unit> onClickListener;
    private Function2<Integer,EPGEvent, Unit> onFocusListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");
    EpgProgramsListAdapter(List<EPGEvent> list,Function2<Integer,EPGEvent, Unit> onClickListener,Function2<Integer,EPGEvent, Unit> onFocusListener){
        this.list=list;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }
    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.epg_program_item,parent,false));
    }

    @Override
    public int getItemCount() {
        if (getItemViewType()==1) return list.size();
        else return 1;
    }

    private int getItemViewType(){
        if (list.size()==0) return 0;
        else return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder holder, final int position) {
        if (getItemViewType()==1){
            final EPGEvent epgEvent = list.get(position);
            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus){
                    holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));
                    onFocusListener.invoke(position,epgEvent);
                }else{
                    holder.itemView.setBackgroundColor(Color.parseColor("#10ffffff"));
                }
            });
            holder.name.setText(epgEvent.getTitle());
            Date that_date = new Date();
            that_date.setTime(epgEvent.getStartTime().getTime()+ Constants.SEVER_OFFSET);
            holder.startTime.setText(dateFormat.format(that_date));
            holder.itemView.setOnClickListener(v -> onClickListener.invoke(position,epgEvent));
        }else {
            holder.name.setText(holder.itemView.getContext().getString(R.string.no_information));
            holder.startTime.setText("");
            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus){
                    holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));
                    onFocusListener.invoke(-1,null);
                }else{
                    holder.itemView.setBackgroundColor(Color.parseColor("#10ffffff"));
                }
            });
            holder.itemView.setOnClickListener(v -> onClickListener.invoke(-1,null));
        }

    }

    class HomeListViewHolder extends RecyclerView.ViewHolder {
        TextView name, startTime;
        HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textView5);
            startTime = itemView.findViewById(R.id.textView6);
        }
    }
}