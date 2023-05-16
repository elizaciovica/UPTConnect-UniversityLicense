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
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivitySpecialPollBinding
import edu.licenta.uptconnect.model.ScheduleData
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class SpecialPollActivity : DrawerLayoutActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var binding: ActivitySpecialPollBinding

    private val db = Firebase.firestore

    private var chosenDuration: String = ""
    private var chosenGroupName: String = ""
    private var chosenGroupId: String = ""
    private var chosenStartDate: String = ""
    private val groupMap = HashMap<String, String>()
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
        binding = ActivitySpecialPollBinding.inflate(layoutInflater)
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

        val coursesMap = HashMap<String, List<String>>()
        var chosenCourseTypes: MutableList<String>
        var finalType = ""

        val getGroupsTask = db.collection("courses")
            .get()

        val populateDropDownTask = getGroupsTask.continueWithTask { task ->
            for (document in task.result.documents) {
                groupMap[document.data!!["Name"].toString()] = document.id
                coursesMap[document.data!!["Name"].toString()] =
                    document["Teaching Way"] as List<String>
            }
            return@continueWithTask Tasks.forResult(null)
        }

        populateDropDownTask.addOnSuccessListener {

            val groupList = ArrayList(groupMap.keys)
            val optionsList = mutableListOf<String>()
            val autoCompleteGroup: AutoCompleteTextView = binding.autoCompleteGroup
            val adapterGroup = ArrayAdapter(this, R.layout.facultieslist_item, groupList)

            autoCompleteGroup.setAdapter(adapterGroup)
            autoCompleteGroup.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                    val groupSelected = adapterView.getItemAtPosition(i)
                    chosenGroupName = groupSelected.toString()
                    chosenGroupId = groupMap[groupSelected.toString()].toString()
                    chosenCourseTypes = coursesMap[groupSelected.toString()] as MutableList<String>

                    binding.editTextContainer.removeAllViews()
                    optionsList.clear()

                    for (courseType in chosenCourseTypes) {
                        if (courseType != "course") {
                            finalType = courseType
                        }
                    }

                    binding.courseTypeText.text = finalType

                    val scheduleCollectionRef =
                        Firebase.firestore.collection("schedules")
                            .document("courses")
                            .collection(chosenGroupId).document(finalType)

                    scheduleCollectionRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                //scheduleList object returned by the get() method is actually a list of HashMap objects, where each HashMap represents a single ScheduleData object
                                val scheduleList =
                                    documentSnapshot.get("scheduleList") as ArrayList<HashMap<String, Any>>
                                val schedules = mutableListOf<ScheduleData>()

                                //map() function to iterate over each HashMap object in the list and convert it into a ScheduleData object
                                for (scheduleMap in scheduleList) {
                                    val day = scheduleMap["day"] as String
                                    val startTime = scheduleMap["startTime"] as String
                                    val endTime = scheduleMap["endTime"] as String
                                    val maxNoOfStudents = scheduleMap["maxNoOfStudents"] as String

                                    val scheduleData =
                                        ScheduleData(day, startTime, endTime, maxNoOfStudents)
                                    schedules.add(scheduleData)
                                }

                                for (schedule in schedules) {
                                    val newOption = TextView(this).apply {
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
                                        setBackgroundColor(
                                            ContextCompat.getColor(
                                                this@SpecialPollActivity,
                                                R.color.mine8
                                            )
                                        )
                                        gravity = Gravity.CENTER
                                    }

                                    val optionDay =
                                        schedule.day + " " + schedule.startTime + " " + schedule.endTime
                                    optionsList += optionDay
                                    newOption.text = optionDay
                                    binding.editTextContainer.addView(newOption)
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "This course does not have a schedule set yet.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w(ContentValues.TAG, "Error retrieving schedule", e)
                        }
                }

            binding.createPoll.setOnClickListener {
                val pollQuestion = binding.createTitleOfPoll.text.toString()

                if (pollQuestion.isEmpty() || optionsList.isEmpty() || chosenDuration.isEmpty() || chosenGroupName.isEmpty()) {
                    Toast.makeText(
                        this,
                        "The poll must have a group selected, question, options and duration",
                        Toast.LENGTH_SHORT
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
                        "isFromLeader" to true,
                        "hasResults" to false,
                        "type" to finalType
                    )

                    val pollCollectionRef =
                        db.collection("polls").document("courses_polls").collection(chosenGroupId)
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
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedTime = dateFormat.format(Date(currentTime))
                    val newsTitle = "New Poll"
                    val newsContent =
                        "A poll was created in $chosenGroupName group, by ADMIN $studentName"
                    val new = hashMapOf(
                        "title" to newsTitle,
                        "content" to newsContent,
                        "time" to formattedTime,
                        "courseId" to chosenGroupId
                    )
                    val newsCollectionRef = db.collection("news")
                    newsCollectionRef.document()
                        .set(new, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "News created successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.d(ContentValues.TAG, "Error creating news", exception)
                        }

                    backHome()
                    finish()
                }
            }
        }
    }

    private fun backHome() {
        val intent = Intent(this, HomeActivity::class.java)
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

    private fun choosePollStartDateAndTime() {
        binding.startDateButton.setOnClickListener {
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@SpecialPollActivity, this@SpecialPollActivity, year, month, day)
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
            this@SpecialPollActivity, this@SpecialPollActivity, hour, minute,
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
