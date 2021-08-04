package com.mrcaracal.activity

import android.Manifest
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.mrcaracal.mobilgezirehberim.R
import java.io.IOException
import java.util.*

private const val TAG = "MyMapActivity"

class MyMapActivity : AppCompatActivity(), OnMapReadyCallback, OnMapClickListener {
    lateinit var locationManager: LocationManager
    lateinit var locationListener: LocationListener

    var latitude = 0.0.toFloat()
    var longitude = 0.0.toFloat()
    var addres = ""
    lateinit var postCode: String

    private var MAP_KEY = "harita"

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    private lateinit var mMap: GoogleMap
    private lateinit var marker: Marker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        title = getString(R.string.map)
        GET = getSharedPreferences(MAP_KEY, MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener {
            // KULLANICININ KONUM VE ADRES BİLGİLERİ ANLIK OLARAK ALINSIN VE PAYLAŞ EKRANINDA YAZDIRILSIN
            override fun onLocationChanged(location: Location) {
                latitude = location.latitude.toFloat()
                longitude = location.longitude.toFloat()
                val geocoder = Geocoder(applicationContext, Locale.getDefault())
                addres = ""
                try {
                    val addressList =
                        geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
                    if (addressList != null && addressList.size > 0) {
                        /*if (addressList.get(0).getCountryName() != null) {
                            adres += addressList.get(0).getCountryName();
                        }
                        if (addressList.get(0).getThoroughfare() != null) {
                            adres += "\t" + addressList.get(0).getThoroughfare();
                        }
                        if (addressList.get(0).getSubThoroughfare() != null) {
                            adres += "\t" + addressList.get(0).getSubThoroughfare();
                        }*/
                        addres += addressList[0].getAddressLine(0)
                        postCode = addressList[0].postalCode
                    }
                } catch (e: IOException) {
                    /*Toast.makeText(Harita.this, "Adres Alınamadı. Hata;\n" + e.getMessage(), Toast.LENGTH_SHORT).show();*/
                    e.printStackTrace()
                }
                SET.putFloat("enlem", latitude)
                SET.putFloat("boylam", longitude)
                SET.putString("adres", addres)
                SET.putString("postaKodu", postCode)
                SET.commit()

                /*Toast.makeText(getApplicationContext(), "Anlık Konum;\n\nEnlem: " + enlem + "\nBoylam: "
                        + boylam + "\nPosta Kodu: " + posta_kodu + "\nAdres: " + adres, Toast.LENGTH_SHORT).show();*/findLocation()
            }

            // Bazı cihazlarda çökmeler yaşandığından aşağıdaki 2 metodu da kullanmak gerekti.
            override fun onProviderDisabled(provider: String) {
                val str_provider = provider
                /*Toast.makeText(Harita.this, str_provider + " Kapalı", Toast.LENGTH_SHORT).show();*/
            }

            override fun onProviderEnabled(provider: String) {
                val str_provider = provider
                /*Toast.makeText(Harita.this, str_provider + " Açık", Toast.LENGTH_SHORT).show();*/
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // İzin işlemleri
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                4
            )
        } else {
            // Lokasyon işlemleri
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                15000,
                3f,
                locationListener as LocationListener
            )
        }
    }

    private fun findLocation() {
        val location = LatLng(
            latitude.toDouble(), longitude.toDouble()
        )
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(MarkerOptions().position(location).title("Konumum"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
        mMap.setOnMapClickListener(this)
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

    //  KULLANICI YENİ KONUM SEÇERSE PAYLAŞ EKRANINDA KONUM VE ADRES BİLGİLERİ YAZDIRILSIN
    override fun onMapClick(latLng: LatLng) {
        latitude = latLng.latitude.toFloat()
        longitude = latLng.longitude.toFloat()
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        addres = ""
        try {
            val addressList = geocoder.getFromLocation(latitude.toDouble(), longitude.toDouble(), 1)
            if (addressList != null && addressList.size > 0) {
                /*if (addressList.get(0).getCountryName() != null) {
                    adres += addressList.get(0).getCountryName();
                }
                if (addressList.get(0).getThoroughfare() != null) {
                    adres += "\t" + addressList.get(0).getThoroughfare();
                }
                if (addressList.get(0).getSubThoroughfare() != null) {
                    adres += "\t" + addressList.get(0).getSubThoroughfare();
                }*/
                addres += addressList[0].getAddressLine(0)
                postCode = addressList[0].postalCode
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        /*Toast.makeText(this, "Yeni Konum Alındı;\n\nEnlem: " + enlem + "\nBoylam: " + boylam
                + "\nPosta Kodu: " + posta_kodu + "\nAdres: " + adres, Toast.LENGTH_SHORT).show();*/
        marker.remove()
        marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Seçilen Yeni Konum")
                .draggable(true)
                .visible(true)
        )
        SET.putFloat("enlem", latitude)
        SET.putFloat("boylam", longitude)
        SET.putString("adres", addres)
        SET.putString("postaKodu", postCode)
        SET.commit()
    }

}