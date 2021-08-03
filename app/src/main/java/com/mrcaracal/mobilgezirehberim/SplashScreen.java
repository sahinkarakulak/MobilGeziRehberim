package com.mrcaracal.mobilgezirehberim;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    TextView txt_giris_yazisi;
    Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        txt_giris_yazisi = findViewById(R.id.txt_giris_yazisi);

        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anim1);
        txt_giris_yazisi.startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
            }
        }, 3000);

    }
}