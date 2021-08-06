package com.mrcaracal.fragment.account

import android.util.Log
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.adapter.RecyclerAdapterStructure
import java.util.*

class FirebaseOperationForAccount(
    var postIDsFirebase: ArrayList<String>,
    var userEmailsFirebase: ArrayList<String>,
    var pictureLinksFirebase: ArrayList<String>,
    var placeNamesFirebase: ArrayList<String>,
    var locationFirebase: ArrayList<String>,
    var addressesFirebase: ArrayList<String>,
    var citiesFirebase: ArrayList<String>,
    var commentsFirebase: ArrayList<String>,
    var postCodesFirebase: ArrayList<String>,
    var tagsFirebase: ArrayList<String>,
    var timesFirebase: ArrayList<Timestamp>
) {

    private val COLLECTION_NAME_SHARED = "Paylasilanlar"
    private val COLLECTION_NAME_THEY_SHARED = "Paylastiklari"
    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    fun clearList() {
        postIDsFirebase.clear()
        userEmailsFirebase.clear()
        pictureLinksFirebase.clear()
        placeNamesFirebase.clear()
        locationFirebase.clear()
        addressesFirebase.clear()
        citiesFirebase.clear()
        commentsFirebase.clear()
        postCodesFirebase.clear()
        tagsFirebase.clear()
        timesFirebase.clear()
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
                        val dataClusterAccount = documentSnapshot.data
                        val postID = dataClusterAccount!!["gonderiID"].toString()
                        val userEmail = dataClusterAccount["kullaniciEposta"].toString()
                        var palceName = dataClusterAccount["yerIsmi"].toString()
                        palceName = palceName.substring(0, 1).uppercase() + palceName.substring(1)
                        val location = dataClusterAccount["konum"].toString()
                        val pictureLink = dataClusterAccount["resimAdresi"].toString()
                        val comment = dataClusterAccount["yorum"].toString()
                        val addres = dataClusterAccount["adres"].toString()
                        val city = dataClusterAccount["sehir"].toString()
                        val postCode = dataClusterAccount["postaKodu"].toString()
                        val time = dataClusterAccount["zaman"] as Timestamp
                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(pictureLink)
                        placeNamesFirebase.add(palceName)
                        locationFirebase.add(location)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataClusterAccount["taglar"].toString())
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        timesFirebase.add(time)
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
                        val dataClusterAccount = documentSnapshot.data
                        val postID = dataClusterAccount!!["gonderiID"].toString()
                        val userEmail = dataClusterAccount["kullaniciEposta"].toString()
                        var placeName = dataClusterAccount["yerIsmi"].toString()
                        placeName = placeName.substring(0, 1)
                            .uppercase(Locale.getDefault()) + placeName.substring(1)
                        val location = dataClusterAccount["konum"].toString()
                        val picatureLink = dataClusterAccount["resimAdresi"].toString()
                        val comment = dataClusterAccount["yorum"].toString()
                        val postCode = dataClusterAccount["postaKodu"].toString()
                        val addres = dataClusterAccount["adres"].toString()
                        val city = dataClusterAccount["sehir"].toString()
                        val time = dataClusterAccount["zaman"] as Timestamp
                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(picatureLink)
                        placeNamesFirebase.add(placeName)
                        locationFirebase.add(location)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataClusterAccount["taglar"].toString())
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        timesFirebase.add(time)
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
            .document(userEmailsFirebase[positionValue])
            .collection(COLLECTION_NAME_THEY_SHARED)
            .document(postIDsFirebase[positionValue])
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
            .document(postIDsFirebase[positionValue])
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
            .document(postIDsFirebase[positionValue])
            .delete()
            .addOnSuccessListener {
                //
            }
            .addOnFailureListener {
                //
            }
    }

    fun showTag(position: Int, tabControl: String): String {
        var taggg = ""
        val al_taglar = tagsFirebase[position]
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