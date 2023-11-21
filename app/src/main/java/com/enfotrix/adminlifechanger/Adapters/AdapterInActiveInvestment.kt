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
import com.enfotrix.adminlifechanger.databinding.ItemInActiveInvestmentBinding

class AdapterInActiveInvestment (val listInvestor: List<User>, val listInvestment: List<InvestmentModel>,val listener: AdapterInActiveInvestment.OnItemClickListener) :
    RecyclerView.Adapter<AdapterInActiveInvestment.ViewHolder>(){


    interface OnItemClickListener {
        fun onItemClick(investment: InvestmentModel)
        fun addInvestment(investment: InvestmentModel)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterInActiveInvestment.ViewHolder {
        return ViewHolder(ItemInActiveInvestmentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: AdapterInActiveInvestment.ViewHolder, position: Int) {
        holder.bind(listInvestor[position])
    }

    override fun getItemCount(): Int {
        return listInvestor.size
    }

    inner class ViewHolder(val itemBinding: ItemInActiveInvestmentBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(investor: User) {
            val context = itemBinding.root.context

            val investment= listInvestment.first { it.investorID.equals(investor.id)}




            itemBinding.tvName.text = investor.firstName
            Glide.with(context)
                .load(investor.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background)
                .into(itemBinding.imgUserProfile)

            itemBinding.tvActiveInvestment.text = investment.investmentBalance
            itemBinding.tvInActiveInvestment.text = investment.lastInvestment
            itemBinding.btnActive.setOnClickListener { listener.onItemClick(investment) }
            //itemBinding.btnAdd.setOnClickListener { listener.addInvestment(investment) }

        }
    }

}