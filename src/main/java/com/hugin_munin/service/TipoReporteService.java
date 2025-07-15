package com.hugin_munin.service;

import com.hugin_munin.model.TipoReporte;
import com.hugin_munin.repository.TipoReporteRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio para gestionar tipos de reporte
 * Contiene la lógica de negocio para tipos de reporte
 */
public class TipoReporteService {

    private final TipoReporteRepository tipoReporteRepository;

    public TipoReporteService(TipoReporteRepository tipoReporteRepository) {
        this.tipoReporteRepository = tipoReporteRepository;
    }

    /**
     * OBTENER todos los tipos de reporte
     */
    public List<TipoReporte> getAllTipos() throws SQLException {
        return tipoReporteRepository.findAll();
    }

    /**
     * OBTENER tipo de reporte por ID
     */
    public TipoReporte getTipoById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<TipoReporte> tipo = tipoReporteRepository.findById(id);
        return tipo.orElseThrow(() ->
                new IllegalArgumentException("Tipo de reporte no encontrado con ID: " + id));
    }

    /**
     * BUSCAR tipos por nombre
     */
    public List<TipoReporte> searchTiposByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return tipoReporteRepository.findByNameContaining(nombre.trim());
    }

    /**
     * CREAR nuevo tipo de reporte
     */
    public TipoReporte createTipo(TipoReporte tipo) throws SQLException {
        // Validaciones básicas
        validateTipoData(tipo);

        // Validar que el nombre no esté en uso
        if (tipoReporteRepository.existsByName(tipo.getNombre_tipo_reporte())) {
            throw new IllegalArgumentException("Ya existe un tipo de reporte con este nombre");
        }

        // Normalizar nombre
        tipo.setNombre_tipo_reporte(capitalizeFirstLetter(tipo.getNombre_tipo_reporte().trim()));

        // Guardar tipo
        return tipoReporteRepository.save(tipo);
    }

    /**
     * ACTUALIZAR tipo de reporte existente
     */
    public TipoReporte updateTipo(TipoReporte tipo) throws SQLException {
        if (tipo.getId_tipo_reporte() == null || tipo.getId_tipo_reporte() <= 0) {
            throw new IllegalArgumentException("ID del tipo requerido para actualización");
        }

        // Verificar que el tipo existe
        Optional<TipoReporte> existingTipo = tipoReporteRepository.findById(tipo.getId_tipo_reporte());
        if (existingTipo.isEmpty()) {
            throw new IllegalArgumentException("Tipo de reporte no encontrado con ID: " + tipo.getId_tipo_reporte());
        }

        // Validaciones básicas
        validateTipoData(tipo);

        // Validar que el nombre no esté en uso por otro tipo
        Optional<TipoReporte> tipoWithName = tipoReporteRepository.findByName(tipo.getNombre_tipo_reporte());
        if (tipoWithName.isPresent() && !tipoWithName.get().getId_tipo_reporte().equals(tipo.getId_tipo_reporte())) {
            throw new IllegalArgumentException("El nombre ya está en uso por otro tipo de reporte");
        }

        // Normalizar datos
        tipo.setNombre_tipo_reporte(capitalizeFirstLetter(tipo.getNombre_tipo_reporte().trim()));
        // Actualizar tipo
        boolean updated = tipoReporteRepository.update(tipo);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el tipo de reporte");
        }

        return tipo;
    }

    /**
     * ELIMINAR tipo de reporte
     */
    public boolean deleteTipo(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el tipo existe
        if (!tipoReporteRepository.existsById(id)) {
            throw new IllegalArgumentException("Tipo de reporte no encontrado con ID: " + id);
        }

        // Eliminación física si no está en uso
        return tipoReporteRepository.deleteById(id);
    }

    /**
     * VERIFICAR si un nombre de tipo está disponible
     */
    public boolean isTipoNameAvailable(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return !tipoReporteRepository.existsByName(nombre.trim());
    }

    /**
     * OBTENER estadísticas de tipos de reporte
     */
    public Map<String, Object> getTipoStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_tipos", tipoReporteRepository.countTotal());

        return stats;
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos del tipo de reporte
     */
    private void validateTipoData(TipoReporte tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de reporte no puede ser nulo");
        }

        if (!tipo.isValid()) {
            throw new IllegalArgumentException("Los datos del tipo de reporte no son válidos");
        }

        // Validar longitudes
        if (tipo.getNombre_tipo_reporte().length() < 2 || tipo.getNombre_tipo_reporte().length() > 100) {
            throw new IllegalArgumentException("El nombre del tipo debe tener entre 2 y 100 caracteres");
        }

        // Validar caracteres permitidos
        if (!tipo.getNombre_tipo_reporte().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s\\-]+$")) {
            throw new IllegalArgumentException("El nombre del tipo solo puede contener letras, espacios y guiones");
        }
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