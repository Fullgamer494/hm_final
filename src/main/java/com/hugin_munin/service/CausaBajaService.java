package com.hugin_munin.service;

import com.hugin_munin.model.CausaBaja;
import com.hugin_munin.repository.CausaBajaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio completo para gestionar causas de baja
 * Contiene la lógica de negocio para causas de baja
 */
public class CausaBajaService {

    private final CausaBajaRepository causaBajaRepository;

    public CausaBajaService(CausaBajaRepository causaBajaRepository) {
        this.causaBajaRepository = causaBajaRepository;
    }

    /**
     * OBTENER todas las causas de baja
     */
    public List<CausaBaja> getAll() throws SQLException {
        return causaBajaRepository.getAllCausaBaja();
    }

    /**
     * OBTENER causa de baja por ID - VERSION LEGACY
     */
    public List<CausaBaja> getById(int id) throws SQLException {
        return causaBajaRepository.getByIdCausaBaja(id);
    }

    /**
     * OBTENER causa de baja por ID - VERSION MEJORADA
     */
    public CausaBaja getCausaById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<CausaBaja> causa = causaBajaRepository.findById(id);
        return causa.orElseThrow(() ->
                new IllegalArgumentException("Causa de baja no encontrada con ID: " + id));
    }

    /**
     * BUSCAR causas por nombre
     */
    public List<CausaBaja> searchCausasByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return causaBajaRepository.findByNameContaining(nombre.trim());
    }

    /**
     * CREAR nueva causa de baja
     */
    public CausaBaja createCausa(CausaBaja causa) throws SQLException {
        // Validaciones básicas
        validateCausaData(causa);

        // Validar que el nombre no esté en uso
        if (causaBajaRepository.existsByName(causa.getNombreCausaBaja())) {
            throw new IllegalArgumentException("Ya existe una causa de baja con este nombre");
        }

        // Normalizar nombre
        causa.setNombreCausaBaja(capitalizeWords(causa.getNombreCausaBaja().trim()));

        // Guardar causa
        return causaBajaRepository.save(causa);
    }

    /**
     * ACTUALIZAR causa de baja - VERSION LEGACY
     */
    public boolean update(int id, String nombreCausaBaja) throws SQLException {
        if (nombreCausaBaja == null || nombreCausaBaja.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la causa de baja no puede estar vacío");
        }

        // Verificar que existe
        if (!causaBajaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró la causa de baja con ID: " + id);
        }

        // Verificar que el nombre no esté en uso por otra causa
        Optional<CausaBaja> causaWithName = causaBajaRepository.findByName(nombreCausaBaja.trim());
        if (causaWithName.isPresent() && !causaWithName.get().getIdCausaBaja().equals(id)) {
            throw new IllegalArgumentException("El nombre ya está en uso por otra causa de baja");
        }

        return causaBajaRepository.updateCausaBaja(id, capitalizeWords(nombreCausaBaja.trim()));
    }

    /**
     * ACTUALIZAR causa de baja - VERSION MEJORADA
     */
    public CausaBaja updateCausa(CausaBaja causa) throws SQLException {
        if (causa.getIdCausaBaja() == null || causa.getIdCausaBaja() <= 0) {
            throw new IllegalArgumentException("ID de la causa requerido para actualización");
        }

        // Verificar que la causa existe
        Optional<CausaBaja> existingCausa = causaBajaRepository.findById(causa.getIdCausaBaja());
        if (existingCausa.isEmpty()) {
            throw new IllegalArgumentException("Causa de baja no encontrada con ID: " + causa.getIdCausaBaja());
        }

        // Validaciones básicas
        validateCausaData(causa);

        // Validar que el nombre no esté en uso por otra causa
        Optional<CausaBaja> causaWithName = causaBajaRepository.findByName(causa.getNombreCausaBaja());
        if (causaWithName.isPresent() && !causaWithName.get().getIdCausaBaja().equals(causa.getIdCausaBaja())) {
            throw new IllegalArgumentException("El nombre ya está en uso por otra causa de baja");
        }

        // Normalizar datos
        causa.setNombreCausaBaja(capitalizeWords(causa.getNombreCausaBaja().trim()));

        // Actualizar causa
        boolean updated = causaBajaRepository.update(causa);
        if (!updated) {
            throw new SQLException("No se pudo actualizar la causa de baja");
        }

        return causa;
    }

    /**
     * ELIMINAR causa de baja
     */
    public boolean deleteCausa(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que la causa existe
        if (!causaBajaRepository.existsById(id)) {
            throw new IllegalArgumentException("Causa de baja no encontrada con ID: " + id);
        }

        // Verificar que la causa no esté siendo usada en registros de baja
        if (causaBajaRepository.isCausaInUse(id)) {
            throw new IllegalArgumentException("No se puede eliminar la causa porque está siendo usada en registros de baja");
        }

        // Eliminar causa
        return causaBajaRepository.deleteById(id);
    }

    /**
     * VERIFICAR si un nombre de causa está disponible
     */
    public boolean isCausaNameAvailable(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        return !causaBajaRepository.existsByName(nombre.trim());
    }

    /**
     * OBTENER estadísticas de causas de baja
     */
    public Map<String, Object> getCausaStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_causas", causaBajaRepository.countTotal());

        // Obtener estadísticas de uso
        List<CausaBajaRepository.CausaEstadistica> estadisticasUso =
                causaBajaRepository.getEstadisticasUso();

        stats.put("estadisticas_uso", estadisticasUso);

        return stats;
    }

    /**
     * OBTENER causas más utilizadas
     */
    public List<CausaBajaRepository.CausaEstadistica> getCausasPopulares(int limit) throws SQLException {
        if (limit <= 0) {
            limit = 10; // Valor por defecto
        }

        List<CausaBajaRepository.CausaEstadistica> estadisticas =
                causaBajaRepository.getEstadisticasUso();

        // Limitar resultados
        if (estadisticas.size() > limit) {
            return estadisticas.subList(0, limit);
        }

        return estadisticas;
    }

    /**
     * OBTENER causas con actividad reciente
     */
    public List<CausaBajaRepository.CausaEstadistica> getCausasConActividadReciente() throws SQLException {
        List<CausaBajaRepository.CausaEstadistica> estadisticas =
                causaBajaRepository.getEstadisticasUso();

        // Filtrar solo las que tienen registros en el último mes
        return estadisticas.stream()
                .filter(est -> est.getRegistrosUltimoMes() > 0)
                .collect(java.util.stream.Collectors.toList());
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos de la causa de baja
     */
    private void validateCausaData(CausaBaja causa) {
        if (causa == null) {
            throw new IllegalArgumentException("La causa de baja no puede ser nula");
        }

        if (!causa.isValid()) {
            throw new IllegalArgumentException("Los datos de la causa de baja no son válidos");
        }

        // Validar longitudes
        if (causa.getNombreCausaBaja().length() < 2 || causa.getNombreCausaBaja().length() > 100) {
            throw new IllegalArgumentException("El nombre de la causa debe tener entre 2 y 100 caracteres");
        }

        // Validar caracteres permitidos
        if (!causa.getNombreCausaBaja().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-\\.\\(\\)]+$")) {
            throw new IllegalArgumentException("El nombre de la causa solo puede contener letras, números, espacios, guiones, puntos y paréntesis");
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