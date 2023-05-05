package edu.licenta.uptconnect

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import edu.licenta.uptconnect.databinding.ActivityAdminHomeBinding

class AdminHomeActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityAdminHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()

        val navigationView = binding.navigationView
        val drawerLayout = binding.drawerLayout
        initializeMenu(
            drawerLayout,
            navigationView,
            0
        )

        navigationView.menu.findItem(R.id.home).isVisible = false
        navigationView.menu.findItem(R.id.edit_profile).isVisible = false
        navigationView.menu.findItem(R.id.my_colleagues).isVisible = false
        navigationView.menu.findItem(R.id.my_courses).isVisible = false

        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.header_text).text = "ADMIN"

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
            startActivity(intent)
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