package com.mrcaracal.activity.editProfile

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
import com.mrcaracal.activity.HomePageActivity
import com.mrcaracal.extensions.toast
import com.mrcaracal.mobilgezirehberim.R
import com.mrcaracal.mobilgezirehberim.databinding.ActivityEditProfileBinding
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    var firebaseUser: FirebaseUser? = null
    lateinit var storageReference: StorageReference
    lateinit var documentReference: DocumentReference
    private lateinit var mImageUri: Uri

    private val STORAGE_NAME = "Resimler"
    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val FIREBASE_DOC_VAL_USERNAME = "kullaniciAdi"
    private val FIREBASE_DOC_VAL_BIO = "bio"
    private val FIREBASE_DOC_VAL_USERPIC = "kullaniciResmi"

    private fun initialize() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageReference = FirebaseStorage.getInstance().getReference(STORAGE_NAME)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initialize()
        title = getString(R.string.edit_profile)

        binding.tvUserEmail.text = firebaseUser?.email
        documentReference = FirebaseFirestore
            .getInstance()
            .collection(FIREBASE_COLLECTION_NAME)
            .document(firebaseUser?.email!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                        binding.edtGetUserName.setText(
                            documentSnapshot.getString(
                                FIREBASE_DOC_VAL_USERNAME
                            )
                        )
                        binding.edtGetBiography.setText(
                            documentSnapshot.getString(
                                FIREBASE_DOC_VAL_BIO
                            )
                        )
                        Picasso.get().load(documentSnapshot.getString(FIREBASE_DOC_VAL_USERPIC))
                            .into(binding.imgUserPicture)
                    }
                }
            }
        binding.tvUserChangePicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        binding.imgUserPicture.setOnClickListener {
            CropImage
                .activity()
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this@EditProfileActivity)
        }
        binding.btnUpdate.setOnClickListener {
            updateUser(
                binding.edtGetUserName.text.toString(),
                binding.edtGetBiography.text.toString()
            )
        }
    }

    private fun updateUser(u_name: String, u_bio: String) {
        val documentReference2 = FirebaseFirestore.getInstance()
            .collection(FIREBASE_COLLECTION_NAME)
            .document(firebaseUser?.email!!)
        val currentDatas: MutableMap<String, Any> = HashMap()
        currentDatas[FIREBASE_DOC_VAL_USERNAME] = u_name
        currentDatas[FIREBASE_DOC_VAL_BIO] = u_bio
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
                hasmap[FIREBASE_DOC_VAL_USERPIC] = "" + myUrl
                FirebaseFirestore.getInstance()
                    .collection(FIREBASE_COLLECTION_NAME)
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
                                            .load(
                                                documentSnapshot.getString(
                                                    FIREBASE_DOC_VAL_USERPIC
                                                )
                                            )
                                            .into(binding.imgUserPicture)

                                    }
                                }
                            }
                    }
            } else {
                toast(getString(R.string.error_occurred))
            }
        }.addOnFailureListener {
            toast(it.localizedMessage.orEmpty())
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            uploadImage()
            toast(getString(R.string.updated))
        } else {
            toast(getString(R.string.did_you_give_up))
        }
    }

}