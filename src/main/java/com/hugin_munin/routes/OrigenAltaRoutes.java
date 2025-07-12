package com.hugin_munin.routes;

import com.hugin_munin.controller.OrigenAltaController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para orígenes de alta
 */
public class OrigenAltaRoutes {

    private final OrigenAltaController origenAltaController;

    public OrigenAltaRoutes(OrigenAltaController origenAltaController) {
        this.origenAltaController = origenAltaController;
    }

    public void defineRoutes(Javalin app) {

        // GET - Obtener todos los orígenes de alta
        app.get("/hm/origenes-alta", origenAltaController::getAllOrigenes);

        // GET - Obtener origen de alta por ID
        app.get("/hm/origenes-alta/{id}", origenAltaController::getOrigenById);

        // GET - Buscar orígenes por nombre
        app.get("/hm/origenes-alta/search", origenAltaController::searchOrigenesByName);

        // POST - Crear nuevo origen de alta
        app.post("/hm/origenes-alta", origenAltaController::createOrigen);

        // PUT - Actualizar origen de alta
        app.put("/hm/origenes-alta/{id}", origenAltaController::updateOrigen);

        // DELETE - Eliminar origen de alta
        app.delete("/hm/origenes_alta/{id}", origenAltaController::deleteOrigen);

        // GET - Estadísticas de orígenes
        app.get("/hm/origenes_alta/estadisticas", origenAltaController::getOrigenStatistics);

        // GET - Orígenes más populares
        app.get("/hm/origenes_alta/populares", origenAltaController::getOrigenesPopulares);

        // GET - Orígenes con actividad reciente
        app.get("/hm/origenes_alta/actividad-reciente", origenAltaController::getOrigenesConActividadReciente);

        // POST - Validar nombre de origen
        app.post("/hm/origenes_alta/validar-nombre", origenAltaController::validateOrigenName);
    }
}