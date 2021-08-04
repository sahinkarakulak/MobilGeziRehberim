package com.mrcaracal.extensions

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * @author yusuf.onder
 * Created on 4.08.2021
 */

fun Context.toast(message: String) {
  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes messageResId: Int) {
  Toast.makeText(this, getString(messageResId), Toast.LENGTH_SHORT).show()
}
