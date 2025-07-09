package com.hugin_munin.model;

public class CausaBaja {
    public int idCausaBaja;
    public String nombreCausaBaja;

    //Constructores
    public CausaBaja(int idCausaBaja, String nombreCausaBaja) {
        this.idCausaBaja = idCausaBaja;
        this.nombreCausaBaja = nombreCausaBaja;
    }

    public CausaBaja() {
    }

    //Getter
    public int getIdCausaBaja() {
        return idCausaBaja;
    }

    public String getNombreCausaBaja() {
        return nombreCausaBaja;
    }

    //Setter
    public void setIdCausaBaja(int idCausaBaja) {
        this.idCausaBaja = idCausaBaja;
    }

    public void setNombreCausaBaja(String nombreCausaBaja) {
        this.nombreCausaBaja = nombreCausaBaja;
    }
}