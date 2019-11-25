package com.it_tech613.tvmulti.ui.movies;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.it_tech613.tvmulti.R;
import com.it_tech613.tvmulti.models.MovieModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

import java.util.List;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.CategoryViewHolder> {
    private List<MovieModel> list;
    private int selected = 0;
    private Function2<MovieModel,Integer, Unit> MovieModelsFunction0;

    public MovieListAdapter(List<MovieModel> list, Function2<MovieModel,Integer, Unit> MovieModelsFunction0) {
        this.list = list;
        this.MovieModelsFunction0=MovieModelsFunction0;
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
                    MovieModelsFunction0.invoke(list.get(position),position);
                    int previouslySelected = selected;
                    selected = position;
                    notifyItemChanged(previouslySelected);
                    notifyItemChanged(selected);
                }
            }
        });

        if(position==0) holder.itemView.requestFocus();
        if(selected==position) holder.itemView.setBackgroundColor(Color.parseColor("#602962FF"));
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