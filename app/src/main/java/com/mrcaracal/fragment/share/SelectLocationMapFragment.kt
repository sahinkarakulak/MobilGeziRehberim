package com.mrcaracal.fragment.share

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.utils.Constants
import com.mrcaracal.utils.ConstantsMap
import java.io.IOException
import java.util.*

private const val TAG = "SelectLocationMapFragme"

class SelectLocationMapFragment : Fragment() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    var latitude = 0.0.toFloat()
    var longitude = 0.0.toFloat()
    var address = ""
    var postCode: String = ""

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermission()
        } else {
            locationManagerAndListener()
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000,
                5f,
                locationListener
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GET = requireActivity().applicationContext.getSharedPreferences(
            getString(R.string.map_key),
            AppCompatActivity.MODE_PRIVATE
        )
        SET = GET.edit()
        return inflater.inflate(R.layout.fragment_select_location_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun locationManagerAndListener() {
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                latitude = location.latitude.toFloat()
                longitude = location.longitude.toFloat()
                findLocation()
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                address = ""
                try {
                    val addressList =
                        geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                    if (!(addressList == null || addressList.size <= 0)) {
                        address += addressList[0].getAddressLine(0)
                        if (addressList[0].postalCode != null) {
                            postCode = addressList[0].postalCode
                            Log.i(TAG, "onLocationChanged: $postCode")
                        } else
                            postCode = ""
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                processSet(
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    postCode = postCode
                )
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                //provider?.let { toast(it) }
            }

            override fun onProviderDisabled(provider: String) {
                //toast(provider)
            }

            override fun onProviderEnabled(provider: String) {
                //toast(provider)
            }
        }
    }

    private fun findLocation() {
        val location = LatLng(
            latitude.toDouble(), longitude.toDouble()
        )
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(location).title(getString(R.string.my_locaiton)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        mMap.setOnMapClickListener { latlng ->
            locationUserClicked(latLng = latlng)
            Log.i(TAG, "findLocation: " + latlng.latitude.toString())
            Log.i(TAG, "findLocation: " + latlng.longitude.toString())
        }
    }

    private fun locationUserClicked(latLng: LatLng) {
        latitude = latLng.latitude.toFloat()
        longitude = latLng.longitude.toFloat()
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        address = ""
        try {
            val addressList = geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
            if (addressList != null && addressList.size > 0) {
                address += addressList[0].getAddressLine(0)
                if (addressList[0].postalCode != null) {
                    postCode = addressList[0].postalCode
                    Log.i(TAG, "locationUserClicked: $postCode")
                } else
                    postCode = ""
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        mMap.clear()
        marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(getString(R.string.selected_new_location))
                .draggable(true)
                .visible(true)
        )!!
        processSet(
            latitude = latitude,
            longitude = longitude,
            address = address,
            postCode = postCode
        )
    }

    fun checkLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), Constants.LOCATION_PERMISSON_CODE
        )
    }

    private fun processSet(latitude: Float, longitude: Float, address: String, postCode: String?) {
        SET.putFloat(ConstantsMap.LATITUDE, latitude)
        SET.putFloat(ConstantsMap.LONGITUDE, longitude)
        SET.putString(ConstantsMap.ADDRESS, address)
        SET.putString(ConstantsMap.POST_CODE, postCode)
        SET.commit()
    }

}