package com.enfotrix.adminlifechanger.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.AgentWithdrawModel
import com.enfotrix.adminlifechanger.databinding.ItemAgentWithdrawBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FaWithdrawAdapter (var activity:String, val data: List<AgentWithdrawModel>) : RecyclerView.Adapter<FaWithdrawAdapter.ViewHolder>(){


    var constant= Constants()

    interface OnItemClickListener {
        fun onItemClick(agentWithdrawModel: AgentWithdrawModel)
        fun onDeleteClick(agentWithdrawModel: AgentWithdrawModel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAgentWithdrawBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemAgentWithdrawBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(agentWithdrawModel: AgentWithdrawModel) {


            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val formattedDate = agentWithdrawModel. lastWithdrawReqDate.toDate().let { dateFormat.format(it) }

            itemBinding.tvReqDate.text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(agentWithdrawModel.lastWithdrawReqDate.toDate())
            itemBinding.tvPreviousBalance.text = agentWithdrawModel.lastWithdrawBalance

            when (activity) {
                constant.FROM_PENDING_WITHDRAW_REQ -> {
                    itemBinding.tvApprovedDate.text = constant.TRANSACTION_STATUS_PENDING
                    //itemBinding.tvNewBalance.text = constant.TRANSACTION_STATUS_PENDING
                    itemBinding.tvPreviousBalance.text = agentWithdrawModel.totalWithdrawBalance
                    itemBinding.tvReqAmount.text = "-${agentWithdrawModel.withdrawBalance}"
                    itemBinding.tvApprovedDate.setTextColor(Color.RED)
                    itemBinding.tvReqAmount.setTextColor(Color.RED)
                }
                constant.FROM_APPROVED_WITHDRAW_REQ, constant.FROM_APPROVED_INVESTMENT_REQ, constant.FROM_PROFIT, constant.FROM_TAX -> {
                    itemBinding.tvPreviousBalance.text = agentWithdrawModel.totalWithdrawBalance
                    //itemBinding.tvNewBalance.text = transactionModel.newBalance





                    if (activity == constant.FROM_PROFIT || activity == constant.FROM_TAX ) {

                        itemBinding.tvApprovedDate.visibility = View.GONE
                    }

                    if (activity == constant.FROM_APPROVED_WITHDRAW_REQ || activity == constant.FROM_APPROVED_INVESTMENT_REQ ) {

                        itemBinding.tvApprovedDate.text = formattedDate
                    }

                    if (activity == constant.FROM_TAX || activity == constant.FROM_APPROVED_WITHDRAW_REQ) {

                        itemBinding.tvReqAmount.text = "-${agentWithdrawModel.withdrawBalance}"
                        itemBinding.tvReqAmount.setTextColor(Color.RED)

                    } else{
                        itemBinding.tvReqAmount.text =agentWithdrawModel.withdrawBalance
                        itemBinding.tvReqAmount.setTextColor(0xFF2F9B47.toInt())
                    }
                    //itemBinding.tvNewBalance.setTextColor(0xFF2F9B47.toInt())
                    //itemBinding.tvReqAmount.setTextColor(if (activity == constant.FROM_TAX) Color.RED else 0xFF2F9B47.toInt())
                }
                constant.FROM_PENDING_INVESTMENT_REQ -> {
                    itemBinding.tvApprovedDate.text = constant.TRANSACTION_STATUS_PENDING
                    //itemBinding.tvNewBalance.text = constant.TRANSACTION_STATUS_PENDING
                    itemBinding.tvReqAmount.text = agentWithdrawModel.withdrawBalance
                    itemBinding.tvPreviousBalance.text = agentWithdrawModel.lastWithdrawBalance
                    itemBinding.tvApprovedDate.setTextColor(Color.RED)
                    //itemBinding.tvNewBalance.setTextColor(Color.RED)
                    itemBinding.tvReqAmount.setTextColor(0xFF2F9B47.toInt())
                }
            }


            /*itemBinding.tvReqDate.text=SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionModel.createdAt.toDate()).toString()
            itemBinding.tvPreviousBalance.text=transactionModel.previousBalance


            if(activity.equals(constant.FROM_PENDING_WITHDRAW_REQ)){

                itemBinding.tvApprovedDate.text=constant.TRANSACTION_STATUS_PENDING
                itemBinding.tvNewBalance.text=constant.TRANSACTION_STATUS_PENDING
                itemBinding.tvReqAmount.text="-"+transactionModel.amount
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvApprovedDate.setTextColor(Color.RED)
                itemBinding.tvNewBalance.setTextColor(Color.RED)
                itemBinding.tvReqAmount.setTextColor(Color.RED)

            }
            else if(activity.equals(constant.FROM_APPROVED_WITHDRAW_REQ)){

                itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                if(transactionModel.transactionAt!=null){
                    val formattedDate = dateFormat.format(transactionModel.transactionAt!!.toDate())
                    itemBinding.tvApprovedDate.text = formattedDate
                }
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvNewBalance.text=transactionModel.newBalance
                itemBinding.tvReqAmount.text="-"+transactionModel.amount
                itemBinding.tvNewBalance.setTextColor(0xFF2F9B47.toInt())
                itemBinding.tvReqAmount.setTextColor(Color.RED)

            }
            else if(activity.equals(constant.FROM_PENDING_INVESTMENT_REQ)){


                itemBinding.tvApprovedDate.text=constant.TRANSACTION_STATUS_PENDING
                itemBinding.tvNewBalance.text=constant.TRANSACTION_STATUS_PENDING
                itemBinding.tvReqAmount.text=transactionModel.amount
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvApprovedDate.setTextColor(Color.RED)
                itemBinding.tvNewBalance.setTextColor(Color.RED)
                itemBinding.tvReqAmount.setTextColor(0xFF2F9B47.toInt())

            }
            else if(activity.equals(constant.FROM_APPROVED_INVESTMENT_REQ)){

                if(transactionModel.transactionAt!=null) itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

//                itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy",
                //                Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                if(transactionModel.transactionAt!=null){
                    val formattedDate = dateFormat.format(transactionModel.transactionAt!!.toDate())
                    itemBinding.tvApprovedDate.text = formattedDate
                }
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvNewBalance.text=transactionModel.newBalance
                itemBinding.tvReqAmount.text=transactionModel.amount
                itemBinding.tvNewBalance.setTextColor(0xFF2F9B47.toInt())
                itemBinding.tvReqAmount.setTextColor(0xFF2F9B47.toInt())

            }
            else if(activity.equals(constant.FROM_PROFIT)){

                if(transactionModel.transactionAt!=null) itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

//                itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy",
                //                Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                if(transactionModel.transactionAt!=null){
                    val formattedDate = dateFormat.format(transactionModel.transactionAt!!.toDate())
                    itemBinding.tvApprovedDate.text = formattedDate
                }
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvNewBalance.text=transactionModel.newBalance
                itemBinding.tvReqAmount.text=transactionModel.amount
                itemBinding.tvNewBalance.setTextColor(0xFF2F9B47.toInt())
                itemBinding.tvReqAmount.setTextColor(0xFF2F9B47.toInt())

            }
            else if(activity.equals(constant.FROM_TAX)){

                if(transactionModel.transactionAt!=null) itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

//                itemBinding.tvApprovedDate.text==SimpleDateFormat("dd/MM/yy",
                //                Locale.getDefault()).format(transactionModel.transactionAt!!.toDate()).toString()

                val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                if(transactionModel.transactionAt!=null){
                    val formattedDate = dateFormat.format(transactionModel.transactionAt!!.toDate())
                    itemBinding.tvApprovedDate.text = formattedDate
                }
                itemBinding.tvPreviousBalance.text=transactionModel.previousBalance

                itemBinding.tvNewBalance.text=transactionModel.newBalance
                itemBinding.tvReqAmount.text="-"+transactionModel.amount
                itemBinding.tvNewBalance.setTextColor(0xFF2F9B47.toInt())
                itemBinding.tvReqAmount.setTextColor(Color.RED)

            }*/





        }

    }

}