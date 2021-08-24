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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragSearchBinding
import com.mrcaracal.modul.Cities
import com.mrcaracal.modul.Posts
import com.mrcaracal.modul.UserAccountStore
import java.text.DateFormat
import java.util.*

class
SearchFragment : Fragment(), RecyclerViewClickInterface {

    private var _binding: FragSearchBinding? = null
    private val binding get() = _binding!!

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure
    private lateinit var selectionOptions: UserAccountStore
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var keyValue = "yerIsmi"
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup
    private lateinit var sp_adapterAccordingToWhat: ArrayAdapter<String>
    private lateinit var sp_adapterCities: ArrayAdapter<String>

    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()
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
                clearList()
                binding.recyclerViewSearch.scrollToPosition(0)
                listNearbyPlaces()
            }
        })

        sp_adapterAccordingToWhat =
            ArrayAdapter(
                (activity)!!,
                android.R.layout.simple_spinner_item,
                UserAccountStore.accordingToWhat
            )
        sp_adapterAccordingToWhat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spSearchByWhat.adapter = sp_adapterAccordingToWhat
        binding.spSearchByWhat.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[0])) {
                    binding.edtKeyValueSearch.visibility = View.VISIBLE
                    binding.spCities.visibility = View.INVISIBLE
                    clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "yerIsmi"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[1].toString())) {
                    binding.edtKeyValueSearch.visibility = View.VISIBLE
                    binding.spCities.visibility = View.INVISIBLE
                    clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "taglar"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[2].toString())) {
                    binding.edtKeyValueSearch.visibility = View.INVISIBLE
                    binding.spCities.visibility = View.VISIBLE
                    clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "sehir"
                }
                if ((parent.selectedItem.toString() == UserAccountStore.accordingToWhat[3].toString())) {
                    binding.edtKeyValueSearch.visibility = View.VISIBLE
                    binding.spCities.visibility = View.INVISIBLE
                    clearList()
                    binding.recyclerViewSearch.scrollToPosition(0)
                    keyValue = "kullaniciEposta"
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val cities_al = Cities()
        sp_adapterCities =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, cities_al.cities)
        sp_adapterCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spCities.adapter = sp_adapterCities
        binding.spCities.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                clearList()
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
                        // VT'de Gonderiler bölümünde posta kodu alınan değerle başlayan tüm gonderileri çeken bir algoritma geliştir.
                        if (selectedCityCode != null) {
                            searchForCity(selectedCityCode)
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.edtKeyValueSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearList()
                if ((keyValue == "taglar")) {
                    searchForTag(keyValue, s.toString().lowercase())
                } else {
                    search(keyValue, s.toString().lowercase())
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
        binding.recyclerViewSearch.layoutManager = LinearLayoutManager(activity)
        recyclerAdapterStructure = RecyclerAdapterStructure(
            recyclerViewClickInterface = this
        )
        binding.recyclerViewSearch.adapter = recyclerAdapterStructure
        return view
    }

    fun clearList() {
        postModelsList.clear()
    }

    fun getData(data: Map<String, Any>?) {
        data?.let {
            val postModel = PostModelProvider.provide(it)
            postModelsList.add(postModel)
        }
    }

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
                                    searchForCity(
                                        postaKodumuz.substring(0, 2)
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                    }
                }, Looper.getMainLooper())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun search(relevantField: String?, keywordWrited: String) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .orderBy((relevantField)!!)
            .startAt(keywordWrited)
            .endAt(keywordWrited + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getData(snapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { e ->
                //
            }
    }

    fun searchForTag(relevantField: String?, keywordWrited: String?) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .whereArrayContains((relevantField)!!, (keywordWrited)!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        getData(documentSnapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener {

            }
    }

    fun searchForCity(postCode: String) {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .orderBy("postaKodu")
            .startAt(postCode)
            .endAt(postCode + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getData(snapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { e ->
                //
            }
    }

    fun saveOperations(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            //
        } else {
            val MGonderiler = Posts(
                postModel.postId,
                postModel.userEmail,
                postModel.pictureLink,
                postModel.placeName,
                postModel.location,
                postModel.address,
                postModel.city,
                postModel.comment,
                postModel.postCode,
                listOf(postModel.tag),
                FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                .collection(COLLECTION_NAME_THEY_SAVED)
                .document((firebaseUser.email)!!)
                .collection(COLLECTION_NAME_SAVED)
                .document(postModel.postId)
            documentReference
                .set(MGonderiler)
                .addOnSuccessListener {
                    //
                }
                .addOnFailureListener { e ->
                    //
                }
        }
    }

    fun showTag(postModel: PostModel): String {
        var taggg = ""
        val al_taglar = postModel.tag
        val tag_uzunluk = al_taglar.length
        val alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
        val a_t = alinan_taglar.split(",").toTypedArray()
        for (tags: String in a_t) {
            taggg += "#" + tags.trim { it <= ' ' } + " "
        }
        return taggg
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
                    "\n\n" + showTag(postModel))
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

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                saveOperations(postModel)
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

        // Detaylı Şikayet Bildir
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if ((postModel.userEmail == firebaseUser.email)) {
                        Toast.makeText(
                            activity,
                            getString(R.string.you_already_shared_this),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_EMAIL, UserAccountStore.adminAccountEmails)
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