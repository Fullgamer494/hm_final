package com.hugin_munin.controller;

import com.hugin_munin.model.CausaBaja;
import com.hugin_munin.service.CausaBajaService;
import com.hugin_munin.repository.CausaBajaRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar causas de baja
 */
public class CausaBajaController {

    private final CausaBajaService causaBajaService;

    public CausaBajaController(CausaBajaService causaBajaService) {
        this.causaBajaService = causaBajaService;
    }

    /**
     * GET /hm/causas-baja - Obtener todas las causas de baja
     */
    public void getAllCausas(Context ctx) {
        try {
            List<CausaBaja> causas = causaBajaService.getAllCausas();
            ctx.json(Map.of(
                    "data", causas,
                    "total", causas.size(),
                    "message", "Causas de baja obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener causas de baja", e.getMessage()));
        }
    }

    /**
     * GET /hm/causas-baja/{id} - Obtener causa de baja por ID
     */
    public void getCausaById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            CausaBaja causa = causaBajaService.getCausaById(id);

            ctx.json(Map.of(
                    "data", causa,
                    "message", "Causa de baja encontrada exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Causa de baja no encontrada", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar causa de baja", e.getMessage()));
        }
    }

    /**
     * GET /hm/causas-baja/search?nombre= - Buscar causas por nombre
     */
    public void searchCausasByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<CausaBaja> causas = causaBajaService.searchCausasByName(nombre);

            ctx.json(Map.of(
                    "data", causas,
                    "total", causas.size(),
                    "search_term", nombre,
                    "message", String.format("Se encontraron %d causas que coinciden con la búsqueda", causas.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/causas-baja - Crear nueva causa de baja
     */
    public void createCausa(Context ctx) {
        try {
            CausaBaja nuevaCausa = ctx.bodyAsClass(CausaBaja.class);
            CausaBaja causaCreada = causaBajaService.createCausa(nuevaCausa);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", causaCreada,
                            "message", "Causa de baja creada exitosamente",
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
                        .json(createErrorResponse("Error al crear causa de baja", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/causas-baja/{id} - Actualizar causa de baja existente
     */
    public void updateCausa(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            CausaBaja causaActualizada = ctx.bodyAsClass(CausaBaja.class);
            causaActualizada.setId_causa_baja(id);

            CausaBaja resultado = causaBajaService.updateCausa(causaActualizada);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Causa de baja actualizada exitosamente",
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
                    .json(createErrorResponse("Error al actualizar causa de baja", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/causas-baja/{id} - Eliminar causa de baja
     */
    public void deleteCausa(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = causaBajaService.deleteCausa(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Causa de baja eliminada exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Causa de baja no encontrada", "No se pudo eliminar la causa"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar causa de baja", e.getMessage()));
        }
    }

    /**
     * GET /hm/causas-baja/estadisticas - Obtener estadísticas de causas
     */
    public void getCausaStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = causaBajaService.getCausaStatistics();

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
     * GET /hm/causas-baja/populares?limit= - Obtener causas más utilizadas
     */
    public void getCausasPopulares(Context ctx) {
        try {
            String limitParam = ctx.queryParam("limit");
            int limit = 10; // Valor por defecto

            if (limitParam != null && !limitParam.trim().isEmpty()) {
                try {
                    limit = Integer.parseInt(limitParam);
                    if (limit <= 0) limit = 10;
                } catch (NumberFormatException e) {
                    // Usar valor por defecto si no es un número válido
                }
            }

            List<CausaBajaRepository.CausaEstadistica> populares =
                    causaBajaService.getCausasPopulares(limit);

            ctx.json(Map.of(
                    "data", populares,
                    "total", populares.size(),
                    "limit", limit,
                    "message", "Causas populares obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener causas populares", e.getMessage()));
        }
    }

    /**
     * GET /hm/causas-baja/actividad-reciente - Obtener causas con actividad reciente
     */
    public void getCausasConActividadReciente(Context ctx) {
        try {
            List<CausaBajaRepository.CausaEstadistica> activas =
                    causaBajaService.getCausasConActividadReciente();

            ctx.json(Map.of(
                    "data", activas,
                    "total", activas.size(),
                    "message", "Causas con actividad reciente obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener causas con actividad reciente", e.getMessage()));
        }
    }

    /**
     * POST /hm/causas-baja/validar-nombre - Validar si un nombre está disponible
     */
    public void validateCausaName(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String nombre = requestBody.get("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nombre requerido", "Debe proporcionar un nombre"));
                return;
            }

            boolean disponible = causaBajaService.isCausaNameAvailable(nombre);

            ctx.json(Map.of(
                    "nombre", nombre,
                    "disponible", disponible,
                    "message", disponible ? "Nombre disponible" : "Nombre ya está en uso"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al validar nombre", e.getMessage()));
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