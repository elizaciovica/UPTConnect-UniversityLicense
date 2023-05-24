package edu.licenta.uptconnect.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.loginButton.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signupButton.setOnClickListener() {
            registerAction()
        }
    }

    private fun registerAction() {
        val password = binding.password.text.toString().trim { it <= ' ' }
        val email = binding.email.text.toString().trim { it <= ' ' }

        when {
            TextUtils.isEmpty(email) -> {
                Toast.makeText(
                    this,
                    "Please enter email.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            TextUtils.isEmpty(password) -> {
                Toast.makeText(
                    this,
                    "Please enter password.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {
                if (email.endsWith("@student.upt.ro")) {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                Toast.makeText(
                                    this,
                                    "Account successfully created",
                                    Toast.LENGTH_SHORT
                                ).show()
                                createUserFirestoreDetails(firebaseUser.uid, email)
                                startCompleteProfile(firebaseUser, email)
                            } else {
                                Toast.makeText(
                                    this,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        this,
                        "Please enter a valid student mail.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createUserFirestoreDetails(studentUid: String, studentEmail: String) {
        val studentsDatabase = Firebase.firestore

        val student = hashMapOf(
            "Uid" to studentUid,
            "Email" to studentEmail,
            "FirstName" to null,
            "LastName" to null,
            "Faculty" to null,
            "Section" to null,
            "StudyYear" to null,
            "IsAdmin" to null,
            "acceptedCourses" to emptyList<String>()
        )

        studentsDatabase.collection("students").document(studentUid)
            .set(student, SetOptions.merge())
            .addOnSuccessListener { Log.d(TAG, "Student Document successfully created!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error creating Student Document", e) }
    }

    private fun startCompleteProfile(firebaseUser: FirebaseUser, email: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId", firebaseUser.uid)
        intent.putExtra("email", email)
        startActivity(intent)
        finish()
    }
}