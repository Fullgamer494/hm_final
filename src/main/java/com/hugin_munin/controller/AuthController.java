package com.hugin_munin.controller;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.model.UsuarioConPermisos;
import com.hugin_munin.service.AuthService;
import com.hugin_munin.service.UsuarioService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;

/**
 * Controlador de autenticación - CORREGIDO PARA USAR MÉTODOS CORRECTOS
 * Maneja login, logout, verificación de sesión y perfil de usuario
 * CORREGIDO: Usa métodos que existen en AuthService
 */
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    /**
     * POST /hm/auth/login - Iniciar sesión - CORREGIDO
     */
    public void login(Context ctx) {
        try {
            System.out.println("🔑 AuthController: Iniciando proceso de login");

            // Obtener datos del cuerpo de la petición
            Map<String, String> credentials = ctx.bodyAsClass(Map.class);
            String nombreUsuario = credentials.get("nombre_usuario");
            String contrasena = credentials.get("contrasena");

            System.out.println("📝 Datos recibidos - Usuario: " + nombreUsuario);

            // Validar que se proporcionen las credenciales
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos incompletos", "El nombre de usuario es requerido"));
                return;
            }

            if (contrasena == null || contrasena.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos incompletos", "La contraseña es requerida"));
                return;
            }

            // CORREGIDO: Usar authenticate en lugar de login
            Usuario usuario = authService.authenticate(nombreUsuario, contrasena);

            if (usuario == null) {
                System.out.println("❌ Login fallido para usuario: " + nombreUsuario);
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("Credenciales inválidas", "Usuario o contraseña incorrectos"));
                return;
            }

            System.out.println("✅ Usuario autenticado: " + usuario.getNombre_usuario());

            // CORREGIDO: Crear sesión usando createSession
            String sessionId = authService.createSession(usuario);

            System.out.println("✅ Sesión creada: " + sessionId);

            // Configurar cookies de sesión
            ctx.cookie("HM_SESSION", sessionId, 86400); // 24 horas
            ctx.cookie("HM_USER_ID", String.valueOf(usuario.getId_usuario()), 86400);
            ctx.cookie("HM_USER_NAME", usuario.getNombre_usuario(), 86400);

            // Respuesta exitosa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("user", Map.of(
                    "id_usuario", usuario.getId_usuario(),
                    "nombre_usuario", usuario.getNombre_usuario(),
                    "correo", usuario.getCorreo(),
                    "id_rol", usuario.getId_rol()
            ));
            response.put("session_id", sessionId);
            response.put("timestamp", System.currentTimeMillis());

            ctx.json(response);

        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno", "Error al procesar el login"));
        }
    }

    /**
     * POST /hm/auth/logout - Cerrar sesión - CORREGIDO
     */
    public void logout(Context ctx) {
        try {
            System.out.println("🚪 AuthController: Iniciando logout");

            String sessionId = ctx.cookie("HM_SESSION");

            if (sessionId != null && !sessionId.trim().isEmpty()) {
                // CORREGIDO: Usar invalidateSession en lugar de logout
                boolean invalidated = authService.invalidateSession(sessionId);
                if (invalidated) {
                    System.out.println("✅ Sesión invalidada: " + sessionId.substring(0, Math.min(10, sessionId.length())) + "...");
                } else {
                    System.out.println("⚠️ Sesión no encontrada para invalidar: " + sessionId);
                }
            }

            // Limpiar todas las cookies
            ctx.removeCookie("HM_SESSION");
            ctx.removeCookie("HM_USER_ID");
            ctx.removeCookie("HM_USER_NAME");

            // Configurar cookies con expiración inmediata
            ctx.cookie("HM_SESSION", "", 0);
            ctx.cookie("HM_USER_ID", "", 0);
            ctx.cookie("HM_USER_NAME", "", 0);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logout exitoso");
            response.put("timestamp", System.currentTimeMillis());

            ctx.json(response);

        } catch (Exception e) {
            System.err.println("❌ Error en logout: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno", "Error al procesar el logout"));
        }
    }

    /**
     * GET /hm/auth/verify - Verificar sesión actual
     */
    public void verifySession(Context ctx) {
        try {
            System.out.println("🔍 AuthController: Verificando sesión");

            String sessionId = ctx.cookie("HM_SESSION");

            if (sessionId == null || sessionId.trim().isEmpty()) {
                System.out.println("⚠️ No hay cookie de sesión");
                ctx.json(Map.of(
                        "success", false,
                        "message", "No hay sesión activa",
                        "authenticated", false,
                        "timestamp", System.currentTimeMillis()
                ));
                return;
            }

            // Verificar si la sesión es válida
            Usuario usuario = authService.getUserBySession(sessionId);

            if (usuario == null) {
                System.out.println("⚠️ Sesión inválida");
                ctx.json(Map.of(
                        "success", false,
                        "message", "Sesión inválida",
                        "authenticated", false,
                        "timestamp", System.currentTimeMillis()
                ));
                return;
            }

            System.out.println("✅ Sesión válida para usuario: " + usuario.getNombre_usuario());

            ctx.json(Map.of(
                    "success", true,
                    "message", "Sesión válida",
                    "authenticated", true,
                    "usuario", Map.of(
                            "id_usuario", usuario.getId_usuario(),
                            "nombre_usuario", usuario.getNombre_usuario(),
                            "correo", usuario.getCorreo(),
                            "id_rol", usuario.getId_rol(),
                            "activo", usuario.isActivo()
                    ),
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error en verifySession: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno", "Error al verificar la sesión"));
        }
    }

    /**
     * GET /hm/auth/profile - Obtener perfil del usuario autenticado
     */
    public void getProfile(Context ctx) {
        try {
            System.out.println("🎯 AuthController: Iniciando getProfile");

            // DEBUGGING TEMPORAL
            System.out.println("🔍 DEBUG getProfile: Usuario en attribute: " + ctx.attribute("usuario"));
            System.out.println("🔍 DEBUG getProfile: Cookie HM_SESSION: " + (ctx.cookie("HM_SESSION") != null ? "presente" : "ausente"));

            // MÉTODO 1: Intentar obtener usuario desde middleware
            Usuario usuario = ctx.attribute("usuario");

            if (usuario == null) {
                System.out.println("⚠️ AuthController: Usuario no encontrado en attributes, intentando con cookie directamente");

                // MÉTODO 2: Si el middleware falló, intentar directamente con la cookie
                String sessionId = ctx.cookie("HM_SESSION");

                if (sessionId == null || sessionId.trim().isEmpty()) {
                    System.out.println("❌ AuthController: No hay cookie de sesión");
                    ctx.status(HttpStatus.UNAUTHORIZED)
                            .json(createErrorResponse("No autorizado", "Debe iniciar sesión para acceder al perfil"));
                    return;
                }

                System.out.println("🔍 AuthController: Verificando sesión directamente: " + sessionId.substring(0, Math.min(10, sessionId.length())) + "...");

                // Verificar sesión directamente
                usuario = authService.getUserBySession(sessionId);

                if (usuario == null) {
                    System.out.println("❌ AuthController: Sesión inválida");
                    ctx.status(HttpStatus.UNAUTHORIZED)
                            .json(createErrorResponse("Sesión inválida", "La sesión ha expirado, debe iniciar sesión nuevamente"));
                    return;
                }
            }

            System.out.println("✅ AuthController: Usuario obtenido: " + usuario.getNombre_usuario());

            // Obtener información completa del usuario con permisos
            try {
                UsuarioConPermisos usuarioConPermisos = usuarioService.getUsuarioConPermisosByCorreo(usuario.getCorreo());

                // Crear respuesta completa del perfil
                Map<String, Object> profile = usuarioConPermisos.toResponseMap();
                profile.put("success", true);
                profile.put("message", "Perfil obtenido exitosamente");
                profile.put("timestamp", System.currentTimeMillis());

                // Agregar información adicional del perfil
                profile.put("session_info", Map.of(
                        "login_time", "Información de sesión disponible",
                        "last_activity", System.currentTimeMillis(),
                        "session_valid", true
                ));

                System.out.println("✅ AuthController: Perfil preparado con " + usuarioConPermisos.getPermisos().size() + " permisos");
                ctx.json(profile);

            } catch (Exception e) {
                System.err.println("⚠️ AuthController: Error obteniendo permisos, devolviendo perfil básico: " + e.getMessage());

                // Si falla obtener permisos, devolver al menos la información básica
                Map<String, Object> basicProfile = new HashMap<>();
                basicProfile.put("success", true);
                basicProfile.put("message", "Perfil básico obtenido exitosamente");
                basicProfile.put("usuario", Map.of(
                        "id_usuario", usuario.getId_usuario(),
                        "nombre_usuario", usuario.getNombre_usuario(),
                        "correo", usuario.getCorreo(),
                        "activo", usuario.isActivo(),
                        "id_rol", usuario.getId_rol()
                ));
                basicProfile.put("warning", "No se pudieron cargar los permisos");
                basicProfile.put("timestamp", System.currentTimeMillis());

                ctx.json(basicProfile);
            }

        } catch (Exception e) {
            System.err.println("❌ AuthController: Error en getProfile: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno", "Error al obtener el perfil del usuario"));
        }
    }

    /**
     * PUT /hm/auth/change-password - Cambiar contraseña - CORREGIDO
     */
    public void changePassword(Context ctx) {
        try {
            System.out.println("🔒 AuthController: Iniciando cambio de contraseña");

            // Obtener usuario autenticado
            Usuario usuario = ctx.attribute("usuario");
            if (usuario == null) {
                ctx.status(HttpStatus.UNAUTHORIZED)
                        .json(createErrorResponse("No autorizado", "Debe estar autenticado para cambiar la contraseña"));
                return;
            }

            // Obtener datos del cuerpo de la petición
            Map<String, String> passwords = ctx.bodyAsClass(Map.class);
            String contrasenaActual = passwords.get("contrasena_actual");
            String contrasenaNueva = passwords.get("contrasena_nueva");

            // Validaciones
            if (contrasenaActual == null || contrasenaActual.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos incompletos", "La contraseña actual es requerida"));
                return;
            }

            if (contrasenaNueva == null || contrasenaNueva.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos incompletos", "La nueva contraseña es requerida"));
                return;
            }

            if (contrasenaNueva.length() < 6) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Contraseña inválida", "La nueva contraseña debe tener al menos 6 caracteres"));
                return;
            }

            // CORREGIDO: Usar changePassword con ID de usuario
            boolean cambiada = authService.changePassword(usuario.getId_usuario(), contrasenaActual, contrasenaNueva);

            if (!cambiada) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Error", "La contraseña actual no es correcta"));
                return;
            }

            System.out.println("✅ Contraseña cambiada para usuario: " + usuario.getNombre_usuario());

            ctx.json(Map.of(
                    "success", true,
                    "message", "Contraseña cambiada exitosamente",
                    "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            System.err.println("❌ Error en changePassword: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno", "Error al cambiar la contraseña"));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}