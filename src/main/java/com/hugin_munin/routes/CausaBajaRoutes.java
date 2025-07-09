package com.hugin_munin.routes;

import com.hugin_munin.controller.CausaBajaController;
import io.javalin.Javalin;

public class CausaBajaRoutes {
    private final CausaBajaController causaBajaController;
    public CausaBajaRoutes(CausaBajaController causaController) {
        this.causaBajaController = causaController;
    }
    public void register(Javalin app) {
        app.get("/causabaja", causaBajaController::getAll);
        app.get("/causabaja/{id}", causaBajaController::getById);
        app.put("/causabaja/{id}", causaBajaController::update);
        //app.post("/causabaja", causaBajaController::create);
        //app.get("/products/{id}", productController::getById);
        // app.put("/products/:id", productController::update);
        // app.delete("/products/:id", productController::delete);
    }
}