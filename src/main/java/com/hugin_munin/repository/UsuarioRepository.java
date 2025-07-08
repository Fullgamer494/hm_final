package com.hugin_munin.repository;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    /**
     * FIND ALL USERS
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
     * FIND BY ID
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
     * FIND BY EMAIL
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
     * FIND BY NAME - Búsqueda por nombre
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
     * SAVE - INSERT nuevo usuario
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
     * UPDATE - Actualizar usuario existente
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
     * DELETE BY ID
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
     * EXISTS BY ID
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
     * EXISTS BY EMAIL
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
     * COUNT TOTAL USERS
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
     * COUNT ACTIVE USERS
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
     * MAP RESULTSET TO USUARIO
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
}