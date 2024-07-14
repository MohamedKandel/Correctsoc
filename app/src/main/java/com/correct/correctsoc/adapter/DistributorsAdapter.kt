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
import com.correct.correctsoc.data.pay.DistributorsModel
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.PHONE_NUMBER
import com.correct.correctsoc.helper.Constants.WA_NUMBER

class DistributorsAdapter(
    private val context: Context,
    private var list: List<DistributorsModel>,
    private val listener: ClickListener
) : RecyclerView.Adapter<DistributorsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contacts_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            hideViews(true, holder)
            txt_name.text = list[position].contact_name
            txt_wa_number.text = list[position].whatsapp
            txt_phone_number.text = list[position].phone_number
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<DistributorsModel>) {
        this.list = newList
        this.notifyDataSetChanged();
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txt_name: TextView
        val txt_phone_number: TextView
        val txt_wa_number: TextView
        val phone_icon: ImageView
        val call_icon: ImageView
        val wa_icon: ImageView
        val vodafone_icon: ImageView
        val etisalat_icon: ImageView
        val orange_icon: ImageView
        val instaPay_icon: ImageView
        val more_icon: ImageView
        val line: View
        val less_icon: ImageView
        private var moreDetails = false

        init {
            txt_name = itemView.findViewById(R.id.txt_contact_name)
            phone_icon = itemView.findViewById(R.id.phone_icon)
            more_icon = itemView.findViewById(R.id.more_icon)
            // hide these views till click on more
            less_icon = itemView.findViewById(R.id.less_icon)
            txt_phone_number = itemView.findViewById(R.id.txt_phone_number)
            txt_wa_number = itemView.findViewById(R.id.txt_wa_number)
            call_icon = itemView.findViewById(R.id.call_icon)
            wa_icon = itemView.findViewById(R.id.wa_icon)
            vodafone_icon = itemView.findViewById(R.id.img_vod)
            etisalat_icon = itemView.findViewById(R.id.img_etisalat)
            orange_icon = itemView.findViewById(R.id.img_orange)
            instaPay_icon = itemView.findViewById(R.id.img_insta_pay)
            line = itemView.findViewById(R.id.view)

            hideViews(true, this)

            // open whatsapp for that number
            txt_wa_number.setOnClickListener {
                val number = txt_wa_number.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(WA_NUMBER, number)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            wa_icon.setOnClickListener {
                val number = txt_wa_number.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(WA_NUMBER, number)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            // open call dial for that number
            txt_phone_number.setOnClickListener {
                val number = txt_phone_number.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(PHONE_NUMBER, number)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
            call_icon.setOnClickListener {
                val number = txt_phone_number.text.toString().trim()
                val bundle = Bundle()
                bundle.putString(PHONE_NUMBER, number)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            more_icon.setOnClickListener {
                hideViews(false, this)
                moreDetails = true
                listener.onItemClickListener(bindingAdapterPosition, null)
            }

            less_icon.setOnClickListener {
                hideViews(true, this)
                moreDetails = false
            }

            itemView.setOnClickListener {
                if (!moreDetails) {
                    hideViews(true, this)
                    moreDetails = true
                } else {
                    hideViews(false, this)
                    moreDetails = false
                }
                listener.onItemClickListener(bindingAdapterPosition, null)
            }
            itemView.setOnLongClickListener {
                listener.onLongItemClickListener(bindingAdapterPosition, null)
                true
            }
        }
    }

    private fun hideViews(isHide: Boolean, holder: ViewHolder) {
        if (isHide) {
            holder.apply {
                txt_phone_number.visibility = View.GONE
                txt_wa_number.visibility = View.GONE
                call_icon.visibility = View.GONE
                wa_icon.visibility = View.GONE
                vodafone_icon.visibility = View.GONE
                etisalat_icon.visibility = View.GONE
                orange_icon.visibility = View.GONE
                instaPay_icon.visibility = View.GONE
                line.visibility = View.GONE
                less_icon.visibility = View.GONE
                phone_icon.visibility = View.VISIBLE
                more_icon.visibility = View.VISIBLE
            }
        } else {
            holder.apply {
                txt_phone_number.visibility = View.VISIBLE
                txt_wa_number.visibility = View.VISIBLE
                call_icon.visibility = View.VISIBLE
                wa_icon.visibility = View.VISIBLE
                vodafone_icon.visibility = View.VISIBLE
                etisalat_icon.visibility = View.VISIBLE
                orange_icon.visibility = View.VISIBLE
                instaPay_icon.visibility = View.VISIBLE
                line.visibility = View.VISIBLE
                less_icon.visibility = View.VISIBLE
                phone_icon.visibility = View.GONE
                more_icon.visibility = View.GONE

                // vodafone
                if (!list[bindingAdapterPosition].isVodafoneAvailable) {
                    vodafone_icon.visibility = View.GONE
                } else {
                    vodafone_icon.visibility = View.VISIBLE
                }
                // orange
                if (list[bindingAdapterPosition].isOrangeAvailable) {
                    orange_icon.visibility = View.VISIBLE
                } else {
                    orange_icon.visibility = View.GONE
                }
                // etisalat
                if (list[bindingAdapterPosition].isEtisalatAvailable) {
                    etisalat_icon.visibility = View.VISIBLE
                } else {
                    etisalat_icon.visibility = View.GONE
                }
                // insta pay
                if (list[bindingAdapterPosition].isInstaPayAvailable) {
                    instaPay_icon.visibility = View.VISIBLE
                } else {
                    instaPay_icon.visibility = View.GONE
                }
            }
        }
    }
}