package edu.licenta.uptconnect.adapter

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Poll

class PollAdapter(
    private val dataSet: List<Poll>
) :
    RecyclerView.Adapter<PollAdapter.ViewHolder>() {
    var onItemClick: ((Poll) -> Unit)? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val question: TextView = itemView.findViewById(R.id.question)
        val student: TextView = itemView.findViewById(R.id.student_who_created)
        val endDate: TextView = itemView.findViewById(R.id.end_date)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_poll, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val studentDoc =
            Firebase.firestore.collection("students").document(dataSet[position].createdBy)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    viewHolder.student.text =
                        documentSnapshot.getString("FirstName") + documentSnapshot.getString("LastName")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
        viewHolder.question.text = dataSet[position].question
        viewHolder.endDate.text = dataSet[position].end_time

        viewHolder.itemView.setOnClickListener() {
            onItemClick?.invoke(dataSet[position])
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}