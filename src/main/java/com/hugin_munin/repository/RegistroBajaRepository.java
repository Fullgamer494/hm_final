package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Repositorio para gestionar los registros de baja
 * VERSIÓN CORREGIDA - Fix en el mapeo de joins
 */
public class RegistroBajaRepository {

    // Query básica sin joins - SIEMPRE FUNCIONA
    private static final String BASIC_QUERY = """
        SELECT rb.id_registro_baja, rb.id_especimen, rb.id_causa_baja, rb.id_responsable,
               rb.fecha_baja, rb.observacion
        FROM registro_baja rb
        """;

    // Query con joins completos - CORREGIDA
    private static final String SAFE_QUERY_WITH_JOINS = """
        SELECT rb.id_registro_baja, rb.id_especimen, rb.id_causa_baja, rb.id_responsable,
               rb.fecha_baja, rb.observacion,
               
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
               
               -- Datos de CausaBaja
               cb.id_causa_baja as cb_id_causa_baja, 
               cb.nombre_causa_baja,
               
               -- Datos de Usuario
               u.id_usuario, 
               u.id_rol as u_id_rol,
               u.nombre_usuario,
               u.correo,
               u.activo as u_activo,
               
               -- Datos de Rol
               r.id_rol as r_id_rol,
               r.nombre_rol
               
        FROM registro_baja rb
        LEFT JOIN especimen esp ON rb.id_especimen = esp.id_especimen
        LEFT JOIN especie e ON esp.id_especie = e.id_especie
        LEFT JOIN causa_baja cb ON rb.id_causa_baja = cb.id_causa_baja
        LEFT JOIN usuario u ON rb.id_responsable = u.id_usuario
        LEFT JOIN rol r ON u.id_rol = r.id_rol
        """;

    /**
     * GUARDAR nuevo registro de baja (y marcar especimen como inactivo)
     */
    public RegistroBaja saveRegister(RegistroBaja registroBaja) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Insertar registro de baja
            String insertSql = """
                INSERT INTO registro_baja (id_especimen, id_causa_baja, id_responsable, 
                                         fecha_baja, observacion) 
                VALUES (?, ?, ?, ?, ?)
                """;

            try (PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, registroBaja.getId_especimen());
                stmt.setInt(2, registroBaja.getId_causa_baja());
                stmt.setInt(3, registroBaja.getId_responsable());
                stmt.setDate(4, new java.sql.Date(registroBaja.getFecha_baja().getTime()));
                stmt.setString(5, registroBaja.getObservacion());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Error al crear el registro de baja");
                }

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        registroBaja.setId_registro_baja(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("No se pudo obtener el ID del registro de baja");
                    }
                }
            }

            // 2. Marcar especimen como inactivo
            String updateEspecimenSql = "UPDATE especimen SET activo = FALSE WHERE id_especimen = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateEspecimenSql)) {
                stmt.setInt(1, registroBaja.getId_especimen());
                stmt.executeUpdate();
            }

            conn.commit(); // Confirmar transacción
            return registroBaja;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir cambios en caso de error
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
     * BUSCAR todos los registros - VERSION SEGURA
     */
    public List<RegistroBaja> findAllRegisters() throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " ORDER BY rb.fecha_baja DESC";
        return executeQueryWithJoins(sql);
    }

    /**
     * BUSCAR registro por ID
     */
    public Optional<RegistroBaja> findRegistersById(Integer id) throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " WHERE rb.id_registro_baja = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            List<RegistroBaja> results = executeQueryWithJoins(stmt);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }
    }

    /**
     * ACTUALIZAR registro existente
     */
    public RegistroBaja updateRegister(RegistroBaja registroBaja) throws SQLException {
        String sql = """
            UPDATE registro_baja 
            SET id_especimen = ?, id_causa_baja = ?, id_responsable = ?,
                fecha_baja = ?, observacion = ?
            WHERE id_registro_baja = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registroBaja.getId_especimen());
            stmt.setInt(2, registroBaja.getId_causa_baja());
            stmt.setInt(3, registroBaja.getId_responsable());
            stmt.setDate(4, new java.sql.Date(registroBaja.getFecha_baja().getTime()));
            stmt.setString(5, registroBaja.getObservacion());
            stmt.setInt(6, registroBaja.getId_registro_baja());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el registro, no existe el ID especificado");
            }

            return registroBaja;
        }
    }

    /**
     * ELIMINAR registro por ID (y reactivar especimen)
     */
    public boolean delete(Integer id) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Obtener ID del especimen antes de eliminar
            Integer idEspecimen = null;
            String selectSql = "SELECT id_especimen FROM registro_baja WHERE id_registro_baja = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectSql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        idEspecimen = rs.getInt("id_especimen");
                    }
                }
            }

            if (idEspecimen == null) {
                return false; // No existe el registro
            }

            // 2. Eliminar registro de baja
            String deleteSql = "DELETE FROM registro_baja WHERE id_registro_baja = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
                stmt.setInt(1, id);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    return false;
                }
            }

            // 3. Reactivar especimen
            String updateEspecimenSql = "UPDATE especimen SET activo = TRUE WHERE id_especimen = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateEspecimenSql)) {
                stmt.setInt(1, idEspecimen);
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
     * BUSCAR registros por especimen
     */
    public List<RegistroBaja> findByEspecimen(Integer idEspecimen) throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " WHERE rb.id_especimen = ? ORDER BY rb.fecha_baja DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEspecimen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR registros por causa de baja
     */
    public List<RegistroBaja> findByCausaBaja(Integer idCausaBaja) throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " WHERE rb.id_causa_baja = ? ORDER BY rb.fecha_baja DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCausaBaja);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR registros por responsable
     */
    public List<RegistroBaja> findByResponsable(Integer idResponsable) throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " WHERE rb.id_responsable = ? ORDER BY rb.fecha_baja DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idResponsable);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR registros por rango de fechas
     */
    public List<RegistroBaja> findByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = SAFE_QUERY_WITH_JOINS + " WHERE rb.fecha_baja BETWEEN ? AND ? ORDER BY rb.fecha_baja DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * VERIFICAR si existe un registro para un especimen
     */
    public boolean existsByEspecimen(Integer idEspecimen) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registro_baja WHERE id_especimen = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEspecimen);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de registros
     */
    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM registro_baja";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * OBTENER estadísticas por causa de baja
     */
    public List<EstadisticaCausa> getEstadisticasPorCausa() throws SQLException {
        String sql = """
            SELECT cb.id_causa_baja, cb.nombre_causa_baja, COUNT(rb.id_registro_baja) as total
            FROM causa_baja cb
            LEFT JOIN registro_baja rb ON cb.id_causa_baja = rb.id_causa_baja
            GROUP BY cb.id_causa_baja, cb.nombre_causa_baja
            ORDER BY total DESC
            """;

        List<EstadisticaCausa> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EstadisticaCausa estadistica = new EstadisticaCausa(
                        rs.getInt("id_causa_baja"),
                        rs.getString("nombre_causa_baja"),
                        rs.getInt("total")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    // MÉTODOS AUXILIARES

    private List<RegistroBaja> executeQueryWithJoins(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeQueryWithJoins(stmt);
        }
    }

    private List<RegistroBaja> executeQueryWithJoins(PreparedStatement stmt) throws SQLException {
        List<RegistroBaja> registros = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                registros.add(mapCompleteResultSet(rs));
            }
        }

        return registros;
    }

    /**
     * MAPEO CORREGIDO - El problema principal estaba aquí
     */
    private RegistroBaja mapCompleteResultSet(ResultSet rs) throws SQLException {
        // Crear objeto Rol CORREGIDO
        Rol rol = null;
        try {
            Integer rolId = rs.getObject("r_id_rol", Integer.class);
            if (rolId != null) {
                rol = new Rol();
                rol.setId_rol(rolId);
                rol.setNombre_rol(rs.getString("nombre_rol"));
                rol.setDescripcion(rs.getString("r_descripcion"));
                rol.setActivo(rs.getBoolean("r_activo"));
            }
        } catch (SQLException e) {
            // Si falla el rol, continuar sin él
            System.err.println("Error mapeando rol: " + e.getMessage());
        }

        // Crear objeto Usuario CORREGIDO
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
                usuario.setRol(rol); // Asignar rol
            }
        } catch (SQLException e) {
            System.err.println("Error mapeando usuario: " + e.getMessage());
        }

        // Crear objeto Especie CORREGIDO
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
            System.err.println("Error mapeando especie: " + e.getMessage());
        }

        // Crear objeto Especimen CORREGIDO
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
                especimen.setEspecie(especie); // Asignar especie
            }
        } catch (SQLException e) {
            System.err.println("Error mapeando especimen: " + e.getMessage());
        }

        // Crear objeto CausaBaja CORREGIDO
        CausaBaja causaBaja = null;
        try {
            Integer causaId = rs.getObject("cb_id_causa_baja", Integer.class);
            if (causaId != null) {
                causaBaja = new CausaBaja();
                causaBaja.setId_causa_baja(causaId);
                causaBaja.setNombre_causa_baja(rs.getString("nombre_causa_baja"));
            }
        } catch (SQLException e) {
            System.err.println("Error mapeando causa baja: " + e.getMessage());
        }

        // Crear objeto RegistroBaja principal
        RegistroBaja registro = new RegistroBaja();
        registro.setId_registro_baja(rs.getInt("id_registro_baja"));
        registro.setId_especimen(rs.getInt("id_especimen"));
        registro.setId_causa_baja(rs.getInt("id_causa_baja"));
        registro.setId_responsable(rs.getInt("id_responsable"));
        registro.setFecha_baja(rs.getDate("fecha_baja"));
        registro.setObservacion(rs.getString("observacion"));

        // Asignar objetos relacionados
        registro.setEspecimen(especimen);
        registro.setCausa_baja(causaBaja);
        registro.setResponsable(usuario);

        return registro;
    }

    /**
     * Clase auxiliar para estadísticas por causa
     */
    public static class EstadisticaCausa {
        private Integer idCausaBaja;
        private String nombreCausaBaja;
        private Integer totalRegistros;

        public EstadisticaCausa(Integer idCausaBaja, String nombreCausaBaja, Integer totalRegistros) {
            this.idCausaBaja = idCausaBaja;
            this.nombreCausaBaja = nombreCausaBaja;
            this.totalRegistros = totalRegistros;
        }

        // Getters y setters
        public Integer getIdCausaBaja() { return idCausaBaja; }
        public void setIdCausaBaja(Integer idCausaBaja) { this.idCausaBaja = idCausaBaja; }

        public String getNombreCausaBaja() { return nombreCausaBaja; }
        public void setNombreCausaBaja(String nombreCausaBaja) { this.nombreCausaBaja = nombreCausaBaja; }

        public Integer getTotalRegistros() { return totalRegistros; }
        public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }
    }
}