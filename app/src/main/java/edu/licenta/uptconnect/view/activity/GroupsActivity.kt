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
import edu.licenta.uptconnect.databinding.ActivityGroupsBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.view.adapter.MandatoryCourseAdapter

class GroupsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityGroupsBinding

    private var coursesList = mutableListOf<Course>()

    private var studentFirebaseId: String = ""
    private var email: String = ""
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
        seeMandatoryCourses()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityGroupsBinding.inflate(layoutInflater)
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

    private fun initializeButtons() {
        binding.enrollButton.setOnClickListener {
            val intent = Intent(this, StudentCourseEnrollActivity::class.java)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("email", email)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }
    }

    private fun seeMandatoryCourses() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val studentsDatabase = Firebase.firestore
        val coursesRef = studentsDatabase.collection("courses")
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId)

        //for the accepted courses
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val acceptedCourses = documentSnapshot.get("acceptedCourses") as? List<String>
                    coursesRef
                        .get()
                        .addOnSuccessListener { documents ->
                            if (acceptedCourses != null) {
                                if (documents.isEmpty || acceptedCourses.isEmpty()) {
                                    binding.progressBar.visibility = View.GONE
                                    binding.recyclerView.visibility = View.GONE
                                    binding.viewForNoCourses.isVisible = true
                                } else {
                                    for (document in documents) {
                                        if (acceptedCourses.contains(document.id)) {
                                            val courseId = document.id
                                            val courseData = document.data
                                            val name = courseData["Name"] as String
                                            val section = courseData["Section"] as String
                                            val year = courseData["Year"] as String
                                            val mandatory = courseData["Mandatory"] as Boolean
                                            val examination = courseData["Examination"] as String
                                            val teachingWay = courseData["Teaching Way"]
                                            val course = Course(
                                                courseId,
                                                name,
                                                section,
                                                year,
                                                mandatory,
                                                examination,
                                                teachingWay!!
                                            )
                                            coursesList.add(course)
                                        }
                                        val adapter = MandatoryCourseAdapter(coursesList)
                                        binding.recyclerView.adapter = adapter
                                        binding.recyclerView.adapter?.notifyDataSetChanged()
                                        binding.progressBar.visibility = View.GONE
                                        binding.viewForNoCourses.visibility = View.GONE
                                        binding.recyclerView.isVisible = true

                                        adapter.onItemClick = {
                                            val intent =
                                                Intent(this, IndividualGroupActivity::class.java)
                                            intent.putExtra("userId", studentFirebaseId)
                                            intent.putExtra("email", email)
                                            intent.putExtra("course", it)
                                            intent.putExtra("imageUrl", imageUrl)
                                            intent.putExtra("studentName", studentName)
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error retrieving courses. ", exception)
                        }
                }
            }
    }
}