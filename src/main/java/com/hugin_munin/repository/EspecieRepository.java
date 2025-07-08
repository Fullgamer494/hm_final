package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Especie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EspecieRepository {
    /**
     * FIND ALL
     **/
    public List<Especie> findAllSpecies() throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = "SELECT * FROM especie ORDER BY id_especie ASC";

        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()){

            while(rs.next()){
                Especie especie = mapResultSetToEspecie(rs);
                especies.add(especie);
            }
        }
        return especies;
    }

    /**
     * FIND BY SCIENTIFIC NAME
     **/
    public List<Especie> findSpeciesByScientificName(String scientificName) throws SQLException {
        List<Especie> especies = new ArrayList<>();
        String query = "SELECT id_especie, genero, especie FROM especie WHERE CONCAT(genero, ' ', especie) LIKE ? ORDER BY genero, especie";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + scientificName + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especies.add(mapResultSetToEspecie(rs));
                }
            }
        }

        return especies;
    }

    /**
     * SAVE
     **/
    public Especie saveSpecie(Especie especie) throws SQLException {
        String query = "INSERT INTO especie (genero, especie) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, especie.getGenero());
            stmt.setString(2, especie.getEspecie());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear especie");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    especie.setId_especie(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID de la especie");
                }
            }
        }

        return especie;
    }


    /**
     * CHECK IF SPECIE EXISTS
     **/
    public boolean existsByGeneroAndEspecie(String genero, String especie) throws SQLException {
        String query = "SELECT COUNT(*) FROM especie WHERE genero = ? AND especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, genero.trim());
            stmt.setString(2, especie.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * MAP RESULTS
     **/
    private Especie mapResultSetToEspecie(ResultSet rs) throws SQLException {
        Especie especie = new Especie();
        especie.setId_especie(rs.getInt("id_especie"));
        especie.setGenero(rs.getString("genero"));
        especie.setEspecie(rs.getString("especie"));
        return especie;
    }
}
