package edu.licenta.uptconnect.adapter

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.EnrollRequest

class AdminEnrollRequestsAdapter(
    private val dataSet: List<EnrollRequest>
) :
    RecyclerView.Adapter<AdminEnrollRequestsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        val courseName = itemView.findViewById<TextView>(R.id.name_request_course)
        val studentName = itemView.findViewById<TextView>(R.id.name_request_student)
        val acceptButton = itemView.findViewById<Button>(R.id.accept)
        val declineButton = itemView.findViewById<Button>(R.id.decline)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): AdminEnrollRequestsAdapter.ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_enroll_approve, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: AdminEnrollRequestsAdapter.ViewHolder, position: Int) {
        val requestsDatabase = Firebase.firestore

        viewHolder.courseName.text = dataSet[position].courseId
        viewHolder.studentName.text = dataSet[position].studentId

        viewHolder.declineButton.setOnClickListener() {
            requestsDatabase.collection("courseEnrollRequests").document(dataSet[position].courseId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DRequest successfully deleted!")
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}