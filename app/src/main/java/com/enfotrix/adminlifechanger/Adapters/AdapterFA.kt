package com.enfotrix.adminlifechanger.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemFaBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterFA(val data: List<ModelFA>, val listener: AdapterFA.OnItemClickListener) :
    RecyclerView.Adapter<AdapterFA.ViewHolder>() {


    var constant = Constants()

    interface OnItemClickListener {
        fun onItemClick(modelFA: ModelFA)
        fun onDeleteClick(modelFA: ModelFA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterFA.ViewHolder {
        return ViewHolder(ItemFaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AdapterFA.ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val itemBinding: ItemFaBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(modelFA: ModelFA) {

            val context = itemBinding.root.context
            itemBinding.tvName.text = modelFA.firstName
            itemBinding.tvCNIC.text = modelFA.cnic
            itemBinding.tvMobile.text = modelFA.phone
            Glide.with(context)
                .load(modelFA.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(itemBinding.imgUserProfile)

            itemBinding.tvtimestamp.text="${SimpleDateFormat( "hh:mm a dd/MM/yy", Locale.getDefault()).format(modelFA.createdAt!!.toDate()).toString()}"



            itemBinding.layFA.setOnClickListener {
                listener.onItemClick(modelFA)
                /*
            itemBinding.layFA.setOnClickListener { listener.onItemClick(modelFA) }*/
                /* itemBinding.tvDate.text= SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(profitTaxModel.createdAt!!.toDate()).toString()
             itemBinding.tvPreviousBalance.text=profitTaxModel.previousBalance
             itemBinding.tvNewBalance.text=profitTaxModel.newBalance


             if(activity.equals(constant.FROM_PROFIT)){
                 itemBinding.tvProfitTax.text=profitTaxModel.amount
                 itemBinding.tvProfitTax.setTextColor(0xFF2F9B47.toInt())
             }
             else if(activity.equals(constant.FROM_TAX)){

                 itemBinding.tvProfitTax.text= "-"+profitTaxModel.amount
                 itemBinding.tvProfitTax.setTextColor(Color.RED)

             }*/


            }

        }
    }
}