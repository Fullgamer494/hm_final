package com.hugin_munin.controller;

import com.hugin_munin.model.TipoReporte;
import com.hugin_munin.service.TipoReporteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar tipos de reporte
 */
public class TipoReporteController {

    private final TipoReporteService tipoReporteService;

    public TipoReporteController(TipoReporteService tipoReporteService) {
        this.tipoReporteService = tipoReporteService;
    }

    /**
     * GET /hm/tipos-reporte - Obtener todos los tipos de reporte
     */
    public void getAllTipos(Context ctx) {
        try {
            List<TipoReporte> tipos = tipoReporteService.getAllTipos();
            ctx.json(Map.of(
                    "data", tipos,
                    "total", tipos.size(),
                    "message", "Tipos de reporte obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener tipos de reporte", e.getMessage()));
        }
    }

    /**
     * GET /hm/tipos-reporte/activos - Obtener tipos activos
     */
    public void getActiveTipos(Context ctx) {
        try {
            List<TipoReporte> tipos = tipoReporteService.getActiveTipos();
            ctx.json(Map.of(
                    "data", tipos,
                    "total", tipos.size(),
                    "message", "Tipos de reporte activos obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener tipos activos", e.getMessage()));
        }
    }

    /**
     * GET /hm/tipos-reporte/{id} - Obtener tipo por ID
     */
    public void getTipoById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TipoReporte tipo = tipoReporteService.getTipoById(id);

            ctx.json(Map.of(
                    "data", tipo,
                    "message", "Tipo de reporte encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Tipo de reporte no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar tipo de reporte", e.getMessage()));
        }
    }

    /**
     * GET /hm/tipos-reporte/search?nombre= - Buscar tipos por nombre
     */
    public void searchTiposByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<TipoReporte> tipos = tipoReporteService.searchTiposByName(nombre);

            ctx.json(Map.of(
                    "data", tipos,
                    "total", tipos.size(),
                    "search_term", nombre,
                    "message", String.format("Se encontraron %d tipos que coinciden con la búsqueda", tipos.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/tipos-reporte - Crear nuevo tipo
     */
    public void createTipo(Context ctx) {
        try {
            TipoReporte nuevoTipo = ctx.bodyAsClass(TipoReporte.class);
            TipoReporte tipoCreado = tipoReporteService.createTipo(nuevoTipo);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", tipoCreado,
                            "message", "Tipo de reporte creado exitosamente",
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
                        .json(createErrorResponse("Error al crear tipo de reporte", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/tipos-reporte/{id} - Actualizar tipo
     */
    public void updateTipo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            TipoReporte tipoActualizado = ctx.bodyAsClass(TipoReporte.class);
            tipoActualizado.setId_tipo_reporte(id);

            TipoReporte resultado = tipoReporteService.updateTipo(tipoActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Tipo de reporte actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar tipo de reporte", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/tipos-reporte/{id} - Eliminar tipo
     */
    public void deleteTipo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = tipoReporteService.deleteTipo(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Tipo de reporte eliminado o desactivado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Tipo no encontrado", "No se pudo eliminar el tipo"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar tipo de reporte", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/tipos-reporte/{id}/activar - Activar tipo
     */
    public void activateTipo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean activado = tipoReporteService.activateTipo(id);

            if (activado) {
                ctx.json(Map.of(
                        "message", "Tipo de reporte activado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Tipo no encontrado", "No se pudo activar el tipo"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al activar tipo de reporte", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/tipos-reporte/{id}/desactivar - Desactivar tipo
     */
    public void deactivateTipo(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean desactivado = tipoReporteService.deactivateTipo(id);

            if (desactivado) {
                ctx.json(Map.of(
                        "message", "Tipo de reporte desactivado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Tipo no encontrado", "No se pudo desactivar el tipo"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al desactivar tipo de reporte", e.getMessage()));
        }
    }

    /**
     * GET /hm/tipos-reporte/estadisticas - Obtener estadísticas
     */
    public void getTipoStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = tipoReporteService.getTipoStatistics();

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
     * POST /hm/tipos-reporte/validar-nombre - Validar nombre
     */
    public void validateTipoName(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String nombre = requestBody.get("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nombre requerido", "Debe proporcionar un nombre"));
                return;
            }

            boolean disponible = tipoReporteService.isTipoNameAvailable(nombre);

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