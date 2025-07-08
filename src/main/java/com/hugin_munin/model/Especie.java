package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Especie {
    //Atributos, usando JsonProperty para indicar el formato de la llaves del .json
    @JsonProperty("id_especie")
    private Integer id_especie;

    @JsonProperty("genero")
    private String genero;

    @JsonProperty("especie")
    private String especie;

    //Constructores
    public Especie(){
    }

    public Especie(String especie, String genero) {
        this.especie = especie;
        this.genero = genero;
    }

    public Especie(Integer id_especie, String genero, String especie) {
        this.id_especie = id_especie;
        this.genero = genero;
        this.especie = especie;
    }

    //Getters
    public Integer getId_especie() {
        return id_especie;
    }

    public String getGenero() {
        return genero;
    }

    public String getEspecie() {
        return especie;
    }

    //Setters
    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public void setId_especie(Integer id_especie) {
        this.id_especie = id_especie;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Especie especie1 = (Especie) o;
        return id_especie == especie1.id_especie && Objects.equals(genero, especie1.genero) && Objects.equals(especie, especie1.especie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_especie, genero, especie);
    }

    public boolean isValid(){
        return genero != null && !genero.trim().isEmpty() && especie != null && !especie.trim().isEmpty();
    }
}