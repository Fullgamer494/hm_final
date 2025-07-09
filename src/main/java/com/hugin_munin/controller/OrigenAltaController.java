package com.hugin_munin.controller;

import com.hugin_munin.model.OrigenAlta;
import com.hugin_munin.service.OrigenAltaService;
import com.hugin_munin.repository.OrigenAltaRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar orígenes de alta
 */
public class OrigenAltaController {

    private final OrigenAltaService origenAltaService;

    public OrigenAltaController(OrigenAltaService origenAltaService) {
        this.origenAltaService = origenAltaService;
    }

    /**
     * GET /hm/origenes-alta - Obtener todos los orígenes de alta
     */
    public void getAllOrigenes(Context ctx) {
        try {
            List<OrigenAlta> origenes = origenAltaService.getAllOrigenes();
            ctx.json(Map.of(
                    "data", origenes,
                    "total", origenes.size(),
                    "message", "Orígenes de alta obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener orígenes de alta", e.getMessage()));
        }
    }

    /**
     * GET /hm/origenes-alta/{id} - Obtener origen de alta por ID
     */
    public void getOrigenById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            OrigenAlta origen = origenAltaService.getOrigenById(id);

            ctx.json(Map.of(
                    "data", origen,
                    "message", "Origen de alta encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Origen de alta no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar origen de alta", e.getMessage()));
        }
    }

    /**
     * GET /hm/origenes-alta/search?nombre= - Buscar orígenes por nombre
     */
    public void searchOrigenesByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<OrigenAlta> origenes = origenAltaService.searchOrigenesByName(nombre);

            ctx.json(Map.of(
                    "data", origenes,
                    "total", origenes.size(),
                    "search_term", nombre,
                    "message", String.format("Se encontraron %d orígenes que coinciden con la búsqueda", origenes.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/origenes-alta - Crear nuevo origen de alta
     */
    public void createOrigen(Context ctx) {
        try {
            OrigenAlta nuevoOrigen = ctx.bodyAsClass(OrigenAlta.class);
            OrigenAlta origenCreado = origenAltaService.createOrigen(nuevoOrigen);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", origenCreado,
                            "message", "Origen de alta creado exitosamente",
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
                        .json(createErrorResponse("Error al crear origen de alta", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/origenes-alta/{id} - Actualizar origen de alta existente
     */
    public void updateOrigen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            OrigenAlta origenActualizado = ctx.bodyAsClass(OrigenAlta.class);
            origenActualizado.setId_origen_alta(id);

            OrigenAlta resultado = origenAltaService.updateOrigen(origenActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Origen de alta actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar origen de alta", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/origenes-alta/{id} - Eliminar origen de alta
     */
    public void deleteOrigen(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = origenAltaService.deleteOrigen(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Origen de alta eliminado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Origen de alta no encontrado", "No se pudo eliminar el origen"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar origen de alta", e.getMessage()));
        }
    }

    /**
     * GET /hm/origenes-alta/estadisticas - Obtener estadísticas de orígenes
     */
    public void getOrigenStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = origenAltaService.getOrigenStatistics();

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
     * GET /hm/origenes-alta/populares?limit= - Obtener orígenes más utilizados
     */
    public void getOrigenesPopulares(Context ctx) {
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

            List<OrigenAltaRepository.OrigenEstadistica> populares =
                    origenAltaService.getOrigenesPopulares(limit);

            ctx.json(Map.of(
                    "data", populares,
                    "total", populares.size(),
                    "limit", limit,
                    "message", "Orígenes populares obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener orígenes populares", e.getMessage()));
        }
    }

    /**
     * GET /hm/origenes-alta/actividad-reciente - Obtener orígenes con actividad reciente
     */
    public void getOrigenesConActividadReciente(Context ctx) {
        try {
            List<OrigenAltaRepository.OrigenEstadistica> activos =
                    origenAltaService.getOrigenesConActividadReciente();

            ctx.json(Map.of(
                    "data", activos,
                    "total", activos.size(),
                    "message", "Orígenes con actividad reciente obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener orígenes con actividad reciente", e.getMessage()));
        }
    }

    /**
     * POST /hm/origenes-alta/validar-nombre - Validar si un nombre está disponible
     */
    public void validateOrigenName(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String nombre = requestBody.get("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nombre requerido", "Debe proporcionar un nombre"));
                return;
            }

            boolean disponible = origenAltaService.isOrigenNameAvailable(nombre);

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