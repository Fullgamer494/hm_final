package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.OrigenAlta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar los orígenes de alta
 * Maneja todas las operaciones CRUD para la entidad OrigenAlta
 */
public class OrigenAltaRepository {

    /**
     * BUSCAR todos los orígenes de alta
     */
    public List<OrigenAlta> findAll() throws SQLException {
        List<OrigenAlta> origenes = new ArrayList<>();
        String query = "SELECT id_origen_alta, nombre_origen_alta FROM origen_alta ORDER BY id_origen_alta ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrigenAlta origen = mapResultSetToOrigenAlta(rs);
                origenes.add(origen);
            }
        }
        return origenes;
    }

    /**
     * BUSCAR origen de alta por ID
     */
    public Optional<OrigenAlta> findById(Integer id) throws SQLException {
        String query = "SELECT id_origen_alta, nombre_origen_alta FROM origen_alta WHERE id_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrigenAlta(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR origen de alta por nombre
     */
    public Optional<OrigenAlta> findByName(String nombreOrigen) throws SQLException {
        String query = "SELECT id_origen_alta, nombre_origen_alta FROM origen_alta WHERE nombre_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreOrigen.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrigenAlta(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR orígenes por nombre (búsqueda parcial)
     */
    public List<OrigenAlta> findByNameContaining(String nombreOrigen) throws SQLException {
        List<OrigenAlta> origenes = new ArrayList<>();
        String query = "SELECT id_origen_alta, nombre_origen_alta FROM origen_alta WHERE nombre_origen_alta LIKE ? ORDER BY nombre_origen_alta ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombreOrigen + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    origenes.add(mapResultSetToOrigenAlta(rs));
                }
            }
        }
        return origenes;
    }

    /**
     * GUARDAR nuevo origen de alta
     */
    public OrigenAlta save(OrigenAlta origenAlta) throws SQLException {
        String query = "INSERT INTO origen_alta (nombre_origen_alta) VALUES (?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, origenAlta.getNombre_origen_alta());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear origen de alta, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    origenAlta.setId_origen_alta(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear origen de alta, no se obtuvo el ID");
                }
            }
        }

        return origenAlta;
    }

    /**
     * ACTUALIZAR origen de alta existente
     */
    public boolean update(OrigenAlta origenAlta) throws SQLException {
        String query = "UPDATE origen_alta SET nombre_origen_alta = ? WHERE id_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, origenAlta.getNombre_origen_alta());
            stmt.setInt(2, origenAlta.getId_origen_alta());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR origen de alta por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM origen_alta WHERE id_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe un origen de alta por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM origen_alta WHERE id_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si existe un origen de alta por nombre
     */
    public boolean existsByName(String nombreOrigen) throws SQLException {
        String query = "SELECT COUNT(*) FROM origen_alta WHERE nombre_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreOrigen.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de orígenes de alta
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM origen_alta";

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
     * VERIFICAR si el origen está siendo usado en registros de alta
     */
    public boolean isOrigenInUse(Integer idOrigen) throws SQLException {
        String query = "SELECT COUNT(*) FROM registro_alta WHERE id_origen_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idOrigen);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * OBTENER estadísticas de uso por origen
     */
    public List<OrigenEstadistica> getEstadisticasUso() throws SQLException {
        String query = """
            SELECT oa.id_origen_alta, oa.nombre_origen_alta, 
                   COUNT(ra.id_registro_alta) as total_registros,
                   COUNT(CASE WHEN ra.fecha_ingreso >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 END) as registros_ultimo_mes
            FROM origen_alta oa
            LEFT JOIN registro_alta ra ON oa.id_origen_alta = ra.id_origen_alta
            GROUP BY oa.id_origen_alta, oa.nombre_origen_alta
            ORDER BY total_registros DESC
            """;

        List<OrigenEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                OrigenEstadistica estadistica = new OrigenEstadistica(
                        rs.getInt("id_origen_alta"),
                        rs.getString("nombre_origen_alta"),
                        rs.getInt("total_registros"),
                        rs.getInt("registros_ultimo_mes")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    /**
     * MAPEAR ResultSet a objeto OrigenAlta
     */
    private OrigenAlta mapResultSetToOrigenAlta(ResultSet rs) throws SQLException {
        OrigenAlta origen = new OrigenAlta();
        origen.setId_origen_alta(rs.getInt("id_origen_alta"));
        origen.setNombre_origen_alta(rs.getString("nombre_origen_alta"));
        return origen;
    }

    /**
     * Clase auxiliar para estadísticas de origen
     */
    public static class OrigenEstadistica {
        private Integer idOrigen;
        private String nombreOrigen;
        private Integer totalRegistros;
        private Integer registrosUltimoMes;

        public OrigenEstadistica(Integer idOrigen, String nombreOrigen,
                                 Integer totalRegistros, Integer registrosUltimoMes) {
            this.idOrigen = idOrigen;
            this.nombreOrigen = nombreOrigen;
            this.totalRegistros = totalRegistros;
            this.registrosUltimoMes = registrosUltimoMes;
        }

        // Getters y setters
        public Integer getIdOrigen() { return idOrigen; }
        public void setIdOrigen(Integer idOrigen) { this.idOrigen = idOrigen; }

        public String getNombreOrigen() { return nombreOrigen; }
        public void setNombreOrigen(String nombreOrigen) { this.nombreOrigen = nombreOrigen; }

        public Integer getTotalRegistros() { return totalRegistros; }
        public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }

        public Integer getRegistrosUltimoMes() { return registrosUltimoMes; }
        public void setRegistrosUltimoMes(Integer registrosUltimoMes) { this.registrosUltimoMes = registrosUltimoMes; }
    }
}