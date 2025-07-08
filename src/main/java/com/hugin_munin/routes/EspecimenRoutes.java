package com.hugin_munin.routes;

import com.hugin_munin.controller.EspecimenController;

import io.javalin.Javalin;

public class EspecimenRoutes {
    private final EspecimenController especimenController;

    public EspecimenRoutes(EspecimenController especimenController) { this.especimenController = especimenController; }

    public void defineRoutes(Javalin app) {

        /**
         * GET ALL SPECIMENS (NO PARAMS)
         */
        app.get("/hm/especimenes", especimenController::getAllSpecimens);

        /**
         * POST SPECIMEN (JSON PARAMS FORMAT)
         */
        app.post("/hm/especimenes", especimenController::postSpecimen);
    }
}