package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import edu.licenta.uptconnect.view.adapter.ChatAdapter
import edu.licenta.uptconnect.databinding.ActivityChatBinding
import edu.licenta.uptconnect.model.Chat
import edu.licenta.uptconnect.model.Course
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var recyclerView: RecyclerView
    val currentStudentFirebaseId = FirebaseAuth.getInstance().currentUser?.uid
    var chatList = ArrayList<Chat>()
    var receiver: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
        recyclerView = binding.chatRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val course = intent.getParcelableExtra<Course>("course")!!
        receiver = course.id
        readMessage(currentStudentFirebaseId!!, receiver)

        binding.chatName.text = course.Name
    }

    private fun setBinding() {
        binding = ActivityChatBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun initializeButtons() {
        binding.sendButton.setOnClickListener {
            val messageSend: EditText = binding.messageSend
            val message: String = messageSend.text.toString()
            if (message != "") {
                sendMessage(currentStudentFirebaseId!!, receiver, message)
            } else {
                Toast.makeText(
                    this@ChatActivity,
                    "You can't send an empty message",
                    Toast.LENGTH_SHORT
                ).show()
            }
            messageSend.setText("")
        }
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(Date(currentTime))

        val chatMessage = hashMapOf(
            "sender" to sender,
            "receiver" to receiver,
            "message" to message,
            "timestamp" to formattedTime
        )

        reference.child("Chats").push().setValue(chatMessage)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chats")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.d(ContentValues.TAG, "Error: $error")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)
                    if (chat!!.sender == senderId && chat.receiver == receiverId ||
                        chat.sender != senderId && chat.receiver == receiverId
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)

                recyclerView.adapter = chatAdapter
            }
        })
    }
}