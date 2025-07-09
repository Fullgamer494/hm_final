package com.hugin_munin.service;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.repository.UsuarioRepository;
import com.hugin_munin.repository.RolRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para gestionar usuarios - Version simplificada
 * Se enfoca en funcionalidad básica y estable
 */
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    /**
     * OBTENER todos los usuarios
     */
    public List<Usuario> getAllUsers() throws SQLException {
        return usuarioRepository.findAll();
    }

    /**
     * OBTENER usuario por ID
     */
    public Usuario getUserById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Usuario usuario = usuarioRepository.findById(id);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        return usuario;
    }

    /**
     * BUSCAR usuarios por nombre
     */
    public List<Usuario> searchUsersByName(String nombre) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        return usuarioRepository.findByName(nombre.trim());
    }

    /**
     * CREAR nuevo usuario
     */
    public Usuario createUser(Usuario usuario) throws SQLException {
        // Validaciones básicas
        validateBasicUserData(usuario);

        // Validar que el email no esté en uso
        if (usuarioRepository.existsByEmail(usuario.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        // Validar que el rol existe
        if (!rolRepository.existsById(usuario.getId_rol())) {
            throw new IllegalArgumentException("El rol especificado no existe");
        }

        // Normalizar email
        usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());

        // Guardar usuario
        return usuarioRepository.save(usuario);
    }

    /**
     * ACTUALIZAR usuario existente
     */
    public Usuario updateUser(Usuario usuario) throws SQLException {
        if (usuario.getId_usuario() == null || usuario.getId_usuario() <= 0) {
            throw new IllegalArgumentException("ID del usuario requerido para actualización");
        }

        // Verificar que el usuario existe
        Usuario existingUser = usuarioRepository.findById(usuario.getId_usuario());
        if (existingUser == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuario.getId_usuario());
        }

        // Validaciones básicas
        validateBasicUserData(usuario);

        // Validar que el email no esté en uso por otro usuario
        Usuario userWithEmail = usuarioRepository.findByEmail(usuario.getCorreo());
        if (userWithEmail != null && !userWithEmail.getId_usuario().equals(usuario.getId_usuario())) {
            throw new IllegalArgumentException("El email ya está en uso por otro usuario");
        }

        // Validar que el rol existe
        if (!rolRepository.existsById(usuario.getId_rol())) {
            throw new IllegalArgumentException("El rol especificado no existe");
        }

        // Normalizar email
        usuario.setCorreo(usuario.getCorreo().trim().toLowerCase());

        // Si no se proporcionó contraseña, mantener la existente
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            usuario.setContrasena(existingUser.getContrasena());
        }

        // Actualizar usuario
        boolean updated = usuarioRepository.update(usuario);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el usuario");
        }

        return usuario;
    }

    /**
     * ELIMINAR usuario
     */
    public boolean deleteUser(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el usuario existe
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
        }

        return usuarioRepository.deleteById(id);
    }

    /**
     * VERIFICAR si un email está disponible
     */
    public boolean isEmailAvailable(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        return !usuarioRepository.existsByEmail(email.trim().toLowerCase());
    }

    /**
     * OBTENER estadísticas básicas de usuarios
     */
    public Map<String, Object> getUserStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_usuarios", usuarioRepository.countTotal());
        stats.put("usuarios_activos", usuarioRepository.countActive());

        return stats;
    }

    /**
     * Validar datos básicos del usuario
     */
    private void validateBasicUserData(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }

        if (usuario.getNombre_usuario() == null || usuario.getNombre_usuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido");
        }

        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es requerido");
        }

        if (usuario.getId_rol() == null || usuario.getId_rol() <= 0) {
            throw new IllegalArgumentException("El rol es requerido");
        }

        // Validar formato de email básico
        if (!usuario.getCorreo().contains("@") || !usuario.getCorreo().contains(".")) {
            throw new IllegalArgumentException("El formato del email no es válido");
        }

        // Validar longitudes
        if (usuario.getNombre_usuario().length() < 2 || usuario.getNombre_usuario().length() > 50) {
            throw new IllegalArgumentException("El nombre debe tener entre 2 y 50 caracteres");
        }

        if (usuario.getCorreo().length() > 100) {
            throw new IllegalArgumentException("El email no puede exceder 100 caracteres");
        }
    }
}