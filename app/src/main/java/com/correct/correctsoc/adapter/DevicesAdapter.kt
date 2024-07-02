package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.DevicesData
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.HelperClass

class DevicesAdapter(
    private val context: Context,
    private val helper: HelperClass,
    private var list: List<DevicesData>,
    private val listener: ClickListener
) : RecyclerView.Adapter<DevicesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.devices_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_ip.text = model.ipAddress
//        holder.img_go.setImageResource(R.drawable.arrow_img)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<DevicesData>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val txt_name: TextView
        val txt_ip: TextView
        val img_go: ImageView

        init {
            txt_ip = itemView.findViewById(R.id.txt_ip)
            img_go = itemView.findViewById(R.id.btn_go)

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(ITEM, list[adapterPosition])
                listener.onItemClickListener(adapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(adapterPosition, null)
                true
            }
        }
    }
}