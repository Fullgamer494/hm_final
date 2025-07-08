package com.hugin_munin.controller;

import com.hugin_munin.model.Especie;
import com.hugin_munin.service.EspecieService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class EspecieController {
    private final EspecieService especieService;

    public EspecieController(EspecieService especieService) {this.especieService = especieService;}

    /**
     * GET /api/especies
     * GET ALL
     **/
    public void getAllSpecies(Context ctx){
        try{
            List<Especie> especies = especieService.getAllSpecies();
            ctx.json(especies);
        }
        catch(Exception e){
            ctx.status(500).result("Error al obtener las especies");
        }
    }


    /**
     * GET /api/especies/
     **/
    public void getSpeciesByScientificName(Context ctx){
        try {
            String scientific_name = ctx.queryParam("scientific_name");

            List<Especie> especies;
            String search_term;

            if (scientific_name != null && !scientific_name.trim().isEmpty()) {
                especies = especieService.getSpeciesByScientificName(scientific_name);
                search_term = scientific_name;
            }
            else {
                ctx.status(HttpStatus.BAD_REQUEST).json(createErrorResponse("Parámetros requeridos", "Debe proporcionar 'nombre cientifico'"));
                return;
            }

            ctx.json(Map.of(
                    "data", especies,
                    "total", especies.size(),
                    "search_term", search_term,
                    "message", String.format("Se encontraron %d especies que coinciden con la búsqueda", especies.size())
            ));

        }
        catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Parámetros inválidos", e.getMessage()));
        }
        catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    /**
     * POST /api/especies
     */
    public void postSpecie(Context ctx) {
        try {
            Especie newSpecies = ctx.bodyAsClass(Especie.class);
            Especie createdSpecies = especieService.createSpecie(newSpecies);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", createdSpecies,
                            "message", "Especie creada exitosamente",
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
