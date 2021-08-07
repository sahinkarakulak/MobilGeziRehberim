package com.mrcaracal.fragment.home

import com.google.firebase.Timestamp
import java.util.ArrayList

class FirebaseModul(
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
}