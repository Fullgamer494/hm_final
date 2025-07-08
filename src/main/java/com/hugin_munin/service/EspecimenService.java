package com.hugin_munin.service;

import com.hugin_munin.model.Especimen;
import com.hugin_munin.repository.EspecimenRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EspecimenService {
    private EspecimenRepository especimenRepository;

    public EspecimenService(EspecimenRepository especimenRepository) { this.especimenRepository = especimenRepository; }

    /**
     * GET ALL
     **/
    public List<Especimen> getAllSpecimens() throws SQLException {
        return especimenRepository.findAllSpecimen();
    }

    /**
     * CREATE SPECIMEN
     **/
    public Especimen createSpecimen(Especimen especimen) throws SQLException {
        validateSpecimenData(especimen);

        if(especimenRepository.existsById(especimen.getId_especimen())){
            throw new IllegalArgumentException("El espécimen ya existe en la base de datos");
        }
        else if(especimenRepository.existsByIN(especimen.getNum_inventario())){
            throw new IllegalArgumentException("El nùmero de inventario ya pertenece a un espécimen registrado");
        }

        especimen.setNum_inventario(especimen.getNum_inventario());
        especimen.setId_especie(especimen.getId_especie());
        especimen.setNombre_especimen(especimen.getNombre_especimen());
        especimen.setActivo(especimen.isActivo());

        return especimenRepository.saveSpecimen(especimen);
    }

    /**
     * VALIDATE SPECIES DATA
     **/
    private void validateSpecimenData(Especimen especimen) throws SQLException {
        if (especimen == null) {
            throw new IllegalArgumentException("El especimen no puede ser nulo");
        }

        if (!especimen.isValid()) {
            throw new IllegalArgumentException("Los datos del especimen no son válidos");
        }

        if (!especimen.getNum_inventario().matches("^[a-zA-Z0-9\\-_.#]+$")) {
            throw new IllegalArgumentException("El número de inventario no admite caracteres inválidos (sólo números, letras, guiones, puntos y hashtag)");
        }

        

        if (!especimenRepository.existsById(especimen.getId_especie())) {
            throw new IllegalArgumentException("La especie indicada no existe");
        }

        if (!especimen.getNombre_especimen().matches("^[a-zA-Z ]+$")) {
            throw new IllegalArgumentException("El nombre del espécimen solo puede tener letras y espacios");
        }
    }

    /**
     * NORMALIZE TEXT
     **/
    private String normalizeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        String trimmed = text.trim();
        return trimmed.substring(0, 1).toUpperCase() + trimmed.substring(1).toLowerCase();
    }
}
