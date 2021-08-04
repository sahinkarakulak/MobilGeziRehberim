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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.EditProfileActivity
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.util.*

private const val TAG = "MyAccountFragment"

class MyAccountFragment() : Fragment(), RecyclerViewClickInterface {

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
    private lateinit var recyclerViewAccount: RecyclerView
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure
    private lateinit var tv_userName: TextView
    private lateinit var tv_userBio: TextView
    private lateinit var btn_editProfile: Button
    lateinit var img_profileProfilePicture: CircleImageView
    var positionValue = 0
    var tabControl = "paylasilanlar"

    // kaydedilenler
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup
    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseUser = firebaseAuth.currentUser
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
        viewGroup = inflater.inflate(R.layout.frag_my_account, container, false) as ViewGroup
        init()
        pullTheShared()

        // RecyclerView Tanımlama İşlemi
        recyclerViewAccount = viewGroup.findViewById(R.id.recyclerViewAccount)
        recyclerViewAccount.setLayoutManager(LinearLayoutManager(activity))
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
        recyclerViewAccount.setAdapter(recyclerAdapterStructure)
        img_profileProfilePicture = viewGroup.findViewById(R.id.img_profileProfilePicture)
        tv_userName = viewGroup.findViewById(R.id.tv_userName)
        tv_userBio = viewGroup.findViewById(R.id.tv_userBio)
        btn_editProfile = viewGroup.findViewById(R.id.btn_editProfile)
        btn_editProfile.setOnClickListener(View.OnClickListener {
            val editProfile = Intent(activity, EditProfileActivity::class.java)
            startActivity(editProfile)
        })
        val documentReference = FirebaseFirestore
            .getInstance()
            .collection("Kullanicilar")
            .document((firebaseUser?.email)!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = (task.result)
                    if (documentSnapshot.exists()) {
                        tv_userName.setText(documentSnapshot.getString("kullaniciAdi"))
                        tv_userBio.setText(documentSnapshot.getString("bio"))
                        Picasso.get().load(documentSnapshot.getString("kullaniciResmi"))
                            .into(img_profileProfilePicture)
                        if (documentSnapshot.getString("kullaniciResmi") == null) {
                            Picasso.get().load(R.drawable.defaultpp).into(img_profileProfilePicture)
                        }
                    }
                }
            }
        val bottomNavigationView: BottomNavigationView =
            viewGroup.findViewById(R.id.bn_accountMenu)
        bottomNavigationView.setOnNavigationItemSelectedListener { item -> // Hangi TAB'a tıklanmışsa onu tespit ediyoruz.
            when (item.itemId) {
                R.id.shared -> {
                    tabControl = "paylasilanlar"
                    clearList()
                    pullTheShared()
                    recyclerViewAccount.scrollToPosition(0)
                }
                R.id.recorded -> {
                    tabControl = "kaydedilenler"
                    clearList()
                    pullTheRecorded()
                    recyclerViewAccount.scrollToPosition(0)
                }
            }
            true
        }
        return viewGroup
    }

    fun clearList() {
        postIDsFirebase.clear()
        userEmailsFirebase.clear()
        pictureLinksFirebase.clear()
        placeNamesFirebase.clear()
        locationFirebase.clear()
        addressesFirebase.clear()
        citiesFirebase.clear()
        commentsFirebase.clear()
        postCodesFirebase.clear()
        tagsFirebase.clear()
        timesFirebase.clear()
    }

    fun pullTheShared() {
        val collectionReference = firebaseFirestore
            .collection("Paylasilanlar")
            .document((firebaseUser?.email)!!)
            .collection("Paylastiklari")
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        val dataClusterAccount = documentSnapshot.data
                        val postID = dataClusterAccount!!["gonderiID"].toString()
                        val userEmail = dataClusterAccount["kullaniciEposta"].toString()
                        var palceName = dataClusterAccount["yerIsmi"].toString()
                        palceName = palceName.substring(0, 1).toUpperCase() + palceName.substring(1)
                        val location = dataClusterAccount["konum"].toString()
                        val pictureLink = dataClusterAccount["resimAdresi"].toString()
                        val comment = dataClusterAccount["yorum"].toString()
                        val addres = dataClusterAccount["adres"].toString()
                        val city = dataClusterAccount["sehir"].toString()
                        val postCode = dataClusterAccount["postaKodu"].toString()
                        val time = dataClusterAccount["zaman"] as Timestamp
                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(pictureLink)
                        placeNamesFirebase.add(palceName)
                        locationFirebase.add(location)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataClusterAccount["taglar"].toString())
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        timesFirebase.add(time)
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun pullTheRecorded() {
        val collectionReference = firebaseFirestore
            .collection("Kaydedenler")
            .document((firebaseUser?.email)!!)
            .collection("Kaydedilenler")
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        val dataClusterAccount = documentSnapshot.data
                        val postID = dataClusterAccount!!["gonderiID"].toString()
                        val userEmail = dataClusterAccount["kullaniciEposta"].toString()
                        var placeName = dataClusterAccount["yerIsmi"].toString()
                        placeName = placeName.substring(0, 1).toUpperCase() + placeName.substring(1)
                        val location = dataClusterAccount["konum"].toString()
                        val picatureLink = dataClusterAccount["resimAdresi"].toString()
                        val comment = dataClusterAccount["yorum"].toString()
                        val postCode = dataClusterAccount["postaKodu"].toString()
                        val addres = dataClusterAccount["adres"].toString()
                        val city = dataClusterAccount["sehir"].toString()
                        val time = dataClusterAccount["zaman"] as Timestamp
                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(picatureLink)
                        placeNamesFirebase.add(placeName)
                        locationFirebase.add(location)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataClusterAccount["taglar"].toString())
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        timesFirebase.add(time)
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun removeFromShared() {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!

        //1. Adım
        firebaseFirestore
            .collection("Paylasilanlar")
            .document(userEmailsFirebase[positionValue])
            .collection("Paylastiklari")
            .document(postIDsFirebase[positionValue])
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }

        //2. Adım
        firebaseFirestore
            .collection("Gonderiler")
            .document(postIDsFirebase[positionValue])
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun removeFromSaved() {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!
        firebaseFirestore
            .collection("Kaydedenler")
            .document((firebaseUser?.email)!!)
            .collection("Kaydedilenler")
            .document(postIDsFirebase[positionValue])
            .delete()
            .addOnSuccessListener {
                Toast.makeText(activity, "Kaldırıldı", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, "İşlem Başarısız", Toast.LENGTH_SHORT).show()
            }
    }

    fun goToLocationFromShared() {
        val postLocation = locationFirebase[positionValue].split(",").toTypedArray()
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

    fun goToLocationFromSaved() {
        val postLocation = locationFirebase[positionValue].split(",").toTypedArray()
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

    fun konumaGitIslemleri(position: Int) {
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

    // Bu kısımda kullanınlan türkçe değişken isimlerini daha sonrasından düzenle inglizce yap
    fun showTag(position: Int): String {
        var taggg = ""
        val al_taglar = tagsFirebase[position]
        val tag_uzunluk = al_taglar.length
        val alinan_taglar: String
        val a_t: Array<String>
        when (tabControl) {
            "paylasilanlar" -> {
                alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
                a_t = alinan_taglar.split(",").toTypedArray()
                for (tags: String in a_t) {
                    taggg += "#" + tags.trim { it <= ' ' } + " "
                }
            }
            "kaydedilenler" -> {
                alinan_taglar = al_taglar.substring(2, tag_uzunluk - 2)
                a_t = alinan_taglar.split(",").toTypedArray()
                for (tags: String in a_t) {
                    taggg += "#" + tags.trim { it <= ' ' } + " "
                }
            }
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
                    "\n\nEtiketler: " + showTag(position))
        val alert = AlertDialog.Builder(activity)
        alert
            .setTitle(placeNamesFirebase[position])
            .setMessage(showDetailPost)
            .setNegativeButton("TAMAM") { dialog, which ->
                //
            }
            .show()
    }

    override fun onOtherOperationsClick(position: Int) {
        positionValue = position
        onOpenDialogWindow(position)
    }

    override fun onOpenDialogWindow(position: Int) {
        val bottomSheetDialog = BottomSheetDialog((activity)!!, R.style.BottomSheetDialogTheme)
        val bottomSheetView = LayoutInflater.from(activity)
            .inflate(
                R.layout.layout_bottom_sheet_hesabim,
                viewGroup.findViewById(R.id.bottomSheetContainer_hesabim)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = placeNamesFirebase.get(position)

        // KONUMA GİT
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation).setOnClickListener(
            View.OnClickListener {
                when (tabControl) {
                    "paylasilanlar" -> goToLocationFromShared()
                    "kaydedilenler" -> goToLocationFromSaved()
                }
                bottomSheetDialog.dismiss()
            })

        // KALDIR
        bottomSheetView.findViewById<View>(R.id.bs_remove)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    when (tabControl) {
                        "paylasilanlar" -> {
                            removeFromShared()
                            clearList()
                            pullTheShared()
                            recyclerViewAccount.scrollToPosition(0)
                        }
                        "kaydedilenler" -> {
                            removeFromSaved()
                            clearList()
                            pullTheRecorded()
                            recyclerViewAccount.scrollToPosition(0)
                        }
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