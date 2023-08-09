package com.enfotrix.lifechanger.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.databinding.ItemProfitTaxBinding
import com.enfotrix.lifechanger.Models.ModelProfitTax
import java.text.SimpleDateFormat
import java.util.Locale

class ProfitTaxAdapter (var activity:String, val data: List<ModelProfitTax>) : RecyclerView.Adapter<ProfitTaxAdapter.ViewHolder>(){


    var constant= Constants()

    interface OnItemClickListener {
        fun onItemClick(profitTaxModel: ModelProfitTax)
        fun onDeleteClick(profitTaxModel: ModelProfitTax)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfitTaxAdapter.ViewHolder {
        return ViewHolder(ItemProfitTaxBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ProfitTaxAdapter.ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }

    inner class ViewHolder(val itemBinding: ItemProfitTaxBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(profitTaxModel: ModelProfitTax) {



            itemBinding.tvDate.text= SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(profitTaxModel.createdAt!!.toDate()).toString()
            itemBinding.tvPreviousBalance.text=profitTaxModel.previousBalance
            itemBinding.tvNewBalance.text=profitTaxModel.newBalance


            if(activity.equals(constant.FROM_PROFIT)){
                itemBinding.tvProfitTax.text=profitTaxModel.amount
                itemBinding.tvProfitTax.setTextColor(0xFF2F9B47.toInt())
            }
            else if(activity.equals(constant.FROM_TAX)){

                itemBinding.tvProfitTax.text= "-"+profitTaxModel.amount
                itemBinding.tvProfitTax.setTextColor(Color.RED)

            }





        }

    }
}