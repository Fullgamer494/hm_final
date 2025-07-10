package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.RegistroBaja;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class RegistroBajaRepository {

    private static final String BASIC_QUERY = """
        SELECT id_registro_baja, id_especimen, id_causa_baja, id_responsable, 
               fecha_baja, observacion 
        FROM registro_baja
        """;

    public RegistroBaja saveRegister(RegistroBaja registro_baja) throws SQLException {
        String sql = """
            INSERT INTO registro_baja (id_especimen, id_causa_baja, id_responsable, 
                                       fecha_baja, observacion) 
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, registro_baja.getId_especimen());
            stmt.setInt(2, registro_baja.getId_causa_baja());
            stmt.setInt(3, registro_baja.getId_responsable());
            stmt.setDate(4, new java.sql.Date(registro_baja.getFecha_baja().getTime()));
            stmt.setString(5, registro_baja.getObservacion());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("No se pudo insertar registro de baja");

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    registro_baja.setId_registro_baja(rs.getInt(1));
                    return registro_baja;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        }
    }

    public List<RegistroBaja> findAllRegisters() throws SQLException {
        List<RegistroBaja> registros = new ArrayList<>();
        String sql = BASIC_QUERY + " ORDER BY fecha_b
