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
        app.get("/hm/causas-baja", causaBajaController::getAllCausas);

        // GET - Obtener causa de baja por ID
        app.get("/hm/causas-baja/{id}", causaBajaController::getCausaById);

        // GET - Buscar causas por nombre
        app.get("/hm/causas-baja/search", causaBajaController::searchCausasByName);

        // POST - Crear nueva causa de baja
        app.post("/hm/causas-baja", causaBajaController::createCausa);

        // PUT - Actualizar causa de baja
        app.put("/hm/causas-baja/{id}", causaBajaController::updateCausa);

        // DELETE - Eliminar causa de baja
        app.delete("/hm/causas-baja/{id}", causaBajaController::deleteCausa);

        // GET - Estadísticas de causas
        app.get("/hm/causas-baja/estadisticas", causaBajaController::getCausaStatistics);

        // GET - Causas más populares
        app.get("/hm/causas-baja/populares", causaBajaController::getCausasPopulares);

        // GET - Causas con actividad reciente
        app.get("/hm/causas-baja/actividad-reciente", causaBajaController::getCausasConActividadReciente);

        // POST - Validar nombre de causa
        app.post("/hm/causas-baja/validar-nombre", causaBajaController::validateCausaName);
    }
}