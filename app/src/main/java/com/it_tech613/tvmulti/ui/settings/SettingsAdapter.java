package com.it_tech613.tvmulti.ui.settings;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.models.Settings;

import java.util.List;

class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.CategoryViewHolder> {
    private List<Settings> list ;
    private OnClickListener onClickListener;
    public SettingsAdapter(List<Settings> list ,OnClickListener onClickListener){
        this.list = list;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, final int position) {
        holder.name.setText(list.get(position).getName());
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    holder.card.setCardElevation(8f);
                    holder.itemView.setBackgroundColor(Color.parseColor("#2962FF"));

                }else{
                    holder.card.setCardElevation(0f);
                    holder.itemView.setBackgroundColor(Color.parseColor("#000096a6"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onClick(position,list.get(position));
            }
        });
    }

    public interface OnClickListener{
        void onClick(int position, Settings settings);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        CardView card;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            card = itemView.findViewById(R.id.card);
        }
    }
}