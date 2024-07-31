package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.openPorts.Port
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.LIST
import com.correct.correctsoc.helper.Constants.MORE
import com.correct.correctsoc.helper.hide
import com.correct.correctsoc.helper.show

class PortsAdapter(
    /*private val helper: HelperClass,
    private val context: Context,*/
    private var list: List<Port>,
    private val listener: ClickListener
) : RecyclerView.Adapter<PortsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.open_port_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_port.text = " ${model.port}"
        holder.txt_version.text =" ${model.version}"
        holder.txt_service.text = " ${model.service}"
        holder.txt_protocol.text = " ${model.protocol}"
        if (model.cpe.isNullOrBlank()) {
            holder.txt_cpe.text = ""
        } else {
            holder.txt_cpe.text = " ${model.cpe}"
        }
        if (model.cvEs.size > 0) {
            holder.txt_cve.text = " [..]"
            holder.see_more.show()
            holder.txt_click.show()
        } else {
            holder.txt_cve.text = " [0]"
            holder.see_more.hide()
            holder.txt_click.hide()
        }
        /*if (helper.getLang(context).equals("ar")) {
            holder.cpe.text.toString().replace(":","")
        }*/
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Port>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_port: TextView = itemView.findViewById(R.id.txt_port)
        val txt_service: TextView = itemView.findViewById(R.id.txt_service)
        val txt_protocol: TextView = itemView.findViewById(R.id.txt_protocol)
        val txt_version: TextView = itemView.findViewById(R.id.txt_version)
        val txt_cpe: TextView = itemView.findViewById(R.id.txt_cpe)
        val txt_cve: TextView = itemView.findViewById(R.id.txt_cve)
        val see_more: ImageButton = itemView.findViewById(R.id.btn_see_more)
        val cpe: TextView = itemView.findViewById(R.id.cpe)
        val txt_click: TextView = itemView.findViewById(R.id.txt_click_here)

        init {
            txt_click.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED, MORE)
                bundle.putParcelableArrayList(LIST, ArrayList(list[bindingAdapterPosition].cvEs))
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            see_more.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED, MORE)
                bundle.putParcelableArrayList(LIST, ArrayList(list[bindingAdapterPosition].cvEs))
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED, ITEM)
                bundle.putParcelableArrayList(LIST, ArrayList(list[bindingAdapterPosition].cvEs))
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}