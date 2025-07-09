package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.model.Especimen;
import com.hugin_munin.model.Especie;
import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.model.Usuario;  // ← IMPORT AGREGADO
import com.hugin_munin.model.Rol;      // ← IMPORT AGREGADO

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Repositorio para gestionar los registros de alta
 * CORREGIDO: Imports agregados para Usuario y Rol
 */
public class RegistroAltaRepository {

    // Query base con todos los joins necesarios
    private static final String BASE_QUERY_WITH_JOINS = """
        SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
               ra.fecha_ingreso, ra.procedencia, ra.observacion,
               
               -- Datos de Especimen
               esp.id_especimen as esp_id_especimen, esp.num_inventario, 
               esp.id_especie as esp_id_especie, esp.nombre_especimen, esp.activo as esp_activo,
               
               -- Datos de Especie
               e.id_especie as e_id_especie, e.genero, e.especie,
               
               -- Datos de OrigenAlta
               oa.id_origen_alta as oa_id_origen_alta, oa.nombre_origen_alta,
               
               -- Datos de Usuario (Responsable)
               u.id_usuario, u.id_rol as u_id_rol, u.nombre_usuario, u.apellido_usuario,
               u.correo, u.telefono, u.activo as u_activo,
               
               -- Datos de Rol
               r.id_rol as r_id_rol, r.nombre_rol, r.descripcion as r_descripcion,
               r.activo as r_activo
               
        FROM registro_alta ra
        LEFT JOIN especimen esp ON ra.id_especimen = esp.id_especimen
        LEFT JOIN especie e ON esp.id_especie = e.id_especie
        LEFT JOIN origen_alta oa ON ra.id_origen_alta = oa.id_origen_alta
        LEFT JOIN usuario u ON ra.id_responsable = u.id_usuario
        LEFT JOIN rol r ON u.id_rol = r.id_rol
        """;

    /**
     * GUARDAR nuevo registro
     */
    public RegistroAlta saveRegister(RegistroAlta registroAlta) throws SQLException {
        String sql = """
            INSERT INTO registro_alta (id_especimen, id_origen_alta, id_responsable, 
                                     fecha_ingreso, procedencia, observacion) 
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, registroAlta.getId_especimen());
            stmt.setInt(2, registroAlta.getId_origen_alta());
            stmt.setInt(3, registroAlta.getId_responsable());
            stmt.setDate(4, new java.sql.Date(registroAlta.getFecha_ingreso().getTime()));
            stmt.setString(5, registroAlta.getProcedencia());
            stmt.setString(6, registroAlta.getObservacion());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Error al crear el registro de alta");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    registroAlta.setId_registro_alta(generatedKeys.getInt(1));
                    return registroAlta;
                } else {
                    throw new SQLException("No se pudo obtener el ID del registro de alta");
                }
            }
        }
    }

    /**
     * BUSCAR todos los registros con información completa
     */
    public List<RegistroAlta> findAllRegisters() throws SQLException {
        String sql = BASE_QUERY_WITH_JOINS + " ORDER BY ra.id_registro_alta DESC";
        return executeQueryWithJoins(sql);
    }

    /**
     * BUSCAR registro por ID con información completa
     */
    public Optional<RegistroAlta> findRegistersById(Integer id) throws SQLException {
        String sql = BASE_QUERY_WITH_JOINS + " WHERE ra.id_registro_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            List<RegistroAlta> results = executeQueryWithJoins(stmt);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }
    }

    /**
     * ACTUALIZAR registro existente
     */
    public RegistroAlta updateRegister(RegistroAlta registroAlta) throws SQLException {
        String sql = """
            UPDATE registro_alta 
            SET id_especimen = ?, id_origen_alta = ?, id_responsable = ?,
                fecha_ingreso = ?, procedencia = ?, observacion = ?
            WHERE id_registro_alta = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, registroAlta.getId_especimen());
            stmt.setInt(2, registroAlta.getId_origen_alta());
            stmt.setInt(3, registroAlta.getId_responsable());
            stmt.setDate(4, new java.sql.Date(registroAlta.getFecha_ingreso().getTime()));
            stmt.setString(5, registroAlta.getProcedencia());
            stmt.setString(6, registroAlta.getObservacion());
            stmt.setInt(7, registroAlta.getId_registro_alta());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo actualizar el registro, no existe el ID especificado");
            }

            return registroAlta;
        }
    }

    /**
     * ELIMINAR registro por ID
     */
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM registro_alta WHERE id_registro_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * BUSCAR registros por especimen
     */
    public List<RegistroAlta> findByEspecimen(Integer idEspecimen) throws SQLException {
        String sql = BASE_QUERY_WITH_JOINS + " WHERE ra.id_especimen = ? ORDER BY ra.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEspecimen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR registros por responsable
     */
    public List<RegistroAlta> findByResponsable(Integer idResponsable) throws SQLException {
        String sql = BASE_QUERY_WITH_JOINS + " WHERE ra.id_responsable = ? ORDER BY ra.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idResponsable);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * BUSCAR registros por rango de fechas
     */
    public List<RegistroAlta> findByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = BASE_QUERY_WITH_JOINS +
                " WHERE ra.fecha_ingreso BETWEEN ? AND ? ORDER BY ra.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * VERIFICAR si existe un registro duplicado
     */
    public boolean existsDuplicateByEspecimenAndDate(Integer idEspecimen, Date fecha) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM registro_alta 
            WHERE id_especimen = ? AND DATE(fecha_ingreso) = DATE(?)
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEspecimen);
            stmt.setDate(2, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de registros
     */
    public int countTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM registro_alta";

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
     * CONTAR registros por mes
     */
    public int countByMonth(int year, int month) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM registro_alta 
            WHERE YEAR(fecha_ingreso) = ? AND MONTH(fecha_ingreso) = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);
            stmt.setInt(2, month);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * OBTENER estadísticas por origen de alta
     */
    public List<EstadisticaOrigen> getEstadisticasPorOrigen() throws SQLException {
        String sql = """
            SELECT oa.id_origen_alta, oa.nombre_origen_alta, COUNT(ra.id_registro_alta) as total
            FROM origen_alta oa
            LEFT JOIN registro_alta ra ON oa.id_origen_alta = ra.id_origen_alta
            GROUP BY oa.id_origen_alta, oa.nombre_origen_alta
            ORDER BY total DESC
            """;

        List<EstadisticaOrigen> estadisticas = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                EstadisticaOrigen estadistica = new EstadisticaOrigen(
                        rs.getInt("id_origen_alta"),
                        rs.getString("nombre_origen_alta"),
                        rs.getInt("total")
                );
                estadisticas.add(estadistica);
            }
        }

        return estadisticas;
    }

    /**
     * Método auxiliar para ejecutar consultas con joins
     */
    private List<RegistroAlta> executeQueryWithJoins(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * Ejecutar consulta con PreparedStatement y mapear resultados completos
     */
    private List<RegistroAlta> executeQueryWithJoins(PreparedStatement stmt) throws SQLException {
        List<RegistroAlta> registros = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                registros.add(mapCompleteResultSet(rs));
            }
        }

        return registros;
    }

    /**
     * Mapear ResultSet completo con todas las relaciones
     */
    private RegistroAlta mapCompleteResultSet(ResultSet rs) throws SQLException {
        // Crear objeto Rol
        Rol rol = null;
        if (rs.getObject("r_id_rol") != null) {
            rol = new Rol();
            rol.setId_rol(rs.getInt("r_id_rol"));
            rol.setNombre_rol(rs.getString("nombre_rol"));
            rol.setDescripcion(rs.getString("r_descripcion"));
            rol.setActivo(rs.getBoolean("r_activo"));
        }

        // Crear objeto Usuario (Responsable)
        Usuario usuario = null;
        if (rs.getObject("id_usuario") != null) {
            usuario = new Usuario();
            usuario.setId_usuario(rs.getInt("id_usuario"));
            usuario.setId_rol(rs.getInt("u_id_rol"));
            usuario.setRol(rol);
            usuario.setNombre_usuario(rs.getString("nombre_usuario"));
            usuario.setCorreo(rs.getString("correo"));
            usuario.setActivo(rs.getBoolean("u_activo"));
        }

        // Crear objeto Especie
        Especie especie = null;
        if (rs.getObject("e_id_especie") != null) {
            especie = new Especie();
            especie.setId_especie(rs.getInt("e_id_especie"));
            especie.setGenero(rs.getString("genero"));
            especie.setEspecie(rs.getString("especie"));
        }

        // Crear objeto Especimen
        Especimen especimen = null;
        if (rs.getObject("esp_id_especimen") != null) {
            especimen = new Especimen();
            especimen.setId_especimen(rs.getInt("esp_id_especimen"));
            especimen.setNum_inventario(rs.getString("num_inventario"));
            especimen.setId_especie(rs.getInt("esp_id_especie"));
            especimen.setEspecie(especie);
            especimen.setNombre_especimen(rs.getString("nombre_especimen"));
            especimen.setActivo(rs.getBoolean("esp_activo"));
        }

        // Crear objeto OrigenAlta
        OrigenAlta origenAlta = null;
        if (rs.getObject("oa_id_origen_alta") != null) {
            origenAlta = new OrigenAlta();
            origenAlta.setId_origen_alta(rs.getInt("oa_id_origen_alta"));
            origenAlta.setNombre_origen_alta(rs.getString("nombre_origen_alta"));
        }

        // Crear objeto RegistroAlta con todas las relaciones
        RegistroAlta registro = new RegistroAlta(
                rs.getInt("id_registro_alta"),
                rs.getInt("id_especimen"),
                especimen,
                rs.getInt("id_origen_alta"),
                origenAlta,
                rs.getInt("id_responsable"),
                usuario,
                rs.getDate("fecha_ingreso"),
                rs.getString("procedencia"),
                rs.getString("observacion")
        );

        return registro;
    }

    /**
     * Clase auxiliar para estadísticas por origen
     */
    public static class EstadisticaOrigen {
        private Integer idOrigenAlta;
        private String nombreOrigenAlta;
        private Integer totalRegistros;

        public EstadisticaOrigen(Integer idOrigenAlta, String nombreOrigenAlta, Integer totalRegistros) {
            this.idOrigenAlta = idOrigenAlta;
            this.nombreOrigenAlta = nombreOrigenAlta;
            this.totalRegistros = totalRegistros;
        }

        // Getters y setters
        public Integer getIdOrigenAlta() { return idOrigenAlta; }
        public void setIdOrigenAlta(Integer idOrigenAlta) { this.idOrigenAlta = idOrigenAlta; }

        public String getNombreOrigenAlta() { return nombreOrigenAlta; }
        public void setNombreOrigenAlta(String nombreOrigenAlta) { this.nombreOrigenAlta = nombreOrigenAlta; }

        public Integer getTotalRegistros() { return totalRegistros; }
        public void setTotalRegistros(Integer totalRegistros) { this.totalRegistros = totalRegistros; }
    }
}