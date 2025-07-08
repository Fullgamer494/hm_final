package com.hugin_munin.service;

import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.model.Usuario;
import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.model.Especimen;
import com.hugin_munin.model.Especie;
import com.hugin_munin.repository.RegistroAltaRepository;

import java.sql.SQLException;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class RegistroAltaService {

    private final RegistroAltaRepository repository;

    public RegistroAltaService(RegistroAltaRepository repository) {
        this.repository = repository;
    }

    // CREAR registro con validación de negocio
    public RegistroAlta create(RegistroAlta registro) throws SQLException {
        if (!registro.isValid()) {
            throw new IllegalArgumentException("Faltan campos obligatorios en el registro.");
        }

        // ✅ Validación corregida - solo verifica si el objeto especimen existe y está activo
        // En un POST típico, el objeto especimen será null, solo tendrás los IDs
        if (registro.getEspecimen() != null && !registro.getEspecimen().isActivo()) {
            throw new IllegalArgumentException("El espécimen no está activo.");
        }

        // Otras reglas de negocio pueden agregarse aquí...
        // Por ejemplo, validar que el id_especimen existe en la base de datos

        return repository.create(registro);
    }

    // OBTENER todos los registros con joins a especie, especimen, traslado
    public List<RegistroAlta> getAll() throws SQLException {
        return repository.findAll();
    }

    // OBTENER por ID
    public RegistroAlta getById(Integer id) throws SQLException {
        Optional<RegistroAlta> optional = repository.findById(id);
        return optional.orElseThrow(() -> new IllegalArgumentException("RegistroAlta no encontrado con ID: " + id));
    }

    // ACTUALIZAR
    public RegistroAlta update(RegistroAlta registro) throws SQLException {
        if (registro.getId_registro_alta() == null) {
            throw new IllegalArgumentException("ID del registro obligatorio para actualizar.");
        }

        if (!registro.isValid()) {
            throw new IllegalArgumentException("Faltan campos obligatorios.");
        }

        return repository.update(registro);
    }

    // ELIMINAR
    public boolean delete(Integer id) throws SQLException {
        return repository.delete(id);
    }

    // FILTRAR por id_especimen
    public List<RegistroAlta> getByEspecimen(Integer idEspecimen) throws SQLException {
        return repository.findByEspecimen(idEspecimen);
    }
}