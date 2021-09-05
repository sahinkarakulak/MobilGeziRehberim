package com.mrcaracal.fragment.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.modul.Posts
import com.mrcaracal.modul.UserAccountStore
import com.mrcaracal.utils.ConstantsFirebase
import com.mrcaracal.utils.ShowTags
import java.util.*

class SearchViewModel : ViewModel() {
    var searchState: MutableLiveData<SearchViewState> = MutableLiveData<SearchViewState>()

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var firebaseUser: FirebaseUser
    var firebaseFirestore: FirebaseFirestore


    val postModelsList: ArrayList<PostModel> = arrayListOf()

    init {
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    fun clearList() {
        postModelsList.clear()
    }

    fun getPostData(data: Map<String, Any>?) {
        data?.let {
            val postModel = PostModelProvider.provide(it)
            postModelsList.add(postModel)
        }
    }

    fun searchOnPost(relevantField: String?, keywordWrited: String) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
        collectionReference
            .orderBy((relevantField)!!)
            .startAt(keywordWrited)
            .endAt(keywordWrited + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getPostData(snapshot.data)
                        searchState.value = SearchViewState.SendRecyclerAdapter(postModelsList = postModelsList)
                    }
                }
            }.addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun searchByTag(relevantField: String?, keywordWrited: String?) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
        collectionReference
            .whereArrayContains((relevantField)!!, (keywordWrited)!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        getPostData(documentSnapshot.data)
                        searchState.value = SearchViewState.SendRecyclerAdapter(postModelsList = postModelsList)
                    }
                }
            }
            .addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun searchByCity(postCode: String) {
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)
        collectionReference
            .orderBy("postaKodu")
            .startAt(postCode)
            .endAt(postCode + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getPostData(snapshot.data)
                        searchState.value = SearchViewState.SendRecyclerAdapter(postModelsList = postModelsList)

                    }
                }
            }.addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun savePostOnSearchFragment(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            searchState.value = SearchViewState.ShowAlreadySharedToastMessage
        } else {
            val MGonderiler = Posts(
                gonderiID = postModel.postId,
                kullaniciEposta = postModel.userEmail,
                resimAdresi = postModel.pictureLink,
                yerIsmi = postModel.placeName,
                konum = postModel.location,
                adres = postModel.address,
                sehir = postModel.city,
                yorum = postModel.comment,
                postaKodu = postModel.postCode,
                taglar = listOf(postModel.tag),
                zaman = FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SAVED)
                .document((firebaseUser.email)!!)
                .collection(ConstantsFirebase.COLLECTION_NAME_SAVED)
                .document(postModel.postId)
            documentReference
                .set(MGonderiler)
                .addOnSuccessListener {
                    //
                }
                .addOnFailureListener { exception ->
                    searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
                }
        }
    }

    fun showTagsOnPost(postModel: PostModel): String {
        return ShowTags.showPostTags(postModel = postModel)
    }

    fun reportPostFromSearchFragment(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            searchState.value = SearchViewState.ShowAlreadySharedToastMessage
        } else {
            searchState.value = SearchViewState.OpenEmail(
                subject = "Subject",
                message = "Message",
                emails = UserAccountStore.adminAccountEmails
            )
        }
    }

}

sealed class SearchViewState {
    object ShowAlreadySharedToastMessage : SearchViewState()

    data class ShowExceptionMessage(val exception: Exception) : SearchViewState()

    data class OpenEmail(val subject: String, val message: String, val emails: ArrayList<String>) :
        SearchViewState()

    data class SendRecyclerAdapter(val postModelsList: ArrayList<PostModel>) :
        SearchViewState()
}