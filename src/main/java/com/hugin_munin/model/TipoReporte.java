package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo para la entidad TipoReporte (Catálogo) - CORREGIDO
 * Representa los diferentes tipos de reportes disponibles en el sistema
 * ELIMINADO: campo 'activo' (no existe en BD)
 */
public class TipoReporte {

    @JsonProperty("id_tipo_reporte")
    private Integer id_tipo_reporte;

    @JsonProperty("nombre_tipo_reporte")
    private String nombre_tipo_reporte;

    // Constructores
    public TipoReporte() {
    }

    public TipoReporte(String nombre_tipo_reporte, String descripcion) {
        this.nombre_tipo_reporte = nombre_tipo_reporte;
    }

    public TipoReporte(Integer id_tipo_reporte, String nombre_tipo_reporte, String descripcion) {
        this.id_tipo_reporte = id_tipo_reporte;
        this.nombre_tipo_reporte = nombre_tipo_reporte;
    }

    // Getters
    public Integer getId_tipo_reporte() {
        return id_tipo_reporte;
    }

    public String getNombre_tipo_reporte() {
        return nombre_tipo_reporte;
    }

    // Setters
    public void setId_tipo_reporte(Integer id_tipo_reporte) {
        this.id_tipo_reporte = id_tipo_reporte;
    }

    public void setNombre_tipo_reporte(String nombre_tipo_reporte) {
        this.nombre_tipo_reporte = nombre_tipo_reporte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoReporte that = (TipoReporte) o;
        return Objects.equals(id_tipo_reporte, that.id_tipo_reporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_tipo_reporte);
    }

    /**
     * Validar si el tipo de reporte tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_tipo_reporte != null && !nombre_tipo_reporte.trim().isEmpty();
    }

    /**
     * Información para mostrar
     */
    public String getDisplayInfo() {
        return nombre_tipo_reporte != null ? nombre_tipo_reporte : "Sin nombre";
    }

    @Override
    public String toString() {
        return String.format("TipoReporte{id=%d, nombre='%s'}",
                id_tipo_reporte, nombre_tipo_reporte);
    }
}