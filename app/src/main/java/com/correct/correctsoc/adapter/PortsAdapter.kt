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
        val txt_port: TextView
        val txt_service: TextView
        val txt_protocol: TextView
        val txt_version: TextView
        val txt_cpe: TextView
        val txt_cve: TextView
        val see_more: ImageButton
        val cpe: TextView
        val txt_click: TextView

        init {

            cpe = itemView.findViewById(R.id.cpe)
            txt_port = itemView.findViewById(R.id.txt_port)
            txt_service = itemView.findViewById(R.id.txt_service)
            txt_protocol = itemView.findViewById(R.id.txt_protocol)
            txt_version = itemView.findViewById(R.id.txt_version)
            txt_cpe = itemView.findViewById(R.id.txt_cpe)
            txt_cve = itemView.findViewById(R.id.txt_cve)
            see_more = itemView.findViewById(R.id.btn_see_more)
            txt_click = itemView.findViewById(R.id.txt_click_here)

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