package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityBuildingsBinding

class BuildingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuildingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getInformation()
    }

    private fun setBinding() {
        binding = ActivityBuildingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getInformation() {
        var title = binding.buildingName
        var image = binding.buildingImage
        val nameFaculty = intent.getStringExtra("name")

        Firebase.firestore.collection("locations").document(nameFaculty.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    title.text = documentSnapshot.get("Name").toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving location", exception)
            }

        val storageRef = FirebaseStorage.getInstance().getReference("locations/$nameFaculty.png")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri.toString()).into(image)
        }
    }
}