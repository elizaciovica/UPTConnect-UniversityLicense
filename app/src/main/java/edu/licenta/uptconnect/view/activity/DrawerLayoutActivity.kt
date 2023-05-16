package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.licenta.uptconnect.R

open class DrawerLayoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun initializeMenu(
        drawer: DrawerLayout,
        navigation: NavigationView,
        screen: Int
    ) {
        drawer.addDrawerListener(
            ActionBarDrawerToggle(
                this,
                drawer,
                R.string.open,
                R.string.close
            )
        )

        val button = findViewById<ImageButton>(R.id.lines_id)
        button.setOnClickListener() {
            drawer.openDrawer(GravityCompat.END)
        }
        navigation.bringToFront()

        navigation.setNavigationItemSelectedListener { menuItem ->
            onOptionsItemSelected(menuItem)
            navigation.setCheckedItem(menuItem)
            drawer.closeDrawer(GravityCompat.END)
            true
        }

        when (screen) {
            0 -> navigation.setCheckedItem(0)
            1 -> navigation.setCheckedItem(R.id.home)
            2 -> navigation.setCheckedItem(R.id.edit_profile)
        }
        getStudentEmail()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.home) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
            intent.putExtra("email", FirebaseAuth.getInstance().currentUser!!.email)
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.edit_profile) {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", FirebaseAuth.getInstance().currentUser!!.uid)
            intent.putExtra("email", FirebaseAuth.getInstance().currentUser!!.email)
            startActivity(intent)
            return true
        }
        if (item.itemId == R.id.log_out) {
            logoutAction()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutAction() {
        val intent = Intent(this, StartActivity::class.java)
        Firebase.auth.signOut()
        startActivity(intent)
        finish()
    }

    private fun getStudentEmail() {
        val studentsDatabase = Firebase.firestore
        val studentFirebaseId = FirebaseAuth.getInstance().currentUser?.uid
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId!!)
        // -> is a lambda consumer - based on its parameter - i need a listener to wait for the database call
        // ex .get() - documentSnapshot is like a response body
        //binding the dynamic linking for the xml component and the content
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    var emailFirebaseTextView = findViewById<TextView>(R.id.email_firebase)
                    emailFirebaseTextView.text = documentSnapshot.getString("Email")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student Name. ", exception)
            }
    }
}