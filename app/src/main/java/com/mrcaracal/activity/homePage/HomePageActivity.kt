package com.mrcaracal.activity.homePage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mrcaracal.activity.contact.ContactActivity
import com.mrcaracal.fragment.home.HomePageFragment
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.login.Login
import com.mrcaracal.utils.SelectFragmentOnHomePageActivity

class HomePageActivity : AppCompatActivity() {

    private lateinit var viewModel: HomePageViewModelActivity
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        initViewModel()
        observeHomePageActivityState()
        selectFragment()
    }

    private fun selectFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, HomePageFragment())
            .commit()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomN)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            val selectedFragment = SelectFragmentOnHomePageActivity.selectFragmentOnHomePage(menuItemId = menuItem.itemId)
            supportFragmentManager.beginTransaction().replace(R.id.frame_layout, selectedFragment)
                .commit()
            true
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(HomePageViewModelActivity::class.java)
    }

    private fun observeHomePageActivityState() {
        viewModel.homePageActivityState.observe(this) { homePageActivityViewState ->
            when (homePageActivityViewState) {

                else -> {
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.contact -> {
                val contact = Intent(this@HomePageActivity, ContactActivity::class.java)
                startActivity(contact)
            }
            R.id.signOut -> {
                val signOut = Intent(this@HomePageActivity, Login::class.java)
                startActivity(signOut)
                finish()
                viewModel.signOut()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}