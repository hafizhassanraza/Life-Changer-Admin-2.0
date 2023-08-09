package com.enfotrix.lifechanger

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.enfotrix.adminlifechanger.R
import com.enfotrix.lifechanger.Models.ModelNominee
import java.text.SimpleDateFormat
import java.util.Locale

class Utils(val context: Context) {

    //lateinit var context: Context
     private var dialog= Dialog(context)




    /*constructor(context: Context) {
        this.context = context
        dialog = Dialog(context)

    }*/


    fun startLoadingAnimation() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_loading)
        dialog.setCancelable(false)
        dialog.show()
    }

    fun endLoadingAnimation() {
        dialog.dismiss()
    }

    fun cnicFormate(cnic :String):String{
        //return (cnic.toCharArray().also { it[4] = '-' }).toCharArray().also { it[12] = '-' }.joinToString("")
        return buildString {
            append(cnic.toString().substring(0, 5))
            append('-')
            append(cnic.toString().substring(5, 12))
            append('-')
            append(cnic.toString().substring(12,13))
        }

        //38403-309353-07
    }
    fun moveFocus(editTextList: List<EditText>) {


        for (i in 0 until editTextList.size-1) {

            val currentEditText = editTextList[i]
            val nextEditText = editTextList[i + 1]

            currentEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Not needed in this case
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Check if the desired condition is met, e.g., when the length of the current EditText is 1
                    if (currentEditText.text.length == 1 ) {
                        nextEditText.requestFocus() // Set focus on the next EditText

                    }


                }

                override fun afterTextChanged(s: Editable?) {
                    // Not needed in this case
                }
            })
        }
    }



   /* fun dateFormat():String{

        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        return dateFormat.format().toString()

    }*/
    fun clearAll(editTextList: List<EditText>) {
        for (i in 0 until editTextList.size ) {
            editTextList[i].text.clear()
        }
    }
    fun checkEmpty(editTextList: List<EditText>): Boolean {
        var empty:Boolean
        empty=false
        for (i in 0 until editTextList.size ) {
            if(editTextList[i].text.isEmpty())empty=true
        }
        return empty
    }
    fun getPIN(editTextList: List<EditText>): String {
        var pin:String=""
        for (i in 0 until editTextList.size ) pin = pin+ editTextList[i].text.toString()
        return pin
    }

    fun enableDisableButton(button: Button, enable: Boolean) {


       /* if(enable){
            button.isEnabled = true
            button.isClickable = true
            button.setTextColor(ContextCompat.getColor(context, R.color.white))
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.primary))
        }
        else {
            button.isEnabled = false
            button.isClickable = false
            button.setTextColor(ContextCompat.getColor(context, R.color.black_m))
            button.setBackgroundColor(ContextCompat.getColor(context, R.color.gray))
        }*/


    }




}