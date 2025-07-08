package com.hugin_munin.service;

import com.hugin_munin.model.Especie;
import com.hugin_munin.repository.EspecieRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EspecieService {
    private final EspecieRepository especieRepository;

    public EspecieService(EspecieRepository especieRepository) {this.especieRepository = especieRepository;}

    /**
     * GET ALL
     **/
    public List<Especie> getAllSpecies() throws SQLException {
        return especieRepository.findAllSpecies();
    }

    /**
     * GET BY SCIENTIFIC NAME
     **/
    public List<Especie> getSpeciesByScientificName(String scientific_name) throws SQLException {
        if(scientific_name == null || scientific_name.isEmpty()){
            throw new SQLException("El nombre científico no puede estar vacío");
        }

        return especieRepository.findSpeciesByScientificName(scientific_name.trim());
    }

    /**
     * CREATE SPECIE
     **/
    public Especie createSpecie(Especie especie) throws SQLException {
        validateSpeciesData(especie);

        if(especieRepository.existsByGeneroAndEspecie(especie.getGenero(), especie.getEspecie())){
            throw new IllegalArgumentException("El genero ya existe en la base de datos");
        }

        especie.setGenero(normalizeText(especie.getGenero()));
        especie.setEspecie(normalizeText(especie.getEspecie()));

        return especieRepository.saveSpecie(especie);
    }

    /**
     * VALIDATE SPECIES DATA
     **/
    private void validateSpeciesData(Especie especie) throws SQLException {
        if (especie == null) {
            throw new IllegalArgumentException("La especie no puede ser nula");
        }

        if (!especie.isValid()) {
            throw new IllegalArgumentException("Los datos de la especie no son válidos");
        }

        if (especie.getGenero().length() < 2 || especie.getGenero().length() > 50) {
            throw new IllegalArgumentException("El género debe tener entre 2 y 50 caracteres");
        }

        if (especie.getEspecie().length() < 2 || especie.getEspecie().length() > 100) {
            throw new IllegalArgumentException("La especie debe tener entre 2 y 100 caracteres");
        }

        if (!especie.getGenero().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s-]+$")) {
            throw new IllegalArgumentException("El género solo puede contener letras, espacios y guiones");
        }

        if (!especie.getEspecie().matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s-]+$")) {
            throw new IllegalArgumentException("La especie solo puede contener letras, espacios y guiones");
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
