package com.hugin_munin.routes;

import com.hugin_munin.controller.RegistroAltaController;
import io.javalin.Javalin;

public class RegistroAltaRoutes {

    private final RegistroAltaController controller;

    public RegistroAltaRoutes(RegistroAltaController controller) {
        this.controller = controller;
    }

    public void defineRoutes(Javalin app) {
        app.get("/hm/registro_alta", controller::getAll);
        app.get("/hm/registro_alta/{id}", controller::getById);
        app.post("/hm/registro_alta", controller::create);
        app.put("/hm/registro_alta/{id}", controller::update);
        app.delete("/hm/registro_alta/{id}", controller::delete);
    }
}
