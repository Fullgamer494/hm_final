package com.hugin_munin.controller;

import com.hugin_munin.model.Especimen;
import com.hugin_munin.service.EspecimenService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class EspecimenController {
    private final EspecimenService especimenService;

    public EspecimenController(EspecimenService especimenService) {this.especimenService = especimenService;}

    /**
     * GET /hm/especies
     * GET ALL
     **/
    public void getAllSpecimens(Context ctx){
        try{
            List<Especimen> especimenes = especimenService.getAllSpecimens();
            ctx.json(especimenes);
        }
        catch(Exception e){
            ctx.status(500).result("Error al obtener los especímenes");
        }
    }

    /**
     * POST /hm/especies
     */
    public void postSpecimen(Context ctx) {
        try {
            Especimen newSpecimens = ctx.bodyAsClass(Especimen.class);
            Especimen createdSpecies = especimenService.createSpecimen(newSpecimens);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", createdSpecies,
                            "message", "Especimen creada exitosamente",
                            "success", true
                    ));

        }
        catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        }
        catch (Exception e) {
            if (e.getMessage().contains("Ya existe")) {
                ctx.status(HttpStatus.CONFLICT)
                        .json(createErrorResponse("Conflicto", e.getMessage()));
            }
            else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(createErrorResponse("Error interno del servidor", e.getMessage()));
            }
        }
    }

    /**
     * ERROR RESPONSE
     **/
    private Map<String, Object> createErrorResponse(String error, String details) {
        return Map.of(
                "success", false,
                "error", error,
                "details", details,
                "timestamp", System.currentTimeMillis()
        );
    }
}
