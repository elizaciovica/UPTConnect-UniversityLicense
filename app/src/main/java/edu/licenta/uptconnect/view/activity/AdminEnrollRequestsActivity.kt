package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityAdminenrollrequestsBinding
import edu.licenta.uptconnect.model.EnrollRequest

class AdminEnrollRequestsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityAdminenrollrequestsBinding
    private lateinit var requestsAdapter: FirestoreRecyclerAdapter<EnrollRequest, AdminEnrollRequestsViewHolder>
    private lateinit var linearLayoutManager: LinearLayoutManager

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

        seeAllRequests()
    }

    private fun setBinding() {
        binding = ActivityAdminenrollrequestsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seeAllRequests() {
        val requestsDatabase = Firebase.firestore
        val requestsDocQuery = requestsDatabase.collection("courseEnrollRequests")
            .whereEqualTo("courseEnrollRequestStatus", "SENT")
        val allRequests = FirestoreRecyclerOptions.Builder<EnrollRequest>()
            .setQuery(requestsDocQuery, EnrollRequest::class.java).build()

        requestsAdapter = AdminEnrollRequestsFirestoreAdapter(allRequests)
        linearLayoutManager = LinearLayoutManager(this)
        binding.recyclerViewRequests.layoutManager = linearLayoutManager
        binding.recyclerViewRequests.adapter = requestsAdapter
        binding.recyclerViewRequests.adapter?.notifyDataSetChanged()
    }

    private inner class AdminEnrollRequestsViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
        fun setRequestDetails(courseNameText: String, studentNameText: String) {
            val courseName = itemView.findViewById<TextView>(R.id.name_request_course)
            val studentName = itemView.findViewById<TextView>(R.id.name_request_student)
            courseName.text = courseNameText
            studentName.text = studentNameText
        }
    }

    private inner class AdminEnrollRequestsFirestoreAdapter(options: FirestoreRecyclerOptions<EnrollRequest>) :
        FirestoreRecyclerAdapter<EnrollRequest, AdminEnrollRequestsViewHolder>(options) {

        override fun onBindViewHolder(
            requestViewHolder: AdminEnrollRequestsViewHolder,
            position: Int,
            request: EnrollRequest
        ) {
            requestViewHolder.setRequestDetails(request.courseName, request.studentName)

            val docId = requestsAdapter.snapshots.getSnapshot(position).id
            val requestsDatabase = Firebase.firestore
            val declineButton = requestViewHolder.itemView.findViewById<Button>(R.id.decline)

            declineButton.setOnClickListener() {
                requestsDatabase.collection("courseEnrollRequests").document(docId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            applicationContext,
                            "The request has been rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            applicationContext,
                            "Failed to delete request",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            val acceptButton = requestViewHolder.itemView.findViewById<Button>(R.id.accept)
            acceptButton.setOnClickListener() {
                val studentsDatabase = Firebase.firestore
                val studentDoc = studentsDatabase.collection("students").document(request.studentId)
                var acceptedCourseList = listOf<String>()
                acceptedCourseList += docId
                studentDoc.update(
                    "acceptedCourses",
                    FieldValue.arrayUnion(*acceptedCourseList.toTypedArray())
                )
                FirebaseMessaging.getInstance().subscribeToTopic(docId)

                //also delete the request from the request collection
                requestsDatabase.collection("courseEnrollRequests").document(docId)
                    .delete()
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error deleting request", exception)
                    }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): AdminEnrollRequestsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_enroll_approve, parent, false)
            return AdminEnrollRequestsViewHolder(view)
        }
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
