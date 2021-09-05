package com.mrcaracal.activity.editProfile

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mrcaracal.utils.ConstantsFirebase
import java.util.*

class EditProfileViewModel : ViewModel() {

    var editProfileViewState: MutableLiveData<EditProfileViewState> =
        MutableLiveData<EditProfileViewState>()

    var firebaseUser: FirebaseUser? = null
    var storageReference: StorageReference
    lateinit var documentReference: DocumentReference
    lateinit var mImageUri: Uri

    init {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        storageReference =
            FirebaseStorage.getInstance().getReference(ConstantsFirebase.STORAGE_NAME)
    }

    fun updateUser(u_name: String, u_bio: String) {
        val documentReference2 = FirebaseFirestore.getInstance()
            .collection(ConstantsFirebase.FIREBASE_COLLECTION_NAME)
            .document(firebaseUser?.email!!)
        val currentDatas: MutableMap<String, Any> = HashMap()
        currentDatas[ConstantsFirebase.FIREBASE_DOC_VAL_USERNAME] = u_name
        currentDatas[ConstantsFirebase.FIREBASE_DOC_VAL_BIO] = u_bio
        documentReference2
            .update(currentDatas)
            .addOnSuccessListener {
                editProfileViewState.value = EditProfileViewState.OpenHomePageActivity
            }
            .addOnFailureListener { exception ->
                editProfileViewState.value =
                    EditProfileViewState.ShowExceptionErrorMessage(exception = exception)
            }
    }

    fun getProfileData() {
        editProfileViewState.value = firebaseUser?.email?.let {
            EditProfileViewState.BindingTvUserEmailChangeText(it)
        }
        documentReference = FirebaseFirestore
            .getInstance()
            .collection(ConstantsFirebase.FIREBASE_COLLECTION_NAME)
            .document(firebaseUser?.email!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot.exists()) {
                        editProfileViewState.value = EditProfileViewState.OnUserInfo(
                            userName = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERNAME),
                            userBio = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_BIO)
                        )
                        editProfileViewState.value = EditProfileViewState.OnUserImage(
                            userImageUrl = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERPIC).orEmpty()
                        )
                    }
                }
            }
            .addOnFailureListener { exception ->
                editProfileViewState.value =
                    EditProfileViewState.ShowExceptionErrorMessage(exception = exception)
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
                .toString() + "." + getFileExtension(uri = mImageUri, contentResolver = contentResolver)
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
                hasmap[ConstantsFirebase.FIREBASE_DOC_VAL_USERPIC] = "" + myUrl
                FirebaseFirestore.getInstance()
                    .collection(ConstantsFirebase.FIREBASE_COLLECTION_NAME)
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
                                            EditProfileViewState.OnUserImage( userImageUrl =
                                                documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERPIC).orEmpty()
                                            )
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                editProfileViewState.value =
                                    EditProfileViewState.ShowExceptionErrorMessage(exception = exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        editProfileViewState.value =
                            EditProfileViewState.ShowExceptionErrorMessage(exception = exception)
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

    data class OnUserImage(
        val userImageUrl: String
    ) : EditProfileViewState()

    data class ShowExceptionErrorMessage(val exception: Exception) : EditProfileViewState()
    data class BindingTvUserEmailChangeText(val firebaseUserName: String) : EditProfileViewState()
    data class OnUserInfo(val userName: String?, val userBio: String?): EditProfileViewState()
}