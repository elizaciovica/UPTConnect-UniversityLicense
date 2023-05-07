package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityHomeBinding

class HomeActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val screenId: Int = 1

    private var studentFirebaseId: String = ""
    private var email: String = ""
    private var imageUrl: String = ""
    private var studentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getExtrasFromIntent()
        getProfileDetails()

        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            screenId
        )
        initializeButtons()
    }

    private fun getProfileDetails() {
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            imageUrl = uri.toString()
            Picasso.get().load(imageUrl).into(binding.profileImage)
        }

        val studentsDatabase = Firebase.firestore
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId)
        // -> is a lambda consumer - based on its parameter - i need a listener to wait for the database call
        // ex .get() - documentSnapshot is like a response body
        //binding the dynamic linking for the xml component and the content
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    studentName =
                        "${documentSnapshot.get("FirstName")} ${documentSnapshot.get("LastName")}"
                    binding.usernameId.text = studentName
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
    }

    private fun setBinding() {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.groupsCard.setOnClickListener {
            val intent = Intent(this, GroupsActivity::class.java)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("email", email)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }
    }
}