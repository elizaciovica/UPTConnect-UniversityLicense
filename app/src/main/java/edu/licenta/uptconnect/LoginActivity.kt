package edu.licenta.uptconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import edu.licenta.uptconnect.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.loginButton.setOnClickListener() {
            Toast.makeText(
                this,
                "To be implemented",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.signupButton.setOnClickListener() {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}