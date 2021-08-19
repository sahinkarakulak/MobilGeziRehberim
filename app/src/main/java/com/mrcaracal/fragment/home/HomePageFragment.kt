package com.mrcaracal.fragment.home

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.MyArrayList
import java.text.DateFormat

class HomePageFragment : Fragment(), RecyclerViewClickInterface {

    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure
    lateinit var viewGroup: ViewGroup
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0

    private lateinit var firebaseOperationForHome: FirebaseOperationForHome

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()

        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
        SET = GET.edit()

        firebaseOperationForHome = FirebaseOperationForHome()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewGroup = inflater.inflate(R.layout.frag_home_page, container, false) as ViewGroup
        init()

        // RecyclerView Tanımlama İşlemi
        recyclerView = viewGroup.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerAdapterStructure = RecyclerAdapterStructure(
            this
        )
        recyclerView.adapter = recyclerAdapterStructure

        // Yeniden eskiye çekme
        firebaseOperationForHome.rewind(FirebaseFirestore.getInstance(), recyclerAdapterStructure)

        return viewGroup
    }

    fun goToLocationOperations(postModel: PostModel) {
        val postLocation = postModel.location.split(",").toTypedArray()
        var adverb = 0
        for (locationXY: String in postLocation) {
            adverb++
            if (adverb == 1) latitude = locationXY.toDouble()
            if (adverb == 2) longitude = locationXY.toDouble()
        }
        SET.putFloat("konum_git_enlem", latitude.toFloat())
        SET.putFloat("konum_git_boylam", longitude.toFloat())
        SET.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
    }

    override fun onLongItemClick(postModel: PostModel) {

        val mDialogView = LayoutInflater.from(activity).inflate(R.layout.custom_dialog_window,viewGroup, false)

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
        labels.text = firebaseOperationForHome.showTag(postModel)

        val mAlertDialog = mBuilder.create()
        mDialogView.findViewById<Button>(R.id.dw_ok).setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog.show()


    }

    override fun onOtherOperationsClick(postModel: PostModel) {
        onOpenDialogWindow(postModel)
    }

    fun onOpenDialogWindow(postModel: PostModel) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup.findViewById(R.id.bottomSheetContainer)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                firebaseUser?.let { it1 ->
                    firebaseOperationForHome.saveOperations(
                        postModel,
                        it1, firebaseFirestore
                    )
                }
                bottomSheetDialog.dismiss()
            })

        // Konuma Git
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    goToLocationOperations(postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // Detaylı Şikayet Bildir (Mail)
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if ((postModel.userEmail == firebaseUser?.email)) {
                        toast(activity!!, getString(R.string.you_already_shared_this))
                    } else {
                        val contactInfo = MyArrayList()
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_account)
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        intent.type = "plain/text"
                        startActivity(
                            Intent.createChooser(
                                intent,
                                getString(R.string.what_would_u_like_to_send_with)
                            )
                        )
                    }
                    bottomSheetDialog.dismiss()
                }
            })

        // İPTAL butonu
        bottomSheetView.findViewById<View>(R.id.bs_cancel)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    //
                    bottomSheetDialog.dismiss()
                }
            })
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

}