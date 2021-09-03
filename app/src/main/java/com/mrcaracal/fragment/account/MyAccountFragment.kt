package com.mrcaracal.fragment.account

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.activity.editProfile.EditProfileActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragMyAccountBinding
import com.mrcaracal.utils.ConstantsMap
import com.mrcaracal.utils.DialogViewCustomize
import com.squareup.picasso.Picasso

class MyAccountFragment : Fragment(), RecyclerViewClickInterface {

    private lateinit var viewModel: MyAccountViewModel
    private var _binding: FragMyAccountBinding? = null
    private val binding get() = _binding!!

    var POSITION_VALUE = 0
    var TAB_CONTROL = "paylasilanlar"
    var LATITUDE = 0.0
    var LONGITUDE = 0.0

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    private lateinit var container: ViewGroup

    private fun init() {
        GET = requireActivity().getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
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

        _binding = FragMyAccountBinding.inflate(inflater, container, false)
        val view = binding.root
        initViewModel()
        initClickListeners()
        observeMyAccountState()

        binding.recyclerViewAccount.layoutManager = LinearLayoutManager(activity)
        viewModel.recyclerAdapterProccese(thisClick = this)
        viewModel.pullTheShared()
        viewModel.getData()

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MyAccountViewModel::class.java)
    }

    fun initClickListeners() {
        binding.btnEditProfile.setOnClickListener(View.OnClickListener {
            val editProfile = Intent(activity, EditProfileActivity::class.java)
            startActivity(editProfile)
        })
        binding.bnAccountMenu.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.shared -> {
                    TAB_CONTROL = "paylasilanlar"
                    viewModel.clearList()
                    viewModel.pullTheShared()
                    binding.recyclerViewAccount.scrollToPosition(0)
                }
                R.id.recorded -> {
                    TAB_CONTROL = "kaydedilenler"
                    viewModel.clearList()
                    viewModel.pullTheRecorded()
                    binding.recyclerViewAccount.scrollToPosition(0)
                }
            }
            true
        }
    }

    fun observeMyAccountState() {
        viewModel.myAccountState.observe(viewLifecycleOwner) { myAccountViewState ->
            when (myAccountViewState) {
                is MyAccountViewState.ShowExceptionMessage -> {
                    context?.let { toast(it, myAccountViewState.exception.toString()) }
                }
                is MyAccountViewState.ShowUserNameAndBio -> {
                    binding.tvUserName.text = myAccountViewState.userName
                    binding.tvUserBio.text = myAccountViewState.bio
                }
                is MyAccountViewState.PicassoProccese -> {
                    Picasso.get().load(myAccountViewState.loadData)
                        .into(binding.imgProfileProfilePicture)
                }
                is MyAccountViewState.PicassoProcceseDefault -> {
                    Picasso.get().load(R.drawable.defaultpp)
                        .into(binding.imgProfileProfilePicture)
                }
                is MyAccountViewState.SendRecyclerAdapter -> {
                    binding.recyclerViewAccount.adapter =
                        myAccountViewState.recyclerAdapterStructure
                }
            }
        }
    }

    fun goToLocationFromShared(postModel: PostModel) {
        goToLocation(postModel = postModel)
    }

    fun goToLocationFromSaved(postModel: PostModel) {
        goToLocation(postModel = postModel)
    }

    private fun goToLocation(postModel: PostModel) {
        val postLocation = postModel.location.split(",").toTypedArray()
        var adverb = 0
        for (locationXY: String in postLocation) {
            adverb++
            if (adverb == 1) LATITUDE = locationXY.toDouble()
            if (adverb == 2) LONGITUDE = locationXY.toDouble()
        }
        SET.putFloat(ConstantsMap.GO_TO_LOCATION_LATITUDE, LATITUDE.toFloat())
        SET.putFloat(ConstantsMap.GO_TO_LOCATION_LONGITUDE, LONGITUDE.toFloat())
        SET.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
    }

    override fun onLongItemClick(postModel: PostModel) {
        var postTags = viewModel.showTag(postModel, TAB_CONTROL)

        DialogViewCustomize.dialogViewCustomize(
            activity = activity,
            container = container,
            postModel = postModel,
            postTags = postTags
        )
    }

    override fun onOtherOperationsClick(postModel: PostModel) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet_hesabim,
                view?.findViewById(R.id.bottomSheetContainer_hesabim)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // Go to locaiton
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation).setOnClickListener(
            View.OnClickListener {
                when (TAB_CONTROL) {
                    "paylasilanlar" -> goToLocationFromShared(postModel)
                    "kaydedilenler" -> goToLocationFromSaved(postModel)
                }
                bottomSheetDialog.dismiss()
            })

        // Remove
        bottomSheetView.findViewById<View>(R.id.bs_remove)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    when (TAB_CONTROL) {
                        "paylasilanlar" -> {
                            viewModel.removeFromShared(
                                POSITION_VALUE
                            )
                            viewModel.clearList()
                            viewModel.pullTheShared()
                            binding.recyclerViewAccount.scrollToPosition(0)
                        }
                        "kaydedilenler" -> {
                            viewModel.removeFromSaved(
                                POSITION_VALUE
                            )
                            viewModel.clearList()
                            viewModel.pullTheRecorded()
                            binding.recyclerViewAccount.scrollToPosition(0)
                        }
                    }
                    bottomSheetDialog.dismiss()
                }
            })

        // cancel
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