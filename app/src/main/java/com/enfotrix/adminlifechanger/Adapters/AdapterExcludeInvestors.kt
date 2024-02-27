package com.enfotrix.adminlifechanger.Adapters

import User
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ItemInvestorsBinding

class AdapterExcludeInvestors(
    var activity: String,
    val data: List<User>,
    val listener: OnItemClickListener,
    val frombtn: String
)
    : RecyclerView.Adapter<AdapterExcludeInvestors.ViewHolder>() {

    var constant = Constants()

    interface OnItemClickListener {
        fun onItemClick(user: User)
        fun onAssignClick(user: User)
        fun onRemoveClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemInvestorsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int { return data.size }

    inner class ViewHolder(val itemBinding: ItemInvestorsBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(user: User) {
            val context = itemBinding.root.context
            if (frombtn=="remove"){
                itemBinding.btnRemove.visibility=View.VISIBLE
                itemBinding.btnAdd.visibility=View.GONE

            }
            else if(frombtn=="select"){
                itemBinding.btnRemove.visibility=View.GONE
                itemBinding.btnAdd.visibility=View.VISIBLE
            }
            else if(frombtn=="profitDeatails"){
                itemBinding.btnRemove.visibility=View.GONE
                itemBinding.btnAdd.visibility=View.GONE
            }

            itemBinding.btnRemove.setOnClickListener { listener.onRemoveClick(user) }
            itemBinding.btnAdd.setOnClickListener { listener.onItemClick(user) }
            itemBinding.tvInvestorName.text = "${user.firstName}"
            itemBinding.tvCNIC.text = user.cnic
            Glide.with(context)
                .load(user.photo)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_background) // Placeholder image while loading
                .into(itemBinding.ivprofile)


        }
    }
}
