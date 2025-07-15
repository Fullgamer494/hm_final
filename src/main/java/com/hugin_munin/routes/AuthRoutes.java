package com.hugin_munin.routes;

import com.hugin_munin.controller.AuthController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para autenticación - COMPLETO Y CORREGIDO
 * IMPORTANTE: Estas rutas se configuran ANTES del middleware de autenticación
 * CORREGIDO: Logs de debugging para confirmar configuración
 */
public class AuthRoutes {

    private final AuthController authController;

    public AuthRoutes(AuthController authController) {
        this.authController = authController;
    }

    public void defineRoutes(Javalin app) {
        System.out.println("🔧 AuthRoutes: Configurando rutas de autenticación...");

        // ========================================
        // RUTAS PÚBLICAS (no requieren autenticación)
        // ========================================

        // POST - Iniciar sesión
        app.post("/hm/auth/login", authController::login);
        System.out.println("✅ Ruta configurada: POST /hm/auth/login (PÚBLICA)");

        // POST - Cerrar sesión
        app.post("/hm/auth/logout", authController::logout);
        System.out.println("✅ Ruta configurada: POST /hm/auth/logout (PÚBLICA)");

        // GET - Verificar sesión actual
        app.get("/hm/auth/verify", authController::verifySession);
        System.out.println("✅ Ruta configurada: GET /hm/auth/verify (PÚBLICA)");

        // ========================================
        // RUTAS PROTEGIDAS (requieren autenticación)
        // ========================================

        // GET - Obtener perfil del usuario autenticado
        app.get("/hm/auth/profile", authController::getProfile);
        System.out.println("✅ Ruta configurada: GET /hm/auth/profile (PROTEGIDA - requiere autenticación)");

        // PUT - Cambiar contraseña
        app.put("/hm/auth/change-password", authController::changePassword);
        System.out.println("✅ Ruta configurada: PUT /hm/auth/change-password (PROTEGIDA)");

        System.out.println("🔧 AuthRoutes: ✅ Todas las rutas de autenticación configuradas correctamente");
        System.out.println("📋 AuthRoutes: 3 rutas públicas + 2 rutas protegidas = 5 rutas totales\n");
    }
}