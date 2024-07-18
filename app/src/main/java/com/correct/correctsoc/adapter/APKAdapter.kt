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
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.Constants.DELETE
import com.correct.correctsoc.helper.Constants.ITEM
import com.correct.correctsoc.helper.Constants.PKG_NAME

class APKAdapter(
    private val context: Context,
    private var list: List<AppInfo>,
    private val listener: ClickListener
) : RecyclerView.Adapter<APKAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.apps_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_appName.text = model.appName
        holder.txt_pkgName.text = model.packageName
        holder.img_icon.setImageDrawable(model.appIcon)
        holder.icon_delte.setImageResource(R.drawable.delete_icon)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<AppInfo>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_appName: TextView
        val img_icon: ImageView
        val txt_pkgName: TextView
        val icon_delte: ImageButton

        init {
            txt_appName = itemView.findViewById(R.id.app_name)
            txt_pkgName = itemView.findViewById(R.id.pkg_name)
            img_icon = itemView.findViewById(R.id.app_icon)
            icon_delte = itemView.findViewById(R.id.delete_icon)

            icon_delte.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(PKG_NAME, list[bindingAdapterPosition].packageName)
                bundle.putString(CLICKED, DELETE)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(PKG_NAME, list[bindingAdapterPosition].packageName)
                bundle.putString(CLICKED, ITEM)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}