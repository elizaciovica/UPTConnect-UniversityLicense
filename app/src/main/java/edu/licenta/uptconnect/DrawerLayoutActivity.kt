package edu.licenta.uptconnect

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

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
            1 -> navigation.setCheckedItem(R.id.home)
            2 -> navigation.setCheckedItem(R.id.edit_profile)
            3 -> navigation.setCheckedItem(R.id.my_courses)
            4 -> navigation.setCheckedItem(R.id.my_colleagues)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            Toast.makeText(applicationContext, "Clicked home", Toast.LENGTH_SHORT).show()
            return true
        }
        if (item.itemId == R.id.edit_profile) {
            Toast.makeText(applicationContext, "Clicked edit profile", Toast.LENGTH_SHORT).show()
            return true
        }
        if (item.itemId == R.id.my_courses) {
            Toast.makeText(applicationContext, "Clicked my courses", Toast.LENGTH_SHORT).show()
            return true
        }
        if (item.itemId == R.id.my_colleagues) {
            Toast.makeText(applicationContext, "Clicked my colleagues", Toast.LENGTH_SHORT).show()
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
        //Firebase.auth.signOut()
        startActivity(intent)
        finish()
    }
}