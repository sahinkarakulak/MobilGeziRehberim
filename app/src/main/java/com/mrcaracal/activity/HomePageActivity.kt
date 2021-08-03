package com.mrcaracal.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.fragment.HomePageFragment
import com.mrcaracal.fragment.MyAccountFragment
import com.mrcaracal.fragment.SearchFragment
import com.mrcaracal.fragment.ShareFragment
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R

private const val TAG = "HomePageActivity"

class HomePageActivity : AppCompatActivity() {

    var doubleBackToExitPressedOnce = false
    var firebaseAuth: FirebaseAuth? = null
    var firebaseFirestore: FirebaseFirestore? = null

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        init()
        title = "Mobil Gezi Rehberim"
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomePageFragment()).commit()
        Log.i(TAG, "onCreate: F_Anasayfa Fragment'i açıldı")

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomN)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            var selectedFragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.homepage -> {
                    selectedFragment = HomePageFragment()
                    title = "Mobil Gezi Rehberim"
                    Log.i(TAG, "onNavigationItemSelected: F_Anasayfa fragment'i seçildi")
                }
                R.id.search -> {
                    selectedFragment = SearchFragment()
                    title = "Ara"
                    Log.i(TAG, "onNavigationItemSelected: F_Ara fragment'i seçildi")
                }
                R.id.share -> {
                    selectedFragment = ShareFragment()
                    title = "Paylaş"
                    Log.i(TAG, "onNavigationItemSelected: F_Paylas fragment'i seçildi")
                }
                R.id.profile -> {
                    selectedFragment = MyAccountFragment()
                    title = "Hesabım"
                    Log.i(TAG, "onNavigationItemSelected: F_Hesabim fragment'i seçildi")
                }
            }
            // Tespit ettiğimiz fragment'i yayınlıyoruz.
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, selectedFragment!!)
                .commit()

            // Seçili fragmentin kullanıcının anlaması için true yaptık
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.contact -> {
                val contact = Intent(this@HomePageActivity, ContactActivity::class.java)
                startActivity(contact)
                Log.i(TAG, "onOptionsItemSelected: İletişim menüsü seçildi")
            }
            R.id.signOut -> {
                val signOut = Intent(this@HomePageActivity, Login::class.java)
                startActivity(signOut)
                finish()
                firebaseAuth!!.signOut()
                Log.i(TAG, "onOptionsItemSelected: Çıkış menüsü seçildi")
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Çıkmak için tekrar basınız", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

}