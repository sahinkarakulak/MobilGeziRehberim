package com.mrcaracal.fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.activity.MyMapActivity
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.Posts
import com.squareup.picasso.Picasso
import java.util.*

private const val TAG = "ShareFragment"

class ShareFragment : Fragment() {
    var MGonderiler: Posts? = null
    var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var firebaseFirestore: FirebaseFirestore? = null
    var picturePath: Uri? = null
    private lateinit var img_sharePictureSelected: ImageView
    var edt_sharePlaceName: EditText? = null
    var edt_shareComment: EditText? = null
    var edt_shareTag: EditText? = null
    var edt_location: EditText? = null
    var edt_addres: EditText? = null
    var edt_city: EditText? = null
    private lateinit var btn_shareSend: Button
    private lateinit var selectLocation: Button
    private lateinit var btn_addTag: Button
    var tv_printTags: TextView? = null
    var sv_share: ScrollView? = null
    private lateinit var GET: SharedPreferences
    private lateinit var SET: SharedPreferences.Editor
    var latitude = 0f
    var longitude = 0f
    var addres: String? = null
    var postCode: String? = null
    var postID: String? = null
    var tags: List<String>? = null
    private var firebaseStorage: FirebaseStorage? = null
    private var storageReference: StorageReference? = null
    private fun init() {
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage!!.reference
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()
        GET = activity!!.getSharedPreferences("harita", Context.MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewGroup = inflater.inflate(R.layout.frag_share, container, false) as ViewGroup
        init()
        img_sharePictureSelected = viewGroup.findViewById(R.id.img_sharePictureSelected)
        edt_sharePlaceName = viewGroup.findViewById(R.id.edt_sharePlaceName)
        edt_location = viewGroup.findViewById(R.id.edt_location)
        edt_addres = viewGroup.findViewById(R.id.edt_addres)
        edt_city = viewGroup.findViewById(R.id.edt_city)
        edt_shareComment = viewGroup.findViewById(R.id.edt_shareComment)
        edt_shareTag = viewGroup.findViewById(R.id.edt_shareTag)
        selectLocation = viewGroup.findViewById(R.id.selectLocation)
        btn_shareSend = viewGroup.findViewById(R.id.btn_shareSend)
        btn_addTag = viewGroup.findViewById(R.id.btn_addTag)
        tv_printTags = viewGroup.findViewById(R.id.tv_printTags)
        sv_share = viewGroup.findViewById(R.id.sv_share)

        // Galeriden resim çekmek için yapılacaklar
        img_sharePictureSelected.setOnClickListener(View.OnClickListener { choosePictureFromGallery() })
        btn_addTag.setOnClickListener(View.OnClickListener { tagOlusturma() })
        selectLocation.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity, MyMapActivity::class.java))
            Log.i(TAG, "onClick: Kullanıcı Harita'a yönlendirildi")
        })
        btn_shareSend.setOnClickListener(View.OnClickListener { shareSend() })
        return viewGroup
    }

    // Daha sonradan değişken isimlerini ingilizce olacak şekilde düzenle
    fun tagOlusturma() {
        val tagler = edt_shareTag!!.text.toString().toLowerCase().split(" ").toTypedArray()
        var etiket_sayisi = 0
        var taggg = ""
        for (tags in tagler) {
            etiket_sayisi++
            Log.d(TAG, "TAGLER: " + tags.trim { it <= ' ' })
            this.tags = Arrays.asList(*tagler)
            taggg += "#$tags   "
            tv_printTags!!.text = taggg
            if (etiket_sayisi == 5) break
        }
    }

    fun shareSend() {
        Log.d(TAG, "ÇALIŞTI")
        btn_shareSend!!.isEnabled = true
        val placeNameControl = edt_sharePlaceName!!.text.toString()
        val commentControl = edt_shareComment!!.text.toString()
        val locationControl = edt_location!!.text.toString()
        val addresControl = edt_addres!!.text.toString()
        Log.i(TAG, "paylasGonder: Kullanıcının girdiği veriler alındı")
        if (placeNameControl != "" && locationControl != "" && commentControl != "" && addresControl != "") {
            val uuid = UUID.randomUUID()
            val placeName = firebaseUser!!.email + "--" + placeNameControl + "--" + uuid
            try {
                storageReference
                    ?.child("Resimler")
                    ?.child(placeName)
                    ?.putFile(picturePath!!)
                    ?.addOnSuccessListener {
                        val storageReference1 =
                            FirebaseStorage.getInstance().getReference("Resimler/$placeName")
                        storageReference1
                            .downloadUrl
                            .addOnSuccessListener { uri ->
                                val firebaseUser = firebaseAuth!!.currentUser
                                val userEmail = firebaseUser!!.email
                                val pictureLink = uri.toString()
                                val placeName = edt_sharePlaceName!!.text.toString().toLowerCase()
                                val location = edt_location!!.text.toString()
                                val comment = edt_shareComment!!.text.toString()
                                val addres = edt_addres!!.text.toString()
                                var cityyy: String? = edt_city!!.text.toString()
                                if (tags == null) {
                                    tags = Arrays.asList(
                                        "mgr",
                                        "gezi",
                                        "rehber",
                                        "seyahat",
                                        "etiketsiz"
                                    )
                                }
                                if (cityyy == null) {
                                    cityyy = null
                                }
                                val uuid1 = UUID.randomUUID()
                                postID = "" + uuid1
                                MGonderiler = Posts(
                                    postID, userEmail, pictureLink, placeName, location, addres,
                                    cityyy, comment, postCode, tags, FieldValue.serverTimestamp()
                                )
                                val documentReference1 = firebaseFirestore
                                    ?.collection("Paylasilanlar")
                                    ?.document(firebaseUser.email!!)
                                    ?.collection("Paylastiklari")
                                    ?.document(postID!!)
                                if (documentReference1 != null) {
                                    documentReference1
                                        .set(MGonderiler!!)
                                        .addOnSuccessListener {
                                            val documentReference2 = firebaseFirestore
                                                ?.collection("Gonderiler")
                                                ?.document(postID!!)
                                            if (documentReference2 != null) {
                                                documentReference2
                                                    .set(MGonderiler!!)
                                                    .addOnSuccessListener {
                                                        val intent =
                                                            Intent(activity, HomePageActivity::class.java)
                                                        // Tüm aktiviteleri kapat
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                        startActivity(intent)
                                                    }
                                                    .addOnFailureListener { e ->
                                                        btn_shareSend!!.isEnabled = false
                                                        Log.i(TAG, "onFailure: " + e.message)
                                                    }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            btn_shareSend!!.isEnabled = false
                                            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT)
                                                .show()
                                            Log.i(TAG, "onFailure: " + e.message)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.i(TAG, "onFailure: " + e.message)
                                btn_shareSend!!.isEnabled = false
                            }
                        val myToast = Toast.makeText(activity, "Gönderildi", Toast.LENGTH_SHORT)
                        myToast.show()
                        val handler = Handler()
                        handler.postDelayed({ myToast.cancel() }, 400)
                    }
                    ?.addOnFailureListener { e ->
                        btn_shareSend!!.isEnabled = false
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                        Log.i(TAG, "onFailure: " + e.message)
                    }
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                Log.i(TAG, "paylasGonder: " + e.message)
                btn_shareSend!!.isEnabled = false
            }
        } else {
            Toast.makeText(activity, "Gerekli alanları doldurunuz", Toast.LENGTH_SHORT).show()
            btn_shareSend!!.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: Çalıştı")
        // BU ACTİVİY'E TEKRAR GELİNDİĞİNDE HARİTA SINIFINDAN GEREKLİ KOORDİNAT VE ADRES BİLGİLERİNİ BURADA ALSIN VE GEREKLİ YERLERDE YAYINLASIN
        latitude = GET!!.getFloat("enlem", 0f)
        longitude = GET!!.getFloat("boylam", 0f)
        addres = GET!!.getString("adres", "Türkiye Üsküdar")
        postCode = GET!!.getString("postaKodu", "12000")
        edt_location!!.setText("$latitude,$longitude")
        edt_addres!!.setText("" + addres)
    }

    private fun choosePictureFromGallery() {
        if (ContextCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
            Log.i(TAG, "onClick: Daha önceden izin verilmediğinden izin istendi")
        } else {
            val intentGaleri =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentGaleri, 2)
            Log.i(TAG, "onClick: Daha önceden izin verildiğinden kullanıcı Galeriye yönlendirildi")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, 2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult: Çalıştı")
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            picturePath = data.data
            Picasso.get()
                .load(picturePath)
                .centerCrop()
                .fit()
                .into(img_sharePictureSelected)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private const val TAG = "F_Paylas"
    }
}