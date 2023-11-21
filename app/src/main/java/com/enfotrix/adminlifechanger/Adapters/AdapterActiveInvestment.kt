package com.enfotrix.adminlifechanger.Adapters

import User
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemActiveInvestmentBinding
import com.enfotrix.adminlifechanger.databinding.ItemFaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class AdapterActiveInvestment (val listInvestor: List<User>, val listInvestment: List<InvestmentModel>) :
    RecyclerView.Adapter<AdapterActiveInvestment.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterActiveInvestment.ViewHolder {
        return ViewHolder(ItemActiveInvestmentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AdapterActiveInvestment.ViewHolder, position: Int) {
        holder.bind(listInvestor[position])
    }

    override fun getItemCount(): Int {
        return listInvestor.size
    }

    inner class ViewHolder(val itemBinding: ItemActiveInvestmentBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(investor: User) {




            val context = itemBinding.root.context
            itemBinding.tvName.text = investor.firstName
            Glide.with(context)
                .load(investor.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(itemBinding.imgUserProfile)
            itemBinding.tvActiveInvestment.text = listInvestment.first { it.investorID.equals(investor.id)}.investmentBalance
            itemBinding.tvName.text = investor.firstName

        }
    }

}