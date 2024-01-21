package com.enfotrix.adminlifechanger.ui

import User
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.enfotrix.adminlifechanger.Constants
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ActivityAddFaBinding
import com.enfotrix.lifechanger.SharedPrefManager
import com.enfotrix.lifechanger.Utils
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch

class ActivityAddFA : AppCompatActivity() {


    private val faViewModel: FAViewModel by viewModels()
    private lateinit var binding: ActivityAddFaBinding


    var constant = Constants()
    private lateinit var utils: Utils
    private lateinit var context: Context
    private lateinit var constants: Constants
    private lateinit var user: User
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var dialog: Dialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddFaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@ActivityAddFA


        supportActionBar?.title = "Add Financial Advisor"


        context = this@ActivityAddFA
        utils = Utils(context)
        constants = Constants()
        sharedPrefManager = SharedPrefManager(context)



        binding.btnProfileRegister.setOnClickListener {
            if ((!IsEmpty()) && IsValid()) checkCNIC(utils.cnicFormate(binding.etCNIC.editText?.text.toString()))
        }


    }


    private fun checkCNIC(cnic: String) {


        utils.startLoadingAnimation()


        lifecycleScope.launch {

            faViewModel.isFAExist(cnic)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        if (task.result.size() > 0) {

                            utils.endLoadingAnimation()

                            Toast.makeText(
                                context,
                                constants.INVESTOR_CNIC_EXIST,
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.etCNIC.editText?.error = constants.INVESTOR_CNIC_EXIST


                        } else {


                            lifecycleScope.launch {


                                faViewModel.addFA(
                                    ModelFA(
                                        cnic,
                                        binding.etFirstName.editText?.text.toString(),
                                        binding.etLastName.editText?.text.toString(),
                                        binding.etAddress.editText?.text.toString(),
                                        "",
                                        "",
                                        "",
                                        "",
                                        "",
                                        binding.etpassword.editText?.text.toString(),
                                        "",
                                        binding.etDesignation.editText?.text.toString(),
                                        Timestamp.now()

                                    )
                                ).observe(this@ActivityAddFA) {
                                    if (it == true) {

                                        lifecycleScope.launch {
                                            faViewModel.getFA()
                                                .addOnCompleteListener { task ->
                                                    utils.endLoadingAnimation()
                                                    if (task.isSuccessful) {
                                                        val list = ArrayList<ModelFA>()
                                                        if (task.result.size() > 0) {
                                                            for (document in task.result) list.add(
                                                                document.toObject(ModelFA::class.java)
                                                                    .apply { id = document.id })

                                                            sharedPrefManager.putFAList(list)

                                                            Toast.makeText(
                                                                context,
                                                                constants.FA_SIGNUP_MESSAGE,
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            startActivity(
                                                                Intent(
                                                                    context,
                                                                    ActivityFA::class.java
                                                                ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                            )
                                                            finish()
                                                        }
                                                    } else Toast.makeText(
                                                        context,
                                                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                }
                                                .addOnFailureListener {
                                                    utils.endLoadingAnimation()
                                                    Toast.makeText(
                                                        context,
                                                        it.message + "",
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                }


                                        }

                                    } else Toast.makeText(
                                        context,
                                        constants.SOMETHING_WENT_WRONG_MESSAGE,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }

                            }

                        }
                    }


                }
                .addOnFailureListener {
                    utils.endLoadingAnimation()
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()

                }

        }

    }


    private fun IsEmpty(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value = true
        if (binding.etCNIC.editText?.text.toString().isEmpty()) binding.etCNIC.editText?.error =
            "Empty CNIC"
        else if (binding.etAddress.editText?.text.toString()
                .isEmpty()
        ) binding.etAddress.editText?.error = "Empty Address"
        else if (binding.etFirstName.editText?.text.toString()
                .isEmpty()
        ) binding.etFirstName.editText?.error = "Empty First Name"
        else if (binding.etLastName.editText?.text.toString()
                .isEmpty()
        ) binding.etLastName.editText?.error = "Empty Last Name"
        else if (binding.etDesignation.editText?.text.toString()
                .isEmpty() )
        else if (binding.etpassword.editText?.text.toString()
                .isEmpty()
        ) binding.etDesignation.editText?.error = "Empty Password"
        //else if (binding.etMobileNumber.editText?.text.toString().isEmpty()) binding.etMobileNumber.editText?.error = "Empty Phone"
        else result.value = false

        return result.value!!
    }

    private fun IsValid(): Boolean {

        val result = MutableLiveData<Boolean>()
        result.value = false
        if (binding.etCNIC.editText?.text.toString().length < 13) binding.etCNIC.editText?.error =
            "Invalid CNIC"
        //else if (binding.etMobileNumber.editText?.text.toString().length<11) binding.etMobileNumber.editText?.error = "Invalid Phone Number"
        else result.value = true

        return result.value!!
    }


}