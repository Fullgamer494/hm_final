package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.model.Especimen;
import com.hugin_munin.model.Especie;
import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.model.Usuario;
import com.hugin_munin.model.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Date;

/**
 * Repositorio para gestionar los registros de alta
 * VERSIÓN A PRUEBA DE ERRORES - Maneja columnas faltantes dinámicamente
 */
public class RegistroAltaRepository {

    // Query básica sin joins - SIEMPRE FUNCIONA
    private static final String BASIC_QUERY = """
        SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
               ra.fecha_ingreso, ra.procedencia, ra.observacion
        FROM registro_alta ra
        """;

    // Query con joins mínimos - solo campos que seguramente existen
    private static final String SAFE_QUERY_WITH_JOINS = """
        SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
               ra.fecha_ingreso, ra.procedencia, ra.observacion,
               
               -- Datos de Especimen (campos básicos)
               esp.id_especimen as esp_id_especimen, 
               esp.num_inventario, 
               esp.id_especie as esp_id_especie, 
               esp.nombre_especimen,
               
               -- Datos de Especie (campos básicos)
               e.id_especie as e_id_especie, 
               e.genero, 
               e.especie,
               
               -- Datos de OrigenAlta (campos básicos)
               oa.id_origen_alta as oa_id_origen_alta, 
               oa.nombre_origen_alta,
               
               -- Datos de Usuario (solo campos esenciales)
               u.id_usuario, 
               u.nombre_usuario,
               u.correo,
               
               -- Datos de Rol (solo campo esencial)
               r.id_rol as r_id_rol,
               r.nombre_rol
               
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
     * BUSCAR todos los registros - VERSION SEGURA
     */
    public List<RegistroAlta> findAllRegisters() throws SQLException {
        // Intentar primero con joins, si falla usar query básica
        try {
            String sql = SAFE_QUERY_WITH_JOINS + " ORDER BY ra.id_registro_alta DESC";
            return executeQueryWithJoins(sql);
        } catch (SQLException e) {
            System.err.println("⚠️ Error con joins, usando query básica: " + e.getMessage());
            return findAllRegistersBasic();
        }
    }

    /**
     * BUSCAR todos los registros - VERSION BÁSICA (sin joins)
     */
    public List<RegistroAlta> findAllRegistersBasic() throws SQLException {
        List<RegistroAlta> registros = new ArrayList<>();
        String sql = BASIC_QUERY + " ORDER BY ra.id_registro_alta DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                registros.add(mapBasicResultSet(rs));
            }
        }
        return registros;
    }

    /**
     * BUSCAR registro por ID - VERSION SEGURA
     */
    public Optional<RegistroAlta> findRegistersById(Integer id) throws SQLException {
        // Intentar primero con joins
        try {
            String sql = SAFE_QUERY_WITH_JOINS + " WHERE ra.id_registro_alta = ?";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, id);
                List<RegistroAlta> results = executeQueryWithJoins(stmt);
                return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error con joins, usando query básica: " + e.getMessage());
            return findRegisterByIdBasic(id);
        }
    }

    /**
     * BUSCAR registro por ID - VERSION BÁSICA
     */
    public Optional<RegistroAlta> findRegisterByIdBasic(Integer id) throws SQLException {
        String sql = BASIC_QUERY + " WHERE ra.id_registro_alta = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBasicResultSet(rs));
                }
            }
        }
        return Optional.empty();
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
        try {
            String sql = SAFE_QUERY_WITH_JOINS + " WHERE ra.id_especimen = ? ORDER BY ra.fecha_ingreso DESC";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, idEspecimen);
                return executeQueryWithJoins(stmt);
            }
        } catch (SQLException e) {
            // Fallback a query básica
            String sql = BASIC_QUERY + " WHERE ra.id_especimen = ? ORDER BY ra.fecha_ingreso DESC";

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, idEspecimen);
                List<RegistroAlta> registros = new ArrayList<>();

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        registros.add(mapBasicResultSet(rs));
                    }
                }
                return registros;
            }
        }
    }

    /**
     * BUSCAR registros por responsable
     */
    public List<RegistroAlta> findByResponsable(Integer idResponsable) throws SQLException {
        String sql = BASIC_QUERY + " WHERE ra.id_responsable = ? ORDER BY ra.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idResponsable);
            List<RegistroAlta> registros = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapBasicResultSet(rs));
                }
            }
            return registros;
        }
    }

    /**
     * BUSCAR registros por rango de fechas
     */
    public List<RegistroAlta> findByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = BASIC_QUERY + " WHERE ra.fecha_ingreso BETWEEN ? AND ? ORDER BY ra.fecha_ingreso DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));

            List<RegistroAlta> registros = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapBasicResultSet(rs));
                }
            }
            return registros;
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
                registros.add(mapSafeResultSet(rs));
            }
        }

        return registros;
    }

    /**
     * Mapear ResultSet básico - SOLO tabla registro_alta
     */
    private RegistroAlta mapBasicResultSet(ResultSet rs) throws SQLException {
        RegistroAlta registro = new RegistroAlta();
        registro.setId_registro_alta(rs.getInt("id_registro_alta"));
        registro.setId_especimen(rs.getInt("id_especimen"));
        registro.setId_origen_alta(rs.getInt("id_origen_alta"));
        registro.setId_responsable(rs.getInt("id_responsable"));
        registro.setFecha_ingreso(rs.getDate("fecha_ingreso"));
        registro.setProcedencia(rs.getString("procedencia"));
        registro.setObservacion(rs.getString("observacion"));

        return registro;
    }

    /**
     * Mapear ResultSet SEGURO - Maneja campos que pueden no existir
     */
    private RegistroAlta mapSafeResultSet(ResultSet rs) throws SQLException {
        // Crear objeto Rol con manejo defensivo
        Rol rol = null;
        try {
            if (rs.getObject("r_id_rol") != null) {
                rol = new Rol();
                rol.setId_rol(rs.getInt("r_id_rol"));
                rol.setNombre_rol(rs.getString("nombre_rol"));

                // Manejar campos opcionales
                try {
                    rol.setDescripcion(rs.getString("r_descripcion"));
                } catch (SQLException e) {
                    rol.setDescripcion(null);
                }

                try {
                    rol.setActivo(rs.getBoolean("r_activo"));
                } catch (SQLException e) {
                    rol.setActivo(true); // Valor por defecto
                }
            }
        } catch (SQLException e) {
            // Si hay problema con rol, continuar sin él
            rol = null;
        }

        // Crear objeto Usuario con manejo defensivo
        Usuario usuario = null;
        try {
            if (rs.getObject("id_usuario") != null) {
                usuario = new Usuario();
                usuario.setId_usuario(rs.getInt("id_usuario"));
                usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setRol(rol);

                // Manejar campos opcionales
                try {
                    usuario.setId_rol(rs.getInt("u_id_rol"));
                } catch (SQLException e) {
                    if (rol != null) {
                        usuario.setId_rol(rol.getId_rol());
                    }
                }

                try {
                    usuario.setActivo(rs.getBoolean("u_activo"));
                } catch (SQLException e) {
                    usuario.setActivo(true);
                }
            }
        } catch (SQLException e) {
            usuario = null;
        }

        // Crear objeto Especie
        Especie especie = null;
        try {
            if (rs.getObject("e_id_especie") != null) {
                especie = new Especie();
                especie.setId_especie(rs.getInt("e_id_especie"));
                especie.setGenero(rs.getString("genero"));
                especie.setEspecie(rs.getString("especie"));
            }
        } catch (SQLException e) {
            especie = null;
        }

        // Crear objeto Especimen
        Especimen especimen = null;
        try {
            if (rs.getObject("esp_id_especimen") != null) {
                especimen = new Especimen();
                especimen.setId_especimen(rs.getInt("esp_id_especimen"));
                especimen.setNum_inventario(rs.getString("num_inventario"));
                especimen.setId_especie(rs.getInt("esp_id_especie"));
                especimen.setEspecie(especie);
                especimen.setNombre_especimen(rs.getString("nombre_especimen"));

                try {
                    especimen.setActivo(rs.getBoolean("esp_activo"));
                } catch (SQLException e) {
                    especimen.setActivo(true);
                }
            }
        } catch (SQLException e) {
            especimen = null;
        }

        // Crear objeto OrigenAlta
        OrigenAlta origenAlta = null;
        try {
            if (rs.getObject("oa_id_origen_alta") != null) {
                origenAlta = new OrigenAlta();
                origenAlta.setId_origen_alta(rs.getInt("oa_id_origen_alta"));
                origenAlta.setNombre_origen_alta(rs.getString("nombre_origen_alta"));
            }
        } catch (SQLException e) {
            origenAlta = null;
        }

        // Crear objeto RegistroAlta principal
        RegistroAlta registro = new RegistroAlta();
        registro.setId_registro_alta(rs.getInt("id_registro_alta"));
        registro.setId_especimen(rs.getInt("id_especimen"));
        registro.setId_origen_alta(rs.getInt("id_origen_alta"));
        registro.setId_responsable(rs.getInt("id_responsable"));
        registro.setFecha_ingreso(rs.getDate("fecha_ingreso"));
        registro.setProcedencia(rs.getString("procedencia"));
        registro.setObservacion(rs.getString("observacion"));

        // Asignar objetos relacionados
        registro.setEspecimen(especimen);
        registro.setOrigen_alta(origenAlta);
        registro.setResponsable(usuario);

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