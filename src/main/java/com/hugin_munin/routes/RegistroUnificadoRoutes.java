// ==================== RegistroUnificadoRoutes.java ====================
package com.hugin_munin.routes;

import com.hugin_munin.controller.RegistroUnificadoController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para registro unificado
 * Maneja la creación coordinada de especie, especimen y registro de alta
 */
public class RegistroUnificadoRoutes {
    private final RegistroUnificadoController controller;

    public RegistroUnificadoRoutes(RegistroUnificadoController controller) {
        this.controller = controller;
    }

    public void defineRoutes(Javalin app) {
        // POST - Crear registro unificado (especie + especimen + registro alta)
        app.post("/hm/registro-unificado", controller::createUnifiedRegistration);

        // POST - Validar datos antes de crear
        app.post("/hm/registro-unificado/validar", controller::validateUnifiedRegistration);

        // GET - Obtener datos necesarios para el formulario
        app.get("/hm/registro-unificado/formulario-data", controller::getFormData);

        // GET - Obtener ejemplo de estructura JSON
        app.get("/hm/registro-unificado/ejemplo", controller::getExampleStructure);
    }
}