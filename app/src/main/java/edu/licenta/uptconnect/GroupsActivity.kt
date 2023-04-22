package edu.licenta.uptconnect

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityGroupsBinding

class GroupsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityGroupsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
           1
        )
        getProfileDetails()
    }

    private fun setBinding() {
        binding = ActivityGroupsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun intializeButton() {

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
        val studentFirebaseId = FirebaseAuth.getInstance().currentUser?.uid
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