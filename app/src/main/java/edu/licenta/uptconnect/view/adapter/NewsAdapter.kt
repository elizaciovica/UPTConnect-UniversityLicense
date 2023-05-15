package edu.licenta.uptconnect.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.New

class NewsAdapter(
    options: FirestoreRecyclerOptions<New>,
) :
    FirestoreRecyclerAdapter<New, NewsAdapter.NewsViewHolder>(
        options
    ) {
    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setNewsDetails(newTitle: String, newContent: String, newTime: String) {
            val newsTitleText = itemView.findViewById<TextView>(R.id.news_title)
            val newsContentText = itemView.findViewById<TextView>(R.id.news_content)
            val newsTimeText = itemView.findViewById<TextView>(R.id.created_date)
            newsTitleText.text = newTitle
            newsContentText.text = newContent
            newsTimeText.text = newTime
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
        holder.setNewsDetails(news.title, news.content, news.time)
    }
}