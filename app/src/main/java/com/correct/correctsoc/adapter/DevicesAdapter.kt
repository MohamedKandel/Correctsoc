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
import com.correct.correctsoc.helper.mappingNumbers

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
        if (helper.getLang(context).equals("ar")) {
            holder.txt_ip.text = model.ipAddress.mappingNumbers()
            holder.img_go.rotation = 180f
        } else {
            holder.txt_ip.text = model.ipAddress
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<DevicesData>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val txt_name: TextView
        val txt_ip: TextView = itemView.findViewById(R.id.txt_ip)
        val img_go: ImageView = itemView.findViewById(R.id.btn_go)
        val img_click: ImageView = itemView.findViewById(R.id.icon_click_here)
        val txt_click: TextView = itemView.findViewById(R.id.txt_click_here)

        init {
            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(ITEM, list[bindingAdapterPosition])
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}