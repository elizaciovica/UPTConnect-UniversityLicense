package edu.licenta.uptconnect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.ChatActivity
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.model.Chat

class ChatAdapter(private val context: Context, private val chatList: ArrayList<Chat>)
    : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MESSAGE_TYPE_RIGHT) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
            ViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_chat_left, parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chatList[position]
        holder.txtUserName.text = chat.message
        holder.txtTimestamp.text = chat.timestamp

        val studentsDatabase = Firebase.firestore
        studentsDatabase.collection("students")
            .whereEqualTo("Uid", chat.sender)
            .get()
            .addOnSuccessListener { documents ->
                for(document in documents) {
                    holder.txtStudentSenderName.text = document.data["FirstName"].toString() + " " + document.data["LastName"].toString()
                }
            }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtUserName: TextView = view.findViewById(R.id.show_message)
        var txtStudentSenderName: TextView = view.findViewById(R.id.sender_student)
        var txtTimestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun getItemViewType(position: Int): Int {
        return if(chatList[position].sender == ChatActivity().currentStudentFirebaseId) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }

}