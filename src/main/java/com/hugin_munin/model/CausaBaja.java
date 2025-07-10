package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo para la entidad CausaBaja
 * Representa las diferentes causas por las que un especimen puede ser dado de baja
 */
public class CausaBaja {

    @JsonProperty("id_causa_baja")
    private Integer id_causa_baja;

    @JsonProperty("nombre_causa_baja")
    private String nombre_causa_baja;

    // Constructores
    public CausaBaja() {
    }

    public CausaBaja(String nombre_causa_baja) {
        this.nombre_causa_baja = nombre_causa_baja;
    }

    public CausaBaja(Integer id_causa_baja, String nombre_causa_baja) {
        this.id_causa_baja = id_causa_baja;
        this.nombre_causa_baja = nombre_causa_baja;
    }

    // Getters
    public Integer getId_causa_baja() {
        return id_causa_baja;
    }

    public String getNombre_causa_baja() {
        return nombre_causa_baja;
    }

    // Setters
    public void setId_causa_baja(Integer id_causa_baja) {
        this.id_causa_baja = id_causa_baja;
    }

    public void setNombre_causa_baja(String nombre_causa_baja) {
        this.nombre_causa_baja = nombre_causa_baja;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CausaBaja that = (CausaBaja) o;
        return Objects.equals(id_causa_baja, that.id_causa_baja);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_causa_baja);
    }

    /**
     * Validar si la causa de baja tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_causa_baja != null && !nombre_causa_baja.trim().isEmpty();
    }

    /**
     * Obtener información para mostrar
     */
    public String getDisplayInfo() {
        return nombre_causa_baja != null ? nombre_causa_baja : "Sin nombre";
    }

    @Override
    public String toString() {
        return String.format("CausaBaja{id=%d, nombre='%s'}",
                id_causa_baja, nombre_causa_baja);
    }
}