package com.mrcaracal.Modul;

public class KullaniciBilgileri {

    private String kullaniciAdi;
    private String kullaniciEposta;
    private String kulaniciParola;
    private String bio;
    private String kullaniciResmi;

    public KullaniciBilgileri() {

    }

    public KullaniciBilgileri(String kullaniciAdi, String kullaniciEposta, String kulaniciParola, String bio, String kullaniciResmi) {
        this.kullaniciAdi = kullaniciAdi;
        this.kullaniciEposta = kullaniciEposta;
        this.kulaniciParola = kulaniciParola;
        this.bio = bio;
        this.kullaniciResmi = kullaniciResmi;
    }

    public KullaniciBilgileri(String kullaniciAdi, String kullaniciEposta, String bio, String kullaniciResmi) {
        this.kullaniciAdi = kullaniciAdi;
        this.kullaniciEposta = kullaniciEposta;
        this.bio = bio;
        this.kullaniciResmi = kullaniciResmi;
    }


    public String getKullaniciAdi() {
        return kullaniciAdi;
    }

    public void setKullaniciAdi(String kullaniciID) {
        this.kullaniciAdi = kullaniciID;
    }

    public String getKullaniciEposta() {
        return kullaniciEposta;
    }

    public void setKullaniciEposta(String kullaniciEposta) {
        this.kullaniciEposta = kullaniciEposta;
    }

    public String getKulaniciParola() {
        return kulaniciParola;
    }

    public void setKulaniciParola(String kulaniciParola) {
        this.kulaniciParola = kulaniciParola;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getKullaniciResmi() {
        return kullaniciResmi;
    }

    public void setKullaniciResmi(String kullaniciResmi) {
        this.kullaniciResmi = kullaniciResmi;
    }
}
