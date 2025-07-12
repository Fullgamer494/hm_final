package com.hugin_munin.routes;

import com.hugin_munin.controller.CausaBajaController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para causas de baja
 */
public class CausaBajaRoutes {

    private final CausaBajaController causaBajaController;

    public CausaBajaRoutes(CausaBajaController causaBajaController) {
        this.causaBajaController = causaBajaController;
    }

    public void defineRoutes(Javalin app) {

        // GET - Obtener todas las causas de baja
        app.get("/hm/causas_baja", causaBajaController::getAllCausas);

        // GET - Obtener causa de baja por ID
        app.get("/hm/causas_baja/{id}", causaBajaController::getCausaById);

        // POST - Crear nueva causa de baja
        app.post("/hm/causas_baja", causaBajaController::createCausa);

        // PUT - Actualizar causa de baja
        app.put("/hm/causas_baja/{id}", causaBajaController::updateCausa);

        // DELETE - Eliminar causa de baja
        app.delete("/hm/causas_baja/{id}", causaBajaController::deleteCausa);
    }
}