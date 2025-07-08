package com.hugin_munin.routes;

import com.hugin_munin.controller.RegistroAltaController;
import io.javalin.Javalin;

public class RegistroAltaRoutes {

    private final RegistroAltaController controller;

    public RegistroAltaRoutes(RegistroAltaController controller) {
        this.controller = controller;
    }

    public void defineRoutes(Javalin app) {
        app.get("/registro_alta", controller::getAll);
        app.get("/registro_alta/{id}", controller::getById);
        app.post("/registro_alta", controller::create);
        app.put("/registro_alta/{id}", controller::update);
        app.delete("/registro_alta/{id}", controller::delete);
    }
}
