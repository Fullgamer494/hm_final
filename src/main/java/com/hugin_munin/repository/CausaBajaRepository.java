package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.CausaBaja;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar las causas de baja
 * Maneja todas las operaciones CRUD para la entidad CausaBaja
 */
public class CausaBajaRepository {

    /**
     * BUSCAR todas las causas de baja
     */
    public List<CausaBaja> findAll() throws SQLException {
        List<CausaBaja> causas = new ArrayList<>();
        String query = "SELECT id_causa_baja, nombre_causa_baja FROM causa_baja ORDER BY id_causa_baja ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CausaBaja causa = mapResultSetToCausaBaja(rs);
                causas.add(causa);
            }
        }
        return causas;
    }

    /**
     * BUSCAR causa de baja por ID
     */
    public Optional<CausaBaja> findById(Integer id) throws SQLException {
        String query = "SELECT id_causa_baja, nombre_causa_baja FROM causa_baja WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCausaBaja(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR causa de baja por nombre
     */
    public Optional<CausaBaja> findByName(String nombreCausa) throws SQLException {
        String query = "SELECT id_causa_baja, nombre_causa_baja FROM causa_baja WHERE nombre_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreCausa.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCausaBaja(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR causas por nombre (búsqueda parcial)
     */
    public List<CausaBaja> findByNameContaining(String nombreCausa) throws SQLException {
        List<CausaBaja> causas = new ArrayList<>();
        String query = "SELECT id_causa_baja, nombre_causa_baja FROM causa_baja WHERE nombre_causa_baja LIKE ? ORDER BY nombre_causa_baja ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombreCausa + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    causas.add(mapResultSetToCausaBaja(rs));
                }
            }
        }
        return causas;
    }

    /**
     * GUARDAR nueva causa de baja
     */
    public CausaBaja save(CausaBaja causaBaja) throws SQLException {
        String query = "INSERT INTO causa_baja (nombre_causa_baja) VALUES (?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, causaBaja.getNombre_causa_baja());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear causa de baja, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    causaBaja.setId_causa_baja(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear causa de baja, no se obtuvo el ID");
                }
            }
        }

        return causaBaja;
    }

    /**
     * ACTUALIZAR causa de baja existente
     */
    public boolean update(CausaBaja causaBaja) throws SQLException {
        String query = "UPDATE causa_baja SET nombre_causa_baja = ? WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, causaBaja.getNombre_causa_baja());
            stmt.setInt(2, causaBaja.getId_causa_baja());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR causa de baja por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM causa_baja WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe una causa de baja por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM causa_baja WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si existe una causa de baja por nombre
     */
    public boolean existsByName(String nombreCausa) throws SQLException {
        String query = "SELECT COUNT(*) FROM causa_baja WHERE nombre_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreCausa.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de causas de baja
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM causa_baja";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * VERIFICAR si la causa está siendo usada en registros de baja
     */
    public boolean isCausaInUse(Integer idCausa) throws SQLException {
        String query = "SELECT COUNT(*) FROM registro_baja WHERE id_causa_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idCausa);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * OBTENER estadísticas de uso por causa
     */
    public List<CausaEstadistica> getEstadisticasUso() throws SQLException {
        String query = """
                SELECT cb.id_causa_baja, cb.nombre_causa_baja, 
                       COUNT(rb.id_registro_baja) as total_registros,
                       COUNT(CASE WHEN rb.fecha_baja >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as registros_ultimo_mes
                FROM causa_baja cb
                LEFT JOIN registro_baja rb ON cb.id_causa_baja = rb.id_causa_baja
                GROUP BY cb.id_causa_baja, cb.nombre_causa_baja
                ORDER BY total_registros DESC
                """;

        List<CausaEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CausaEstadistica estadistica = new CausaEstadistica(
                        rs.getInt("id_causa_baja"),
                        rs.getString("nombre_causa_baja"),
                        rs.getInt("total_registros"),
                        rs.getInt("registros_ultimo_mes")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    /**
     * MAPEAR ResultSet a objeto CausaBaja
     */
    private CausaBaja mapResultSetToCausaBaja(ResultSet rs) throws SQLException {
        CausaBaja causa = new CausaBaja();
        causa.setId_causa_baja(rs.getInt("id_causa_baja"));
        causa.setNombre_causa_baja(rs.getString("nombre_causa_baja"));
        return causa;
    }

    /**
     * Clase auxiliar para estadísticas de causa
     */
    public static class CausaEstadistica {
        private Integer idCausa;
        private String nombreCausa;
        private Integer totalRegistros;
        private Integer registrosUltimoMes;

        public CausaEstadistica(Integer idCausa, String nombreCausa,
                                Integer totalRegistros, Integer registrosUltimoMes) {
            this.idCausa = idCausa;
            this.nombreCausa = nombreCausa;
            this.totalRegistros = totalRegistros;
            this.registrosUltimoMes = registrosUltimoMes;
        }

        // Getters y setters
        public Integer getIdCausa() { return idCausa; }
        public void setIdCausa(Integer idCausa) { this.idCausa = idCausa; }

        public String getNombreCausa() { return nombreCausa; }
        public void setNombreCausa(String nombreCausa) { this.nombreCausa = nombreCausa; }

        public Integer getTotalRegistros() { return totalRegistros; }
        public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

        public Integer getRegistrosUltimoMes() { return registrosUltimoMes; }
        public void setRegistrosUltimoMes(Integer registrosUltimoMes) { this.registrosUltimoMes = registrosUltimoMes; }
    }
}