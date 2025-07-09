package com.hugin_munin.service;

import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.repository.OrigenAltaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio para gestionar orígenes de alta
 * Contiene la lógica de negocio para orígenes de alta
 */
public class OrigenAltaService {

    private final OrigenAltaRepository origenAltaRepository;

    public OrigenAltaService(OrigenAltaRepository origenAltaRepository) {
        this.origenAltaRepository = origenAltaRepository;
    }

    /**
     * OBTENER todos los orígenes de alta
     */
    public List<OrigenAlta> getAllOrigenes() throws SQLException {
        return origenAltaRepository.findAll();
    }

    /**
     * OBTENER origen de alta por ID
     */
    public OrigenAlta getOrigenById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<OrigenAlta> origen = origenAltaRepository.findById(id);
        return origen.orElseThrow(() ->
                new IllegalArgumentException("Origen de alta no encontrado con ID: " + id));
    }

    /**
     * BUSCAR orígenes por nombre
     */
    public List<OrigenAlta> searchOrigenesByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return origenAltaRepository.findByNameContaining(nombre.trim());
    }

    /**
     * CREAR nuevo origen de alta
     */
    public OrigenAlta createOrigen(OrigenAlta origen) throws SQLException {
        // Validaciones básicas
        validateOrigenData(origen);

        // Validar que el nombre no esté en uso
        if (origenAltaRepository.existsByName(origen.getNombre_origen_alta())) {
            throw new IllegalArgumentException("Ya existe un origen de alta con este nombre");
        }

        // Normalizar nombre
        origen.setNombre_origen_alta(capitalizeWords(origen.getNombre_origen_alta().trim()));

        // Guardar origen
        return origenAltaRepository.save(origen);
    }

    /**
     * ACTUALIZAR origen de alta existente
     */
    public OrigenAlta updateOrigen(OrigenAlta origen) throws SQLException {
        if (origen.getId_origen_alta() == null || origen.getId_origen_alta() <= 0) {
            throw new IllegalArgumentException("ID del origen requerido para actualización");
        }

        // Verificar que el origen existe
        Optional<OrigenAlta> existingOrigen = origenAltaRepository.findById(origen.getId_origen_alta());
        if (existingOrigen.isEmpty()) {
            throw new IllegalArgumentException("Origen de alta no encontrado con ID: " + origen.getId_origen_alta());
        }

        // Validaciones básicas
        validateOrigenData(origen);

        // Validar que el nombre no esté en uso por otro origen
        Optional<OrigenAlta> origenWithName = origenAltaRepository.findByName(origen.getNombre_origen_alta());
        if (origenWithName.isPresent() && !origenWithName.get().getId_origen_alta().equals(origen.getId_origen_alta())) {
            throw new IllegalArgumentException("El nombre ya está en uso por otro origen de alta");
        }

        // Normalizar datos
        origen.setNombre_origen_alta(capitalizeWords(origen.getNombre_origen_alta().trim()));

        // Actualizar origen
        boolean updated = origenAltaRepository.update(origen);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el origen de alta");
        }

        return origen;
    }

    /**
     * ELIMINAR origen de alta
     */
    public boolean deleteOrigen(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el origen existe
        if (!origenAltaRepository.existsById(id)) {
            throw new IllegalArgumentException("Origen de alta no encontrado con ID: " + id);
        }

        // Verificar que el origen no esté siendo usado en registros de alta
        if (origenAltaRepository.isOrigenInUse(id)) {
            throw new IllegalArgumentException("No se puede eliminar el origen porque está siendo usado en registros de alta");
        }

        // Eliminar origen
        return origenAltaRepository.deleteById(id);
    }

    /**
     * VERIFICAR si un nombre de origen está disponible
     */
    public boolean isOrigenNameAvailable(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return !origenAltaRepository.existsByName(nombre.trim());
    }

    /**
     * OBTENER estadísticas de orígenes de alta
     */
    public Map<String, Object> getOrigenStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_origenes", origenAltaRepository.countTotal());

        // Obtener estadísticas de uso
        List<OrigenAltaRepository.OrigenEstadistica> estadisticasUso =
                origenAltaRepository.getEstadisticasUso();

        stats.put("estadisticas_uso", estadisticasUso);

        return stats;
    }

    /**
     * OBTENER orígenes más utilizados
     */
    public List<OrigenAltaRepository.OrigenEstadistica> getOrigenesPopulares(int limit) throws SQLException {
        if (limit <= 0) {
            limit = 10; // Valor por defecto
        }

        List<OrigenAltaRepository.OrigenEstadistica> estadisticas =
                origenAltaRepository.getEstadisticasUso();

        // Limitar resultados
        if (estadisticas.size() > limit) {
            return estadisticas.subList(0, limit);
        }

        return estadisticas;
    }

    /**
     * OBTENER orígenes con actividad reciente
     */
    public List<OrigenAltaRepository.OrigenEstadistica> getOrigenesConActividadReciente() throws SQLException {
        List<OrigenAltaRepository.OrigenEstadistica> estadisticas =
                origenAltaRepository.getEstadisticasUso();

        // Filtrar solo los que tienen registros en el último mes
        return estadisticas.stream()
                .filter(est -> est.getRegistrosUltimoMes() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos del origen de alta
     */
    private void validateOrigenData(OrigenAlta origen) {
        if (origen == null) {
            throw new IllegalArgumentException("El origen de alta no puede ser nulo");
        }

        if (origen.getNombre_origen_alta() == null || origen.getNombre_origen_alta().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del origen de alta es requerido");
        }

        // Validar longitudes
        if (origen.getNombre_origen_alta().length() < 2 || origen.getNombre_origen_alta().length() > 100) {
            throw new IllegalArgumentException("El nombre del origen debe tener entre 2 y 100 caracteres");
        }

        // Validar caracteres permitidos
        if (!origen.getNombre_origen_alta().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-\\.\\(\\)]+$")) {
            throw new IllegalArgumentException("El nombre del origen solo puede contener letras, números, espacios, guiones, puntos y paréntesis");
        }
    }

    /**
     * Capitalizar palabras
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase())
                        .append(words[i].substring(1));
            }
        }

        return result.toString();
    }
}