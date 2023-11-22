package com.enfotrix.adminlifechanger.Adapters

import User
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemFaBinding
import com.enfotrix.lifechanger.Models.TransactionModel
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterFA(val data: List<ModelFA> ,  val listener: AdapterFA.OnItemClickListener) :
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

        if (position < data.size) {
            holder.bind(data[position])
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(val itemBinding: ItemFaBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(modelFA: ModelFA) {

            val context = itemBinding.root.context
            itemBinding.tvName.text = modelFA.firstName

            Glide.with(context).load(modelFA.photo).centerCrop().placeholder(R.drawable.ic_launcher_background).into(itemBinding.imgUserProfile)



            itemBinding.tvActiveInvestment.text=modelFA.cnic //ActiveInvestment_Counter
            itemBinding.tvInActiveInvestment.text=modelFA.phone //InActiveInvestment_Counter
            itemBinding.tvProfit.text=modelFA.address //Profit_Counter






            itemBinding.layFA.setOnClickListener { listener.onItemClick(modelFA)


            }

        }
    }
}