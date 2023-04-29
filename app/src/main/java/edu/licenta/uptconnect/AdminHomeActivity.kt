package edu.licenta.uptconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import edu.licenta.uptconnect.databinding.ActivityAdminHomeBinding

class AdminHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityAdminHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.ctienCard.setOnClickListener() {
            val intent = Intent(this, AdminEnrollRequestsActivity::class.java)
            intent.putExtra("sectionName", "CTI-ENG")
            startActivity(intent)
            finish()
        }
        binding.ctiCard.setOnClickListener() {
            Toast.makeText(
                this, "To be implemented", Toast.LENGTH_SHORT
            ).show()
        }
        binding.isCard.setOnClickListener() {
            Toast.makeText(
                this, "To be implemented", Toast.LENGTH_SHORT
            ).show()
        }
        binding.infoCard.setOnClickListener() {
            Toast.makeText(
                this, "To be implemented", Toast.LENGTH_SHORT
            ).show()
        }
    }
}