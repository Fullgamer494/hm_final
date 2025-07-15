package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Usuario;
import com.hugin_munin.model.Rol;
import com.hugin_munin.model.Permiso;
import com.hugin_munin.model.UsuarioConPermisos;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Repositorio para gestionar usuarios - COMPLETO CON NUEVO MÉTODO
 * Versión completa con funcionalidades de permisos - CORREGIDA
 * AGREGADO: Método para buscar por nombre de usuario
 */
public class UsuarioRepository {

    /**
     * BUSCAR todos los usuarios (sin joins inicialmente)
     */
    public List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String query = "SELECT id_usuario, id_rol, nombre_usuario, correo, contrasena, activo FROM usuario ORDER BY id_usuario ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = mapResultSetToUsuario(rs);
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    /**
     * BUSCAR usuario por ID
     */
    public Usuario findById(Integer id) throws SQLException {
        String query = "SELECT id_usuario, id_rol, nombre_usuario, correo, contrasena, activo FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * BUSCAR usuario por email
     */
    public Usuario findByEmail(String correo) throws SQLException {
        String query = "SELECT id_usuario, id_rol, nombre_usuario, correo, contrasena, activo FROM usuario WHERE correo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, correo.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }
        return null;
    }

    /**
     * BUSCAR usuario con permisos por correo electrónico
     * Incluye JOIN con rol y permisos - CORREGIDO
     */
    public UsuarioConPermisos findUsuarioConPermisosByCorreo(String correo) throws SQLException {
        // QUERY CORREGIDO - Sin columna descripcion que no existe
        String query = """
            SELECT 
                u.id_usuario,
                u.nombre_usuario,
                u.correo,
                u.activo,
                u.id_rol,
                r.nombre_rol,
                p.id_permiso,
                p.nombre_permiso
            FROM usuario u
            INNER JOIN rol r ON u.id_rol = r.id_rol
            LEFT JOIN rol_permiso rp ON r.id_rol = rp.id_rol
            LEFT JOIN permiso p ON rp.id_permiso = p.id_permiso
            WHERE u.correo = ?
            ORDER BY p.nombre_permiso ASC
            """;

        System.out.println("🔍 Repository: Ejecutando query corregido para: " + correo);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, correo.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                UsuarioConPermisos resultado = mapResultSetToUsuarioConPermisos(rs);

                if (resultado == null) {
                    System.out.println("❌ Repository: No se encontró usuario con correo: " + correo);
                } else {
                    System.out.println("✅ Repository: Usuario encontrado con " + resultado.getPermisos().size() + " permisos");
                }

                return resultado;
            }
        }
    }

    /**
     * BUSCAR usuario con permisos por nombre de usuario - NUEVO MÉTODO
     * Incluye JOIN con rol y permisos
     */
    public UsuarioConPermisos findUsuarioConPermisosByNombre(String nombreUsuario) throws SQLException {
        // QUERY para buscar por nombre de usuario
        String query = """
            SELECT 
                u.id_usuario,
                u.nombre_usuario,
                u.correo,
                u.activo,
                u.id_rol,
                r.nombre_rol,
                p.id_permiso,
                p.nombre_permiso
            FROM usuario u
            INNER JOIN rol r ON u.id_rol = r.id_rol
            LEFT JOIN rol_permiso rp ON r.id_rol = rp.id_rol
            LEFT JOIN permiso p ON rp.id_permiso = p.id_permiso
            WHERE u.nombre_usuario = ?
            ORDER BY p.nombre_permiso ASC
            """;

        System.out.println("🔍 Repository: Ejecutando query por nombre para: " + nombreUsuario);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreUsuario.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                UsuarioConPermisos resultado = mapResultSetToUsuarioConPermisos(rs);

                if (resultado == null) {
                    System.out.println("❌ Repository: No se encontró usuario con nombre: " + nombreUsuario);
                } else {
                    System.out.println("✅ Repository: Usuario encontrado con " + resultado.getPermisos().size() + " permisos");
                }

                return resultado;
            }
        }
    }

    /**
     * BUSCAR usuario con permisos por ID - CORREGIDO
     */
    public UsuarioConPermisos findUsuarioConPermisosById(Integer id) throws SQLException {
        // QUERY CORREGIDO - Sin columna descripcion que no existe
        String query = """
            SELECT 
                u.id_usuario,
                u.nombre_usuario,
                u.correo,
                u.activo,
                u.id_rol,
                r.nombre_rol,
                p.id_permiso,
                p.nombre_permiso
            FROM usuario u
            INNER JOIN rol r ON u.id_rol = r.id_rol
            LEFT JOIN rol_permiso rp ON r.id_rol = rp.id_rol
            LEFT JOIN permiso p ON rp.id_permiso = p.id_permiso
            WHERE u.id_usuario = ?
            ORDER BY p.nombre_permiso ASC
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return mapResultSetToUsuarioConPermisos(rs);
            }
        }
    }

    /**
     * BUSCAR usuarios por nombre
     */
    public List<Usuario> findByName(String nombre) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String query = "SELECT id_usuario, id_rol, nombre_usuario, correo, contrasena, activo FROM usuario WHERE nombre_usuario LIKE ? ORDER BY nombre_usuario ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
        }
        return usuarios;
    }

    /**
     * GUARDAR nuevo usuario
     */
    public Usuario save(Usuario usuario) throws SQLException {
        String query = "INSERT INTO usuario (id_rol, nombre_usuario, correo, contrasena, activo) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, usuario.getId_rol());
            stmt.setString(2, usuario.getNombre_usuario());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getContrasena());
            stmt.setBoolean(5, usuario.isActivo());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Error al crear usuario, no se insertaron filas");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId_usuario(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Error al crear usuario, no se obtuvo el ID");
                }
            }
        }

        return usuario;
    }

    /**
     * ACTUALIZAR usuario existente
     */
    public boolean update(Usuario usuario) throws SQLException {
        String query = "UPDATE usuario SET id_rol = ?, nombre_usuario = ?, correo = ?, contrasena = ?, activo = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, usuario.getId_rol());
            stmt.setString(2, usuario.getNombre_usuario());
            stmt.setString(3, usuario.getCorreo());
            stmt.setString(4, usuario.getContrasena());
            stmt.setBoolean(5, usuario.isActivo());
            stmt.setInt(6, usuario.getId_usuario());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * ELIMINAR usuario por ID
     */
    public boolean deleteById(Integer id) throws SQLException {
        String query = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * VERIFICAR si existe usuario por ID
     */
    public boolean existsById(Integer id) throws SQLException {
        String query = "SELECT COUNT(*) FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * VERIFICAR si existe usuario por email
     */
    public boolean existsByEmail(String correo) throws SQLException {
        String query = "SELECT COUNT(*) FROM usuario WHERE correo = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, correo.trim().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * CONTAR total de usuarios
     */
    public int countTotal() throws SQLException {
        String query = "SELECT COUNT(*) FROM usuario";

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
     * CONTAR usuarios activos
     */
    public int countActive() throws SQLException {
        String query = "SELECT COUNT(*) FROM usuario WHERE activo = TRUE";

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
     * MAPEAR ResultSet a objeto Usuario (version simplificada)
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId_usuario(rs.getInt("id_usuario"));
        usuario.setId_rol(rs.getInt("id_rol"));
        usuario.setNombre_usuario(rs.getString("nombre_usuario"));
        usuario.setCorreo(rs.getString("correo"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setActivo(rs.getBoolean("activo"));
        return usuario;
    }

    /**
     * Mapear ResultSet a UsuarioConPermisos - MÉTODO CORREGIDO
     */
    private UsuarioConPermisos mapResultSetToUsuarioConPermisos(ResultSet rs) throws SQLException {
        UsuarioConPermisos usuarioConPermisos = null;
        List<Permiso> permisos = new ArrayList<>();

        while (rs.next()) {
            // Si es la primera fila, crear el usuario
            if (usuarioConPermisos == null) {
                // Crear usuario
                Usuario usuario = new Usuario();
                usuario.setId_usuario(rs.getInt("id_usuario"));
                usuario.setNombre_usuario(rs.getString("nombre_usuario"));
                usuario.setCorreo(rs.getString("correo"));
                usuario.setActivo(rs.getBoolean("activo"));
                usuario.setId_rol(rs.getInt("id_rol"));

                // Crear rol - SIN descripcion que no existe en BD
                Rol rol = new Rol();
                rol.setId_rol(rs.getInt("id_rol"));
                rol.setNombre_rol(rs.getString("nombre_rol"));
                // CORREGIDO: Generar descripción automática
                rol.setDescripcion("Rol " + rs.getString("nombre_rol"));
                rol.setActivo(true); // Asumimos que está activo

                // Crear objeto combinado
                usuarioConPermisos = new UsuarioConPermisos(usuario, rol);
            }

            // Agregar permiso si existe (puede ser NULL por LEFT JOIN)
            Integer idPermiso = rs.getObject("id_permiso", Integer.class);
            if (idPermiso != null) {
                Permiso permiso = new Permiso();
                permiso.setId_permiso(idPermiso);
                permiso.setNombre_permiso(rs.getString("nombre_permiso"));
                permisos.add(permiso);
            }
        }

        if (usuarioConPermisos != null) {
            usuarioConPermisos.setPermisos(permisos);
        }

        return usuarioConPermisos;
    }
}