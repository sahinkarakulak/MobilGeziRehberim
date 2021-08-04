package com.mrcaracal.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*

private const val TAG = "EditProfileActivity"

class EditProfileActivity : AppCompatActivity() {

    var firebaseUser: FirebaseUser? = null
    lateinit var storageReference: StorageReference
    lateinit var img_userPicture: ImageView
    lateinit var tv_userChangePicture: TextView
    lateinit var edt_getUserName: EditText
    lateinit var edt_getBiography: EditText
    lateinit var tv_userEmail: TextView
    lateinit var btn_update: Button
    lateinit var documentReference: DocumentReference
    private lateinit var mImageUri: Uri

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
        title = getString(R.string.edit_profile)

        tv_userEmail.text = firebaseUser?.email
        documentReference = FirebaseFirestore
            .getInstance()
            .collection("Kullanicilar")
            .document(firebaseUser?.email!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                        edt_getUserName.setText(documentSnapshot.getString("kullaniciAdi"))
                        edt_getBiography.setText(documentSnapshot.getString("bio"))
                        Picasso.get().load(documentSnapshot.getString("kullaniciResmi"))
                            .into(img_userPicture)
                    }
                }
            }
        tv_userChangePicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        img_userPicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        btn_update.setOnClickListener {
            updateUser(edt_getUserName.text.toString(), edt_getBiography.text.toString())
        }
    }

    private fun updateUser(u_name: String, u_bio: String) {
        val documentReference2 = FirebaseFirestore.getInstance()
            .collection("Kullanicilar")
            .document(firebaseUser?.email!!)
        val currentDatas: MutableMap<String, Any> = HashMap()
        currentDatas["kullaniciAdi"] = u_name
        currentDatas["bio"] = u_bio
        documentReference2
            .update(currentDatas)
            .addOnSuccessListener {
                startActivity(Intent(this@EditProfileActivity, HomePageActivity::class.java))
            }
    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    // Convert işlemi sonrası hatalar gerçekleşti. DÜZELTİLECEKTİR!
    private fun uploadImage() {
        val storageReference2 = storageReference.child(firebaseUser!!.email!!).child(
            System.currentTimeMillis()
                .toString() + "." + getFileExtension(mImageUri)
        )

        val uploadTask = storageReference2.putFile(mImageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference2.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val myUrl = downloadUri.toString()
                val hasmap: MutableMap<String, Any> = HashMap()
                hasmap["kullaniciResmi"] = "" + myUrl
                FirebaseFirestore.getInstance()
                    .collection("Kullanicilar")
                    .document(firebaseUser?.email!!)
                    .update(hasmap)
                    .addOnSuccessListener {
                        documentReference
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val documentSnapshot = task.result
                                    if (documentSnapshot.exists()) {
                                        Picasso.get()
                                            .load(documentSnapshot.getString("kullaniciResmi"))
                                            .into(img_userPicture)

                                    }
                                }
                            }
                    }
            } else {
                toast(R.string.error_occurred)
            }
        } .addOnFailureListener {
            toast(it.localizedMessage.orEmpty())
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            uploadImage()
            toast("Profil resmi güncellendi")
        } else {
            toast("Vaz mı geçtin?")
        }
    }

}