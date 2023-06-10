package edu.licenta.uptconnect.view.activity

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
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


class CreatePollActivity : DrawerLayoutActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivityCreatePollBinding

    private val db = Firebase.firestore

    private var chosenDuration: String = ""
    private var chosenStartDate: String = ""
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""

    private var day = 0
    private var month: Int = 0
    private var year: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var chosenDay = 0
    private var chosenMouth: Int = 0
    private var chosenYear: Int = 0
    private var chosenHour: Int = 0
    private var chosenMinute: Int = 0

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
        choosePollStartDateAndTime()
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
        val newsCollectionRef = db.collection("news")

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

            if (pollQuestion.isEmpty() || optionsList.isEmpty() || chosenDuration.isEmpty() || chosenStartDate.isEmpty() || chosenStartDate.isEmpty()) {
                Toast.makeText(
                    this, "The poll must have a question, options and duration", Toast.LENGTH_SHORT
                ).show()
            } else {

                calendar.time = dateFormat.parse(chosenStartDate)!!
                calendar.add(Calendar.DAY_OF_YEAR, chosenDuration.toInt())

                val poll = hashMapOf(
                    "question" to pollQuestion,
                    "options" to optionsList,
                    "start_time" to chosenStartDate,
                    "end_time" to dateFormat.format(
                        calendar.time
                    ),
                    "createdBy" to studentFirebaseId,
                    "isFromLeader" to false,
                    "hasResults" to false,
                    "type" to "noType"
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

                val currentTime = System.currentTimeMillis()
                val formattedTime = dateFormat.format(Date(currentTime))
                val newsTitle = "New Poll"
                val newsContent = "A new poll has been created"
                val new = hashMapOf(
                    "title" to newsTitle,
                    "content" to newsContent,
                    "time" to formattedTime,
                    "courseId" to course.id,
                    "createdBy" to studentName
                )
                newsCollectionRef.document()
                    .set(new, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(ContentValues.TAG, "News created successfully")
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error creating news", exception)
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
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, facultyLists)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)
                chosenDuration = itemSelected.toString()
            }
    }

    private fun choosePollStartDateAndTime() {
        binding.startDateButton.setOnClickListener {
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@CreatePollActivity, this@CreatePollActivity, year, month, day)
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        chosenDay = dayOfMonth
        chosenYear = year
        chosenMouth = month
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog = TimePickerDialog(
            this@CreatePollActivity, this@CreatePollActivity, hour, minute,
            DateFormat.is24HourFormat(this)
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        binding.chosenDate.visibility = View.VISIBLE
        chosenHour = hourOfDay
        chosenMinute = minute

        calendar.set(Calendar.YEAR, chosenYear)
        calendar.set(Calendar.MONTH, chosenMouth)
        calendar.set(Calendar.DAY_OF_MONTH, chosenDay)
        calendar.set(Calendar.HOUR_OF_DAY, chosenHour)
        calendar.set(Calendar.MINUTE, chosenMinute)
        val date = calendar.time
        val formattedDate = dateFormat.format(date).toString()

        binding.chosenDate.text = formattedDate
        chosenStartDate = formattedDate
    }
}