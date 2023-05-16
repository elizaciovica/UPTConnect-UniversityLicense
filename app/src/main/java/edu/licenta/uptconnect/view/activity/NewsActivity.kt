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
import edu.licenta.uptconnect.databinding.ActivityNewsBinding
import edu.licenta.uptconnect.model.New
import edu.licenta.uptconnect.view.adapter.NewsAdapter

class NewsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityNewsBinding

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""
    private var courses = mutableListOf<String>()
    private var isLeader = false
    private var newsAdapter: FirestoreRecyclerAdapter<New, NewsAdapter.NewsViewHolder>? = null

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
        seeAvailableNews()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityNewsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun initializeButtons() {
        binding.createNews.setOnClickListener {
            val intent = Intent(this, CreateNewsActivity::class.java)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("email", email)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            intent.putExtra("coursesIds", courses.toTypedArray())
            intent.putExtra("isLeader", isLeader)
            startActivity(intent)
        }
    }

    private fun seeAvailableNews() {

        val getStudentCoursesTask = Firebase.firestore
            .collection("students")
            .document(studentFirebaseId)
            .get()
        getStudentCoursesTask.addOnSuccessListener { task ->
            courses = task.get("acceptedCourses") as MutableList<String>
            if (task.get("YearLeader") == null) {
                isLeader = false
            } else {
                isLeader = task.get("YearLeader") as Boolean
            }
            binding.viewForNoNews.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.newsRecyclerView.visibility = View.VISIBLE

            val recyclerView = findViewById<RecyclerView>(R.id.news_recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(this)
            val linearLayoutManager = LinearLayoutManager(this)
            binding.newsRecyclerView.layoutManager = linearLayoutManager

            val newsQuery =
                Firebase.firestore.collection("news")
                    .whereIn("courseId", courses.toList())
            val allNews = FirestoreRecyclerOptions.Builder<New>()
                .setQuery(newsQuery, New::class.java).build()

            newsAdapter = NewsAdapter(allNews)
            binding.newsRecyclerView.adapter = newsAdapter
            binding.newsRecyclerView.adapter?.notifyDataSetChanged()

            newsAdapter!!.startListening()
        }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving student details", exception)
            }
    }

    override fun onStop() {
        super.onStop()
        if (newsAdapter != null) {
            newsAdapter!!.stopListening()
        }
    }
}
