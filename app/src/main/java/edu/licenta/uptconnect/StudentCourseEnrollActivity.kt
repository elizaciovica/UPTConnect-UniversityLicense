package edu.licenta.uptconnect

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.adapter.EnrollCourseAdapter
import edu.licenta.uptconnect.databinding.ActivityStudentCourseEnrollBinding
import edu.licenta.uptconnect.model.CourseEnrollRequest
import edu.licenta.uptconnect.model.CourseEnrollRequestStatus

class StudentCourseEnrollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityStudentCourseEnrollBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        getProfileDetails()
        seeAvailableCourses()
    }

    private fun setBinding() {
        binding = ActivityStudentCourseEnrollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seeAvailableCourses() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewEnroll)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val studentsDatabase = Firebase.firestore
        val studentFirebaseId: String = intent.getStringExtra("userId").toString()
        val coursesRef = studentsDatabase.collection("courses")

        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    coursesRef.whereEqualTo("Section", documentSnapshot.getString("Section"))
                        .whereEqualTo("Year", documentSnapshot.getString("StudyYear"))
                        .whereEqualTo("Mandatory", false)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (documents.isEmpty) {
                                binding.progressBar.isVisible = false
                                binding.recyclerViewEnroll.isVisible = false
                                binding.viewForNoCoursesEnrolls.isVisible = true
                            } else {
                                val coursesList = mutableListOf<CourseEnrollRequest>()
                                for (document in documents) {
                                    val courseId = document.id
                                    val courseData = document.data
                                    val name = courseData["Name"] as String
                                    val section = courseData["Section"] as String
                                    val year = courseData["Year"] as String
                                    val mandatory = courseData["Mandatory"] as Boolean
                                    val examination = courseData["Examination"] as String
                                    val teachingWay = courseData["Teaching Way"]
                                    val course = CourseEnrollRequest(
                                        courseId,
                                        name,
                                        section,
                                        year,
                                        mandatory,
                                        examination,
                                        teachingWay!!,
                                        studentFirebaseId,
                                        documentSnapshot.getString("FirstName") + " " + documentSnapshot.getString("LastName"),
                                        CourseEnrollRequestStatus.INITIAL
                                    )
                                    coursesList.add(course)
                                }
                                val adapter = EnrollCourseAdapter(coursesList)
                                binding.recyclerViewEnroll.adapter = adapter
                                binding.recyclerViewEnroll.adapter?.notifyDataSetChanged()
                                binding.progressBar.isVisible = false
                                binding.recyclerViewEnroll.isVisible = true
                            }
                        }.addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error retrieving courses. ", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
                binding.progressBar.isVisible = false
                binding.recyclerViewEnroll.isVisible = false
                binding.viewForNoCoursesEnrolls.isVisible = true
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