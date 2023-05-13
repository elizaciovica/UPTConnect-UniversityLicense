package edu.licenta.uptconnect.view.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.EnrollRequest

class AdminEnrollRequestsAdapter(
    options: FirestoreRecyclerOptions<EnrollRequest>,
) :
    FirestoreRecyclerAdapter<EnrollRequest, AdminEnrollRequestsAdapter.AdminEnrollRequestsViewHolder>(
        options
    ) {
    inner class AdminEnrollRequestsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setRequestDetails(courseNameText: String, studentNameText: String) {
            val courseName = itemView.findViewById<TextView>(R.id.name_request_course)
            val studentName = itemView.findViewById<TextView>(R.id.name_request_student)
            courseName.text = courseNameText
            studentName.text = studentNameText
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdminEnrollRequestsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_enroll_approve, parent, false)
        return AdminEnrollRequestsViewHolder(view)
    }

    override fun onBindViewHolder(
        requestViewHolder: AdminEnrollRequestsViewHolder,
        position: Int,
        request: EnrollRequest
    ) {
        requestViewHolder.setRequestDetails(request.courseName, request.studentName)

        val requestsDatabase = Firebase.firestore
        val declineButton = requestViewHolder.itemView.findViewById<Button>(R.id.decline)
        println(request.section)

        declineButton.setOnClickListener {

            requestsDatabase.collection("courseEnrollRequests").document("sections")
                .collection(request.section).document(request.courseId + request.studentId)
                .delete()
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Failed to delete record", exception)
                }
        }

        val acceptButton = requestViewHolder.itemView.findViewById<Button>(R.id.accept)
        acceptButton.setOnClickListener {
            val studentsDatabase = Firebase.firestore
            val studentDoc = studentsDatabase.collection("students").document(request.studentId)
            var acceptedCourseList = listOf<String>()
            acceptedCourseList += request.courseId
            studentDoc.update(
                "acceptedCourses",
                FieldValue.arrayUnion(*acceptedCourseList.toTypedArray())
            )

            //also delete the request from the request collection
            requestsDatabase.collection("courseEnrollRequests").document("sections")
                .collection(request.section).document(request.courseId + request.studentId)
                .delete()
                .addOnSuccessListener {
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Error deleting request", exception)
                }
        }
    }
}
