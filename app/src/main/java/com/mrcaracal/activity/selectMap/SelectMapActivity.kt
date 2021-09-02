package com.mrcaracal.activity.selectMap

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.utils.ConstantsMap
import java.io.IOException
import java.util.*

class SelectMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener {
    private lateinit var viewModel: SelectMapViewModel

    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener

    var latitude = 0.0.toFloat()
    var longitude = 0.0.toFloat()
    var addres = ""
    lateinit var postCode: String

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_map)
        initViewModel()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        title = getString(R.string.map)
        GET = getSharedPreferences(getString(R.string.map_key), MODE_PRIVATE)
        SET = GET.edit()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(SelectMapViewModel::class.java)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                latitude = location.latitude.toFloat()
                longitude = location.longitude.toFloat()
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                addres = ""
                try {
                    val addressList =
                        geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                    if (addressList != null && addressList.size > 0) {
                        addres += addressList[0].getAddressLine(0)
                        postCode = addressList[0].postalCode
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                processSet(
                    latitude = latitude,
                    longitude = longitude,
                    address = addres,
                    postCode = postCode
                )
            }

            override fun onProviderDisabled(provider: String) {
                toast(provider)
            }

            override fun onProviderEnabled(provider: String) {
                toast(provider)
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                4
            )
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                15000,
                3f,
                locationListener
            )
        }
    }

    private fun findLocation() {
        val location = LatLng(
            latitude.toDouble(), longitude.toDouble()
        )
        mMap.addMarker(MarkerOptions().position(location).title(getString(R.string.my_locaiton)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
        mMap.setOnMapClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.map_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (grantResults.size > 0) {
            if (requestCode == 4) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        15000,
                        3f,
                        locationListener
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapClick(latLng: LatLng) {
        latitude = latLng.latitude.toFloat()
        longitude = latLng.longitude.toFloat()
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        addres = ""
        try {
            val addressList = geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
            if (addressList != null && addressList.size > 0) {
                addres += addressList[0].getAddressLine(0)
                postCode = addressList[0].postalCode
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        marker.remove()
        marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.selected_new_location))
                .draggable(true)
                .visible(true)
        )
        processSet(
            latitude = latitude,
            longitude = longitude,
            address = addres,
            postCode = postCode
        )
    }

    private fun processSet(latitude: Float, longitude: Float, address: String, postCode: String) {
        SET.putFloat(ConstantsMap.LATITUDE, latitude)
        SET.putFloat(ConstantsMap.LONGITUDE, longitude)
        SET.putString(ConstantsMap.ADDRESS, address)
        SET.putString(ConstantsMap.POST_CODE, postCode)
        SET.commit()
    }
}