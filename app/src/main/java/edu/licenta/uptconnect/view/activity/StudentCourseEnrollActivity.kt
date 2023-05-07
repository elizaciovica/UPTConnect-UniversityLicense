package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityStudentCourseEnrollBinding
import edu.licenta.uptconnect.model.CourseEnrollRequest
import edu.licenta.uptconnect.model.CourseEnrollRequestStatus
import edu.licenta.uptconnect.view.adapter.EnrollCourseAdapter

class StudentCourseEnrollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityStudentCourseEnrollBinding

    private var coursesList = mutableListOf<CourseEnrollRequest>()

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
        seeAvailableCourses()
    }

    private fun setBinding() {
        binding = ActivityStudentCourseEnrollBinding.inflate(layoutInflater)
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

    private fun seeAvailableCourses() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewEnroll)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val studentsDatabase = Firebase.firestore
        val coursesRef = studentsDatabase.collection("courses")
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId)

        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val acceptedCourses = documentSnapshot.get("acceptedCourses") as? List<String>
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
                                for (document in documents) {
                                    if (acceptedCourses != null && acceptedCourses.contains(document.id)) {
                                        Log.d(ContentValues.TAG, "Course already accepted")
                                    } else {
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
                                            documentSnapshot.getString("FirstName") + " " + documentSnapshot.getString(
                                                "LastName"
                                            ),
                                            CourseEnrollRequestStatus.INITIAL
                                        )
                                        coursesList.add(course)
                                    }
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
}