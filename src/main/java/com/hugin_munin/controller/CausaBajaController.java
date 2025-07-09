package com.hugin_munin.controller;

import com.hugin_munin.model.CausaBaja;
import com.hugin_munin.service.CausaBajaService;
import io.javalin.http.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class CausaBajaController {
    private final CausaBajaService causaBajaService;

    public CausaBajaController(CausaBajaService causaBajaService) {
        this.causaBajaService = causaBajaService;
    }

    //GetAll
    public void getAll(Context ctx) {
        try {
            List<CausaBaja> baja = causaBajaService.getAll();
            ctx.json(baja);
        } catch (SQLException e) {
            ctx.status(500).result("Error al obtener las causas de baja");
        }
    }

    //GetById
    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            List<CausaBaja> baja = causaBajaService.getById(id);

            if (baja.isEmpty()) {
                ctx.status(404).result("No se encontró la causa de baja con ID: " + id);
            } else {
                ctx.json(baja);
            }
        } catch (SQLException e) {
            ctx.status(500).result("Error al obtener las causas de baja");
        } catch (NumberFormatException e) {
            ctx.status(400).result("El ID proporcionado no es válido");
        }
    }

    //Update - CORREGIDO
    public void update(Context ctx) {
        try {
            // Obtener ID del path parameter (no del body)
            int id = Integer.parseInt(ctx.pathParam("id"));

            // Obtener datos del body
            CausaBaja causa = ctx.bodyAsClass(CausaBaja.class);

            // Validar que el nombre no esté vacío
            if (causa.nombreCausaBaja == null || causa.nombreCausaBaja.trim().isEmpty()) {
                ctx.status(400).json(createErrorResponse("Datos inválidos", "El nombre de la causa de baja no puede estar vacío"));
                return;
            }

            // Verificar si existe antes de actualizar
            List<CausaBaja> existingCausa = causaBajaService.getById(id);
            if (existingCausa.isEmpty()) {
                ctx.status(404).json(createErrorResponse("No encontrado", "No se encontró la causa de baja con ID: " + id));
                return;
            }

            // Actualizar
            boolean actualizada = causaBajaService.update(id, causa.nombreCausaBaja.trim());

            if (actualizada) {
                // Crear respuesta exitosa
                CausaBaja causaActualizada = new CausaBaja(id, causa.nombreCausaBaja.trim());
                ctx.status(200).json(Map.of(
                        "success", true,
                        "message", "Causa de baja actualizada exitosamente",
                        "data", causaActualizada
                ));
            } else {
                ctx.status(404).json(createErrorResponse("No actualizado", "No se pudo actualizar la causa de baja"));
            }
        } catch (NumberFormatException e) {
            ctx.status(400).json(createErrorResponse("ID inválido", "El ID proporcionado no es válido"));
        } catch (SQLException e) {
            ctx.status(500).json(createErrorResponse("Error interno", "Error interno al actualizar la causa de baja: " + e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(createErrorResponse("Error interno", "Error inesperado: " + e.getMessage()));
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