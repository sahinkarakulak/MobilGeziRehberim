package com.mrcaracal.utils

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mrcaracal.fragment.home.HomePageViewModel
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import java.text.DateFormat

object DialogViewCustomize {

    fun dialogViewCustomize(
        activity: FragmentActivity?,
        container: ViewGroup,
        postModel: PostModel,
        postTags: String
    ){

        val mDialogView =
            LayoutInflater.from(activity).inflate(R.layout.custom_dialog_window, container, false)

        val dateAndTime = DateFormat.getDateTimeInstance().format(
            postModel.time.toDate()
        )

        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)

        var title = mDialogView.findViewById<TextView>(R.id.dw_title)
        var comment = mDialogView.findViewById<TextView>(R.id.dw_comment)
        var sharing = mDialogView.findViewById<TextView>(R.id.dw_sharing)
        var date = mDialogView.findViewById<TextView>(R.id.dw_date)
        var addres = mDialogView.findViewById<TextView>(R.id.dw_addres)
        var labels = mDialogView.findViewById<TextView>(R.id.dw_labels)
        title.text = postModel.placeName
        comment.text = postModel.comment
        sharing.text = postModel.userEmail
        date.text = dateAndTime
        addres.text = postModel.address
        labels.text = postTags

        val mAlertDialog = mBuilder.create()
        mDialogView.findViewById<Button>(R.id.dw_ok).setOnClickListener {
            mAlertDialog.dismiss()
        }
        mAlertDialog.show()
    }

}