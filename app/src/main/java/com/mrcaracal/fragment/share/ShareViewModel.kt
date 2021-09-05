package com.mrcaracal.fragment.share

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mrcaracal.modul.Posts
import com.mrcaracal.utils.ConstantsFirebase
import java.util.*

class ShareViewModel : ViewModel() {
    var shareState: MutableLiveData<ShareViewState> = MutableLiveData<ShareViewState>()

    private lateinit var mGonderiler: Posts
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseUser: FirebaseUser = firebaseAuth.currentUser!!
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var storageReference: StorageReference = firebaseStorage.reference
    private lateinit var picturePath: Uri

    var postCode: String? = null
    lateinit var postID: String
    lateinit var tags: List<String>

    fun shareThePost(
        getPlaceName: String,
        getComment: String,
        getLocation: String,
        getAddress: String,
        getCity: String
    ) {
        if (getPlaceName.isEmpty() || getComment.isEmpty() || getLocation.isEmpty() || getAddress.isEmpty() || getCity.isEmpty()) {
            shareState.value = ShareViewState.ShowToastMessageAndBtnState
        } else {
            val uuid = UUID.randomUUID()
            val placeName = firebaseUser.email + "--" + getPlaceName + "--" + uuid
            try {
                storageReference
                    .child(ConstantsFirebase.STORAGE_NAME)
                    .child(placeName)
                    .putFile(picturePath)
                    .addOnSuccessListener {
                        val storageReference1 =
                            FirebaseStorage.getInstance()
                                .getReference(ConstantsFirebase.STORAGE_NAME + "/$placeName")
                        storageReference1
                            .downloadUrl
                            .addOnSuccessListener { uri ->
                                val firebaseUser = firebaseAuth.currentUser
                                val userEmail = firebaseUser!!.email
                                val pictureLink = uri.toString()
                                val placeName = getPlaceName
                                val location = getLocation
                                val comment = getComment
                                val address = getAddress
                                val city = getCity

                                val uuid1 = UUID.randomUUID()
                                postID = "" + uuid1
                                mGonderiler = Posts(
                                    gonderiID = postID,
                                    kullaniciEposta = userEmail,
                                    resimAdresi = pictureLink,
                                    yerIsmi = placeName,
                                    konum = location,
                                    adres = address,
                                    sehir = city,
                                    yorum = comment,
                                    postaKodu = postCode,
                                    taglar = tags,
                                    zaman = FieldValue.serverTimestamp()
                                )
                                val documentReference1 = firebaseFirestore
                                    .collection(ConstantsFirebase.COLLECTION_NAME_SHARED)
                                    .document(firebaseUser.email!!)
                                    .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SHARED)
                                    .document(postID)
                                documentReference1
                                    .set(mGonderiler)
                                    .addOnSuccessListener {
                                        val documentReference2 = firebaseFirestore
                                            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
                                            .document(postID)
                                        documentReference2
                                            .set(mGonderiler)
                                            .addOnSuccessListener {
                                                shareState.value = ShareViewState.OpenHomePage
                                            }
                                            .addOnFailureListener { exception ->
                                                shareState.value =
                                                    ShareViewState.ShowExceptionAndBtnState(
                                                        exception = exception
                                                    )
                                            }
                                    }
                                    .addOnFailureListener { exception ->
                                        shareState.value =
                                            ShareViewState.ShowExceptionAndBtnState(exception = exception)
                                    }
                            }
                            .addOnFailureListener { exception ->
                                shareState.value =
                                    ShareViewState.ShowExceptionAndBtnState(exception = exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        shareState.value =
                            ShareViewState.ShowExceptionAndBtnState(exception = exception)
                    }
            } catch (exception: Exception) {
                shareState.value = ShareViewState.ShowExceptionAndBtnState(exception = exception)
            }
        }
    }

    fun createTagForPost(tagsTakenByEditText: Array<String>) {
        var numberOfTags = 0
        var tagsToReturn = ""
        for (tags in tagsTakenByEditText) {
            numberOfTags++
            this.tags = Arrays.asList(*tagsTakenByEditText)
            tagsToReturn += "#$tags   "
            shareState.value = ShareViewState.GetTags(tagsToReturn)
            if (numberOfTags == 5) break
        }
    }

    fun imagePathForPostToBeShared(picturePath: Uri) {
        this.picturePath = picturePath
        shareState.value = ShareViewState.PicassoPross(picturePath = picturePath)
    }

    fun sendPostCode(postCode: String) {
        this.postCode = postCode
    }
}

sealed class ShareViewState {
    object ShowToastMessageAndBtnState : ShareViewState()
    object OpenHomePage : ShareViewState()

    data class ShowExceptionAndBtnState(val exception: Exception) : ShareViewState()
    data class GetTags(val tags: String) : ShareViewState()
    data class PicassoPross(val picturePath: Uri) : ShareViewState()
}