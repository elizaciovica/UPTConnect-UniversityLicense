package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.cardview.widget.CardView
import com.google.android.gms.tasks.Tasks
import com.google.android.material.card.MaterialCardView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityCreateScheduleAdminBinding
import edu.licenta.uptconnect.model.Schedule
import edu.licenta.uptconnect.model.ScheduleData

class CreateScheduleAdminActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityCreateScheduleAdminBinding

    private val db = Firebase.firestore
    private val coursesMap = HashMap<String, List<String>>()
    private val coursesMapIds = HashMap<String, String>()
    private var section: String = ""
    private var chosenCourse: String = ""
    private var chosenType: String = ""
    private var chosenId: String = ""
    private var chosenCourseTypes = mutableListOf<String>()
    private var cardViewList = mutableListOf<CardView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()

        val navigationView = binding.navigationView
        val drawerLayout = binding.drawerLayout
        initializeMenu(
            drawerLayout,
            navigationView,
            0
        )
        section = intent.getStringExtra("section").toString()

        setCoursesDropDown()
        createSchedule()
        saveSchedule()
    }

    private fun setBinding() {
        binding = ActivityCreateScheduleAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun createSchedule() {
        val cardViewContainer = binding.scheduleLayout

        binding.optionsDaysButton.setOnClickListener {

            val cardView = layoutInflater.inflate(R.layout.item_schedule, null) as MaterialCardView
            cardViewContainer.addView(cardView)

            cardViewList.add(cardView)

            val autoCompleteStartTime =
                cardView.findViewById<AutoCompleteTextView>(R.id.auto_complete_start_time)
            val autoCompleteEndTime =
                cardView.findViewById<AutoCompleteTextView>(R.id.auto_complete_end_time)

            setStartAndEndTimeDropDown(autoCompleteStartTime, autoCompleteEndTime)
        }
    }

    private fun saveSchedule() {
        binding.saveButton.setOnClickListener {
            val schedule = mutableListOf<ScheduleData>()
            var isScheduleValid = true

            for (card in cardViewList) {
                val radioGroup1 = card.findViewById<RadioGroup>(R.id.radio_group)
                val autoCompleteStartTime1 =
                    card.findViewById<AutoCompleteTextView>(R.id.auto_complete_start_time)
                val autoCompleteEndTime1 =
                    card.findViewById<AutoCompleteTextView>(R.id.auto_complete_end_time)
                val maxNoOfStudents1 = card.findViewById<EditText>(R.id.max_no_of_students)
                val radioButton1 =
                    radioGroup1.findViewById<RadioButton>(radioGroup1.checkedRadioButtonId)

                if (autoCompleteEndTime1.text.toString()
                        .isNotEmpty() && autoCompleteStartTime1.text.toString().isNotEmpty()
                ) {
                    if (autoCompleteEndTime1.text.toString()
                            .toInt() < autoCompleteStartTime1.text.toString().toInt()
                    ) {
                        isScheduleValid = false
                        Toast.makeText(
                            this,
                            "The end hour should be after the start hour",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                 if(autoCompleteEndTime1.text.isEmpty() || autoCompleteStartTime1.text.isEmpty() || radioGroup1.checkedRadioButtonId == -1 ||
                    ((chosenType != "course") && (maxNoOfStudents1.text.toString().isEmpty())) || chosenCourse.isEmpty() ) {
                    isScheduleValid = false
                } else {
                    val scheduleData = ScheduleData(
                        radioButton1.text.toString(),
                        autoCompleteStartTime1.text.toString(),
                        autoCompleteEndTime1.text.toString(),
                        maxNoOfStudents1.text.toString()
                    )

                    schedule.add(scheduleData)
                }
            }

            if(isScheduleValid && schedule.isNotEmpty()) {
                val scheduleList = Schedule(schedule)

                val scheduleCollectionRef =
                    Firebase.firestore.collection("schedules")
                        .document("courses")
                        .collection(chosenId).document(chosenType)

                scheduleCollectionRef.set(scheduleList)
                Toast.makeText(
                    this,
                    "Schedule created successfully",
                    Toast.LENGTH_SHORT
                ).show()
                backHome()
                finish()
            }
            else {
                Toast.makeText(
                    this,
                    "All the fields are required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun backHome() {
        val intent = Intent(this, AdminIndividualSectionActivity::class.java)
        intent.putExtra("section", section)
        startActivity(intent)
    }

    private fun setStartAndEndTimeDropDown(
        autoCompleteTextViewStart: AutoCompleteTextView,
        autoCompleteTextViewEnd: AutoCompleteTextView
    ) {
        val startHoursList = listOf("8", "10", "12", "14", "16", "18")
        val endHoursList = listOf("10", "12", "14", "16", "18", "20")
        val autoCompleteStart: AutoCompleteTextView = autoCompleteTextViewStart
        val adapterStart = ArrayAdapter(this, R.layout.dropdown_item, startHoursList)
        autoCompleteStart.setAdapter(adapterStart)
        autoCompleteStart.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)
            }

        val autoCompleteEnd: AutoCompleteTextView = autoCompleteTextViewEnd
        val adapterEnd = ArrayAdapter(this, R.layout.dropdown_item, endHoursList)
        autoCompleteEnd.setAdapter(adapterEnd)
        autoCompleteEnd.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)
            }
    }

    private fun setCoursesDropDown() {

        val getCoursesTask = db.collection("courses")
            .whereEqualTo("Section", section)
            .get()

        val populateDropDownTask = getCoursesTask.continueWithTask { task ->
            for (document in task.result.documents) {
                coursesMap[document.data!!["Name"].toString()] =
                    document["teachingWay"] as List<String>
                coursesMapIds[document.data!!["Name"].toString()] = document.id
            }
            return@continueWithTask Tasks.forResult(null)
        }

        populateDropDownTask.addOnSuccessListener {

            val courseList = ArrayList(coursesMap.keys)
            val autoCompleteCourse: AutoCompleteTextView = binding.autoCompleteCourse
            val adapterCourse = ArrayAdapter(this, R.layout.dropdown_item, courseList)

            autoCompleteCourse.setAdapter(adapterCourse)
            autoCompleteCourse.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                    val courseSelected = adapterView.getItemAtPosition(i)
                    chosenCourse = courseSelected.toString()
                    chosenCourseTypes = coursesMap[courseSelected.toString()] as MutableList<String>
                    chosenId = coursesMapIds[courseSelected.toString()].toString()

                    val autoCompleteCourseType: AutoCompleteTextView =
                        binding.autoCompleteCourseType
                    val adapterType =
                        ArrayAdapter(this, R.layout.dropdown_item, chosenCourseTypes)

                    autoCompleteCourseType.setAdapter(adapterType)
                    autoCompleteCourseType.onItemClickListener =
                        AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                            val coursetypeSelected = adapterView.getItemAtPosition(i)
                            chosenType = coursetypeSelected.toString()
                        }
                }
        }
    }
}