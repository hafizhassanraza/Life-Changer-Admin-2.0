package com.enfotrix.lifechanger

import User
import android.content.Context
import android.content.SharedPreferences
import android.view.Display.Mode
import com.enfotrix.adminlifechanger.Models.InvestmentModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.lifechanger.Models.ModelBankAccount
import com.enfotrix.lifechanger.Models.ModelNominee
import com.enfotrix.lifechanger.Models.ModelProfitTax
import com.enfotrix.lifechanger.Models.TransactionModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


class SharedPrefManager(context: Context) {





    private val sharedPref: SharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPref.edit()



    fun saveUser(user: User) {

        editor.putString("Investor", Gson().toJson(user))
        editor.commit()


        /*editor.putString("cnic", user.cnic)
        editor.putString("firstName", user.firstName)
        editor.putString("lastName", user.lastName)
        editor.putString("address", user.address)
        editor.putString("phone", user.phone)
        editor.putString("status", user.status)
        editor.putString("photo", user.status)
        editor.putString("cnic_front", user.status)
        editor.putString("cnic_back", user.status)
        editor.putString("pin", user.pin)
        editor.putString("createdAt", user.createdAt.toString())
        editor.commit()*/

    }



    fun isPhoneNumberAdded(): Boolean {
        return sharedPref.getBoolean("IsPhoneNumberAdded", false)!!
    }
    fun isNomineeAdded(): Boolean {
        return sharedPref.getBoolean("IsNomineeAdded", false)!!
    }
    fun isNomineeBankAdded(): Boolean {
        return sharedPref.getBoolean("IsNomineeBankAdded", false)!!
    }
    fun isUserBankAdded(): Boolean {
        return sharedPref.getBoolean("IsUserBankAdded", false)!!
    }
    fun isUserPhotoAdded(): Boolean {
        return sharedPref.getBoolean("IsUserPhotoAdded", false)!!
    }
    fun isUserCnicAdded(): Boolean {
        return sharedPref.getBoolean("IsUserCnicAdded", false)!!
    }
    fun isNomineeCnicAdded(): Boolean {
        return sharedPref.getBoolean("IsNomineeCnicAdded", false)!!
    }



    fun getNominee(): ModelNominee {

        val json = sharedPref.getString("Nominee", "") ?: ""
        return Gson().fromJson(json, ModelNominee::class.java)

        //return Gson().fromJson(sharedPref.getString("Nominee", "")!!, ModelNominee::class.java)
    }


    fun getFAList(): List<ModelFA>{

        val json = sharedPref.getString("ListFA", "") ?: ""
        val type: Type = object : TypeToken<List<ModelFA?>?>() {}.getType()
        //return Gson().fromJson(json, type)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getUsersList(): List<User>{

        val json = sharedPref.getString("ListUser", "") ?: ""
        val type: Type = object : TypeToken<List<User?>?>() {}.getType()
        //return Gson().fromJson(json, type)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getAccountList(): List<ModelBankAccount>{

        val json = sharedPref.getString("ListAccount", "") ?: ""
        val type: Type = object : TypeToken<List<ModelBankAccount?>?>() {}.getType()
        //return Gson().fromJson(json, type)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getActiveInvestmentList(): List<InvestmentModel>{

        val json = sharedPref.getString("ListActiveInvestment", "") ?: ""
        val type: Type = object : TypeToken<List<InvestmentModel?>?>() {}.getType()
        //return Gson().fromJson(json, type)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getNomineeList(): List<ModelNominee>{

        val json = sharedPref.getString("ListNominee", "") ?: ""
        val type: Type = object : TypeToken<List<ModelNominee?>?>() {}.getType()
        //return Gson().fromJson(json, type)
        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getInvestorBankList(): List<ModelBankAccount>{

        val json = sharedPref.getString("ListInvestorBanks", "") ?: ""
        val type: Type = object : TypeToken<List<ModelBankAccount?>?>() {}.getType()
        return Gson().fromJson(json, type)
    }
    fun getAdminBankList(): List<ModelBankAccount>{

        val json = sharedPref.getString("ListAdminBanks", "") ?: ""
        val type: Type = object : TypeToken<List<ModelBankAccount?>?>() {}.getType()
        return Gson().fromJson(json, type)
    }

    //JNI DETECTED ERROR IN APPLICATION: JNI CallVoidMethodV called with pending exception java.lang.NullPointerException: Gson().fromJson(json, type) must not be null

    fun getProfitTaxList(): List<ModelProfitTax>{

        val json = sharedPref.getString("ListProfitTax", "") ?: ""
        val type: Type = object : TypeToken<List<ModelProfitTax?>?>() {}.getType()
        //return Gson().fromJson(json, type)

        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getWithdrawReqList(): List<TransactionModel>{

        val json = sharedPref.getString("ListWithdrawReq", "") ?: ""
        val type: Type = object : TypeToken<List<TransactionModel?>?>() {}.getType()
        //return Gson().fromJson(json, type)

        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }
    fun getInvestmentReqList(): List<TransactionModel>{

        val json = sharedPref.getString("ListInvestmentReq", "") ?: ""
        val type: Type = object : TypeToken<List<TransactionModel?>?>() {}.getType()
        //return Gson().fromJson(json, type)

        return if (!json.isNullOrEmpty()) {
            Gson().fromJson(json, type) ?: emptyList()
        } else {
            emptyList()
        }
    }


    fun saveNominee(nominee: ModelNominee) {
        /*val gson = Gson()
        val json = gson.toJson(nominee)*/
        editor.putString("Nominee", Gson().toJson(nominee))
        editor.putBoolean("IsNomineeAdded",true)
        editor.commit()

    }
    fun putFAList(list: List<ModelFA>) {
        editor.putString("ListFA", Gson().toJson(list))
        editor.commit()
    }
    fun putAccountList(list: List<ModelBankAccount>) {
        editor.putString("ListAccount", Gson().toJson(list))
        editor.commit()
    }
    fun putNomineeList(list: List<ModelNominee>) {
        editor.putString("ListNominee", Gson().toJson(list))
        editor.commit()
    }
    fun putUserList(list: List<User>) {
        editor.putString("ListUser", Gson().toJson(list))
        editor.commit()
    }
    fun putInvestorBankList(list: List<ModelBankAccount>) {
        editor.putString("ListInvestorBanks", Gson().toJson(list))
        editor.commit()
    }
    fun putAdminBankList(list: List<ModelBankAccount>) {
        editor.putString("ListAdminBanks", Gson().toJson(list))
        editor.commit()
    }
    fun putProfitTaxList(list: List<ModelProfitTax>) {
        editor.putString("ListProfitTax", Gson().toJson(list))
        editor.commit()
    }
    fun putWithdrawReqList(list: List<TransactionModel>) {
        editor.putString("ListWithdrawReq", Gson().toJson(list))
        editor.commit()
    }
    fun putInvestmentReqList(list: List<TransactionModel>) {
        editor.putString("ListInvestmentReq", Gson().toJson(list))
        editor.commit()
    }
    fun putActiveInvestment(list: List<InvestmentModel>) {
        editor.putString("ListActiveInvestment", Gson().toJson(list))
        editor.commit()
    }
    fun putInvestorPhoneNumber(IsPhoneNumberAdded: Boolean) {
        editor.putBoolean("IsPhoneNumberAdded",IsPhoneNumberAdded)
        editor.commit()
    }
    fun putNomineeBank(IsNomineeBankAdded: Boolean) {
        editor.putBoolean("IsNomineeBankAdded",IsNomineeBankAdded)
        editor.commit()
    }
    fun putUserBank(IsUserBankAdded: Boolean) {
        editor.putBoolean("IsUserBankAdded",IsUserBankAdded)
        editor.commit()
    }

    fun putUserPhoto(IsUserBankAdded: Boolean) {
        editor.putBoolean("IsUserPhotoAdded",IsUserBankAdded)
        editor.commit()
    }
    fun putUserCnic(IsUserCnicAdded: Boolean) {
        editor.putBoolean("IsUserCnicAdded",IsUserCnicAdded)
        editor.commit()
    }
    fun putNomineeCnic(IsNomineeCnicAdded: Boolean) {
        editor.putBoolean("IsNomineeCnicAdded",IsNomineeCnicAdded)
        editor.commit()
    }



    /*fun saveNominee(nominee: ModelNominee) {

        editor.putString("cnic", nominee.cnic)
        editor.putString("firstName", nominee.firstName)
        editor.putString("lastName", nominee.lastName)
        editor.putString("address", nominee.address)
        editor.putString("phone", nominee.phone)
        editor.putString("cnic_front", nominee.cnic_front)
        editor.putString("cnic_back", nominee.cnic_back)
        editor.putString("acc_tittle", nominee.acc_tittle)
        editor.putString("acc_number", nominee.acc_number)
        editor.putString("bank_name", nominee.bank_name)
        editor.putString("nominator", nominee.nominator)
        editor.putString("nominator_relation", nominee.nominator_relation)
        editor.commit()
    }*/
/*
    fun saveInvestorAccount(investorAccountModel: InvestorAccountModel) {

        editor.putString("investorBankName", investorAccountModel.bankName)
        editor.putString("investorBankAccountNumber", investorAccountModel.accountNumber)
        editor.putString("investorBankAccountTitle", investorAccountModel.accountTitle)
        editor.commit()
    }


    fun getInvestorAccount(): InvestorAccountModel {

        return InvestorAccountModel(
            bankName = sharedPref.getString("investorBankName", "")!!,
            accountTitle = sharedPref.getString("investorBankAccountNumber", "")!!,
            accountNumber = sharedPref.getString("investorBankAccountTitle", "")!!
        )

    }*/

    /*fun getNominee(): InvestorNomineeModel {

        return InvestorNomineeModel(
            nomineeName = sharedPref.getString("nomineeName", "")!!,
            nomineeAddress = sharedPref.getString("nomineeAddress", "")!!,
            nomineeCNIC = sharedPref.getString("nomineeCNIC", "")!!,
            nomineeMobile = sharedPref.getString("nomineeMobile", "")!!

        )

    }*/

    fun getUser(): User {

        val json = sharedPref.getString("Investor", "") ?: ""
        return Gson().fromJson(json, User::class.java)

        /*return User(
            sharedPref.getString("cnic", "")!!,
            sharedPref.getString("firstName", "")!!,
            sharedPref.getString("lastName", "")!!,
            sharedPref.getString("address", "")!!,
            sharedPref.getString("phone", "")!!,
            sharedPref.getString("status", "")!!,
            sharedPref.getString("pin", "")!!

        )*/

    }







    fun putToken(docID: String) {
        editor.putString("docID", docID)
        editor.commit()
    }

    fun getToken(): String {
        return sharedPref.getString("docID", "")!!
    }
    fun getBalance(): String {
        return sharedPref.getString("balance", "0")!!
    }


    fun putCNIC(CNIC: String) {
        editor.putString("CNIC", CNIC)
        editor.commit()
    }

    fun getCNIC(): String {
        return sharedPref.getString("CNIC", "")!!
    }

    fun clear() {
        editor.clear()
        editor.commit()
    }

    fun setLogin(isLoggedIn: Boolean = true) {
        editor.putBoolean("isLoggedIn", isLoggedIn)
        editor.commit()
    }
    fun isLoggedIn(): Boolean {
        var isLoggedIn:Boolean=false
        if(sharedPref.getBoolean("isLoggedIn", false).equals(null)) isLoggedIn= false
        else if(sharedPref.getBoolean("isLoggedIn", false)==true) isLoggedIn =true
        return isLoggedIn
    }

    fun logOut(isLoggedOut: Boolean = false) {
        editor.putBoolean("isLoggedIn", isLoggedOut)
        editor.commit()
    }

    fun checkLogin(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false)
    }

    fun setAccountNumber(accountNumber: String) {
        editor.putString("accountNumber", accountNumber)
        editor.commit()
    }

    fun getAccountNumber(): String {
        return sharedPref.getString("accountNumber", "")!!
    }

    fun setTotalAmount(totalAmount: String) {
        editor.putString("totalAmount", totalAmount)
        editor.commit()
    }

    fun getTotalAmount(): String {
        return sharedPref.getString("totalAmount", "")!!
    }

}