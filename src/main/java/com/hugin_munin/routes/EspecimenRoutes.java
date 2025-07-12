/**
 * Configuración de rutas para especímenes con CRUD completo
 */
public class EspecimenRoutes {
    private final EspecimenController especimenController;

    public EspecimenRoutes(EspecimenController especimenController) {
        this.especimenController = especimenController;
    }

    public void defineRoutes(Javalin app) {
        // GET - Obtener todos los especímenes
        app.get("/hm/especimenes", especimenController::getAllSpecimens);

        // GET - Obtener especimen por ID
        app.get("/hm/especimenes/{id}", especimenController::getSpecimenById);

        // GET - Obtener especímenes activos
        app.get("/hm/especimenes/activos", especimenController::getActiveSpecimens);

        // GET - Buscar especímenes por nombre
        app.get("/hm/especimenes/search", especimenController::searchSpecimensByName);

        // POST - Crear nuevo especimen
        app.post("/hm/especimenes", especimenController::createSpecimen);

        // PUT - Actualizar especimen existente
        app.put("/hm/especimenes/{id}", especimenController::updateSpecimen);

        // DELETE - Eliminar especimen
        app.delete("/hm/especimenes/{id}", especimenController::deleteSpecimen);

        // PATCH - Activar especimen
        app.patch("/hm/especimenes/{id}/activar", especimenController::activateSpecimen);

        // PATCH - Desactivar especimen
        app.patch("/hm/especimenes/{id}/desactivar", especimenController::deactivateSpecimen);

        // POST - Validar número de inventario
        app.post("/hm/especimenes/validar-inventario", especimenController::validateInventoryNumber);

        // GET - Estadísticas de especímenes
        app.get("/hm/especimenes/estadisticas", especimenController::getSpecimenStatistics);
    }
}