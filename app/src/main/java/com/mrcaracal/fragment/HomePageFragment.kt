package com.mrcaracal.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
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
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.ContactInfo
import com.mrcaracal.modul.Posts
import java.text.DateFormat
import java.util.*

private const val TAG = "HomePageFragment"

class HomePageFragment() : Fragment(), RecyclerViewClickInterface {

    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
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
    private lateinit var timesFirebase: ArrayList<Timestamp>
    private lateinit var recyclerView: RecyclerView
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure
    lateinit var viewGroup: ViewGroup
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
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
    ): View {
        viewGroup = inflater.inflate(R.layout.frag_home_page, container, false) as ViewGroup
        init()

        // Yeniden eskiye çekme
        rewind()

        // RecyclerView Tanımlama İşlemi
        recyclerView = viewGroup.findViewById(R.id.recyclerView)
        recyclerView.setLayoutManager(LinearLayoutManager(activity))
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
        recyclerView.setAdapter(recyclerAdapterStructure)
        return viewGroup
    }

    fun rewind() {
        val collectionReference = firebaseFirestore
            .collection("Gonderiler")

        // VT'ye kaydedilme zamanına göre verileri çek
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
                        palceName = palceName.substring(0, 1).uppercase() + palceName.substring(1)
                        val pictureLink = dataCluster["resimAdresi"].toString()
                        val location = dataCluster["konum"].toString()
                        val addres = dataCluster["adres"].toString()
                        val city = dataCluster["sehir"].toString()
                        val comment = dataCluster["yorum"].toString()
                        val postCode = dataCluster["postaKodu"].toString()
                        val time = dataCluster["zaman"] as Timestamp

                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(pictureLink)
                        placeNamesFirebase.add(palceName)
                        locationFirebase.add(location)
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataCluster["taglar"].toString())
                        timesFirebase.add(time)
                        recyclerAdapterStructure.notifyDataSetChanged()

                    }
                }
            }
    }

    // Daha sonra uygun ingilizce kelimelerle değişken isimlerini oluştur.
    fun tagGoster(position: Int): String {
        var taggg = ""
        val al_taglar = tagsFirebase[position]
        val tag_uzunluk = al_taglar.length
        val alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
        val a_t = alinan_taglar.split(",").toTypedArray()
        for (tags: String in a_t) {
            taggg += "#" + tags.trim { it <= ' ' } + " "
        }
        return taggg
    }

    // Her bir recyclerRow'a uzunca tıklandığında yapılacak işlemler
    override fun onLongItemClick(position: Int) {
        val dateAndTime = DateFormat.getDateTimeInstance().format(
            timesFirebase[position].toDate()
        )
        val showDetailPost =
            (commentsFirebase.get(position) + "\n\nPaylaşan: " + userEmailsFirebase[position] +
                    "\nTarih: " + dateAndTime + "\nAdres: " + addressesFirebase[position] +
                    "\n\nEtiketler: " + tagGoster(position))
        val alert = AlertDialog.Builder(activity)
        alert
            .setTitle(placeNamesFirebase[position])
            .setMessage(showDetailPost)
            .setNegativeButton("TAMAM") { _dialog, which ->
                //
            }
            .show()
    }

    fun saveOperations(position: Int) {
        if ((userEmailsFirebase[position] == firebaseUser!!.email)) {
            activity?.let { toast(it, "Bunu zaten siz paylaştınız") }
        } else {
            val MGonderiler = Posts(
                postIDsFirebase[position],
                userEmailsFirebase[position],
                pictureLinksFirebase[position],
                placeNamesFirebase[position],
                locationFirebase[position],
                addressesFirebase[position],
                citiesFirebase[position],
                commentsFirebase[position],
                postCodesFirebase[position], listOf(tagsFirebase[position]),
                FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                .collection("Kaydedenler")
                .document((firebaseUser?.email)!!)
                .collection("Kaydedilenler")
                .document(postIDsFirebase[position])
            documentReference
                .set(MGonderiler)
                .addOnSuccessListener {
                    activity?.let { it1 -> toast(it1, "Kaydedildi") }
                }
                .addOnFailureListener { e ->
                    activity?.let { e.message?.let { it1 -> toast(it, it1) } }
                }
        }
    }

    fun goToLocationOperations(position: Int) {
        val postLocation = locationFirebase[position].split(",").toTypedArray()
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

    override fun onOtherOperationsClick(position: Int) {
        //Toast.makeText(getActivity(), "DİĞER", Toast.LENGTH_SHORT).show();
        onOpenDialogWindow(position)
    }

    override fun onOpenDialogWindow(position: Int) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet,
                viewGroup.findViewById(R.id.bottomSheetContainer)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = placeNamesFirebase.get(position)

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

        // Detaylı Şikayet Bildir (Mail)
        // Bu kısımda cihazdaki MAİL uygulamasının açılıp kullanıcının burada geliştiricilere istediği verileri göndermesi sağlanacak
        bottomSheetView.findViewById<View>(R.id.bs_reportAComplaint)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    if ((userEmailsFirebase[position] == firebaseUser?.email)) {
                        toast(activity!!, "Bunu zaten siz paylaştınız")
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

}