package com.mrcaracal.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrcaracal.fragment.home.HomePageFragment
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.search.SearchFragment
import com.mrcaracal.mobilgezirehberim.R

object PostDetailBottomSheet {

    fun ShowPostDataDetails(
        context: Context,
        postModel: PostModel,
        container: ViewGroup
    ) {

        val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(context).inflate(
            R.layout.layout_bottom_sheet,
            container.findViewById(R.id.bottomSheetContainer)
        )

        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // Save Post
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener {
            HomePageFragment().viewModel.saveOperations(postModel = postModel)
            //viewModel.saveOperations(postModel = postModel)
            bottomSheetDialog.dismiss()
        }

        // Go to locaiton
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    HomePageFragment().goToLocationOperations(postModel = postModel)
                    //goToLocationOperations(postModel = postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // Report Post
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    HomePageFragment().viewModel.reportPostFromHomePage(postModel = postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // Cancel
        bottomSheetView.findViewById<View>(R.id.bs_cancel)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    bottomSheetDialog.dismiss()
                }
            })
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()

    }


}