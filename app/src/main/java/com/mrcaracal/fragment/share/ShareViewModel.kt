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

    private lateinit var MGonderiler: Posts
    private lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var picturePath: Uri

    var postCode: String? = null
    lateinit var postID: String
    lateinit var tags: List<String>

    fun init() {
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    fun shareSend(
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
            val placeName = firebaseUser!!.email + "--" + getPlaceName + "--" + uuid
            try {
                storageReference
                    .child(ConstantsFirebase.STORAGE_NAME)
                    .child(placeName)
                    .putFile(picturePath)
                    .addOnSuccessListener {
                        val storageReference1 =
                            FirebaseStorage.getInstance().getReference(ConstantsFirebase.STORAGE_NAME + "/$placeName")
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
                                var city = getCity

                                val uuid1 = UUID.randomUUID()
                                postID = "" + uuid1
                                MGonderiler = Posts(
                                    postID, userEmail, pictureLink, placeName, location, address,
                                    city, comment, postCode, tags, FieldValue.serverTimestamp()
                                )
                                val documentReference1 = firebaseFirestore
                                    .collection(ConstantsFirebase.COLLECTION_NAME_SHARED)
                                    .document(firebaseUser.email!!)
                                    .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SHARED)
                                    .document(postID)
                                documentReference1
                                    .set(MGonderiler)
                                    .addOnSuccessListener {
                                        val documentReference2 = firebaseFirestore
                                            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
                                            .document(postID)
                                        documentReference2
                                            .set(MGonderiler)
                                            .addOnSuccessListener {
                                                shareState.value = ShareViewState.OpenHomePage
                                            }
                                            .addOnFailureListener { e ->
                                                shareState.value =
                                                    ShareViewState.ShowExceptionAndBtnState(
                                                        exception = e
                                                    )
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        shareState.value =
                                            ShareViewState.ShowExceptionAndBtnState(exception = e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                shareState.value =
                                    ShareViewState.ShowExceptionAndBtnState(exception = e)
                            }
                        /*val myToast =
                            Toast.makeText(activity, getString(R.string.sended), Toast.LENGTH_SHORT)
                        myToast.show()
                        val handler = Handler()
                        handler.postDelayed({ myToast.cancel() }, 400)*/
                    }
                    .addOnFailureListener { e ->
                        shareState.value = ShareViewState.ShowExceptionAndBtnState(exception = e)
                    }
            } catch (e: Exception) {
                shareState.value = ShareViewState.ShowExceptionAndBtnState(exception = e)
            }
        }
    }

    fun createTag(tagler: Array<String>) {
        var etiket_sayisi = 0
        var taggg = ""
        for (tags in tagler) {
            etiket_sayisi++
            this.tags = Arrays.asList(*tagler)
            taggg += "#$tags   "
            shareState.value = ShareViewState.GetTags(taggg)
            if (etiket_sayisi == 5) break
        }
    }

    fun picturePath(picturePath: Uri) {
        this.picturePath = picturePath
        shareState.value = ShareViewState.PicassoPross(picturePath = picturePath)
    }

    fun sendPostCode(postCode: String){
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