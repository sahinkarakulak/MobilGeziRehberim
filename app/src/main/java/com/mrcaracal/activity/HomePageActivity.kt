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
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.HomePageFragment
import com.mrcaracal.fragment.MyAccountFragment
import com.mrcaracal.fragment.SearchFragment
import com.mrcaracal.fragment.ShareFragment
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R

private const val TAG = "HomePageActivity"

class HomePageActivity : AppCompatActivity() {

    var doubleBackToExitPressedOnce = false
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseFirestore: FirebaseFirestore

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        init()
        title = getString(R.string.app_name_home)
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomePageFragment()).commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomN)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            var selectedFragment: Fragment? = null
            when (menuItem.itemId) {
                R.id.homepage -> {
                    selectedFragment = HomePageFragment()
                    title = getString(R.string.app_name_home)
                }
                R.id.search -> {
                    selectedFragment = SearchFragment()
                    title = getString(R.string.search)
                }
                R.id.share -> {
                    selectedFragment = ShareFragment()
                    title = getString(R.string.share)
                }
                R.id.profile -> {
                    selectedFragment = MyAccountFragment()
                    title = getString(R.string.my_account)
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
            }
            R.id.signOut -> {
                val signOut = Intent(this@HomePageActivity, Login::class.java)
                startActivity(signOut)
                finish()
                firebaseAuth.signOut()
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
        toast("Çıkmak için tekrar basınız")
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

}