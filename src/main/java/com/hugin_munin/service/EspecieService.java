package com.hugin_munin.service;

import com.hugin_munin.model.Especie;
import com.hugin_munin.repository.EspecieRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio para gestionar especies
 * Contiene toda la lógica de negocio para operaciones CRUD de especies
 */
public class EspecieService {
    private final EspecieRepository especieRepository;

    public EspecieService(EspecieRepository especieRepository) {
        this.especieRepository = especieRepository;
    }

    /**
     * Obtener todas las especies
     */
    public List<Especie> getAllSpecies() throws SQLException {
        return especieRepository.findAllSpecies();
    }

    /**
     * Obtener especie por ID
     */
    public Especie getSpecieById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<Especie> especie = especieRepository.findById(id);
        return especie.orElseThrow(() ->
                new IllegalArgumentException("Especie no encontrada con ID: " + id));
    }

    /**
     * Buscar especies por nombre científico
     */
    public List<Especie> getSpeciesByScientificName(String scientific_name) throws SQLException {
        if (scientific_name == null || scientific_name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre científico no puede estar vacío");
        }

        return especieRepository.findSpeciesByScientificName(scientific_name.trim());
    }

    /**
     * Crear nueva especie
     */
    public Especie createSpecie(Especie especie) throws SQLException {
        validateSpeciesData(especie);

        // Verificar que no exista ya la combinación género + especie
        if (especieRepository.existsByGeneroAndEspecie(especie.getGenero(), especie.getEspecie())) {
            throw new IllegalArgumentException("Ya existe una especie con este género y especie");
        }

        // Normalizar datos
        especie.setGenero(normalizeText(especie.getGenero()));
        especie.setEspecie(normalizeText(especie.getEspecie()));

        return especieRepository.saveSpecie(especie);
    }

    /**
     * Actualizar especie existente
     */
    public Especie updateSpecie(Especie especie) throws SQLException {
        if (especie.getId_especie() == null || especie.getId_especie() <= 0) {
            throw new IllegalArgumentException("ID de la especie requerido para actualización");
        }

        // Verificar que la especie existe
        Optional<Especie> existingEspecie = especieRepository.findById(especie.getId_especie());
        if (existingEspecie.isEmpty()) {
            throw new IllegalArgumentException("Especie no encontrada con ID: " + especie.getId_especie());
        }

        // Validar datos
        validateSpeciesData(especie);

        // Verificar que la combinación género + especie no esté en uso por otra especie
        if (especieRepository.existsByGeneroAndEspecie(especie.getGenero(), especie.getEspecie())) {
            // Verificar si es la misma especie que estamos actualizando
            List<Especie> especiesExistentes = especieRepository.findSpeciesByScientificName(
                    especie.getGenero() + " " + especie.getEspecie());

            if (!especiesExistentes.isEmpty() &&
                    !especiesExistentes.get(0).getId_especie().equals(especie.getId_especie())) {
                throw new IllegalArgumentException("Ya existe otra especie con este género y especie");
            }
        }

        // Normalizar datos
        especie.setGenero(normalizeText(especie.getGenero()));
        especie.setEspecie(normalizeText(especie.getEspecie()));

        // Actualizar especie
        boolean updated = especieRepository.update(especie);
        if (!updated) {
            throw new SQLException("No se pudo actualizar la especie");
        }

        return especie;
    }

    /**
     * Eliminar especie
     */
    public boolean deleteSpecie(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que la especie existe
        if (!especieRepository.existsById(id)) {
            throw new IllegalArgumentException("Especie no encontrada con ID: " + id);
        }

        // Verificar que la especie no esté siendo usada por especímenes
        if (especieRepository.isSpecieInUse(id)) {
            throw new IllegalArgumentException("No se puede eliminar la especie porque está siendo usada por especímenes");
        }

        return especieRepository.deleteById(id);
    }

    /**
     * Verificar si un nombre científico está disponible
     */
    public boolean isSpecieNameAvailable(String genero, String especie) throws SQLException {
        if (genero == null || genero.trim().isEmpty() ||
                especie == null || especie.trim().isEmpty()) {
            return false;
        }

        return !especieRepository.existsByGeneroAndEspecie(genero.trim(), especie.trim());
    }

    /**
     * Buscar o crear especie por nombre científico
     * Utilizado por el servicio de especímenes para creación unificada
     */
    public Especie findOrCreateByScientificName(String genero, String especie) throws SQLException {
        // Buscar especie existente
        if (especieRepository.existsByGeneroAndEspecie(genero, especie)) {
            List<Especie> especies = especieRepository.findSpeciesByScientificName(genero + " " + especie);
            if (!especies.isEmpty()) {
                return especies.get(0);
            }
        }

        // Crear nueva especie si no existe
        Especie nuevaEspecie = new Especie();
        nuevaEspecie.setGenero(normalizeText(genero));
        nuevaEspecie.setEspecie(normalizeText(especie));

        // Validar antes de crear
        validateSpeciesData(nuevaEspecie);

        return especieRepository.saveSpecie(nuevaEspecie);
    }

    /**
     * Obtener estadísticas de especies
     */
    public Map<String, Object> getSpecieStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_especies", especieRepository.countTotal());
        stats.put("generos_unicos", especieRepository.countUniqueGeneros());

        // Obtener géneros más comunes
        List<Map<String, Object>> generosMasComunes = especieRepository.getMostCommonGeneros(10);
        stats.put("generos_mas_comunes", generosMasComunes);

        return stats;
    }

    /**
     * Buscar especies por género
     */
    public List<Especie> getSpeciesByGenero(String genero) throws SQLException {
        if (genero == null || genero.trim().isEmpty()) {
            throw new IllegalArgumentException("El género no puede estar vacío");
        }

        return especieRepository.findByGenero(genero.trim());
    }

    /**
     * Obtener todos los géneros únicos
     */
    public List<String> getAllGeneros() throws SQLException {
        return especieRepository.findAllGeneros();
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos de la especie
     */
    private void validateSpeciesData(Especie especie) {
        if (especie == null) {
            throw new IllegalArgumentException("La especie no puede ser nula");
        }

        if (!especie.isValid()) {
            throw new IllegalArgumentException("Los datos de la especie no son válidos");
        }

        // Validar longitudes
        if (especie.getGenero().length() < 2 || especie.getGenero().length() > 50) {
            throw new IllegalArgumentException("El género debe tener entre 2 y 50 caracteres");
        }

        if (especie.getEspecie().length() < 2 || especie.getEspecie().length() > 100) {
            throw new IllegalArgumentException("La especie debe tener entre 2 y 100 caracteres");
        }

        // Validar caracteres permitidos (solo letras, espacios y guiones)
        if (!especie.getGenero().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s-]+$")) {
            throw new IllegalArgumentException("El género solo puede contener letras, espacios y guiones");
        }

        if (!especie.getEspecie().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s-]+$")) {
            throw new IllegalArgumentException("La especie solo puede contener letras, espacios y guiones");
        }

        // Validar que no empiecen o terminen con espacios o guiones
        if (especie.getGenero().trim().startsWith("-") || especie.getGenero().trim().endsWith("-")) {
            throw new IllegalArgumentException("El género no puede empezar o terminar con guiones");
        }

        if (especie.getEspecie().trim().startsWith("-") || especie.getEspecie().trim().endsWith("-")) {
            throw new IllegalArgumentException("La especie no puede empezar o terminar con guiones");
        }
    }

    /**
     * Normalizar texto para nombres científicos
     * Primera letra mayúscula, resto minúscula
     */
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String trimmed = text.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}