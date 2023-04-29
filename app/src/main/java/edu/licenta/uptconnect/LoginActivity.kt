package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        // FIXME: delete me - and do not commit me!
        //forcedLogin("sign@student.upt.ro", "111111")

        binding.loginButton.setOnClickListener() {
            loginAction()
        }

        binding.signupButton.setOnClickListener() {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun forcedLogin(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val studentsDatabase = Firebase.firestore
                    val studentFirebaseId = FirebaseAuth.getInstance().currentUser?.uid
                    val studentDoc = studentsDatabase.collection("students")
                        .document(studentFirebaseId!!)

                    studentDoc
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists() &&
                                documentSnapshot.getBoolean("IsAdmin") == true
                            ) {
                                val intent = Intent(this, AdminHomeActivity::class.java)
                                startActivity(intent)
                            } else {
                                val firebaseUser: FirebaseUser = task.result!!.user!!
                                startApplication(firebaseUser, email)
                            }
                        }
                } else {
                    Toast.makeText(
                        this, task.exception!!.message.toString(), Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun loginAction() {
        when {
            TextUtils.isEmpty(binding.email.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this, "Please enter email.", Toast.LENGTH_SHORT
                ).show()
            }

            TextUtils.isEmpty(binding.password.text.toString().trim { it <= ' ' }) -> {
                Toast.makeText(
                    this, "Please enter password.", Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                val email: String = binding.email.text.toString().trim { it <= ' ' }
                val password: String = binding.password.text.toString().trim { it <= ' ' }

                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val studentsDatabase = Firebase.firestore
                            val studentFirebaseId = FirebaseAuth.getInstance().currentUser?.uid
                            val studentDoc = studentsDatabase.collection("students")
                                .document(studentFirebaseId!!)

                            studentDoc
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists() &&
                                        documentSnapshot.getBoolean("IsAdmin") == true
                                    ) {
                                        val intent = Intent(this, AdminHomeActivity::class.java)
                                        startActivity(intent)
                                    } else {
                                        val firebaseUser: FirebaseUser = task.result!!.user!!
                                        startApplication(firebaseUser, email)
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this, task.exception!!.message.toString(), Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }

    private fun startApplication(
        firebaseUser: FirebaseUser, email: String
    ) {
        val studentsDatabase = Firebase.firestore
        val studentReference =
            studentsDatabase.collection("students").document(firebaseUser.uid)
        studentReference.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = document.data
                var hasEmptyFields = false
                for ((key, value) in data!!) {
                    if (value == null || value == "") {
                        hasEmptyFields = true
                        break
                    }
                }
                if (hasEmptyFields) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("userId", firebaseUser.uid)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    Toast.makeText(
                        this, "You need to complete your details first.", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.putExtra("userId", firebaseUser.uid)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    Toast.makeText(
                        this, "Welcome!", Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }.addOnFailureListener { e ->
            Log.w(
                ContentValues.TAG, "Error retrieving Student Document", e
            )
        }
    }
}