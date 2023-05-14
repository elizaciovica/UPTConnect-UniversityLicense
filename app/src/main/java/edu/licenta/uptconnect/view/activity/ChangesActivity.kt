package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityChangesBinding
import edu.licenta.uptconnect.model.Course

class ChangesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangesBinding

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""
    private lateinit var course: Course
    private var actualHour = ""
    private var wantedHour = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getExtrasFromIntent()
        setProfileDetails()
        createChangeRequest()
        showMatches()
    }

    private fun setBinding() {
        binding = ActivityChangesBinding.inflate(layoutInflater)
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
        course = intent.getParcelableExtra("course")!!
    }

    private fun createChangeRequest() {
        val teachingWay = course.teachingWay as ArrayList<*>
        var finalTeachingWay = ""
        for (way in teachingWay) {
            if (way != "course") {
                finalTeachingWay = way as String
            }
        }

        binding.textChanges.text = "$finalTeachingWay changes"
        val scheduleCollectionRef =
            Firebase.firestore.collection("schedules")
                .document("courses").collection(course.id).document(finalTeachingWay)

        scheduleCollectionRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val scheduleList =
                        documentSnapshot.get("scheduleList") as java.util.ArrayList<HashMap<String, Any>>
                    val schedules = mutableListOf<String>()

                    //map() function to iterate over each HashMap object in the list and convert it into a ScheduleData object
                    for (scheduleMap in scheduleList) {
                        val day = scheduleMap["day"] as String
                        val startTime = scheduleMap["startTime"] as String
                        val endTime = scheduleMap["endTime"] as String
                        val optionDay =
                            "$day $startTime-$endTime"
                        schedules.add(optionDay)
                    }
                    val autoCompleteActualHour: AutoCompleteTextView =
                        binding.autoCompleteActualHour
                    val adapterActualHour =
                        ArrayAdapter(this, R.layout.facultieslist_item, schedules)
                    autoCompleteActualHour.setAdapter(adapterActualHour)
                    autoCompleteActualHour.onItemClickListener =
                        AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                            actualHour = adapterView.getItemAtPosition(i).toString()
                        }

                    val autoCompleteWantedHour: AutoCompleteTextView =
                        binding.autoCompleteWantedHour
                    autoCompleteWantedHour.setAdapter(adapterActualHour)
                    autoCompleteWantedHour.onItemClickListener =
                        AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                            wantedHour = adapterView.getItemAtPosition(i).toString()
                        }
                } else {
                    Toast.makeText(
                        this,
                        "This course has not yet any hour assigned",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        binding.createChangesRequest.setOnClickListener {

            if (wantedHour.isEmpty() || actualHour.isEmpty() || wantedHour == actualHour) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                val changesMap = hashMapOf(
                    "actualHour" to actualHour,
                    "wantedHour" to wantedHour
                )

                Firebase.firestore
                    .collection("changes")
                    .document("courses")
                    .collection(course.id)
                    .document(studentFirebaseId)
                    .set(changesMap, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Request registered successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnCanceledListener {
                        Toast.makeText(
                            this,
                            "Error creating request",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                backHome()
                finish()
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

    private fun showMatches() {
        binding.seeMatches.setOnClickListener {
            showMatchesDialog()
        }
    }

    private fun showMatchesDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.item_matches_dialog, null)

        matchQuery(view)
        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun matchQuery(view: View) {
        var currentStudentActualHour = ""
        var currentStudentWantedHour = ""
        val matchesDatabaseRef = Firebase.firestore
            .collection("changes")
            .document("courses")
            .collection(course.id)
        val matchesContainer = view.findViewById<LinearLayout>(R.id.matches_container)

        matchesDatabaseRef
            .document(studentFirebaseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    currentStudentActualHour = documentSnapshot.getString("actualHour").toString()
                    currentStudentWantedHour = documentSnapshot.getString("wantedHour").toString()
                } else {
                    val textView = view.findViewById<TextView>(R.id.no_match_message)
                    textView.visibility = View.VISIBLE
                    textView.text = "You didn't send any request"
                }
                matchesDatabaseRef.whereEqualTo("actualHour", currentStudentWantedHour)
                    .whereEqualTo("wantedHour", currentStudentActualHour)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                Firebase.firestore.collection("students").document(document.id)
                                    .get().addOnSuccessListener { studentDocument ->
                                        val studentNameView = TextView(this).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            ).apply {
                                                text =
                                                    studentDocument.data!!["FirstName"].toString() + " " + studentDocument.data!!["LastName"].toString()
                                                topMargin = 50
                                                marginStart = 150
                                                marginEnd = 150
                                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                            }
                                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                            setBackgroundResource(R.drawable.rounded_corners)
                                            gravity = Gravity.CENTER
                                        }
                                        matchesContainer.addView(studentNameView)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d(ContentValues.TAG, "Error retrieving Students Name", exception)
                                    }

                            }
                        } else {
                            view.findViewById<TextView>(R.id.no_match_message).visibility =
                                View.VISIBLE
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error retrieving matches", exception)
                    }
            }
    }
}