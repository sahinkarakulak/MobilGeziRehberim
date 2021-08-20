package com.mrcaracal.fragment.account

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.fragment.model.PostModelProvider

class FirebaseOperationForAccount {

    private val COLLECTION_NAME_SHARED = "Paylasilanlar"
    private val COLLECTION_NAME_THEY_SHARED = "Paylastiklari"
    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    fun clearList() {
        postModelsList.clear()
    }

    fun pullTheShared(
        firebaseFirestore: FirebaseFirestore,
        firebaseUser: FirebaseUser,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_SHARED)
            .document((firebaseUser.email)!!)
            .collection(COLLECTION_NAME_THEY_SHARED)
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
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                //
            }
    }

    fun pullTheRecorded(
        firebaseFirestore: FirebaseFirestore,
        firebaseUser: FirebaseUser,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
        val collectionReference = firebaseFirestore
            .collection(COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser.email)!!)
            .collection(COLLECTION_NAME_SAVED)
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
                        recyclerAdapterStructure.postModelList = postModelsList
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { e ->
                //
            }
    }

    fun removeFromShared(firebaseFirestore: FirebaseFirestore, positionValue: Int) {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!

        //1. Adım
        firebaseFirestore
            .collection(COLLECTION_NAME_SHARED)
            .document(postModelsList[positionValue].userEmail)
            .collection(COLLECTION_NAME_THEY_SHARED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }

        //2. Adım
        firebaseFirestore
            .collection(COLLECTION_NAME_POST)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun removeFromSaved(
        firebaseFirestore: FirebaseFirestore,
        firebaseUser: FirebaseUser,
        positionValue: Int
    ) {

        // ÖNEMLİ
        // ALERTDIALOG İLE EMİN MİSİN DİYE KULLANICIYA SORULSUN. VERİLEN CEVABA GÖRE İŞLEM YAPILSIN!
        firebaseFirestore
            .collection(COLLECTION_NAME_THEY_SAVED)
            .document((firebaseUser.email)!!)
            .collection(COLLECTION_NAME_SAVED)
            .document(postModelsList[positionValue].postId)
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun showTag(postModel: PostModel, tabControl: String): String {
        var taggg = ""
        val al_taglar = postModel.tag
        val tag_uzunluk = al_taglar.length
        val alinan_taglar: String
        val a_t: Array<String>
        when (tabControl) {
            "paylasilanlar" -> {
                alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
                a_t = alinan_taglar.split(",").toTypedArray()
                for (tags: String in a_t) {
                    taggg += "#" + tags.trim { it <= ' ' } + " "
                }
            }
            "kaydedilenler" -> {
                alinan_taglar = al_taglar.substring(2, tag_uzunluk - 2)
                a_t = alinan_taglar.split(",").toTypedArray()
                for (tags: String in a_t) {
                    taggg += "#" + tags.trim { it <= ' ' } + " "
                }
            }
        }
        return taggg
    }

}