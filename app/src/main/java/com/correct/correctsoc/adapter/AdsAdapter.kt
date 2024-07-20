package com.correct.correctsoc.adapter;

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.data.user.AdsResponse
import com.correct.correctsoc.data.user.AdsResult
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.AD_OBJECT
import com.correct.correctsoc.helper.Constants.API_TAG
import com.correct.correctsoc.helper.Constants.CLICKED
import com.correct.correctsoc.helper.parseBase64

class AdsAdapter(
    private val context: Context,
    private var list: List<AdsResult>,
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
        /*holder.description.viewTreeObserver.addOnGlobalLayoutListener {

            Log.v("description mohamed displayed", getDisplayedText(holder.description))
            //Log.v(API_TAG, getDisplayedText(holder.description))
        }*/
        holder.img.setImageBitmap(model.image.parseBase64())
    }

    fun getDisplayedText(textView: TextView): String {
        val layout = textView.layout
        val lines = textView.lineCount.coerceAtMost(textView.maxLines)
        val displayedText = StringBuilder()
        for (i in 0 until lines) {
            val start = layout.getLineStart(i)
            val end = layout.getLineEnd(i)
            val lineText = textView.text.substring(start, end)
            displayedText.append(lineText)
        }
        return displayedText.toString()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<AdsResult>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.findViewById(R.id.img_ad)
        val title: TextView = itemView.findViewById(R.id.txt_ad_title)
        val description: TextView = itemView.findViewById(R.id.txt_ad_description)
        val txt_more: TextView = itemView.findViewById(R.id.txt_see_more)
        //val btn_buy: Button = itemView.findViewById(R.id.btn_buy)

        init {
            //txt_more.visibility = View.GONE
            txt_more.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(AD_OBJECT, list[bindingAdapterPosition])
                bundle.putString(CLICKED, "ads")
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putParcelable(AD_OBJECT, list[bindingAdapterPosition])
                bundle.putString(CLICKED, "ads")
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }
}