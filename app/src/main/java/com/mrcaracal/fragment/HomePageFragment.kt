package com.mrcaracal.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.ContactInfo
import com.mrcaracal.modul.Posts
import java.text.DateFormat
import java.util.*

class HomePageFragment() : Fragment(), RecyclerViewClickInterface {

    //    ProgressDialog progressDialog;
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
    private lateinit var recyclerView: RecyclerView
    var recyclerAdapterStructure: RecyclerAdapterStructure? = null
    var viewGroup: ViewGroup? = null
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0

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
        viewGroup = inflater.inflate(R.layout.frag_home_page, container, false) as ViewGroup
        init()

        // Yeniden eskiye çekme
        rewind()

        // RecyclerView Tanımlama İşlemi
        recyclerView = viewGroup!!.findViewById(R.id.recyclerView)
        recyclerView.setLayoutManager(LinearLayoutManager(activity))
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
        recyclerView.setAdapter(recyclerAdapterStructure)
        Log.i(
            TAG,
            "onCreateView: RecyclerView tanımlama ve Adapter'a gerekli paremetrelerin gönderilmesi tamamlandı"
        )
        return viewGroup
    }

    fun rewind() {
        //progressDialog = new ProgressDialog(getActivity());
        //progressDialog.setMessage("Yükleniyor");
        //progressDialog.show();
        val collectionReference = firebaseFirestore
            ?.collection("Gonderiler")

        // VT'ye kaydedilme zamanına göre verileri çek
        if (collectionReference != null) {
            collectionReference
                .orderBy("zaman", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val querySnapshot = (task.result)
                        for (snapshot: DocumentSnapshot in querySnapshot) {

                            // Çekilen her veriyi Map dizinine at ve daha sonra çekip kullan
                            val dataCluster = snapshot.data
                            val postID = dataCluster!!["gonderiID"].toString()
                            val userEmail = dataCluster["kullaniciEposta"].toString()
                            var palceName = dataCluster["yerIsmi"].toString()
                            palceName = palceName.substring(0, 1).toUpperCase() + palceName.substring(1)
                            val pictureLink = dataCluster["resimAdresi"].toString()
                            val location = dataCluster["konum"].toString()
                            val addres = dataCluster["adres"].toString()
                            val city = dataCluster["sehir"].toString()
                            val comment = dataCluster["yorum"].toString()
                            val postCode = dataCluster["postaKodu"].toString()
                            val time = dataCluster["zaman"] as Timestamp
                            
                            postIDsFirebase!!.add(postID)
                            userEmailsFirebase!!.add(userEmail)
                            pictureLinksFirebase!!.add(pictureLink)
                            placeNamesFirebase!!.add(palceName)
                            locationFirebase!!.add(location)
                            addressesFirebase!!.add(addres)
                            citiesFirebase!!.add(city)
                            commentsFirebase!!.add(comment)
                            postCodesFirebase!!.add(postCode)
                            tagsFirebase!!.add(dataCluster["taglar"].toString())
                            timesFirebase!!.add(time)
                            recyclerAdapterStructure!!.notifyDataSetChanged()
                            Log.i(
                                TAG,
                                "onComplete: VT'den veriler çekildi, ArrayListlere aktarılıp RecyclerAdapterYapim'a aktarıldı"
                            )
                            //                                progressDialog.dismiss();
                        }
                    }
                }
        }
    }

    // Daha sonra uygun ingilizce kelimelerle değişken isimlerini oluştur.
    fun tagGoster(position: Int): String {
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
        Log.d(TAG, "onLongItemClick: Uzun tık")
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            timesFirebase!![position]!!.toDate()
        )
        val showDetailPost =
            (commentsFirebase!!.get(position) + "\n\nPaylaşan: " + userEmailsFirebase!![position] +
                    "\nTarih: " + dateAndTime + "\nAdres: " + addressesFirebase!![position] +
                    "\n\nEtiketler: " + tagGoster(position))
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
            Log.d(TAG, "onClick: Gönderi kaydedildi")
        }
    }

    fun goToLocationOperations(position: Int) {
        val postLocation = locationFirebase!![position].split(",").toTypedArray()
        var adverb = 0
        for (locationXY: String in postLocation) {
            adverb++
            if (adverb == 1) latitude = locationXY.toDouble()
            if (adverb == 2) longitude = locationXY.toDouble()
        }
        SET!!.putFloat("konum_git_enlem", latitude.toFloat())
        SET!!.putFloat("konum_git_boylam", longitude.toFloat())
        SET!!.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
        Log.i(TAG, "Enlem: $latitude   \tBoylam: $longitude")
    }

    override fun onOtherOperationsClick(position: Int) {
        //Toast.makeText(getActivity(), "DİĞER", Toast.LENGTH_SHORT).show();
        onOpenDialogWindow(position)
    }

    override fun onOpenDialogWindow(position: Int) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup!!.findViewById(R.id.bottomSheetContainer)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = placeNamesFirebase!!.get(position)

        // Gönderiyi Kaydet
        bottomSheetView.findViewById<View>(R.id.bs_gonderiyi_kaydet).setOnClickListener(
            View.OnClickListener {
                saveOperations(position)
                bottomSheetDialog.dismiss()
            })

        // Konuma Git
        bottomSheetView.findViewById<View>(R.id.bs_konuma_git)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    goToLocationOperations(position)
                    bottomSheetDialog.dismiss()
                }
            })

        // Detaylı Şikayet Bildir (Mail)
        // Bu kısımda cihazdaki MAİL uygulamasının açılıp kullanıcının burada geliştiricilere istediği verileri göndermesi sağlanacak
        bottomSheetView.findViewById<View>(R.id.bs_detayli_sikayet_bildir)
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
        bottomSheetView.findViewById<View>(R.id.bottom_sheet_iptal_btnsi)
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
        private val TAG = "F_Anasayfa"
    }
}