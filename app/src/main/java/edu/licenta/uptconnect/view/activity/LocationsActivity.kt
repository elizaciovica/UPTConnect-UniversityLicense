package edu.licenta.uptconnect.view.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.licenta.uptconnect.R

class LocationsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.setOnMarkerClickListener(this)

        val automationFaculty = LatLng(45.74748, 21.22623)
        val aspcFaculty = LatLng(45.74572, 21.23166)
        val hydrotechnicalDepartment = LatLng(45.74894, 21.22866)
        val industrialChemistryFaculty = LatLng(45.75393, 21.22873)
        val mechanicsFaculty = LatLng(45.74601, 21.22569)
        val spmBuilding = LatLng(45.74542, 21.22618)

        mMap.addMarker(
            MarkerOptions()
                .position(automationFaculty)
                .title("Electro")
                .snippet("Faculty of Automation & Computer Science")
        )
        mMap.addMarker(
            MarkerOptions()
                .position(aspcFaculty)
                .title("ASPC")
        )
        mMap.addMarker(
            MarkerOptions()
                .position(hydrotechnicalDepartment)
                .title("Hydro")
                .snippet("Hydrotechnical Department")
        )
        mMap.addMarker(
            MarkerOptions()
                .position(industrialChemistryFaculty)
                .title("ChemCenter")
                .snippet("Chemistry Center")
        )
        mMap.addMarker(
            MarkerOptions()
                .position(mechanicsFaculty)
                .title("Mechanics")
                .snippet("Faculty of Mechanics")
        )
        mMap.addMarker(
            MarkerOptions()
                .position(spmBuilding)
                .title("SPM")
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(automationFaculty, 15F))
    }

    private fun showMarkerDialog(title: String) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_map_marker, null)

        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<TextView>(R.id.name_marker).text = title
        view.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<Button>(R.id.details_btn).setOnClickListener {
            val intent = Intent(this, BuildingsActivity::class.java)
            intent.putExtra("name", title)
            startActivity(intent)
        }

        dialog.show()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if(marker.snippet != null)
        {
            showMarkerDialog(marker.snippet!!)
        } else {
            showMarkerDialog(marker.title!!)
        }
        return true
    }
}