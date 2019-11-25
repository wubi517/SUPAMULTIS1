package com.it_tech613.tvmulti.ui.multi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.apps.Constants;
import com.it_tech613.tvmulti.apps.MyApp;
import com.it_tech613.tvmulti.models.EPGChannel;
import com.it_tech613.tvmulti.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by RST on 7/19/2017.
 */

public class MainListAdapter extends BaseAdapter {

    private Context context;
    private List<EPGChannel> epgChannels;
    private LayoutInflater inflater;
    private int selected_pos;

    public MainListAdapter(Context context, List<EPGChannel> epgChannels) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.epgChannels = epgChannels;
//        sortModels(epgChannels);
    }

    private void sortModels(List<EPGChannel> channels) {
        epgChannels = new ArrayList<>();
        epgChannels.addAll(channels);
        int sorting_id;
        if (MyApp.instance.getPreference().get(Constants.getCurrentSorting())!=null)
            sorting_id = (int) MyApp.instance.getPreference().get(Constants.getCurrentSorting());
        else sorting_id = 0;
        switch (sorting_id){
            case 0://last added
                Collections.sort(epgChannels, (o1, o2) -> o2.getAdded().compareTo(o1.getAdded()));
                break;
            case 1://top to bottom
                break;
            case 2://bottom to top
                Collections.reverse(epgChannels);
                break;
        }
    }

    void setEpgChannels(List<EPGChannel> epgChannels){
        this.epgChannels = epgChannels;
//        sortModels(epgChannels);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return epgChannels.size();
    }

    @Override
    public Object getItem(int position) {
        return epgChannels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_main_list, parent, false);
        }
        LinearLayout main_lay = (LinearLayout) convertView.findViewById(R.id.main_lay);
        TextView title = (TextView) convertView.findViewById(R.id.main_list_txt);
        TextView num = (TextView) convertView.findViewById(R.id.main_list_num);
        ImageView image_play = (ImageView) convertView.findViewById(R.id.image_play);
        ImageView image_star = (ImageView) convertView.findViewById(R.id.image_star);
        ImageView image_clock = (ImageView) convertView.findViewById(R.id.image_clock);
        ImageView channel_logo = (ImageView) convertView.findViewById(R.id.channel_logo);
        if(epgChannels.get(position).getImageURL()!=null && !epgChannels.get(position).getImageURL().isEmpty()){
            Glide.with(context).load(epgChannels.get(position).getImageURL())
                    .apply(new RequestOptions().placeholder(R.drawable.icon).error(R.drawable.icon).signature(new ObjectKey("myKey1")))
                    .into(channel_logo);
            channel_logo.setVisibility(View.VISIBLE);
        }else {
            channel_logo.setVisibility(View.GONE);
        }
        View view = (View) convertView.findViewById(R.id.view);
        LinearLayout ly_info = (LinearLayout) convertView.findViewById(R.id.ly_info);
        title.setText(epgChannels.get(position).getName());
        num.setText(epgChannels.get(position).getNumber());
        if(epgChannels.get(position).is_favorite()){
            image_star.setVisibility(View.VISIBLE);
        }else {
            image_star.setVisibility(View.GONE);
        }
        if(epgChannels.get(position).getTv_archive()==1){
            image_clock.setVisibility(View.VISIBLE);
        }else {
            image_clock.setVisibility(View.GONE);
        }
        if (selected_pos == position && MyApp.is_first) {
            image_play.setVisibility(View.VISIBLE);
        } else {
            image_play.setVisibility(View.GONE);
            num.setTextColor(Color.parseColor("#18477f"));
            num.setBackgroundResource(R.drawable.yellowback);
            main_lay.setBackgroundResource(R.drawable.list_item_channel_draw);
        }

        view.setVisibility(View.GONE);
        ly_info.setVisibility(View.GONE);

        if(context.getResources().getBoolean(R.bool.is_phone)){
            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        }else {
            main_lay.setPadding(Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5), Utils.dp2px(context, 5));
        }

        return convertView;
    }

    public void selectItem(int pos) {
        selected_pos = pos;
        notifyDataSetChanged();
    }
}
