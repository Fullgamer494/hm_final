package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Repositorio para gestionar reportes de traslado
 * Maneja todas las operaciones CRUD para la entidad ReporteTraslado
 */
public class ReporteTrasladoRepository {

    // Query básica solo de tabla reporte_traslado
    private static final String BASIC_QUERY = """
        SELECT rt.id_reporte, rt.area_origen, rt.area_destino, 
               rt.ubicacion_origen, rt.ubicacion_destino, rt.motivo
        FROM reporte_traslado rt
        """;

    // Query completa con joins a reporte y demás tablas
    private static final String COMPLETE_QUERY = """
        SELECT rt.id_reporte, rt.area_origen, rt.area_destino,
               rt.ubicacion_origen, rt.ubicacion_destino, rt.motivo,
               
               -- Datos del Reporte padre
               r.id_tipo_reporte, r.id_especimen, r.id_responsable,
               r.asunto, r.contenido, r.fecha_reporte, r.activo,
               
               -- Datos de TipoReporte
               tr.id_tipo_reporte as tr_id_tipo_reporte,
               tr.nombre_tipo_reporte,
               tr.descripcion as tr_descripcion,
               tr.activo as tr_activo,
               
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
               
        FROM reporte_traslado rt
        INNER JOIN reporte r ON rt.id_reporte = r.id_reporte
        LEFT JOIN tipo_reporte tr ON r.id_tipo_reporte = tr.id_tipo_reporte
        LEFT JOIN especimen esp ON r.id_especimen = esp.id_especimen
        LEFT JOIN especie e ON esp.id_especie = e.id_especie
        LEFT JOIN usuario u ON r.id_responsable = u.id_usuario
        LEFT JOIN rol rol ON u.id_rol = rol.id_rol
        """;

    /**
     * GUARDAR nuevo reporte de traslado (transacción completa)
     */
    public ReporteTraslado save(ReporteTraslado reporteTraslado) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar en tabla reporte
            String insertReporteQuery = """
                INSERT INTO reporte (id_tipo_reporte, id_especimen, id_responsable, 
                                   asunto, contenido, fecha_reporte, activo) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

            int reporteId;
            try (PreparedStatement stmt = conn.prepareStatement(insertReporteQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, reporteTraslado.getId_tipo_reporte());
                stmt.setInt(2, reporteTraslado.getId_especimen());
                stmt.setInt(3, reporteTraslado.getId_responsable());
                stmt.setString(4, reporteTraslado.getAsunto());
                stmt.setString(5, reporteTraslado.getContenido());
                stmt.setDate(6, new java.sql.Date(reporteTraslado.getFecha_reporte().getTime()));
                stmt.setBoolean(7, reporteTraslado.isActivo());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Error al crear el reporte padre");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        reporteId = generatedKeys.getInt(1);
                        reporteTraslado.setId_reporte(reporteId);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del reporte");
                    }
                }
            }

            // 2. Insertar en tabla reporte_traslado
            String insertTrasladoQuery = """
                INSERT INTO reporte_traslado (id_reporte, area_origen, area_destino,
                                            ubicacion_origen, ubicacion_destino, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(insertTrasladoQuery)) {
                stmt.setInt(1, reporteId);
                stmt.setString(2, reporteTraslado.getArea_origen());
                stmt.setString(3, reporteTraslado.getArea_destino());
                stmt.setString(4, reporteTraslado.getUbicacion_origen());
                stmt.setString(5, reporteTraslado.getUbicacion_destino());
                stmt.setString(6, reporteTraslado.getMotivo());

                stmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
            return reporteTraslado;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Log error
                }
            }
        }
    }

    /**
     * BUSCAR todos los reportes de traslado
     */
    public List<ReporteTraslado> findAll() throws SQLException {
        String query = COMPLETE_QUERY + " ORDER BY r.fecha_reporte DESC";
        return executeQueryWithJoins(query);
    }

    /**
     * BUSCAR reporte de traslado por ID
     */
    public Optional<ReporteTraslado> findById(Integer id) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.id_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            List<ReporteTraslado> results = executeQueryWithJoins(stmt);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }
    }

    /**
     * BUSCAR reportes de traslado por área origen
     */
    public List<ReporteTraslado> findByAreaOrigen(String areaOrigen) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.area_origen = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, areaOrigen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por área destino
     */
    public List<ReporteTraslado> findByAreaDestino(String areaDestino) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.area_destino = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, areaDestino);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por ubicación origen
     */
    public List<ReporteTraslado> findByUbicacionOrigen(String ubicacionOrigen) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.ubicacion_origen = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ubicacionOrigen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por ubicación destino
     */
    public List<ReporteTraslado> findByUbicacionDestino(String ubicacionDestino) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.ubicacion_destino = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, ubicacionDestino);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por motivo (búsqueda parcial)
     */
    public List<ReporteTraslado> findByMotivoContaining(String motivo) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE rt.motivo LIKE ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + motivo + "%");
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por especimen
     */
    public List<ReporteTraslado> findByEspecimen(Integer idEspecimen) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_especimen = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idEspecimen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por responsable
     */
    public List<ReporteTraslado> findByResponsable(Integer idResponsable) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.id_responsable = ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idResponsable);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR reportes de traslado por rango de fechas
     */
    public List<ReporteTraslado> findByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        String query = COMPLETE_QUERY + " WHERE r.fecha_reporte BETWEEN ? AND ? ORDER BY r.fecha_reporte DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * ACTUALIZAR reporte de traslado (transacción completa)
     */
    public boolean update(ReporteTraslado reporteTraslado) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Actualizar tabla reporte
            String updateReporteQuery = """
                    UPDATE reporte 
                    SET id_tipo_reporte = ?, id_especimen = ?, id_responsable = ?,
                        asunto = ?, contenido = ?, fecha_reporte = ?, activo = ?
                    WHERE id_reporte = ?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(updateReporteQuery)) {
                stmt.setInt(1, reporteTraslado.getId_tipo_reporte());
                stmt.setInt(2, reporteTraslado.getId_especimen());
                stmt.setInt(3, reporteTraslado.getId_responsable());
                stmt.setString(4, reporteTraslado.getAsunto());
                stmt.setString(5, reporteTraslado.getContenido());
                stmt.setDate(6, new java.sql.Date(reporteTraslado.getFecha_reporte().getTime()));
                stmt.setBoolean(7, reporteTraslado.isActivo());
                stmt.setInt(8, reporteTraslado.getId_reporte());

                stmt.executeUpdate();
            }

            // 2. Actualizar tabla reporte_traslado
            String updateTrasladoQuery = """
                    UPDATE reporte_traslado 
                    SET area_origen = ?, area_destino = ?, ubicacion_origen = ?,
                        ubicacion_destino = ?, motivo = ?
                    WHERE id_reporte = ?
                    """;

            try (PreparedStatement stmt = conn.prepareStatement(updateTrasladoQuery)) {
                stmt.setString(1, reporteTraslado.getArea_origen());
                stmt.setString(2, reporteTraslado.getArea_destino());
                stmt.setString(3, reporteTraslado.getUbicacion_origen());
                stmt.setString(4, reporteTraslado.getUbicacion_destino());
                stmt.setString(5, reporteTraslado.getMotivo());
                stmt.setInt(6, reporteTraslado.getId_reporte());

                stmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Log error
                }
            }
        }
    }

    /**
     * ELIMINAR reporte de traslado por ID (transacción completa)
     */
    public boolean deleteById(Integer id) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Eliminar de tabla reporte_traslado
            String deleteTrasladoQuery = "DELETE FROM reporte_traslado WHERE id_reporte = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteTrasladoQuery)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }

            // 2. Eliminar de tabla reporte
            String deleteReporteQuery = "DELETE FROM reporte WHERE id_reporte = ?";
            boolean deleted;
            try (PreparedStatement stmt = conn.prepareStatement(deleteReporteQuery)) {
                stmt.setInt(1, id);
                deleted = stmt.executeUpdate() > 0;
            }

            conn.commit();
            return deleted;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Log error
                }
            }
        }
    }

    /**
     * VERIFICAR si existe reporte de traslado por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM reporte_traslado WHERE id_reporte = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de reportes de traslado
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM reporte_traslado";

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
     * OBTENER áreas más utilizadas como origen
     */
    public List<AreaEstadistica> getAreasOrigenMasUsadas(int limit) throws SQLException {
        String query = """
            SELECT area_origen, COUNT(*) as total_traslados
            FROM reporte_traslado 
            GROUP BY area_origen 
            ORDER BY total_traslados DESC 
            LIMIT ?
            """;

        List<AreaEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AreaEstadistica estadistica = new AreaEstadistica(
                            rs.getString("area_origen"),
                            rs.getInt("total_traslados")
                    );
                    estadisticas.add(estadistica);
                }
            }
        }

        return estadisticas;
    }

    /**
     * OBTENER áreas más utilizadas como destino
     */
    public List<AreaEstadistica> getAreasDestinoMasUsadas(int limit) throws SQLException {
        String query = """
            SELECT area_destino, COUNT(*) as total_traslados
            FROM reporte_traslado 
            GROUP BY area_destino 
            ORDER BY total_traslados DESC 
            LIMIT ?
            """;

        List<AreaEstadistica> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AreaEstadistica estadistica = new AreaEstadistica(
                            rs.getString("area_destino"),
                            rs.getInt("total_traslados")
                    );
                    estadisticas.add(estadistica);
                }
            }
        }

        return estadisticas;
    }

    // MÉTODOS AUXILIARES

    private List<ReporteTraslado> executeQueryWithJoins(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeQueryWithJoins(stmt);
        }
    }

    private List<ReporteTraslado> executeQueryWithJoins(PreparedStatement stmt) throws SQLException {
        List<ReporteTraslado> reportes = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reportes.add(mapCompleteResultSet(rs));
            }
        }

        return reportes;
    }

    /**
     * MAPEO COMPLETO - ReporteTraslado con todas las entidades relacionadas
     */
    private ReporteTraslado mapCompleteResultSet(ResultSet rs) throws SQLException {
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
                tipoReporte.setActivo(rs.getBoolean("tr_activo"));
            }
        } catch (SQLException e) {
            // Si falla el tipo reporte, continuar sin él
        }

        // Crear objeto ReporteTraslado principal
        ReporteTraslado reporteTraslado = new ReporteTraslado();

        // Datos del reporte padre
        reporteTraslado.setId_reporte(rs.getInt("id_reporte"));
        reporteTraslado.setId_tipo_reporte(rs.getInt("id_tipo_reporte"));
        reporteTraslado.setId_especimen(rs.getInt("id_especimen"));
        reporteTraslado.setId_responsable(rs.getInt("id_responsable"));
        reporteTraslado.setAsunto(rs.getString("asunto"));
        reporteTraslado.setContenido(rs.getString("contenido"));
        reporteTraslado.setFecha_reporte(rs.getDate("fecha_reporte"));
        reporteTraslado.setActivo(rs.getBoolean("activo"));

        // Datos específicos de traslado
        reporteTraslado.setArea_origen(rs.getString("area_origen"));
        reporteTraslado.setArea_destino(rs.getString("area_destino"));
        reporteTraslado.setUbicacion_origen(rs.getString("ubicacion_origen"));
        reporteTraslado.setUbicacion_destino(rs.getString("ubicacion_destino"));
        reporteTraslado.setMotivo(rs.getString("motivo"));

        // Asignar objetos relacionados
        reporteTraslado.setTipo_reporte(tipoReporte);
        reporteTraslado.setEspecimen(especimen);
        reporteTraslado.setResponsable(usuario);

        return reporteTraslado;
    }

    /**
     * Clase auxiliar para estadísticas de áreas
     */
    public static class AreaEstadistica {
        private String area;
        private Integer totalTraslados;

        public AreaEstadistica(String area, Integer totalTraslados) {
            this.area = area;
            this.totalTraslados = totalTraslados;
        }

        // Getters y setters
        public String getArea() { return area; }
        public void setArea(String area) { this.area = area; }

        public Integer getTotalTraslados() { return totalTraslados; }
        public void setTotalTraslados(Integer totalTraslados) { this.totalTraslados = totalTraslados; }
    }
}