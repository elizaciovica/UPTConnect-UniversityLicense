package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.adapter.PollAdapter
import edu.licenta.uptconnect.databinding.ActivityPollBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.Poll

class PollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityPollBinding
    private var studentFirebaseId = ""
    private var email = ""
    private lateinit var course: Course
    private var pollList = mutableListOf<Poll>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        getProfileDetails()
        initializeButtons()
        seeAvailablePolls()
    }

    private fun setBinding() {
        binding = ActivityPollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seeAvailablePolls() {
        val recyclerView = findViewById<RecyclerView>(R.id.polls_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val pollsDatabase = Firebase.firestore
        pollsDatabase.collection("polls").document("courses_polls").collection(course.id)
            .get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.progressBar.visibility = View.GONE
                    binding.pollsRecyclerView.visibility = View.GONE
                    binding.viewForNoPolls.isVisible = true
                } else {
                    for (document in documents) {
                        val pollId = document.id
                        val pollData = document.data
                        val createdBy = pollData["createdBy"] as String
                        val endTime = pollData["end_time"] as String
                        val startTime = pollData["start_time"] as String
                        val question = pollData["question"] as String
                        val options = pollData["options"] as List<String>
                        val poll = Poll(pollId, createdBy, endTime, startTime, question, options)
                        pollList.add(poll)
                    }
                    val adapter = PollAdapter(pollList)
                    binding.pollsRecyclerView.adapter = adapter
                    binding.pollsRecyclerView.adapter?.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                    binding.viewForNoPolls.visibility = View.GONE
                    binding.pollsRecyclerView.isVisible = true

                    adapter.onItemClick = {
                        val intent = Intent(this, IndividualPollActivity::class.java)
                        intent.putExtra("userId", studentFirebaseId)
                        intent.putExtra("email", email)
                        intent.putExtra("poll", it)
                        intent.putExtra("course", course)
                        startActivity(intent)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving polls. ", exception)
            }
    }

    private fun initializeButtons() {
        course = intent.getParcelableExtra("course")!!

        binding.createPoll.setOnClickListener {
            val intent = Intent(this, CreatePollActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            startActivity(intent)
        }
    }

    private fun getProfileDetails() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageURL = uri.toString()
            Picasso.get().load(imageURL).into(binding.profileImage)
        }

        val studentsDatabase = Firebase.firestore
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    binding.usernameId.text =
                        documentSnapshot.getString("FirstName") + documentSnapshot.getString("LastName")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
    }
}