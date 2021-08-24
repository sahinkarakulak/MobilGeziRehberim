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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragHomePageBinding
import com.mrcaracal.utils.IntentProcessor
import java.text.DateFormat
import java.util.*

class HomePageFragment : Fragment(), RecyclerViewClickInterface {

    private lateinit var viewModel: HomePageViewModel

    private var _binding: FragHomePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var view: ViewGroup

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    private lateinit var container: ViewGroup

    private fun init() {
        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        init()
        if (container != null) {
            this.container = container
        }
        _binding = FragHomePageBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        viewModel.init()
        observeHomePageState()

        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        viewModel.recyclerAdapterProccese(thisClick = this)
        viewModel.rewind(postModelsList = postModelsList)

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(HomePageViewModel::class.java)
    }

    fun observeHomePageState() {
        viewModel.homePageState.observe(viewLifecycleOwner) { homePageViewState ->
            when (homePageViewState) {
                is HomePageViewState.OpenEmail -> {
                    context?.let {
                        IntentProcessor.process(
                            context = it,
                            emails = homePageViewState.emails,
                            subject = homePageViewState.subject,
                            text = homePageViewState.message
                        )
                    }
                }
                is HomePageViewState.ShowAlreadySharedToastMessage -> {
                    toast(activity!!, getString(R.string.you_already_shared_this))
                }
                is HomePageViewState.SendRecyclerAdapter -> {
                    binding.recyclerView.adapter = homePageViewState.recyclerAdapterStructure
                }
            }
        }
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
        labels.text = viewModel.showTag(postModel)

        val mAlertDialog = mBuilder.create()
        mDialogView.findViewById<Button>(R.id.dw_ok).setOnClickListener {
            mAlertDialog.dismiss()
        }
        mAlertDialog.show()
    }

    override fun onOtherOperationsClick(postModel: PostModel) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet, view.findViewById(R.id.bottomSheetContainer)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                viewModel.getSaveOperations(postModel = postModel)
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
                    viewModel.reportPost(postModel = postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // İPTAL butonu
        bottomSheetView.findViewById<View>(R.id.bs_cancel)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    bottomSheetDialog.dismiss()
                }
            })
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}