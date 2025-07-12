package com.hugin_munin.controller;

import com.hugin_munin.model.ReporteTraslado;
import com.hugin_munin.service.ReporteTrasladoService;
import com.hugin_munin.repository.ReporteTrasladoRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar reportes de traslado (clase hija)
 */
public class ReporteTrasladoController {

    private final ReporteTrasladoService reporteTrasladoService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public ReporteTrasladoController(ReporteTrasladoService reporteTrasladoService) {
        this.reporteTrasladoService = reporteTrasladoService;
    }

    /**
     * GET /hm/reportes-traslado - Obtener todos los reportes de traslado
     */
    public void getAllReportesTraslado(Context ctx) {
        try {
            List<ReporteTraslado> reportes = reporteTrasladoService.getAllReportesTraslado();
            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "message", "Reportes de traslado obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener reportes de traslado", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/{id} - Obtener reporte de traslado por ID
     */
    public void getReporteTrasladoById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ReporteTraslado reporte = reporteTrasladoService.getReporteTrasladoById(id);

            ctx.json(Map.of(
                    "data", reporte,
                    "message", "Reporte de traslado encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Reporte de traslado no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar reporte de traslado", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/area-origen/{area} - Buscar por área origen
     */
    public void getReportesByAreaOrigen(Context ctx) {
        try {
            String areaOrigen = ctx.pathParam("area");
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByAreaOrigen(areaOrigen);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "area_origen", areaOrigen,
                    "message", String.format("Se encontraron %d reportes con área origen '%s'", reportes.size(), areaOrigen)
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar por área origen", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/area-destino/{area} - Buscar por área destino
     */
    public void getReportesByAreaDestino(Context ctx) {
        try {
            String areaDestino = ctx.pathParam("area");
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByAreaDestino(areaDestino);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "area_destino", areaDestino,
                    "message", String.format("Se encontraron %d reportes con área destino '%s'", reportes.size(), areaDestino)
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar por área destino", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/ubicacion-origen/{ubicacion} - Buscar por ubicación origen
     */
    public void getReportesByUbicacionOrigen(Context ctx) {
        try {
            String ubicacionOrigen = ctx.pathParam("ubicacion");
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByUbicacionOrigen(ubicacionOrigen);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "ubicacion_origen", ubicacionOrigen,
                    "message", String.format("Se encontraron %d reportes con ubicación origen '%s'", reportes.size(), ubicacionOrigen)
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar por ubicación origen", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/ubicacion-destino/{ubicacion} - Buscar por ubicación destino
     */
    public void getReportesByUbicacionDestino(Context ctx) {
        try {
            String ubicacionDestino = ctx.pathParam("ubicacion");
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByUbicacionDestino(ubicacionDestino);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "ubicacion_destino", ubicacionDestino,
                    "message", String.format("Se encontraron %d reportes con ubicación destino '%s'", reportes.size(), ubicacionDestino)
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar por ubicación destino", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/search/motivo?q= - Buscar por motivo
     */
    public void searchReportesByMotivo(Context ctx) {
        try {
            String motivo = ctx.queryParam("q");

            if (motivo == null || motivo.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'q'"));
                return;
            }

            List<ReporteTraslado> reportes = reporteTrasladoService.searchReportesByMotivo(motivo);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "search_term", motivo,
                    "message", String.format("Se encontraron %d reportes que coinciden con el motivo", reportes.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda por motivo", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/especimen/{id} - Buscar por especimen
     */
    public void getReportesByEspecimen(Context ctx) {
        try {
            int idEspecimen = Integer.parseInt(ctx.pathParam("id"));
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByEspecimen(idEspecimen);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "especimen_id", idEspecimen,
                    "message", String.format("Se encontraron %d reportes de traslado para este especimen", reportes.size())
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
     * GET /hm/reportes-traslado/responsable/{id} - Buscar por responsable
     */
    public void getReportesByResponsable(Context ctx) {
        try {
            int idResponsable = Integer.parseInt(ctx.pathParam("id"));
            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByResponsable(idResponsable);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "responsable_id", idResponsable,
                    "message", String.format("Se encontraron %d reportes de traslado de este responsable", reportes.size())
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
     * GET /hm/reportes-traslado/fechas?inicio=YYYY-MM-DD&fin=YYYY-MM-DD - Buscar por rango de fechas
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

            List<ReporteTraslado> reportes = reporteTrasladoService.getReportesByDateRange(fechaInicio, fechaFin);

            ctx.json(Map.of(
                    "data", reportes,
                    "total", reportes.size(),
                    "fecha_inicio", fechaInicioStr,
                    "fecha_fin", fechaFinStr,
                    "message", String.format("Se encontraron %d reportes de traslado en el rango de fechas", reportes.size())
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
     * POST /hm/reportes-traslado - Crear nuevo reporte de traslado
     */
    public void createReporteTraslado(Context ctx) {
        try {
            ReporteTraslado nuevoReporte = ctx.bodyAsClass(ReporteTraslado.class);
            ReporteTraslado reporteCreado = reporteTrasladoService.createReporteTraslado(nuevoReporte);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", reporteCreado,
                            "message", "Reporte de traslado creado exitosamente",
                            "success", true
                    ));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al crear reporte de traslado", e.getMessage()));
        }
    }

    /**
     * PUT /hm/reportes-traslado/{id} - Actualizar reporte de traslado
     */
    public void updateReporteTraslado(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            ReporteTraslado reporteActualizado = ctx.bodyAsClass(ReporteTraslado.class);
            reporteActualizado.setId_reporte(id);

            ReporteTraslado resultado = reporteTrasladoService.updateReporteTraslado(reporteActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Reporte de traslado actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar reporte de traslado", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/reportes-traslado/{id} - Eliminar reporte de traslado
     */
    public void deleteReporteTraslado(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = reporteTrasladoService.deleteReporteTraslado(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Reporte de traslado eliminado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Reporte no encontrado", "No se pudo eliminar el reporte de traslado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar reporte de traslado", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/estadisticas - Obtener estadísticas de traslados
     */
    public void getReporteTrasladoStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = reporteTrasladoService.getReporteTrasladoStatistics();

            ctx.json(Map.of(
                    "data", estadisticas,
                    "message", "Estadísticas de traslados obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener estadísticas", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/estadisticas/areas-origen?limit= - Áreas origen más populares
     */
    public void getAreasOrigenPopulares(Context ctx) {
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

            List<ReporteTrasladoRepository.AreaEstadistica> areasPopulares =
                    reporteTrasladoService.getAreasOrigenPopulares(limit);

            ctx.json(Map.of(
                    "data", areasPopulares,
                    "total", areasPopulares.size(),
                    "limit", limit,
                    "message", "Áreas origen más populares obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener áreas origen populares", e.getMessage()));
        }
    }

    /**
     * GET /hm/reportes-traslado/estadisticas/areas-destino?limit= - Áreas destino más populares
     */
    public void getAreasDestinoPopulares(Context ctx) {
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

            List<ReporteTrasladoRepository.AreaEstadistica> areasPopulares =
                    reporteTrasladoService.getAreasDestinoPopulares(limit);

            ctx.json(Map.of(
                    "data", areasPopulares,
                    "total", areasPopulares.size(),
                    "limit", limit,
                    "message", "Áreas destino más populares obtenidas exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener áreas destino populares", e.getMessage()));
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