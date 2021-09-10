package com.mrcaracal.utils

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentActivity
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import java.text.DateFormat

private const val TAG = "DialogViewCustomize"

object DialogViewCustomize {

    fun dialogViewCustomize(
        activity: FragmentActivity?,
        container: ViewGroup,
        postModel: PostModel,
        postTags: String
    ) {

        val mDialogView =
            LayoutInflater.from(activity).inflate(R.layout.custom_dialog_window, container, false)

        val dateAndTime = DateFormat.getDateTimeInstance().format(
            postModel.time.toDate()
        )

        val mBuilder = AlertDialog.Builder(activity)
            .setView(mDialogView)

        val title = mDialogView.findViewById<TextView>(R.id.dw_title)
        val comment = mDialogView.findViewById<TextView>(R.id.dw_comment)
        val sharing = mDialogView.findViewById<TextView>(R.id.dw_sharing)
        val date = mDialogView.findViewById<TextView>(R.id.dw_date)
        val addres = mDialogView.findViewById<TextView>(R.id.dw_addres)
        val labels = mDialogView.findViewById<TextView>(R.id.dw_labels)
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

        mDialogView.findViewById<LinearLayoutCompat>(R.id.dw_save_post).setOnClickListener {
            Log.i(TAG, "dialogViewCustomize: Save Post")
        }

        mDialogView.findViewById<LinearLayoutCompat>(R.id.dw_location_post).setOnClickListener {
            Log.i(TAG, "dialogViewCustomize: Location Post")
        }

        mDialogView.findViewById<LinearLayoutCompat>(R.id.dw_report_post).setOnClickListener {
            Log.i(TAG, "dialogViewCustomize: Report Post")
        }

        mAlertDialog.show()
    }

}