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
 * Servicio de autenticación básico con cookies
 * Maneja login, logout y sesiones en memoria
 */
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    // Almacén de sesiones en memoria (sessionId -> SessionData)
    private final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    // Tiempo de expiración de sesión en milisegundos (30 días)
    private static final long SESSION_DURATION = 30L * 24 * 60 * 60 * 1000; // 30 días

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;

        // Iniciar hilo de limpieza de sesiones expiradas
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
        Usuario usuario = usuarioRepository.findByName(nombreUsuario.trim())
                .stream()
                .filter(u -> u.getNombre_usuario().equals(nombreUsuario.trim()))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            return null;
        }

        // Verificar contraseña
        if (verifyPassword(contrasena, usuario.getContrasena())) {
            return usuario;
        }

        return null;
    }

    /**
     * Crear nueva sesión para usuario autenticado
     */
    public String createSession(Usuario usuario) {
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

        System.out.println("🔐 Sesión creada para usuario: " + usuario.getNombre_usuario() + " (ID: " + sessionId + ")");

        return sessionId;
    }

    /**
     * Obtener usuario por ID de sesión
     */
    public Usuario getUserBySession(String sessionId) throws SQLException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return null;
        }

        SessionData sessionData = activeSessions.get(sessionId);

        if (sessionData == null) {
            return null;
        }

        // Verificar si la sesión ha expirado
        if (sessionData.isExpired()) {
            activeSessions.remove(sessionId);
            return null;
        }

        // Obtener usuario actualizado de la base de datos
        Usuario usuario = usuarioRepository.findById(sessionData.getUserId());

        if (usuario == null || !usuario.isActivo()) {
            // Usuario eliminado o desactivado, invalidar sesión
            activeSessions.remove(sessionId);
            return null;
        }

        // Actualizar última actividad
        sessionData.updateActivity();

        return usuario;
    }

    /**
     * Invalidar sesión específica
     */
    public void invalidateSession(String sessionId) {
        if (sessionId != null) {
            SessionData session = activeSessions.remove(sessionId);
            if (session != null) {
                System.out.println("🔐 Sesión invalidada: " + sessionId + " (Usuario: " + session.getUsername() + ")");
            }
        }
    }

    /**
     * Invalidar todas las sesiones de un usuario
     */
    public void invalidateAllUserSessions(Integer userId) {
        activeSessions.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUserId().equals(userId);
            if (shouldRemove) {
                System.out.println("🔐 Sesión invalidada por limpieza de usuario: " + entry.getKey());
            }
            return shouldRemove;
        });
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
     * Obtener información de sesiones activas (para administración)
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
     * Verificar contraseña
     */
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        // Si la contraseña en BD no está hasheada (backward compatibility)
        if (!hashedPassword.startsWith("sha256:")) {
            return plainPassword.equals(hashedPassword);
        }

        // Verificar contraseña hasheada
        String expectedHash = hashPassword(plainPassword);
        return expectedHash.equals(hashedPassword);
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
                    // Ejecutar cada 30 minutos
                    Thread.sleep(30 * 60 * 1000);

                    // Limpiar sesiones expiradas
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
        // Recopilar las sesiones expiradas primero
        List<String> expiredSessions = new ArrayList<>();

        for (Map.Entry<String, SessionData> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessions.add(entry.getKey());
            }
        }

        // Eliminar las sesiones expiradas
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