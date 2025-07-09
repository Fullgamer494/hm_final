package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Modelo Usuario simplificado
 * Se enfoca en campos esenciales para evitar problemas
 */
public class Usuario {

    @JsonProperty("id_usuario")
    private Integer id_usuario;

    @JsonProperty("id_rol")
    private Integer id_rol;

    @JsonProperty("rol")
    private Rol rol;  // Para joins opcionales

    @JsonProperty("nombre_usuario")
    private String nombre_usuario;

    @JsonProperty("correo")
    private String correo;

    @JsonIgnore  // No incluir en JSON por seguridad
    private String contrasena;

    @JsonProperty("activo")
    private boolean activo;

    // Constructores
    public Usuario() {
        this.activo = true; // Por defecto activo
    }

    public Usuario(Integer id_rol, String nombre_usuario, String correo, String contrasena) {
        this.id_rol = id_rol;
        this.nombre_usuario = nombre_usuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.activo = true;
    }

    public Usuario(Integer id_usuario, Integer id_rol, String nombre_usuario,
                   String correo, String contrasena, boolean activo) {
        this.id_usuario = id_usuario;
        this.id_rol = id_rol;
        this.nombre_usuario = nombre_usuario;
        this.correo = correo;
        this.contrasena = contrasena;
        this.activo = activo;
    }

    // Getters
    public Integer getId_usuario() {
        return id_usuario;
    }

    public Integer getId_rol() {
        return id_rol;
    }

    public Rol getRol() {
        return rol;
    }

    public String getNombre_usuario() {
        return nombre_usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public boolean isActivo() {
        return activo;
    }

    // Setters
    public void setId_usuario(Integer id_usuario) {
        this.id_usuario = id_usuario;
    }

    public void setId_rol(Integer id_rol) {
        this.id_rol = id_rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public void setNombre_usuario(String nombre_usuario) {
        this.nombre_usuario = nombre_usuario;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id_usuario, usuario.id_usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_usuario);
    }

    /**
     * Validar si el usuario tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return nombre_usuario != null && !nombre_usuario.trim().isEmpty() &&
                correo != null && !correo.trim().isEmpty() &&
                id_rol != null && id_rol > 0;
    }

    /**
     * Validar formato básico de email
     */
    public boolean hasValidEmail() {
        if (correo == null) return false;
        return correo.contains("@") && correo.contains(".") && correo.length() > 5;
    }

    /**
     * Información para mostrar con rol (si está disponible)
     */
    public String getDisplayInfo() {
        if (rol != null) {
            return String.format("%s (%s)", nombre_usuario, rol.getNombre_rol());
        }
        return nombre_usuario;
    }

    @Override
    public String toString() {
        return String.format("Usuario{id=%d, nombre='%s', correo='%s', activo=%s}",
                id_usuario, nombre_usuario, correo, activo);
    }
}