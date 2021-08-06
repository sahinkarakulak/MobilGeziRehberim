package com.mrcaracal.modul

class UserInfo {
    var kullaniciAdi: String? = null
    var kullaniciEposta: String? = null
    var kulaniciParola: String? = null
    var bio: String? = null
    var kullaniciResmi: String? = null

    constructor(
        kullaniciAdi: String?,
        kullaniciEposta: String?,
        kulaniciParola: String?,
        bio: String?,
        kullaniciResmi: String?
    ) {
        this.kullaniciAdi = kullaniciAdi
        this.kullaniciEposta = kullaniciEposta
        this.kulaniciParola = kulaniciParola
        this.bio = bio
        this.kullaniciResmi = kullaniciResmi
    }
}