package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class OrigenAlta {
    //Atributos, usando JsonProperty para indicar el formato de la llaves del .json
    @JsonProperty
    private Integer id_origen_alta;

    @JsonProperty
    private String nombre_origen_alta;


    public OrigenAlta() {}

    public OrigenAlta(Integer id_origen_alta, String nombre_origen_alta) {
        this.id_origen_alta = id_origen_alta;
        this.nombre_origen_alta = nombre_origen_alta;
    }

    public Integer getId_origen_alta() {
        return id_origen_alta;
    }

    public String getNombre_origen_alta() {
        return nombre_origen_alta;
    }

    public void setId_origen_alta(Integer id_origen_alta) {
        this.id_origen_alta = id_origen_alta;
    }

    public void setNombre_origen_alta(String nombre_origen_alta) {
        this.nombre_origen_alta = nombre_origen_alta;
    }
}
