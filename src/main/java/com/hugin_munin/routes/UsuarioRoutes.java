package com.hugin_munin.routes;

import com.hugin_munin.controller.UsuarioController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para usuarios
 */
public class UsuarioRoutes {

    private final UsuarioController usuarioController;

    public UsuarioRoutes(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
    }

    public void defineRoutes(Javalin app) {

        // GET - Obtener todos los usuarios
        app.get("/hm/usuarios", usuarioController::getAllUsers);

        // GET - Obtener usuario por ID
        app.get("/hm/usuarios/{id}", usuarioController::getUserById);

        // GET - Buscar usuarios por nombre
        app.get("/hm/usuarios/search", usuarioController::searchUsersByName);

        // POST - Crear nuevo usuario
        app.post("/hm/usuarios", usuarioController::createUser);

        // PUT - Actualizar usuario
        app.put("/hm/usuarios/{id}", usuarioController::updateUser);

        // DELETE - Eliminar usuario
        app.delete("/hm/usuarios/{id}", usuarioController::deleteUser);

        // GET - Estadísticas de usuarios
        app.get("/hm/usuarios/estadisticas", usuarioController::getUserStatistics);

        // POST - Validar email
        app.post("/hm/usuarios/validar-email", usuarioController::validateEmail);
    }
}