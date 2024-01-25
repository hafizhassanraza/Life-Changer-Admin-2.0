package com.enfotrix.adminlifechanger.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelEarning
import com.enfotrix.adminlifechanger.databinding.ItemEarningBinding
import com.enfotrix.adminlifechanger.databinding.ItemStatmentBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterEarning (val earningList: List<ModelEarning>, val listener: OnItemClickListener) : RecyclerView.Adapter<AdapterEarning.ViewHolder>() {


    var constant = Constants()

    interface OnItemClickListener {
        fun onItemClick(modelEarning: ModelEarning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemEarningBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(earningList[position])
    }

    override fun getItemCount(): Int {
        return earningList.size
    }

    inner class ViewHolder(val itemBinding: ItemEarningBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(earning: ModelEarning) {


            itemBinding.tvReqDate.text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(earning.createdAt.toDate())

           // if (earning.status.equals(constant.EARNING_STATUS_WITHDRAW)) itemBinding.tvApprovedDate.text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(earning.withdrawAt?.toDate() ?: "Pending")
           // else itemBinding.tvApprovedDate.text = "Pending"



            itemBinding.tvDisc.text = earning.disc
            itemBinding.tvEarning.text = earning.amount
            itemBinding.tvPreviousBalance.text = earning.balance
            itemBinding.layEarning.setOnClickListener {
                listener.onItemClick(earning)


            }

        }

    }
}
