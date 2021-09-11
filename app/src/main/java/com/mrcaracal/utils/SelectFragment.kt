package com.mrcaracal.utils

import androidx.fragment.app.Fragment
import com.mrcaracal.fragment.account.MyAccountFragment
import com.mrcaracal.fragment.home.HomePageFragment
import com.mrcaracal.fragment.search.SearchFragment
import com.mrcaracal.fragment.share.SelectLocationMap
import com.mrcaracal.fragment.share.ShareFragment
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.utils.Constants

object SelectFragment {

    fun selectFragment(menuItemId : Int) : Fragment{
        when(menuItemId){
            R.id.homepage -> {
                /*selectedFragment = HomePageFragment()
                title = getString(R.string.app_name_home)*/
                return HomePageFragment()
            }
            R.id.search -> {
                /*selectedFragment = SearchFragment()
                title = getString(R.string.search)*/
                return SearchFragment()
            }
            R.id.share -> {
                /*selectedFragment = ShareFragment()
                title = getString(R.string.share)*/
                return ShareFragment()
            }
            R.id.profile -> {
                /*selectedFragment = MyAccountFragment()
                title = getString(R.string.my_account)*/
                return MyAccountFragment()
            }
            Constants.SELECT_MAP_FRAGMENT -> {
                return SelectLocationMap()
            }
        }
        return HomePageFragment()
    }
}