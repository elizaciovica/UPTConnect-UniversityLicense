package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityHomeBinding
import edu.licenta.uptconnect.databinding.ActivityPollsBinding
import edu.licenta.uptconnect.model.Course

class PollsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPollsBinding
    private var studentFirebaseId = ""
    private var email = ""

    private lateinit var questionTextView: TextView
    private lateinit var optionsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getProfileDetails()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityPollsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        val course = intent.getParcelableExtra<Course>("course")!!

        binding.createPoll.setOnClickListener() {
            val intent = Intent(this, CreatePollActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

//    private fun getAvailablePolls() {
//        binding.questionTextview =
//    }
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

    //todo retrieve all the available polls


}