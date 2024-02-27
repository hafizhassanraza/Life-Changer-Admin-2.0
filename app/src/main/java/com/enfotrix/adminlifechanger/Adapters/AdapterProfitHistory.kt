package com.enfotrix.adminlifechanger.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ProfitHistory
import com.enfotrix.adminlifechanger.databinding.ItemProfitHistoryBinding
import com.enfotrix.adminlifechanger.ui.ActivityProfitHistory
import com.enfotrix.lifechanger.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdapterProfitHistory(
    val data: List<ProfitHistory>,
    val mContext: Context,
    val listener: ActivityProfitHistory,
) : RecyclerView.Adapter<AdapterProfitHistory.ViewHolder>() {

    var constant = Constants()
    val sharedPrefManager = SharedPrefManager(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProfitHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    interface OnItemClickListener {
        fun onItemClick(profitModel: ProfitHistory)
        fun onAssignClick(profitModel: ProfitHistory)
        fun onRemoveClick(profitModel: ProfitHistory)
    }
    inner class ViewHolder(val itemBinding: ItemProfitHistoryBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(profitModel: ProfitHistory) {
            itemBinding.activeInvestments.text = profitModel.activeInvestment
            itemBinding.availableBalance.text = profitModel.availableBalance
            itemBinding.tvinactiveInvestments.text =profitModel.inActiveInvestments
            itemBinding.newProfitAmount.text =profitModel.newProfitAmount
            itemBinding.etRemarks.text =profitModel.remarks
            itemBinding.investorsStrength.text=getStrength(profitModel)
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            itemBinding.date.text = dateFormat.format(profitModel.createdAt.toDate())
            itemBinding.btnSeeInvestors.setOnClickListener { listener.onItemClick(profitModel)
            }
        }

    }


    fun getStrength(profitModel: ProfitHistory): String {
        val filteredTransactionList = sharedPrefManager.getTransactionList()
            .filter { it.type == constant.PROFIT_TYPE }
            .filter { transaction ->
                val transactionDate = transaction.createdAt.toDate()
                val profitModelDate = profitModel.createdAt.toDate()

                val transactionCalendar = Calendar.getInstance().apply {
                    time = transactionDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val profitModelCalendar = Calendar.getInstance().apply {
                    time = profitModelDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                return@filter transactionCalendar.timeInMillis == profitModelCalendar.timeInMillis
            }

        // Return the size of the filtered list
        return filteredTransactionList.size.toString()
    }

}
