import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.correct.correctsoc.R
import com.correct.correctsoc.helper.ClickListener
import com.correct.correctsoc.helper.Constants.MAIL

class AccountsAdapter(
    private var list: List<String>,
    private val listener: ClickListener
) : RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.account_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountsAdapter.ViewHolder, position: Int) {
        holder.option.text = list[position]
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: List<String>) {
        this.list = list
        this.notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val option = itemView.findViewById<RadioButton>(R.id.account_option)

        init {

            val bundle = Bundle()

            option.setOnClickListener {
                bundle.putBoolean(MAIL, true)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }

            itemView.setOnClickListener {
                bundle.putBoolean(MAIL, true)
                listener.onItemClickListener(bindingAdapterPosition, bundle)
            }
        }
    }
}