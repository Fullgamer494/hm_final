package com.hugin_munin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Modelo para usuario con permisos - CORREGIDO
 * Clase independiente para mejor mantenimiento
 * CORREGIDO: Maneja roles sin descripción
 */
public class UsuarioConPermisos {
    private Usuario usuario;
    private Rol rol;
    private List<Permiso> permisos;

    public UsuarioConPermisos(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.permisos = new ArrayList<>();
    }

    // Getters y setters
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public List<Permiso> getPermisos() { return permisos; }
    public void setPermisos(List<Permiso> permisos) { this.permisos = permisos; }

    /**
     * Convertir a Map para respuesta JSON - MÉTODO CORREGIDO
     */
    public Map<String, Object> toResponseMap() {
        Map<String, Object> response = new HashMap<>();

        // Información del usuario
        Map<String, Object> usuarioInfo = new HashMap<>();
        usuarioInfo.put("id_usuario", usuario.getId_usuario());
        usuarioInfo.put("nombre_usuario", usuario.getNombre_usuario());
        usuarioInfo.put("correo", usuario.getCorreo());
        usuarioInfo.put("activo", usuario.isActivo());
        response.put("usuario", usuarioInfo);

        // Información del rol - CORREGIDO para manejar descripción nula
        Map<String, Object> rolInfo = new HashMap<>();
        rolInfo.put("id_rol", rol.getId_rol());
        rolInfo.put("nombre_rol", rol.getNombre_rol());

        // CORREGIDO: Solo agregar descripción si existe y no es nula
        String descripcion = rol.getDescripcion();
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            rolInfo.put("descripcion", descripcion);
        } else {
            // Generar descripción automática basada en el nombre del rol
            rolInfo.put("descripcion", "Rol " + rol.getNombre_rol());
        }

        response.put("rol", rolInfo);

        // Lista de permisos
        List<Map<String, Object>> permisosInfo = new ArrayList<>();
        for (Permiso permiso : permisos) {
            Map<String, Object> permisoInfo = new HashMap<>();
            permisoInfo.put("id_permiso", permiso.getId_permiso());
            permisoInfo.put("nombre_permiso", permiso.getNombre_permiso());

            // Categorizar basado en el nombre del permiso
            String categoria = determinarCategoria(permiso.getNombre_permiso());
            permisoInfo.put("categoria", categoria);

            permisosInfo.add(permisoInfo);
        }
        response.put("permisos", permisosInfo);

        // Estadísticas
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("total_permisos", permisos.size());

        // Contar por categorías
        Map<String, Integer> permisosPorCategoria = new HashMap<>();
        for (Permiso permiso : permisos) {
            String categoria = determinarCategoria(permiso.getNombre_permiso());
            permisosPorCategoria.put(categoria,
                    permisosPorCategoria.getOrDefault(categoria, 0) + 1);
        }
        estadisticas.put("permisos_por_categoria", permisosPorCategoria);

        response.put("estadisticas", estadisticas);

        return response;
    }

    /**
     * Determinar categoría basada en el nombre del permiso
     */
    private String determinarCategoria(String nombrePermiso) {
        if (nombrePermiso == null || nombrePermiso.trim().isEmpty()) {
            return "general";
        }

        String nombre = nombrePermiso.toLowerCase().trim();

        // Categorías específicas (orden importante - más específico primero)
        if (nombre.contains("reporte_traslado") || nombre.contains("reportes_traslado")) {
            return "reportes_traslado";
        }
        if (nombre.contains("reporte_defuncion") || nombre.contains("reportes_defuncion")) {
            return "reportes_defuncion";
        }
        if (nombre.contains("reporte_clinico") || nombre.contains("reportes_clinicos")) {
            return "reportes_clinicos";
        }
        if (nombre.contains("reporte_conductual") || nombre.contains("reportes_conductuales")) {
            return "reportes_conductuales";
        }
        if (nombre.contains("reporte_alimenticio") || nombre.contains("reportes_alimenticios")) {
            return "reportes_alimenticios";
        }
        if (nombre.contains("reporte") || nombre.contains("reportes")) {
            return "reportes";
        }
        if (nombre.contains("especie") || nombre.contains("especies")) {
            return "especies";
        }
        if (nombre.contains("especimen") || nombre.contains("especimenes")) {
            return "especimenes";
        }
        if (nombre.contains("usuario") || nombre.contains("usuarios")) {
            return "usuarios";
        }
        if (nombre.contains("rol") || nombre.contains("roles")) {
            return "roles";
        }
        if (nombre.contains("alta") || nombre.contains("altas")) {
            return "altas";
        }
        if (nombre.contains("baja") || nombre.contains("bajas")) {
            return "bajas";
        }
        if (nombre.contains("admin") || nombre.contains("administra")) {
            return "administracion";
        }
        if (nombre.contains("sistema") || nombre.contains("config")) {
            return "sistema";
        }

        return "general";
    }

    /**
     * Verificar si el usuario tiene un permiso específico
     */
    public boolean tienePermiso(String nombrePermiso) {
        if (nombrePermiso == null || permisos == null) {
            return false;
        }
        return permisos.stream()
                .anyMatch(p -> nombrePermiso.equals(p.getNombre_permiso()));
    }

    /**
     * Obtener nombres de permisos
     */
    public List<String> getNombresPermisos() {
        if (permisos == null) {
            return new ArrayList<>();
        }
        List<String> nombres = new ArrayList<>();
        for (Permiso permiso : permisos) {
            if (permiso.getNombre_permiso() != null) {
                nombres.add(permiso.getNombre_permiso());
            }
        }
        return nombres;
    }

    /**
     * Información resumida
     */
    public String getResumen() {
        return String.format("Usuario: %s (%s) - Rol: %s - Permisos: %d",
                usuario.getNombre_usuario(),
                usuario.getCorreo(),
                rol.getNombre_rol(),
                permisos.size());
    }
}