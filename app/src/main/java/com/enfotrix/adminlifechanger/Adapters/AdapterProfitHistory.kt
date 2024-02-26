package com.enfotrix.adminlifechanger.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ProfitModel
import com.enfotrix.adminlifechanger.databinding.ItemEarningBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterProfitHistory(val data: List<ProfitModel>, val mContext: Context) : RecyclerView.Adapter<AdapterProfitHistory.ViewHolder>() {

    var constant = Constants()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemEarningBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val itemBinding: ItemEarningBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(profitModel: ProfitModel) {
            itemBinding.tvPreviousBalance.text = profitModel.previousProfit
            itemBinding.tvEarning.text = profitModel.newProfit
            itemBinding.tvReqDate.text =
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(profitModel.createdAt.toDate())
            itemBinding.tvDisc.text = profitModel.remarks
        }

    }
}
