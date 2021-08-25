package com.mrcaracal.fragment.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.Interface.RecyclerViewClickInterface
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.modul.Posts
import com.mrcaracal.modul.UserAccountStore
import java.util.*

class SearchViewModel : ViewModel() {
    var searchState: MutableLiveData<SearchViewState> = MutableLiveData<SearchViewState>()

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseUser: FirebaseUser
    lateinit var firebaseFirestore: FirebaseFirestore
    lateinit var recyclerAdapterStructure: RecyclerAdapterStructure

    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    fun clearList() {
        postModelsList.clear()
    }

    fun getData(data: Map<String, Any>?) {
        data?.let {
            val postModel = PostModelProvider.provide(it)
            postModelsList.add(postModel)
        }
    }

    fun search(relevantField: String?, keywordWrited: String) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .orderBy((relevantField)!!)
            .startAt(keywordWrited)
            .endAt(keywordWrited + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getData(snapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun searchForTag(relevantField: String?, keywordWrited: String?) {
        clearList()
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .whereArrayContains((relevantField)!!, (keywordWrited)!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (documentSnapshot: DocumentSnapshot in querySnapshot) {
                        getData(documentSnapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun searchForCity(postCode: String) {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)
        collectionReference
            .orderBy("postaKodu")
            .startAt(postCode)
            .endAt(postCode + "\uf8ff")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val querySnapshot = (task.result)
                    for (snapshot: DocumentSnapshot in querySnapshot) {
                        getData(snapshot.data)
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { exception ->
                searchState.value = SearchViewState.ShowExceptionMessage(exception = exception)
            }
    }

    fun saveOperations(postModel: PostModel) {
        if ((postModel.userEmail == firebaseUser.email)) {
            searchState.value = SearchViewState.ShowAlreadySharedToastMessage
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
                .collection(COLLECTION_NAME_THEY_SAVED)
                .document((firebaseUser.email)!!)
                .collection(COLLECTION_NAME_SAVED)
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

    fun reportPost(postModel: PostModel) {
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

    fun recyclerAdapterProccese(thisClick: RecyclerViewClickInterface) {
        recyclerAdapterStructure = RecyclerAdapterStructure(thisClick)
        searchState.value =
            SearchViewState.SendRecyclerAdapter(recyclerAdapterStructure = recyclerAdapterStructure)
    }


}

sealed class SearchViewState {
    object ShowAlreadySharedToastMessage : SearchViewState()

    data class ShowExceptionMessage(val exception: Exception): SearchViewState()

    data class OpenEmail(val subject: String, val message: String, val emails: ArrayList<String>) :
        SearchViewState()

    data class SendRecyclerAdapter(val recyclerAdapterStructure: RecyclerAdapterStructure) :
        SearchViewState()
}