package com.mrcaracal.fragment.search

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.PostAdapter
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragSearchBinding
import com.mrcaracal.modul.Cities
import com.mrcaracal.modul.UserAccountStore
import com.mrcaracal.utils.ConstantsMap
import com.mrcaracal.utils.DialogViewCustomize
import com.mrcaracal.utils.IntentProcessor
import java.util.*

class SearchFragment : Fragment(), RecyclerViewClickInterface {

    private lateinit var viewModel: SearchViewModel
    private var _binding: FragSearchBinding? = null
    private val binding get() = _binding!!
    lateinit var postAdapter: PostAdapter

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var keyValue = "yerIsmi"
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup
    private lateinit var sp_adapterAccordingToWhat: ArrayAdapter<String>
    private lateinit var sp_adapterCities: ArrayAdapter<String>

    private lateinit var container: ViewGroup

    private fun init() {
        GET = requireActivity().getSharedPreferences(
            getString(R.string.map_key),
            Context.MODE_PRIVATE
        )
        SET = GET.edit()
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        init()
        if (container != null) {
            this.container = container
        }
        initViewModel()
        initClickListener()
        initSelectListener()
        initChangedListener()
        observeSearchState()
        initSpinnerMethod()
        initSpinnerCity()
        recyclerViewManager()

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    fun recyclerViewManager() {
        binding.recyclerViewSearch.layoutManager = LinearLayoutManager(activity)
        postAdapter = PostAdapter(recyclerViewClickInterface = this)
    }

    private fun initClickListener() {
        binding.imgFinfByLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    (activity)!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    (activity)!!,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSON
                )
            } else {
                viewModel.clearList()
                binding.recyclerViewSearch.scrollToPosition(0)
                listNearbyPlaces()
            }
        }
    }

    private fun initSelectListener() {
        binding.spCities.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                viewModel.clearList()
                binding.recyclerViewSearch.scrollToPosition(0)
                if ((keyValue == "sehir")) {
                    val cities = Cities()
                    val selectedCityCode = cities.selectedCity(parent.selectedItem.toString())
                    if ((selectedCityCode == "Şehir Seçin!")) {
                        toast(container.context, R.string.please_select_city)
                    } else {
                        if (selectedCityCode != null) {
                            viewModel.searchByCity(postCode = selectedCityCode)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.spSearchByWhat.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[0])) {
                    searchViewVisibilityState()
                    keyValue = "yerIsmi"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[1])) {
                    searchViewVisibilityState()
                    keyValue = "taglar"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[2])) {
                    binding.edtKeyValueSearch.visibility = View.INVISIBLE
                    binding.spCities.visibility = View.VISIBLE
                    viewModel.clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "sehir"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[3])) {
                    searchViewVisibilityState()
                    keyValue = "kullaniciEposta"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun searchViewVisibilityState() {
        binding.edtKeyValueSearch.visibility = View.VISIBLE
        binding.spCities.visibility = View.INVISIBLE
        viewModel.clearList()
        binding.recyclerViewSearch.scrollToPosition(0)
    }

    private fun initChangedListener() {
        binding.edtKeyValueSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.clearList()
                if ((keyValue == "taglar")) {
                    viewModel.searchByTag(
                        relevantField = keyValue,
                        keywordWrited = s.toString().lowercase()
                    )
                } else {
                    viewModel.searchOnPost(
                        relevantField = keyValue,
                        keywordWrited = s.toString().lowercase()
                    )
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun initSpinnerMethod() {
        sp_adapterAccordingToWhat =
            ArrayAdapter(
                (activity)!!,
                android.R.layout.simple_spinner_item,
                UserAccountStore.accordingToWhat
            )
        sp_adapterAccordingToWhat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSearchByWhat.adapter = sp_adapterAccordingToWhat
    }

    private fun initSpinnerCity() {
        val citiesAl = Cities()
        sp_adapterCities =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, citiesAl.cities)
        sp_adapterCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCities.adapter = sp_adapterCities
    }

    private fun observeSearchState() {
        viewModel.searchState.observe(viewLifecycleOwner) { searchViewState ->
            when (searchViewState) {
                is SearchViewState.ShowAlreadySharedToastMessage -> {
                    toast(container.context, R.string.you_already_shared_this)
                }
                is SearchViewState.OpenEmail -> {
                    context?.let {
                        IntentProcessor.processForEmail(
                            context = it,
                            emails = searchViewState.emails,
                            subject = searchViewState.subject,
                            text = searchViewState.message
                        )
                    }
                }
                is SearchViewState.SendRecyclerAdapter -> {
                    postAdapter.postModelList = searchViewState.postModelsList
                    postAdapter.notifyDataSetChanged()
                    binding.recyclerViewSearch.adapter = postAdapter
                }
                is SearchViewState.ShowExceptionMessage -> {
                    toast(container.context, searchViewState.exception.toString())
                }
            }
        }
    }

    private fun listNearbyPlaces() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        try {
            if (context?.let {
                    ActivityCompat.checkSelfPermission(
                        it,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED && context?.let {
                    ActivityCompat.checkSelfPermission(
                        it, Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            LocationServices.getFusedLocationProviderClient(activity)
                .requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        LocationServices.getFusedLocationProviderClient(activity)
                            .removeLocationUpdates(this)
                        if (locationResult.locations.size > 0) {
                            val lastestLocationIndex = locationResult.locations.size - 1
                            val latitude = locationResult.locations[lastestLocationIndex].latitude
                            val longitude = locationResult.locations[lastestLocationIndex].longitude
                            val geocoder = Geocoder(activity, Locale.getDefault())
                            val addressList: List<Address>
                            try {
                                addressList = geocoder.getFromLocation(latitude, longitude, 1)
                                if (addressList != null) {
                                    val postaKodumuz = addressList[0].postalCode
                                    viewModel.searchByCity(
                                        postCode = postaKodumuz.substring(0, 2)
                                    )
                                }
                            } catch (e: Exception) {
                                toast(container.context, e.toString())
                            }
                        }
                    }

                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                    }
                }, Looper.getMainLooper())
        } catch (e: Exception) {
            toast(container.context, e.toString())
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
                R.layout.layout_bottom_sheet,
                view?.findViewById(R.id.bottomSheetContainer)
            )

        // Save Post
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener {
            viewModel.savePostOnSearchFragment(postModel = postModel)
            bottomSheetDialog.dismiss()
        }

        // Go To Location
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener {
                goToLocationOperations(postModel = postModel)
                bottomSheetDialog.dismiss()
            }

        // Report Post
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener {
                viewModel.reportPostFromSearchFragment(postModel = postModel)
                bottomSheetDialog.dismiss()
            }

        // Cancel
        bottomSheetView.findViewById<View>(R.id.bs_cancel)
            .setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSON && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //listNearbyPlaces()
                activity?.let {
                    listNearbyPlaces()
                }
            } else {
                Toast.makeText(activity, getString(R.string.not_allowed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_LOCATION_PERMISSON = 203
    }
}