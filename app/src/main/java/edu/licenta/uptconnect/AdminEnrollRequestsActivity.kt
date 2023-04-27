package edu.licenta.uptconnect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.adapter.AdminEnrollRequestsAdapter
import edu.licenta.uptconnect.adapter.EnrollCourseAdapter
import edu.licenta.uptconnect.databinding.ActivityAdminenrollrequestsBinding
import edu.licenta.uptconnect.model.EnrollRequest

class AdminEnrollRequestsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminenrollrequestsBinding
    private var requestsLiveData = MutableLiveData<List<EnrollRequest>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        seeAllRequests()
    }

    private fun setBinding() {
        binding = ActivityAdminenrollrequestsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seeAllRequests() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRequests)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val requestsDatabase = Firebase.firestore

        val requestsDoc = requestsDatabase.collection("courseEnrollRequests")
        requestsDoc.get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.progressBar.isVisible = false
                    binding.recyclerViewRequests.isVisible = false
                    binding.viewForNoRequests.isVisible = true
                } else {
                    val requestsList = mutableListOf<EnrollRequest>()
                    for (document in result) {
                        val requestId = document.id
                        val requestData = document.data
                        val courseId = requestData["courseId"] as String
                        val courseEnrollRequestStatus =
                            requestData["courseEnrollRequestStatus"] as String
                        val studentId = requestData["studentId"] as String
                        val course = EnrollRequest(
                            requestId,
                            courseId,
                            courseEnrollRequestStatus,
                            studentId
                        )
                        requestsList.add(course)
                    }
                    requestsLiveData.postValue(requestsList)
                    val adapter = AdminEnrollRequestsAdapter(requestsList)
                    binding.recyclerViewRequests.adapter = adapter
                    adapter.notifyDataSetChanged()

                    observeClientLiveData().observe(this) {
                        requestsList ->
                        if(requestsList.isNotEmpty()) {
                            binding.progressBar.isVisible = false
                            binding.recyclerViewRequests.isVisible = true

                        } else {
                            binding.progressBar.isVisible = false
                            binding.recyclerViewRequests.isVisible = false
                            binding.viewForNoRequests.isVisible = true
                        }
                    }
                }

            }
    }

    private fun observeClientLiveData(): LiveData<List<EnrollRequest>> {
        return requestsLiveData
    }
}
