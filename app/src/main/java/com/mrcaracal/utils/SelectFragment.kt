package com.mrcaracal.utils

import androidx.fragment.app.Fragment
import com.mrcaracal.fragment.account.MyAccountFragment
import com.mrcaracal.fragment.home.HomePageFragment
import com.mrcaracal.fragment.search.SearchFragment
import com.mrcaracal.fragment.share.SelectLocationMapFragment
import com.mrcaracal.fragment.share.ShareFragment
import com.mrcaracal.mobilgezirehberim.R

object SelectFragment {

    fun selectFragment(menuItemId: Int): Fragment{
        when(menuItemId){
            R.id.homepage -> {
                return HomePageFragment()
            }
            R.id.search -> {
                return SearchFragment()
            }
            R.id.share -> {
                return ShareFragment()
            }
            R.id.profile -> {

                return MyAccountFragment()
            }
            Constants.SELECT_MAP_FRAGMENT -> {
                return SelectLocationMapFragment()
            }
            else -> {
                return HomePageFragment()
            }
        }
    }

}