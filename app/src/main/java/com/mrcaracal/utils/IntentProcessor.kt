package com.mrcaracal.utils

import android.content.Context
import android.content.Intent
import com.mrcaracal.mobilgezirehberim.R
object IntentProcessor {

  fun process(
    context: Context,
    emails: ArrayList<String>,
    subject: String,
    text: String,
    titleResId  : Int = R.string.what_would_u_like_to_send_with
  ) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.putExtra(Intent.EXTRA_EMAIL, emails)
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, text)
    intent.type = Constants.PLAIN_TEXT
    context.startActivity(
      Intent.createChooser(
        intent, context.getString(titleResId)
      ), null
    )
  }
}