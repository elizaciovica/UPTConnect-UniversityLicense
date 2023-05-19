package edu.licenta.uptconnect.view.activity

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityIndividualPollBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.Poll
import edu.licenta.uptconnect.util.CoursePlacesAssignmentUtil
import java.text.SimpleDateFormat
import java.util.*

class IndividualPollActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityIndividualPollBinding
    private val coursePlacesAssignmentUtil = CoursePlacesAssignmentUtil()

    private lateinit var course: Course
    private lateinit var poll: Poll

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""

    @SuppressLint("ClickableViewAccessibility")
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
        if (poll.isFromLeader) {
            showLeaderPoll()
        } else {
            seeUsualPoll()
        }
    }

    private fun setBinding() {
        binding = ActivityIndividualPollBinding.inflate(layoutInflater)
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
        poll = intent.getParcelableExtra("poll")!!
    }

    private fun showLeaderPoll() {
        val question = poll.question
        val options = poll.options
        val linearLayout = binding.newPollLayout
        val optionOrder = mutableMapOf<String, Int>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        binding.questionPoll.text = question

        for (option in options) {
            val optionView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    text = option
                    topMargin = 50
                    marginStart = 150
                    marginEnd = 150
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                setBackgroundResource(R.drawable.rounded_corners)
                gravity = Gravity.CENTER
            }
            optionView.isClickable = true
            optionView.setOnLongClickListener {
                val clipData = ClipData.newPlainText("", "")
                val shadowBuilder = DragShadowBuilder(optionView)
                optionView.startDragAndDrop(clipData, shadowBuilder, optionView, 0)
                true
            }
            linearLayout.addView(optionView)
        }

        for (i in 0 until linearLayout.childCount) {
            val optionView = linearLayout.getChildAt(i) as TextView
            optionOrder[optionView.text.toString()] = i
        }

        linearLayout.setOnDragListener { view, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    true
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    true
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    val draggedView = event.localState as TextView
                    // Clamp the y-coordinate of the drag event to the range of valid y-coordinates
                    val y = event.y.coerceIn(0f, (view as ViewGroup).height.toFloat())
                    // Calculate the index at which to insert the dragged view based on the y-coordinate
                    val dropIndex = calculateDropIndex(view.parent as ViewGroup, y)
                    // Move the dragged view to the calculated index
                    moveViewToIndex(draggedView, dropIndex)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    true
                }
                DragEvent.ACTION_DROP -> {
                    val draggedView = event.localState as TextView
                    val oldParent = draggedView.parent as ViewGroup

                    // Get the new parent, which should be the LinearLayout
                    val newParent = view as LinearLayout

                    // Calculate the index at which to add the dragged view
                    val dropIndex = calculateDropIndex(newParent, event.y)

                    // Remove the dragged view from its old parent and add it to the new parent
                    oldParent.removeView(draggedView)
                    newParent.addView(draggedView, dropIndex)

                    // Set the background color of the LinearLayout back to normal
                    view.setBackgroundColor(Color.WHITE)

                    // Set the visibility of the dragged view back to visible
                    draggedView.visibility = View.VISIBLE

                    var buttonSend = "SEND"
                    binding.pollButton.text = buttonSend

                    //skip the title end the button
                    for (i in 0 until linearLayout.childCount) {
                        val optionView = linearLayout.getChildAt(i) as TextView
                        optionOrder[optionView.text.toString()] = i
                    }
                    true
                }
                else -> false
            }
        }

        //set the choice and refresh activity
        binding.pollButton.setOnClickListener() {
            val leaderPollCollectionRef =
                Firebase.firestore.collection("polls")
                    .document("courses_polls_votes_leader_polls")
                    .collection(course.id + poll.pollId)
            val chosenData = hashMapOf(
                "votedBy" to studentFirebaseId,
                "optionOrder" to optionOrder,
                "timeOfVote" to dateFormat.format(Date(currentTime))
            )
            leaderPollCollectionRef.document(studentName)
                .set(chosenData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Choice recorded successfully",
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
            recreate()
        }

        //delete poll is available for the user who created it
        deletePollIfCreatedByCurrentUser(binding.materialCard)

        //if the user already voted then disable the options
        disableOptionsIfTheUserAlreadyVoted(binding.newPollLayout)

        //if the end date passed -> show results after calling the algorithm
        //the first person who enters the poll will trigger the algorithm and a flag will be set for
        //results for the other persons not to trigger it again
        val currentDate = Date()
        val endDate = dateFormat.parse(poll.end_time)

        if (endDate.before(currentDate) && !poll.hasResults) {

            binding.questionPoll.text = "Results for " + poll.question
            binding.pollButton.visibility = View.GONE
            for (i in 0 until linearLayout.childCount) {
                linearLayout.getChildAt(i).visibility = View.GONE
            }

            val task = coursePlacesAssignmentUtil.getDataFromFirebaseAndRearangeItBasedOnTimeStamp(
                course.id,
                poll.pollId
            )

            task.addOnSuccessListener { finalListOfPollChoices ->

                //todo change the hardcoded type
                val scheduleHoursTask =
                    coursePlacesAssignmentUtil.getLabHoursFromTheSchedule(course.id, poll.type)
                scheduleHoursTask.addOnSuccessListener { scheduleHours ->
                    val results = coursePlacesAssignmentUtil.pollResultsAlgorithm(
                        finalListOfPollChoices,
                        scheduleHours
                    )

                    val stringKeyMap = HashMap<String, MutableList<String>>()
                    for ((scheduleData, courseList) in results) {
                        val scheduleDataDay =
                            scheduleData.day + " " + scheduleData.startTime + " - " + scheduleData.endTime
                        stringKeyMap[scheduleDataDay] = courseList
                    }

                    Firebase.firestore
                        .collection("courses_repartition")
                        .document("courses")
                        .collection(course.id)
                        .document(poll.type)
                        .set(stringKeyMap, SetOptions.merge())
                        .addOnSuccessListener {
                            //save the results and also trigger the flag
                            Firebase.firestore.collection("polls")
                                .document("courses_polls")
                                .collection(course.id)
                                .document(poll.pollId)
                                .update("hasResults", true)
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Failure when creating the repartition", exception)
                        }

                    //change the layout in this case
                    for ((scheduleData, students) in results) {
                        for (student in students) {
                            val studentsDatabase = Firebase.firestore
                            val studentDoc =
                                studentsDatabase.collection("students").document(student)
                            studentDoc
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        studentName =
                                            "${documentSnapshot.get("FirstName")} ${
                                                documentSnapshot.get(
                                                    "LastName"
                                                )
                                            }"

                                        val labStudentsView = TextView(this).apply {
                                            layoutParams = LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                            ).apply {
                                                text =
                                                    "$studentName - ${scheduleData.day} ${scheduleData.startTime} - ${scheduleData.endTime}"
                                                topMargin = 50
                                                marginStart = 150
                                                marginEnd = 150
                                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                            }
                                            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                            setBackgroundResource(R.drawable.rounded_corners)
                                            gravity = Gravity.CENTER
                                        }
                                        linearLayout.addView(labStudentsView)
                                    }
                                }
                        }
                    }
                }
            }
        }

        //if the end date has passed, but the algorithm was already triggered
        val queryHashmapData = HashMap<String, MutableList<String>>()
        if (endDate.before(currentDate) && poll.hasResults) {

            binding.questionPoll.text = "Results for " + poll.question
            binding.pollButton.visibility = View.GONE
            for (i in 0 until linearLayout.childCount) {
                linearLayout.getChildAt(i).visibility = View.GONE
            }

            val getRepartitionTask =
                Firebase.firestore.collection("courses_repartition").document("courses")
                    .collection(course.id).document(poll.type).get()

            getRepartitionTask.addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val data = documentSnapshot.data
                    if (data != null) {
                        for ((field, value) in data) {
                            if (value is ArrayList<*>) {
                                val mutableList = mutableListOf<String>()
                                for (element in value) {
                                    if (element is String) {
                                        mutableList.add(element)
                                    }
                                }
                                queryHashmapData[field] = mutableList
                            }
                        }

                        //change the layout in this case
                        for ((scheduleData, students) in queryHashmapData) {
                            for (student in students) {
                                val studentsDatabase = Firebase.firestore
                                val studentDocTask =
                                    studentsDatabase.collection("students").document(student).get()
                                studentDocTask
                                    .addOnSuccessListener { documentSnapshot ->
                                        if (documentSnapshot.exists()) {
                                            studentName =
                                                "${documentSnapshot.get("FirstName")} ${
                                                    documentSnapshot.get(
                                                        "LastName"
                                                    )
                                                }"
                                            val labStudentsView = TextView(this).apply {
                                                layoutParams = LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                                ).apply {
                                                    text = "$studentName - $scheduleData"
                                                    topMargin = 50
                                                    bottomMargin = 70
                                                    marginStart = 150
                                                    marginEnd = 150
                                                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                                                }
                                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                                                setBackgroundResource(R.drawable.rounded_corners)
                                                gravity = Gravity.CENTER
                                            }
                                            linearLayout.addView(labStudentsView)
                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun disableOptionsIfTheUserAlreadyVoted(linearLayout: LinearLayout) {
        Firebase.firestore
            .collection("polls")
            .document("courses_polls_votes_leader_polls")
            .collection(course.id + poll.pollId)
            .whereEqualTo("votedBy", studentFirebaseId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // a document already exists -> disable the radio group
                    for (i in 0 until linearLayout.childCount) {
                        linearLayout.getChildAt(i).isEnabled = false
                    }
                    //set question text to grey
                    binding.questionPoll.setTextColor(ContextCompat.getColor(this, R.color.grey))
                    binding.pollButton.visibility = View.GONE
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error retrieving poll votes", exception)
            }
    }

    private fun calculateDropIndex(parent: ViewGroup, y: Float): Int {
        // Iterate over the children of the parent view
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            // Check if the y-coordinate of the drop location is within the bounds of the child view
            if (y < child.y + child.height / 2) {
                // If it is, return the index of the child view
                return i
            }
        }
        // If the y-coordinate is not within any child view, add the view to the end of the parent view
        return parent.childCount - 1
    }

    private fun moveViewToIndex(view: View, index: Int) {
        val parent = view.parent as ViewGroup
        parent.removeView(view)
        parent.addView(view, index)
    }

    private fun seeUsualPoll() {
        val question = poll.question
        val options = poll.options
        val linearLayout = binding.newPollLayout

        binding.questionPoll.text = question

        //add the options
        val radioGroup = RadioGroup(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        options.forEach { option ->
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
        binding.pollButton.visibility = View.VISIBLE

        //verify if the endDate has passed
        checkPollEndDateForUsualPoll(radioGroup, poll, linearLayout)

        //if the user already voted then disable the radio group
        Firebase.firestore.collection("polls").document("courses_polls_votes")
            .collection(course.id + poll.pollId)
            .whereEqualTo("votedBy", studentFirebaseId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // a document already exists -> disable the radio group
                    radioGroup.isEnabled = false
                    for (i in 0 until radioGroup.childCount) {
                        radioGroup.getChildAt(i).isEnabled = false
                    }
                    //set question text to grey
                    binding.questionPoll.setTextColor(ContextCompat.getColor(this, R.color.grey))
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
                        val textViewChoiceText = "Your choice: ${document.data["answer"]}"
                        textViewChoice.text = textViewChoiceText
                        textViewChoice.setTextColor(ContextCompat.getColor(this, R.color.grey))
                        binding.pollButton.visibility = View.GONE
                        linearLayout.addView(textViewChoice)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "Error retrieving poll votes", exception)
            }

        //make the vote button appear when an option is selected
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // get the selected radio button and its text
            val radioButton = group.findViewById<RadioButton>(checkedId)
            val selectedOption = radioButton.text.toString()
            val voteText = "VOTE"
            binding.pollButton.text = voteText
            binding.pollButton.setOnClickListener {
                // perform actions based on the selected option
                val pollCollectionRef =
                    Firebase.firestore
                        .collection("polls")
                        .document("courses_polls_votes")
                        .collection(course.id + poll.pollId)
                val pollAnswer = hashMapOf(
                    "answer" to selectedOption,
                    "votedBy" to studentFirebaseId
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

        //make possible to delete the poll if the current user created it
        deletePollIfCreatedByCurrentUser(binding.materialCard)
    }

    private fun checkPollEndDateForUsualPoll(
        radioGroup: RadioGroup,
        poll: Poll,
        linearLayout: LinearLayout
    ) {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val endDate = dateFormat.parse(poll.end_time)
        if (currentDate.before(endDate)) {
            // The current date is before the end date, so the views should be visible
            binding.questionPoll.visibility = View.VISIBLE
            binding.pollButton.visibility = View.VISIBLE
            radioGroup.visibility = View.VISIBLE
        } else {
            // The current date is after the end date, so the views should be hidden
            binding.questionPoll.visibility = View.GONE
            binding.pollButton.visibility = View.GONE
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
            val textViewInfoText = "POLL CLOSED"
            textViewInfo.text = textViewInfoText
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
            val textToDisplay = "Results for: ${binding.questionPoll.text} poll"
            textViewStatement.text = textToDisplay
            textViewStatement.setTextColor(ContextCompat.getColor(this, R.color.white))
            linearLayoutText.addView(textViewStatement)
            calculatePollResults(poll, linearLayoutText)
            cardView.addView(linearLayoutText)
        }
    }

    private fun calculatePollResults(poll: Poll, linearLayout: LinearLayout) {
        val pollVotesDatabase = Firebase.firestore
        val list = mutableListOf<String>()

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
                    val textViewAnswerText = "$value $count"
                    textViewAnswer.text = textViewAnswerText
                    textViewAnswer.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
                    linearLayout.addView(textViewAnswer)
                }
            }
    }

    private fun seePolls() {
        val intent = Intent(this, PollActivity::class.java)
        intent.putExtra("course", course)
        intent.putExtra("email", email)
        intent.putExtra("userId", studentFirebaseId)
        intent.putExtra("imageUrl", imageUrl)
        intent.putExtra("studentName", studentName)
        startActivity(intent)
    }

    private fun deletePollIfCreatedByCurrentUser(layout: LinearLayout) {
        if (poll.createdBy == studentFirebaseId) {
            val buttonDelete = Button(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 100 // set the margin top
                    leftMargin = 200
                    rightMargin = 200
                }
            }
            val deleteText = "DELETE POLL"
            buttonDelete.text = deleteText
            buttonDelete.setTextColor(ContextCompat.getColor(this, R.color.white))
            buttonDelete.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
            layout.addView(buttonDelete)

            buttonDelete.setOnClickListener {
                showConfirmationDialog()
            }
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        val newsCollectionRef = Firebase.firestore.collection("news")

        builder.setView(view)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        view.findViewById<Button>(R.id.delete_btn).setOnClickListener {

            if (poll.isFromLeader) {
                Firebase.firestore.collection("polls")
                    .document("courses_polls_votes_leader_polls")
                    .collection(course.id + poll.pollId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error deleting documents: ", exception)
                    }
            } else {
                Firebase.firestore.collection("polls")
                    .document("courses_polls_votes")
                    .collection(course.id + poll.pollId)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error deleting documents: ", exception)
                    }
            }
            Firebase.firestore.collection("polls")
                .document("courses_polls")
                .collection(course.id).document(poll.pollId).delete()
            Toast.makeText(
                this,
                "Poll deleted successfully",
                Toast.LENGTH_SHORT
            ).show()

            val currentTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedTime = dateFormat.format(Date(currentTime))
            val newsTitle = "Deleted Poll"
            val newsContent = "Poll " + poll.question + " was deleted from " + course.name + " group, by " + studentName
            val new = hashMapOf(
                "title" to newsTitle,
                "content" to newsContent,
                "time" to formattedTime,
                "courseId" to course.id
            )
            newsCollectionRef.document()
                .set(new, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "News created successfully")
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error creating news", exception)
                }

            seePolls()
            dialog.dismiss()
        }

        view.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}