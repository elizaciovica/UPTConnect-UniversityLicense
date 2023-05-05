package edu.licenta.uptconnect

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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.adapter.ChatAdapter
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
    var receiver : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeButtons()
        recyclerView = binding.chatRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val course = intent.getParcelableExtra<Course>("course")!!
        receiver = course.id
        readMessage(currentStudentFirebaseId!!, receiver)

        binding.chatName.text = course.name
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

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["timestamp"] = formattedTime

        reference.child("Chats").push().setValue(hashMap)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("Chats")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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