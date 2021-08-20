package com.mrcaracal.fragment.home

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.modul.Posts
import java.util.*

class FirebaseOperationForHome {

    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    fun rewind(
        firebaseFirestore: FirebaseFirestore,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_POST)

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

    fun saveOperations(
        postModel: PostModel,
        firebaseUser: FirebaseUser,
        firebaseFirestore: FirebaseFirestore
    ) {
        if ((postModel.userEmail == firebaseUser.email)) {
            //activity?.let { toast(it, "Bunu zaten siz paylaştınız") }
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

}