package edu.licenta.uptconnect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.EnrollRequest

class AdminEnrollRequestsAdapter(
    private val context: Context,
    dataSet: FirestoreRecyclerOptions<EnrollRequest>
) :
    FirestoreRecyclerAdapter<EnrollRequest, AdminEnrollRequestsAdapter.ViewHolder>(dataSet) {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        val courseName = itemView.findViewById<TextView>(R.id.name_request_course)
        val studentName = itemView.findViewById<TextView>(R.id.name_request_student)
        val acceptButton = itemView.findViewById<Button>(R.id.accept)
        val declineButton = itemView.findViewById<Button>(R.id.decline)
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): AdminEnrollRequestsAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_enroll_approve, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        viewHolder: AdminEnrollRequestsAdapter.ViewHolder,
        position: Int,
        model: EnrollRequest
    ) {
        val requestsDatabase = Firebase.firestore

        viewHolder.courseName.text = model.courseName
        viewHolder.studentName.text = model.studentName

        viewHolder.declineButton.setOnClickListener() {
            requestsDatabase.collection("courseEnrollRequests").document(model.courseId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(
                        context,
                        "The request has been rejected",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        context,
                        "Failed to delete request",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}