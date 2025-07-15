package com.hugin_munin.middleware;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.service.AuthService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

/**
 * Middleware de autenticación CORREGIDO - Sistema unificado
 * SOLO usa cookies personalizadas, NO usa sessionAttribute de Javalin
 * CORREGIDO: Lista de rutas públicas actualizada y debugging mejorado
 */
public class AuthMiddleware {

    private final AuthService authService;

    // Rutas que NO requieren autenticación - LISTA ACTUALIZADA
    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
            "/",
            "/hm/docs",
            "/hm/test-db",
            "/hm/auth/login",     // POST - login
            "/hm/auth/verify",    // GET - verificar sesión
            "/hm/auth/logout",    // POST - logout
            "/routes"
    );
    // IMPORTANTE: /hm/auth/profile NO está en PUBLIC_ROUTES porque SÍ requiere autenticación

    public AuthMiddleware(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handler principal del middleware - VERSIÓN CON DEBUG MEJORADO
     */
    public Handler handle() {
        return ctx -> {
            String path = ctx.path();
            String method = ctx.method().toString();

            System.out.println("🔍 Middleware verificando: " + method + " " + path);

            // Permitir rutas públicas sin autenticación
            if (isPublicRoute(path)) {
                System.out.println("✅ Ruta pública permitida: " + path);
                return;
            }

            // DEBUGGING ESPECÍFICO para /hm/auth/profile
            if (path.equals("/hm/auth/profile")) {
                System.out.println("🔍 DEBUG PROFILE: Verificando autenticación para profile");

                // Mostrar todas las cookies
                Map<String, String> cookies = ctx.cookieMap();
                System.out.println("🍪 Cookies disponibles: " + cookies.keySet());

                String sessionId = ctx.cookie("HM_SESSION");
                System.out.println("🍪 HM_SESSION cookie: " + (sessionId != null ? "presente (" + sessionId.substring(0, Math.min(10, sessionId.length())) + "...)" : "AUSENTE"));

                if (sessionId != null) {
                    System.out.println("🔍 DEBUG: Intentando verificar sesión con AuthService...");
                }
            }

            // Verificar autenticación para todas las demás rutas
            Usuario usuario = authenticateRequest(ctx);

            if (usuario == null) {
                System.out.println("❌ Acceso denegado para: " + method + " " + path);
                sendUnauthorizedResponse(ctx);
                return;
            }

            // CRÍTICO: Usar attribute() en lugar de sessionAttribute()
            // Esto almacena los datos solo para esta request, no en sesión de Javalin
            ctx.attribute("usuario", usuario);
            ctx.attribute("user_id", usuario.getId_usuario());
            ctx.attribute("user_name", usuario.getNombre_usuario());
            ctx.attribute("user_role", usuario.getId_rol());

            System.out.println("✅ Usuario autenticado: " + usuario.getNombre_usuario() + " accediendo a " + path);
        };
    }

    /**
     * Middleware específico para rutas que requieren rol de administrador
     */
    public Handler requireAdmin() {
        return ctx -> {
            Usuario usuario = ctx.attribute("usuario");

            if (usuario == null) {
                sendUnauthorizedResponse(ctx);
                return;
            }

            if (!isAdminUser(usuario)) {
                sendForbiddenResponse(ctx, "Se requieren permisos de administrador");
                return;
            }
        };
    }

    /**
     * Autenticar request usando SOLO cookie personalizada - MÉTODO CON DEBUG MEJORADO
     */
    private Usuario authenticateRequest(Context ctx) {
        try {
            String sessionId = ctx.cookie("HM_SESSION");

            System.out.println("🍪 Cookie HM_SESSION: " + (sessionId != null ? "presente" : "ausente"));

            if (sessionId == null || sessionId.trim().isEmpty()) {
                System.out.println("⚠️ No hay cookie de sesión");
                return null;
            }

            System.out.println("🔍 Verificando sesión con AuthService...");

            // Verificar sesión usando AuthService
            Usuario usuario = authService.getUserBySession(sessionId);

            if (usuario == null) {
                System.out.println("⚠️ Sesión inválida, limpiando cookies");
                // Sesión inválida, limpiar TODAS las cookies
                clearAllAuthCookies(ctx);
                return null;
            }

            System.out.println("✅ Sesión válida para usuario: " + usuario.getNombre_usuario());
            return usuario;

        } catch (Exception e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
            e.printStackTrace();
            clearAllAuthCookies(ctx);
            return null;
        }
    }

    /**
     * Limpiar TODAS las cookies de autenticación - CRÍTICO
     */
    private void clearAllAuthCookies(Context ctx) {
        try {
            // Remover todas las cookies de autenticación con diferentes configuraciones

            // Configuración básica
            ctx.removeCookie("HM_SESSION");
            ctx.removeCookie("HM_USER_ID");
            ctx.removeCookie("HM_USER_NAME");

            // Configuración con path específico
            ctx.removeCookie("HM_SESSION", "/");
            ctx.removeCookie("HM_USER_ID", "/");
            ctx.removeCookie("HM_USER_NAME", "/");

            // Configuración adicional para asegurar eliminación
            ctx.cookie("HM_SESSION", "", 0);
            ctx.cookie("HM_USER_ID", "", 0);
            ctx.cookie("HM_USER_NAME", "", 0);

            System.out.println("🧹 Cookies de autenticación limpiadas");
        } catch (Exception e) {
            System.err.println("Error limpiando cookies: " + e.getMessage());
        }
    }

    /**
     * Verificar si una ruta es pública - MÉTODO MEJORADO
     */
    private boolean isPublicRoute(String path) {
        // Verificar rutas exactas
        for (String publicRoute : PUBLIC_ROUTES) {
            if (publicRoute.equals(path)) {
                return true;
            }
        }

        // Verificar prefijos específicos
        if (path.equals("/") || path.equals("/routes")) {
            return true;
        }

        // IMPORTANTE: Solo permitir rutas de auth específicas que están en PUBLIC_ROUTES
        // /hm/auth/profile NO está en PUBLIC_ROUTES, por lo que requiere autenticación
        if (path.startsWith("/hm/auth/")) {
            return PUBLIC_ROUTES.contains(path);
        }

        return false;
    }

    /**
     * Verificar si el usuario es administrador
     */
    private boolean isAdminUser(Usuario usuario) {
        return usuario.getId_rol() != null && usuario.getId_rol() == 1;
    }

    /**
     * Enviar respuesta de no autorizado
     */
    private void sendUnauthorizedResponse(Context ctx) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "No autorizado");
        response.put("message", "Debe iniciar sesión para acceder a este recurso");
        response.put("login_url", "/hm/auth/login");
        response.put("timestamp", System.currentTimeMillis());

        ctx.status(HttpStatus.UNAUTHORIZED).json(response);
    }

    /**
     * Enviar respuesta de prohibido
     */
    private void sendForbiddenResponse(Context ctx, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "Acceso prohibido");
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        ctx.status(HttpStatus.FORBIDDEN).json(response);
    }

    /**
     * Handler para extraer información del usuario autenticado
     */
    public static Usuario getCurrentUser(Context ctx) {
        return ctx.attribute("usuario");
    }

    /**
     * Verificar si el usuario actual es administrador
     */
    public static boolean isCurrentUserAdmin(Context ctx) {
        Usuario usuario = getCurrentUser(ctx);
        return usuario != null && usuario.getId_rol() != null && usuario.getId_rol() == 1;
    }

    /**
     * Obtener ID del usuario actual
     */
    public static Integer getCurrentUserId(Context ctx) {
        Usuario usuario = getCurrentUser(ctx);
        return usuario != null ? usuario.getId_usuario() : null;
    }
}