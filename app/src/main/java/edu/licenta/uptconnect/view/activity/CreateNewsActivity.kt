package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityCreateNewsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class CreateNewsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityCreateNewsBinding

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
        createNews()
    }

    private fun setBinding() {
        binding = ActivityCreateNewsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun createNews() {
        val array = intent.getStringArrayExtra("coursesIds")
        val groupMap = HashMap<String, String>()
        var chosenGroupId = ""
        val isLeader = intent.getBooleanExtra("isLeader", false)

        runBlocking {
            val deferredResults = array?.map { item ->
                async(Dispatchers.IO) {
                    val documentSnapshot =
                        Firebase.firestore.collection("courses").document(item).get().await()
                    documentSnapshot.data!!["Name"].toString() to item
                }
            }

            deferredResults?.map { deferredResult ->
                val (item, name) = deferredResult.await()
                groupMap[item] = name
            }
        }

        val groupList = ArrayList(groupMap.keys)
        val autoCompleteGroup: AutoCompleteTextView = binding.autoCompleteGroup
        val adapterGroup = ArrayAdapter(this, R.layout.facultieslist_item, groupList)
        autoCompleteGroup.setAdapter(adapterGroup)
        autoCompleteGroup.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val groupSelected = adapterView.getItemAtPosition(i)
                chosenGroupId = groupMap[groupSelected.toString()].toString()
            }


        if (isLeader) {
            val getGroupsTask = Firebase.firestore.collection("courses")
                .get()

            val populateDropDownTask = getGroupsTask.continueWithTask { task ->
                for (document in task.result.documents) {
                    groupMap[document.data!!["Name"].toString()] = document.id
                }
                return@continueWithTask Tasks.forResult(null)
            }

            populateDropDownTask.addOnSuccessListener {
                val groupListLeader = ArrayList(groupMap.keys)
                val autoCompleteGroupLeader: AutoCompleteTextView = binding.autoCompleteGroup
                val adapterGroupLeader =
                    ArrayAdapter(this, R.layout.facultieslist_item, groupListLeader)

                autoCompleteGroupLeader.setAdapter(adapterGroupLeader)
                autoCompleteGroupLeader.onItemClickListener =
                    AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                        val groupSelected = adapterView.getItemAtPosition(i)
                        chosenGroupId = groupMap[groupSelected.toString()].toString()
                    }
            }
        }

        binding.publish.setOnClickListener {
            val title = binding.createTitleOfNews.text.toString()
            val content = binding.createContentOfNews.text.toString()

            if (chosenGroupId.isEmpty() || title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            } else {
                val currentTime = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedTime = dateFormat.format(Date(currentTime))
                val new = hashMapOf(
                    "title" to "$title",
                    "content" to content,
                    "time" to formattedTime,
                    "courseId" to chosenGroupId,
                    "createdBy" to studentName
                )

                Firebase.firestore.collection("news")
                    .document()
                    .set(new, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Publish finished successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .addOnFailureListener { exception ->
                        Log.d(ContentValues.TAG, "Error creating news", exception)
                    }
                seeNews()
                finish()
            }

        }
    }

    private fun seeNews() {
        val intent = Intent(this, NewsActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("userId", studentFirebaseId)
        intent.putExtra("imageUrl", imageUrl)
        intent.putExtra("studentName", studentName)
        startActivity(intent)
    }
}