package com.mrcaracal.fragment.share

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
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.modul.Posts
import com.squareup.picasso.Picasso
import java.util.*

class ShareFragment : Fragment() {
    lateinit var MGonderiler: Posts
    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var picturePath: Uri
    private lateinit var img_sharePictureSelected: ImageView
    lateinit var edt_sharePlaceName: EditText
    lateinit var edt_shareComment: EditText
    lateinit var edt_shareTag: EditText
    lateinit var edt_location: EditText
    lateinit var edt_addres: EditText
    lateinit var edt_city: EditText
    private lateinit var btn_shareSend: Button
    private lateinit var selectLocation: Button
    private lateinit var btn_addTag: Button
    lateinit var tv_printTags: TextView
    lateinit var sv_share: ScrollView
    lateinit var GET: SharedPreferences
    lateinit var SET: SharedPreferences.Editor
    var latitude = 0f
    var longitude = 0f
    lateinit var addres: String
    lateinit var postCode: String
    lateinit var postID: String
    lateinit var tags: List<String>
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val STORAGE_NAME = "Resimler"
    private val COLLECTION_NAME_SHARED = "Paylasilanlar"
    private val COLLECTION_NAME_THEY_SHARED = "Paylastiklari"
    private val COLLECTION_NAME_POST = "Gonderiler"

    private fun init() {
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()
        GET = activity!!.getSharedPreferences(getString(R.string.map_key), Context.MODE_PRIVATE)
        SET = GET.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        btn_addTag.setOnClickListener(View.OnClickListener { createTag() })
        selectLocation.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity, MyMapActivity::class.java))
        })
        btn_shareSend.setOnClickListener(View.OnClickListener { shareSend() })
        return viewGroup
    }

    fun createTag() {
        val tagler = edt_shareTag.text.toString().lowercase().split(" ").toTypedArray()
        var etiket_sayisi = 0
        var taggg = ""
        for (tags in tagler) {
            etiket_sayisi++
            this.tags = Arrays.asList(*tagler)
            taggg += "#$tags   "
            tv_printTags.text = taggg
            if (etiket_sayisi == 5) break
        }
    }

    fun shareSend() {
        btn_shareSend.isEnabled = true
        val placeNameControl = edt_sharePlaceName.text.toString()
        val commentControl = edt_shareComment.text.toString()
        val locationControl = edt_location.text.toString()
        val addresControl = edt_addres.text.toString()
        if (placeNameControl != "" && locationControl != "" && commentControl != "" && addresControl != "") {
            val uuid = UUID.randomUUID()
            val placeName = firebaseUser!!.email + "--" + placeNameControl + "--" + uuid
            try {
                storageReference
                    .child(STORAGE_NAME)
                    .child(placeName)
                    .putFile(picturePath)
                    .addOnSuccessListener {
                        val storageReference1 =
                            FirebaseStorage.getInstance().getReference(STORAGE_NAME + "/$placeName")
                        storageReference1
                            .downloadUrl
                            .addOnSuccessListener { uri ->
                                val firebaseUser = firebaseAuth.currentUser
                                val userEmail = firebaseUser!!.email
                                val pictureLink = uri.toString()
                                val placeName = edt_sharePlaceName.text.toString().lowercase()
                                val location = edt_location.text.toString()
                                val comment = edt_shareComment.text.toString()
                                val addres = edt_addres.text.toString()
                                var cityyy: String? = edt_city.text.toString()
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
                                    .collection(COLLECTION_NAME_SHARED)
                                    .document(firebaseUser.email!!)
                                    .collection(COLLECTION_NAME_THEY_SHARED)
                                    .document(postID)
                                documentReference1
                                    .set(MGonderiler)
                                    .addOnSuccessListener {
                                        val documentReference2 = firebaseFirestore
                                            .collection(COLLECTION_NAME_POST)
                                            .document(postID)
                                        documentReference2
                                            .set(MGonderiler)
                                            .addOnSuccessListener {
                                                val intent =
                                                    Intent(activity, HomePageActivity::class.java)
                                                // Tüm aktiviteleri kapat
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                                startActivity(intent)
                                            }
                                            .addOnFailureListener { e ->
                                                btn_shareSend.isEnabled = false
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        btn_shareSend.isEnabled = false
                                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                btn_shareSend.isEnabled = false
                            }
                        val myToast = Toast.makeText(activity, getString(R.string.sended), Toast.LENGTH_SHORT)
                        myToast.show()
                        val handler = Handler()
                        handler.postDelayed({ myToast.cancel() }, 400)
                    }
                    .addOnFailureListener { e ->
                        btn_shareSend.isEnabled = false
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                btn_shareSend.isEnabled = false
            }
        } else {
            activity?.let { toast(it, getString(R.string.fill_in_the_required_fields)) }
            btn_shareSend.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        latitude = GET.getFloat("enlem", 0f)
        longitude = GET.getFloat("boylam", 0f)
        addres = GET.getString("adres", "Türkiye Üsküdar")!!
        postCode = GET.getString("postaKodu", "12000")!!
        edt_location.setText("$latitude,$longitude")
        edt_addres.setText("" + addres)
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
        } else {
            val intentGaleri =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intentGaleri, 2)
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
        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            picturePath = data.data!!
            Picasso.get()
                .load(picturePath)
                .centerCrop()
                .fit()
                .into(img_sharePictureSelected)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}