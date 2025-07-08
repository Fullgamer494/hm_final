package com.hugin_munin.routes;

import com.hugin_munin.controller.EspecieController;

import io.javalin.Javalin;

public class EspecieRoutes {
    private final EspecieController especieController;

    public EspecieRoutes(EspecieController especieController) { this.especieController = especieController; }

    public void defineRoutes(Javalin app) {
        /**
         * GET ALL SPECIES (NO PARAMS)
         */
        app.get("/hm/especies", especieController::getAllSpecies);

        /**
         * GET BY SCIENTIFIC_NAME (PARAMS EXAMPLE: /hm/especies/search?scientific_name="")
         */
        app.get("/hm/especies/search", especieController::getSpeciesByScientificName);

        /**
         * POST SPECIE (JSON PARAMS FORMAT)
         */
        app.post("/hm/especies", especieController::postSpecie);
    }
}