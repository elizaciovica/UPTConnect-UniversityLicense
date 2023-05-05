package edu.licenta.uptconnect.view.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.CourseEnrollRequest
import edu.licenta.uptconnect.model.CourseEnrollRequestStatus

class EnrollCourseAdapter(
    private val dataSet: List<CourseEnrollRequest>
) :
    RecyclerView.Adapter<EnrollCourseAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        val courseName = itemView.findViewById<TextView>(R.id.name)
        val enrollButton = itemView.findViewById<Button>(R.id.enroll)
        val cancelButton = itemView.findViewById<Button>(R.id.cancel)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_course, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.courseName.text = dataSet[position].name
        viewHolder.enrollButton.setOnClickListener() {
            //send request to admin
            changeButtons(position, viewHolder)
            createCourseEnrollRequest(
                dataSet[position].id,
                dataSet[position].studentId,
                dataSet[position].studentName,
                dataSet[position].name
            )
        }

        viewHolder.cancelButton.setOnClickListener() {
            revertButtons(position, viewHolder)
            cancelCourseEnrollRequest(dataSet[position].id)
        }

        verifyIfRequestHasBeenUpdated(position, viewHolder)
    }

    private fun changeButtons(position: Int, viewHolder: ViewHolder) {
        if (position == viewHolder.adapterPosition) {
            viewHolder.enrollButton.setBackgroundColor(viewHolder.itemView.resources.getColor(R.color.grey))
            viewHolder.enrollButton.isClickable = false
            viewHolder.enrollButton.text = "SENT"
            viewHolder.cancelButton.visibility = View.VISIBLE
        }
    }

    private fun revertButtons(position: Int, viewHolder: ViewHolder) {
        if (position == viewHolder.adapterPosition) {
            viewHolder.enrollButton.setBackgroundColor(viewHolder.itemView.resources.getColor(R.color.green))
            viewHolder.enrollButton.isClickable = true
            viewHolder.enrollButton.text = "ENROLL"
            viewHolder.cancelButton.visibility = View.INVISIBLE
        }
    }

    private fun createCourseEnrollRequest(
        courseId: String,
        studentId: String,
        studentName: String,
        courseName: String
    ) {
        val courseEnrollRequestsDatabase = Firebase.firestore
        val courseEnrollRequest = hashMapOf(
            "courseId" to courseId,
            "studentId" to studentId,
            "courseEnrollRequestStatus" to CourseEnrollRequestStatus.SENT,
            "studentName" to studentName,
            "courseName" to courseName
        )
        courseEnrollRequestsDatabase.collection("courseEnrollRequests").document(courseId)
            .set(courseEnrollRequest, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    ContentValues.TAG,
                    "Course Enroll Request Document successfully created!"
                )
            }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error creating Document", e) }
    }

    private fun cancelCourseEnrollRequest(courseId: String) {
        val courseEnrollRequestsDatabase = Firebase.firestore
        courseEnrollRequestsDatabase.collection("courseEnrollRequests").document(courseId)
            .update(
                "courseEnrollRequestStatus", CourseEnrollRequestStatus.CANCELED
            )
    }

    private fun verifyIfRequestHasBeenUpdated(position: Int, viewHolder: ViewHolder) {
        val courseEnrollRequestsDatabase = Firebase.firestore
        val courseEnrollRequest = courseEnrollRequestsDatabase.collection("courseEnrollRequests")
        courseEnrollRequest.whereEqualTo("studentId", dataSet[position].studentId)
            .whereEqualTo("courseId", dataSet[position].id)
            .whereEqualTo("courseEnrollRequestStatus", CourseEnrollRequestStatus.SENT.toString())
            .get()
            .addOnSuccessListener { querySnapshot ->
                val courseEnrollRequest =
                    querySnapshot.documents.firstOrNull()?.toObject(CourseEnrollRequest::class.java)
                if (courseEnrollRequest != null) {
                    changeButtons(position, viewHolder)
                }
            }
            .addOnFailureListener() {
            }

        courseEnrollRequest.whereEqualTo("studentId", dataSet[position].studentId)
            .whereEqualTo("courseId", dataSet[position].id)
            .whereEqualTo(
                "courseEnrollRequestStatus",
                CourseEnrollRequestStatus.CANCELED.toString()
            )
            .get()
            .addOnSuccessListener { querySnapshot ->
                val courseEnrollRequest =
                    querySnapshot.documents.firstOrNull()?.toObject(CourseEnrollRequest::class.java)
                if (courseEnrollRequest != null) {
                    revertButtons(position, viewHolder)
                }
            }
            .addOnFailureListener() {
            }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}