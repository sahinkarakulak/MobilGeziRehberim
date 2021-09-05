package com.mrcaracal.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.modul.Posts
import com.mrcaracal.modul.UserAccountStore
import com.mrcaracal.utils.ConstantsFirebase
import com.mrcaracal.utils.ShowTags

class HomePageViewModel : ViewModel() {
    var homePageState: MutableLiveData<HomePageViewState> = MutableLiveData<HomePageViewState>()

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseUser: FirebaseUser = firebaseAuth.currentUser!!
    private var firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getPostByPostTime(postModelsList: ArrayList<PostModel>) {
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)

        collectionReference
            .orderBy("zaman", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        snapshot.data?.let {
                            val postModel = PostModelProvider.provide(it)
                            postModelsList.add(postModel)
                        }
                        homePageState.value =
                            HomePageViewState.SendRecyclerAdapter(postModelsList = postModelsList)
                    }
                }
            }
            .addOnFailureListener { exception ->
                homePageState.value = HomePageViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun savePostOnHomePage(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            homePageState.value = HomePageViewState.ShowAlreadySharedToastMessage
        } else {
            val mGonderiler = Posts(
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
                .set(mGonderiler)
                .addOnSuccessListener {
                    //
                }
                .addOnFailureListener { exception ->
                    homePageState.value =
                        HomePageViewState.ShowExceptionMessage(exception = exception)
                }
        }
    }

    fun showTagsOnPost(postModel: PostModel): String {
        return ShowTags.showPostTags(postModel = postModel)
    }

    fun saveOperations(postModel: PostModel) {
        firebaseUser.let {
            savePostOnHomePage(postModel = postModel)
        }
    }

    fun reportPostFromHomePage(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            homePageState.value = HomePageViewState.ShowAlreadySharedToastMessage
        } else {
            homePageState.value = HomePageViewState.OpenEmail(
                subject = "Subject",
                message = "Message",
                emails = UserAccountStore.adminAccountEmails
            )
        }
    }
}

sealed class HomePageViewState {
    object ShowAlreadySharedToastMessage : HomePageViewState()

    data class OpenEmail(val subject: String, val message: String, val emails: ArrayList<String>) :
        HomePageViewState()

    data class SendRecyclerAdapter(val postModelsList: ArrayList<PostModel>) :
        HomePageViewState()

    data class ShowExceptionMessage(val exception: Exception) : HomePageViewState()
}