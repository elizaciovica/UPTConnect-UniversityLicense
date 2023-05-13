package edu.licenta.uptconnect.view.adapter

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Poll
import java.text.SimpleDateFormat
import java.util.*

private const val CLICKABLE_VIEW_TYPE = 1
private const val NON_CLICKABLE_VIEW_TYPE = 2

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
        val linearLayout: LinearLayout = itemView.findViewById(R.id.poll)
        val openUntilTextView: TextView = itemView.findViewById(R.id.open_until)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_poll, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        val studentDoc =
            Firebase.firestore.collection("students").document(dataSet[position].createdBy)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    viewHolder.student.text =
                        documentSnapshot.getString("FirstName") + " " + documentSnapshot.getString("LastName")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
        viewHolder.question.text = dataSet[position].question

        if (dateFormat.format(Date(currentTime)) > dataSet[position].end_time) {
            var closedText = "CLOSED"
            viewHolder.openUntilTextView.text = closedText
            var detailsText = "click for more details"
            viewHolder.endDate.text = detailsText

            viewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.grey2
                )
            )

        } else {
            viewHolder.endDate.text = dataSet[position].end_time
        }

        if(dateFormat.format(Date(currentTime)) < dataSet[position].start_time) {
            viewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.grey2
                )
            )

            var closedText = "OPENS IN "
            viewHolder.openUntilTextView.text = closedText
            viewHolder.endDate.text = dataSet[position].start_time
        }

        when (viewHolder.itemViewType) {
            CLICKABLE_VIEW_TYPE -> {
                // Set up click listener for clickable items
                viewHolder.itemView.setOnClickListener {
                    onItemClick?.invoke(dataSet[position])
                }
            }
            NON_CLICKABLE_VIEW_TYPE -> {
                // Disable click listener for non-clickable items
                viewHolder.itemView.setOnClickListener(null)
            }
        }

        if (dataSet[position].isFromLeader) {
            val textAdmin = viewHolder.itemView.findViewById<TextView>(R.id.admin)
            viewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    viewHolder.itemView.context,
                    R.color.red3
                )
            )
            textAdmin.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getItemViewType(position: Int): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        // Return different view types based on the position of the item
        return if (dateFormat.format(Date(currentTime)) > dataSet[position].start_time) {
            CLICKABLE_VIEW_TYPE
        } else {
            NON_CLICKABLE_VIEW_TYPE
        }
    }
}