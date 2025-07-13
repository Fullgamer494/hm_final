package com.hugin_munin.controller;

import com.hugin_munin.model.Reporte;
import com.hugin_munin.service.ReporteService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar reportes (clase padre)
 */
public class ReporteController {

    private final ReporteService reporteService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * GET /hm/reportes - Obtener todos los reportes
     */
    public void getAllReportes(Context ctx) {
        try {
            List<Reporte> reportes = reporteService.getAllReportes();
            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "message", "Reportes obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener reportes", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/{id} - Obtener reporte por ID
     */
    public void getReporteById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Reporte reporte = reporteService.getReporteById(id);

            ctx.json(Map.of(
                    "data", reporte,
                    "message", "Reporte encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Reporte no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reporte", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/tipo/{id} - Buscar reportes por tipo
     */
    public void getReportesByTipo(Context ctx) {
        try {
            int idTipoReporte = Integer.parseInt(ctx.pathParam("id"));
            List<Reporte> reportes = reporteService.getReportesByTipo(idTipoReporte);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "tipo_reporte_id", idTipoReporte,
                    "message", String.format("Se encontraron %d reportes de este tipo", reportes.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reportes por tipo", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/especimen/{id} - Buscar reportes por especimen
     */
    public void getReportesByEspecimen(Context ctx) {
        try {
            int idEspecimen = Integer.parseInt(ctx.pathParam("id"));
            List<Reporte> reportes = reporteService.getReportesByEspecimen(idEspecimen);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "especimen_id", idEspecimen,
                    "message", String.format("Se encontraron %d reportes para este especimen", reportes.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reportes por especimen", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/responsable/{id} - Buscar reportes por responsable
     */
    public void getReportesByResponsable(Context ctx) {
        try {
            int idResponsable = Integer.parseInt(ctx.pathParam("id"));
            List<Reporte> reportes = reporteService.getReportesByResponsable(idResponsable);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "responsable_id", idResponsable,
                    "message", String.format("Se encontraron %d reportes de este responsable", reportes.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reportes por responsable", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/search/asunto?q= - Buscar reportes por asunto
     */
    public void searchReportesByAsunto(Context ctx) {
        try {
            String asunto = ctx.queryParam("q");

            if (asunto == null || asunto.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'q'"));
                return;
            }

            List<Reporte> reportes = reporteService.searchReportesByAsunto(asunto);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "search_term", asunto,
                    "message", String.format("Se encontraron %d reportes que coinciden con el asunto", reportes.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda por asunto", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/search/contenido?q= - Buscar reportes por contenido
     */
    public void searchReportesByContenido(Context ctx) {
        try {
            String contenido = ctx.queryParam("q");

            if (contenido == null || contenido.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'q'"));
                return;
            }

            List<Reporte> reportes = reporteService.searchReportesByContenido(contenido);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "search_term", contenido,
                    "message", String.format("Se encontraron %d reportes que coinciden con el contenido", reportes.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda por contenido", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/fechas?inicio=YYYY-MM-DD&fin=YYYY-MM-DD - Buscar reportes por rango de fechas
     */
    public void getReportesByDateRange(Context ctx) {
        try {
            String fechaInicioStr = ctx.queryParam("inicio");
            String fechaFinStr = ctx.queryParam("fin");

            if (fechaInicioStr == null || fechaFinStr == null) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetros requeridos", "Debe proporcionar 'inicio' y 'fin'"));
                return;
            }

            Date fechaInicio = DATE_FORMAT.parse(fechaInicioStr);
            Date fechaFin = DATE_FORMAT.parse(fechaFinStr);

            List<Reporte> reportes = reporteService.getReportesByDateRange(fechaInicio, fechaFin);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "fecha_inicio", fechaInicioStr,
                    "fecha_fin", fechaFinStr,
                    "message", String.format("Se encontraron %d reportes en el rango de fechas", reportes.size())
            ));
        } catch (ParseException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Formato de fecha inválido", "Use el formato YYYY-MM-DD"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reportes por fechas", e.getMessage()));
        }
    }

    /**
     * POST /hm/reportes - Crear nuevo reporte
     */
    public void createReporte(Context ctx) {
        try {
            Reporte nuevoReporte = ctx.bodyAsClass(Reporte.class);
            Reporte reporteCreado = reporteService.createReporte(nuevoReporte);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", reporteCreado,
                            "message", "Reporte creado exitosamente",
                            "success", true
                    ));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al crear reporte", e.getMessage()));
        }
    }

    /**
     * PUT /hm/reportes/{id} - Actualizar reporte
     */
    public void updateReporte(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Reporte reporteActualizado = ctx.bodyAsClass(Reporte.class);
            reporteActualizado.setId_reporte(id);

            Reporte resultado = reporteService.updateReporte(reporteActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Reporte actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar reporte", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/reportes/{id} - Eliminar reporte
     */
    public void deleteReporte(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = reporteService.deleteReporte(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Reporte eliminado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Reporte no encontrado", "No se pudo eliminar el reporte"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar reporte", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes/estadisticas - Obtener estadísticas
     */
    public void getReporteStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = reporteService.getReporteStatistics();

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