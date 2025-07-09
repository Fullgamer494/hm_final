package com.hugin_munin.service;

import com.hugin_munin.model.Rol;
import com.hugin_munin.repository.RolRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio para gestionar roles
 * Contiene la lógica de negocio para roles
 */
public class RolService {

    private final RolRepository rolRepository;

    public RolService(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    /**
     * OBTENER todos los roles
     */
    public List<Rol> getAllRoles() throws SQLException {
        return rolRepository.findAll();
    }

    /**
     * OBTENER roles activos
     */
    public List<Rol> getActiveRoles() throws SQLException {
        return rolRepository.findAllActive();
    }

    /**
     * OBTENER rol por ID
     */
    public Rol getRolById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<Rol> rol = rolRepository.findById(id);
        return rol.orElseThrow(() ->
                new IllegalArgumentException("Rol no encontrado con ID: " + id));
    }

    /**
     * BUSCAR roles por nombre
     */
    public List<Rol> searchRolesByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return rolRepository.findByNameContaining(nombre.trim());
    }

    /**
     * CREAR nuevo rol
     */
    public Rol createRol(Rol rol) throws SQLException {
        // Validaciones básicas
        validateRolData(rol);

        // Validar que el nombre no esté en uso
        if (rolRepository.existsByName(rol.getNombre_rol())) {
            throw new IllegalArgumentException("Ya existe un rol con este nombre");
        }

        // Normalizar nombre
        rol.setNombre_rol(capitalizeFirstLetter(rol.getNombre_rol().trim()));

        if (rol.getDescripcion() != null) {
            rol.setDescripcion(rol.getDescripcion().trim());
        }

        // Guardar rol
        return rolRepository.save(rol);
    }

    /**
     * ACTUALIZAR rol existente
     */
    public Rol updateRol(Rol rol) throws SQLException {
        if (rol.getId_rol() == null || rol.getId_rol() <= 0) {
            throw new IllegalArgumentException("ID del rol requerido para actualización");
        }

        // Verificar que el rol existe
        Optional<Rol> existingRol = rolRepository.findById(rol.getId_rol());
        if (existingRol.isEmpty()) {
            throw new IllegalArgumentException("Rol no encontrado con ID: " + rol.getId_rol());
        }

        // Validaciones básicas
        validateRolData(rol);

        // Validar que el nombre no esté en uso por otro rol
        Optional<Rol> rolWithName = rolRepository.findByName(rol.getNombre_rol());
        if (rolWithName.isPresent() && !rolWithName.get().getId_rol().equals(rol.getId_rol())) {
            throw new IllegalArgumentException("El nombre ya está en uso por otro rol");
        }

        // Normalizar datos
        rol.setNombre_rol(capitalizeFirstLetter(rol.getNombre_rol().trim()));

        if (rol.getDescripcion() != null) {
            rol.setDescripcion(rol.getDescripcion().trim());
        }

        // Actualizar rol
        boolean updated = rolRepository.update(rol);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el rol");
        }

        return rol;
    }

    /**
     * ELIMINAR rol
     */
    public boolean deleteRol(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el rol existe
        if (!rolRepository.existsById(id)) {
            throw new IllegalArgumentException("Rol no encontrado con ID: " + id);
        }

        // Verificar que el rol no esté siendo usado por usuarios
        if (rolRepository.isRolInUse(id)) {
            // En lugar de eliminar, desactivar
            return rolRepository.deactivateById(id);
        }

        // Eliminación física si no está en uso
        return rolRepository.deleteById(id);
    }

    /**
     * ACTIVAR rol
     */
    public boolean activateRol(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        if (!rolRepository.existsById(id)) {
            throw new IllegalArgumentException("Rol no encontrado con ID: " + id);
        }

        return rolRepository.activateById(id);
    }

    /**
     * DESACTIVAR rol
     */
    public boolean deactivateRol(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        if (!rolRepository.existsById(id)) {
            throw new IllegalArgumentException("Rol no encontrado con ID: " + id);
        }

        // Verificar que no sea un rol crítico del sistema
        Optional<Rol> rol = rolRepository.findById(id);
        if (rol.isPresent() && isCriticalRole(rol.get().getNombre_rol())) {
            throw new IllegalArgumentException("No se puede desactivar un rol crítico del sistema");
        }

        return rolRepository.deactivateById(id);
    }

    /**
     * VERIFICAR si un nombre de rol está disponible
     */
    public boolean isRoleNameAvailable(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return !rolRepository.existsByName(nombre.trim());
    }

    /**
     * OBTENER estadísticas de roles
     */
    public Map<String, Object> getRoleStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_roles", rolRepository.countTotal());
        stats.put("roles_activos", rolRepository.countActive());

        return stats;
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos del rol
     */
    private void validateRolData(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }

        if (!rol.isValid()) {
            throw new IllegalArgumentException("Los datos del rol no son válidos");
        }

        // Validar longitudes
        if (rol.getNombre_rol().length() < 2 || rol.getNombre_rol().length() > 50) {
            throw new IllegalArgumentException("El nombre del rol debe tener entre 2 y 50 caracteres");
        }

        if (rol.getDescripcion() != null && rol.getDescripcion().length() > 200) {
            throw new IllegalArgumentException("La descripción no puede exceder 200 caracteres");
        }

        // Validar caracteres permitidos
        if (!rol.getNombre_rol().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            throw new IllegalArgumentException("El nombre del rol solo puede contener letras y espacios");
        }
    }

    /**
     * Verificar si es un rol crítico del sistema
     */
    private boolean isCriticalRole(String nombreRol) {
        String[] criticalRoles = {"ADMINISTRADOR", "ADMIN", "SUPERUSER", "ROOT"};
        String upperName = nombreRol.toUpperCase();

        for (String critical : criticalRoles) {
            if (upperName.contains(critical)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Capitalizar primera letra
     */
    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}