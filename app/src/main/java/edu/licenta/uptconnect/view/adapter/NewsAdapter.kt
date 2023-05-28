package edu.licenta.uptconnect.view.adapter

import android.content.ContentValues
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.New

class NewsAdapter(
    options: FirestoreRecyclerOptions<New>,
) :
    FirestoreRecyclerAdapter<New, NewsAdapter.NewsViewHolder>(
        options
    ) {
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName = itemView.findViewById<TextView>(R.id.course_name)
        val courseContent = itemView.findViewById<LinearLayout>(R.id.course)

        fun setNewsDetails(
            newTitle: String,
            newContent: String,
            newTime: String,
            newsStudent: String
        ) {
            val newsTitleText = itemView.findViewById<TextView>(R.id.news_title)
            val newsContentText = itemView.findViewById<TextView>(R.id.news_content)
            val newsTimeText = itemView.findViewById<TextView>(R.id.created_date)
            val newsStudentText = itemView.findViewById<TextView>(R.id.student_name)
            newsTitleText.text = newTitle
            newsContentText.text = newContent
            newsTimeText.text = newTime
            newsStudentText.text = newsStudent
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_new, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int, news: New) {
        holder.setNewsDetails(news.title, news.content, news.time, news.createdBy)

        Firebase.firestore.collection("courses").document(news.courseId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    holder.courseName.text = documentSnapshot.data!!["Name"].toString()
                } else {
                    holder.courseContent.visibility = View.INVISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving course", exception)
            }
    }
}