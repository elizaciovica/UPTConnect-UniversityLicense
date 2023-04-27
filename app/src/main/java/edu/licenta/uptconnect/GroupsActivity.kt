package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class GroupsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityGroupsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
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
        //for adapter
        val studentsDatabase = Firebase.firestore
        val studentFirebaseId: String = intent.getStringExtra("userId").toString()
        val coursesRef = studentsDatabase.collection("courses")
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    coursesRef.whereEqualTo("Section", documentSnapshot.getString("Section"))
                        .whereEqualTo("Year", documentSnapshot.getString("StudyYear"))
                        .whereEqualTo("Mandatory", true)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                binding.progressBar.isVisible = false
                                binding.recyclerView.isVisible = false
                                binding.viewForNoCourses.isVisible = true
                            } else {
                                val coursesList = mutableListOf<Course>()
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
                                binding.progressBar.isVisible = false
                                binding.recyclerView.isVisible = true
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
        val email: String = intent.getStringExtra("email").toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        println("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageURL = uri.toString()
            Picasso.get().load(imageURL).into(binding.profileImage)
        }

        val studentsDatabase = Firebase.firestore
        val studentFirebaseId: String = intent.getStringExtra("userId").toString()
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