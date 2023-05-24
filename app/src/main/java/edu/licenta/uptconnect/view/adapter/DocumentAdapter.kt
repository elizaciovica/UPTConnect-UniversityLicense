package edu.licenta.uptconnect.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Document

class DocumentAdapter(private var items: List<Document>) :
    RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = itemView.context
        val documentTextView: TextView = itemView.findViewById(R.id.document_name_recycler)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_document, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: DocumentAdapter.ViewHolder, position: Int) {
        val item: Document = items[position]
        viewHolder.documentTextView.text = item.documentName
        viewHolder.itemView.setOnClickListener {
            onItemClick?.invoke(item)

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    var onItemClick: ((Document) -> Unit)? = null

}