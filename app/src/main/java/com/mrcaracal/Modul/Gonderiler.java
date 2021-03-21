package com.mrcaracal.Modul;

import com.google.firebase.firestore.FieldValue;

import java.util.Arrays;
import java.util.List;

public class Gonderiler {

    private String gonderiID;
    private String kullaniciEposta;
    private String resimAdresi;
    private String yerIsmi;
    private String konum;
    private String adres;
    private String yorum;
    private String postaKodu;
    private List<String> taglar;
    private FieldValue zaman;

    public Gonderiler() {

    }

    public Gonderiler(String gonderiID, String kullaniciEposta, String resimAdresi, String yerIsmi, String konum, String adres, String yorum, FieldValue zaman) {
        this.gonderiID = gonderiID;
        this.kullaniciEposta = kullaniciEposta;
        this.resimAdresi = resimAdresi;
        this.yerIsmi = yerIsmi;
        this.konum = konum;
        this.adres = adres;
        this.yorum = yorum;
        this.zaman = zaman;
    }

    public Gonderiler(String gonderiID, String kullaniciEposta, String resimAdresi, String yerIsmi, String konum, String adres, String yorum, String postaKodu, List<String> taglar, FieldValue zaman) {
        this.gonderiID = gonderiID;
        this.kullaniciEposta = kullaniciEposta;
        this.resimAdresi = resimAdresi;
        this.yerIsmi = yerIsmi;
        this.konum = konum;
        this.adres = adres;
        this.yorum = yorum;
        this.postaKodu = postaKodu;
        this.taglar = taglar;
        this.zaman = zaman;
    }

    public String getGonderiID() {
        return gonderiID;
    }

    public void setGonderiID(String gonderiID) {
        this.gonderiID = gonderiID;
    }

    public String getKullaniciEposta() {
        return kullaniciEposta;
    }

    public void setKullaniciEposta(String kullaniciEposta) {
        this.kullaniciEposta = kullaniciEposta;
    }

    public String getResimAdresi() {
        return resimAdresi;
    }

    public void setResimAdresi(String resimAdresi) {
        this.resimAdresi = resimAdresi;
    }

    public String getYerIsmi() {
        return yerIsmi;
    }

    public void setYerIsmi(String yerIsmi) {
        this.yerIsmi = yerIsmi;
    }

    public String getKonum() {
        return konum;
    }

    public void setKonum(String konum) {
        this.konum = konum;
    }

    public String getAdres() {
        return adres;
    }

    public void setAdres(String adres) {
        this.adres = adres;
    }

    public String getYorum() {
        return yorum;
    }

    public void setYorum(String yorum) {
        this.yorum = yorum;
    }

    public String getPostaKodu() {
        return postaKodu;
    }

    public void setPostaKodu(String postaKodu) {
        this.postaKodu = postaKodu;
    }

    public List<String> getTaglar(){
        return taglar;
    }

    public void setTaglar(List<String> taglar){
        this.taglar = taglar;
    }

    public FieldValue getZaman() {
        return zaman;
    }

    public void setZaman(FieldValue zaman) {
        this.zaman = zaman;
    }
}