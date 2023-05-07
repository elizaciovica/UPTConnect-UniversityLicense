package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityCreatePollBinding
import edu.licenta.uptconnect.model.Course
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class CreatePollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityCreatePollBinding

    private val db = Firebase.firestore

    private var chosenDuration: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val currentTime = System.currentTimeMillis()

    private var studentFirebaseId = ""
    private var email = ""
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
        setPollDurationDropDown()
        createPoll()
    }

    private fun setBinding() {
        binding = ActivityCreatePollBinding.inflate(layoutInflater)
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

    private fun createPoll() {
        val optionsList = mutableListOf<String>()
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
                .setPositiveButton("Add") { dialog, _ ->
                    val newOption = newEditText.text.toString()
                    if (newOption.isNotEmpty()) {
                        optionsList += newOption
                        newText.text = newOption
                        binding.editTextContainer.addView(newText)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        binding.createPoll.setOnClickListener {
            val pollQuestion = binding.createTitleOfPoll.text.toString()

            if (pollQuestion.isEmpty() || optionsList.isEmpty() || chosenDuration.isEmpty()) {
                Toast.makeText(
                    this, "The poll must have a question, options and duration", Toast.LENGTH_SHORT
                ).show()
            } else {
                val poll = hashMapOf(
                    "question" to pollQuestion,
                    "options" to optionsList,
                    "start_time" to dateFormat.format(Date(currentTime)),
                    "end_time" to dateFormat.format(
                        Date(
                            currentTime + TimeUnit.DAYS.toMillis(
                                chosenDuration.toLong()
                            )
                        )
                    ),// 24 hours from now
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
        intent.putExtra("imageUrl", imageUrl)
        intent.putExtra("studentName", studentName)
        startActivity(intent)
    }

    private fun setPollDurationDropDown() {
        val facultyLists = listOf("1", "2", "3", "7")
        val autoComplete: AutoCompleteTextView = binding.autoCompletePollDuration
        val adapter = ArrayAdapter(this, R.layout.facultieslist_item, facultyLists)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)
                chosenDuration = itemSelected.toString()
            }
    }
}