package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.MenuData
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.CLICKED

class MenuAdapter(
    private val context: Context,
    private var list: List<MenuData>,
    private val listener: ClickListener
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.menu_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt.text = model.text
        if (model.icon == 0) {
            holder.icon.visibility = View.GONE
            holder.txt.setTextColor(context.resources.getColor(R.color.alphaWhite,context.theme))
        } else {
            holder.icon.setImageResource(model.icon)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<MenuData>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt: TextView
        val icon: ImageView

        init {
            txt = itemView.findViewById(R.id.txt)
            icon = itemView.findViewById(R.id.img)

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(CLICKED,"menu")
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}