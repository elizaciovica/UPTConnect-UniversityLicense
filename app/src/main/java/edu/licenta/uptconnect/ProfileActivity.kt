package edu.licenta.uptconnect

import android.R
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import edu.licenta.uptconnect.databinding.ActivityProfileBinding


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        val firebaseId = FirebaseAuth.getInstance().currentUser?.uid

        super.onCreate(savedInstanceState)
        setBinding()
        setProfileImage(firebaseId!!)
    }

    private fun setBinding() {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun setProfileImage(studentUid:String) {
        binding.uploadProfilePicture.setOnClickListener() {
            selectImage()
        }
        binding.setProfile.setOnClickListener() {
            uploadProfileImage()

            val studentsDatabase = Firebase.firestore
            studentsDatabase.collection("students").document(studentUid)
                .update(
                    "FirstName", binding.firstName.text.toString(),
                    "LastName", binding.lastName.text.toString(),
                    "Faculty", binding.facultyName.text.toString(),
                    "Section", binding.sectionName.text.toString(),
                    "StudyYear", binding.studyYear.text.toString(),
                    "RegistrationNumber", binding.registrationNumber.text.toString()
                )

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadProfileImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Picture ...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val email: String = intent.getStringExtra("email").toString()

        val fileName = "profileImage$email"

        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Image Successfully Set", Toast.LENGTH_SHORT).show()
                if(progressDialog.isShowing) progressDialog.dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                if(progressDialog.isShowing) progressDialog.dismiss()
        }

//        Glide.with(this)
//            .load(imageUri)
//            .circleCrop()
//            .into(binding.profileImage)
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