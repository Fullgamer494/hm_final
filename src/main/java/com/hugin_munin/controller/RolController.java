package com.hugin_munin.controller;

import com.hugin_munin.model.Rol;
import com.hugin_munin.service.RolService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar roles
 */
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    /**
     * GET /hm/roles - Obtener todos los roles
     */
    public void getAllRoles(Context ctx) {
        try {
            List<Rol> roles = rolService.getAllRoles();
            ctx.json(Map.of(
                    "data", roles,
                    "total", roles.size(),
                    "message", "Roles obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener roles", e.getMessage()));
        }
    }

    /**
     * GET /hm/roles/activos - Obtener roles activos
     */
    public void getActiveRoles(Context ctx) {
        try {
            List<Rol> roles = rolService.getActiveRoles();
            ctx.json(Map.of(
                    "data", roles,
                    "total", roles.size(),
                    "message", "Roles activos obtenidos exitosamente"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener roles activos", e.getMessage()));
        }
    }

    /**
     * GET /hm/roles/{id} - Obtener rol por ID
     */
    public void getRolById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Rol rol = rolService.getRolById(id);

            ctx.json(Map.of(
                    "data", rol,
                    "message", "Rol encontrado exitosamente"
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Rol no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar rol", e.getMessage()));
        }
    }

    /**
     * GET /hm/roles/search?nombre= - Buscar roles por nombre
     */
    public void searchRolesByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<Rol> roles = rolService.searchRolesByName(nombre);

            ctx.json(Map.of(
                    "data", roles,
                    "total", roles.size(),
                    "search_term", nombre,
                    "message", String.format("Se encontraron %d roles que coinciden con la búsqueda", roles.size())
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/roles - Crear nuevo rol
     */
    public void createRol(Context ctx) {
        try {
            Rol nuevoRol = ctx.bodyAsClass(Rol.class);
            Rol rolCreado = rolService.createRol(nuevoRol);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", rolCreado,
                            "message", "Rol creado exitosamente",
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
                        .json(createErrorResponse("Error al crear rol", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/roles/{id} - Actualizar rol existente
     */
    public void updateRol(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Rol rolActualizado = ctx.bodyAsClass(Rol.class);
            rolActualizado.setId_rol(id);

            Rol resultado = rolService.updateRol(rolActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Rol actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar rol", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/roles/{id} - Eliminar rol
     */
    public void deleteRol(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = rolService.deleteRol(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Rol eliminado o desactivado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Rol no encontrado", "No se pudo eliminar el rol"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar rol", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/roles/{id}/activar - Activar rol
     */
    public void activateRol(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean activado = rolService.activateRol(id);

            if (activado) {
                ctx.json(Map.of(
                        "message", "Rol activado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Rol no encontrado", "No se pudo activar el rol"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al activar rol", e.getMessage()));
        }
    }

    /**
     * PATCH /hm/roles/{id}/desactivar - Desactivar rol
     */
    public void deactivateRol(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean desactivado = rolService.deactivateRol(id);

            if (desactivado) {
                ctx.json(Map.of(
                        "message", "Rol desactivado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Rol no encontrado", "No se pudo desactivar el rol"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Operación no permitida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al desactivar rol", e.getMessage()));
        }
    }

    /**
     * GET /hm/roles/estadisticas - Obtener estadísticas de roles
     */
    public void getRoleStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = rolService.getRoleStatistics();

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
     * POST /hm/roles/validar-nombre - Validar si un nombre está disponible
     */
    public void validateRoleName(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String nombre = requestBody.get("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Nombre requerido", "Debe proporcionar un nombre"));
                return;
            }

            boolean disponible = rolService.isRoleNameAvailable(nombre);

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