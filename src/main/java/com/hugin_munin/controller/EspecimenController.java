package com.hugin_munin.controller;

import com.hugin_munin.model.Especimen;
import com.hugin_munin.service.EspecimenService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar especímenes
 * Maneja todas las operaciones CRUD para especímenes
 */
public class EspecimenController {
    private final EspecimenService especimenService;

    public EspecimenController(EspecimenService especimenService) {
        this.especimenService = especimenService;
    }

    /**
     * GET /hm/especimenes - Obtener todos los especímenes
     */
    public void getAllSpecimens(Context ctx) {
        try {
            List<Especimen> especimenes = especimenService.getAllSpecimens();
            ctx.json(Map.of(
                    "data", especimenes,
                    "total", especimenes.size(),
                    "message", "Especímenes obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener especímenes", e.getMessage()));
        }
    }

    /**
     * GET /hm/especimenes/{id} - Obtener especimen por ID
     */
    public void getSpecimenById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Especimen especimen = especimenService.getSpecimenById(id);

            ctx.json(Map.of(
                    "data", especimen,
                    "message", "Especimen encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Especimen no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar especimen", e.getMessage()));
        }
    }

    /**
     * GET /hm/especimenes/activos - Obtener especímenes activos
     */
    public void getActiveSpecimens(Context ctx) {
        try {
            List<Especimen> especimenes = especimenService.getActiveSpecimens();
            ctx.json(Map.of(
                    "data", especimenes,
                    "total", especimenes.size(),
                    "message", "Especímenes activos obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener especímenes activos", e.getMessage()));
        }
    }

    /**
     * GET /hm/especimenes/search?nombre= - Buscar especímenes por nombre
     */
    public void searchSpecimensByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar 'nombre'"));
                return;
            }

            List<Especimen> especimenes = especimenService.searchSpecimensByName(nombre);

            ctx.json(Map.of(
                    "data", especimenes,
                    "total", especimenes.size(),
                    "search_term", nombre,
                    "message", String.format("Se encontraron %d especímenes que coinciden con la búsqueda", especimenes.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/especimenes - Crear nuevo especimen
     */
    public void createSpecimen(Context ctx) {
        try {
            Especimen newSpecimens = ctx.bodyAsClass(Especimen.class);
            Especimen createdSpecimen = especimenService.createSpecimen(newSpecimens);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", createdSpecimen,
                            "message", "Especimen creado exitosamente",
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
     * PUT /hm/especimenes/{id} - Actualizar especimen existente
     */
    public void updateSpecimen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Especimen especimenActualizado = ctx.bodyAsClass(Especimen.class);
            especimenActualizado.setId_especimen(id);

            Especimen resultado = especimenService.updateSpecimen(especimenActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Especimen actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar especimen", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/especimenes/{id} - Eliminar especimen
     */
    public void deleteSpecimen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = especimenService.deleteSpecimen(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Especimen eliminado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Especimen no encontrado", "No se pudo eliminar el especimen"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar especimen", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/especimenes/{id}/activar - Activar especimen
     */
    public void activateSpecimen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean activado = especimenService.activateSpecimen(id);

            if (activado) {
                ctx.json(Map.of(
                        "message", "Especimen activado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Especimen no encontrado", "No se pudo activar el especimen"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al activar especimen", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/especimenes/{id}/desactivar - Desactivar especimen
     */
    public void deactivateSpecimen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean desactivado = especimenService.deactivateSpecimen(id);

            if (desactivado) {
                ctx.json(Map.of(
                        "message", "Especimen desactivado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Especimen no encontrado", "No se pudo desactivar el especimen"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al desactivar especimen", e.getMessage()));
        }
    }

    /**
     * POST /hm/especimenes/validar-inventario - Validar si un número de inventario está disponible
     */
    public void validateInventoryNumber(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String numInventario = requestBody.get("num_inventario");

            if (numInventario == null || numInventario.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Número de inventario requerido", "Debe proporcionar un número de inventario"));
                return;
            }

            boolean disponible = especimenService.isInventoryNumberAvailable(numInventario);

            ctx.json(Map.of(
                    "num_inventario", numInventario,
                    "disponible", disponible,
                    "message", disponible ? "Número de inventario disponible" : "Número de inventario ya está en uso"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al validar número de inventario", e.getMessage()));
        }
    }

    /**
     * GET /hm/especimenes/estadisticas - Obtener estadísticas de especímenes
     */
    public void getSpecimenStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = especimenService.getSpecimenStatistics();

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