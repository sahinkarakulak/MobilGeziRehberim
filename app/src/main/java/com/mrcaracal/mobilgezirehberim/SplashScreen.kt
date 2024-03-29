package com.mrcaracal.mobilgezirehberim

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mrcaracal.mobilgezirehberim.login.Login

class SplashScreen : AppCompatActivity() {

    private lateinit var txt_giris_yazisi: TextView
    private var animation: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        txt_giris_yazisi = findViewById(R.id.txt_giris_yazisi)
        animation = AnimationUtils.loadAnimation(applicationContext, R.anim.anim1)
        txt_giris_yazisi.startAnimation(animation)

        Handler(Looper.myLooper() ?: return).postDelayed({
            val intent = Intent(this@SplashScreen, Login::class.java)
            startActivity(intent)
        }, 2000)

    }

}