package edu.licenta.uptconnect.view.activity

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.R
import edu.licenta.uptconnect.databinding.ActivityBuildingsBinding
import edu.licenta.uptconnect.model.Body
import edu.licenta.uptconnect.model.Location

class BuildingsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityBuildingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()

        val navigationView = binding.navigationView
        val drawerLayout = binding.drawerLayout
        initializeMenu(
            drawerLayout,
            navigationView,
            0
        )
        getInformation()
    }

    private fun setBinding() {
        binding = ActivityBuildingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getInformation() {
        var image = binding.buildingImage
        val nameFaculty = intent.getStringExtra("name")

        Firebase.firestore.collection("locations").document(nameFaculty.toString()).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    binding.buildingName.text = documentSnapshot.get("Name").toString()
                    val document = documentSnapshot.toObject(Location::class.java)

                    //iterate through the bodies
                    for (bodyBuilding in document!!.bodies) {
                        if (bodyBuilding.bodyName == "NA") { //if there is only one body
                            for (floor in bodyBuilding.floor) {
                                createButton(
                                    floor.floorNumber,
                                    nameFaculty.toString(),
                                    binding.bodiesContainer,
                                    bodyBuilding
                                )
                            }
                        } else {
                            createButton(
                                "Body " + bodyBuilding.bodyName,
                                nameFaculty.toString(),
                                binding.bodiesContainer,
                                bodyBuilding
                            )
                        }
                    }
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

    private fun createButton(
        buttonName: String,
        nameFaculty: String,
        linearLayout: LinearLayout,
        body: Body
    ) {
        val button = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                text = buttonName
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = 50
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(resources.getColor(R.color.black))
            setBackgroundResource(R.drawable.rounded_corners)
        }
        linearLayout.addView(button)

        button.setOnClickListener {
            showFloorsDialog(button, nameFaculty, body)
        }
    }

    private fun showFloorsDialog(button: Button, nameFaculty: String, body: Body) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_floors, null)
        val imageFloor = view.findViewById<ImageView>(R.id.floor_image)
        val floorContainer = view.findViewById<LinearLayout>(R.id.floor_container)
        val floorNumber = view.findViewById<TextView>(R.id.name_floor)

        builder.setView(view)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        if (!button.text.contains("Body")) { // if there is no body building

            imageFloor.visibility = View.VISIBLE
            floorContainer.visibility = View.GONE
            floorNumber.text = "Floor ${button.text}"

            val storageRef = FirebaseStorage.getInstance()
                .getReference("locations/$nameFaculty${button.text}.png")
            storageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(imageFloor)
                }
                .addOnFailureListener {
                    floorNumber.text =
                        "There is no plan available for ${floorNumber.text}"
                    imageFloor.visibility = View.GONE
                }
        } else {
            imageFloor.visibility = View.GONE
            floorContainer.visibility = View.VISIBLE
            floorNumber.text = "Floors"
            for (floor in body.floor) {
                val buttonFloorBody = Button(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        500,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        text = floor.floorNumber
                        textAlignment = View.TEXT_ALIGNMENT_CENTER
                        gravity = Gravity.CENTER_HORIZONTAL
                        bottomMargin = 50
                    }
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    setTextColor(resources.getColor(R.color.black))
                    setBackgroundResource(R.drawable.rounded_corners)
                }
                floorContainer.addView(buttonFloorBody)

                buttonFloorBody.setOnClickListener {
                    showBodyFloorsDialog(button, buttonFloorBody, nameFaculty)
                }
            }
        }

        view.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showBodyFloorsDialog(buttonBody: Button, buttonFloor: Button, nameFaculty: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_body_floor, null)
        val imageFloor = view.findViewById<ImageView>(R.id.body_floor_image)
        val floorNumber = view.findViewById<TextView>(R.id.name_body_floor)

        builder.setView(view)
        val dialog = builder.create()
        dialog.window!!.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        floorNumber.text = "Floor ${buttonFloor.text}"
        val storageRef = FirebaseStorage.getInstance().getReference(
            "locations/$nameFaculty${
                buttonBody.text.toString().substringAfter("Body ")
            }${buttonFloor.text}.png"
        )
        storageRef.downloadUrl
            .addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(imageFloor)
            }
            .addOnFailureListener {
                floorNumber.text = "There is no plan available for floor ${buttonFloor.text}"
            }

        view.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}