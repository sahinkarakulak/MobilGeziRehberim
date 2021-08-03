package com.mrcaracal.fragment

import android.Manifest
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
import android.util.Log
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
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.Cities
import com.mrcaracal.modul.ContactInfo
import com.mrcaracal.modul.Posts
import java.text.DateFormat
import java.util.*

class SearchFragment() : Fragment(), RecyclerViewClickInterface {
    private val accordingToWhat = arrayOf("Yer İsmi", "Etiket", "Şehir", "Kullanıcı")

    var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var firebaseFirestore: FirebaseFirestore? = null
    var postIDsFirebase: ArrayList<String>? = null
    var userEmailsFirebase: ArrayList<String>? = null
    var pictureLinksFirebase: ArrayList<String>? = null
    var placeNamesFirebase: ArrayList<String>? = null
    var locationFirebase: ArrayList<String>? = null
    var addressesFirebase: ArrayList<String>? = null
    var citiesFirebase: ArrayList<String>? = null
    var commentsFirebase: ArrayList<String>? = null
    var postCodesFirebase: ArrayList<String>? = null
    var tagsFirebase: ArrayList<String>? = null
    private lateinit var timesFirebase: ArrayList<Timestamp>
    private lateinit var recycler_view_search: RecyclerView
    var recyclerAdapterStructure: RecyclerAdapterStructure? = null
    private lateinit var img_finfByLocation: ImageView
    private lateinit var edt_keyValueSearch: EditText
    private lateinit var sp_searchByWhat: Spinner
    private lateinit var sp_cities: Spinner
    var keyValue = "yerIsmi"
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0
    var viewGroup: ViewGroup? = null
    private var sp_adapterAccordingToWhat: ArrayAdapter<String>? = null
    private var sp_adapterCities: ArrayAdapter<String>? = null

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser
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
        GET = activity!!.getSharedPreferences("harita", Context.MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewGroup = inflater.inflate(R.layout.frag_search, container, false) as ViewGroup
        init()
        img_finfByLocation = viewGroup!!.findViewById(R.id.img_finfByLocation)
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
                clearList()
                recycler_view_search!!.scrollToPosition(0)
                listNearbyPlaces()
            }
        })
        sp_searchByWhat = viewGroup!!.findViewById(R.id.sp_searchByWhat)
        sp_adapterAccordingToWhat = ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, accordingToWhat)
        sp_adapterAccordingToWhat!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_searchByWhat.setAdapter(sp_adapterAccordingToWhat)
        sp_searchByWhat.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                if ((parent.selectedItem.toString() == accordingToWhat[0])) {
                    edt_keyValueSearch!!.visibility = View.VISIBLE
                    sp_cities!!.visibility = View.INVISIBLE

                    // Yer İsmine göre işlemler yapılsın.
                    clearList()
                    recycler_view_search!!.scrollToPosition(0)
                    keyValue = "yerIsmi"
                }
                if ((parent.selectedItem.toString() == accordingToWhat[1])) {
                    edt_keyValueSearch!!.visibility = View.VISIBLE
                    sp_cities!!.visibility = View.INVISIBLE

                    // Etikete göre işlemler yapılsın.
                    clearList()
                    recycler_view_search!!.scrollToPosition(0)
                    keyValue = "taglar"
                }

                // Bu item seçilirse Ara çubuğu spinner'a dönüşsün ve orada şehirler listelensin.
                // Seçilen şehre göre posta kodu değeri alınsın ve VT de ona göre bir arama yapılsın.
                if ((parent.selectedItem.toString() == accordingToWhat[2])) {
                    edt_keyValueSearch!!.visibility = View.INVISIBLE
                    sp_cities!!.visibility = View.VISIBLE
                    clearList()
                    recycler_view_search!!.scrollToPosition(0)
                    keyValue = "sehir"
                }
                if ((parent.selectedItem.toString() == accordingToWhat[3])) {
                    edt_keyValueSearch!!.visibility = View.VISIBLE
                    sp_cities!!.visibility = View.INVISIBLE

                    // Kullanıcıya göre işlemler yapılsın.
                    clearList()
                    recycler_view_search!!.scrollToPosition(0)
                    keyValue = "kullaniciEposta"
                }

                /*if(parent.getSelectedItem().toString().equals(neye_gore[4])){

                    recycler_view_ara.setVisibility(View.INVISIBLE);
                    webView = viewGroup.findViewById(R.id.webview);
                    webView.setVisibility(View.VISIBLE);
                    webView.getSettings().setJavaScriptEnabled(true);
                    //Zoom yapılabilinir olup olmadığını belirtiyoruz
                    webView.getSettings().setBuiltInZoomControls(true);
                    //Hangi adresi açacağını belirtiyoruz
                    webView.loadUrl("https://www.instagram.com/explore/tags/kapadokya/");
                    //Açılacak olan web adresinin uygulama içinde mi yoksa tarayıcı da mı açılacağını ayarlıyoruz
                    webView.setWebViewClient(new WebViewClient());
                }*/
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
        val cities_al = Cities()
        sp_cities = viewGroup!!.findViewById(R.id.sp_cities)
        sp_adapterCities =
            ArrayAdapter((activity)!!, android.R.layout.simple_spinner_item, cities_al.sehirler)
        sp_adapterCities!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sp_cities.setAdapter(sp_adapterCities)
        sp_cities.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                clearList()
                recycler_view_search!!.scrollToPosition(0)
                if ((keyValue == "sehir")) {
                    val cities = Cities()
                    val selectedCityCode = cities.sehirler(parent.selectedItem.toString())
                    /*Toast.makeText(getActivity(), "Seçilen şehir kodu: "+secilen_sehir_kodu, Toast.LENGTH_SHORT).show();*/if ((selectedCityCode == "Şehir Seçin!")) {
                        Toast.makeText(activity, "Lütfen Şehir Seçin!", Toast.LENGTH_SHORT).show()
                    } else {
                        // VT'de Gonderiler bölümünde posta kodu alınan değerle başlayan tüm gonderileri çeken bir algoritma geliştir.
                        searchForCity(selectedCityCode)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        })
        edt_keyValueSearch = viewGroup!!.findViewById(R.id.edt_keyValueSearch)
        edt_keyValueSearch.addTextChangedListener(object : TextWatcher {
            // Önce
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.i(TAG, "beforeTextChanged: Önce")
                //
            }

            // Esnasında
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: Esnasında")
                clearList()
                if ((keyValue == "taglar")) {
                    searchForTag(keyValue, s.toString().toLowerCase())
                } else {
                    search(keyValue, s.toString().toLowerCase())
                }
            }

            // Sonra
            override fun afterTextChanged(s: Editable) {
                Log.i(TAG, "afterTextChanged: Sonra")
                //
            }
        })
        recycler_view_search = viewGroup!!.findViewById(R.id.recycler_view_search)
        recycler_view_search.setLayoutManager(LinearLayoutManager(activity))
        recyclerAdapterStructure = RecyclerAdapterStructure(
            (postIDsFirebase)!!,
            (userEmailsFirebase)!!,
            (pictureLinksFirebase)!!,
            (placeNamesFirebase)!!,
            (locationFirebase)!!,
            (addressesFirebase)!!,
            (citiesFirebase)!!,
            (commentsFirebase)!!,
            (postCodesFirebase)!!,
            (tagsFirebase)!!,
            timesFirebase,
            this
        )
        recycler_view_search.setAdapter(recyclerAdapterStructure)
        return viewGroup
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSON && grantResults.size > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listNearbyPlaces()
            } else {
                Toast.makeText(activity, "İzin Verilmedi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listNearbyPlaces() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        try {
            if (ActivityCompat.checkSelfPermission(
                    (activity)!!,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    (activity)!!, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            LocationServices.getFusedLocationProviderClient(activity)
                .requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        LocationServices.getFusedLocationProviderClient(activity)
                            .removeLocationUpdates(this)
                        if (locationResult != null && locationResult.locations.size > 0) {
                            val lastestLocationIndex = locationResult.locations.size - 1
                            val latitude = locationResult.locations[lastestLocationIndex].latitude
                            val longitude = locationResult.locations[lastestLocationIndex].longitude
                            val geocoder = Geocoder(activity, Locale.getDefault())
                            var addressList: List<Address>? = null
                            try {
                                addressList = geocoder.getFromLocation(latitude, longitude, 1)

                                //if (addressList != null || !addressList!!.isEmpty()) {
                                if (addressList != null) {
                                    val postaKodumuz = addressList[0].postalCode
                                    searchForCity(postaKodumuz.substring(0, 2))
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

    fun clearList() {
        postIDsFirebase!!.clear()
        userEmailsFirebase!!.clear()
        pictureLinksFirebase!!.clear()
        placeNamesFirebase!!.clear()
        locationFirebase!!.clear()
        addressesFirebase!!.clear()
        citiesFirebase!!.clear()
        commentsFirebase!!.clear()
        postCodesFirebase!!.clear()
        tagsFirebase!!.clear()
        timesFirebase!!.clear()
    }

    fun veriCagir(data: Map<String, Any>?) {
        val dataCluster = data
        val postID = dataCluster!!["gonderiID"].toString()
        val userEmail = dataCluster["kullaniciEposta"].toString()
        var placeName = dataCluster["yerIsmi"].toString()
        placeName = placeName.substring(0, 1).toUpperCase() + placeName.substring(1)
        val pictureLink = dataCluster["resimAdresi"].toString()
        val location = dataCluster["konum"].toString()
        val addres = dataCluster["adres"].toString()
        val city = dataCluster["sehir"].toString()
        val comment = dataCluster["yorum"].toString()
        val postCode = dataCluster["postaKodu"].toString()
        val time = dataCluster["zaman"] as Timestamp?
        postIDsFirebase!!.add(postID)
        userEmailsFirebase!!.add(userEmail)
        pictureLinksFirebase!!.add(pictureLink)
        placeNamesFirebase!!.add(placeName)
        locationFirebase!!.add(location)
        addressesFirebase!!.add(addres)
        citiesFirebase!!.add(city)
        commentsFirebase!!.add(comment)
        postCodesFirebase!!.add(postCode)
        tagsFirebase!!.add(dataCluster["taglar"].toString())
        if (time != null) {
            timesFirebase!!.add(time)
        }
    }

    fun search(relevantField: String?, keywordWrited: String) {
        Log.i(TAG, "aramaYap: Çalıştı")
        clearList()

        //recycler_view_ara.scrollToPosition(0);
        val collectionReference = firebaseFirestore
            ?.collection("Gonderiler")
        if (collectionReference != null) {
            collectionReference
                .orderBy((relevantField)!!)
                .startAt(keywordWrited)
                .endAt(keywordWrited + "\uf8ff")
                .get()
                .addOnCompleteListener { task ->
                    Log.i(TAG, "onComplete: Veriler filtrelenmiş şekilde çekildi")
                    if (task.isSuccessful) {
                        val querySnapshot = (task.result)
                        for (snapshot: DocumentSnapshot in querySnapshot) {
                            veriCagir(snapshot.data)
                            recyclerAdapterStructure!!.notifyDataSetChanged()
                            Log.i(TAG, "onComplete: Sonu...")

                            // Arraylistlerin içinde tüm özellikleriyle aynı olan gönderiler var ise
                            // aynı olanların 1 tanesi hariç hepsini ArrayList'n çıkar.
                        }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "onFailure: " + e.message)
                }
        }
    }

    fun searchForTag(relevantField: String?, keywordWrited: String?) {
        Log.d(TAG, "aramaYapEtiketIcin: Çalıştı")
        clearList()
        val collectionReference = firebaseFirestore
            ?.collection("Gonderiler")
        if (collectionReference != null) {
            collectionReference
                .whereArrayContains((relevantField)!!, (keywordWrited)!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val querySnapshot = (task.result)
                        for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                            veriCagir(documentSnapshot.data)
                            recyclerAdapterStructure!!.notifyDataSetChanged()
                            Log.i(TAG, "onComplete: Sonu...")
                        }
                    }
                }
                .addOnFailureListener { }
        }
    }

    fun searchForCity(postCode: String) {
        Log.i(TAG, "aramaYapSehirIcin: Çalıştı")
        val collectionReference = firebaseFirestore
            ?.collection("Gonderiler")
        if (collectionReference != null) {
            collectionReference
                .orderBy("postaKodu")
                .startAt(postCode)
                .endAt(postCode + "\uf8ff")
                .get()
                .addOnCompleteListener { task ->
                    Log.i(TAG, "onComplete: Veriler filtrelenmiş şekilde çekildi")
                    if (task.isSuccessful) {
                        val querySnapshot = (task.result)
                        for (snapshot: DocumentSnapshot in querySnapshot) {
                            veriCagir(snapshot.data)
                            recyclerAdapterStructure!!.notifyDataSetChanged()
                            Log.d(TAG, "onComplete: Sonu...")

                            // Arraylistlerin içinde tüm özellikleriyle aynı olan gönderiler var ise
                            // aynı olanların 1 tanesi hariç hepsini ArrayList'n çıkar.
                        }
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "onFailure: " + e.message)
                }
        }
    }

    // Değişken isimlerini daha sonra ingilizce olarak düzenle
    fun showTag(position: Int): String {
        var taggg = ""
        val al_taglar = tagsFirebase!![position]
        val tag_uzunluk = al_taglar.length
        val alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
        val a_t = alinan_taglar.split(",").toTypedArray()
        for (tags: String in a_t) {
            Log.d(TAG, "onLongItemClick: " + tags.trim { it <= ' ' })
            taggg += "#" + tags.trim { it <= ' ' } + " "
        }
        return taggg
    }

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    override fun onLongItemClick(position: Int) {
        Log.i(TAG, "onLongItemClick: Uzun tık")
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            timesFirebase!![position]!!.toDate()
        )
        val showDetailPost =
            (commentsFirebase!!.get(position) + "\n\nPaylaşan: " + userEmailsFirebase!![position] +
                    "\nTarih: " + dateAndTime + "\nAdres: " + addressesFirebase!![position] +
                    "\n\nEtiketler: " + showTag(position))
        val alert = AlertDialog.Builder(activity)
        alert
            .setTitle(placeNamesFirebase!![position])
            .setMessage(showDetailPost)
            .setNegativeButton("TAMAM") { dialog, which ->
                //
            }
            .show()
    }

    fun saveOperations(position: Int) {
        if ((userEmailsFirebase!![position] == firebaseUser!!.email)) {
            Toast.makeText(activity, "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT).show()
        } else {
            val MGonderiler = Posts(
                postIDsFirebase!![position],
                userEmailsFirebase!![position],
                pictureLinksFirebase!![position],
                placeNamesFirebase!![position],
                locationFirebase!![position],
                addressesFirebase!![position],
                citiesFirebase!![position],
                commentsFirebase!![position],
                postCodesFirebase!![position], listOf(tagsFirebase!![position]),
                FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                ?.collection("Kaydedenler")
                ?.document((firebaseUser!!.email)!!)
                ?.collection("Kaydedilenler")
                ?.document(postIDsFirebase!![position])
            if (documentReference != null) {
                documentReference
                    .set(MGonderiler)
                    .addOnSuccessListener {
                        Toast.makeText(activity, "Kaydedildi", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
            }


            // Aşağıdaki işlemler gerekmiyor. Ama şimdilik burada bulunsun
            /*Map<String, Object> map = new HashMap();
            map.put("gonderiID", true);
            map.put("kaydeden", firebaseUser.getEmail());
            map.put("IDsi", gonderiIDleriFB.get(position));
            DocumentReference documentReference1 = firebaseFirestore
                    .collection("Kaydedilenler")
                    .document(gonderiIDleriFB.get(position))
                    .collection("Kaydedenler")
                    .document(firebaseUser.getEmail());
            documentReference1
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(@NonNull Void aVoid) {
                            // İşlem Başarılı
                        }
                    });*/Log.d(TAG, "onClick: Gönderi kaydedildi")
        }
    }

    fun goToLocationOperations(position: Int) {
        val postLocation = locationFirebase!![position].split(",").toTypedArray()
        var adverb = 0
        for (konumxy: String in postLocation) {
            adverb++
            if (adverb == 1) latitude = konumxy.toDouble()
            if (adverb == 2) longitude = konumxy.toDouble()
        }
        SET!!.putFloat("konum_git_enlem", latitude.toFloat())
        SET!!.putFloat("konum_git_boylam", longitude.toFloat())
        SET!!.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
        Log.i(TAG, "Enlem: $latitude   \tBoylam: $longitude")
    }

    override fun onOtherOperationsClick(position: Int) {
        onOpenDialogWindow(position)
    }

    override fun onOpenDialogWindow(position: Int) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup!!.findViewById(R.id.bottomSheetContainer)
            )

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_postSave).setOnClickListener(
            View.OnClickListener {
                saveOperations(position)
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
                    if ((userEmailsFirebase!![position] == firebaseUser!!.email)) {
                        Toast.makeText(activity, "Bunu zaten siz paylaştınız", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        val contactInfo = ContactInfo()
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.putExtra(Intent.EXTRA_EMAIL, contactInfo.admin_hesaplari)
                        intent.putExtra(Intent.EXTRA_SUBJECT, "")
                        intent.putExtra(Intent.EXTRA_TEXT, "")
                        intent.type = "plain/text"
                        startActivity(Intent.createChooser(intent, "Ne ile göndermek istersiniz?"))
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

    companion object {
        private val TAG = "F_Ara"
        private val REQUEST_CODE_LOCATION_PERMISSON = 203
    }
}