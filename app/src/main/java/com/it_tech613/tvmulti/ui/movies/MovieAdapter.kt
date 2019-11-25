package com.it_tech613.tvmulti.ui.movies

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.it_tech613.tvmulti.R
import com.it_tech613.tvmulti.models.MovieModel
import kotlinx.android.synthetic.main.home_category_list_item.view.*
import kotlinx.android.synthetic.main.all_category_list_item.view.*

@Suppress("DEPRECATION")
class MovieAdapter(val list: MutableList<out MovieModel>, val clickListenerFunction: (Int, MutableList<MovieModel>) -> Unit) : RecyclerView.Adapter<MovieAdapter.HomeListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        return if (viewType==1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.home_category_list_item,parent,false)
            HomeListViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.all_category_list_item,parent,false)
            HomeListViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(position==0){
            0
        }else{
            1
        }
    }

    override fun getItemCount() = list.size+1

    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        if(holder.itemViewType==1){
            val movieModel = list[position-1]
            if (movieModel.stream_icon!=null && movieModel.stream_icon != ""){
                holder.itemView.image.setImageURI(Uri.parse(movieModel.stream_icon))
                holder.itemView.title.visibility = View.GONE
                holder.itemView.name.visibility = View.VISIBLE
                holder.itemView.name.text = movieModel.name
//                Log.e("MovieAdapter",movieModel.name +" "+movieModel.stream_icon)
            }else{
                holder.itemView.image.setActualImageResource(R.drawable.pkg_dlg_title_bg)
                holder.itemView.name.visibility = View.INVISIBLE
                holder.itemView.title.visibility = View.VISIBLE
                holder.itemView.title.text = movieModel.name
            }
            holder.itemView.setOnClickListener {
                val models = mutableListOf<MovieModel>()
                models+= movieModel
                clickListenerFunction(position,models)
            }
            holder.itemView.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    holder.itemView.card.cardElevation = 10f
                    holder.itemView.card.setCardBackgroundColor(Color.parseColor("#FFD600"))
                    holder.itemView.scaleX = 1f
                    holder.itemView.scaleY = 1f
                    holder.itemView.name.setTextColor(Color.parseColor("#212121"))
                    holder.itemView.title.setTextColor(Color.parseColor("#eeeeee"))
                }else{
                    holder.itemView.card.cardElevation = 1f
                    holder.itemView.card.setCardBackgroundColor(Color.parseColor("#25ffffff"))
                    holder.itemView.scaleX = 0.85f
                    holder.itemView.scaleY = 0.85f
                    holder.itemView.name.setTextColor(Color.parseColor("#eeeeee"))
                    holder.itemView.title.setTextColor(Color.parseColor("#eeeeee"))
                }
            }
        }else{
            if (position == 0) holder.itemView.requestFocus()
            holder.itemView.setOnClickListener {
                val models = mutableListOf<MovieModel>()
                models+=list
                clickListenerFunction(position,models)
            }
            holder.itemView.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus){
                    holder.itemView.all_card.cardElevation = 10f
                    holder.itemView.all_card.setCardBackgroundColor(Color.parseColor("#FFD600"))
                    holder.itemView.scaleX = 1f
                    holder.itemView.scaleY = 1f
                    holder.itemView.text.setTextColor(Color.parseColor("#212121"))

                }else{
                    holder.itemView.all_card.cardElevation = 1f
                    holder.itemView.all_card.setCardBackgroundColor(Color.parseColor("#25ffffff"))
                    holder.itemView.scaleX = 0.85f
                    holder.itemView.scaleY = 0.85f
                    holder.itemView.text.setTextColor(Color.parseColor("#eeeeee"))
                }
            }
        }

    }

    class HomeListViewHolder(view: View):RecyclerView.ViewHolder(view)
}