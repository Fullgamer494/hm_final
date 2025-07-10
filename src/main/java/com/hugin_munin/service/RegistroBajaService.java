package com.hugin_munin.service;

import com.hugin_munin.model.RegistroBaja;
import com.hugin_munin.repository.RegistroBajaRepository;
import com.hugin_munin.repository.EspecimenRepository;
import com.hugin_munin.repository.UsuarioRepository;
import com.hugin_munin.repository.CausaBajaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Servicio para gestionar los registros de baja
 * Maneja la lógica de negocio y validaciones
 */
public class RegistroBajaService {

    private final RegistroBajaRepository repository;
    private final EspecimenRepository especimenRepository;
    private final UsuarioRepository usuarioRepository;
    private final CausaBajaRepository causaBajaRepository;

    public RegistroBajaService(RegistroBajaRepository repository,
                               EspecimenRepository especimenRepository,
                               UsuarioRepository usuarioRepository,
                               CausaBajaRepository causaBajaRepository) {
        this.repository = repository;
        this.especimenRepository = especimenRepository;
        this.usuarioRepository = usuarioRepository;
        this.causaBajaRepository = causaBajaRepository;
    }

    /**
     * CREAR nuevo registro de baja con validaciones completas
     */
    public RegistroBaja create(RegistroBaja registro) throws SQLException {
        // Validaciones básicas
        validateBasicData(registro);

        // Validaciones de relaciones foráneas
        validateForeignKeys(registro);

        // Validaciones de negocio específicas para baja
        validateBusinessRules(registro);

        // Establecer fecha de baja si no se proporcionó
        if (registro.getFecha_baja() == null) {
            registro.setFecha_baja(new Date());
        }

        return repository.saveRegister(registro);
    }

    /**
     * OBTENER todos los registros con información completa de relaciones
     */
    public List<RegistroBaja> getAll() throws SQLException {
        return repository.findAllRegisters();
    }

    /**
     * OBTENER registro por ID con información completa
     */
    public RegistroBaja getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<RegistroBaja> optional = repository.findRegistersById(id);
        return optional.orElseThrow(() ->
                new IllegalArgumentException("RegistroBaja no encontrado con ID: " + id));
    }

    /**
     * ACTUALIZAR registro existente
     */
    public RegistroBaja update(RegistroBaja registro) throws SQLException {
        if (registro.getId_registro_baja() == null || registro.getId_registro_baja() <= 0) {
            throw new IllegalArgumentException("ID del registro obligatorio para actualizar");
        }

        // Verificar que el registro existe
        Optional<RegistroBaja> existingOptional = repository.findRegistersById(registro.getId_registro_baja());
        if (existingOptional.isEmpty()) {
            throw new IllegalArgumentException("No existe el registro con ID: " + registro.getId_registro_baja());
        }

        // Validaciones
        validateBasicData(registro);
        validateForeignKeys(registro);
        validateBusinessRules(registro);

        return repository.updateRegister(registro);
    }

    /**
     * ELIMINAR registro por ID (reactivar especimen)
     */
    public boolean delete(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el registro existe
        Optional<RegistroBaja> existingOptional = repository.findRegistersById(id);
        if (existingOptional.isEmpty()) {
            throw new IllegalArgumentException("No existe el registro con ID: " + id);
        }

        return repository.delete(id);
    }

    /**
     * BUSCAR registros por especimen
     */
    public List<RegistroBaja> getByEspecimen(Integer idEspecimen) throws SQLException {
        if (idEspecimen == null || idEspecimen <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        return repository.findByEspecimen(idEspecimen);
    }

    /**
     * BUSCAR registros por causa de baja
     */
    public List<RegistroBaja> getByCausaBaja(Integer idCausaBaja) throws SQLException {
        if (idCausaBaja == null || idCausaBaja <= 0) {
            throw new IllegalArgumentException("ID de causa de baja inválido");
        }

        return repository.findByCausaBaja(idCausaBaja);
    }

    /**
     * BUSCAR registros por responsable
     */
    public List<RegistroBaja> getByResponsable(Integer idResponsable) throws SQLException {
        if (idResponsable == null || idResponsable <= 0) {
            throw new IllegalArgumentException("ID de responsable inválido");
        }

        return repository.findByResponsable(idResponsable);
    }

    /**
     * BUSCAR registros por rango de fechas
     */
    public List<RegistroBaja> getByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        return repository.findByDateRange(fechaInicio, fechaFin);
    }

    /**
     * OBTENER estadísticas por causa de baja
     */
    public List<RegistroBajaRepository.EstadisticaCausa> getEstadisticasPorCausa() throws SQLException {
        return repository.getEstadisticasPorCausa();
    }

    /**
     * CONTAR total de registros
     */
    public int countTotal() throws SQLException {
        return repository.countTotal();
    }

    /**
     * VERIFICAR si un especimen ya está dado de baja
     */
    public boolean isEspecimenDadoDeBaja(Integer idEspecimen) throws SQLException {
        if (idEspecimen == null || idEspecimen <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        return repository.existsByEspecimen(idEspecimen);
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos básicos del registro
     */
    private void validateBasicData(RegistroBaja registro) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro no puede ser nulo");
        }

        if (!registro.isValid()) {
            throw new IllegalArgumentException("Faltan campos obligatorios en el registro");
        }

        if (registro.getObservacion() != null && registro.getObservacion().length() > 500) {
            throw new IllegalArgumentException("La observación no puede exceder 500 caracteres");
        }

        if (registro.getObservacion() == null || registro.getObservacion().trim().isEmpty()) {
            throw new IllegalArgumentException("La observación es obligatoria para registrar una baja");
        }
    }

    /**
     * Validar que las claves foráneas existen
     */
    private void validateForeignKeys(RegistroBaja registro) throws SQLException {
        // Validar que el especimen existe
        if (!especimenRepository.existsById(registro.getId_especimen())) {
            throw new IllegalArgumentException("El especimen con ID " + registro.getId_especimen() + " no existe");
        }

        // Validar que el responsable existe
        if (!usuarioRepository.existsById(registro.getId_responsable())) {
            throw new IllegalArgumentException("El responsable con ID " + registro.getId_responsable() + " no existe");
        }

        // Validar que la causa de baja existe
        if (causaBajaRepository.findById(registro.getId_causa_baja()).isEmpty()) {
            throw new IllegalArgumentException("La causa de baja con ID " + registro.getId_causa_baja() + " no existe");
        }
    }

    /**
     * Validar reglas de negocio específicas para baja
     */
    private void validateBusinessRules(RegistroBaja registro) throws SQLException {
        // Validar que el especimen no esté ya dado de baja
        if (repository.existsByEspecimen(registro.getId_especimen())) {
            throw new IllegalArgumentException("El especimen ya está dado de baja");
        }

        // Validar que la fecha de baja no sea futura
        if (registro.getFecha_baja() != null && registro.getFecha_baja().after(new Date())) {
            throw new IllegalArgumentException("La fecha de baja no puede ser futura");
        }

        // Validar que el especimen esté activo (no se puede dar de baja un especimen ya inactivo)
        Optional<com.hugin_munin.model.Especimen> especimen = especimenRepository.findById(registro.getId_especimen());
        if (especimen.isPresent() && !especimen.get().isActivo()) {
            throw new IllegalArgumentException("No se puede dar de baja un especimen que ya está inactivo");
        }
    }
}