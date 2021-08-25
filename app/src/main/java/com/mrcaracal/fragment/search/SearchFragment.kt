package com.mrcaracal.fragment.search

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import com.mrcaracal.extensions.toast
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragSearchBinding
import com.mrcaracal.modul.Cities
import com.mrcaracal.modul.UserAccountStore
import com.mrcaracal.utils.IntentProcessor
import java.text.DateFormat
import java.util.*

class
SearchFragment : Fragment(), RecyclerViewClickInterface {

    private lateinit var viewModel: SearchViewModel
    private var _binding: FragSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var keyValue = "yerIsmi"
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup
    private lateinit var sp_adapterAccordingToWhat: ArrayAdapter<String>
    private lateinit var sp_adapterCities: ArrayAdapter<String>

    private fun init() {
        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
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
        initViewModel()
        viewModel.init()
        initClickListener()
        initSelectListener()
        initChangedListener()
        observeSearchState()
        initSpinnerMethod()
        initSpinnerCity()

        binding.recyclerViewSearch.layoutManager = LinearLayoutManager(activity)
        viewModel.recyclerAdapterProccese(thisClick = this)

        return view
    }

    fun initViewModel() {
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
    }

    fun initClickListener() {
        binding.imgFinfByLocation.setOnClickListener(View.OnClickListener {
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
        })
    }

    fun initSelectListener() {
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
                        Toast.makeText(
                            activity,
                            getString(R.string.please_select_city),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (selectedCityCode != null) {
                            viewModel.searchForCity(selectedCityCode)
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
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[1].toString())) {
                    searchViewVisibilityState()
                    keyValue = "taglar"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[2].toString())) {
                    binding.edtKeyValueSearch.visibility = View.INVISIBLE
                    binding.spCities.visibility = View.VISIBLE
                    viewModel.clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "sehir"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[3].toString())) {
                    searchViewVisibilityState()
                    keyValue = "kullaniciEposta"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    fun searchViewVisibilityState(){
        binding.edtKeyValueSearch.visibility = View.VISIBLE
        binding.spCities.visibility = View.INVISIBLE
        viewModel.clearList()
        binding.recyclerViewSearch.scrollToPosition(0)
    }

    fun initChangedListener() {
        binding.edtKeyValueSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.clearList()
                if ((keyValue == "taglar")) {
                    viewModel.searchForTag(keyValue, s.toString().lowercase())
                } else {
                    viewModel.search(keyValue, s.toString().lowercase())
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun initSpinnerMethod() {
        sp_adapterAccordingToWhat =
            ArrayAdapter(
                (activity)!!,
                android.R.layout.simple_spinner_item,
                UserAccountStore.accordingToWhat
            )
        sp_adapterAccordingToWhat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSearchByWhat.adapter = sp_adapterAccordingToWhat
    }

    fun initSpinnerCity() {
        val cities_al = Cities()
        sp_adapterCities =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, cities_al.cities)
        sp_adapterCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCities.adapter = sp_adapterCities
    }

    fun observeSearchState() {
        viewModel.searchState.observe(viewLifecycleOwner) { searchViewState ->
            when (searchViewState) {
                is SearchViewState.ShowAlreadySharedToastMessage -> {
                    context?.let { toast(it, R.string.you_already_shared_this) }
                }
                is SearchViewState.OpenEmail -> {
                    context?.let {
                        IntentProcessor.process(
                            context = it,
                            emails = searchViewState.emails,
                            subject = searchViewState.subject,
                            text = searchViewState.message
                        )
                    }
                }
                is SearchViewState.SendRecyclerAdapter -> {
                    binding.recyclerViewSearch.adapter = searchViewState.recyclerAdapterStructure
                }
                is SearchViewState.ShowExceptionMessage -> {
                    context?.let { toast(it, searchViewState.exception.toString()) }
                }
            }
        }
    }

    //There are problems converting to ViewModel
    fun listNearbyPlaces() {
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
                            var addressList: List<Address>
                            try {
                                addressList = geocoder.getFromLocation(latitude, longitude, 1)
                                if (addressList != null) {
                                    val postaKodumuz = addressList[0].postalCode
                                    viewModel.searchForCity(
                                        postaKodumuz.substring(0, 2)
                                    )
                                }
                            } catch (e: Exception) {
                                context?.let { toast(it, e.toString()) }
                            }
                        }
                    }
                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                    }
                }, Looper.getMainLooper())
        } catch (e: Exception) {
            e.printStackTrace()
            context?.let { toast(it, e.toString()) }
        }
    }

    fun goToLocationOperations(postModel: PostModel) {
        val postLocation = postModel.location.split(",").toTypedArray()
        var adverb = 0
        for (konumxy: String in postLocation) {
            adverb++
            if (adverb == 1) latitude = konumxy.toDouble()
            if (adverb == 2) longitude = konumxy.toDouble()
        }
        SET.putFloat("konum_git_enlem", latitude.toFloat())
        SET.putFloat("konum_git_boylam", longitude.toFloat())
        SET.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
    }

    override fun onLongItemClick(postModel: PostModel) {
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            postModel.time.toDate()
        )
        val showDetailPost =
            (postModel.comment +
                    "\n\n${getString(R.string.sharing)}: " + postModel.userEmail +
                    "\n${getString(R.string.date)}: " + dateAndTime +
                    "\n${getString(R.string.addres)}: " + postModel.address +
                    "\n\n" + viewModel.showTag(postModel))
        val alert = AlertDialog.Builder(activity)
        alert
            .setTitle(postModel.placeName)
            .setMessage(showDetailPost)
            .setNegativeButton(getString(R.string.ok)) { _dialog, which ->
                //
            }
            .show()
    }

    override fun onOtherOperationsClick(postModel: PostModel) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup.findViewById(R.id.bottomSheetContainer)
            )

        // Save Post
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                viewModel.saveOperations(postModel)
                bottomSheetDialog.dismiss()
            })

        // Go To Location
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    goToLocationOperations(postModel)
                    bottomSheetDialog.dismiss()
                }
            })

        // Report Post
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    viewModel.reportPost(postModel = postModel)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSON && grantResults.size > 0) {
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
        private val TAG = "SearchFragment"
        private val REQUEST_CODE_LOCATION_PERMISSON = 203
    }
}