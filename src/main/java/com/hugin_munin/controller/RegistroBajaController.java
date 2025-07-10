package com.hugin_munin.controller;

import com.hugin_munin.model.RegistroBaja;
import com.hugin_munin.service.RegistroBajaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar registros de baja
 */
public class RegistroBajaController {

    private final RegistroBajaService service;

    public RegistroBajaController(RegistroBajaService service) {
        this.service = service;
    }

    /**
     * GET /hm/registro_baja - Obtener todos los registros de baja
     */
    public void getAll(Context ctx) {
        try {
            List<RegistroBaja> registros = service.getAll();
            ctx.json(Map.of(
                    "data", registros,
                    "total", registros.size(),
                    "message", "Registros de baja obtenidos exitosamente"
            ));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al obtener registros de baja: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/{id} - Obtener registro de baja por ID
     */
    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RegistroBaja registro = service.getById(id);

            ctx.json(Map.of(
                    "data", registro,
                    "message", "Registro de baja encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al buscar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    /**
     * POST /hm/registro_baja - Crear nuevo registro de baja
     */
    public void create(Context ctx) {
        try {
            RegistroBaja nuevo = ctx.bodyAsClass(RegistroBaja.class);
            System.out.println("Datos recibidos: " + nuevo);

            if (!nuevo.isValid()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(errorResponse("Datos incompletos para el registro de baja"));
                return;
            }

            RegistroBaja resultado = service.create(nuevo);
            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", resultado,
                            "message", "Registro de baja creado exitosamente. El especimen ha sido marcado como inactivo.",
                            "success", true
                    ));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al crear el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    /**
     * PUT /hm/registro_baja/{id} - Actualizar registro de baja
     */
    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RegistroBaja actualizado = ctx.bodyAsClass(RegistroBaja.class);
            actualizado.setId_registro_baja(id);

            if (!actualizado.isValid()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(errorResponse("Datos incompletos"));
                return;
            }

            RegistroBaja result = service.update(actualizado);
            ctx.status(HttpStatus.OK)
                    .json(Map.of(
                            "data", result,
                            "message", "Registro de baja actualizado exitosamente",
                            "success", true
                    ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al actualizar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    /**
     * DELETE /hm/registro_baja/{id} - Eliminar registro de baja (reactivar especimen)
     */
    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean result = service.delete(id);

            if (result) {
                ctx.json(Map.of(
                        "message", "Registro de baja eliminado exitosamente. El especimen ha sido reactivado.",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(errorResponse("Registro no encontrado para eliminar"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID inválido"));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al eliminar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/especimen/{id} - Obtener registros por especimen
     */
    public void getByEspecimen(Context ctx) {
        try {
            int idEspecimen = Integer.parseInt(ctx.pathParam("id"));
            List<RegistroBaja> registros = service.getByEspecimen(idEspecimen);

            ctx.json(Map.of(
                    "data", registros,
                    "total", registros.size(),
                    "especimen_id", idEspecimen,
                    "message", String.format("Se encontraron %d registros de baja para el especimen", registros.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID de especimen inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al buscar registros: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/causa/{id} - Obtener registros por causa de baja
     */
    public void getByCausaBaja(Context ctx) {
        try {
            int idCausaBaja = Integer.parseInt(ctx.pathParam("id"));
            List<RegistroBaja> registros = service.getByCausaBaja(idCausaBaja);

            ctx.json(Map.of(
                    "data", registros,
                    "total", registros.size(),
                    "causa_baja_id", idCausaBaja,
                    "message", String.format("Se encontraron %d registros con esta causa de baja", registros.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID de causa de baja inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al buscar registros: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/responsable/{id} - Obtener registros por responsable
     */
    public void getByResponsable(Context ctx) {
        try {
            int idResponsable = Integer.parseInt(ctx.pathParam("id"));
            List<RegistroBaja> registros = service.getByResponsable(idResponsable);

            ctx.json(Map.of(
                    "data", registros,
                    "total", registros.size(),
                    "responsable_id", idResponsable,
                    "message", String.format("Se encontraron %d registros de baja realizados por este responsable", registros.size())
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID de responsable inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al buscar registros: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/estadisticas/causas - Obtener estadísticas por causa de baja
     */
    public void getEstadisticasPorCausa(Context ctx) {
        try {
            var estadisticas = service.getEstadisticasPorCausa();

            ctx.json(Map.of(
                    "data", estadisticas,
                    "total_causas", estadisticas.size(),
                    "message", "Estadísticas por causa de baja obtenidas exitosamente"
            ));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/estadisticas/general - Obtener estadísticas generales
     */
    public void getEstadisticasGenerales(Context ctx) {
        try {
            int totalRegistros = service.countTotal();
            var estadisticasCausas = service.getEstadisticasPorCausa();

            ctx.json(Map.of(
                    "total_registros_baja", totalRegistros,
                    "estadisticas_por_causa", estadisticasCausas,
                    "message", "Estadísticas generales obtenidas exitosamente"
            ));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/registro_baja/verificar/{id} - Verificar si un especimen está dado de baja
     */
    public void verificarEspecimenDadoDeBaja(Context ctx) {
        try {
            int idEspecimen = Integer.parseInt(ctx.pathParam("id"));
            boolean dadoDeBaja = service.isEspecimenDadoDeBaja(idEspecimen);

            ctx.json(Map.of(
                    "especimen_id", idEspecimen,
                    "dado_de_baja", dadoDeBaja,
                    "message", dadoDeBaja ?
                            "El especimen está dado de baja" :
                            "El especimen está activo"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse("ID de especimen inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al verificar especimen: " + e.getMessage()));
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private static Map<String, Object> errorResponse(String message) {
        return Map.of(
                "success", false,
                "error", message,
                "timestamp", System.currentTimeMillis()
        );
    }
}