package com.mrcaracal.activity

import android.content.SharedPreferences
import android.os.Bundle
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

class GoToLocationOnMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    private lateinit var mMap: GoogleMap

    private var GO_TO_LOCATION_LATITUDE = "konum_git_enlem"
    private var GO_TO_LOCATION_LONGITUDE = "konum_git_boylam"
    private var POST_LOCATION = "Gönderi Konumu"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_go_to_location_on_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map2) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        title = getString(R.string.postLocation)
        GET = getSharedPreferences(R.string.map_key.toString(), MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val latitude = GET.getFloat(GO_TO_LOCATION_LATITUDE, 0f).toDouble()
        val longitude = GET.getFloat(GO_TO_LOCATION_LONGITUDE, 0f).toDouble()

        val postLocation = LatLng(latitude, longitude)
        mMap.addMarker(MarkerOptions().position(postLocation).title(POST_LOCATION))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(postLocation, 16f))
    }

    // FARKLI HARİTA TÜRLERİ İÇİN MENÜLEİR LİSTELEDİK
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_menu, menu)
        return true
    }

    // FARKLI HARİTA TÜRLERİNE TIKLANDIĞINDA YAPILACAK İŞLEMLERİ BELİRLEDİK
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Change the map type based on the user's selection.
        return when (item.itemId) {
            R.id.normal_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }
            R.id.hybrid_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }
            R.id.satellite_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }
            R.id.terrain_map -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}