package com.mrcaracal.fragment.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.modul.Posts
import com.mrcaracal.modul.UserAccountStore
import com.mrcaracal.utils.ConstantsFirebase

class HomePageViewModel : ViewModel() {
    var homePageState: MutableLiveData<HomePageViewState> = MutableLiveData<HomePageViewState>()

    lateinit var firebaseAuth: FirebaseAuth
    var firebaseUser: FirebaseUser? = null
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    fun rewind(postModelsList: ArrayList<PostModel>) {
        val collectionReference = firebaseFirestore
            .collection(ConstantsFirebase.COLLECTION_NAME_POST)

        // VT'ye kaydedilme zamanına göre verileri çek
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
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
    }

    fun saveOperations(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser!!.email)) {
            homePageState.value = HomePageViewState.ShowAlreadySharedToastMessage
        } else {
            val MGonderiler = Posts(
                postModel.postId,
                postModel.userEmail,
                postModel.pictureLink,
                postModel.placeName,
                postModel.location,
                postModel.address,
                postModel.city,
                postModel.comment,
                postModel.postCode,
                listOf(postModel.tag),
                FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                .collection(ConstantsFirebase.COLLECTION_NAME_THEY_SAVED)
                .document((firebaseUser!!.email)!!)
                .collection(ConstantsFirebase.COLLECTION_NAME_SAVED)
                .document(postModel.postId)
            documentReference
                .set(MGonderiler)
                .addOnSuccessListener {
                    //
                }
                .addOnFailureListener { e ->
                    //
                }
        }
    }

    fun showTag(postModel: PostModel): String {
        var taggg = ""
        val al_taglar = postModel.tag
        val tag_uzunluk = al_taglar.length
        val alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
        val a_t = alinan_taglar.split(",").toTypedArray()
        for (tags: String in a_t) {
            taggg += "#" + tags.trim { it <= ' ' } + " "
        }
        return taggg
    }

    fun getSaveOperations(postModel: PostModel) {
        firebaseUser?.let { it1 ->
            saveOperations(postModel)
        }
    }

    fun reportPost(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser?.email)) {
            homePageState.value = HomePageViewState.ShowAlreadySharedToastMessage
        } else {
            homePageState.value = HomePageViewState.OpenEmail(
                subject = "Subject",
                message = "Message",
                emails = UserAccountStore.adminAccountEmails
            )
        }
    }

    fun recyclerAdapterProccese(thisClick: RecyclerViewClickInterface) {
        recyclerAdapterStructure = RecyclerAdapterStructure(thisClick)
        homePageState.value = HomePageViewState.SendRecyclerAdapter(recyclerAdapterStructure = recyclerAdapterStructure)
    }

}

sealed class HomePageViewState {
    object ShowAlreadySharedToastMessage : HomePageViewState()

    data class OpenEmail(val subject: String, val message: String, val emails: ArrayList<String>) :
        HomePageViewState()

    data class SendRecyclerAdapter(val recyclerAdapterStructure: RecyclerAdapterStructure) :
        HomePageViewState()
}