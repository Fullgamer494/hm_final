package com.hugin_munin.controller;

import com.hugin_munin.model.Permiso;
import com.hugin_munin.service.PermisoService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar permisos
 */
public class PermisoController {

    private final PermisoService permisoService;

    public PermisoController(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    /**
     * GET /hm/permisos - Obtener todos los permisos
     */
    public void getAllPermisos(Context ctx) {
        try {
            List<Permiso> permisos = permisoService.getAllPermisos();
            ctx.json(permisos);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener permisos", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/categorias - Obtener permisos agrupados por categoría
     */
    public void getPermisosByCategory(Context ctx) {
        try {
            Map<String, List<Permiso>> permisosPorCategoria = permisoService.getPermisosByCategory();
            ctx.json(permisosPorCategoria);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener permisos por categoría", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/{id} - Obtener permiso por ID
     */
    public void getPermisoById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Permiso permiso = permisoService.getPermisoById(id);
            ctx.json(permiso);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Permiso no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar permiso", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/search?nombre= - Buscar permisos por nombre
     */
    public void searchPermisosByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<Permiso> permisos = permisoService.searchPermisosByName(nombre);
            ctx.json(permisos);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/categoria/{categoria} - Obtener permisos por categoría específica
     */
    public void getPermisosBySpecificCategory(Context ctx) {
        try {
            String categoria = ctx.pathParam("categoria");
            List<Permiso> permisos = permisoService.getPermisosByCategory(categoria);
            ctx.json(permisos);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Categoría inválida", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener permisos por categoría", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/rol/{idRol} - Obtener permisos asignados a un rol
     */
    public void getPermisosByRol(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));
            List<Permiso> permisos = permisoService.getPermisosByRol(idRol);
            ctx.json(permisos);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID de rol inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener permisos del rol", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/rol/{idRol}/disponibles - Obtener permisos NO asignados a un rol
     */
    public void getPermisosNotAssignedToRol(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));
            List<Permiso> permisos = permisoService.getPermisosNotAssignedToRol(idRol);
            ctx.json(permisos);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID de rol inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener permisos disponibles", e.getMessage()));
        }
    }

    /**
     * POST /hm/permisos - Crear nuevo permiso
     */
    public void createPermiso(Context ctx) {
        try {
            Permiso nuevoPermiso = ctx.bodyAsClass(Permiso.class);
            Permiso permisoCreado = permisoService.createPermiso(nuevoPermiso);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", permisoCreado,
                            "message", "Permiso creado exitosamente",
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
                        .json(createErrorResponse("Error al crear permiso", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/permisos/{id} - Actualizar permiso existente
     */
    public void updatePermiso(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Permiso permisoActualizado = ctx.bodyAsClass(Permiso.class);
            permisoActualizado.setId_permiso(id);

            Permiso resultado = permisoService.updatePermiso(permisoActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Permiso actualizado exitosamente",
                    "success", true
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("no encontrado")) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Permiso no encontrado", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos inválidos", e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al actualizar permiso", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/permisos/{id} - Eliminar permiso
     */
    public void deletePermiso(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = permisoService.deletePermiso(id);

            if (eliminado) {
                ctx.json(Map.of(
                        "message", "Permiso eliminado exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Permiso no encontrado", "No se pudo eliminar el permiso"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("crítico") || e.getMessage().contains("asignado")) {
                ctx.status(HttpStatus.CONFLICT)
                        .json(createErrorResponse("No se puede eliminar", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Error de validación", e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar permiso", e.getMessage()));
        }
    }

    /**
     * POST /hm/permisos/{idPermiso}/rol/{idRol} - Asignar permiso a rol
     */
    public void assignPermisoToRol(Context ctx) {
        try {
            int idPermiso = Integer.parseInt(ctx.pathParam("idPermiso"));
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));

            boolean asignado = permisoService.assignPermisoToRol(idPermiso, idRol);

            if (asignado) {
                ctx.json(Map.of(
                        "message", "Permiso asignado al rol exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(createErrorResponse("Error", "No se pudo asignar el permiso al rol"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("IDs inválidos", "Los IDs deben ser números enteros"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al asignar permiso", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/permisos/{idPermiso}/rol/{idRol} - Remover permiso de rol
     */
    public void removePermisoFromRol(Context ctx) {
        try {
            int idPermiso = Integer.parseInt(ctx.pathParam("idPermiso"));
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));

            boolean removido = permisoService.removePermisoFromRol(idPermiso, idRol);

            if (removido) {
                ctx.json(Map.of(
                        "message", "Permiso removido del rol exitosamente",
                        "success", true
                ));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(createErrorResponse("Error", "No se pudo remover el permiso del rol"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("IDs inválidos", "Los IDs deben ser números enteros"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Error de validación", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al remover permiso", e.getMessage()));
        }
    }

    /**
     * POST /hm/permisos/rol/{idRol}/multiple - Asignar múltiples permisos a un rol
     */
    public void assignMultiplePermisosToRol(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));

            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            @SuppressWarnings("unchecked")
            List<Integer> idsPermisos = (List<Integer>) requestBody.get("permisos");

            Map<String, Object> resultado = permisoService.assignMultiplePermisosToRol(idsPermisos, idRol);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Operación de asignación múltiple completada",
                    "success", true
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID de rol inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en asignación múltiple", e.getMessage()));
        }
    }

    /**
     * PUT /hm/permisos/rol/{idRol}/sync - Sincronizar permisos de un rol (reemplazar completamente)
     */
    public void syncPermisosToRol(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));

            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            @SuppressWarnings("unchecked")
            List<Integer> idsPermisos = (List<Integer>) requestBody.get("permisos");

            Map<String, Object> resultado = permisoService.syncPermisosToRol(idsPermisos, idRol);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Sincronización de permisos completada",
                    "success", resultado.get("success")
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID de rol inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en sincronización", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/rol/{idRol}/verificar/{idPermiso} - Verificar si un rol tiene un permiso específico
     */
    public void checkRolHasPermiso(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));
            int idPermiso = Integer.parseInt(ctx.pathParam("idPermiso"));

            boolean hasPermiso = permisoService.rolHasPermiso(idRol, idPermiso);

            ctx.json(Map.of(
                    "id_rol", idRol,
                    "id_permiso", idPermiso,
                    "tiene_permiso", hasPermiso
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("IDs inválidos", "Los IDs deben ser números enteros"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al verificar permiso", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/rol/{idRol}/verificar-nombre/{nombrePermiso} - Verificar si un rol tiene un permiso por nombre
     */
    public void checkRolHasPermisoByName(Context ctx) {
        try {
            int idRol = Integer.parseInt(ctx.pathParam("idRol"));
            String nombrePermiso = ctx.pathParam("nombrePermiso");

            boolean hasPermiso = permisoService.rolHasPermisoByName(idRol, nombrePermiso);

            ctx.json(Map.of(
                    "id_rol", idRol,
                    "nombre_permiso", nombrePermiso,
                    "tiene_permiso", hasPermiso
            ));
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID de rol inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al verificar permiso", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/estadisticas - Obtener estadísticas generales de permisos
     */
    public void getEstadisticasGenerales(Context ctx) {
        try {
            Map<String, Object> estadisticas = permisoService.getEstadisticasGenerales();
            ctx.json(estadisticas);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener estadísticas", e.getMessage()));
        }
    }

    /**
     * GET /hm/permisos/estadisticas/uso - Obtener estadísticas de uso de permisos
     */
    public void getEstadisticasUso(Context ctx) {
        try {
            var estadisticas = permisoService.getEstadisticasUso();
            ctx.json(estadisticas);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener estadísticas de uso", e.getMessage()));
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