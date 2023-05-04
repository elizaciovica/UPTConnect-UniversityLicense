package edu.licenta.uptconnect

import android.content.ContentValues
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityIndividualPollBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.Poll
import java.text.SimpleDateFormat
import java.util.*

class IndividualPollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityIndividualPollBinding
    private var studentFirebaseId = ""
    private var email = ""
    private lateinit var course: Course
    private lateinit var poll: Poll
    private var button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        getProfileDetails()
        seePoll()
    }

    private fun setBinding() {
        binding = ActivityIndividualPollBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun seePoll() {
        course = intent.getParcelableExtra<Course>("course")!!
        poll = intent.getParcelableExtra<Poll>("poll")!!
        val question = poll.question
        val options = poll.options

        //add the question to the layout
        val questionTextView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 500 // set the margin top
                marginStart = 70
                marginEnd = 70
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)
            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            setBackgroundResource(R.drawable.two_round_corners)
            gravity = Gravity.CENTER
        }
        questionTextView.text = question
        val linearLayout = binding.newPollLayout
        linearLayout.addView(questionTextView)

        //add the options
        val radioGroup = RadioGroup(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        options?.forEach { option ->
            val radioButton = RadioButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 100 // set the margin top
                    leftMargin = 500
                }
                text = option
                id = View.generateViewId() // generate a unique ID for each radio button

            }
            radioGroup.addView(radioButton)
        }
        // add the radio group to your layout
        linearLayout.addView(radioGroup)

        //verify if the endDate has passed
        checkPollEndDate(questionTextView, radioGroup, poll, linearLayout)

        //if the user already voted then disable the radiogroup
        Firebase.firestore.collection("polls").document("courses_polls_votes")
            .collection(course.id + poll.pollId)
            .whereEqualTo("votedBy", studentFirebaseId)
            .whereEqualTo("pollId", poll.pollId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // a document already exists -> disable the radio group
                    radioGroup.isEnabled = false
                    for (i in 0 until radioGroup.childCount) {
                        radioGroup.getChildAt(i).isEnabled = false
                    }
                    //set question text to grey
                    questionTextView.setTextColor(ContextCompat.getColor(this, R.color.grey))
                    for (document in querySnapshot) {
                        //put the selected option
                        val textViewChoice = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                leftMargin = 70
                                rightMargin = 70
                                bottomMargin = 50
                                topMargin = 70
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                            }
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                            typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                            gravity = Gravity.CENTER
                        }
                        textViewChoice.text = "Your choice: " + document.data["answer"]
                        textViewChoice.setTextColor(ContextCompat.getColor(this, R.color.grey))
                        linearLayout.addView(textViewChoice)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving poll votes", exception)
            }

        //make the vote button appear when an option is selected
        var isRadioButtonSelected = false
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (button == null) {
                button = Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 30 // set the margin top
                        marginStart = 70
                        marginEnd = 70
                    }
                }
                // get the selected radio button and its text
                val radioButton = group.findViewById<RadioButton>(checkedId)

                //todo deselect. does not work
                if (radioButton.isChecked && isRadioButtonSelected) {
                    // Deselect the radio button
                    radioButton.isChecked = false
                    isRadioButtonSelected = false
                    button?.visibility = View.INVISIBLE
                } else {
                    // Select the radio button
                    isRadioButtonSelected = true
                    button?.visibility = View.VISIBLE
                }

                val selectedOption = radioButton.text.toString()
                linearLayout.addView(button)
                button?.text = "VOTE"
                button?.setOnClickListener {
                    // perform actions based on the selected option
                    val pollCollectionRef =
                        Firebase.firestore.collection("polls")
                            .document("courses_polls_votes")
                            .collection(course.id + poll.pollId)
                    val pollAnswer = hashMapOf(
                        "answer" to selectedOption,
                        "votedBy" to studentFirebaseId,
                        "pollId" to poll.pollId
                    )
                    pollCollectionRef.document()
                        .set(pollAnswer, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Vote recorded successfully",
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
                    //refresh
                    recreate()
                }
            }

        }
    }

    private fun checkPollEndDate(
        textView: TextView,
        radioGroup: RadioGroup,
        poll: Poll,
        linearLayout: LinearLayout
    ) {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val endDate = dateFormat.parse(poll.end_time)
        if (currentDate.before(endDate)) {
            // The current date is before the end date, so the views should be visible
            textView.visibility = View.VISIBLE
            radioGroup.visibility = View.VISIBLE
        } else {
            // The current date is after the end date, so the views should be hidden
            textView.visibility = View.GONE
            radioGroup.visibility = View.GONE

            val cardView = CardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 500
                    leftMargin = 70
                    rightMargin = 70
                    bottomMargin = 50
                }
                background =
                    ContextCompat.getDrawable(this@IndividualPollActivity, R.drawable.poll_results)
            }
            cardView.cardElevation = 50f
            linearLayout.addView(cardView)

            val linearLayoutText = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }

            //add a new view with details
            val textViewInfo = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 70
                    rightMargin = 70
                    bottomMargin = 50
                    topMargin = 100
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            textViewInfo.text = "POLL CLOSED"
            textViewInfo.setTextColor(ContextCompat.getColor(this, R.color.red))
            linearLayoutText.addView(textViewInfo)

            val textViewStatement = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    leftMargin = 70
                    rightMargin = 70
                    bottomMargin = 50
                    topMargin = 300
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            }
            textViewStatement.text = "Results for: " + textView.text + " poll"
            textViewStatement.setTextColor(ContextCompat.getColor(this, R.color.white))
            linearLayoutText.addView(textViewStatement)
            calculatePollResults(poll, linearLayoutText)
            cardView.addView(linearLayoutText)
        }
    }

    //maybe when voting we create a collection
    private fun calculatePollResults(poll: Poll, linearLayout: LinearLayout) {
        val pollVotesDatabase = Firebase.firestore
        var list = listOf<String>()

        pollVotesDatabase.collection("polls").document("courses_polls_votes")
            .collection(course.id + poll.pollId)
            .get().addOnSuccessListener { results ->
                for (result in results) {
                    //take result["answer"] put it in a list and then iterate through that list and take how many entries of same
                    list += result["answer"].toString()
                }
                val answers = list.groupingBy { it }.eachCount().filter { it.value >= 1 }
                for ((value, count) in answers) {
                    val textViewAnswer = TextView(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            leftMargin = 70
                            rightMargin = 70
                            bottomMargin = 100
                        }
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    }
                    textViewAnswer.text = "$value $count"
                    textViewAnswer.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
                    linearLayout.addView(textViewAnswer)
                }
            }
    }

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
}