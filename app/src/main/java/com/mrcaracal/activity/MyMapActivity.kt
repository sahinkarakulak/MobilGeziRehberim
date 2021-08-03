package com.mrcaracal.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrcaracal.mobilgezirehberim.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MyMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = "Harita";

    LocationManager locationManager;
    LocationListener locationListener;
    float enlem = (float) 0.0;
    float boylam = (float) 0.0;
    String adres = "";
    String posta_kodu;
    SharedPreferences GET;
    SharedPreferences.Editor SET;
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

        setTitle("Harita");

        GET = getSharedPreferences("harita", MODE_PRIVATE);
        SET = GET.edit();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {

            // KULLANICININ KONUM VE ADRES BİLGİLERİ ANLIK OLARAK ALINSIN VE PAYLAŞ EKRANINDA YAZDIRILSIN
            @Override
            public void onLocationChanged(@NonNull Location location) {
                enlem = (float) location.getLatitude();
                boylam = (float) location.getLongitude();

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                adres = "";
                try {
                    List<Address> addressList = geocoder.getFromLocation(enlem, boylam, 1);
                    if (addressList != null && addressList.size() > 0) {
                        /*if (addressList.get(0).getCountryName() != null) {
                            adres += addressList.get(0).getCountryName();
                        }
                        if (addressList.get(0).getThoroughfare() != null) {
                            adres += "\t" + addressList.get(0).getThoroughfare();
                        }
                        if (addressList.get(0).getSubThoroughfare() != null) {
                            adres += "\t" + addressList.get(0).getSubThoroughfare();
                        }*/
                        adres += addressList.get(0).getAddressLine(0);
                        posta_kodu = addressList.get(0).getPostalCode();

                        if (posta_kodu == null) {
                            posta_kodu = "?";
                        }
                    }
                } catch (IOException e) {
                    /*Toast.makeText(Harita.this, "Adres Alınamadı. Hata;\n" + e.getMessage(), Toast.LENGTH_SHORT).show();*/
                    e.printStackTrace();
                }

                SET.putFloat("enlem", enlem);
                SET.putFloat("boylam", boylam);
                SET.putString("adres", adres);
                SET.putString("postaKodu", posta_kodu);
                SET.commit();

                /*Toast.makeText(getApplicationContext(), "Anlık Konum;\n\nEnlem: " + enlem + "\nBoylam: "
                        + boylam + "\nPosta Kodu: " + posta_kodu + "\nAdres: " + adres, Toast.LENGTH_SHORT).show();*/
                konumuBul();

            }

            // Bazı cihazlarda çökmeler yaşandığından aşağıdaki 2 metodu da kullanmak gerekti.
            @Override
            public void onProviderDisabled(@NonNull String provider) {
                String str_provider = provider;
                /*Toast.makeText(Harita.this, str_provider + " Kapalı", Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                String str_provider = provider;
                /*Toast.makeText(Harita.this, str_provider + " Açık", Toast.LENGTH_SHORT).show();*/
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // İzin işlemleri
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 4);
        } else {
            // Lokasyon işlemleri
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 3, locationListener);
        }
    }


    private void konumuBul() {
        LatLng konum = new LatLng(enlem, boylam);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(new MarkerOptions().position(konum).title("Konumum"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(konum, 16));
        mMap.setOnMapClickListener(this);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0) {
            if (requestCode == 4) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 3, locationListener);
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //  KULLANICI YENİ KONUM SEÇERSE PAYLAŞ EKRANINDA KONUM VE ADRES BİLGİLERİ YAZDIRILSIN
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick: Çalıştı");

        enlem = (float) latLng.latitude;
        boylam = (float) latLng.longitude;

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        adres = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(enlem, boylam, 1);
            if (addressList != null && addressList.size() > 0) {
                /*if (addressList.get(0).getCountryName() != null) {
                    adres += addressList.get(0).getCountryName();
                }
                if (addressList.get(0).getThoroughfare() != null) {
                    adres += "\t" + addressList.get(0).getThoroughfare();
                }
                if (addressList.get(0).getSubThoroughfare() != null) {
                    adres += "\t" + addressList.get(0).getSubThoroughfare();
                }*/
                adres += addressList.get(0).getAddressLine(0);
                posta_kodu = addressList.get(0).getPostalCode();

                if (posta_kodu == null) {
                    posta_kodu = "?";
                }
            }
        } catch (IOException e) {
            /*Toast.makeText(Harita.this, "Adres Alınamadı. Hata;\n" + e.getMessage(), Toast.LENGTH_SHORT).show();*/
            Log.d(TAG, "onMapClick: " + e.getMessage());
            e.printStackTrace();
        }

        /*Toast.makeText(this, "Yeni Konum Alındı;\n\nEnlem: " + enlem + "\nBoylam: " + boylam
                + "\nPosta Kodu: " + posta_kodu + "\nAdres: " + adres, Toast.LENGTH_SHORT).show();*/

        if (marker != null) {
            marker.remove();
        }
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Seçilen Yeni Konum")
                .draggable(true)
                .visible(true)
        );

        SET.putFloat("enlem", enlem);
        SET.putFloat("boylam", boylam);
        SET.putString("adres", adres);
        SET.putString("postaKodu", posta_kodu);
        SET.commit();

    }

}