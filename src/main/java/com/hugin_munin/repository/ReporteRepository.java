package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Repositorio para gestionar reportes - CORREGIDO
 * Maneja todas las operaciones CRUD para la entidad Reporte
 * CORREGIDO: Eliminado campo 'activo' y ajustado manejo de fechas
 */
public class ReporteRepository {

    // Query básica sin joins
    private static final String BASIC_QUERY = """
            SELECT r.id_reporte, r.id_tipo_reporte, r.id_especimen, r.id_responsable,
                   r.asunto, r.contenido, r.fecha_reporte
            FROM reporte r
            """;

    // Query con joins completos CORREGIDA
    private static final String COMPLETE_QUERY = """
            SELECT r.id_reporte, r.id_tipo_reporte, r.id_especimen, r.id_responsable,
                   r.asunto, r.contenido, r.fecha_reporte,
                     
                   -- Datos de TipoReporte
                   tr.id_tipo_reporte as tr_id_tipo_reporte,
                   tr.nombre_tipo_reporte,
                   tr.descripcion as tr_descripcion,
                     
                   -- Datos de Especimen
                   esp.id_especimen as esp_id_especimen,
                   esp.num_inventario,
                   esp.id_especie as esp_id_especie,
                   esp.nombre_especimen,
                   esp.activo as esp_activo,
                     
                   -- Datos de Especie
                   e.id_especie as e_id_especie,
                   e.genero,
                   e.especie,
                     
                   -- Datos de Usuario
                   u.id_usuario,
                   u.id_rol as u_id_rol,
                   u.nombre_usuario,
                   u.correo,
                   u.activo as u_activo,
                     
                   -- Datos de Rol
                   rol.id_rol as rol_id_rol,
                   rol.nombre_rol
                     
            FROM reporte r
            LEFT JOIN tipo_reporte tr ON r.id_tipo_reporte = tr.id_tipo_reporte
            LEFT JOIN especimen esp ON r.id_especimen = esp.id_especimen
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            LEFT JOIN usuario u ON r.id_responsable = u.id_usuario
            LEFT JOIN rol rol ON u.id_rol = rol.id_rol
            """;

    /**
     * GUARDAR nuevo reporte
     */
    public Reporte save(Reporte reporte) throws SQLException {
        String query = """
            INSERT INTO reporte (id_tipo_reporte, id_especimen, id_responsable, 
                               asunto, contenido, fecha_reporte) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reporte.getId_tipo_reporte());
            stmt.setInt(2, reporte.getId_especimen());
            stmt.setInt(3, reporte.getId_responsable());
            stmt.setString(4, reporte.getAsunto());
            stmt.setString(5, reporte.getContenido());
            stmt.setTimestamp(6, new java.sql.Timestamp(reporte.getFecha_reporte().getTime()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al crear el reporte");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reporte.setId_reporte(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("No se pudo obtener el ID del reporte");
                }
            }
        }

        return reporte;
    }

    /**
     * BUSCAR todos los reportes
     */
    public List<Reporte> findAll() throws SQLException {
        String query = COMPLETE_QUERY + " ORDER BY r.fecha_reporte DESC";
        return executeQueryWithJoins(query);
    }

    /**
     * BUSCAR reporte por ID
     */
    public Optional<Reporte> findById(Integer id) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            List<Reporte> results = executeQueryWithJoins(stmt);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }
    }

    /**
     * BUSCAR reportes por tipo
     */
    public List<Reporte> findByTipoReporte(Integer idTipoReporte) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_tipo_reporte = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idTipoReporte);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes por especimen
     */
    public List<Reporte> findByEspecimen(Integer idEspecimen) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_especimen = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEspecimen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes por responsable
     */
    public List<Reporte> findByResponsable(Integer idResponsable) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_responsable = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idResponsable);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes por asunto (búsqueda parcial)
     */
    public List<Reporte> findByAsuntoContaining(String asunto) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.asunto LIKE ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + asunto + "%");
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes por contenido (búsqueda parcial)
     */
    public List<Reporte> findByContenidoContaining(String contenido) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.contenido LIKE ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + contenido + "%");
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes por rango de fechas
     */
    public List<Reporte> findByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.fecha_reporte BETWEEN ? AND ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setTimestamp(1, new java.sql.Timestamp(fechaInicio.getTime()));
            stmt.setTimestamp(2, new java.sql.Timestamp(fechaFin.getTime()));
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * ACTUALIZAR reporte existente
     */
    public boolean update(Reporte reporte) throws SQLException {
        String query = """
            UPDATE reporte 
            SET id_tipo_reporte = ?, id_especimen = ?, id_responsable = ?,
                asunto = ?, contenido = ?, fecha_reporte = ?
            WHERE id_reporte = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reporte.getId_tipo_reporte());
            stmt.setInt(2, reporte.getId_especimen());
            stmt.setInt(3, reporte.getId_responsable());
            stmt.setString(4, reporte.getAsunto());
            stmt.setString(5, reporte.getContenido());
            stmt.setTimestamp(6, new java.sql.Timestamp(reporte.getFecha_reporte().getTime()));
            stmt.setInt(7, reporte.getId_reporte());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR reporte por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM reporte WHERE id_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe reporte por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM reporte WHERE id_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de reportes
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM reporte";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // MÉTODOS AUXILIARES

    private List<Reporte> executeQueryWithJoins(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeQueryWithJoins(stmt);
        }
    }

    private List<Reporte> executeQueryWithJoins(PreparedStatement stmt) throws SQLException {
        List<Reporte> reportes = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reportes.add(mapCompleteResultSet(rs));
            }
        }

        return reportes;
    }

    /**
     * MAPEO COMPLETO con todas las entidades relacionadas
     */
    private Reporte mapCompleteResultSet(ResultSet rs) throws SQLException {
        // Crear objeto Rol
        Rol rol = null;
        try {
            Integer rolId = rs.getObject("rol_id_rol", Integer.class);
            if (rolId != null) {
                rol = new Rol();
                rol.setId_rol(rolId);
                rol.setNombre_rol(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            // Si falla el rol, continuar sin él
        }

        // Crear objeto Usuario
        Usuario usuario = null;
        try {
            Integer usuarioId = rs.getObject("id_usuario", Integer.class);
            if (usuarioId != null) {
                usuario = new Usuario();
                usuario.setId_usuario(usuarioId);
                usuario.setId_rol(rs.getObject("u_id_rol", Integer.class));
                usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setActivo(rs.getBoolean("u_activo"));
                usuario.setRol(rol);
            }
        } catch (SQLException e) {
            // Si falla el usuario, continuar sin él
        }

        // Crear objeto Especie
        Especie especie = null;
        try {
            Integer especieId = rs.getObject("e_id_especie", Integer.class);
            if (especieId != null) {
                especie = new Especie();
                especie.setId_especie(especieId);
                especie.setGenero(rs.getString("genero"));
                especie.setEspecie(rs.getString("especie"));
            }
        } catch (SQLException e) {
            // Si falla la especie, continuar sin ella
        }

        // Crear objeto Especimen
        Especimen especimen = null;
        try {
            Integer especimenId = rs.getObject("esp_id_especimen", Integer.class);
            if (especimenId != null) {
                especimen = new Especimen();
                especimen.setId_especimen(especimenId);
                especimen.setNum_inventario(rs.getString("num_inventario"));
                especimen.setId_especie(rs.getObject("esp_id_especie", Integer.class));
                especimen.setNombre_especimen(rs.getString("nombre_especimen"));
                especimen.setActivo(rs.getBoolean("esp_activo"));
                especimen.setEspecie(especie);
            }
        } catch (SQLException e) {
            // Si falla el especimen, continuar sin él
        }

        // Crear objeto TipoReporte
        TipoReporte tipoReporte = null;
        try {
            Integer tipoId = rs.getObject("tr_id_tipo_reporte", Integer.class);
            if (tipoId != null) {
                tipoReporte = new TipoReporte();
                tipoReporte.setId_tipo_reporte(tipoId);
                tipoReporte.setNombre_tipo_reporte(rs.getString("nombre_tipo_reporte"));
                tipoReporte.setDescripcion(rs.getString("tr_descripcion"));
            }
        } catch (SQLException e) {
            // Si falla el tipo reporte, continuar sin él
        }

        // Crear objeto Reporte principal
        Reporte reporte = new Reporte();
        reporte.setId_reporte(rs.getInt("id_reporte"));
        reporte.setId_tipo_reporte(rs.getInt("id_tipo_reporte"));
        reporte.setId_especimen(rs.getInt("id_especimen"));
        reporte.setId_responsable(rs.getInt("id_responsable"));
        reporte.setAsunto(rs.getString("asunto"));
        reporte.setContenido(rs.getString("contenido"));
        reporte.setFecha_reporte(rs.getTimestamp("fecha_reporte"));

        // Asignar objetos relacionados
        reporte.setTipo_reporte(tipoReporte);
        reporte.setEspecimen(especimen);
        reporte.setResponsable(usuario);

        return reporte;
    }
}