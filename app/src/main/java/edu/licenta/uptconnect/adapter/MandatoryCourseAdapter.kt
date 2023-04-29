package edu.licenta.uptconnect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Course

class MandatoryCourseAdapter(
    private val dataSet: List<Course>
) :
    RecyclerView.Adapter<MandatoryCourseAdapter.ViewHolder>() {
    var onItemClick : ((Course) -> Unit)? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val courseName: TextView = itemView.findViewById(R.id.mandatory_course_name)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_mandatory_course, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.courseName.text = dataSet[position].name
        viewHolder.itemView.setOnClickListener() {
            onItemClick?.invoke(dataSet[position])
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}