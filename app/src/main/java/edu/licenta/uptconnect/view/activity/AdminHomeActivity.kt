package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import edu.licenta.uptconnect.R
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
        binding.ctienCard.setOnClickListener {
            startActivityOnClick(binding.ctienText.text.toString())
        }
        binding.ctiCard.setOnClickListener {
            startActivityOnClick(binding.ctiText.text.toString())
        }
        binding.isCard.setOnClickListener {
            startActivityOnClick(binding.isText.text.toString())
        }
        binding.infoCard.setOnClickListener {
            startActivityOnClick(binding.infoText.text.toString())
        }
    }

    private fun startActivityOnClick(sectionText: String) {
        val intent = Intent(this, AdminIndividualSectionActivity::class.java)
        intent.putExtra("section", sectionText)
        startActivity(intent)
    }
}