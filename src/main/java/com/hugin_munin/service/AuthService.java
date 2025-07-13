package com.hugin_munin.service;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Servicio de autenticación CORREGIDO - Sistema unificado
 * Maneja login, logout y sesiones SOLO con cookies personalizadas
 */
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    // Almacén de sesiones en memoria (sessionId -> SessionData)
    private final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    // Tiempo de expiración de sesión en milisegundos (30 días)
    private static final long SESSION_DURATION = 30L * 24 * 60 * 60 * 1000; // 30 días

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        startSessionCleanup();
    }

    /**
     * Autenticar usuario con nombre y contraseña
     */
    public Usuario authenticate(String nombreUsuario, String contrasena) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty() ||
                contrasena == null || contrasena.trim().isEmpty()) {
            return null;
        }

        // Buscar usuario por nombre
        List<Usuario> usuarios = usuarioRepository.findByName(nombreUsuario.trim());
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getNombre_usuario().equals(nombreUsuario.trim()))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado: " + nombreUsuario);
            return null;
        }

        // Verificar contraseña
        if (verifyPassword(contrasena, usuario.getContrasena())) {
            System.out.println("✅ Usuario autenticado: " + usuario.getNombre_usuario());
            return usuario;
        }

        System.out.println("❌ Contraseña incorrecta para: " + nombreUsuario);
        return null;
    }

    /**
     * Crear nueva sesión para usuario autenticado
     */
    public String createSession(Usuario usuario) {
        // LIMPIAR sesiones previas del mismo usuario primero
        invalidateAllUserSessions(usuario.getId_usuario());

        // Generar ID único de sesión
        String sessionId = generateSessionId();

        // Crear datos de sesión
        SessionData sessionData = new SessionData(
                sessionId,
                usuario.getId_usuario(),
                usuario.getNombre_usuario(),
                new Date(),
                new Date(System.currentTimeMillis() + SESSION_DURATION)
        );

        // Almacenar sesión
        activeSessions.put(sessionId, sessionData);

        System.out.println("🔐 Nueva sesión creada para usuario: " + usuario.getNombre_usuario() +
                " (ID: " + sessionId + ") - Total sesiones activas: " + activeSessions.size());

        return sessionId;
    }

    /**
     * Obtener usuario por ID de sesión - VERSIÓN CORREGIDA
     */
    public Usuario getUserBySession(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            System.out.println("⚠️ SessionId vacío o nulo");
            return null;
        }

        SessionData sessionData = activeSessions.get(sessionId);
        if (sessionData == null) {
            System.out.println("⚠️ Sesión no encontrada: " + sessionId);
            return null;
        }

        // Verificar si la sesión ha expirado
        if (sessionData.isExpired()) {
            System.out.println("⚠️ Sesión expirada: " + sessionId);
            activeSessions.remove(sessionId);
            return null;
        }

        // Obtener usuario actualizado de la base de datos
        Usuario usuario = usuarioRepository.findById(sessionData.getUserId());
        if (usuario == null || !usuario.isActivo()) {
            System.out.println("⚠️ Usuario eliminado o desactivado: " + sessionData.getUserId());
            activeSessions.remove(sessionId);
            return null;
        }

        // Actualizar última actividad
        sessionData.updateActivity();
        System.out.println("✅ Sesión válida para: " + usuario.getNombre_usuario());

        return usuario;
    }

    /**
     * Invalidar sesión específica - VERSIÓN CORREGIDA
     */
    public boolean invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return false;
        }

        SessionData session = activeSessions.remove(sessionId);
        if (session != null) {
            System.out.println("🔐 Sesión invalidada: " + sessionId +
                    " (Usuario: " + session.getUsername() + ")");
            return true;
        }

        System.out.println("⚠️ Sesión no encontrada para invalidar: " + sessionId);
        return false;
    }

    /**
     * Invalidar todas las sesiones de un usuario - MEJORADO
     */
    public void invalidateAllUserSessions(Integer userId) {
        int removed = 0;
        List<String> sessionsToRemove = new ArrayList<>();

        // Identificar sesiones a remover
        for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
            if (entry.getValue().getUserId().equals(userId)) {
                sessionsToRemove.add(entry.getKey());
            }
        }

        // Remover sesiones
        for (String sessionId : sessionsToRemove) {
            activeSessions.remove(sessionId);
            removed++;
        }

        if (removed > 0) {
            System.out.println("🔐 " + removed + " sesiones invalidadas para usuario ID: " + userId);
        }
    }

    /**
     * Cambiar contraseña de usuario
     */
    public boolean changePassword(Integer userId, String currentPassword, String newPassword) throws SQLException {
        Usuario usuario = usuarioRepository.findById(userId);

        if (usuario == null) {
            return false;
        }

        // Verificar contraseña actual
        if (!verifyPassword(currentPassword, usuario.getContrasena())) {
            return false;
        }

        // Establecer nueva contraseña
        usuario.setContrasena(hashPassword(newPassword));

        // Actualizar en base de datos
        boolean updated = usuarioRepository.update(usuario);

        if (updated) {
            // Invalidar todas las sesiones del usuario (forzar re-login)
            invalidateAllUserSessions(userId);
            System.out.println("🔐 Contraseña cambiada para usuario ID: " + userId + " - Sesiones invalidadas");
        }

        return updated;
    }

    /**
     * Obtener información de sesiones activas
     */
    public Map<String, Object> getSessionInfo() {
        int totalSessions = activeSessions.size();
        long expiredSessions = activeSessions.values().stream()
                .mapToLong(session -> session.isExpired() ? 1 : 0)
                .sum();

        Map<String, Object> info = new ConcurrentHashMap<>();
        info.put("total_sesiones_activas", totalSessions);
        info.put("sesiones_expiradas", expiredSessions);
        info.put("sesiones_validas", totalSessions - expiredSessions);

        return info;
    }

    // MÉTODOS PRIVADOS

    /**
     * Verificar contraseña - MEJORADO
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        // Si la contraseña en BD no está hasheada (backward compatibility)
        if (!hashedPassword.startsWith("sha256:")) {
            boolean matches = plainPassword.equals(hashedPassword);
            System.out.println("🔑 Verificando contraseña plain text: " + matches);
            return matches;
        }

        // Verificar contraseña hasheada
        String expectedHash = hashPassword(plainPassword);
        boolean matches = expectedHash.equals(hashedPassword);
        System.out.println("🔑 Verificando contraseña hasheada: " + matches);
        return matches;
    }

    /**
     * Hashear contraseña usando SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return "sha256:" + hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    /**
     * Generar ID único de sesión
     */
    private String generateSessionId() {
        return "HM_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Iniciar hilo de limpieza de sesiones expiradas
     */
    private void startSessionCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30 * 60 * 1000); // Cada 30 minutos
                    int removedCount = cleanupExpiredSessions();
                    if (removedCount > 0) {
                        System.out.println("🧹 Limpieza de sesiones: " + removedCount + " sesiones expiradas eliminadas");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error en limpieza de sesiones: " + e.getMessage());
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.setName("SessionCleanup");
        cleanupThread.start();
    }

    /**
     * Limpiar sesiones expiradas
     */
    private int cleanupExpiredSessions() {
        List<String> expiredSessions = new ArrayList<>();

        for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessions.add(entry.getKey());
            }
        }

        for (String sessionId : expiredSessions) {
            activeSessions.remove(sessionId);
        }

        return expiredSessions.size();
    }

    /**
     * Clase interna para almacenar datos de sesión
     */
    private static class SessionData {
        private final String sessionId;
        private final Integer userId;
        private final String username;
        private final Date createdAt;
        private Date expiresAt;
        private Date lastActivity;

        public SessionData(String sessionId, Integer userId, String username, Date createdAt, Date expiresAt) {
            this.sessionId = sessionId;
            this.userId = userId;
            this.username = username;
            this.createdAt = createdAt;
            this.expiresAt = expiresAt;
            this.lastActivity = new Date();
        }

        public boolean isExpired() {
            return new Date().after(expiresAt);
        }

        public void updateActivity() {
            this.lastActivity = new Date();
            // Extender expiración por actividad
            this.expiresAt = new Date(System.currentTimeMillis() + SESSION_DURATION);
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public Integer getUserId() { return userId; }
        public String getUsername() { return username; }
        public Date getCreatedAt() { return createdAt; }
        public Date getExpiresAt() { return expiresAt; }
        public Date getLastActivity() { return lastActivity; }
    }
}