package com.enfotrix.lifechanger.Adapters

import User
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentTransactionModel
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ItemTransactionBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Locale

class AgentTransactionsAdapter (
    var activity:String,
    val data: List<AgentWithdrawModel>,
    val financialAdvisors: List<ModelFA>,
    val listener: OnItemClickListener) : RecyclerView.Adapter<AgentTransactionsAdapter.ViewHolder>(){


    var constant= Constants()

    interface OnItemClickListener {

        fun onAgentItemClick(agentWithdrawModel: AgentWithdrawModel, modelFA: ModelFA)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {



        financialAdvisors.find { it.id.equals(data[position].fa_ID)}?.let { holder.bind(data[position], it) }

        //holder.bind(data[position])

    }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemTransactionBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(agentWithdrawModel: AgentWithdrawModel, modelFA: ModelFA)
        {




            itemBinding.layItem.setOnClickListener {
                listener.onAgentItemClick(agentWithdrawModel,modelFA)
            }


            itemBinding.tvInvestorName.text="${modelFA.firstName}"
            itemBinding.tvPreviousBalance.text="${agentWithdrawModel.lastWithdrawBalance}"
            itemBinding.tvInvestmentDate.text="${SimpleDateFormat( "hh:mm a dd/MM/yy", Locale.getDefault()).format(agentWithdrawModel.lastWithdrawReqDate!!.toDate()).toString()}"
            if(activity.equals(constant.FROM_PENDING_WITHDRAW_REQ) || activity.equals(constant.FROM_APPROVED_WITHDRAW_REQ)) {
                itemBinding.tvInvestWithdrawHeader.text="Withdraw"

            }
            itemBinding.tvInvestment.text="${agentWithdrawModel.withdrawBalance}"




        }

    }

}