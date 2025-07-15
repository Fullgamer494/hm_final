package com.hugin_munin.service;

import com.hugin_munin.model.Permiso;
import com.hugin_munin.repository.PermisoRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar permisos
 * Contiene la lógica de negocio para permisos
 */
public class PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoService(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    /**
     * OBTENER todos los permisos
     */
    public List<Permiso> getAllPermisos() throws SQLException {
        return permisoRepository.findAll();
    }

    /**
     * OBTENER permisos agrupados por categoría
     */
    public Map<String, List<Permiso>> getPermisosByCategory() throws SQLException {
        List<Permiso> permisos = permisoRepository.findAll();

        return permisos.stream()
                .collect(Collectors.groupingBy(Permiso::getCategory));
    }

    /**
     * OBTENER permiso por ID
     */
    public Permiso getPermisoById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<Permiso> permiso = permisoRepository.findById(id);
        return permiso.orElseThrow(() ->
                new IllegalArgumentException("Permiso no encontrado con ID: " + id));
    }

    /**
     * BUSCAR permisos por nombre
     */
    public List<Permiso> searchPermisosByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return permisoRepository.findByNameContaining(nombre.trim());
    }

    /**
     * BUSCAR permisos por categoría
     */
    public List<Permiso> getPermisosByCategory(String categoria) throws SQLException {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría no puede estar vacía");
        }

        return permisoRepository.findByCategory(categoria.trim());
    }

    /**
     * OBTENER permisos asignados a un rol
     */
    public List<Permiso> getPermisosByRol(Integer idRol) throws SQLException {
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        return permisoRepository.findByRol(idRol);
    }

    /**
     * OBTENER permisos NO asignados a un rol
     */
    public List<Permiso> getPermisosNotAssignedToRol(Integer idRol) throws SQLException {
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        return permisoRepository.findNotAssignedToRol(idRol);
    }

    /**
     * CREAR nuevo permiso
     */
    public Permiso createPermiso(Permiso permiso) throws SQLException {
        // Validaciones básicas
        validatePermisoData(permiso);

        // Validar que el nombre no esté en uso
        if (permisoRepository.existsByName(permiso.getNombre_permiso())) {
            throw new IllegalArgumentException("Ya existe un permiso con este nombre");
        }

        // Normalizar nombre
        permiso.setNombre_permiso(normalizePermisoName(permiso.getNombre_permiso()));

        // Guardar permiso
        return permisoRepository.save(permiso);
    }

    /**
     * ACTUALIZAR permiso existente
     */
    public Permiso updatePermiso(Permiso permiso) throws SQLException {
        if (permiso.getId_permiso() == null || permiso.getId_permiso() <= 0) {
            throw new IllegalArgumentException("ID del permiso requerido para actualización");
        }

        // Verificar que el permiso existe
        Optional<Permiso> existingPermiso = permisoRepository.findById(permiso.getId_permiso());
        if (existingPermiso.isEmpty()) {
            throw new IllegalArgumentException("Permiso no encontrado con ID: " + permiso.getId_permiso());
        }

        // Validaciones básicas
        validatePermisoData(permiso);

        // Validar que el nombre no esté en uso por otro permiso
        Optional<Permiso> permisoWithName = permisoRepository.findByName(permiso.getNombre_permiso());
        if (permisoWithName.isPresent() && !permisoWithName.get().getId_permiso().equals(permiso.getId_permiso())) {
            throw new IllegalArgumentException("El nombre ya está en uso por otro permiso");
        }

        // Normalizar datos
        permiso.setNombre_permiso(normalizePermisoName(permiso.getNombre_permiso()));

        // Actualizar permiso
        boolean updated = permisoRepository.update(permiso);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el permiso");
        }

        return permiso;
    }

    /**
     * ELIMINAR permiso
     */
    public boolean deletePermiso(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el permiso existe
        if (!permisoRepository.existsById(id)) {
            throw new IllegalArgumentException("Permiso no encontrado con ID: " + id);
        }

        // Verificar que el permiso no esté siendo usado
        if (permisoRepository.isPermisoInUse(id)) {
            throw new IllegalArgumentException("No se puede eliminar el permiso porque está asignado a uno o más roles");
        }

        // Verificar que no sea un permiso crítico
        Optional<Permiso> permiso = permisoRepository.findById(id);
        if (permiso.isPresent() && permiso.get().isCritical()) {
            throw new IllegalArgumentException("No se puede eliminar un permiso crítico del sistema");
        }

        // Eliminar permiso
        return permisoRepository.deleteById(id);
    }

    /**
     * ASIGNAR permiso a rol
     */
    public boolean assignPermisoToRol(Integer idPermiso, Integer idRol) throws SQLException {
        if (idPermiso == null || idPermiso <= 0) {
            throw new IllegalArgumentException("ID de permiso inválido");
        }
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        // Verificar que el permiso existe
        if (!permisoRepository.existsById(idPermiso)) {
            throw new IllegalArgumentException("El permiso con ID " + idPermiso + " no existe");
        }

        // Verificar que no esté ya asignado
        if (permisoRepository.rolHasPermiso(idRol, idPermiso)) {
            throw new IllegalArgumentException("El permiso ya está asignado a este rol");
        }

        return permisoRepository.assignPermisoToRol(idPermiso, idRol);
    }

    /**
     * REMOVER permiso de rol
     */
    public boolean removePermisoFromRol(Integer idPermiso, Integer idRol) throws SQLException {
        if (idPermiso == null || idPermiso <= 0) {
            throw new IllegalArgumentException("ID de permiso inválido");
        }
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        // Verificar que el permiso está asignado al rol
        if (!permisoRepository.rolHasPermiso(idRol, idPermiso)) {
            throw new IllegalArgumentException("El permiso no está asignado a este rol");
        }

        return permisoRepository.removePermisoFromRol(idPermiso, idRol);
    }

    /**
     * ASIGNAR múltiples permisos a un rol
     */
    public Map<String, Object> assignMultiplePermisosToRol(List<Integer> idsPermisos, Integer idRol) throws SQLException {
        if (idsPermisos == null || idsPermisos.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos un permiso");
        }
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        int exitosos = 0;
        int fallos = 0;
        List<String> errores = new ArrayList<>();

        for (Integer idPermiso : idsPermisos) {
            try {
                assignPermisoToRol(idPermiso, idRol);
                exitosos++;
            } catch (Exception e) {
                fallos++;
                errores.add("Permiso ID " + idPermiso + ": " + e.getMessage());
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("total", idsPermisos.size());
        resultado.put("exitosos", exitosos);
        resultado.put("fallos", fallos);
        resultado.put("errores", errores);

        return resultado;
    }

    /**
     * VERIFICAR si un rol tiene un permiso específico
     */
    public boolean rolHasPermiso(Integer idRol, Integer idPermiso) throws SQLException {
        if (idRol == null || idRol <= 0 || idPermiso == null || idPermiso <= 0) {
            return false;
        }

        return permisoRepository.rolHasPermiso(idRol, idPermiso);
    }

    /**
     * VERIFICAR si un rol tiene un permiso específico por nombre
     */
    public boolean rolHasPermisoByName(Integer idRol, String nombrePermiso) throws SQLException {
        if (idRol == null || idRol <= 0 || nombrePermiso == null || nombrePermiso.trim().isEmpty()) {
            return false;
        }

        Optional<Permiso> permiso = permisoRepository.findByName(nombrePermiso.trim());
        if (permiso.isEmpty()) {
            return false;
        }

        return permisoRepository.rolHasPermiso(idRol, permiso.get().getId_permiso());
    }

    /**
     * OBTENER estadísticas de uso de permisos
     */
    public List<PermisoRepository.PermisoEstadistica> getEstadisticasUso() throws SQLException {
        return permisoRepository.getEstadisticasUso();
    }

    /**
     * OBTENER estadísticas generales del sistema de permisos
     */
    public Map<String, Object> getEstadisticasGenerales() throws SQLException {
        Map<String, Object> estadisticas = new HashMap<>();

        // Contadores básicos
        estadisticas.put("total_permisos", permisoRepository.countTotal());

        // Permisos por categoría
        Map<String, List<Permiso>> permisosPorCategoria = getPermisosByCategory();
        Map<String, Integer> conteosPorCategoria = new HashMap<>();
        for (Map.Entry<String, List<Permiso>> entry : permisosPorCategoria.entrySet()) {
            conteosPorCategoria.put(entry.getKey(), entry.getValue().size());
        }
        estadisticas.put("permisos_por_categoria", conteosPorCategoria);

        // Permisos críticos
        List<Permiso> todosPermisos = permisoRepository.findAll();
        long permisosCriticos = todosPermisos.stream()
                .filter(Permiso::isCritical)
                .count();
        estadisticas.put("permisos_criticos", permisosCriticos);

        // Tipos de permisos
        long permisosLectura = todosPermisos.stream().filter(Permiso::isReadPermission).count();
        long permisosEscritura = todosPermisos.stream().filter(Permiso::isWritePermission).count();
        long permisosEdicion = todosPermisos.stream().filter(Permiso::isEditPermission).count();
        long permisosEliminacion = todosPermisos.stream().filter(Permiso::isDeletePermission).count();

        Map<String, Long> tiposPermisos = new HashMap<>();
        tiposPermisos.put("lectura", permisosLectura);
        tiposPermisos.put("escritura", permisosEscritura);
        tiposPermisos.put("edicion", permisosEdicion);
        tiposPermisos.put("eliminacion", permisosEliminacion);
        estadisticas.put("tipos_permisos", tiposPermisos);

        return estadisticas;
    }

    /**
     * SINCRONIZAR permisos de un rol (reemplazar completamente)
     */
    public Map<String, Object> syncPermisosToRol(List<Integer> idsPermisos, Integer idRol) throws SQLException {
        if (idRol == null || idRol <= 0) {
            throw new IllegalArgumentException("ID de rol inválido");
        }

        // Obtener permisos actuales del rol
        List<Permiso> permisosActuales = permisoRepository.findByRol(idRol);

        int removidos = 0;
        int agregados = 0;
        List<String> errores = new ArrayList<>();

        // Remover permisos que ya no están en la nueva lista
        for (Permiso permisoActual : permisosActuales) {
            if (idsPermisos == null || !idsPermisos.contains(permisoActual.getId_permiso())) {
                try {
                    removePermisoFromRol(permisoActual.getId_permiso(), idRol);
                    removidos++;
                } catch (Exception e) {
                    errores.add("Error al remover permiso " + permisoActual.getId_permiso() + ": " + e.getMessage());
                }
            }
        }

        // Agregar nuevos permisos
        if (idsPermisos != null) {
            for (Integer idPermiso : idsPermisos) {
                try {
                    if (!rolHasPermiso(idRol, idPermiso)) {
                        assignPermisoToRol(idPermiso, idRol);
                        agregados++;
                    }
                } catch (Exception e) {
                    errores.add("Error al agregar permiso " + idPermiso + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permisos_removidos", removidos);
        resultado.put("permisos_agregados", agregados);
        resultado.put("errores", errores);
        resultado.put("success", errores.isEmpty());

        return resultado;
    }

    /**
     * Validar datos del permiso
     */
    private void validatePermisoData(Permiso permiso) {
        if (permiso == null) {
            throw new IllegalArgumentException("El permiso no puede ser nulo");
        }

        if (permiso.getNombre_permiso() == null || permiso.getNombre_permiso().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del permiso es requerido");
        }

        if (permiso.getNombre_permiso().length() < 3) {
            throw new IllegalArgumentException("El nombre del permiso debe tener al menos 3 caracteres");
        }

        if (permiso.getNombre_permiso().length() > 100) {
            throw new IllegalArgumentException("El nombre del permiso no puede exceder 100 caracteres");
        }

        // Validar caracteres permitidos (letras, números, guiones bajos y espacios)
        if (!permiso.getNombre_permiso().matches("^[a-zA-Z0-9_\\s]+$")) {
            throw new IllegalArgumentException("El nombre del permiso solo puede contener letras, números, guiones bajos y espacios");
        }
    }

    /**
     * Normalizar nombre del permiso
     */
    private String normalizePermisoName(String nombre) {
        if (nombre == null) return null;

        return nombre.trim()
                .toLowerCase()
                .replaceAll("\\s+", "_"); // Reemplazar espacios múltiples con un guión bajo
    }
}