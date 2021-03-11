package com.mrcaracal.Activity;

import androidx.fragment.app.FragmentActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrcaracal.mobilgezirehberim.R;

public class HaritaKonumaGit extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "HaritaKonumaGit";

    SharedPreferences GET;
    SharedPreferences.Editor SET;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harita_konuma_git);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        GET = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SET = GET.edit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double enlem = (double) GET.getFloat("konum_git_enlem", 0);
        double boylam = (double) GET.getFloat("konum_git_boylam", 0);

        Log.d(TAG, "onMapReady: "+enlem + "," + boylam);


        // Add a marker in Sydney and move the camera
        LatLng gonderi_konumu = new LatLng(enlem, boylam);
        mMap.addMarker(new MarkerOptions().position(gonderi_konumu).title("Gönderi Konumu"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonderi_konumu, 16));
    }
}