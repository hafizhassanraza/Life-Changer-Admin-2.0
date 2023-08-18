package com.enfotrix.adminlifechanger.Adapters

import User
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemInvestorAccountBinding
import com.enfotrix.adminlifechanger.databinding.ItemUserBinding

class InvestorAdapter (var activity:String, val data: List<User>, val listener: OnItemClickListener)
    : RecyclerView.Adapter<InvestorAdapter.ViewHolder>() {

    var constant= Constants()

    interface OnItemClickListener {
        fun onItemClick(user:User)
        fun onAssignClick(user:User)
        fun onRemoveClick(user:User)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { holder.bind(data[position]) }
    override fun getItemCount(): Int { return data.size }
    inner class ViewHolder(val itemBinding: ItemUserBinding) : RecyclerView.ViewHolder(itemBinding.root){

        fun bind(user:User) {
            val context = itemBinding.root.context
            if(activity.equals(constant.FROM_ASSIGNED_FA)) itemBinding.btnAssign.setVisibility(View.GONE)
            else if(activity.equals(constant.FROM_UN_ASSIGNED_FA)) itemBinding.btnRemove.setVisibility(View.GONE)
            else if(activity.equals(constant.FROM_PENDING_INVESTOR_REQ)) {
                itemBinding.btnRemove.setVisibility(View.GONE)
                itemBinding.btnAssign.setVisibility(View.GONE)
            }

            itemBinding.tvInvestorName.text= "${user.firstName}"
            itemBinding.tvCNIC.text=user.cnic

            Glide.with(context)
                .load(user.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background) // Placeholder image while loading
                .into(itemBinding.ivprofile)

            itemBinding.btnAssign.setOnClickListener { listener.onAssignClick(user) }
            itemBinding.btnRemove.setOnClickListener {
                listener.onRemoveClick(user)
            }
            itemBinding.layUser.setOnClickListener { listener.onItemClick(user) }


        }

    }



}