package com.hugin_munin.routes;

import com.hugin_munin.controller.AuthController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para autenticación
 */
public class AuthRoutes {

    private final AuthController authController;

    public AuthRoutes(AuthController authController) {
        this.authController = authController;
    }

    public void defineRoutes(Javalin app) {

        // POST - Iniciar sesión
        app.post("/hm/auth/login", authController::login);

        // POST - Cerrar sesión
        app.post("/hm/auth/logout", authController::logout);

        // GET - Verificar sesión actual
        app.get("/hm/auth/verify", authController::verifySession);

        // GET - Obtener perfil del usuario autenticado
        app.get("/hm/auth/profile", authController::getProfile);

        // PUT - Cambiar contraseña
        app.put("/hm/auth/change-password", authController::changePassword);
    }
}