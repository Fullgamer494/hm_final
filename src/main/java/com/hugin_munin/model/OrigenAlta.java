package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo para la entidad OrigenAlta
 * Representa los diferentes orígenes de donde provienen los especímenes
 */
public class OrigenAlta {

    @JsonProperty("id_origen_alta")
    private Integer id_origen_alta;

    @JsonProperty("nombre_origen_alta")
    private String nombre_origen_alta;

    // Constructores
    public OrigenAlta() {
    }

    public OrigenAlta(String nombre_origen_alta) {
        this.nombre_origen_alta = nombre_origen_alta;
    }

    public OrigenAlta(Integer id_origen_alta, String nombre_origen_alta) {
        this.id_origen_alta = id_origen_alta;
        this.nombre_origen_alta = nombre_origen_alta;
    }

    // Getters
    public Integer getId_origen_alta() {
        return id_origen_alta;
    }

    public String getNombre_origen_alta() {
        return nombre_origen_alta;
    }

    // Setters
    public void setId_origen_alta(Integer id_origen_alta) {
        this.id_origen_alta = id_origen_alta;
    }

    public void setNombre_origen_alta(String nombre_origen_alta) {
        this.nombre_origen_alta = nombre_origen_alta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrigenAlta that = (OrigenAlta) o;
        return Objects.equals(id_origen_alta, that.id_origen_alta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_origen_alta);
    }

    /**
     * Validar si el origen de alta tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_origen_alta != null && !nombre_origen_alta.trim().isEmpty();
    }

    /**
     * Obtener información para mostrar
     */
    public String getDisplayInfo() {
        return nombre_origen_alta != null ? nombre_origen_alta : "Sin nombre";
    }

    @Override
    public String toString() {
        return String.format("OrigenAlta{id=%d, nombre='%s'}",
                id_origen_alta, nombre_origen_alta);
    }
}