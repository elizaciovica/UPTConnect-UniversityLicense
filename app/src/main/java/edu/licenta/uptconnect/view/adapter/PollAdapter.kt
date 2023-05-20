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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Poll
import java.text.SimpleDateFormat
import java.util.*

private const val CLICKABLE_VIEW_TYPE = 1
private const val NON_CLICKABLE_VIEW_TYPE = 2

class PollAdapter(
    options: FirestoreRecyclerOptions<Poll>
) :
    FirestoreRecyclerAdapter<Poll, PollAdapter.PollViewHolder>(
        options
    ) {
    var onItemClick: ((Poll) -> Unit)? = null

    inner class PollViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.poll)
        val context: Context = itemView.context
        val question = itemView.findViewById<TextView>(R.id.question)
        val student = itemView.findViewById<TextView>(R.id.student_who_created)
        val endDate = itemView.findViewById<TextView>(R.id.end_date)
        val openUntilTextView = itemView.findViewById<TextView>(R.id.open_until)

        fun setPollDetails(questionText: String, studentText: String,
        endDateText: String) {
            question.text = questionText
            student.text = studentText
            endDate.text = endDateText
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int): PollViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_poll, parent, false)
        return PollViewHolder(view)
    }

    override fun onBindViewHolder(
        pollViewHolder: PollViewHolder,
        position: Int,
        poll: Poll
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()

        pollViewHolder.setPollDetails(poll.question, poll.createdBy, poll.end_time)

        val studentDoc =
            Firebase.firestore.collection("students").document(poll.createdBy)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    pollViewHolder.student.text =
                        documentSnapshot.getString("FirstName") + " " + documentSnapshot.getString("LastName")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
        pollViewHolder.question.text = poll.question

        if (dateFormat.format(Date(currentTime)) > poll.end_time) {
            var closedText = "CLOSED"
            pollViewHolder.openUntilTextView.text = closedText
            var detailsText = "click for more details"
            pollViewHolder.endDate.text = detailsText

            pollViewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    pollViewHolder.itemView.context,
                    R.color.grey2
                )
            )

        } else {
            pollViewHolder.endDate.text = poll.end_time
        }

        if(dateFormat.format(Date(currentTime)) < poll.start_time) {
            pollViewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    pollViewHolder.itemView.context,
                    R.color.grey2
                )
            )

            var closedText = "OPENS IN "
            pollViewHolder.openUntilTextView.text = closedText
            pollViewHolder.endDate.text = poll.start_time
        }

        when (pollViewHolder.itemViewType) {
            CLICKABLE_VIEW_TYPE -> {
                // Set up click listener for clickable items
                pollViewHolder.itemView.setOnClickListener {
                    onItemClick?.invoke(poll) //hmmm
                }
            }
            NON_CLICKABLE_VIEW_TYPE -> {
                // Disable click listener for non-clickable items
                pollViewHolder.itemView.setOnClickListener(null)
                val currentUser = Firebase.auth.currentUser!!.uid
                if(currentUser == poll.createdBy) {
                    pollViewHolder.itemView.setOnClickListener {
                        onItemClick?.invoke(poll)
                    }
                }
            }
        }

        if (poll.isFromLeader) {
            val textAdmin = pollViewHolder.itemView.findViewById<TextView>(R.id.admin)
            pollViewHolder.linearLayout.setBackgroundColor(
                ContextCompat.getColor(
                    pollViewHolder.itemView.context,
                    R.color.red3
                )
            )
            textAdmin.visibility = View.VISIBLE
        }
    }

    override fun getItemViewType(position: Int): Int {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = System.currentTimeMillis()
        val poll = getItem(position) // Get the Poll object at the specified position
        // Return different view types based on the position of the item
        return if (dateFormat.format(Date(currentTime)) > poll.start_time) {
            CLICKABLE_VIEW_TYPE
        } else {
            NON_CLICKABLE_VIEW_TYPE
        }
    }
}