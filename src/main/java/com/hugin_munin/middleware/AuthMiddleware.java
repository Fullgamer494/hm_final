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
 */
public class AuthMiddleware {

    private final AuthService authService;

    // Rutas que NO requieren autenticación
    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
            "/",
            "/hm/docs",
            "/hm/auth/login",
            "/hm/auth/verify",
            "/routes"
    );

    public AuthMiddleware(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handler principal del middleware - VERSIÓN CORREGIDA
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
     * Autenticar request usando SOLO cookie personalizada
     */
    private Usuario authenticateRequest(Context ctx) {
        try {
            String sessionId = ctx.cookie("HM_SESSION");

            System.out.println("🍪 Cookie HM_SESSION: " + (sessionId != null ? "presente" : "ausente"));

            if (sessionId == null || sessionId.trim().isEmpty()) {
                System.out.println("⚠️ No hay cookie de sesión");
                return null;
            }

            // Verificar sesión usando AuthService
            Usuario usuario = authService.getUserBySession(sessionId);

            if (usuario == null) {
                System.out.println("⚠️ Sesión inválida, limpiando cookies");
                // Sesión inválida, limpiar TODAS las cookies
                clearAllAuthCookies(ctx);
                return null;
            }

            return usuario;

        } catch (Exception e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
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
     * Verificar si una ruta es pública
     */
    private boolean isPublicRoute(String path) {
        return PUBLIC_ROUTES.stream().anyMatch(publicRoute -> {
            if (publicRoute.equals(path)) {
                return true;
            }
            // Permitir rutas que empiecen con rutas públicas
            return path.startsWith(publicRoute);
        });
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