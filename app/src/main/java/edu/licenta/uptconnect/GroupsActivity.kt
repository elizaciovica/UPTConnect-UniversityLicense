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
import edu.licenta.uptconnect.adapter.EnrollCourseAdapter
import edu.licenta.uptconnect.adapter.MandatoryCourseAdapter
import edu.licenta.uptconnect.databinding.ActivityGroupsBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.CourseEnrollRequest
import edu.licenta.uptconnect.model.CourseEnrollRequestStatus

class GroupsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityGroupsBinding
    private var coursesList = mutableListOf<Course>()
    private var studentFirebaseId = ""
    private var email = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // your code here

        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        getProfileDetails()
        seeMandatoryCourses()
        initializeButtons()
        } catch (e: Exception) {
            Log.e("GroupsActivity", "Error in onCreate", e)
        }
    }

    private fun setBinding() {
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.enrollButton.setOnClickListener() {
            val email: String = intent.getStringExtra("email").toString()
            val firebaseUser: String = intent.getStringExtra("userId").toString()
            val intent = Intent(this, StudentCourseEnrollActivity::class.java)
            intent.putExtra("userId", firebaseUser)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

    private fun seeMandatoryCourses() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val studentsDatabase = Firebase.firestore
        val coursesRef = studentsDatabase.collection("courses")
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)

        //for the accepted courses
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val acceptedCourses = documentSnapshot.get("acceptedCourses") as? List<String>
                    coursesRef
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                binding.progressBar.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.viewForNoCourses.isVisible = true
                            } else {
                                for (document in documents) {
                                    if (acceptedCourses != null && acceptedCourses.contains(document.id)) {
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
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error retrieving courses. ", exception)
                        }
                }
            }

        //for the mandatory courses
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    coursesRef.whereEqualTo("Section", documentSnapshot.getString("Section"))
                        .whereEqualTo("Year", documentSnapshot.getString("StudyYear"))
                        .whereEqualTo("Mandatory", true)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                binding.progressBar.visibility = View.GONE
                                binding.recyclerView.visibility = View.GONE
                                binding.viewForNoCourses.isVisible = true
                            } else {
                                for (document in documents) {
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
                                    val intent = Intent(this, IndividualGroupActivity::class.java)
                                    intent.putExtra("userId", studentFirebaseId)
                                    intent.putExtra("email", email)
                                    intent.putExtra("course", it)
                                    startActivity(intent)
                                }
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error retrieving courses. ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
    }

    private fun getProfileDetails() {
        email = intent.getStringExtra("email").toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageURL = uri.toString()
            Picasso.get().load(imageURL).into(binding.profileImage)
        }

        val studentsDatabase = Firebase.firestore
        studentFirebaseId = intent.getStringExtra("userId").toString()
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)
        // -> is a lambda consumer - based on its parameter - i need a listener to wait for the database call
        // ex .get() - documentSnapshot is like a response body
        //binding the dynamic linking for the xml component and the content
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