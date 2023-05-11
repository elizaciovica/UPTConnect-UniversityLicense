package edu.licenta.uptconnect.util

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.model.PollChoice
import edu.licenta.uptconnect.model.ScheduleData
import java.text.SimpleDateFormat
import java.util.*

class CoursePlacesAssignmentUtil {

    fun getDataFromFirebaseAndRearangeItBasedOnTimeStamp(
        courseId: String,
        pollId: String
    ): Task<MutableList<PollChoice>> {
        val tcs = TaskCompletionSource<MutableList<PollChoice>>()
        var listOfPollChoices = mutableListOf<PollChoice>()
        var finalListOfPollChoices = mutableListOf<PollChoice>()
        val getStudentsPollChoicesTask =
            Firebase.firestore.collection("polls").document("courses_polls_votes_leader_polls")
                .collection(courseId + pollId)
                .get()
        val populateListOfChoicesTask = getStudentsPollChoicesTask.continueWithTask { task ->
            for (document in task.result.documents) {
                val pollChoiceId = document.id
                val pollData = document.data
                val pollTimeOfVote = pollData!!["timeOfVote"] as String
                val pollVotedBy = pollData["votedBy"] as String
                val pollOptionOrder = pollData["optionOrder"] as HashMap<String, Int>
                val pollChoice =
                    PollChoice(pollChoiceId, pollTimeOfVote, pollVotedBy, pollOptionOrder)
                listOfPollChoices.add(pollChoice)
            }
            return@continueWithTask Tasks.forResult(null)
        }

        populateListOfChoicesTask
            .addOnSuccessListener {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                //sort the list to start from the first student who submitted its vote
                finalListOfPollChoices =
                    listOfPollChoices.sortedBy { dateFormat.parse(it.timeOfVote) } as MutableList<PollChoice>
                tcs.setResult(finalListOfPollChoices)
            }
            .addOnFailureListener {
                tcs.setException(it)
            }
        return tcs.task
    }

    fun getLabHoursFromTheSchedule(
        courseId: String,
        courseType: String
    ): Task<MutableList<ScheduleData>> {
        val tcs = TaskCompletionSource<MutableList<ScheduleData>>()
        val schedules = mutableListOf<ScheduleData>()

        val scheduleCollectionRefTask =
            Firebase.firestore.collection("schedules")
                .document("courses")
                .collection(courseId).document(courseType)
                .get()

        scheduleCollectionRefTask
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    //scheduleList object returned by the get() method is actually a list of HashMap objects, where each HashMap represents a single ScheduleData object
                    val scheduleList =
                        documentSnapshot.get("scheduleList") as ArrayList<java.util.HashMap<String, Any>>

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
                    tcs.setResult(schedules)
                }
            }
            .addOnFailureListener {
                tcs.setException(it)
            }

        return tcs.task
    }

    //labHours will contain lab hour and no of available places for students
    //the function will return a HashMap<String, List<String>>
    fun pollResultsAlgorithm(
        pollChoices: MutableList<PollChoice>,
        schedules: MutableList<ScheduleData>
    ): HashMap<ScheduleData, MutableList<String>> {
        //this will map each student that voted to an empty ScheduleData object
        val students = HashMap<PollChoice, ScheduleData?>()
        for (poll in pollChoices) {
            students[poll] = null
        }

        val comparator = Comparator<PollChoice> { p1, p2 ->
            p1.timeOfVote.compareTo(p2.timeOfVote)
        }
        val sortedStudents = students.toSortedMap(comparator)

        //while there is a student that is not yet assigned to a non empty ScheduleData, the algorithm stands
        while (existsStudentsWithoutAssignment(sortedStudents)) {
            for ((studentPollChoiceDataObject, scheduleDataObject) in sortedStudents) {
                if (scheduleDataObject == null) {
                    val sortedOptions =
                        studentPollChoiceDataObject.optionOrder.entries.sortedBy { it.value.toLong() }
                    val sortedHashMap = sortedOptions.associate { it.toPair() }
                    var assignedStudent = false
                    for (optionText in sortedHashMap) {
                        for (schedule in schedules) {
                            val availablePlaces = schedule.maxNoOfStudents.toInt()
                            val currentOption =
                                schedule.day + " " + schedule.startTime + " " + schedule.endTime

                            if (optionText.key == currentOption && availablePlaces > 0) {
                                sortedStudents[studentPollChoiceDataObject] = schedule
                                schedule.maxNoOfStudents =
                                    (schedule.maxNoOfStudents.toInt() - 1).toString()
                                assignedStudent = true
                                break
                            }
                        }
                        if (assignedStudent) {
                            break
                        }
                    }
                }
            }
        }

        //this will be returned
        //this will be saved in firebase in "coursesRepartition" -> "course_id" ->

        val uniqueSchedules = hashSetOf<ScheduleData>()
        for (student in sortedStudents) {
            student.value?.let { uniqueSchedules.add(it) }
        }

        val courseRepartition = HashMap<ScheduleData, MutableList<String>>()

        for (uniqueSchedule in uniqueSchedules) {
            courseRepartition[uniqueSchedule] = mutableListOf()
        }

        for (student in sortedStudents) {
            courseRepartition[student.value]?.add(student.key.votedBy)
        }

        return courseRepartition
    }


    private fun existsStudentsWithoutAssignment(students: SortedMap<PollChoice, ScheduleData?>): Boolean {
        if (students.containsValue(null)) {
            return true
        }
        return false
    }
}