package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityAdminIndividualSectionBinding

class AdminIndividualSectionActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityAdminIndividualSectionBinding

    private var section = ""

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
        binding = ActivityAdminIndividualSectionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        section = intent.getStringExtra("section").toString()
        binding.enrollRequests.setOnClickListener {
            val intent = Intent(this, AdminEnrollRequestsActivity::class.java)
            intent.putExtra("section", section)
            startActivity(intent)
        }
        binding.createSchedule.setOnClickListener() {
            val intent = Intent(this, CreateScheduleAdminActivity::class.java)
            intent.putExtra("section", section)
            startActivity(intent)
        }
    }
}