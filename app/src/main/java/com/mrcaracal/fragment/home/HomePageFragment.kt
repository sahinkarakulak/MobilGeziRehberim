package com.mrcaracal.fragment.home

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
import com.mrcaracal.adapter.PostAdapter
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragHomePageBinding
import com.mrcaracal.utils.*
import java.util.*

class HomePageFragment : Fragment(), RecyclerViewClickInterface {

    lateinit var viewModel: HomePageViewModel
    private var _binding: FragHomePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0
    val postModelsList: ArrayList<PostModel> = arrayListOf()
    private lateinit var container: ViewGroup
    lateinit var postAdapter: PostAdapter

    private fun init() {
        GET = requireActivity().getSharedPreferences(
            getString(R.string.map_key),
            Context.MODE_PRIVATE
        )
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
        observeHomePageState()
        recyclerViewManager()
        viewModel.getPostByPostTime(postModelsList = postModelsList)

        // Swipe Refresh ...
        /*binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getPostByPostTime(postModelsList = postModelsList)
            binding.swipeRefreshLayout.isRefreshing = true
        }*/

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(HomePageViewModel::class.java)
    }

    fun recyclerViewManager() {
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        postAdapter = PostAdapter(recyclerViewClickInterface = this)
    }

    private fun observeHomePageState() {
        viewModel.homePageState.observe(viewLifecycleOwner) { homePageViewState ->
            when (homePageViewState) {
                is HomePageViewState.OpenEmail -> {
                    context?.let {
                        IntentProcessor.processForEmail(
                            context = it,
                            emails = homePageViewState.emails,
                            subject = homePageViewState.subject,
                            text = homePageViewState.message
                        )
                    }
                }
                is HomePageViewState.ShowAlreadySharedToastMessage -> {
                    toast(requireActivity(), getString(R.string.you_already_shared_this))
                }
                is HomePageViewState.SendRecyclerAdapter -> {
                    postAdapter.postModelList = postModelsList
                    postAdapter.notifyDataSetChanged()
                    binding.recyclerView.adapter = postAdapter
                }
                else -> {
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
        SET.putFloat(ConstantsMap.GO_TO_LOCATION_LATITUDE, latitude.toFloat())
        SET.putFloat(ConstantsMap.GO_TO_LOCATION_LONGITUDE, longitude.toFloat())
        SET.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
    }

    override fun onLongItemClick(postModel: PostModel) {
        val postTags = viewModel.showTagsOnPost(postModel = postModel)

        // Data send to PostDetailFragment()
        val bundle = Bundle()
        bundle.putString("postTags", postTags)
        bundle.putSerializable("postModel", postModel)

        val selectedFragment = SelectFragment.selectFragment(Constants.SELECT_DETAIL_FRAGMENT)
        selectedFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, selectedFragment)
            .commit()

        /*DialogViewCustomize.dialogViewCustomize(
            activity = activity,
            container = container,
            postModel = postModel,
            postTags = postTags
        )*/

    }

    override fun onOtherOperationsClick(postModel: PostModel) {
        /*PostDetailBottomSheet.ShowPostDataDetails(
            context = requireContext(),
            postModel = postModel,
            container = container
        )*/
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet, container.findViewById(R.id.bottomSheetContainer)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // Save Post
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener {
            viewModel.saveOperations(postModel = postModel)
            bottomSheetDialog.dismiss()
        }

        // Go to locaiton
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    goToLocationOperations(postModel = postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // Report Post
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    viewModel.reportPostFromHomePage(postModel = postModel)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}