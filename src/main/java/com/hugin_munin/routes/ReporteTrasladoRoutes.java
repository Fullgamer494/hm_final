package com.hugin_munin.routes;

import com.hugin_munin.controller.ReporteTrasladoController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para reportes de traslado (clase hija)
 */
public class ReporteTrasladoRoutes {

    private final ReporteTrasladoController reporteTrasladoController;

    public ReporteTrasladoRoutes(ReporteTrasladoController reporteTrasladoController) {
        this.reporteTrasladoController = reporteTrasladoController;
    }

    public void defineRoutes(Javalin app) {

        // CRUD básico
        app.get("/hm/reportes-traslado", reporteTrasladoController::getAllReportesTraslado);
        app.get("/hm/reportes-traslado/{id}", reporteTrasladoController::getReporteTrasladoById);
        app.post("/hm/reportes-traslado", reporteTrasladoController::createReporteTraslado);
        app.put("/hm/reportes-traslado/{id}", reporteTrasladoController::updateReporteTraslado);
        app.delete("/hm/reportes-traslado/{id}", reporteTrasladoController::deleteReporteTraslado);

        // Búsquedas específicas por atributos de traslado
        app.get("/hm/reportes-traslado/area-origen/{area}", reporteTrasladoController::getReportesByAreaOrigen);
        app.get("/hm/reportes-traslado/area-destino/{area}", reporteTrasladoController::getReportesByAreaDestino);
        app.get("/hm/reportes-traslado/ubicacion-origen/{ubicacion}", reporteTrasladoController::getReportesByUbicacionOrigen);
        app.get("/hm/reportes-traslado/ubicacion-destino/{ubicacion}", reporteTrasladoController::getReportesByUbicacionDestino);

        // Búsqueda por motivo
        app.get("/hm/reportes-traslado/search/motivo", reporteTrasladoController::searchReportesByMotivo);

        // Búsquedas por atributos heredados del padre
        app.get("/hm/reportes-traslado/especimen/{id}", reporteTrasladoController::getReportesByEspecimen);
        app.get("/hm/reportes-traslado/responsable/{id}", reporteTrasladoController::getReportesByResponsable);
        app.get("/hm/reportes-traslado/fechas", reporteTrasladoController::getReportesByDateRange);

        // Estadísticas específicas de traslados
        app.get("/hm/reportes-traslado/estadisticas", reporteTrasladoController::getReporteTrasladoStatistics);
        app.get("/hm/reportes-traslado/estadisticas/areas-origen", reporteTrasladoController::getAreasOrigenPopulares);
        app.get("/hm/reportes-traslado/estadisticas/areas-destino", reporteTrasladoController::getAreasDestinoPopulares);
    }
}