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
 * Middleware de autenticación para verificar sesiones
 * Protege rutas que requieren autenticación
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

    // Rutas que requieren autenticación pero son de solo lectura
    private static final List<String> READ_ONLY_ROUTES = Arrays.asList(
            "/hm/especies",
            "/hm/especimenes",
            "/hm/roles",
            "/hm/usuarios",
            "/hm/origenes-alta",
            "/hm/causas-baja",
            "/hm/tipos-reporte",
            "/hm/reportes",
            "/hm/reportes-traslado"
    );

    public AuthMiddleware(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handler principal del middleware
     */
    public Handler handle() {
        return ctx -> {
            String path = ctx.path();
            String method = ctx.method().toString();

            // Permitir rutas públicas sin autenticación
            if (isPublicRoute(path)) {
                return;
            }

            // Verificar autenticación para todas las demás rutas
            Usuario usuario = authenticateRequest(ctx);

            if (usuario == null) {
                sendUnauthorizedResponse(ctx);
                return;
            }

            // Establecer usuario en el contexto para uso posterior
            ctx.sessionAttribute("usuario", usuario);
            ctx.sessionAttribute("user_id", usuario.getId_usuario());
            ctx.sessionAttribute("user_name", usuario.getNombre_usuario());
            ctx.sessionAttribute("user_role", usuario.getId_rol());

            // Log de acceso (opcional)
            logAccess(usuario, method, path);
        };
    }

    /**
     * Middleware específico para rutas que requieren rol de administrador
     */
    public Handler requireAdmin() {
        return ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");

            if (usuario == null) {
                sendUnauthorizedResponse(ctx);
                return;
            }

            // Verificar si el usuario tiene rol de administrador (asumiendo rol ID 1 = admin)
            if (!isAdminUser(usuario)) {
                sendForbiddenResponse(ctx, "Se requieren permisos de administrador");
                return;
            }
        };
    }

    /**
     * Middleware para verificar que el usuario puede modificar datos
     */
    public Handler requireWritePermission() {
        return ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuario");

            if (usuario == null) {
                sendUnauthorizedResponse(ctx);
                return;
            }

            // Aquí puedes implementar lógica específica de permisos de escritura
            // Por ejemplo, verificar rol o permisos específicos
        };
    }

    /**
     * Autenticar request usando cookie de sesión
     */
    private Usuario authenticateRequest(Context ctx) {
        try {
            String sessionId = ctx.cookie("HM_SESSION");

            if (sessionId == null || sessionId.trim().isEmpty()) {
                return null;
            }

            // Verificar sesión y obtener usuario
            Usuario usuario = authService.getUserBySession(sessionId);

            if (usuario == null) {
                // Sesión inválida, limpiar cookies
                ctx.removeCookie("HM_SESSION");
                ctx.removeCookie("HM_USER_ID");
                ctx.removeCookie("HM_USER_NAME");
                return null;
            }

            return usuario;

        } catch (Exception e) {
            System.err.println("Error en autenticación: " + e.getMessage());
            return null;
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
        // Asumiendo que el rol ID 1 es administrador
        // Puedes ajustar esta lógica según tu esquema de roles
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
     * Registrar acceso en logs
     */
    private void logAccess(Usuario usuario, String method, String path) {
        System.out.println(String.format("🔐 Acceso: %s %s - Usuario: %s (ID: %d)",
                method, path, usuario.getNombre_usuario(), usuario.getId_usuario()));
    }

    /**
     * Handler para extraer información del usuario autenticado
     */
    public static Usuario getCurrentUser(Context ctx) {
        return ctx.sessionAttribute("usuario");
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