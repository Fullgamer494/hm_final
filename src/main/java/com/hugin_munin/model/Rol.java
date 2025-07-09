package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo para la entidad Rol
 * Representa los diferentes roles que pueden tener los usuarios en el sistema
 */
public class Rol {

    @JsonProperty("id_rol")
    private Integer id_rol;

    @JsonProperty("nombre_rol")
    private String nombre_rol;

    @JsonProperty("descripcion")
    private String descripcion;

    @JsonProperty("activo")
    private boolean activo;

    // Constructores
    public Rol() {
        this.activo = true; // Por defecto activo
    }

    public Rol(String nombre_rol, String descripcion) {
        this.nombre_rol = nombre_rol;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public Rol(Integer id_rol, String nombre_rol, String descripcion, boolean activo) {
        this.id_rol = id_rol;
        this.nombre_rol = nombre_rol;
        this.descripcion = descripcion;
        this.activo = activo;
    }

    // Getters
    public Integer getId_rol() {
        return id_rol;
    }

    public String getNombre_rol() {
        return nombre_rol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isActivo() {
        return activo;
    }

    // Setters
    public void setId_rol(Integer id_rol) {
        this.id_rol = id_rol;
    }

    public void setNombre_rol(String nombre_rol) {
        this.nombre_rol = nombre_rol;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(id_rol, rol.id_rol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_rol);
    }

    /**
     * Validar si el rol tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_rol != null && !nombre_rol.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Rol{id=%d, nombre='%s', activo=%s}",
                id_rol, nombre_rol, activo);
    }
}