package com.mrcaracal.extensions

import android.widget.ImageView
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String){
    Picasso.get()
        .load(url)
        .centerCrop()
        .fit()
        .into(this)
}