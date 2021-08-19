package com.mrcaracal.fragment.search

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mrcaracal.adapter.RecyclerAdapterStructure
import com.mrcaracal.fragment.model.PostModelProvider
import com.mrcaracal.fragment.model.PostModel
import com.mrcaracal.modul.Posts
import java.util.*

private const val TAG = "FirebaseOperationForSea"

class FirebaseOperationForSearch() {

    private val COLLECTION_NAME_SAVED = "Kaydedilenler"
    private val COLLECTION_NAME_THEY_SAVED = "Kaydedenler"
    private val COLLECTION_NAME_POST = "Gonderiler"

    val postModelsList: ArrayList<PostModel> = arrayListOf()

    fun clearList() {
        postModelsList.clear()
    }

    fun getData(data: Map<String, Any>?) {

        data?.let {
            val postModel = PostModelProvider.provide(it)
            postModelsList.add(postModel)
            Log.i(TAG, "getData: " + postModel.placeName)
        }

    }

    fun listNearbyPlaces(activity: Activity, recyclerAdapterStructure: RecyclerAdapterStructure) {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        try {
            if (ActivityCompat.checkSelfPermission(
                    (activity),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    (activity), Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            LocationServices.getFusedLocationProviderClient(activity)
                .requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        LocationServices.getFusedLocationProviderClient(activity)
                            .removeLocationUpdates(this)
                        if (locationResult.locations.size > 0) {
                            val lastestLocationIndex = locationResult.locations.size - 1
                            val latitude = locationResult.locations[lastestLocationIndex].latitude
                            val longitude = locationResult.locations[lastestLocationIndex].longitude
                            val geocoder = Geocoder(activity, Locale.getDefault())
                            var addressList: List<Address>
                            try {
                                addressList = geocoder.getFromLocation(latitude, longitude, 1)

                                if (addressList != null) {
                                    val postaKodumuz = addressList[0].postalCode
                                    searchForCity(
                                        postaKodumuz.substring(0, 2),
                                        FirebaseFirestore.getInstance(),
                                        recyclerAdapterStructure
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                        super.onLocationAvailability(locationAvailability)
                    }
                }, Looper.getMainLooper())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun search(
        relevantField: String?,
        keywordWrited: String,
        firebaseFirestore: FirebaseFirestore,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
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
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { e ->
                //
            }
    }

    fun searchForTag(
        relevantField: String?,
        keywordWrited: String?,
        firebaseFirestore: FirebaseFirestore,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
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
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }
            .addOnFailureListener { }
    }

    fun searchForCity(
        postCode: String,
        firebaseFirestore: FirebaseFirestore,
        recyclerAdapterStructure: RecyclerAdapterStructure
    ) {
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
                        recyclerAdapterStructure.notifyDataSetChanged()
                    }
                }
            }.addOnFailureListener { e ->
                //
            }
    }

    fun saveOperations(
        postModel: PostModel,
        firebaseUser: FirebaseUser,
        firebaseFirestore: FirebaseFirestore
    ) {
        if ((postModel.userEmail == firebaseUser.email)) {
            //
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