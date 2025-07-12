package com.hugin_munin.routes;

import com.hugin_munin.controller.TipoReporteController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para tipos de reporte
 */
public class TipoReporteRoutes {

    private final TipoReporteController tipoReporteController;

    public TipoReporteRoutes(TipoReporteController tipoReporteController) {
        this.tipoReporteController = tipoReporteController;
    }

    public void defineRoutes(Javalin app) {

        // GET - Obtener todos los tipos de reporte
        app.get("/hm/tipos-reporte", tipoReporteController::getAllTipos);

        // GET - Obtener tipos activos
        app.get("/hm/tipos-reporte/activos", tipoReporteController::getActiveTipos);

        // GET - Obtener tipo por ID
        app.get("/hm/tipos-reporte/{id}", tipoReporteController::getTipoById);

        // GET - Buscar tipos por nombre
        app.get("/hm/tipos-reporte/search", tipoReporteController::searchTiposByName);

        // POST - Crear nuevo tipo
        app.post("/hm/tipos-reporte", tipoReporteController::createTipo);

        // PUT - Actualizar tipo
        app.put("/hm/tipos-reporte/{id}", tipoReporteController::updateTipo);

        // DELETE - Eliminar tipo
        app.delete("/hm/tipos-reporte/{id}", tipoReporteController::deleteTipo);

        // PATCH - Activar tipo
        app.patch("/hm/tipos-reporte/{id}/activar", tipoReporteController::activateTipo);

        // PATCH - Desactivar tipo
        app.patch("/hm/tipos-reporte/{id}/desactivar", tipoReporteController::deactivateTipo);

        // GET - Estadísticas
        app.get("/hm/tipos-reporte/estadisticas", tipoReporteController::getTipoStatistics);

        // POST - Validar nombre
        app.post("/hm/tipos-reporte/validar-nombre", tipoReporteController::validateTipoName);
    }
}