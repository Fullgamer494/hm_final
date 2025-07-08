package com.hugin_munin.service;

import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.repository.RegistroAltaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RegistroAltaService {

    private final RegistroAltaRepository repository;

    public RegistroAltaService(RegistroAltaRepository repository) {
        this.repository = repository;
    }

    /**
     * CREAR registro con validación de negocio
     */
    public RegistroAlta create(RegistroAlta registro) throws SQLException {
        if (!registro.isValid()) {
            throw new IllegalArgumentException("Faltan campos obligatorios en el registro.");
        }

        // Validar que los IDs existen (aquí podrías agregar validaciones con otros repositories)
        if (registro.getId_especimen() == null || registro.getId_especimen() <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        if (registro.getId_origen_alta() == null || registro.getId_origen_alta() <= 0) {
            throw new IllegalArgumentException("ID de origen alta inválido");
        }

        if (registro.getId_responsable() == null || registro.getId_responsable() <= 0) {
            throw new IllegalArgumentException("ID de responsable inválido");
        }

        // ✅ CORREGIDO: Usar el método correcto del repository
        return repository.saveRegister(registro);
    }

    /**
     * OBTENER todos los registros con joins
     */
    public List<RegistroAlta> getAll() throws SQLException {
        // ✅ CORREGIDO: Usar el método correcto del repository
        return repository.findAllRegisters();
    }

    /**
     * OBTENER por ID
     */
    public RegistroAlta getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // ✅ CORREGIDO: Usar el método correcto del repository
        Optional<RegistroAlta> optional = repository.findRegistersById(id);
        return optional.orElseThrow(() -> new IllegalArgumentException("RegistroAlta no encontrado con ID: " + id));
    }

    /**
     * ACTUALIZAR
     */
    public RegistroAlta update(RegistroAlta registro) throws SQLException {
        if (registro.getId_registro_alta() == null || registro.getId_registro_alta() <= 0) {
            throw new IllegalArgumentException("ID del registro obligatorio para actualizar.");
        }

        if (!registro.isValid()) {
            throw new IllegalArgumentException("Faltan campos obligatorios.");
        }

        // Verificar que el registro existe
        Optional<RegistroAlta> existingOptional = repository.findRegistersById(registro.getId_registro_alta());
        if (existingOptional.isEmpty()) {
            throw new IllegalArgumentException("No existe el registro con ID: " + registro.getId_registro_alta());
        }

        // ✅ CORREGIDO: Usar el método correcto del repository
        return repository.updateRegister(registro);
    }

    /**
     * ELIMINAR
     */
    public boolean delete(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el registro existe antes de eliminarlo
        Optional<RegistroAlta> existingOptional = repository.findRegistersById(id);
        if (existingOptional.isEmpty()) {
            throw new IllegalArgumentException("No existe el registro con ID: " + id);
        }

        return repository.delete(id);
    }

    /**
     * FILTRAR por id_especimen
     */
    public List<RegistroAlta> getByEspecimen(Integer idEspecimen) throws SQLException {
        if (idEspecimen == null || idEspecimen <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        return repository.findByEspecimen(idEspecimen);
    }
}