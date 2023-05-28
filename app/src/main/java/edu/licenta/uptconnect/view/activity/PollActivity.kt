package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
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
    private var pollAdapter: FirestoreRecyclerAdapter<Poll, PollAdapter.PollViewHolder>? = null

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
        val pollQuery =
            Firebase.firestore.collection("polls")
                .document("courses_polls")
                .collection(course.id)

        pollQuery.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val pollList = FirestoreRecyclerOptions.Builder<Poll>()
                        .setQuery(pollQuery, Poll::class.java).build()

                    pollAdapter = PollAdapter(pollList)
                    binding.pollsRecyclerView.adapter = pollAdapter
                    binding.pollsRecyclerView.adapter?.notifyDataSetChanged()

                    pollAdapter!!.startListening()

                    binding.progressBar.visibility = View.GONE
                    binding.viewForNoPolls.visibility = View.GONE
                    binding.pollsRecyclerView.visibility = View.VISIBLE

                    (pollAdapter as PollAdapter).onItemClick = {
                        val intent = Intent(this, IndividualPollActivity::class.java)
                        intent.putExtra("userId", studentFirebaseId)
                        intent.putExtra("email", email)
                        intent.putExtra("poll", it)
                        intent.putExtra("course", course)
                        intent.putExtra("imageUrl", imageUrl)
                        intent.putExtra("studentName", studentName)
                        startActivity(intent)
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.pollsRecyclerView.visibility = View.GONE
                    binding.viewForNoPolls.visibility = View.VISIBLE
                }

            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving poll", exception)
            }
    }

    override fun onStop() {
        super.onStop()
        if (pollAdapter != null) {
            pollAdapter!!.stopListening()
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