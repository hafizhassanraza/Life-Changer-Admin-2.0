package com.enfotrix.lifechanger.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.databinding.ItemInvestorAccountBinding
import com.enfotrix.lifechanger.Models.ModelBankAccount

class InvestorAccountsAdapter(var activity:String,val data: List<ModelBankAccount>,val listener: OnItemClickListener)
    : RecyclerView.Adapter<InvestorAccountsAdapter.ViewHolder>() {


    var constant= Constants()

    interface OnItemClickListener {
        fun onItemClick(modelBankAccount: ModelBankAccount)
        fun onDeleteClick(modelBankAccount: ModelBankAccount)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemInvestorAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemInvestorAccountBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(modelBankAccount: ModelBankAccount) {

            if(!activity.equals(constant.FROM_INVESTOR_ACCOUNTS)) itemBinding.imgDelete.setVisibility(View.GONE)
            itemBinding.tvBankName.text=modelBankAccount.bank_name
            itemBinding.tvAccountNumber.text=modelBankAccount.account_number
            itemBinding.tvAccountTittle.text=modelBankAccount.account_tittle
            itemBinding.layItem.setOnClickListener{ listener.onItemClick(modelBankAccount)}
            itemBinding.imgDelete.setOnClickListener{ listener.onDeleteClick(modelBankAccount)}
        }

    }



}