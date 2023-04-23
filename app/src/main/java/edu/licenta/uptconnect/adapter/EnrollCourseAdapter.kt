package edu.licenta.uptconnect.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Course

class EnrollCourseAdapter(
    private val dataSet: List<Course>
) :
    RecyclerView.Adapter<EnrollCourseAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        val courseName = itemView.findViewById<TextView>(R.id.name)
        val enrollButton = itemView.findViewById<Button>(R.id.enroll)
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
            viewHolder.enrollButton.setBackgroundColor(viewHolder.itemView.resources.getColor(R.color.grey))
            viewHolder.enrollButton.isClickable = false
            viewHolder.enrollButton.text = "SENT"
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}