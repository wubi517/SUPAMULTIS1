package com.it_tech613.tvmulti.ui.liveTv;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.models.CategoryModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function3;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryModel> list;
    private int selected = 0;
    private Function3<CategoryModel,Integer,Boolean, Unit> categoryModelsFunction0;

    public CategoryAdapter(List<CategoryModel> list, Function3<CategoryModel,Integer,Boolean, Unit> categoryModelsFunction0) {
        this.list = list;
        this.categoryModelsFunction0=categoryModelsFunction0;
    }

    public void setSelected(int selected){
        this.selected = selected;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item,parent,false));
    }

    @Override
    public int getItemCount() {
        return list.size();
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
                    categoryModelsFunction0.invoke(list.get(position),position,false);
                }else{
                    holder.card.setCardElevation(0f);
                    holder.itemView.setBackgroundColor(Color.parseColor("#000096a6"));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selected!=position){
                    //clickeListenerFunction(list[position])
                    categoryModelsFunction0.invoke(list.get(position),position,true);
                    int previouslySelected = selected;
                    selected = position;
                    notifyItemChanged(previouslySelected);
                    notifyItemChanged(selected);
                }

            }
        });

        if(position==selected) {
            holder.itemView.requestFocus();
            holder.itemView.setBackgroundColor(Color.parseColor("#602962FF"));
        }
        else holder.itemView.setBackgroundColor(Color.parseColor("#002962FF"));

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