package com.it_tech613.tvmulti.ui.tvGuide;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.models.EPGEvent;
import kotlin.Unit;
import kotlin.jvm.functions.Function4;

import java.util.ArrayList;
import java.util.List;

public class EpgAdapter extends RecyclerView.Adapter<EpgAdapter.HomeListViewHolder> {
    private List<EPGChannel> list ;
    private Context context;
    private Function4<Integer,Integer, EPGChannel, EPGEvent, Unit> onClickListener;
    private Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onFocusListener;
    private int channelPos=-1;
    private boolean is_header_focused = true;
    public EpgAdapter(List<EPGChannel> list, Context context, Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onClickListener,
                      Function4<Integer, Integer, EPGChannel, EPGEvent, Unit> onFocusListener) {
        this.list = list;
        this.context = context;
        this.onClickListener = onClickListener;
        this.onFocusListener = onFocusListener;
    }

    public void setChannelPos(int channelPos){
        this.channelPos=channelPos;
        notifyItemChanged(channelPos);
    }
    public void setList(List<EPGChannel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    boolean getIs_Header_focused(){
        return is_header_focused;
    }
    @NonNull
    @Override
    public HomeListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.epg_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final HomeListViewHolder holder, final int position) {
        final EPGChannel epgChannel = list.get(position);
        holder.image.setImageURI(Uri.parse(epgChannel.getImageURL()));
        holder.programs_recyclerview.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        List<EPGEvent> epgEvents = new ArrayList<>();
        int now_i= Constants.findNowEvent(epgChannel.getEvents());
        if (now_i!=-1){
            epgEvents = epgChannel.getEvents().subList(now_i, epgChannel.getEvents().size());
        }
        holder.programs_recyclerview.setAdapter(new EpgProgramsListAdapter(epgEvents, (integer, epgEvent) -> {
            //onClickListener
            onClickListener.invoke(position, integer, epgChannel, epgEvent);
            return null;
        }, (integer, epgEvent) -> {
            //onFocusListener
            onFocusListener.invoke(position, integer, epgChannel, epgEvent);
            is_header_focused = false;
            return null;
        }));
        List<EPGEvent> finalEpgEvents = epgEvents;
        holder.image.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                holder.image.setBackgroundColor(Color.parseColor("#2962FF"));
                if (finalEpgEvents.size()!=0) {
                    onFocusListener.invoke(position, -1, epgChannel, finalEpgEvents.get(0));
                }else {
                    onFocusListener.invoke(position, -1, epgChannel, null);
                }
                is_header_focused = true;
            }else{
                holder.image.setBackgroundColor(Color.parseColor("#ababab"));
            }
        });
        if (finalEpgEvents.size()!=0) {
            holder.image.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, finalEpgEvents.get(0)));
        }else {
            holder.image.setOnClickListener(v -> onClickListener.invoke(position, -1, epgChannel, null));
        }
        if (channelPos==position) {
            holder.image.requestFocus();
            channelPos=-1;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class HomeListViewHolder extends RecyclerView.ViewHolder{
        SimpleDraweeView image;
        RecyclerView programs_recyclerview;
        public HomeListViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            programs_recyclerview= itemView.findViewById(R.id.programs_recyclerview);
        }
    }
}