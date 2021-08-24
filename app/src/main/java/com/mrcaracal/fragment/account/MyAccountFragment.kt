package com.mrcaracal.fragment.account

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.activity.editProfile.EditProfileActivity
import com.mrcaracal.activity.GoToLocationOnMapActivity
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragMyAccountBinding
import com.squareup.picasso.Picasso
import java.text.DateFormat

class MyAccountFragment : Fragment(), RecyclerViewClickInterface {

    private var _binding: FragMyAccountBinding? = null
    private val binding get() = _binding!!

    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure

    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val FIREBASE_DOC_VAL_USERNAME = "kullaniciAdi"
    private val FIREBASE_DOC_VAL_BIO = "bio"
    private val FIREBASE_DOC_VAL_USERPIC = "kullaniciResmi"
    var POSITION_VALUE = 0
    var TAB_CONTROL = "paylasilanlar"
    var LATITUDE = 0.0
    var LONGITUDE = 0.0

    private val COLLECTION_NAME_SHARED = "Paylasilanlar"
    private val COLLECTION_NAME_THEY_SHARED = "Paylastiklari"
    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseUser = firebaseAuth.currentUser
        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        init()

        _binding = FragMyAccountBinding.inflate(inflater, container, false)
        val view = binding.root

        // RecyclerView Tanımlama İşlemi
        binding.recyclerViewAccount.layoutManager = LinearLayoutManager(activity)
        recyclerAdapterStructure = RecyclerAdapterStructure(
            recyclerViewClickInterface = this
        )

        firebaseUser?.let {
            pullTheShared()
        }

        binding.recyclerViewAccount.adapter = recyclerAdapterStructure
        binding.btnEditProfile.setOnClickListener(View.OnClickListener {
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
                        binding.tvUserName.text =
                            documentSnapshot.getString(FIREBASE_DOC_VAL_USERNAME)
                        binding.tvUserBio.text = documentSnapshot.getString(FIREBASE_DOC_VAL_BIO)
                        Picasso.get().load(documentSnapshot.getString(FIREBASE_DOC_VAL_USERPIC))
                            .into(binding.imgProfileProfilePicture)
                        if (documentSnapshot.getString(FIREBASE_DOC_VAL_USERPIC) == null) {
                            Picasso.get().load(R.drawable.defaultpp)
                                .into(binding.imgProfileProfilePicture)
                        }
                    }
                }
            }
        binding.bnAccountMenu.setOnNavigationItemSelectedListener { item -> // Hangi TAB'a tıklanmışsa onu tespit ediyoruz.
            when (item.itemId) {
                R.id.shared -> {
                    TAB_CONTROL = "paylasilanlar"
                    clearList()
                    pullTheShared()
                    binding.recyclerViewAccount.scrollToPosition(0)
                }
                R.id.recorded -> {
                    TAB_CONTROL = "kaydedilenler"
                    clearList()
                    pullTheRecorded()
                    binding.recyclerViewAccount.scrollToPosition(0)
                }
            }
            true
        }
        return view
    }

    fun clearList() {
        postModelsList.clear()
    }

    fun pullTheShared() {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_SHARED)
            .document((firebaseUser?.email)!!)
            .collection(COLLECTION_NAME_THEY_SHARED)
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {

                        documentSnapshot.data?.let {
                            val postModel = PostModelProvider.provide(it)
                            postModelsList.add(postModel)
                        }
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                //
            }
    }

    fun pullTheRecorded() {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser?.email)!!)
            .collection(COLLECTION_NAME_SAVED)
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        documentSnapshot.data?.let {
                            val postModel = PostModelProvider.provide(it)
                            postModelsList.add(postModel)

                        }
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                //
            }
    }

    fun removeFromShared(positionValue: Int) {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!

        //1. Adım
        firebaseFirestore
            .collection(COLLECTION_NAME_SHARED)
            .document(postModelsList[positionValue].userEmail)
            .collection(COLLECTION_NAME_THEY_SHARED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }

        //2. Adım
        firebaseFirestore
            .collection(COLLECTION_NAME_POST)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun removeFromSaved(
        positionValue: Int
    ) {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!
        firebaseFirestore
            .collection(COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser?.email)!!)
            .collection(COLLECTION_NAME_SAVED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun showTag(postModel: PostModel, tabControl: String): String {
        var taggg = ""
        val al_taglar = postModel.tag
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

    fun goToLocationFromShared(postModel: PostModel) {
        val postLocation = postModel.location.split(",").toTypedArray()
        var adverb = 0
        for (locationXY: String in postLocation) {
            adverb++
            if (adverb == 1) LATITUDE = locationXY.toDouble()
            if (adverb == 2) LONGITUDE = locationXY.toDouble()
        }
        SET.putFloat("konum_git_enlem", LATITUDE.toFloat())
        SET.putFloat("konum_git_boylam", LONGITUDE.toFloat())
        SET.commit()
        startActivity(Intent(activity, GoToLocationOnMapActivity::class.java))
    }

    fun goToLocationFromSaved(postModel: PostModel) {
        val postLocation = postModel.location.split(",").toTypedArray()
        var adverb = 0
        for (locationXY: String in postLocation) {
            adverb++
            if (adverb == 1) LATITUDE = locationXY.toDouble()
            if (adverb == 2) LONGITUDE = locationXY.toDouble()
        }
        SET.putFloat("konum_git_enlem", LATITUDE.toFloat())
        SET.putFloat("konum_git_boylam", LONGITUDE.toFloat())
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
                    "\n\n" + showTag(postModel, TAB_CONTROL))
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
                R.layout.layout_bottom_sheet_hesabim,
                view?.findViewById(R.id.bottomSheetContainer_hesabim)
            )
        val title = bottomSheetView.findViewById<TextView>(R.id.bs_baslik)
        title.text = postModel.placeName

        // KONUMA GİT
        bottomSheetView.findViewById<View>(R.id.bs_goToLocation).setOnClickListener(
            View.OnClickListener {
                when (TAB_CONTROL) {
                    "paylasilanlar" -> goToLocationFromShared(postModel)
                    "kaydedilenler" -> goToLocationFromSaved(postModel)
                }
                bottomSheetDialog.dismiss()
            })

        // KALDIR
        bottomSheetView.findViewById<View>(R.id.bs_remove)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    when (TAB_CONTROL) {
                        "paylasilanlar" -> {
                            removeFromShared(
                                POSITION_VALUE
                            )
                            clearList()
                            firebaseUser?.let {
                                pullTheShared()
                            }
                            binding.recyclerViewAccount.scrollToPosition(0)
                        }
                        "kaydedilenler" -> {
                            firebaseUser?.let {
                                removeFromSaved(POSITION_VALUE
                                )
                            }
                            clearList()
                            firebaseUser?.let {
                                pullTheRecorded()
                            }
                            binding.recyclerViewAccount.scrollToPosition(0)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}