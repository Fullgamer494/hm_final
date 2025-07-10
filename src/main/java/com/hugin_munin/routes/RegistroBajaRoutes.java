package com.hugin_munin.routes;

import com.hugin_munin.controller.RegistroBajaController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para registros de baja
 */
public class RegistroBajaRoutes {

    private final RegistroBajaController controller;

    public RegistroBajaRoutes(RegistroBajaController controller) {
        this.controller = controller;
    }

    public void defineRoutes(Javalin app) {

        // CRUD básico
        app.get("/hm/registro_baja", controller::getAll);
        app.get("/hm/registro_baja/{id}", controller::getById);
        app.post("/hm/registro_baja", controller::create);
        app.put("/hm/registro_baja/{id}", controller::update);
        app.delete("/hm/registro_baja/{id}", controller::delete);

        // Búsquedas específicas
        app.get("/hm/registro_baja/especimen/{id}", controller::getByEspecimen);
        app.get("/hm/registro_baja/causa/{id}", controller::getByCausaBaja);
        app.get("/hm/registro_baja/responsable/{id}", controller::getByResponsable);

        // Estadísticas y reportes
        app.get("/hm/registro_baja/estadisticas/causas", controller::getEstadisticasPorCausa);
        app.get("/hm/registro_baja/estadisticas/general", controller::getEstadisticasGenerales);

        // Verificaciones
        app.get("/hm/registro_baja/verificar/{id}", controller::verificarEspecimenDadoDeBaja);
    }
}