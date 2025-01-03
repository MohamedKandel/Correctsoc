package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.openPorts.CvEs
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.Constants.MORE
import com.correct.correctsoc.helper.Constants.URL

class CVEAdapter(
    private var list: List<CvEs>,
    private val listener: ClickListener
) : RecyclerView.Adapter<CVEAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cve_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_id.text = " ${model.id}"
        holder.txt_url.text = " ${model.url}"
        holder.txt_cvss.text = " ${model.cvss}"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<CvEs>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_id: TextView = itemView.findViewById(R.id.txt_id)
        val txt_url: TextView = itemView.findViewById(R.id.txt_url)
        val txt_cvss: TextView = itemView.findViewById(R.id.txt_cvss)

        init {
            txt_url.setOnClickListener {
                val url = txt_url.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(CLICKED, MORE)
                bundle.putString(URL, url)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val url = txt_url.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(CLICKED, MORE)
                bundle.putString(URL, url)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}