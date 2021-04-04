package com.mrcaracal.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrcaracal.mobilgezirehberim.R;

public class HaritaKonumaGit extends AppCompatActivity implements OnMapReadyCallback {

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
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);

        setTitle("Gönderi Konumu");

        GET = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SET = GET.edit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double enlem = GET.getFloat("konum_git_enlem", 0);
        double boylam = GET.getFloat("konum_git_boylam", 0);

        Log.d(TAG, "onMapReady: " + enlem + "," + boylam);

        LatLng gonderi_konumu = new LatLng(enlem, boylam);
        mMap.addMarker(new MarkerOptions().position(gonderi_konumu).title("Gönderi Konumu"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gonderi_konumu, 16));
    }

    // FARKLI HARİTA TÜRLERİ İÇİN MENÜLEİR LİSTELEDİK
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.harita_menu, menu);
        return true;
    }

    // FARKLI HARİTA TÜRLERİNE TIKLANDIĞINDA YAPILACAK İŞLEMLERİ BELİRLEDİK
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}