package com.hugin_munin.controller;

import com.hugin_munin.model.Especie;
import com.hugin_munin.service.EspecieService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar especies
 * Maneja todas las operaciones CRUD para especies
 */
public class EspecieController {
    private final EspecieService especieService;

    public EspecieController(EspecieService especieService) {
        this.especieService = especieService;
    }

    /**
     * GET /hm/especies - Obtener todas las especies
     */
    public void getAllSpecies(Context ctx) {
        try {
            List<Especie> especies = especieService.getAllSpecies();
            ctx.json(Map.of(
                    "data", especies,
                    "total", especies.size(),
                    "message", "Especies obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener especies", e.getMessage()));
        }
    }

    /**
     * GET /hm/especies/{id} - Obtener especie por ID
     */
    public void getSpecieById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Especie especie = especieService.getSpecieById(id);

            ctx.json(Map.of(
                    "data", especie,
                    "message", "Especie encontrada exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Especie no encontrada", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar especie", e.getMessage()));
        }
    }

    /**
     * GET /hm/especies/search?scientific_name= - Buscar especies por nombre científico
     */
    public void getSpeciesByScientificName(Context ctx) {
        try {
            String scientific_name = ctx.queryParam("scientific_name");

            if (scientific_name == null || scientific_name.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar 'scientific_name'"));
                return;
            }

            List<Especie> especies = especieService.getSpeciesByScientificName(scientific_name);

            ctx.json(Map.of(
                    "data", especies,
                    "total", especies.size(),
                    "search_term", scientific_name,
                    "message", String.format("Se encontraron %d especies que coinciden con la búsqueda", especies.size())
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Parámetros inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", e.getMessage()));
        }
    }

    /**
     * POST /hm/especies - Crear nueva especie
     */
    public void createSpecie(Context ctx) {
        try {
            Especie newSpecies = ctx.bodyAsClass(Especie.class);
            Especie createdSpecies = especieService.createSpecie(newSpecies);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", createdSpecies,
                            "message", "Especie creada exitosamente",
                            "success", true
                    ));

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            if (e.getMessage().contains("Ya existe")) {
                ctx.status(HttpStatus.CONFLICT)
                        .json(createErrorResponse("Conflicto", e.getMessage()));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(createErrorResponse("Error interno del servidor", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/especies/{id} - Actualizar especie existente
     */
    public void updateSpecie(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Especie especieActualizada = ctx.bodyAsClass(Especie.class);
            especieActualizada.setId_especie(id);

            Especie resultado = especieService.updateSpecie(especieActualizada);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Especie actualizada exitosamente",
                    "success", true
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al actualizar especie", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/especies/{id} - Eliminar especie
     */
    public void deleteSpecie(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = especieService.deleteSpecie(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Especie eliminada exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Especie no encontrada", "No se pudo eliminar la especie"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar especie", e.getMessage()));
        }
    }

    /**
     * POST /hm/especies/validar-nombre - Validar si un nombre científico está disponible
     */
    public void validateSpecieName(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String genero = requestBody.get("genero");
            String especie = requestBody.get("especie");

            if (genero == null || genero.trim().isEmpty() || especie == null || especie.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos requeridos", "Debe proporcionar género y especie"));
                return;
            }

            boolean disponible = especieService.isSpecieNameAvailable(genero, especie);

            ctx.json(Map.of(
                    "genero", genero,
                    "especie", especie,
                    "disponible", disponible,
                    "message", disponible ? "Nombre científico disponible" : "Nombre científico ya está en uso"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al validar nombre", e.getMessage()));
        }
    }

    /**
     * GET /hm/especies/estadisticas - Obtener estadísticas de especies
     */
    public void getSpecieStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = especieService.getSpecieStatistics();

            ctx.json(Map.of(
                    "data", estadisticas,
                    "message", "Estadísticas obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener estadísticas", e.getMessage()));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, Object> createErrorResponse(String error, String details) {
        return Map.of(
                "success", false,
                "error", error,
                "details", details,
                "timestamp", System.currentTimeMillis()
        );
    }
}