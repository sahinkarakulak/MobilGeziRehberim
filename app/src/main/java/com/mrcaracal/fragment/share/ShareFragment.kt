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
import android.os.Looper
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
import com.mrcaracal.mobilgezirehberim.Login
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.FragShareBinding
import com.mrcaracal.modul.Posts
import com.squareup.picasso.Picasso
import java.util.*

class ShareFragment : Fragment() {

    private var _binding : FragShareBinding? = null
    private val binding get() = _binding!!

    private lateinit var MGonderiler: Posts
    private lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var picturePath: Uri

    lateinit var GET: SharedPreferences
    lateinit var SET: SharedPreferences.Editor
    lateinit var addres: String
    lateinit var postCode: String
    lateinit var postID: String
    lateinit var tags: List<String>

    var latitude = 0f
    var longitude = 0f
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
        /*val viewGroup = inflater.inflate(R.layout.frag_share, container, false) as ViewGroup*/
        init()

        _binding = FragShareBinding.inflate(inflater, container, false)
        val view = binding.root


        /*img_sharePictureSelected = viewGroup.findViewById(R.id.img_sharePictureSelected)
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
        sv_share = viewGroup.findViewById(R.id.sv_share)*/

        // Galeriden resim çekmek için yapılacaklar
        binding.imgSharePictureSelected.setOnClickListener(View.OnClickListener { choosePictureFromGallery() })
        binding.btnAddTag.setOnClickListener(View.OnClickListener { createTag() })
        binding.selectLocation.setOnClickListener(View.OnClickListener {
            startActivity(Intent(activity, MyMapActivity::class.java))
        })
        binding.btnShareSend.setOnClickListener(View.OnClickListener { shareSend() })
        /*return viewGroup*/
        return view
    }

    fun createTag() {
        val tagler = binding.edtShareTag.text.toString().lowercase().split(" ").toTypedArray()
        var etiket_sayisi = 0
        var taggg = ""
        for (tags in tagler) {
            etiket_sayisi++
            this.tags = Arrays.asList(*tagler)
            taggg += "#$tags   "
            binding.tvPrintTags.text = taggg
            if (etiket_sayisi == 5) break
        }
    }

    fun shareSend() {
        binding.btnShareSend.isEnabled = true
        val placeNameControl = binding.edtSharePlaceName.text.toString()
        val commentControl = binding.edtShareComment.text.toString()
        val locationControl = binding.edtLocation.text.toString()
        val addresControl = binding.edtAddres.text.toString()
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
                                val placeName = binding.edtSharePlaceName.text.toString().lowercase()
                                val location = binding.edtLocation.text.toString()
                                val comment = binding.edtShareComment.text.toString()
                                val addres = binding.edtAddres.text.toString()
                                var cityyy: String? = binding.edtCity.text.toString()
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
                                                binding.btnShareSend.isEnabled = false
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        binding.btnShareSend.isEnabled = false
                                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                binding.btnShareSend.isEnabled = false
                            }
                        val myToast = Toast.makeText(activity, getString(R.string.sended), Toast.LENGTH_SHORT)
                        myToast.show()
                        val handler = Handler()
                        handler.postDelayed({ myToast.cancel() }, 400)

                    }
                    .addOnFailureListener { e ->
                        binding.btnShareSend.isEnabled = false
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                binding.btnShareSend.isEnabled = false
            }
        } else {
            activity?.let { toast(it, getString(R.string.fill_in_the_required_fields)) }
            binding.btnShareSend.isEnabled = false
        }
    }

    override fun onResume() {
        super.onResume()
        latitude = GET.getFloat("enlem", 0f)
        longitude = GET.getFloat("boylam", 0f)
        addres = GET.getString("adres", "Türkiye Üsküdar")!!
        postCode = GET.getString("postaKodu", "12000")!!
        binding.edtLocation.setText("$latitude,$longitude")
        binding.edtAddres.setText("" + addres)
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
                .into(binding.imgSharePictureSelected)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}