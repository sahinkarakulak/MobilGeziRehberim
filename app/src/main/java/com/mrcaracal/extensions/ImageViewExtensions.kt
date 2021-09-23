package com.mrcaracal.extensions

import android.net.Uri
import android.widget.ImageView
import com.squareup.picasso.Picasso

fun ImageView.loadUrl(url: String) {
    Picasso.get()
        .load(url)
        .centerCrop()
        .fit()
        .into(this)
}

fun ImageView.loadUrl(urlInt: Int) {
    Picasso.get()
        .load(urlInt)
        .centerCrop()
        .fit()
        .into(this)
}

fun ImageView.loadUrl(urlUri: Uri) {
    Picasso.get()
        .load(urlUri)
        .centerCrop()
        .fit()
        .into(this)
}