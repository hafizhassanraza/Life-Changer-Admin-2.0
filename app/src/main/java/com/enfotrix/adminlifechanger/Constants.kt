package com.enfotrix.adminlifechanger

public class Constants  {



    ///////////////////////////// COLLECTIONS NAME //////////////////////////

    public var AGENT_EARNING_COLLECTION="Agent Transaction"
    public var INVESTOR_COLLECTION="Investors"
    public var FA_COLLECTION="Financial Advisor"
    public var AGENT_TRANSACTION="Agent Transaction"
    public var NOMINEE_COLLECTION="Nominees"
    public var ACCOUNTS_COLLECTION="Accounts"
    public var ANNOUNCEMENT_COLLECTION="Accounts"
    public var INVESTMENT_COLLECTION="Investment"
    public var TRANSACTION_REQ_COLLECTION="Transactions"
    public var PROFIT_TAX_COLLECTION="ProfitTax"
    public var WITHDRAW_COLLECTION="Agent Withdraw"
    public var NOTIFICATION_COLLECTION="Notification"



    ///////////////////////////  KEYS  ///////////////////
    public var INVESTOR_CNIC= "cnic"
    public var INVESTOR_PIN= "pin"
    public var ACCOUNT_HOLDER= "account_holder"
    public var INVESTOR_ID= "investorID"
    public var type= "investorID"
    public var TRANSACTION_TYPE= "type" // Withdraw , Investment
    public var TRANSACTION_STATUS= "status" // Pending , Approved


    ///////////////////////////  LOCAL KEYS  ///////////////////
    public var KEY_ACTIVITY_FLOW= "flow"

    ///////////////////////////  LOCAL KEYS Values ///////////////////
    public var VALUE_ACTIVITY_FLOW_USER_DETAILS= "from_user_details"
    public var VALUE_DIALOG_FLOW_INVESTOR_BANK= "from_investor"
    public var VALUE_DIALOG_FLOW_INVESTOR_CNIC= "from_investor"
    public var VALUE_DIALOG_FLOW_NOMINEE_BANK= "from_nominee"
    public var VALUE_DIALOG_FLOW_NOMINEE_CNIC= "from_nominee"

    public var VALUE_DIALOG_FLOW_INVESTOR= "from_investor"
    public var VALUE_DIALOG_FLOW_ADMIN= "from_admin"



    //////////////////////////// KEYS VALUES ////////////////////////////////
    public var ADMIN= "Admin"
    public var TRANSACTION_STATUS_PENDING= "Pending"
    public var EARNING_STATUS_PENDING= "Pending"
    public var TRANSACTION_STATUS_APPROVED= "Approved"
    public var EARNING_STATUS_WITHDRAW= "Withdraw"
    public var TRANSACTION_STATUS_REJECT= "Reject"
    public var TRANSACTION_TYPE_WITHDRAW= "Withdraw"
    public var TRANSACTION_TYPE_Profit= "Profit"
    public var TRANSACTION_TYPE_Tax= "Tax"
    public var TRANSACTION_TYPE_INVESTMENT= "Investment"
    public var PROFIT_TYPE= "Profit"
    public var TAX_TYPE= "Tax"
    public var INVESTOR_STATUS_ACTIVE= "Active"
    public var INVESTOR_STATUS_PENDING= "Pending"
    public var INVESTOR_STATUS_INCOMPLETE= "Incomplete"
    public var INVESTOR_STATUS_BLOCKED= "Blocked"
    public var TRANSACTION_TYPE_TAX= "Tax"
    public var TRANSACTION_TYPE_PROFIT= "Profit"



    ///////////////////////////  MESSAGES ///////////////////////
    public var INVESTOR_LOGIN_MESSAGE= "User login successfully!"
    public var INVESTOR_LOGIN_FAILURE_MESSAGE= "Incorrect PIN!"

    public var NOMINEE_SIGNUP_MESSAGE= "Nominee added successfully!"
    public var INVESTOR_SIGNUP_MESSAGE= "User registered successfully!"
    public var FA_SIGNUP_MESSAGE= "Financial Adv. registered successfully!"
    public var INVESTOR_SIGNUP_FAILURE_MESSAGE= "Something went wrong!"
    public var SOMETHING_WENT_WRONG_MESSAGE= "Something went wrong!"

    public var ACCOPUNT_ADDED_MESSAGE= "Account Added Successfully!"


    public var INVESTOR_CNIC_EXIST= "User(CNIC) already exist!"
    public var INVESTOR_CNIC_NOT_EXIST= "User(CNIC) not exist!"
    public var INVESTOR_CNIC_BLOCKED= "User(CNIC) Blocked!"
    public var AGENT_NOTIFICATIONS_TITLE= "Earning"

    ///////////////////////////// Activities/Fragment Flow //////////////////////////

    public var FROM_PROFIT= "ActivityProfit"
    public var FROM_TAX= "ActivityTax"

    public var FROM_FA= "ActivityFA"
    public var FROM_INVESTOR_ACCOUNTS= "ActivityInvestorAccounts"
    public var FROM_ASSIGNED_FA= "Assigned"
    public var FROM_UN_ASSIGNED_FA= "UnAssigned"
    public var FROM_NEW_WITHDRAW_REQ= "ActivityNewWithdrawReq"
    public var FROM_NEW_INVESTMENT_REQ= "ActivityNewInvestmentReq"
    public var FROM_PENDING_WITHDRAW_REQ= "FragmentPendingWithdrawReq"
    public var FROM_PENDING_INVESTMENT_REQ= "FragmentPendingInvestmentReq"
    public var FROM_PENDING_INVESTOR_REQ= "ActivityPendingInvestorReq"
    public var FROM_APPROVED_WITHDRAW_REQ= "FragmentApprovedWithdrawReq"
    public var FROM_APPROVED_INVESTMENT_REQ= "FragmentApprovedInvestmentReq"




}