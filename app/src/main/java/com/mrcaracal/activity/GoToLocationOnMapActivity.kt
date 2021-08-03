package com.mrcaracal.activity

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.mrcaracal.mobilgezirehberim.R

private const val TAG = "GoToLocationOnMapActivi"

class GoToLocationOnMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_to_location_on_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        title = "Gönderi Konumu"
        GET = getSharedPreferences("harita", MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latitude = GET!!.getFloat("konum_git_enlem", 0f).toDouble()
        val longitude = GET!!.getFloat("konum_git_boylam", 0f).toDouble()
        Log.i(TAG, "onMapReady: $latitude,$longitude")

        val postLocation = LatLng(latitude, longitude)
        mMap!!.addMarker(MarkerOptions().position(postLocation).title("Gönderi Konumu"))
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 16f))
    }

    // FARKLI HARİTA TÜRLERİ İÇİN MENÜLEİR LİSTELEDİK
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.harita_menu, menu)
        return true
    }

    // FARKLI HARİTA TÜRLERİNE TIKLANDIĞINDA YAPILACAK İŞLEMLERİ BELİRLEDİK
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Change the map type based on the user's selection.
        return when (item.itemId) {
            R.id.normal_map -> {
                mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                mMap!!.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                mMap!!.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                mMap!!.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "HaritaKonumaGit"
    }
}