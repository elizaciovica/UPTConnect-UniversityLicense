package edu.licenta.uptconnect.view.activity

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private var imageUri: Uri? = null
    private var chosenFaculty: String = ""
    private var chosenSection: String = ""
    private var chosenYear: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        val firebaseId = FirebaseAuth.getInstance().currentUser?.uid

        super.onCreate(savedInstanceState)
        setBinding()
        setDropDowns()
        setProfileImage(firebaseId!!)
    }

    private fun setBinding() {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun setDropDowns() {
        val facultyLists = listOf("AC", "")
        val autoComplete: AutoCompleteTextView = binding.autoComplete
        val adapter = ArrayAdapter(this, R.layout.facultieslist_item, facultyLists)
        autoComplete.setAdapter(adapter)
        autoComplete.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                val itemSelected = adapterView.getItemAtPosition(i)
                chosenFaculty = itemSelected.toString()
                binding.autoCompleteSection.setAdapter(
                    ArrayAdapter<String>(
                        this,
                        R.layout.facultieslist_item,
                        emptyList()
                    )
                )
                binding.autoCompleteSection.setText("")
                binding.autoCompleteYear.setAdapter(
                    ArrayAdapter<String>(
                        this,
                        R.layout.facultieslist_item,
                        emptyList()
                    )
                )
                binding.autoCompleteYear.setText("")
                setSectionDropDown()
            }

    }

    private fun setSectionDropDown() {
        val sectionListsForAC = listOf("CTI", "CTI-EN", "IS", "Informatics")
        if (chosenFaculty == "AC") {
            val autoComplete2: AutoCompleteTextView = binding.autoCompleteSection
            val adapter2 = ArrayAdapter(this, R.layout.facultieslist_item, sectionListsForAC)
            autoComplete2.setAdapter(adapter2)
            autoComplete2.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                    val itemSelected2 = adapterView.getItemAtPosition(i)
                    chosenSection = itemSelected2.toString()
                    binding.autoCompleteYear.setAdapter(
                        ArrayAdapter<String>(
                            this,
                            R.layout.facultieslist_item,
                            emptyList()
                        )
                    )
                    binding.autoCompleteYear.setText("")
                    setYearDropDown()
                }
        }
    }

    private fun setYearDropDown() {
        val sectionList = listOf(1, 2, 3, 4)
        val sectionListForInformatics = listOf(1, 2, 3)
        if (chosenSection == "Informatics") {
            val autoComplete3: AutoCompleteTextView = binding.autoCompleteYear
            val adapter3 =
                ArrayAdapter(this, R.layout.facultieslist_item, sectionListForInformatics)
            autoComplete3.setAdapter(adapter3)
            autoComplete3.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                    val itemSelected3 = adapterView.getItemAtPosition(i)
                    chosenYear = itemSelected3.toString()
                }
        } else {
            val autoComplete3: AutoCompleteTextView = binding.autoCompleteYear
            val adapter3 = ArrayAdapter(this, R.layout.facultieslist_item, sectionList)
            autoComplete3.setAdapter(adapter3)
            autoComplete3.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                    val itemSelected3 = adapterView.getItemAtPosition(i)
                    chosenYear = itemSelected3.toString()
                }
        }
    }

    private fun verifyInputDetails(): Boolean {
        if (!binding.firstName.text.toString().all { char -> char.isLetter() }) {
            Toast.makeText(
                this,
                "Please enter a valid First Name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (!binding.lastName.text.toString().all { char -> char.isLetter() }) {
            Toast.makeText(
                this,
                "Please enter a valid Last Name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun setProfileImage(studentUid: String) {

        val email: String = intent.getStringExtra("email").toString()
        val firebaseUser = intent.getStringExtra("userId").toString()

        binding.uploadProfilePicture.setOnClickListener {
            selectImage()
        }
        binding.setProfile.setOnClickListener {
            val validDetails = verifyInputDetails()
            if (chosenFaculty.isEmpty() || chosenSection.isEmpty()
                || chosenYear.isEmpty() || imageUri == null || !validDetails
            ) {
                Toast.makeText(
                    this,
                    "All the fields are required",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val fireStoreDb = Firebase.firestore
                val getMandatoryCoursesTask = fireStoreDb.collection("courses")
                    .whereEqualTo("Section", chosenSection)
                    .whereEqualTo("Year", chosenYear)
                    .whereEqualTo("Mandatory", true)
                    .get()

                val updateStudentsMandatoryCoursesTask = getMandatoryCoursesTask.continueWithTask { task ->
                    val mandatoryCoursesList = mutableListOf<String>()
                    for(document in task.result.documents) {
                        mandatoryCoursesList += document.id
                        Firebase.messaging.subscribeToTopic(document.id)
                            .addOnCompleteListener { task ->
                                var msg = "Subscribed"
                                if (!task.isSuccessful) {
                                    msg = "Subscribe failed"
                                }
                                Log.d(TAG, msg)
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                    }

                    fireStoreDb.collection("students").document(studentUid)
                        .update(
                            "FirstName", binding.firstName.text.toString(),
                            "LastName", binding.lastName.text.toString(),
                            "Faculty", chosenFaculty,
                            "Section", chosenSection,
                            "StudyYear", chosenYear,
                            "IsAdmin", false,
                            "acceptedCourses", FieldValue.arrayUnion(*mandatoryCoursesList.toTypedArray())
                        )
                }

                val uploadImageTask = updateStudentsMandatoryCoursesTask.continueWithTask {
                    val fileName = "profileImage$email"
                    val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
                    storageReference.putFile(imageUri!!)
                }

                uploadImageTask.addOnSuccessListener {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("userId", firebaseUser)
                    intent.putExtra("email", email)
                    startActivity(intent)
                }.addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Something went wrong",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent, 100)
    }

    //this method will be called when an user selects an image from the gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            binding.profileImage.setImageURI(imageUri)

        }
    }
}