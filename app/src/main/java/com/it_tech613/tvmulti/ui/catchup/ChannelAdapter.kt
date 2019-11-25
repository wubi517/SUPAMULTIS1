package com.it_tech613.tvmulti.ui.catchup

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.it_tech613.tvmulti.R
import com.it_tech613.tvmulti.models.EPGChannel
import kotlinx.android.synthetic.main.home_category_list_item.view.*

@Suppress("DEPRECATION")
class ChannelAdapter(val list: MutableList<out EPGChannel>, val clickListenerFunction: (Int, EPGChannel) -> Unit) : RecyclerView.Adapter<ChannelAdapter.HomeListViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_category_list_item,parent,false)
        return HomeListViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {
        val epgChannel=list[position]
        if (epgChannel.imageURL!=null && epgChannel.imageURL != "") {
            holder.itemView.image.setImageURI(Uri.parse(epgChannel.imageURL))
            holder.itemView.title.visibility = View.GONE
            holder.itemView.name.visibility = View.VISIBLE
            holder.itemView.name.text = epgChannel.name
        }else{
            holder.itemView.image.setActualImageResource(R.drawable.pkg_dlg_title_bg)
            holder.itemView.title.visibility = View.VISIBLE
            holder.itemView.title.text = epgChannel.name
            holder.itemView.name.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            clickListenerFunction(position,epgChannel)
        }
        holder.itemView.setOnFocusChangeListener {_ , hasFocus ->
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
    }

    class HomeListViewHolder(view: View):RecyclerView.ViewHolder(view)
}