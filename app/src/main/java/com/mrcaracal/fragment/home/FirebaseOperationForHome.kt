package com.mrcaracal.fragment.home

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.modul.Posts
import java.util.*

class FirebaseOperationForHome(
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

    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    fun tagGoster(position: Int): String {
        var taggg = ""
        val al_taglar = tagsFirebase[position]
        val tag_uzunluk = al_taglar.length
        val alinan_taglar = al_taglar.substring(1, tag_uzunluk - 1)
        val a_t = alinan_taglar.split(",").toTypedArray()
        for (tags: String in a_t) {
            taggg += "#" + tags.trim { it <= ' ' } + " "
        }
        return taggg
    }

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

                        // Çekilen her veriyi Map dizinine at ve daha sonra çekip kullan
                        val dataCluster = snapshot.data
                        val postID = dataCluster!!["gonderiID"].toString()
                        val userEmail = dataCluster["kullaniciEposta"].toString()
                        var palceName = dataCluster["yerIsmi"].toString()
                        palceName = palceName.substring(0, 1).uppercase() + palceName.substring(1)
                        val pictureLink = dataCluster["resimAdresi"].toString()
                        val location = dataCluster["konum"].toString()
                        val addres = dataCluster["adres"].toString()
                        val city = dataCluster["sehir"].toString()
                        val comment = dataCluster["yorum"].toString()
                        val postCode = dataCluster["postaKodu"].toString()
                        val time = dataCluster["zaman"] as Timestamp

                        postIDsFirebase.add(postID)
                        userEmailsFirebase.add(userEmail)
                        pictureLinksFirebase.add(pictureLink)
                        placeNamesFirebase.add(palceName)
                        locationFirebase.add(location)
                        addressesFirebase.add(addres)
                        citiesFirebase.add(city)
                        commentsFirebase.add(comment)
                        postCodesFirebase.add(postCode)
                        tagsFirebase.add(dataCluster["taglar"].toString())
                        timesFirebase.add(time)
                        recyclerAdapterStructure.notifyDataSetChanged()

                    }
                }
            }
    }

    fun saveOperations(
        position: Int,
        firebaseUser: FirebaseUser,
        firebaseFirestore: FirebaseFirestore
    ) {
        if ((userEmailsFirebase[position] == firebaseUser.email)) {
            //activity?.let { toast(it, "Bunu zaten siz paylaştınız") }
        } else {
            val MGonderiler = Posts(
                postIDsFirebase[position],
                userEmailsFirebase[position],
                pictureLinksFirebase[position],
                placeNamesFirebase[position],
                locationFirebase[position],
                addressesFirebase[position],
                citiesFirebase[position],
                commentsFirebase[position],
                postCodesFirebase[position], listOf(tagsFirebase[position]),
                FieldValue.serverTimestamp()
            )
            val documentReference = firebaseFirestore
                .collection(COLLECTION_NAME_THEY_SAVED)
                .document((firebaseUser.email)!!)
                .collection(COLLECTION_NAME_SAVED)
                .document(postIDsFirebase[position])
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

}