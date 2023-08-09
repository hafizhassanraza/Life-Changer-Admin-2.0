package com.enfotrix.lifechanger.Adapters

import User
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ItemTransactionBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Locale

class TransactionsAdapter (
    var activity:String,
    val data: List<TransactionModel>,
    val users: List<User>,
    val financialAdvisors: List<ModelFA>,
    val listener: TransactionsAdapter.OnItemClickListener) : RecyclerView.Adapter<TransactionsAdapter.ViewHolder>(){


    var constant= Constants()

    interface OnItemClickListener {
        fun onItemClick(transactionModel: TransactionModel, user: User)
        fun onDeleteClick(transactionModel: TransactionModel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {



        users.find { it.id.equals(data[position].investorID)}?.let { holder.bind(data[position], it) }

        //holder.bind(data[position])

    }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemTransactionBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(transactionModel: TransactionModel, user: User)
        {




            itemBinding.layItem.setOnClickListener {
                listener.onItemClick(transactionModel,user)
            }


            itemBinding.tvInvestorName.text="${user.firstName}"
            itemBinding.tvPreviousBalance.text="${transactionModel.previousBalance}"
            itemBinding.tvInvestmentDate.text="${SimpleDateFormat( "hh:mm a dd/MM/yy", Locale.getDefault()).format(transactionModel.createdAt!!.toDate()).toString()}"
            if(activity.equals(constant.FROM_PENDING_WITHDRAW_REQ)) itemBinding.tvInvestWithdrawHeader.text="Withdraw"
            itemBinding.tvInvestment.text="${transactionModel.amount}"





        }

    }

}