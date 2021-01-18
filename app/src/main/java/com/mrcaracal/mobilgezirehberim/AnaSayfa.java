package com.mrcaracal.mobilgezirehberim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AnaSayfa extends AppCompatActivity {

    boolean doubleBackToExitPressedOnce = false;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    private void init() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ana_sayfa);
        init();

        setTitle("Mobil Gezi Rehberim");

        // Uygulama açıldığı gibi ana_sayfa fragment'in çalışması için aşağıdaki kod satırını kullandık.
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new F_Anasayfa()).commit();

        // BottomNavigation
        // Nesnesini oluşturup tıklanma işlemlerinde neler yapılacağını belirttik.
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomN);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment seciliFragment = null;

                // Hangi menüye tıklanmışsa onu tespit ediyoruz.
                switch (menuItem.getItemId()) {
                    case R.id.ana_sayfa:
                        seciliFragment = new F_Anasayfa();
                        setTitle("Mobil Gezi Rehberim");
                        break;
                    case R.id.ara:
                        seciliFragment = new F_Ara();
                        setTitle("Ara");
                        break;
                    case R.id.paylas:
                        seciliFragment = new F_Paylas();
                        setTitle("Paylaş");
                        break;
                    case R.id.hesabim:
                        seciliFragment = new F_Hesabim();
                        setTitle("Hesabım");
                        break;
                }
                // Tespit ettiğimiz fragment'i yayınlıyoruz.
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, seciliFragment).commit();

                return true;
            }
        });
    }


    // Uygulamanın sağ üst tarafında menünün yer almasını sağlayan kısım
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.ust_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Uygulamanın sağ üst tarafındaki menüde tıklama durumunda yapılacak işlemler...
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.iletisim:
                Intent iletisim = new Intent(AnaSayfa.this, Iletisim.class);
                startActivity(iletisim);
                break;

            case R.id.cikis:
                Intent cikis = new Intent(AnaSayfa.this, Giris.class);
                startActivity(cikis);
                finish();
                firebaseAuth.signOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Geri tuşuna çift tıklama ile uygulamadan çıkma işlemi
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Çıkmak için tekrar basınız", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }
}