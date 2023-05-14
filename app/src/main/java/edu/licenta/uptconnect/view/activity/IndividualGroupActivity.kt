package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.os.Bundle
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityIndividualGroupBinding
import edu.licenta.uptconnect.model.Course

class IndividualGroupActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityIndividualGroupBinding

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getExtrasFromIntent()
        setProfileDetails()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityIndividualGroupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
    }

    private fun initializeButtons() {
        val course = intent.getParcelableExtra<Course>("course")!!
        binding.chatCard.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            startActivity(intent)
        }

        binding.pollsCard.setOnClickListener {
            val intent = Intent(this, PollActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }

        binding.changesCard.setOnClickListener {
            val intent = Intent(this, ChangesActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("email", email)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            startActivity(intent)
        }
    }
}