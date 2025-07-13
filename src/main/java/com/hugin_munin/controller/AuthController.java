package com.hugin_munin.controller;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.service.AuthService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador para manejo de autenticación
 * Sistema básico con cookies
 */
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /hm/auth/login - Iniciar sesión
     */
    public void login(Context ctx) {
        try {
            // Obtener credenciales del request
            Map<String, String> credentials = ctx.bodyAsClass(Map.class);
            String nombreUsuario = credentials.get("nombre_usuario");
            String contrasena = credentials.get("contrasena");

            // Validar que se proporcionaron las credenciales
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nombre de usuario requerido", "Debe proporcionar un nombre de usuario"));
                return;
            }

            if (contrasena == null || contrasena.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Contraseña requerida", "Debe proporcionar una contraseña"));
                return;
            }

            // Autenticar usuario
            Usuario usuario = authService.authenticate(nombreUsuario.trim(), contrasena);

            if (usuario == null) {
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("Credenciales inválidas", "Usuario o contraseña incorrectos"));
                return;
            }

            // Verificar que el usuario esté activo
            if (!usuario.isActivo()) {
                ctx.status(HttpStatus.FORBIDDEN)
                        .json(createErrorResponse("Usuario inactivo", "Su cuenta está desactivada. Contacte al administrador"));
                return;
            }

            // Crear sesión y establecer cookie
            String sessionId = authService.createSession(usuario);

            // Configurar cookie de sesión (30 días de duración)
            ctx.cookie("HM_SESSION", sessionId, 30 * 24 * 60 * 60); // 30 días en segundos
            ctx.cookie("HM_USER_ID", usuario.getId_usuario().toString(), 30 * 24 * 60 * 60);
            ctx.cookie("HM_USER_NAME", usuario.getNombre_usuario(), 30 * 24 * 60 * 60);

            // Preparar respuesta exitosa (sin incluir contraseña)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id_usuario", usuario.getId_usuario());
            userInfo.put("nombre_usuario", usuario.getNombre_usuario());
            userInfo.put("correo", usuario.getCorreo());
            userInfo.put("id_rol", usuario.getId_rol());
            userInfo.put("activo", usuario.isActivo());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Inicio de sesión exitoso");
            response.put("usuario", userInfo);
            response.put("session_id", sessionId);

            ctx.json(response);

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.UNAUTHORIZED)
                    .json(createErrorResponse("Error de autenticación", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", "Error al procesar el login"));
        }
    }

    /**
     * POST /hm/auth/logout - Cerrar sesión
     */
    public void logout(Context ctx) {
        try {
            String sessionId = ctx.cookie("HM_SESSION");

            if (sessionId != null) {
                // Eliminar sesión del servidor
                authService.invalidateSession(sessionId);
            }

            // Eliminar cookies
            ctx.removeCookie("HM_SESSION");
            ctx.removeCookie("HM_USER_ID");
            ctx.removeCookie("HM_USER_NAME");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesión cerrada exitosamente");

            ctx.json(response);

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al cerrar sesión", e.getMessage()));
        }
    }

    /**
     * GET /hm/auth/verify - Verificar sesión actual
     */
    public void verifySession(Context ctx) {
        try {
            String sessionId = ctx.cookie("HM_SESSION");

            if (sessionId == null || sessionId.trim().isEmpty()) {
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("No hay sesión activa", "No se encontró cookie de sesión"));
                return;
            }

            // Verificar si la sesión es válida
            Usuario usuario = authService.getUserBySession(sessionId);

            if (usuario == null) {
                // Sesión inválida, limpiar cookies
                ctx.removeCookie("HM_SESSION");
                ctx.removeCookie("HM_USER_ID");
                ctx.removeCookie("HM_USER_NAME");

                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("Sesión inválida", "La sesión ha expirado"));
                return;
            }

            // Preparar información del usuario
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id_usuario", usuario.getId_usuario());
            userInfo.put("nombre_usuario", usuario.getNombre_usuario());
            userInfo.put("correo", usuario.getCorreo());
            userInfo.put("id_rol", usuario.getId_rol());
            userInfo.put("activo", usuario.isActivo());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sesión válida");
            response.put("usuario", userInfo);

            ctx.json(response);

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al verificar sesión", e.getMessage()));
        }
    }

    /**
     * GET /hm/auth/profile - Obtener perfil del usuario autenticado
     */
    public void getProfile(Context ctx) {
        try {
            // Obtener usuario de la sesión (asumiendo que hay middleware que lo establece)
            Usuario usuario = ctx.sessionAttribute("usuario");

            if (usuario == null) {
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("No autenticado", "Debe iniciar sesión"));
                return;
            }

            // Preparar información completa del perfil
            Map<String, Object> profile = new HashMap<>();
            profile.put("id_usuario", usuario.getId_usuario());
            profile.put("nombre_usuario", usuario.getNombre_usuario());
            profile.put("correo", usuario.getCorreo());
            profile.put("id_rol", usuario.getId_rol());
            profile.put("activo", usuario.isActivo());

            // Si tiene rol cargado, incluir información del rol
            if (usuario.getRol() != null) {
                Map<String, Object> rolInfo = new HashMap<>();
                rolInfo.put("id_rol", usuario.getRol().getId_rol());
                rolInfo.put("nombre_rol", usuario.getRol().getNombre_rol());
                rolInfo.put("descripcion", usuario.getRol().getDescripcion());
                profile.put("rol", rolInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("profile", profile);

            ctx.json(response);

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener perfil", e.getMessage()));
        }
    }

    /**
     * PUT /hm/auth/change-password - Cambiar contraseña
     */
    public void changePassword(Context ctx) {
        try {
            Usuario usuario = ctx.sessionAttribute("usuario");

            if (usuario == null) {
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("No autenticado", "Debe iniciar sesión"));
                return;
            }

            Map<String, String> passwordData = ctx.bodyAsClass(Map.class);
            String currentPassword = passwordData.get("contrasena_actual");
            String newPassword = passwordData.get("contrasena_nueva");

            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Contraseña actual requerida", "Debe proporcionar su contraseña actual"));
                return;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nueva contraseña requerida", "Debe proporcionar una nueva contraseña"));
                return;
            }

            if (newPassword.length() < 6) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Contraseña muy corta", "La nueva contraseña debe tener al menos 6 caracteres"));
                return;
            }

            // Cambiar contraseña
            boolean changed = authService.changePassword(usuario.getId_usuario(), currentPassword, newPassword);

            if (!changed) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Contraseña actual incorrecta", "La contraseña actual no es válida"));
                return;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Contraseña cambiada exitosamente");

            ctx.json(response);

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al cambiar contraseña", e.getMessage()));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, Object> createErrorResponse(String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("details", details);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}