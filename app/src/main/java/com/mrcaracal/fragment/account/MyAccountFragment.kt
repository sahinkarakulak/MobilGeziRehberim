package com.mrcaracal.fragment.account

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.EditProfileActivity
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.util.*

class MyAccountFragment : Fragment(), RecyclerViewClickInterface {

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

    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val FIREBASE_DOC_VAL_USERNAME = "kullaniciAdi"
    private val FIREBASE_DOC_VAL_BIO = "bio"
    private val FIREBASE_DOC_VAL_USERPIC = "kullaniciResmi"

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0.0
    var longitude = 0.0
    lateinit var viewGroup: ViewGroup

    private lateinit var firebaseOperationForAccount: FirebaseOperationForAccount

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
        GET = activity!!.getSharedPreferences(R.string.map_key.toString(), Context.MODE_PRIVATE)
        SET = GET.edit()

        firebaseOperationForAccount = FirebaseOperationForAccount(
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewGroup = inflater.inflate(R.layout.frag_my_account, container, false) as ViewGroup
        init()

        // RecyclerView Tanımlama İşlemi
        recyclerViewAccount = viewGroup.findViewById(R.id.recyclerViewAccount)
        recyclerViewAccount.layoutManager = LinearLayoutManager(activity)
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

        firebaseUser?.let {
            firebaseOperationForAccount.pullTheShared(
                firebaseFirestore,
                it, recyclerAdapterStructure
            )
        }

        recyclerViewAccount.adapter = recyclerAdapterStructure
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
            .collection(FIREBASE_COLLECTION_NAME)
            .document((firebaseUser?.email)!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = (task.result)
                    if (documentSnapshot.exists()) {
                        tv_userName.text = documentSnapshot.getString(FIREBASE_DOC_VAL_USERNAME)
                        tv_userBio.text = documentSnapshot.getString(FIREBASE_DOC_VAL_BIO)
                        Picasso.get().load(documentSnapshot.getString(FIREBASE_DOC_VAL_USERPIC))
                            .into(img_profileProfilePicture)
                        if (documentSnapshot.getString(FIREBASE_DOC_VAL_USERPIC) == null) {
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
                    firebaseOperationForAccount.clearList()
                    firebaseOperationForAccount.pullTheShared(
                        firebaseFirestore,
                        firebaseUser!!, recyclerAdapterStructure
                    )
                    recyclerViewAccount.scrollToPosition(0)
                }
                R.id.recorded -> {
                    tabControl = "kaydedilenler"
                    firebaseOperationForAccount.clearList()
                    firebaseOperationForAccount.pullTheRecorded(
                        firebaseFirestore,
                        firebaseUser!!, recyclerAdapterStructure
                    )
                    recyclerViewAccount.scrollToPosition(0)
                }
            }
            true
        }
        return viewGroup
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
                    R.string.labels.toString() + "\n\n: " + firebaseOperationForAccount.showTag(position, tabControl))
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
                            firebaseOperationForAccount.removeFromShared(
                                firebaseFirestore,
                                positionValue
                            )
                            firebaseOperationForAccount.clearList()
                            firebaseUser?.let {
                                firebaseOperationForAccount.pullTheShared(
                                    firebaseFirestore,
                                    it, recyclerAdapterStructure
                                )
                            }
                            recyclerViewAccount.scrollToPosition(0)
                        }
                        "kaydedilenler" -> {
                            firebaseUser?.let {
                                firebaseOperationForAccount.removeFromSaved(
                                    firebaseFirestore,
                                    it, positionValue
                                )
                            }
                            firebaseOperationForAccount.clearList()
                            firebaseUser?.let {
                                firebaseOperationForAccount.pullTheRecorded(
                                    firebaseFirestore,
                                    it, recyclerAdapterStructure
                                )
                            }
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