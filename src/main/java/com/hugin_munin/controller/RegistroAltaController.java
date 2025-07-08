package com.hugin_munin.controller;

import com.hugin_munin.model.RegistroAlta;
import com.hugin_munin.service.RegistroAltaService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.sql.SQLException;
import java.util.List;

public class RegistroAltaController {

    private final RegistroAltaService service;

    public RegistroAltaController(RegistroAltaService service) {
        this.service = service;
    }

    public void getAll(Context ctx) {
        try {
            List<RegistroAlta> registros = service.getAll();
            ctx.json(registros);
        } catch (SQLException e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al obtener registros: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RegistroAlta registro = service.getById(id);
            if (registro != null) {
                ctx.json(registro);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(errorResponse("Registro no encontrado"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse("ID inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND).json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al buscar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    public void create(Context ctx) {
        try {
            RegistroAlta nuevo = ctx.bodyAsClass(RegistroAlta.class);
            System.out.println("Datos recibidos: " + nuevo); // Para debugging

            if (!nuevo.isValid()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse("Datos incompletos"));
                return;
            }

            RegistroAlta resultado = service.create(nuevo);
            ctx.status(HttpStatus.CREATED).json(resultado);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al crear el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            RegistroAlta actualizado = ctx.bodyAsClass(RegistroAlta.class);
            actualizado.setId_registro_alta(id);

            if (!actualizado.isValid()) {
                ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse("Datos incompletos"));
                return;
            }

            RegistroAlta result = service.update(actualizado);
            ctx.status(HttpStatus.OK).json(result);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse("ID inválido"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse(e.getMessage()));
        } catch (SQLException e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al actualizar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean result = service.delete(id);
            if (result) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json(errorResponse("Registro no encontrado para eliminar"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(errorResponse("ID inválido"));
        } catch (SQLException e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error al eliminar el registro: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // Para debugging
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(errorResponse("Error inesperado: " + e.getMessage()));
        }
    }

    private static Object errorResponse(String message) {
        return new Object() {
            public final boolean success = false;
            public final String error = message;
        };
    }
}