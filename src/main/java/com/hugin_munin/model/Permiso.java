package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo para la entidad Permiso
 * Representa los permisos disponibles en el sistema
 */
public class Permiso {

    @JsonProperty("id_permiso")
    private Integer id_permiso;

    @JsonProperty("nombre_permiso")
    private String nombre_permiso;

    // Constructores
    public Permiso() {
    }

    public Permiso(String nombre_permiso) {
        this.nombre_permiso = nombre_permiso;
    }

    public Permiso(Integer id_permiso, String nombre_permiso) {
        this.id_permiso = id_permiso;
        this.nombre_permiso = nombre_permiso;
    }

    // Getters
    public Integer getId_permiso() {
        return id_permiso;
    }

    public String getNombre_permiso() {
        return nombre_permiso;
    }

    // Setters
    public void setId_permiso(Integer id_permiso) {
        this.id_permiso = id_permiso;
    }

    public void setNombre_permiso(String nombre_permiso) {
        this.nombre_permiso = nombre_permiso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permiso permiso = (Permiso) o;
        return Objects.equals(id_permiso, permiso.id_permiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_permiso);
    }

    /**
     * Validar si el permiso tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_permiso != null && !nombre_permiso.trim().isEmpty();
    }

    /**
     * Obtener información para mostrar
     */
    public String getDisplayInfo() {
        return nombre_permiso != null ? nombre_permiso : "Sin nombre";
    }

    /**
     * Verificar si es un permiso crítico del sistema
     */
    public boolean isCritical() {
        if (nombre_permiso == null) return false;
        String nombre = nombre_permiso.toLowerCase();
        return nombre.contains("admin") || nombre.contains("eliminar") ||
                nombre.contains("delete") || nombre.contains("sistema");
    }

    /**
     * Obtener categoría del permiso basada en el nombre
     */
    public String getCategory() {
        if (nombre_permiso == null) return "general";

        String nombre = nombre_permiso.toLowerCase();

        if (nombre.contains("alta")) return "altas";
        if (nombre.contains("baja")) return "bajas";
        if (nombre.contains("reporte_clinico")) return "reportes_clinicos";
        if (nombre.contains("reporte_conductual")) return "reportes_conductuales";
        if (nombre.contains("reporte_alimenticio")) return "reportes_alimenticios";
        if (nombre.contains("reporte_defuncion")) return "reportes_defuncion";
        if (nombre.contains("reporte_traslado")) return "reportes_traslado";
        if (nombre.contains("reporte")) return "reportes_general";

        return "general";
    }

    /**
     * Verificar si es un permiso de lectura
     */
    public boolean isReadPermission() {
        if (nombre_permiso == null) return false;
        String nombre = nombre_permiso.toLowerCase();
        return nombre.contains("ver") || nombre.contains("leer") || nombre.contains("read");
    }

    /**
     * Verificar si es un permiso de escritura
     */
    public boolean isWritePermission() {
        if (nombre_permiso == null) return false;
        String nombre = nombre_permiso.toLowerCase();
        return nombre.contains("registrar") || nombre.contains("crear") ||
                nombre.contains("generar") || nombre.contains("write");
    }

    /**
     * Verificar si es un permiso de edición
     */
    public boolean isEditPermission() {
        if (nombre_permiso == null) return false;
        String nombre = nombre_permiso.toLowerCase();
        return nombre.contains("editar") || nombre.contains("actualizar") || nombre.contains("edit");
    }

    /**
     * Verificar si es un permiso de eliminación
     */
    public boolean isDeletePermission() {
        if (nombre_permiso == null) return false;
        String nombre = nombre_permiso.toLowerCase();
        return nombre.contains("eliminar") || nombre.contains("delete");
    }

    @Override
    public String toString() {
        return String.format("Permiso{id=%d, nombre='%s', categoria='%s'}",
                id_permiso, nombre_permiso, getCategory());
    }
}