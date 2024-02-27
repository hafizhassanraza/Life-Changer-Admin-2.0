package com.enfotrix.adminlifechanger.Adapters

import User
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ProfitHistory
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemClientsBinding
import com.enfotrix.adminlifechanger.databinding.ItemProfitHistoryBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import com.enfotrix.lifechanger.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AdapterCreditedInvestors(
    val investorList: List<User>,
    val TransactionList: List<TransactionModel>,
    val mContext: Context,
) : RecyclerView.Adapter<AdapterCreditedInvestors.ViewHolder>() {

    var constant = Constants()
    val sharedPrefManager = SharedPrefManager(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemClientsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(TransactionList[position])
    }

    override fun getItemCount(): Int {
        return TransactionList.size
    }

    inner class ViewHolder(val itemBinding: ItemClientsBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(transactionModel: TransactionModel) {
            val user=investorList.find { it.id==transactionModel.investorID }
           itemBinding.name.text=user!!.firstName
            Glide.with(mContext)
               .load(user.photo)
               .centerCrop()
               .placeholder(R.drawable.ic_launcher_background) // Placeholder image while loading
               .into(itemBinding.photo)
            itemBinding.cnic.text=user!!.cnic
            itemBinding.profit.text=transactionModel!!.amount


        }
    }
}
