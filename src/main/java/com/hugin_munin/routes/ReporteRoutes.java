package com.hugin_munin.routes;

import com.hugin_munin.controller.ReporteController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para reportes (clase padre)
 */
public class ReporteRoutes {

    private final ReporteController reporteController;

    public ReporteRoutes(ReporteController reporteController) {
        this.reporteController = reporteController;
    }

    public void defineRoutes(Javalin app) {

        // CRUD básico
        app.get("/hm/reportes", reporteController::getAllReportes);
        app.get("/hm/reportes/activos", reporteController::getActiveReportes);
        app.get("/hm/reportes/{id}", reporteController::getReporteById);
        app.post("/hm/reportes", reporteController::createReporte);
        app.put("/hm/reportes/{id}", reporteController::updateReporte);
        app.delete("/hm/reportes/{id}", reporteController::deleteReporte);

        // Activar/Desactivar
        app.patch("/hm/reportes/{id}/activar", reporteController::activateReporte);
        app.patch("/hm/reportes/{id}/desactivar", reporteController::deactivateReporte);

        // Búsquedas específicas por atributos
        app.get("/hm/reportes/tipo/{id}", reporteController::getReportesByTipo);
        app.get("/hm/reportes/especimen/{id}", reporteController::getReportesByEspecimen);
        app.get("/hm/reportes/responsable/{id}", reporteController::getReportesByResponsable);

        // Búsquedas por contenido
        app.get("/hm/reportes/search/asunto", reporteController::searchReportesByAsunto);
        app.get("/hm/reportes/search/contenido", reporteController::searchReportesByContenido);

        // Búsqueda por fechas
        app.get("/hm/reportes/fechas", reporteController::getReportesByDateRange);

        // Estadísticas
        app.get("/hm/reportes/estadisticas", reporteController::getReporteStatistics);
    }
}