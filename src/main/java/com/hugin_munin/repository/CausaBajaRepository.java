package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.CausaBaja;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CausaBajaRepository {

    //GetAll
    public List<CausaBaja> getAllCausaBaja() throws SQLException {
        List<CausaBaja> CausaBaja = new ArrayList<>();
        String query = "SELECT * FROM causa_baja ORDER BY id_causa_baja ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CausaBaja CausaBaja1 = new CausaBaja();
                CausaBaja1.idCausaBaja = rs.getInt("id_causa_baja");
                CausaBaja1.nombreCausaBaja = rs.getString("nombre_causa_baja");
                CausaBaja.add(CausaBaja1);
            }
        }
        return CausaBaja;
    }

    //GetById
    public List<CausaBaja> getByIdCausaBaja(int idCausaBaja) throws SQLException {
        List<CausaBaja> causaBajaList = new ArrayList<>();
        String query = "SELECT * FROM causa_baja WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCausaBaja);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    CausaBaja causa = new CausaBaja();
                    causa.idCausaBaja = rs.getInt("id_causa_baja");
                    causa.nombreCausaBaja = rs.getString("nombre_causa_baja");
                    causaBajaList.add(causa);
                }
            }
        }

        return causaBajaList;
    }


    //Update
    public boolean updateCausaBaja(int idCausaBaja, String nombreCausaBaja) throws SQLException {
        String sqlUpdate = "UPDATE causa_baja SET nombre_causa_baja = ? WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {

            stmt.setString(1, nombreCausaBaja);
            stmt.setInt(2, idCausaBaja);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }


}