package edu.licenta.uptconnect

import android.content.ContentValues
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityCreatePollBinding
import edu.licenta.uptconnect.model.Course
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class CreatePollActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePollBinding
    private val db = Firebase.firestore
    private var studentFirebaseId = ""
    private var email = ""
    private var chosenDuration: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getProfileDetails()
        setPollDurationDropDown()
        createPoll()
    }

    private fun setBinding() {
        binding = ActivityCreatePollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val currentTime = System.currentTimeMillis()

    private fun createPoll() {
        var optionsList = mutableListOf<String>()
        val course = intent.getParcelableExtra<Course>("course")!!
        val pollCollectionRef =
            db.collection("polls").document("courses_polls").collection(course.id)

        binding.optionsButton.setOnClickListener {
            val newEditText = EditText(this)
            newEditText.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val newText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 30 // set the margin top
                    marginStart = 70
                    marginEnd = 70
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                setBackgroundResource(R.drawable.almostround)
                gravity = Gravity.CENTER
            }

            AlertDialog.Builder(this)
                .setTitle("New option")
                .setView(newEditText)
                .setPositiveButton("Add") { dialog, which ->
                    val newOption = newEditText.text.toString()
                    if (newOption.isNotEmpty()) {
                        optionsList += newOption
                        newText.text = newOption
                        binding.editTextContainer.addView(newText)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.createPoll.setOnClickListener {
            val pollQuestion = binding.createTitleOfPoll.text.toString()

            if(pollQuestion.isEmpty() || optionsList.isEmpty() || chosenDuration.isEmpty()) {
                Toast.makeText(
                    this, "The poll must have a question, options and duration", Toast.LENGTH_SHORT
                ).show()
            } else {
                val poll = hashMapOf(
                    "question" to pollQuestion,
                    "options" to optionsList,
                    "start_time" to dateFormat.format(Date(currentTime)),
                    "end_time" to dateFormat.format(Date(currentTime + TimeUnit.DAYS.toMillis(chosenDuration.toLong()))),// 24 hours from now
                    "createdBy" to studentFirebaseId
                )
                pollCollectionRef.document()
                    .set(poll, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Poll created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this,
                            "Error creating poll",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                seePoll()
                finish()
            }
        }
    }

    private fun seePoll() {
        val course = intent.getParcelableExtra<Course>("course")!!
        val intent = Intent(this, PollActivity::class.java)
        intent.putExtra("course", course)
        intent.putExtra("email", email)
        intent.putExtra("userId", studentFirebaseId)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setPollDurationDropDown() {
        val facultyLists = listOf("1", "2", "3", "7")
        val autoComplete: AutoCompleteTextView = binding.autoCompletePollDuration
        val adapter = ArrayAdapter(this, R.layout.facultieslist_item, facultyLists)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, i, l ->
                val itemSelected = adapterView.getItemAtPosition(i)
                chosenDuration = itemSelected.toString()
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