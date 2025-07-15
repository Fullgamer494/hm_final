package com.hugin_munin.routes;

import com.hugin_munin.controller.PermisoController;
import io.javalin.Javalin;

/**
 * Configuración de rutas para permisos
 */
public class PermisoRoutes {

    private final PermisoController permisoController;

    public PermisoRoutes(PermisoController permisoController) {
        this.permisoController = permisoController;
    }

    public void defineRoutes(Javalin app) {

        // ========================================
        // RUTAS BÁSICAS DE CRUD
        // ========================================

        // GET - Obtener todos los permisos
        app.get("/hm/permisos", permisoController::getAllPermisos);

        // GET - Obtener permiso por ID
        app.get("/hm/permisos/{id}", permisoController::getPermisoById);

        // POST - Crear nuevo permiso
        app.post("/hm/permisos", permisoController::createPermiso);

        // PUT - Actualizar permiso
        app.put("/hm/permisos/{id}", permisoController::updatePermiso);

        // DELETE - Eliminar permiso
        app.delete("/hm/permisos/{id}", permisoController::deletePermiso);

        // ========================================
        // RUTAS DE BÚSQUEDA Y FILTRADO
        // ========================================

        // GET - Buscar permisos por nombre
        app.get("/hm/permisos/search", permisoController::searchPermisosByName);

        // GET - Obtener permisos agrupados por categoría
        app.get("/hm/permisos/categorias", permisoController::getPermisosByCategory);

        // GET - Obtener permisos por categoría específica
        app.get("/hm/permisos/categoria/{categoria}", permisoController::getPermisosBySpecificCategory);

        // ========================================
        // RUTAS DE GESTIÓN ROL-PERMISO
        // ========================================

        // GET - Obtener permisos asignados a un rol
        app.get("/hm/permisos/rol/{idRol}", permisoController::getPermisosByRol);

        // GET - Obtener permisos NO asignados a un rol
        app.get("/hm/permisos/rol/{idRol}/disponibles", permisoController::getPermisosNotAssignedToRol);

        // POST - Asignar permiso específico a rol
        app.post("/hm/permisos/{idPermiso}/rol/{idRol}", permisoController::assignPermisoToRol);

        // DELETE - Remover permiso específico de rol
        app.delete("/hm/permisos/{idPermiso}/rol/{idRol}", permisoController::removePermisoFromRol);

        // POST - Asignar múltiples permisos a un rol
        app.post("/hm/permisos/rol/{idRol}/multiple", permisoController::assignMultiplePermisosToRol);

        // PUT - Sincronizar permisos de un rol (reemplazar completamente)
        app.put("/hm/permisos/rol/{idRol}/sync", permisoController::syncPermisosToRol);

        // ========================================
        // RUTAS DE VERIFICACIÓN
        // ========================================

        // GET - Verificar si un rol tiene un permiso específico
        app.get("/hm/permisos/rol/{idRol}/verificar/{idPermiso}", permisoController::checkRolHasPermiso);

        // GET - Verificar si un rol tiene un permiso por nombre
        app.get("/hm/permisos/rol/{idRol}/verificar-nombre/{nombrePermiso}", permisoController::checkRolHasPermisoByName);

        // ========================================
        // RUTAS DE ESTADÍSTICAS Y REPORTES
        // ========================================

        // GET - Estadísticas generales de permisos
        app.get("/hm/permisos/estadisticas", permisoController::getEstadisticasGenerales);

        // GET - Estadísticas de uso de permisos
        app.get("/hm/permisos/estadisticas/uso", permisoController::getEstadisticasUso);
    }
}