package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.TipoReporte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar tipos de reporte - CORREGIDO
 * Maneja todas las operaciones CRUD para la entidad TipoReporte
 * ELIMINADO: Todas las referencias al campo 'activo' que no existe en BD
 */
public class TipoReporteRepository {

    /**
     * BUSCAR todos los tipos de reporte
     */
    public List<TipoReporte> findAll() throws SQLException {
        List<TipoReporte> tipos = new ArrayList<>();
        String query = "SELECT id_tipo_reporte, nombre_tipo_reporte FROM tipo_reporte ORDER BY id_tipo_reporte ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TipoReporte tipo = mapResultSetToTipoReporte(rs);
                tipos.add(tipo);
            }
        }
        return tipos;
    }

    /**
     * BUSCAR tipo de reporte por ID
     */
    public Optional<TipoReporte> findById(Integer id) throws SQLException {
        String query = "SELECT id_tipo_reporte, nombre_tipo_reporte FROM tipo_reporte WHERE id_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTipoReporte(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR tipo de reporte por nombre
     */
    public Optional<TipoReporte> findByName(String nombre) throws SQLException {
        String query = "SELECT id_tipo_reporte, nombre_tipo_reporte FROM tipo_reporte WHERE nombre_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombre.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTipoReporte(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR tipos por nombre (búsqueda parcial)
     */
    public List<TipoReporte> findByNameContaining(String nombre) throws SQLException {
        List<TipoReporte> tipos = new ArrayList<>();
        String query = "SELECT id_tipo_reporte, nombre_tipo_reporte FROM tipo_reporte WHERE nombre_tipo_reporte LIKE ? ORDER BY nombre_tipo_reporte ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tipos.add(mapResultSetToTipoReporte(rs));
                }
            }
        }
        return tipos;
    }

    /**
     * GUARDAR nuevo tipo de reporte
     */
    public TipoReporte save(TipoReporte tipoReporte) throws SQLException {
        String query = "INSERT INTO tipo_reporte (nombre_tipo_reporte) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, tipoReporte.getNombre_tipo_reporte());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear tipo de reporte, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    tipoReporte.setId_tipo_reporte(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear tipo de reporte, no se obtuvo el ID");
                }
            }
        }

        return tipoReporte;
    }

    /**
     * ACTUALIZAR tipo de reporte existente
     */
    public boolean update(TipoReporte tipoReporte) throws SQLException {
        String query = "UPDATE tipo_reporte SET nombre_tipo_reporte = ?, WHERE id_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tipoReporte.getNombre_tipo_reporte());
            stmt.setInt(2, tipoReporte.getId_tipo_reporte());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR tipo de reporte por ID (eliminación física)
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM tipo_reporte WHERE id_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe un tipo de reporte por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM tipo_reporte WHERE id_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si existe un tipo de reporte por nombre
     */
    public boolean existsByName(String nombre) throws SQLException {
        String query = "SELECT COUNT(*) FROM tipo_reporte WHERE nombre_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombre.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de tipos de reporte
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM tipo_reporte";

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
     * VERIFICAR si el tipo está siendo usado en reportes
     */
    public boolean isTipoInUse(Integer idTipo) throws SQLException {
        String query = "SELECT COUNT(*) FROM reporte WHERE id_tipo_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idTipo);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * MAPEAR ResultSet a objeto TipoReporte
     */
    private TipoReporte mapResultSetToTipoReporte(ResultSet rs) throws SQLException {
        TipoReporte tipo = new TipoReporte();
        tipo.setId_tipo_reporte(rs.getInt("id_tipo_reporte"));
        tipo.setNombre_tipo_reporte(rs.getString("nombre_tipo_reporte"));
        return tipo;
    }
}