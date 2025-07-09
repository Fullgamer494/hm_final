package com.hugin_munin.controller;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.service.UsuarioService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar usuarios - Version simplificada
 */
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * GET /hm/usuarios - Obtener todos los usuarios
     */
    public void getAllUsers(Context ctx) {
        try {
            List<Usuario> usuarios = usuarioService.getAllUsers();
            ctx.json(usuarios);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener usuarios", e.getMessage()));
        }
    }

    /**
     * GET /hm/usuarios/{id} - Obtener usuario por ID
     */
    public void getUserById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Usuario usuario = usuarioService.getUserById(id);
            ctx.json(usuario);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.NOT_FOUND)
                    .json(createErrorResponse("Usuario no encontrado", e.getMessage()));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al buscar usuario", e.getMessage()));
        }
    }

    /**
     * GET /hm/usuarios/search?nombre= - Buscar usuarios por nombre
     */
    public void searchUsersByName(Context ctx) {
        try {
            String nombre = ctx.queryParam("nombre");

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre'"));
                return;
            }

            List<Usuario> usuarios = usuarioService.searchUsersByName(nombre);
            ctx.json(usuarios);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en la búsqueda", e.getMessage()));
        }
    }

    /**
     * POST /hm/usuarios - Crear nuevo usuario
     */
    public void createUser(Context ctx) {
        try {
            Usuario nuevoUsuario = ctx.bodyAsClass(Usuario.class);
            Usuario usuarioCreado = usuarioService.createUser(nuevoUsuario);

            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "data", usuarioCreado,
                            "message", "Usuario creado exitosamente",
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
                        .json(createErrorResponse("Error al crear usuario", e.getMessage()));
            }
        }
    }

    /**
     * PUT /hm/usuarios/{id} - Actualizar usuario existente
     */
    public void updateUser(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Usuario usuarioActualizado = ctx.bodyAsClass(Usuario.class);
            usuarioActualizado.setId_usuario(id);

            Usuario resultado = usuarioService.updateUser(usuarioActualizado);

            ctx.json(Map.of(
                    "data", resultado,
                    "message", "Usuario actualizado exitosamente",
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
                    .json(createErrorResponse("Error al actualizar usuario", e.getMessage()));
        }
    }

    /**
     * DELETE /hm/usuarios/{id} - Eliminar usuario
     */
    public void deleteUser(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            boolean eliminado = usuarioService.deleteUser(id);

            if (eliminado) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Usuario no encontrado", "No se pudo eliminar el usuario"));
            }
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al eliminar usuario", e.getMessage()));
        }
    }

    /**
     * GET /hm/usuarios/estadisticas - Obtener estadísticas de usuarios
     */
    public void getUserStatistics(Context ctx) {
        try {
            Map<String, Object> estadisticas = usuarioService.getUserStatistics();
            ctx.json(estadisticas);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener estadísticas", e.getMessage()));
        }
    }

    /**
     * POST /hm/usuarios/validar-email - Validar si un email está disponible
     */
    public void validateEmail(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String email = requestBody.get("email");

            if (email == null || email.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Email requerido", "Debe proporcionar un email"));
                return;
            }

            boolean disponible = usuarioService.isEmailAvailable(email);

            ctx.json(Map.of(
                    "email", email,
                    "disponible", disponible,
                    "message", disponible ? "Email disponible" : "Email ya está en uso"
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al validar email", e.getMessage()));
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