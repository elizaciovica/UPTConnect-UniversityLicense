package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityIndividualGroupBinding
import edu.licenta.uptconnect.databinding.ActivityStudentCourseEnrollBinding
import edu.licenta.uptconnect.model.Course

class IndividualGroupActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityIndividualGroupBinding
    private var studentFirebaseId = ""
    private var email = ""

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
    }

    private fun setBinding() {
        binding = ActivityIndividualGroupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        val course = intent.getParcelableExtra<Course>("course")!!
        binding.chatCard.setOnClickListener() {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            startActivity(intent)
        }

        binding.pollsCard.setOnClickListener() {
            val intent = Intent(this, PollActivity::class.java)
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