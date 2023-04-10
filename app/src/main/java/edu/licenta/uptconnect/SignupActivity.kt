package edu.licenta.uptconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import edu.licenta.uptconnect.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.loginButton.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signupButton.setOnClickListener() {
            Toast.makeText(
                this,
                "To be implemented",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}