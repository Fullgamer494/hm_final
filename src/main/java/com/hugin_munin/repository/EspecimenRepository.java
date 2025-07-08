package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Especimen;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EspecimenRepository {
    /**
     * FIND ALL - Obtener todos los especímenes
     **/
    public List<Especimen> findAllSpecimen() throws SQLException {
        List<Especimen> especimenes = new ArrayList<>();
        String query = "SELECT * FROM especimen ORDER BY id_especimen ASC";

        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery()){

            while(rs.next()){
                Especimen especimen = mapResultSetToEspecimenes(rs);
                especimenes.add(especimen);
            }
        }
        return especimenes;
    }

    /**
     *  SAVE - Insertar nuevo espécimen
     **/
    public Especimen saveSpecimen(Especimen especimen) throws SQLException {
        String query = "INSERT INTO especimen (num_inventario, id_especie, nombre_especimen, activo) VALUES (?, ?, ?, ?)";

        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);){

            stmt.setString(1, especimen.getNum_inventario());
            stmt.setObject(2, especimen.getId_especie(), Types.INTEGER);
            stmt.setString(3, especimen.getNombre_especimen());
            stmt.setBoolean(4, especimen.isActivo());

            int rowsAffected = stmt.executeUpdate();

            if(rowsAffected == 0){
                throw new SQLException("Error al crear especimen");
            }

            try(ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if(generatedKeys.next()){
                    Integer id = generatedKeys.getObject(1, Integer.class);
                    especimen.setId_especimen(id);
                }
                else{
                    throw new SQLException("No se pudo obtener el ID del especimen");
                }
            }
        }

        return especimen;
    }

    /**
     * VALIDATE BY ID
     **/
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM especie WHERE id_especie = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setObject(1, id, Types.INTEGER);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VALIDATE BY INVENTARY NUM
     **/
    public boolean existsByIN(String numInv) throws SQLException {
        String query = "SELECT COUNT(*) FROM especimen WHERE num_inventario = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, numInv.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * MAP RESULTS
     **/
    private Especimen mapResultSetToEspecimenes(ResultSet rs) throws SQLException {
        Especimen especimen = new Especimen();
        especimen.setId_especimen(rs.getInt("id_especimen"));
        especimen.setNum_inventario(rs.getString("num_inventario"));
        especimen.setId_especie(rs.getInt("id_especie"));
        especimen.setNombre_especimen(rs.getString("nombre_especimen"));
        especimen.setActivo(rs.getBoolean("activo"));

        return especimen;
    }
}