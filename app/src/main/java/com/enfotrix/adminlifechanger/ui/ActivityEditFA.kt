package com.enfotrix.adminlifechanger.ui
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.enfotrix.adminlifechanger.Models.FAViewModel
import com.enfotrix.adminlifechanger.Models.ModelFA
import com.enfotrix.adminlifechanger.databinding.ActivityEditFaBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ActivityEditFA : AppCompatActivity() {

    private val faViewModel: FAViewModel by viewModels()


    private lateinit var binding: ActivityEditFaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditFaBinding.inflate(layoutInflater)
        setContentView(binding.root)
edit()

        binding.btnProfileedit.setOnClickListener {
            update()
            val intent = Intent(this@ActivityEditFA, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }

fun update()
{
    val modelFAStr = intent.getStringExtra("FA")
    val model: ModelFA? = modelFAStr?.let { ModelFA.fromString(it) }
        val editedFirstName = binding.etFirstName.editText?.text.toString()
        val editedLastName = binding.etLastName.editText?.text.toString()
        val editedDesignation = binding.etDesignation.editText?.text.toString()
        val editedCNIC = binding.etCNIC.editText?.text.toString()
        val editedpassword = binding.etpassword.editText?.text.toString()
        lifecycleScope.launch {
            val isSuccessLiveData = faViewModel.updateFADetails(
                model!!.id,
                editedFirstName,
                editedLastName,
                editedDesignation,
                editedCNIC,
                editedpassword
            )
            isSuccessLiveData.observe(this@ActivityEditFA) { isSuccess ->
                if (isSuccess) {
                    // FA details updated successfully
                    Toast.makeText(this@ActivityEditFA,editedFirstName +" FA details updated", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(this@ActivityEditFA, "Failed to update FA details", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    fun edit()
    {
        val modelFAStr = intent.getStringExtra("FA")
        val model: ModelFA? = modelFAStr?.let { ModelFA.fromString(it) }
        if (model != null) {
            binding.etFirstName.editText?.setText(model.firstName)
            binding.etLastName.editText?.setText(model.lastName)
            binding.etDesignation.editText?.setText(model.designantion)
            binding.etCNIC.editText?.setText(model.cnic)
        }
    }

}


