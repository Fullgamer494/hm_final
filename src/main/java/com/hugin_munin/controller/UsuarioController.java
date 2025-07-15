package com.hugin_munin.controller;

import com.hugin_munin.model.Usuario;
import com.hugin_munin.model.UsuarioConPermisos;
import com.hugin_munin.service.UsuarioService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * Controlador para gestionar usuarios - COMPLETO Y CORREGIDO
 * Incluye funcionalidades básicas y gestión de permisos
 * AGREGADO: Nuevo método para permisos por nombre de usuario
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
     * GET /hm/usuarios/{id}/permisos - Obtener usuario con permisos por ID
     */
    public void getUsuarioConPermisosById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            // Obtener usuario con permisos
            UsuarioConPermisos usuarioConPermisos = usuarioService.getUsuarioConPermisosById(id);

            // Convertir a respuesta JSON estructurada
            Map<String, Object> response = usuarioConPermisos.toResponseMap();
            response.put("success", true);
            response.put("message", "Usuario con permisos obtenido exitosamente");
            response.put("timestamp", System.currentTimeMillis());

            ctx.json(response);
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("ID inválido", "El ID debe ser un número entero"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("no encontrado") || e.getMessage().contains("No se encontró")) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Usuario no encontrado", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Error de validación", e.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", "Error al obtener usuario con permisos"));
        }
    }

    /**
     * GET /hm/usuarios/permisos?correo={correo} - Obtener usuario con permisos por correo
     */
    public void getUsuarioConPermisosByCorreo(Context ctx) {
        try {
            System.out.println("🎯 Controller: Iniciando getUsuarioConPermisosByCorreo");

            String correo = ctx.queryParam("correo");
            if (correo == null || correo.trim().isEmpty()) {
                System.out.println("❌ Controller: Correo vacío");
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'correo'"));
                return;
            }

            System.out.println("✅ Controller: Correo recibido: " + correo);

            // Obtener usuario con permisos
            UsuarioConPermisos usuarioConPermisos = usuarioService.getUsuarioConPermisosByCorreo(correo);

            System.out.println("✅ Controller: Usuario obtenido del service");

            // Convertir a respuesta JSON estructurada
            Map<String, Object> response = usuarioConPermisos.toResponseMap();
            response.put("success", true);
            response.put("message", "Usuario con permisos obtenido exitosamente");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ Controller: Respuesta preparada, enviando JSON");
            ctx.json(response);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: Error de argumento: " + e.getMessage());
            if (e.getMessage().contains("no se encontró") || e.getMessage().contains("No se encontró")) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Usuario no encontrado", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Error de validación", e.getMessage()));
            }
        } catch (Exception e) {
            System.err.println("❌ Controller: Error interno: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", "Error al obtener usuario con permisos: " + e.getMessage()));
        }
    }

    /**
     * GET /hm/usuarios/permisos-por-nombre?nombre_usuario={nombre} - NUEVO MÉTODO
     * Obtener usuario con permisos por nombre de usuario
     */
    public void getUsuarioConPermisosByNombre(Context ctx) {
        try {
            System.out.println("🎯 Controller: Iniciando getUsuarioConPermisosByNombre");

            String nombreUsuario = ctx.queryParam("nombre_usuario");
            if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
                System.out.println("❌ Controller: Nombre de usuario vacío");
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Parámetro requerido", "Debe proporcionar el parámetro 'nombre_usuario'"));
                return;
            }

            System.out.println("✅ Controller: Nombre de usuario recibido: " + nombreUsuario);

            // Obtener usuario con permisos por nombre
            UsuarioConPermisos usuarioConPermisos = usuarioService.getUsuarioConPermisosByNombre(nombreUsuario);

            System.out.println("✅ Controller: Usuario obtenido del service por nombre");

            // Convertir a respuesta JSON estructurada
            Map<String, Object> response = usuarioConPermisos.toResponseMap();
            response.put("success", true);
            response.put("message", "Usuario con permisos obtenido exitosamente por nombre de usuario");
            response.put("timestamp", System.currentTimeMillis());

            System.out.println("✅ Controller: Respuesta preparada, enviando JSON");
            ctx.json(response);

        } catch (IllegalArgumentException e) {
            System.err.println("❌ Controller: Error de argumento: " + e.getMessage());
            if (e.getMessage().contains("no se encontró") || e.getMessage().contains("No se encontró")) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Usuario no encontrado", e.getMessage()));
            } else {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Error de validación", e.getMessage()));
            }
        } catch (Exception e) {
            System.err.println("❌ Controller: Error interno: " + e.getMessage());
            e.printStackTrace();
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error interno del servidor", "Error al obtener usuario con permisos por nombre: " + e.getMessage()));
        }
    }

    /**
     * POST /hm/usuarios/verificar-permiso - Verificar si un usuario tiene un permiso específico
     */
    public void verificarPermiso(Context ctx) {
        try {
            Map<String, String> requestBody = ctx.bodyAsClass(Map.class);
            String correo = requestBody.get("correo");
            String permiso = requestBody.get("permiso");

            if (correo == null || correo.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Correo requerido", "Debe proporcionar un correo electrónico"));
                return;
            }

            if (permiso == null || permiso.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Permiso requerido", "Debe proporcionar el nombre del permiso"));
                return;
            }

            boolean tienePermiso = usuarioService.userHasPermission(correo, permiso);

            ctx.json(Map.of(
                    "success", true,
                    "correo", correo,
                    "permiso", permiso,
                    "tiene_permiso", tienePermiso,
                    "message", tienePermiso ? "Usuario tiene el permiso" : "Usuario no tiene el permiso",
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al verificar permiso", e.getMessage()));
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
                            "success", true,
                            "timestamp", System.currentTimeMillis()
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
                    "success", true,
                    "timestamp", System.currentTimeMillis()
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
            estadisticas.put("success", true);
            estadisticas.put("timestamp", System.currentTimeMillis());
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
                    "message", disponible ? "Email disponible" : "Email ya está en uso",
                    "success", true,
                    "timestamp", System.currentTimeMillis()
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