package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.AppInfo
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.ISLOCKED
import com.correct.correctsoc.helper.Constants.PACKAGE
import com.correct.correctsoc.helper.HelperClass
import com.correct.correctsoc.room.App

class AppsAdapter(
    private val context: Context,
    private var list: List<App>,
    private val listener: ClickListener,
    private val databaseList: List<App>?,
    private val helper: HelperClass
) : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.application_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.txt_app.text = list[position].appName
        holder.app_icon.setImageDrawable(helper.getAppIcon(context, list[position].packageName))
        if (databaseList != null) {
            val lockedPkgs = databaseList.map { it.packageName }
            if (model.packageName in lockedPkgs) {
                holder.txt_status.text = context.resources.getString(R.string.lock)
                holder.lock_icon.setImageResource(R.drawable.lock_icon)
                holder.lock_icon.tag = context.resources.getString(R.string.lock)
            } else {
                holder.txt_status.text = context.resources.getString(R.string.unlock)
                holder.lock_icon.setImageResource(R.drawable.lock_open_icon)
                holder.lock_icon.tag = context.resources.getString(R.string.unlock)
            }
        } else {
            holder.txt_status.text = context.resources.getString(R.string.unlock)
            holder.lock_icon.setImageResource(R.drawable.lock_open_icon)
            holder.lock_icon.tag = context.resources.getString(R.string.unlock)
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<App>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_app: TextView = itemView.findViewById(R.id.txt_app_name)
        val txt_status: TextView = itemView.findViewById(R.id.txt_app_status)
        val lock_icon: ImageView = itemView.findViewById(R.id.img_status)
        val app_icon: ImageView = itemView.findViewById(R.id.app_icon)

        init {
            itemView.setOnClickListener {
                Log.i("IsAppLock","${lock_icon.tag}")
                val bundle = Bundle()
                bundle.putString(ISLOCKED,"${lock_icon.tag}")
                bundle.putString(PACKAGE,list[bindingAdapterPosition].packageName)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}