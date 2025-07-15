package com.hugin_munin.routes;

import com.hugin_munin.controller.UsuarioController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para usuarios - COMPLETO CON NUEVA RUTA
 * ORDEN CORREGIDO - Rutas específicas ANTES que rutas con parámetros
 * AGREGADO: Nueva ruta para permisos por nombre de usuario
 */
public class UsuarioRoutes {

    private final UsuarioController usuarioController;

    public UsuarioRoutes(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;
    }

    public void defineRoutes(Javalin app) {

        // ========================================
        // RUTAS ESPECÍFICAS PRIMERO (MUY IMPORTANTE)
        // ========================================

        // GET - Obtener usuario con permisos por correo (DEBE IR PRIMERO)
        app.get("/hm/usuarios/permisos", usuarioController::getUsuarioConPermisosByCorreo);

        // GET - Obtener usuario con permisos por nombre de usuario - NUEVA RUTA
        app.get("/hm/usuarios/permisos-por-nombre", usuarioController::getUsuarioConPermisosByNombre);

        // GET - Buscar usuarios por nombre
        app.get("/hm/usuarios/search", usuarioController::searchUsersByName);

        // GET - Estadísticas de usuarios
        app.get("/hm/usuarios/estadisticas", usuarioController::getUserStatistics);

        // ========================================
        // RUTAS GENERALES DESPUÉS
        // ========================================

        // GET - Obtener todos los usuarios
        app.get("/hm/usuarios", usuarioController::getAllUsers);

        // ========================================
        // RUTAS CON PARÁMETROS AL FINAL (CRÍTICO)
        // ========================================

        // GET - Obtener usuario por ID (DEBE IR AL FINAL)
        app.get("/hm/usuarios/{id}", usuarioController::getUserById);

        // GET - Obtener usuario con permisos por ID
        app.get("/hm/usuarios/{id}/permisos", usuarioController::getUsuarioConPermisosById);

        // ========================================
        // RUTAS DE MODIFICACIÓN
        // ========================================

        // POST - Crear nuevo usuario
        app.post("/hm/usuarios", usuarioController::createUser);

        // PUT - Actualizar usuario
        app.put("/hm/usuarios/{id}", usuarioController::updateUser);

        // DELETE - Eliminar usuario
        app.delete("/hm/usuarios/{id}", usuarioController::deleteUser);

        // ========================================
        // RUTAS DE VALIDACIÓN Y PERMISOS
        // ========================================

        // POST - Validar email
        app.post("/hm/usuarios/validar-email", usuarioController::validateEmail);

        // POST - Verificar permiso específico
        app.post("/hm/usuarios/verificar-permiso", usuarioController::verificarPermiso);
    }
}