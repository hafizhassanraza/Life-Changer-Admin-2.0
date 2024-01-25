package com.enfotrix.adminlifechanger.ui

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.enfotrix.adminlifechanger.Adapters.FaWithdrawAdapter
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.R
import com.enfotrix.adminlifechanger.databinding.ActivityAgentWithdrawBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils

class ActivityAgentWithdraw : AppCompatActivity() {

    private lateinit var binding: ActivityAgentWithdrawBinding
    private lateinit var utils: Utils
    private lateinit var mContext: Context
    private lateinit var constants: Constants
    private lateinit var sharedPrefManagar: SharedPrefManager
    private lateinit var modelFA: ModelFA

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding=ActivityAgentWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext=this@ActivityAgentWithdraw
        utils = Utils(mContext)
        constants= Constants()
        sharedPrefManagar= SharedPrefManager(mContext)

        binding.imgBack.setOnClickListener{finish()}

        modelFA = ModelFA.fromString(intent.getStringExtra("Fa").toString())!!

        val spinner = binding.spWithdraws

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.withdraw_options, // Replace with your array of items
            R.layout.item_investment_selection_spiner // Use the custom layout
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        binding.rvWithdraws.layoutManager = LinearLayoutManager(mContext)

        binding.spWithdraws.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {

                when (position) {
                    0 -> {
                        binding.rvWithdraws.adapter= FaWithdrawAdapter(constants.FROM_APPROVED_WITHDRAW_REQ,
                            sharedPrefManagar.getAgentWithdrawList().filter { it.fa_ID.equals(modelFA.id) && it.status == constants.TRANSACTION_STATUS_APPROVED })
                    }
                    1 -> {
                        binding.rvWithdraws.adapter= FaWithdrawAdapter(constants.FROM_PENDING_WITHDRAW_REQ,
                        sharedPrefManagar.getAgentWithdrawList().filter { it.fa_ID.equals(modelFA.id) && it.status == constants.TRANSACTION_STATUS_PENDING })
                    }
                }

            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing if nothing is selected
            }
        }

    }
}