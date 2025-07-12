package com.hugin_munin.service;

import com.hugin_munin.model.Especimen;
import com.hugin_munin.model.Especie;
import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.repository.EspecimenRepository;
import com.hugin_munin.repository.EspecieRepository;
import com.hugin_munin.repository.RegistroAltaRepository;
import com.hugin_munin.repository.UsuarioRepository;
import com.hugin_munin.repository.OrigenAltaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Date;

/**
 * Servicio para gestionar especímenes con lógica de creación unificada
 * Maneja la creación coordinada de especie, especimen y registro de alta
 */
public class EspecimenService {
    private final EspecimenRepository especimenRepository;
    private final EspecieRepository especieRepository;
    private final RegistroAltaRepository registroAltaRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrigenAltaRepository origenAltaRepository;

    public EspecimenService(EspecimenRepository especimenRepository,
                            EspecieRepository especieRepository,
                            RegistroAltaRepository registroAltaRepository,
                            UsuarioRepository usuarioRepository,
                            OrigenAltaRepository origenAltaRepository) {
        this.especimenRepository = especimenRepository;
        this.especieRepository = especieRepository;
        this.registroAltaRepository = registroAltaRepository;
        this.usuarioRepository = usuarioRepository;
        this.origenAltaRepository = origenAltaRepository;
    }

    /**
     * Obtener todos los especímenes
     */
    public List<Especimen> getAllSpecimens() throws SQLException {
        return especimenRepository.findAllSpecimen();
    }

    /**
     * Obtener especimen por ID
     */
    public Especimen getSpecimenById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<Especimen> especimen = especimenRepository.findById(id);
        return especimen.orElseThrow(() ->
                new IllegalArgumentException("Especimen no encontrado con ID: " + id));
    }

    /**
     * Obtener especímenes activos
     */
    public List<Especimen> getActiveSpecimens() throws SQLException {
        return especimenRepository.findActiveSpecimens();
    }

    /**
     * Buscar especímenes por nombre
     */
    public List<Especimen> searchSpecimensByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return especimenRepository.findByNameContaining(nombre.trim());
    }

    /**
     * Crear especimen con manejo unificado de especie y registro de alta
     * Este método maneja todo el proceso de creación desde el formulario único del frontend
     */
    public Map<String, Object> createSpecimenWithRegistration(Map<String, Object> requestData) throws SQLException {
        // Extraer datos del mapa de solicitud
        Map<String, String> especieData = (Map<String, String>) requestData.get("especie");
        Map<String, Object> especimenData = (Map<String, Object>) requestData.get("especimen");
        Map<String, Object> registroData = (Map<String, Object>) requestData.get("registro_alta");

        // Validar que todos los datos necesarios estén presentes
        validateUnifiedRequestData(especieData, especimenData, registroData);

        // 1. Buscar o crear la especie
        Especie especie = findOrCreateEspecie(especieData);

        // 2. Crear el especimen con la especie encontrada/creada
        Especimen especimen = createSpecimen(especimenData, especie);

        // 3. Crear el registro de alta
        RegistroAlta registroAlta = createRegistroAlta(registroData, especimen);

        // Preparar respuesta completa
        Map<String, Object> response = new HashMap<>();
        response.put("especie", especie);
        response.put("especimen", especimen);
        response.put("registro_alta", registroAlta);
        response.put("message", "Especimen registrado exitosamente con todos sus datos asociados");
        response.put("success", true);

        return response;
    }

    /**
     * Buscar especie existente o crear una nueva
     */
    private Especie findOrCreateEspecie(Map<String, String> especieData) throws SQLException {
        String genero = especieData.get("genero");
        String especie = especieData.get("especie");

        // Buscar si ya existe la especie
        if (especieRepository.existsByGeneroAndEspecie(genero, especie)) {
            // Obtener la especie existente
            List<Especie> especies = especieRepository.findSpeciesByScientificName(genero + " " + especie);
            if (!especies.isEmpty()) {
                return especies.get(0);
            }
        }

        // Crear nueva especie si no existe
        Especie nuevaEspecie = new Especie();
        nuevaEspecie.setGenero(normalizeText(genero));
        nuevaEspecie.setEspecie(normalizeText(especie));

        return especieRepository.saveSpecie(nuevaEspecie);
    }

    /**
     * Crear especimen con datos del mapa
     */
    private Especimen createSpecimen(Map<String, Object> especimenData, Especie especie) throws SQLException {
        Especimen especimen = new Especimen();
        especimen.setNum_inventario((String) especimenData.get("num_inventario"));
        especimen.setId_especie(especie.getId_especie());
        especimen.setNombre_especimen((String) especimenData.get("nombre_especimen"));
        especimen.setActivo(true); // Por defecto activo

        // Validar datos del especimen
        validateSpecimenData(especimen);

        // Verificar que el número de inventario no esté en uso
        if (especimenRepository.existsByIN(especimen.getNum_inventario())) {
            throw new IllegalArgumentException("El número de inventario ya está en uso");
        }

        return especimenRepository.saveSpecimen(especimen);
    }

    /**
     * Crear registro de alta
     */
    private RegistroAlta createRegistroAlta(Map<String, Object> registroData, Especimen especimen) throws SQLException {
        RegistroAlta registro = new RegistroAlta();
        registro.setId_especimen(especimen.getId_especimen());
        registro.setId_origen_alta((Integer) registroData.get("id_origen_alta"));
        registro.setId_responsable((Integer) registroData.get("id_responsable"));
        registro.setProcedencia((String) registroData.get("procedencia"));
        registro.setObservacion((String) registroData.get("observacion"));

        // Establecer fecha de ingreso
        if (registroData.containsKey("fecha_ingreso") && registroData.get("fecha_ingreso") != null) {
            registro.setFecha_ingreso((Date) registroData.get("fecha_ingreso"));
        } else {
            registro.setFecha_ingreso(new Date());
        }

        // Validar que las referencias existan
        validateRegistroReferences(registro);

        return registroAltaRepository.saveRegister(registro);
    }

    /**
     * Crear especimen simple (método original mantenido para compatibilidad)
     */
    public Especimen createSpecimen(Especimen especimen) throws SQLException {
        validateSpecimenData(especimen);

        if (especimenRepository.existsByIN(especimen.getNum_inventario())) {
            throw new IllegalArgumentException("El número de inventario ya está en uso");
        }

        if (!especieRepository.existsById(especimen.getId_especie())) {
            throw new IllegalArgumentException("La especie especificada no existe");
        }

        especimen.setActivo(true); // Por defecto activo
        return especimenRepository.saveSpecimen(especimen);
    }

    /**
     * Actualizar especimen existente
     */
    public Especimen updateSpecimen(Especimen especimen) throws SQLException {
        if (especimen.getId_especimen() == null || especimen.getId_especimen() <= 0) {
            throw new IllegalArgumentException("ID del especimen requerido para actualización");
        }

        // Verificar que el especimen existe
        Optional<Especimen> existingSpecimen = especimenRepository.findById(especimen.getId_especimen());
        if (existingSpecimen.isEmpty()) {
            throw new IllegalArgumentException("Especimen no encontrado con ID: " + especimen.getId_especimen());
        }

        // Validar datos
        validateSpecimenData(especimen);

        // Verificar que el número de inventario no esté en uso por otro especimen
        if (especimenRepository.existsByIN(especimen.getNum_inventario())) {
            Optional<Especimen> especimenWithInventory = especimenRepository.findByInventoryNumber(especimen.getNum_inventario());
            if (especimenWithInventory.isPresent() &&
                    !especimenWithInventory.get().getId_especimen().equals(especimen.getId_especimen())) {
                throw new IllegalArgumentException("El número de inventario ya está en uso por otro especimen");
            }
        }

        // Verificar que la especie existe
        if (!especieRepository.existsById(especimen.getId_especie())) {
            throw new IllegalArgumentException("La especie especificada no existe");
        }

        // Actualizar especimen
        boolean updated = especimenRepository.update(especimen);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el especimen");
        }

        return especimen;
    }

    /**
     * Eliminar especimen
     */
    public boolean deleteSpecimen(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el especimen existe
        if (!especimenRepository.existsById(id)) {
            throw new IllegalArgumentException("Especimen no encontrado con ID: " + id);
        }

        // Verificar que el especimen no esté siendo usado en registros
        if (especimenRepository.isSpecimenInUse(id)) {
            throw new IllegalArgumentException("No se puede eliminar el especimen porque está siendo usado en registros");
        }

        return especimenRepository.deleteById(id);
    }

    /**
     * Activar especimen
     */
    public boolean activateSpecimen(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        if (!especimenRepository.existsById(id)) {
            throw new IllegalArgumentException("Especimen no encontrado con ID: " + id);
        }

        return especimenRepository.activateById(id);
    }

    /**
     * Desactivar especimen
     */
    public boolean deactivateSpecimen(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        if (!especimenRepository.existsById(id)) {
            throw new IllegalArgumentException("Especimen no encontrado con ID: " + id);
        }

        return especimenRepository.deactivateById(id);
    }

    /**
     * Verificar si un número de inventario está disponible
     */
    public boolean isInventoryNumberAvailable(String numInventario) throws SQLException {
        if (numInventario == null || numInventario.trim().isEmpty()) {
            return false;
        }

        return !especimenRepository.existsByIN(numInventario.trim());
    }

    /**
     * Obtener estadísticas de especímenes
     */
    public Map<String, Object> getSpecimenStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_especimenes", especimenRepository.countTotal());
        stats.put("especimenes_activos", especimenRepository.countActive());
        stats.put("especimenes_inactivos", especimenRepository.countInactive());

        return stats;
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos de solicitud unificada
     */
    private void validateUnifiedRequestData(Map<String, String> especieData,
                                            Map<String, Object> especimenData,
                                            Map<String, Object> registroData) {
        if (especieData == null || especimenData == null || registroData == null) {
            throw new IllegalArgumentException("Todos los datos (especie, especimen, registro) son requeridos");
        }

        // Validar datos de especie
        if (especieData.get("genero") == null || especieData.get("genero").trim().isEmpty()) {
            throw new IllegalArgumentException("El género de la especie es requerido");
        }
        if (especieData.get("especie") == null || especieData.get("especie").trim().isEmpty()) {
            throw new IllegalArgumentException("La especie es requerida");
        }

        // Validar datos de especimen
        if (especimenData.get("num_inventario") == null || especimenData.get("num_inventario").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de inventario es requerido");
        }
        if (especimenData.get("nombre_especimen") == null || especimenData.get("nombre_especimen").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del especimen es requerido");
        }

        // Validar datos de registro
        if (registroData.get("id_origen_alta") == null) {
            throw new IllegalArgumentException("El origen de alta es requerido");
        }
        if (registroData.get("id_responsable") == null) {
            throw new IllegalArgumentException("El responsable es requerido");
        }
        if (registroData.get("procedencia") == null || registroData.get("procedencia").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("La procedencia es requerida");
        }
        if (registroData.get("observacion") == null || registroData.get("observacion").toString().trim().isEmpty()) {
            throw new IllegalArgumentException("La observación es requerida");
        }
    }

    /**
     * Validar datos del especimen
     */
    private void validateSpecimenData(Especimen especimen) throws SQLException {
        if (especimen == null) {
            throw new IllegalArgumentException("El especimen no puede ser nulo");
        }

        if (!especimen.isValid()) {
            throw new IllegalArgumentException("Los datos del especimen no son válidos");
        }

        if (especimen.getNum_inventario().length() < 1 || especimen.getNum_inventario().length() > 50) {
            throw new IllegalArgumentException("El número de inventario debe tener entre 1 y 50 caracteres");
        }

        if (!especimen.getNum_inventario().matches("^[a-zA-Z0-9\\-_.#]+$")) {
            throw new IllegalArgumentException("El número de inventario solo puede contener letras, números, guiones, puntos y #");
        }

        if (especimen.getNombre_especimen().length() < 2 || especimen.getNombre_especimen().length() > 100) {
            throw new IllegalArgumentException("El nombre del especimen debe tener entre 2 y 100 caracteres");
        }

        if (!especimen.getNombre_especimen().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            throw new IllegalArgumentException("El nombre del especimen solo puede contener letras y espacios");
        }
    }

    /**
     * Validar referencias del registro de alta
     */
    private void validateRegistroReferences(RegistroAlta registro) throws SQLException {
        // Verificar que el responsable existe
        if (!usuarioRepository.existsById(registro.getId_responsable())) {
            throw new IllegalArgumentException("El responsable con ID " + registro.getId_responsable() + " no existe");
        }

        // Verificar que el origen de alta existe
        if (!origenAltaRepository.existsById(registro.getId_origen_alta())) {
            throw new IllegalArgumentException("El origen de alta con ID " + registro.getId_origen_alta() + " no existe");
        }

        // Validar datos básicos del registro
        if (registro.getProcedencia() == null || registro.getProcedencia().trim().isEmpty()) {
            throw new IllegalArgumentException("La procedencia es requerida");
        }

        if (registro.getObservacion() == null || registro.getObservacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La observación es requerida");
        }

        if (registro.getProcedencia().length() > 200) {
            throw new IllegalArgumentException("La procedencia no puede exceder 200 caracteres");
        }

        if (registro.getObservacion().length() > 500) {
            throw new IllegalArgumentException("La observación no puede exceder 500 caracteres");
        }
    }

    /**
     * Normalizar texto para especie
     */
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String trimmed = text.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}