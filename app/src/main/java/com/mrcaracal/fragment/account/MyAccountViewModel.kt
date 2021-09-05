package com.mrcaracal.fragment.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.utils.ConstantsFirebase

class MyAccountViewModel : ViewModel() {
    var myAccountState: MutableLiveData<MyAccountViewState> = MutableLiveData<MyAccountViewState>()

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var firebaseUser: FirebaseUser? = null
    var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()


    val postModelsList: ArrayList<PostModel> = arrayListOf()

    init {
        firebaseUser = firebaseAuth.currentUser
    }

    fun getPostData() {
        val documentReference = FirebaseFirestore
            .getInstance()
            .collection(ConstantsFirebase.FIREBASE_COLLECTION_NAME)
            .document((firebaseUser?.email)!!)
        documentReference
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = (task.result)
                    if (documentSnapshot.exists()) {
                        myAccountState.value = MyAccountViewState.ShowUserNameAndBio(
                            userName = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERNAME)
                                .toString(),
                            bio = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_BIO)
                                .toString()
                        )
                        myAccountState.value = MyAccountViewState.PicassoProccese(
                            loadData = documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERPIC)
                                .toString()
                        )
                        if (documentSnapshot.getString(ConstantsFirebase.FIREBASE_DOC_VAL_USERPIC) == null) {
                            myAccountState.value = MyAccountViewState.PicassoProcceseDefault
                        }
                    }
                }
            }
    }

    fun pullTheSharedOnMyAccount() {
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_SHARED)
            .document((firebaseUser?.email)!!)
            .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SHARED)
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        documentSnapshot.data?.let {
                            val postModel = PostModelProvider.provide(it)
                            postModelsList.add(postModel)
                        }
                        myAccountState.value = MyAccountViewState.SendRecyclerAdapter(postModelList = postModelsList)
                    }
                }
            }
            .addOnFailureListener { exception ->
                myAccountState.value =
                    MyAccountViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun pullTheRecordedOnMyAccount() {
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser?.email)!!)
            .collection(ConstantsFirebase.COLLECTION_NAME_SAVED)
        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        documentSnapshot.data?.let {
                            val postModel = PostModelProvider.provide(it)
                            postModelsList.add(postModel)
                        }
                        myAccountState.value = MyAccountViewState.SendRecyclerAdapter(postModelList = postModelsList)
                    }
                }
            }
            .addOnFailureListener { exception ->
                myAccountState.value =
                    MyAccountViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun clearList() {
        postModelsList.clear()
    }

    fun removePostFromShared(positionValue: Int) {

        // Step-1
        firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_SHARED)
            .document(postModelsList[positionValue].userEmail)
            .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SHARED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener { exception ->
                myAccountState.value =
                    MyAccountViewState.ShowExceptionMessage(exception = exception)
            }

        // Step-2
        firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener { exception ->
                myAccountState.value =
                    MyAccountViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun removePostFromSaved(
        positionValue: Int
    ) {
        firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser?.email)!!)
            .collection(ConstantsFirebase.COLLECTION_NAME_SAVED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener { exception ->
                myAccountState.value =
                    MyAccountViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun showTagsOnPost(postModel: PostModel, tabControl: String): String {
        var tagsToReturn = ""
        val tagsTakenByEditText = postModel.tag
        val tagLength = tagsTakenByEditText.length
        val tagTaken: String
        val tagShredding: Array<String>
        when (tabControl) {
            "paylasilanlar" -> {
                tagTaken = tagsTakenByEditText.substring(1, tagLength - 1)
                tagShredding = tagTaken.split(",").toTypedArray()
                for (tags: String in tagShredding) {
                    tagsToReturn += "#" + tags.trim { it <= ' ' } + " "
                }
            }
            "kaydedilenler" -> {
                tagTaken = tagsTakenByEditText.substring(2, tagLength - 2)
                tagShredding = tagTaken.split(",").toTypedArray()
                for (tags: String in tagShredding) {
                    tagsToReturn += "#" + tags.trim { it <= ' ' } + " "
                }
            }
        }
        return tagsToReturn
    }

}

sealed class MyAccountViewState {
    object PicassoProcceseDefault : MyAccountViewState()

    data class ShowExceptionMessage(val exception: Exception) : MyAccountViewState()
    data class PicassoProccese(val loadData: String) : MyAccountViewState()
    data class ShowUserNameAndBio(val userName: String, val bio: String) : MyAccountViewState()
    data class SendRecyclerAdapter(val postModelList: ArrayList<PostModel>) :
        MyAccountViewState()
}