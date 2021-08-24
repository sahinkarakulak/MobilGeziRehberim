package com.mrcaracal.activity.editProfile

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*

class EditProfileViewModel : ViewModel() {

    var editProfileViewState: MutableLiveData<EditProfileViewState> =
        MutableLiveData<EditProfileViewState>()

    var firebaseUser: FirebaseUser? = null
    lateinit var storageReference: StorageReference
    lateinit var documentReference: DocumentReference
    lateinit var mImageUri: Uri

    private val STORAGE_NAME = "Resimler"
    private val FIREBASE_COLLECTION_NAME = "Kullanicilar"
    private val FIREBASE_DOC_VAL_USERNAME = "kullaniciAdi"
    private val FIREBASE_DOC_VAL_BIO = "bio"
    private val FIREBASE_DOC_VAL_USERPIC = "kullaniciResmi"

    fun initialize() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageReference = FirebaseStorage.getInstance().getReference(STORAGE_NAME)
    }

    fun updateUser(u_name: String, u_bio: String) {
        val documentReference2 = FirebaseFirestore.getInstance()
            .collection(FIREBASE_COLLECTION_NAME)
            .document(firebaseUser?.email!!)
        val currentDatas: MutableMap<String, Any> = HashMap()
        currentDatas[FIREBASE_DOC_VAL_USERNAME] = u_name
        currentDatas[FIREBASE_DOC_VAL_BIO] = u_bio
        documentReference2
            .update(currentDatas)
            .addOnSuccessListener {
                editProfileViewState.value = EditProfileViewState.OpenHomePageActivity
            }
    }

    fun getProfileData() {
        editProfileViewState.value = firebaseUser?.email?.let {
            EditProfileViewState.BindingTvUserEmailChangeText(it)
        }
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
                        editProfileViewState.value = EditProfileViewState.GetFirebaseDocValUserName(
                            documentSnapshot = documentSnapshot,
                            firebaseDocValUserName = FIREBASE_DOC_VAL_USERNAME
                        )
                        editProfileViewState.value = EditProfileViewState.GetFirebaseDocValBio(
                            documentSnapshot = documentSnapshot,
                            firebaseDocValBio = FIREBASE_DOC_VAL_BIO
                        )
                        editProfileViewState.value = EditProfileViewState.PicassoPross(
                            documentSnapshot = documentSnapshot,
                            firebaseDocValueUserPic = FIREBASE_DOC_VAL_USERPIC
                        )
                    }
                }
            }
    }

    fun getFileExtension(uri: Uri, contentResolver: ContentResolver): String? {
        editProfileViewState.value = EditProfileViewState.GetContentResolver
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    fun uploadImage(contentResolver: ContentResolver) {
        val storageReference2 = storageReference.child(firebaseUser!!.email!!).child(
            System.currentTimeMillis()
                .toString() + "." + getFileExtension(mImageUri, contentResolver)
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
                                        editProfileViewState.value =
                                            EditProfileViewState.PicassoPross(
                                                documentSnapshot = documentSnapshot,
                                                firebaseDocValueUserPic = FIREBASE_DOC_VAL_USERPIC
                                            )
                                    }
                                }
                            }
                    }
            } else {
                editProfileViewState.value = EditProfileViewState.ShowErrorOccuredMessage
            }
        }.addOnFailureListener { exception ->
            editProfileViewState.value =
                EditProfileViewState.ShowExceptionErrorMessage(exception = exception)
        }
    }
}

sealed class EditProfileViewState {
    object OpenHomePageActivity : EditProfileViewState()
    object ShowErrorOccuredMessage : EditProfileViewState()
    object GetContentResolver : EditProfileViewState()

    data class GetFirebaseDocValUserName(
        val documentSnapshot: DocumentSnapshot,
        val firebaseDocValUserName: String
    ) : EditProfileViewState()

    data class GetFirebaseDocValBio(
        val documentSnapshot: DocumentSnapshot,
        val firebaseDocValBio: String
    ) : EditProfileViewState()

    data class PicassoPross(
        val documentSnapshot: DocumentSnapshot,
        val firebaseDocValueUserPic: String
    ) : EditProfileViewState()

    data class ShowExceptionErrorMessage(val exception: Exception) : EditProfileViewState()
    data class BindingTvUserEmailChangeText(val firebaseUserName: String) : EditProfileViewState()
}