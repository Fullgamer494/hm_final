package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Permiso;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar permisos
 * Maneja todas las operaciones CRUD para la entidad Permiso
 */
public class PermisoRepository {

    /**
     * BUSCAR todos los permisos
     */
    public List<Permiso> findAll() throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String query = "SELECT id_permiso, nombre_permiso FROM permiso ORDER BY id_permiso ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Permiso permiso = mapResultSetToPermiso(rs);
                permisos.add(permiso);
            }
        }
        return permisos;
    }

    /**
     * BUSCAR permiso por ID
     */
    public Optional<Permiso> findById(Integer id) throws SQLException {
        String query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermiso(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR permiso por nombre
     */
    public Optional<Permiso> findByName(String nombrePermiso) throws SQLException {
        String query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE nombre_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombrePermiso.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPermiso(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * BUSCAR permisos por nombre (búsqueda parcial)
     */
    public List<Permiso> findByNameContaining(String nombrePermiso) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE nombre_permiso LIKE ? ORDER BY nombre_permiso ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombrePermiso + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermiso(rs));
                }
            }
        }
        return permisos;
    }

    /**
     * BUSCAR permisos por categoría
     */
    public List<Permiso> findByCategory(String categoria) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String query;

        switch (categoria.toLowerCase()) {
            case "altas":
                query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE nombre_permiso LIKE '%alta%' ORDER BY nombre_permiso ASC";
                break;
            case "bajas":
                query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE nombre_permiso LIKE '%baja%' ORDER BY nombre_permiso ASC";
                break;
            case "reportes":
                query = "SELECT id_permiso, nombre_permiso FROM permiso WHERE nombre_permiso LIKE '%reporte%' ORDER BY nombre_permiso ASC";
                break;
            default:
                return findAll();
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                permisos.add(mapResultSetToPermiso(rs));
            }
        }
        return permisos;
    }

    /**
     * BUSCAR permisos asignados a un rol
     */
    public List<Permiso> findByRol(Integer idRol) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String query = """
            SELECT p.id_permiso, p.nombre_permiso 
            FROM permiso p
            INNER JOIN rol_permiso rp ON p.id_permiso = rp.id_permiso
            WHERE rp.id_rol = ?
            ORDER BY p.nombre_permiso ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermiso(rs));
                }
            }
        }
        return permisos;
    }

    /**
     * BUSCAR permisos NO asignados a un rol
     */
    public List<Permiso> findNotAssignedToRol(Integer idRol) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String query = """
            SELECT p.id_permiso, p.nombre_permiso 
            FROM permiso p
            WHERE p.id_permiso NOT IN (
                SELECT rp.id_permiso 
                FROM rol_permiso rp 
                WHERE rp.id_rol = ?
            )
            ORDER BY p.nombre_permiso ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permisos.add(mapResultSetToPermiso(rs));
                }
            }
        }
        return permisos;
    }

    /**
     * GUARDAR nuevo permiso
     */
    public Permiso save(Permiso permiso) throws SQLException {
        String query = "INSERT INTO permiso (nombre_permiso) VALUES (?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, permiso.getNombre_permiso());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear permiso, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permiso.setId_permiso(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear permiso, no se obtuvo el ID");
                }
            }
        }

        return permiso;
    }

    /**
     * ACTUALIZAR permiso existente
     */
    public boolean update(Permiso permiso) throws SQLException {
        String query = "UPDATE permiso SET nombre_permiso = ? WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, permiso.getNombre_permiso());
            stmt.setInt(2, permiso.getId_permiso());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR permiso por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM permiso WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe un permiso por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM permiso WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si existe un permiso por nombre
     */
    public boolean existsByName(String nombrePermiso) throws SQLException {
        String query = "SELECT COUNT(*) FROM permiso WHERE nombre_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombrePermiso.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si el permiso está siendo usado en rol_permiso
     */
    public boolean isPermisoInUse(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM rol_permiso WHERE id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de permisos
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM permiso";

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
     * OBTENER estadísticas de uso por permiso
     */
    public List<PermisoEstadistica> getEstadisticasUso() throws SQLException {
        String query = """
            SELECT p.id_permiso, p.nombre_permiso, 
                   COUNT(rp.id_rol) as total_roles_asignados
            FROM permiso p
            LEFT JOIN rol_permiso rp ON p.id_permiso = rp.id_permiso
            GROUP BY p.id_permiso, p.nombre_permiso
            ORDER BY total_roles_asignados DESC, p.nombre_permiso ASC
            """;

        List<PermisoEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PermisoEstadistica estadistica = new PermisoEstadistica(
                        rs.getInt("id_permiso"),
                        rs.getString("nombre_permiso"),
                        rs.getInt("total_roles_asignados")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    /**
     * ASIGNAR permiso a rol
     */
    public boolean assignPermisoToRol(Integer idPermiso, Integer idRol) throws SQLException {
        String query = "INSERT INTO rol_permiso (id_rol, id_permiso) VALUES (?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * REMOVER permiso de rol
     */
    public boolean removePermisoFromRol(Integer idPermiso, Integer idRol) throws SQLException {
        String query = "DELETE FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si un rol tiene un permiso específico
     */
    public boolean rolHasPermiso(Integer idRol, Integer idPermiso) throws SQLException {
        String query = "SELECT COUNT(*) FROM rol_permiso WHERE id_rol = ? AND id_permiso = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * MAPEAR ResultSet a objeto Permiso
     */
    private Permiso mapResultSetToPermiso(ResultSet rs) throws SQLException {
        Permiso permiso = new Permiso();
        permiso.setId_permiso(rs.getInt("id_permiso"));
        permiso.setNombre_permiso(rs.getString("nombre_permiso"));
        return permiso;
    }

    /**
     * Clase auxiliar para estadísticas de permiso
     */
    public static class PermisoEstadistica {
        private Integer idPermiso;
        private String nombrePermiso;
        private Integer totalRolesAsignados;

        public PermisoEstadistica(Integer idPermiso, String nombrePermiso, Integer totalRolesAsignados) {
            this.idPermiso = idPermiso;
            this.nombrePermiso = nombrePermiso;
            this.totalRolesAsignados = totalRolesAsignados;
        }

        // Getters y setters
        public Integer getIdPermiso() { return idPermiso; }
        public void setIdPermiso(Integer idPermiso) { this.idPermiso = idPermiso; }

        public String getNombrePermiso() { return nombrePermiso; }
        public void setNombrePermiso(String nombrePermiso) { this.nombrePermiso = nombrePermiso; }

        public Integer getTotalRolesAsignados() { return totalRolesAsignados; }
        public void setTotalRolesAsignados(Integer totalRolesAsignados) { this.totalRolesAsignados = totalRolesAsignados; }
    }
}