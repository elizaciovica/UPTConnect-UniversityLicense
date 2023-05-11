package edu.licenta.uptconnect.view.activity

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
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityPollBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.Poll
import edu.licenta.uptconnect.view.adapter.PollAdapter

class PollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityPollBinding

    private lateinit var course: Course
    private var pollList = mutableListOf<Poll>()

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getExtrasFromIntent()
        setProfileDetails()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        initializeButtons()
        seeAvailablePolls()
    }

    private fun setBinding() {
        binding = ActivityPollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
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
                        val isFromLeader = pollData["isFromLeader"] as Boolean
                        val hasResults = pollData["hasResults"] as Boolean
                        val poll = Poll(pollId, createdBy, endTime, startTime, question, options, isFromLeader, hasResults)
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
                        intent.putExtra("imageUrl", imageUrl)
                        intent.putExtra("studentName", studentName)
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
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }
    }
}