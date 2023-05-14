package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private var imageUri: Uri? = null
    private var imageUrl: String = ""
    private var email = ""
    private var firebaseUser = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getProfileDetails()
        setProfileImage()
    }

    private fun setBinding() {
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getProfileDetails() {
        email = intent.getStringExtra("email").toString()
        firebaseUser = intent.getStringExtra("userId").toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            imageUrl = uri.toString()
            Picasso.get().load(imageUrl).into(binding.profileImage)
        }

        Firebase.firestore.collection("students").document(firebaseUser).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    binding.firstName.setText(documentSnapshot.get("FirstName").toString())
                    binding.lastName.setText(documentSnapshot.get("LastName").toString())
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

        if (!binding.lastName.text.toString()
                .all { char -> char.isLetter() } || binding.lastName.text!!.isEmpty()
        ) {
            Toast.makeText(
                this,
                "Please enter a valid Last Name",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    private fun setProfileImage() {

        binding.uploadProfilePicture.setOnClickListener {
            selectImage()
        }
        binding.setProfile.setOnClickListener {
            val validDetails = verifyInputDetails()
            if (!validDetails) {
            } else {
                val fireStoreDb = Firebase.firestore
                fireStoreDb.collection("students").document(firebaseUser)
                    .update(
                        "FirstName", binding.firstName.text.toString(),
                        "LastName", binding.lastName.text.toString()
                    )

                if (imageUri == null) {
                    startApplication()
                } else {
                    val fileName = "profileImage$email"
                    val storageReference =
                        FirebaseStorage.getInstance().getReference("images/$fileName")
                    storageReference.putFile(imageUri!!).addOnSuccessListener {
                        startApplication()
                    }
                }

            }
        }
    }

    private fun startApplication() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("userId", firebaseUser)
        intent.putExtra("email", email)
        startActivity(intent)
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