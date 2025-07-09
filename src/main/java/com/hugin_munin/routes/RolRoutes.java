package com.hugin_munin.routes;

import com.hugin_munin.controller.RolController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para roles
 */
public class RolRoutes {

    private final RolController rolController;

    public RolRoutes(RolController rolController) {
        this.rolController = rolController;
    }

    public void defineRoutes(Javalin app) {

        // GET - Obtener todos los roles
        app.get("/hm/roles", rolController::getAllRoles);

        // GET - Obtener roles activos
        app.get("/hm/roles/activos", rolController::getActiveRoles);

        // GET - Obtener rol por ID
        app.get("/hm/roles/{id}", rolController::getRolById);

        // GET - Buscar roles por nombre
        app.get("/hm/roles/search", rolController::searchRolesByName);

        // POST - Crear nuevo rol
        app.post("/hm/roles", rolController::createRol);

        // PUT - Actualizar rol
        app.put("/hm/roles/{id}", rolController::updateRol);

        // DELETE - Eliminar rol
        app.delete("/hm/roles/{id}", rolController::deleteRol);

        // PATCH - Activar rol
        app.patch("/hm/roles/{id}/activar", rolController::activateRol);

        // PATCH - Desactivar rol
        app.patch("/hm/roles/{id}/desactivar", rolController::deactivateRol);

        // GET - Estadísticas de roles
        app.get("/hm/roles/estadisticas", rolController::getRoleStatistics);

        // POST - Validar nombre de rol
        app.post("/hm/roles/validar-nombre", rolController::validateRoleName);
    }
}