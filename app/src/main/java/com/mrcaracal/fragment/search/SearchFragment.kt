package com.mrcaracal.fragment.search

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.Cities
import com.mrcaracal.modul.MyArrayList
import java.text.DateFormat

class SearchFragment : Fragment(), RecyclerViewClickInterface {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var postIDsFirebase: ArrayList<String>
    lateinit var userEmailsFirebase: ArrayList<String>
    lateinit var pictureLinksFirebase: ArrayList<String>
    lateinit var placeNamesFirebase: ArrayList<String>
    lateinit var locationFirebase: ArrayList<String>
    lateinit var addressesFirebase: ArrayList<String>
    lateinit var citiesFirebase: ArrayList<String>
    lateinit var commentsFirebase: ArrayList<String>
    lateinit var postCodesFirebase: ArrayList<String>
    lateinit var tagsFirebase: ArrayList<String>
    lateinit var timesFirebase: ArrayList<Timestamp>
    private lateinit var recycler_view_search: RecyclerView
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure
    private lateinit var img_finfByLocation: ImageView
    private lateinit var edt_keyValueSearch: EditText
    private lateinit var sp_searchByWhat: Spinner
    private lateinit var sp_cities: Spinner
    var keyValue = "yerIsmi"
    private lateinit var selectionOptions: MyArrayList
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup
    private lateinit var sp_adapterAccordingToWhat: ArrayAdapter<String>
    private lateinit var sp_adapterCities: ArrayAdapter<String>

    private lateinit var firebaseOperationForSearch: FirebaseOperationForSearch

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()
        postIDsFirebase = ArrayList()
        userEmailsFirebase = ArrayList()
        pictureLinksFirebase = ArrayList()
        placeNamesFirebase = ArrayList()
        locationFirebase = ArrayList()
        addressesFirebase = ArrayList()
        citiesFirebase = ArrayList()
        commentsFirebase = ArrayList()
        postCodesFirebase = ArrayList()
        tagsFirebase = ArrayList()
        timesFirebase = ArrayList()
        GET = activity!!.getSharedPreferences(R.string.map_key.toString(), Context.MODE_PRIVATE)
        SET = GET.edit()

        selectionOptions = MyArrayList()

        firebaseOperationForSearch = FirebaseOperationForSearch(
            postIDsFirebase,
            userEmailsFirebase,
            pictureLinksFirebase,
            placeNamesFirebase,
            locationFirebase,
            addressesFirebase,
            citiesFirebase,
            commentsFirebase,
            postCodesFirebase,
            tagsFirebase,
            timesFirebase
        )
    }

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewGroup = inflater.inflate(R.layout.frag_search, container, false) as ViewGroup
        init()
        img_finfByLocation = viewGroup.findViewById(R.id.img_finfByLocation)
        img_finfByLocation.setOnClickListener(View.OnClickListener {
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
                firebaseOperationForSearch.clearList()
                recycler_view_search.scrollToPosition(0)
                //listNearbyPlaces()
                firebaseOperationForSearch.listNearbyPlaces(activity!!, recyclerAdapterStructure)
            }
        })

        sp_searchByWhat = viewGroup.findViewById(R.id.sp_searchByWhat)
        sp_adapterAccordingToWhat =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, selectionOptions.accordingToWhat)
        sp_adapterAccordingToWhat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_searchByWhat.adapter = sp_adapterAccordingToWhat
        sp_searchByWhat.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if ((parent.selectedItem.toString() == selectionOptions.accordingToWhat[0])) {
                    edt_keyValueSearch.visibility = View.VISIBLE
                    sp_cities.visibility = View.INVISIBLE
                    firebaseOperationForSearch.clearList()
                    recycler_view_search.scrollToPosition(0)
                    keyValue = "yerIsmi"
                }
                if ((parent.selectedItem.toString() == selectionOptions.accordingToWhat[1].toString())) {
                    edt_keyValueSearch.visibility = View.VISIBLE
                    sp_cities.visibility = View.INVISIBLE
                    firebaseOperationForSearch.clearList()
                    recycler_view_search.scrollToPosition(0)
                    keyValue = "taglar"
                }
                if ((parent.selectedItem.toString() == selectionOptions.accordingToWhat[2].toString())) {
                    edt_keyValueSearch.visibility = View.INVISIBLE
                    sp_cities.visibility = View.VISIBLE
                    firebaseOperationForSearch.clearList()
                    recycler_view_search.scrollToPosition(0)
                    keyValue = "sehir"
                }
                if ((parent.selectedItem.toString() == selectionOptions.accordingToWhat[3].toString())) {
                    edt_keyValueSearch.visibility = View.VISIBLE
                    sp_cities.visibility = View.INVISIBLE
                    firebaseOperationForSearch.clearList()
                    recycler_view_search.scrollToPosition(0)
                    keyValue = "kullaniciEposta"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val cities_al = Cities()
        sp_cities = viewGroup.findViewById(R.id.sp_cities)
        sp_adapterCities =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, cities_al.cities)
        sp_adapterCities.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_cities.adapter = sp_adapterCities
        sp_cities.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                firebaseOperationForSearch.clearList()
                recycler_view_search.scrollToPosition(0)
                if ((keyValue == "sehir")) {
                    val cities = Cities()
                    val selectedCityCode = cities.selectedCity(parent.selectedItem.toString())
                    if ((selectedCityCode == "Şehir Seçin!")) {
                        Toast.makeText(activity, R.string.please_select_city.toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        // VT'de Gonderiler bölümünde posta kodu alınan değerle başlayan tüm gonderileri çeken bir algoritma geliştir.
                        if (selectedCityCode != null) {
                            //searchForCity(selectedCityCode)
                            firebaseOperationForSearch.searchForCity(
                                selectedCityCode,
                                FirebaseFirestore.getInstance(),
                                recyclerAdapterStructure
                            )
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        edt_keyValueSearch = viewGroup.findViewById(R.id.edt_keyValueSearch)
        edt_keyValueSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                firebaseOperationForSearch.clearList()
                if ((keyValue == "taglar")) {
                    //searchForTag(keyValue, s.toString().lowercase())
                    firebaseOperationForSearch.searchForTag(
                        keyValue,
                        s.toString().lowercase(),
                        FirebaseFirestore.getInstance(),
                        recyclerAdapterStructure
                    )

                } else {
                    //search(keyValue, s.toString().lowercase())
                    firebaseOperationForSearch.search(
                        keyValue,
                        s.toString().lowercase(),
                        FirebaseFirestore.getInstance(),
                        recyclerAdapterStructure
                    )
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
        recycler_view_search = viewGroup.findViewById(R.id.recycler_view_search)
        recycler_view_search.layoutManager = LinearLayoutManager(activity)
        recyclerAdapterStructure = RecyclerAdapterStructure(
            (postIDsFirebase),
            (userEmailsFirebase),
            (pictureLinksFirebase),
            (placeNamesFirebase),
            (locationFirebase),
            (addressesFirebase),
            (citiesFirebase),
            (commentsFirebase),
            (postCodesFirebase),
            (tagsFirebase),
            timesFirebase,
            this
        )
        recycler_view_search.adapter = recyclerAdapterStructure
        return viewGroup
    }

    fun goToLocationOperations(position: Int) {
        val postLocation = locationFirebase[position].split(",").toTypedArray()
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

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    override fun onLongItemClick(position: Int) {
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            timesFirebase[position].toDate()
        )

        val showDetailPost =
            (commentsFirebase.get(position) +
                    R.string.sharing.toString() + "\n\n: " + userEmailsFirebase[position] +
                    R.string.date.toString() + "\n: " + dateAndTime +
                    R.string.addres.toString() + "\n: " + addressesFirebase[position] +
                    R.string.labels.toString() + "\n\n: " + firebaseOperationForSearch.showTag(position))
        val alert = AlertDialog.Builder(activity)
        alert
            .setTitle(placeNamesFirebase[position])
            .setMessage(showDetailPost)
            .setNegativeButton(R.string.ok.toString()) { dialog, which ->
                //
            }
            .show()
    }

    override fun onOtherOperationsClick(position: Int) {
        onOpenDialogWindow(position)
    }

    override fun onOpenDialogWindow(position: Int) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup.findViewById(R.id.bottomSheetContainer)
            )

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                //saveOperations(position)
                firebaseOperationForSearch.saveOperations(position, firebaseUser, firebaseFirestore)
                bottomSheetDialog.dismiss()
            })

        // Konuma Git
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    goToLocationOperations(position)
                    bottomSheetDialog.dismiss()
                }
            })

        // Detaylı Şikayet Bildir
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if ((userEmailsFirebase[position] == firebaseUser.email)) {
                        Toast.makeText(activity, R.string.you_already_shared_this.toString(), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val contactInfo = MyArrayList()
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_account)
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        intent.type = "plain/text"
                        startActivity(Intent.createChooser(intent, R.string.what_would_u_like_to_send_with.toString()))
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
                    firebaseOperationForSearch.listNearbyPlaces(
                        it,
                        recyclerAdapterStructure
                    )
                }
            } else {
                Toast.makeText(activity, R.string.not_allowed.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private val TAG = "SearchFragment"
        private val REQUEST_CODE_LOCATION_PERMISSON = 203
    }
}