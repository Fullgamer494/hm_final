package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Especimen {
    //Atributos, usando JsonProperty para indicar el formato de la llaves del .json
    @JsonProperty("id_especimen")
    private Integer id_especimen;

    @JsonProperty("num_inventario")
    private String num_inventario;

    @JsonProperty("id_especie")
    private Integer id_especie;

    @JsonProperty("especie")
    private Especie especie;

    @JsonProperty("nombre_especimen")
    private String nombre_especimen;

    @JsonProperty("activo")
    private boolean activo;

    //Constructores
    public Especimen(){
    }

    public Especimen(String num_inventario, Integer id_especie, Especie especie, String nombre_especimen, boolean activo) {
        this.num_inventario = num_inventario;
        this.id_especie = id_especie;
        this.especie = especie;
        this.nombre_especimen = nombre_especimen;
        this.activo = activo;
    }

    public Especimen(Integer id_especimen, String num_inventario, Integer id_especie, Especie especie, String nombre_especimen, boolean activo) {
        this.id_especimen = id_especimen;
        this.num_inventario = num_inventario;
        this.id_especie = id_especie;
        this.especie = especie;
        this.nombre_especimen = nombre_especimen;
        this.activo = activo;
    }

    //Getters
    public Integer getId_especimen() {
        return id_especimen;
    }

    public String getNum_inventario() {
        return num_inventario;
    }

    public Integer getId_especie() {
        return id_especie;
    }

    public Especie getEspecie() {
        return especie;
    }

    public String getNombre_especimen() {
        return nombre_especimen;
    }

    public boolean isActivo() {
        return activo;
    }

    //Setters
    public void setId_especimen(Integer id_especimen) {
        this.id_especimen = id_especimen;
    }

    public void setNum_inventario(String num_inventario) {
        this.num_inventario = num_inventario;
    }

    public void setId_especie(Integer id_especie) {
        this.id_especie = id_especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public void setNombre_especimen(String nombre_especimen) {
        this.nombre_especimen = nombre_especimen;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Especimen especimen = (Especimen) o;
        return id_especimen == especimen.id_especimen && num_inventario == especimen.num_inventario && id_especie == especimen.id_especie && activo == especimen.activo && Objects.equals(especie, especimen.especie) && Objects.equals(nombre_especimen, especimen.nombre_especimen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_especimen, num_inventario, id_especie, especie, nombre_especimen, activo);
    }

    public boolean isValid(){
        return num_inventario != null && id_especie != null && nombre_especimen != null && !nombre_especimen.trim().isEmpty();
    }
}