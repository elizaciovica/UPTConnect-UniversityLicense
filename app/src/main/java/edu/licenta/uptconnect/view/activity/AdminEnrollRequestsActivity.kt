package edu.licenta.uptconnect.view.activity

import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityAdminenrollrequestsBinding
import edu.licenta.uptconnect.model.EnrollRequest
import edu.licenta.uptconnect.view.adapter.AdminEnrollRequestsAdapter

class AdminEnrollRequestsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityAdminenrollrequestsBinding
    private lateinit var requestsAdapter: FirestoreRecyclerAdapter<EnrollRequest, AdminEnrollRequestsAdapter.AdminEnrollRequestsViewHolder>
    private lateinit var linearLayoutManager: LinearLayoutManager

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

        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.header_text).text = "ADMIN"

        seeAllRequests()
    }

    private fun setBinding() {
        binding = ActivityAdminenrollrequestsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seeAllRequests() {
        section = intent.getStringExtra("section").toString()
        val requestsDatabase = Firebase.firestore
        val requestsDocQuery =
            requestsDatabase.collection("courseEnrollRequests").document("sections")
                .collection(section)
                .whereEqualTo("courseEnrollRequestStatus", "SENT")

        val allRequests = FirestoreRecyclerOptions.Builder<EnrollRequest>()
            .setQuery(requestsDocQuery, EnrollRequest::class.java).build()

        requestsAdapter = AdminEnrollRequestsAdapter(allRequests)
        linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewRequests.layoutManager = linearLayoutManager
        binding.recyclerViewRequests.adapter = requestsAdapter
        binding.recyclerViewRequests.adapter?.notifyDataSetChanged()
    }

    override fun onStart() {
        super.onStart()
        requestsAdapter!!.startListening()
    }

    override fun onStop() {
        super.onStop()
        if (requestsAdapter != null) {
            requestsAdapter!!.stopListening()
        }
    }
}
