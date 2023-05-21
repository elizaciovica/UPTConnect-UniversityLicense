package edu.licenta.uptconnect.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Course

class CourseAdapter(
    options: FirestoreRecyclerOptions<Course>
) :
    FirestoreRecyclerAdapter<Course, CourseAdapter.CourseViewHolder>(
        options
    ) {
    var onItemClick: ((Course) -> Unit)? = null

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.mandatory_course_name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CourseViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mandatory_course, parent, false)

        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(
        courseViewHolder: CourseViewHolder,
        position: Int,
        course: Course
    ) {
        courseViewHolder.courseName.text = course.Name
        courseViewHolder.itemView.setOnClickListener() {
            onItemClick?.invoke(course)
        }
    }
}