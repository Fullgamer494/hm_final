package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.model.Especimen;
import com.hugin_munin.model.Especie;
import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistroAltaRepository {

    /**
     * SAVE - Insertar nuevo registro de alta
     */
    public RegistroAlta saveRegister(RegistroAlta registroAlta) throws SQLException {
        String sql = "INSERT INTO registro_alta (id_especimen, id_origen_alta, id_responsable, fecha_ingreso, procedencia, observacion) VALUES (?, ?, ?, ?, ?, ?)";

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
                throw new SQLException("Error al crear el registro de alta.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    registroAlta.setId_registro_alta(generatedKeys.getInt(1));
                    return registroAlta;
                } else {
                    throw new SQLException("No se pudo obtener el ID del registro de alta.");
                }
            }
        }
    }

    /**
     * FIND ALL - Obtener todos los registros
     */
    public List<RegistroAlta> findAllRegisters() throws SQLException {
        String sql = """
            SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
                   ra.fecha_ingreso, ra.procedencia, ra.observacion,
                   
                   -- Datos de Especimen
                   esp.id_especimen as esp_id_especimen, esp.num_inventario, esp.id_especie as esp_id_especie,
                   esp.nombre_especimen, esp.activo,
                   
                   -- Datos de Especie
                   e.id_especie as e_id_especie, e.genero, e.especie,
                   
                   -- Datos de OrigenAlta
                   oa.id_origen_alta as oa_id_origen_alta, oa.nombre_origen_alta,
                   
                   -- Datos de Responsable (Usuario)
                   u.id_usuario, u.nombre_usuario,
                   
                   -- Datos de ReporteTraslado
                   rt.ubicacion_destino, rt.area_destino
                   
            FROM registro_alta ra
            LEFT JOIN especimen esp ON ra.id_especimen = esp.id_especimen
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            LEFT JOIN origen_alta oa ON ra.id_origen_alta = oa.id_origen_alta
            LEFT JOIN usuario u ON ra.id_responsable = u.id_usuario
            LEFT JOIN reporte_traslado rt ON esp.id_especimen = rt.id_especimen
            ORDER BY ra.id_registro_alta
            """;

        return executeQueryWithJoins(sql);
    }

    /**
     * FIND BY ID - Obtener por ID
     */
    public Optional<RegistroAlta> findRegistersById(Integer id) throws SQLException {
        String sql = """
            SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
                   ra.fecha_ingreso, ra.procedencia, ra.observacion,
                   
                   -- Datos de Especimen
                   esp.id_especimen as esp_id_especimen, esp.num_inventario, esp.id_especie as esp_id_especie,
                   esp.nombre_especimen, esp.activo,
                   
                   -- Datos de Especie
                   e.id_especie as e_id_especie, e.genero, e.especie,
                   
                   -- Datos de OrigenAlta
                   oa.id_origen_alta as oa_id_origen_alta, oa.nombre_origen_alta,
                   
                   -- Datos de Responsable (Usuario)
                   u.id_usuario, u.nombre_usuario,
                   
                   -- Datos de ReporteTraslado
                   rt.ubicacion_destino, rt.area_destino
                   
            FROM registro_alta ra
            LEFT JOIN especimen esp ON ra.id_especimen = esp.id_especimen
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            LEFT JOIN origen_alta oa ON ra.id_origen_alta = oa.id_origen_alta
            LEFT JOIN usuario u ON ra.id_responsable = u.id_usuario
            LEFT JOIN reporte_traslado rt ON esp.id_especimen = rt.id_especimen
            WHERE ra.id_registro_alta = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            List<RegistroAlta> results = executeQueryWithJoins(stmt);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        }
    }

    /**
     * UPDATE - Actualizar registro
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
                throw new SQLException("No se pudo actualizar el registro, no existe el ID especificado.");
            }

            return registroAlta;
        }
    }

    /**
     * DELETE - Eliminar registro
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
     * Método auxiliar para ejecutar consultas con joins
     */
    private List<RegistroAlta> executeQueryWithJoins(String sql) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return executeQueryWithJoins(stmt);
        }
    }

    private List<RegistroAlta> executeQueryWithJoins(PreparedStatement stmt) throws SQLException {
        List<RegistroAlta> registros = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Crear objeto Especie (con manejo de null)
                Especie especie = null;
                if (rs.getObject("e_id_especie") != null) {
                    especie = new Especie();
                    especie.setId_especie(rs.getInt("e_id_especie"));
                    especie.setGenero(rs.getString("genero"));
                    especie.setEspecie(rs.getString("especie"));
                }

                // Crear objeto Especimen (con manejo de null)
                Especimen especimen = null;
                if (rs.getObject("esp_id_especimen") != null) {
                    especimen = new Especimen();
                    especimen.setId_especimen(rs.getInt("esp_id_especimen"));
                    especimen.setNum_inventario(rs.getString("num_inventario"));
                    especimen.setId_especie(rs.getInt("esp_id_especie"));
                    especimen.setNombre_especimen(rs.getString("nombre_especimen"));
                    especimen.setActivo(rs.getBoolean("activo"));
                    especimen.setEspecie(especie);
                }

                // Crear objeto OrigenAlta (con manejo de null)
                OrigenAlta origenAlta = null;
                if (rs.getObject("oa_id_origen_alta") != null) {
                    origenAlta = new OrigenAlta();
                    origenAlta.setId_origen_alta(rs.getInt("oa_id_origen_alta"));
                    origenAlta.setNombre_origen_alta(rs.getString("nombre_origen_alta"));
                }

                // Crear objeto Responsable (Usuario) (con manejo de null)
                Usuario usuario = null;
                if (rs.getObject("id_usuario") != null) {
                    usuario = new Usuario();
                    usuario.setId_usuario(rs.getInt("id_usuario"));
                    usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                }

                // Crear objeto RegistroAlta
                RegistroAlta registro = new RegistroAlta(
                        rs.getInt("id_registro_alta"),
                        rs.getInt("id_especimen"),
                        especimen,
                        rs.getInt("id_origen_alta"),
                        origenAlta,
                        rs.getInt("id_responsable"), // ✅ Corregido
                        usuario,
                        rs.getDate("fecha_ingreso"),
                        rs.getString("procedencia"),
                        rs.getString("observacion")
                );

                registros.add(registro);
            }
        }

        return registros;
    }

    /**
     * Buscar por especimen
     */
    public List<RegistroAlta> findByEspecimen(Integer idEspecimen) throws SQLException {
        String sql = """
            SELECT ra.id_registro_alta, ra.id_especimen, ra.id_origen_alta, ra.id_responsable,
                   ra.fecha_ingreso, ra.procedencia, ra.observacion,
                   
                   -- Datos de Especimen
                   esp.id_especimen as esp_id_especimen, esp.num_inventario, esp.id_especie as esp_id_especie,
                   esp.nombre_especimen, esp.activo,
                   
                   -- Datos de Especie
                   e.id_especie as e_id_especie, e.genero, e.especie,
                   
                   -- Datos de OrigenAlta
                   oa.id_origen_alta as oa_id_origen_alta, oa.nombre_origen_alta,
                   
                   -- Datos de Responsable (Usuario)
                   u.id_usuario, u.nombre_usuario,
                   
                   -- Datos de ReporteTraslado
                   rt.ubicacion_destino, rt.area_destino
                   
            FROM registro_alta ra
            LEFT JOIN especimen esp ON ra.id_especimen = esp.id_especimen
            LEFT JOIN especie e ON esp.id_especie = e.id_especie
            LEFT JOIN origen_alta oa ON ra.id_origen_alta = oa.id_origen_alta
            LEFT JOIN usuario u ON ra.id_responsable = u.id_usuario
            LEFT JOIN reporte_traslado rt ON esp.id_especimen = rt.id_especimen
            WHERE ra.id_especimen = ?
            ORDER BY ra.id_registro_alta
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEspecimen);
            return executeQueryWithJoins(stmt);
        }
    }

    /**
     * Clase auxiliar para información de traslado
     */
    public static class TrasladoInfo {
        private Integer idEspecimen;
        private String nombreEspecimen;
        private String ubicacionDestino;
        private String areaDestino;

        public TrasladoInfo(Integer idEspecimen, String nombreEspecimen,
                            String ubicacionDestino, String areaDestino) {
            this.idEspecimen = idEspecimen;
            this.nombreEspecimen = nombreEspecimen;
            this.ubicacionDestino = ubicacionDestino;
            this.areaDestino = areaDestino;
        }

        // Getters y setters
        public Integer getIdEspecimen() { return idEspecimen; }
        public void setIdEspecimen(Integer idEspecimen) { this.idEspecimen = idEspecimen; }

        public String getNombreEspecimen() { return nombreEspecimen; }
        public void setNombreEspecimen(String nombreEspecimen) { this.nombreEspecimen = nombreEspecimen; }

        public String getUbicacionDestino() { return ubicacionDestino; }
        public void setUbicacionDestino(String ubicacionDestino) { this.ubicacionDestino = ubicacionDestino; }

        public String getAreaDestino() { return areaDestino; }
        public void setAreaDestino(String areaDestino) { this.areaDestino = areaDestino; }
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