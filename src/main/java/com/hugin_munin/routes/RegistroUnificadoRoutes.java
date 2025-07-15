// ==================== RegistroUnificadoRoutes.java ====================
package com.hugin_munin.routes;

import com.hugin_munin.controller.RegistroUnificadoController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para registro unificado
 * Maneja CRUD completo de la creación coordinada de especie, especimen y registro de alta
 */
public class RegistroUnificadoRoutes {
    private final RegistroUnificadoController controller;

    public RegistroUnificadoRoutes(RegistroUnificadoController controller) {
        this.controller = controller;
    }

    public void defineRoutes(Javalin app) {
        // POST - Crear registro unificado (especie + especimen + registro alta)
        app.post("/hm/registro_unificado", controller::createUnifiedRegistration);

        // GET - Obtener registro unificado completo por ID de especimen
        app.get("/hm/registro_unificado/{id_especimen}", controller::getUnifiedRegistration);

        // PUT - Actualizar registro unificado completo
        app.put("/hm/registro_unificado/{id_especimen}", controller::updateUnifiedRegistration);

        // POST - Validar datos antes de crear
        app.post("/hm/registro_unificado/validar", controller::validateUnifiedRegistration);

        // GET - Obtener datos necesarios para el formulario
        app.get("/hm/registro_unificado/formulario-data", controller::getFormData);

        // GET - Obtener ejemplo de estructura JSON
        app.get("/hm/registro_unificado/ejemplo", controller::getExampleStructure);

        // GET - Listar todos los registros unificados (paginado)
        app.get("/hm/registro_unificado", controller::listUnifiedRegistrations);
    }
}