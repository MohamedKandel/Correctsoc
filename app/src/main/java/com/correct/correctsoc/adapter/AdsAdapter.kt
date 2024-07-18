package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.correct.correctsoc.R
import com.correct.correctsoc.data.AdsModel
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.AD_OBJECT
import com.correct.correctsoc.helper.Constants.CLICKED

class AdsAdapter(
    private val context: Context,
    private var list: List<AdsModel>,
    private val listener: ClickListener
) : RecyclerView.Adapter<AdsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ads_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]
        holder.title.text = model.title
        holder.description.text = model.description
        Glide.with(context)
            .load(model.img)
            .placeholder(R.drawable.correct)
            .into(holder.img)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<AdsModel>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_ad)
        val title: TextView = itemView.findViewById(R.id.txt_ad_title)
        val description: TextView = itemView.findViewById(R.id.txt_ad_description)
        val btn_buy: Button = itemView.findViewById(R.id.btn_buy)

        init {
            description.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val layout = description.layout
                    if (layout != null) {
                        val lineCount = layout.lineCount
                        if (lineCount > 0 && layout.getEllipsisCount(lineCount - 1) > 0) {
                            // Text is ellipsized, show the "See More" button
                            description.append("...")
                        } else {
                            // Text is fully displayed, hide the "See More" button

                        }
                    }

                    // Remove the listener to avoid multiple callbacks
                    description.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })

            btn_buy.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(AD_OBJECT, list[bindingAdapterPosition])
                bundle.putString(CLICKED,"ads")
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(AD_OBJECT, list[bindingAdapterPosition])
                bundle.putString(CLICKED,"ads")
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}