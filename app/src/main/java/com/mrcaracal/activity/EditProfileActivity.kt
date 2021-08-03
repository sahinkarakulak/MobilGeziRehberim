package com.mrcaracal.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*

private const val TAG = "EditProfileActivity"

class EditProfileActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    var storageReference: StorageReference? = null
    var img_userPicture: ImageView? = null
    var tv_userChangePicture: TextView? = null
    var edt_getUserName: EditText? = null
    var edt_getBiography: EditText? = null
    var tv_userEmail: TextView? = null
    var btn_update: Button? = null
    var documentReference: DocumentReference? = null
    private var mImageUri: Uri? = null
    private var uploadTask: StorageTask<*>? = null

    private fun initialize() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageReference = FirebaseStorage.getInstance().getReference("Resimler")
        img_userPicture = findViewById(R.id.img_userPicture)
        tv_userChangePicture = findViewById(R.id.tv_userChangePicture)
        edt_getUserName = findViewById(R.id.edt_getUserName)
        edt_getBiography = findViewById(R.id.edt_getBiography)
        tv_userEmail = findViewById(R.id.tv_userEmail)
        btn_update = findViewById(R.id.btn_update)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        initialize()
        title = "Profili Düzenle"
        tv_userEmail!!.text = firebaseUser!!.email
        documentReference = FirebaseFirestore
            .getInstance()
            .collection("Kullanicilar")
            .document(firebaseUser!!.email!!)
        documentReference!!
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                        edt_getUserName!!.setText(documentSnapshot.getString("kullaniciAdi"))
                        edt_getBiography!!.setText(documentSnapshot.getString("bio"))
                        Picasso.get().load(documentSnapshot.getString("kullaniciResmi"))
                            .into(img_userPicture)
                        Log.i(TAG, "onComplete: VT'den kullanıcı bilgileri alındı ve gösterildi")
                    }
                }
            }
        tv_userChangePicture!!.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        img_userPicture!!.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        btn_update!!.setOnClickListener {
            updateUser(edt_getUserName!!.text.toString(), edt_getBiography!!.text.toString())
            Log.i(TAG, "onClick: EditText'en alınan veriler parametre olarak gönderildi")
        }
    }

    private fun updateUser(u_name: String, u_bio: String) {
        val documentReference2 = FirebaseFirestore.getInstance()
            .collection("Kullanicilar")
            .document(firebaseUser!!.email!!)
        val currentDatas: MutableMap<String, Any> = HashMap()
        currentDatas["kullaniciAdi"] = u_name
        currentDatas["bio"] = u_bio
        documentReference2
            .update(currentDatas)
            .addOnSuccessListener {
                Log.i(TAG, "onSuccess: Sadece istenen veriler güncellendi")
                startActivity(Intent(this@EditProfileActivity, HomePageActivity::class.java))
                Log.i(TAG, "onSuccess: Kullanıcı A_Anasayfaya yönlendirildi")
            }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    // Convert işlemi sonrası hatalar gerçekleşti. DÜZELTİLECEKTİR!
    /*private fun uploadImage() {
        if (mImageUri != null) {
            Log.d(TAG, "uploadImage: Koşul sağlandı")
            val storageReference2 = storageReference!!.child(firebaseUser!!.email!!).child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(mImageUri!!)
            )
            uploadTask = storageReference2.putFile(mImageUri!!)
            Log.d(TAG, "uploadImage: Resim yolu alındı")
            uploadTask.continueWithTask(object : Continuation<Any?, Any?> {
                @Throws(Exception::class)
                override fun then(task: Task<*>): Any? {
                    if (!task.isSuccessful) {
                        throw task.exception
                    }
                    return storageReference2.downloadUrl
                }
            }).addOnCompleteListener(OnCompleteListener<Uri?> { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    val myUrl = downloadUri.toString()
                    val hasmap: MutableMap<String, Any> = HashMap()
                    hasmap["kullaniciResmi"] = "" + myUrl
                    FirebaseFirestore.getInstance()
                        .collection("Kullanicilar")
                        .document(firebaseUser!!.email!!)
                        .update(hasmap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onSuccess: VT'de resim yolu değiştirildi")
                            documentReference
                                ?.get()
                                ?.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val documentSnapshot = task.result
                                        if (documentSnapshot.exists()) {
                                            Picasso.get()
                                                .load(documentSnapshot.getString("kullaniciResmi"))
                                                .into(img_userPicture)
                                            Log.d(
                                                TAG,
                                                "onComplete: Resim yolu çekilip Picasso ile yayınlandı"
                                            )
                                        }
                                    }
                                }
                        }
                } else {
                    Toast.makeText(
                        this@EditProfileActivity,
                        "Bir hata gerçekleşti",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }).addOnFailureListener(OnFailureListener { e ->
                Toast.makeText(this@EditProfileActivity, e.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "onFailure: " + e.message)
            })
        } else {
            Toast.makeText(this, "Resim seçilmedi", Toast.LENGTH_SHORT).show()
        }
    }*/

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            uploadImage()
            Toast.makeText(this, "Profil resmi güncellendi", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "onActivityResult: Profil resmi güncellendi")
        } else {
            Toast.makeText(this, "Vaz mı geçtin?", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "onActivityResult: İşlemden vazgeçildi")
        }
    }*/

}