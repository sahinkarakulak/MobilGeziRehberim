package com.mrcaracal.mobilgezirehberim;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class A_Harita extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    double enlem, boylam;
    private GoogleMap mMap;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harita);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            // Kullanıcının konumu anlık olarak alınsın ve paylaş ekranında konum kısmında konum bilgileri yazdırılsın.
            // Adres bilgileri de alınsın ve yazdırılsın
            @Override
            public void onLocationChanged(@NonNull Location location) {
                enlem = location.getLatitude();
                boylam = location.getLongitude();

                Toast.makeText(getApplicationContext(), "Enlem: " + enlem + "\nBoylam: " + boylam, Toast.LENGTH_SHORT).show();
                konumuBul();

            }

            // Bazı cihazlarda çökmeler yaşandığından aşağıdaki 2 metodu da kullanmak gerekti.
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                String str_provider = provider;
                Toast.makeText(A_Harita.this, str_provider + " Kapalı", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                String str_provider = provider;
                Toast.makeText(A_Harita.this, str_provider + " Açık", Toast.LENGTH_SHORT).show();
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // İzin işlemleri
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        } else {
            // Lokasyon işlemleri
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 3, locationListener);
        }
    }

    private void konumuBul() {
        LatLng konum = new LatLng(enlem, boylam);
        mMap.addMarker(new MarkerOptions().position(konum).title("Konumum"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum, 15));

        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0) {
            if (requestCode == 4) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5, 3, locationListener);
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Kullanıcı yeni konum seçer ise Paylaş ekranında konum kısmına konum bilgileri yazdırılsın.
    // Adres bilgileri de alınsın ve yazdırılsın
    @Override
    public void onMapClick(LatLng latLng) {
        Toast.makeText(this, "Enlem: " + latLng.latitude + "\nBoylam: " + latLng.longitude, Toast.LENGTH_SHORT).show();

        if (marker != null) {
            marker.remove();
        }
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Seçilen Yeni Konum")
                .draggable(true)
                .visible(true)
        );
    }
}