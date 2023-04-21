package edu.licenta.uptconnect

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityHomeBinding

class HomeActivity : DrawerLayoutActivity() {

    private val screenId: Int = 1
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            screenId
        )
        getProfileImage()
    }

    private fun setBinding() {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getProfileImage() {
        val email: String = intent.getStringExtra("email").toString()
        val storageRef = FirebaseStorage.getInstance().getReference("images/profileImage$email")
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageURL = uri.toString()
            Picasso.get().load(imageURL).into(binding.profileImage)
        }
    }

}