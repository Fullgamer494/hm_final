package com.hugin_munin;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.di.AppModule;

import io.javalin.Javalin;
import io.javalin.http.Context;

import com.hugin_munin.middleware.AuthMiddleware;
import com.hugin_munin.service.AuthService;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * HUGIN MUNIN API - CLASE PRINCIPAL ACTUALIZADA
 * Sistema de gestión integral para el manejo de especies, especímenes y registros
 * ACTUALIZADO: Incluye módulos completos de reportes (TipoReporte, Reporte, ReporteTraslado)
 */
public class Main {

    private static final String API_VERSION = "1.0.0";
    private static final String API_NAME = "Hugin Munin API";

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando " + API_NAME + " v" + API_VERSION);

            // Verificar conexión a base de datos primero
            testDatabaseConnection();

            // Mostrar información del módulo
            AppModule.printModuleInfo();

            // Crear aplicación Javalin
            Javalin app = Javalin.create(config -> {
                // Configurar CORS
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        it.anyHost();
                        it.allowCredentials = true;
                    });
                });

                // Configurar logging
                config.bundledPlugins.enableRouteOverview("/routes");
                config.http.defaultContentType = "application/json";
            });

            // Configurar rutas principales
            setupMainRoutes(app);

            // Configurar rutas de módulos ACTUALIZADAS
            setupModuleRoutes(app);

            // Iniciar servidor
            app.start(7000);

            System.out.println("\n🚀 " + API_NAME + " v" + API_VERSION);
            System.out.println("📡 Servidor iniciado en: http://localhost:7000");
            System.out.println("🔗 Health Check: http://localhost:7000/");
            System.out.println("📊 Test DB: http://localhost:7000/hm/test-db");
            System.out.println("📚 Documentación: http://localhost:7000/hm/docs");
            System.out.println("🔐 Login: http://localhost:7000/hm/auth/login");
            System.out.println("👤 Perfil: http://localhost:7000/hm/auth/profile");
            System.out.println("🚀 Registro Unificado: http://localhost:7000/hm/registro-unificado/ejemplo");
            System.out.println("📋 Tipos de Reporte: http://localhost:7000/hm/tipos-reporte");
            System.out.println("📄 Reportes: http://localhost:7000/hm/reportes");
            System.out.println("🔄 Reportes Traslado: http://localhost:7000/hm/reportes-traslado");
            System.out.println("🔐 AUTENTICACIÓN REQUERIDA para la mayoría de endpoints");
            System.out.println("===============================================\n");

        } catch (Exception e) {
            System.err.println("❌ Error al iniciar la aplicación:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Verificar conexión a base de datos al inicio
     */
    private static void testDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            System.out.println("✅ Conexión a base de datos verificada exitosamente");
        } catch (Exception e) {
            System.err.println("❌ ERROR: No se pudo conectar a la base de datos");
            System.err.println("Detalle del error: " + e.getMessage());
            System.err.println("\nVerifica que:");
            System.err.println("1. MySQL esté ejecutándose");
            System.err.println("2. La base de datos HUGIN_MUNIN exista");
            System.err.println("3. Las credenciales en .env sean correctas");
            System.err.println("4. El archivo .env exista en la raíz del proyecto");
            throw new RuntimeException("Error de conexión a base de datos", e);
        }
    }

    /**
     * Configurar rutas principales de la API ACTUALIZADAS
     */
    private static void setupMainRoutes(Javalin app) {

        // Health Check - Ruta raíz
        app.get("/", ctx -> {
            Map<String, Object> health = new HashMap<>();
            health.put("api", API_NAME);
            health.put("version", API_VERSION);
            health.put("status", "OK");
            health.put("timestamp", System.currentTimeMillis());
            health.put("message", "Hugin Munin API funcionando correctamente con autenticación");
            health.put("auth_available", true);
            health.put("login_endpoint", "/hm/auth/login");

            ctx.json(health);
        });

        // Documentación de API ACTUALIZADA con nuevos módulos
        app.get("/hm/docs", ctx -> {
        Map<String, Object> docs = new HashMap<>();
        docs.put("api", API_NAME);
        docs.put("version", API_VERSION);
        docs.put("description", "Sistema de gestión integral Hugin Munin con módulos completos de reportes");

        Map<String, Object> endpoints = new HashMap<>();

        // NUEVO: Documentar autenticación
        Map<String, String> auth = new HashMap<>();
        auth.put("POST /hm/auth/login", "Iniciar sesión con nombre_usuario y contrasena");
        auth.put("POST /hm/auth/logout", "Cerrar sesión actual");
        auth.put("GET /hm/auth/verify", "Verificar si hay sesión activa");
        auth.put("GET /hm/auth/profile", "Obtener perfil del usuario autenticado");
        auth.put("PUT /hm/auth/change-password", "Cambiar contraseña del usuario");
        endpoints.put("autenticacion", auth);

        // Documentar registro unificado ACTUALIZADO
        Map<String, String> registroUnificado = new HashMap<>();
        registroUnificado.put("POST /hm/registro-unificado", "Crear especie, especimen, registro de alta y opcionalmente reporte de traslado");
        registroUnificado.put("POST /hm/registro-unificado/validar", "Validar datos sin crear registros");
        registroUnificado.put("GET /hm/registro-unificado/ejemplo", "Obtener ejemplo de estructura JSON con reportes");
        registroUnificado.put("GET /hm/registro-unificado/formulario-data", "Obtener datos para formulario");
        endpoints.put("registro_unificado", registroUnificado);

        // Documentar especies
        Map<String, String> especies = new HashMap<>();
        especies.put("GET /hm/especies", "Obtener todas las especies");
        especies.put("GET /hm/especies/{id}", "Obtener especie por ID");
        especies.put("GET /hm/especies/search?scientific_name=", "Buscar especies por nombre científico");
        especies.put("POST /hm/especies", "Crear nueva especie");
        especies.put("PUT /hm/especies/{id}", "Actualizar especie");
        especies.put("DELETE /hm/especies/{id}", "Eliminar especie");
        especies.put("POST /hm/especies/validar-nombre", "Validar nombre científico");
        especies.put("GET /hm/especies/estadisticas", "Obtener estadísticas");
        endpoints.put("especies", especies);

        // Documentar especímenes
        Map<String, String> especimenes = new HashMap<>();
        especimenes.put("GET /hm/especimenes", "Obtener todos los especímenes");
        especimenes.put("GET /hm/especimenes/{id}", "Obtener especimen por ID");
        especimenes.put("GET /hm/especimenes/activos", "Obtener especímenes activos");
        especimenes.put("GET /hm/especimenes/search?nombre=", "Buscar especímenes por nombre");
        especimenes.put("POST /hm/especimenes", "Crear nuevo especimen");
        especimenes.put("PUT /hm/especimenes/{id}", "Actualizar especimen");
        especimenes.put("DELETE /hm/especimenes/{id}", "Eliminar especimen");
        especimenes.put("PATCH /hm/especimenes/{id}/activar", "Activar especimen");
        especimenes.put("PATCH /hm/especimenes/{id}/desactivar", "Desactivar especimen");
        especimenes.put("POST /hm/especimenes/validar-inventario", "Validar número de inventario");
        especimenes.put("GET /hm/especimenes/estadisticas", "Obtener estadísticas");
        endpoints.put("especimenes", especimenes);

        // Documentar roles
        Map<String, String> roles = new HashMap<>();
        roles.put("GET /hm/roles", "Obtener todos los roles");
        roles.put("GET /hm/roles/activos", "Obtener roles activos");
        roles.put("GET /hm/roles/{id}", "Obtener rol por ID");
        roles.put("POST /hm/roles", "Crear nuevo rol");
        roles.put("PUT /hm/roles/{id}", "Actualizar rol");
        roles.put("DELETE /hm/roles/{id}", "Eliminar rol");
        endpoints.put("roles", roles);

        // Documentar usuarios
        Map<String, String> usuarios = new HashMap<>();
        usuarios.put("GET /hm/usuarios", "Obtener todos los usuarios");
        usuarios.put("GET /hm/usuarios/{id}", "Obtener usuario por ID");
        usuarios.put("POST /hm/usuarios", "Crear nuevo usuario");
        usuarios.put("PUT /hm/usuarios/{id}", "Actualizar usuario");
        usuarios.put("DELETE /hm/usuarios/{id}", "Eliminar usuario");
        endpoints.put("usuarios", usuarios);

        // Documentar orígenes de alta
        Map<String, String> origenes = new HashMap<>();
        origenes.put("GET /hm/origenes-alta", "Obtener todos los orígenes de alta");
        origenes.put("GET /hm/origenes-alta/{id}", "Obtener origen por ID");
        origenes.put("POST /hm/origenes-alta", "Crear nuevo origen de alta");
        origenes.put("PUT /hm/origenes-alta/{id}", "Actualizar origen");
        origenes.put("DELETE /hm/origenes-alta/{id}", "Eliminar origen");
        endpoints.put("origenes_alta", origenes);

        // Documentar causas de baja
        Map<String, String> causas = new HashMap<>();
        causas.put("GET /hm/causas-baja", "Obtener todas las causas de baja");
        causas.put("GET /hm/causas-baja/{id}", "Obtener causa por ID");
        causas.put("POST /hm/causas-baja", "Crear nueva causa de baja");
        causas.put("PUT /hm/causas-baja/{id}", "Actualizar causa");
        causas.put("DELETE /hm/causas-baja/{id}", "Eliminar causa");
        endpoints.put("causas_baja", causas);

        // NUEVO: Documentar tipos de reporte
        Map<String, String> tiposReporte = new HashMap<>();
        tiposReporte.put("GET /hm/tipos-reporte", "Obtener todos los tipos de reporte");
        tiposReporte.put("GET /hm/tipos-reporte/activos", "Obtener tipos activos");
        tiposReporte.put("GET /hm/tipos-reporte/{id}", "Obtener tipo por ID");
        tiposReporte.put("POST /hm/tipos-reporte", "Crear nuevo tipo de reporte");
        tiposReporte.put("PUT /hm/tipos-reporte/{id}", "Actualizar tipo");
        tiposReporte.put("DELETE /hm/tipos-reporte/{id}", "Eliminar tipo");
        tiposReporte.put("PATCH /hm/tipos-reporte/{id}/activar", "Activar tipo");
        tiposReporte.put("PATCH /hm/tipos-reporte/{id}/desactivar", "Desactivar tipo");
        tiposReporte.put("GET /hm/tipos-reporte/estadisticas", "Obtener estadísticas");
        endpoints.put("tipos_reporte", tiposReporte);

        // NUEVO: Documentar reportes (clase padre)
        Map<String, String> reportes = new HashMap<>();
        reportes.put("GET /hm/reportes", "Obtener todos los reportes");
        reportes.put("GET /hm/reportes/activos", "Obtener reportes activos");
        reportes.put("GET /hm/reportes/{id}", "Obtener reporte por ID");
        reportes.put("POST /hm/reportes", "Crear nuevo reporte");
        reportes.put("PUT /hm/reportes/{id}", "Actualizar reporte");
        reportes.put("DELETE /hm/reportes/{id}", "Eliminar reporte");
        reportes.put("GET /hm/reportes/tipo/{id}", "Buscar por tipo de reporte");
        reportes.put("GET /hm/reportes/especimen/{id}", "Buscar por especimen");
        reportes.put("GET /hm/reportes/responsable/{id}", "Buscar por responsable");
        reportes.put("GET /hm/reportes/search/asunto?q=", "Buscar por asunto");
        reportes.put("GET /hm/reportes/search/contenido?q=", "Buscar por contenido");
        reportes.put("GET /hm/reportes/fechas?inicio=YYYY-MM-DD&fin=YYYY-MM-DD", "Buscar por rango de fechas");
        reportes.put("GET /hm/reportes/estadisticas", "Obtener estadísticas");
        endpoints.put("reportes", reportes);

        // NUEVO: Documentar reportes de traslado (clase hija)
        Map<String, String> reportesTraslado = new HashMap<>();
        reportesTraslado.put("GET /hm/reportes-traslado", "Obtener todos los reportes de traslado");
        reportesTraslado.put("GET /hm/reportes-traslado/{id}", "Obtener reporte de traslado por ID");
        reportesTraslado.put("POST /hm/reportes-traslado", "Crear nuevo reporte de traslado");
        reportesTraslado.put("PUT /hm/reportes-traslado/{id}", "Actualizar reporte de traslado");
        reportesTraslado.put("DELETE /hm/reportes-traslado/{id}", "Eliminar reporte de traslado");
        reportesTraslado.put("GET /hm/reportes-traslado/area-origen/{area}", "Buscar por área origen");
        reportesTraslado.put("GET /hm/reportes-traslado/area-destino/{area}", "Buscar por área destino");
        reportesTraslado.put("GET /hm/reportes-traslado/ubicacion-origen/{ubicacion}", "Buscar por ubicación origen");
        reportesTraslado.put("GET /hm/reportes-traslado/ubicacion-destino/{ubicacion}", "Buscar por ubicación destino");
        reportesTraslado.put("GET /hm/reportes-traslado/search/motivo?q=", "Buscar por motivo");
        reportesTraslado.put("GET /hm/reportes-traslado/especimen/{id}", "Buscar por especimen");
        reportesTraslado.put("GET /hm/reportes-traslado/responsable/{id}", "Buscar por responsable");
        reportesTraslado.put("GET /hm/reportes-traslado/fechas?inicio=YYYY-MM-DD&fin=YYYY-MM-DD", "Buscar por fechas");
        reportesTraslado.put("GET /hm/reportes-traslado/estadisticas", "Estadísticas de traslados");
        reportesTraslado.put("GET /hm/reportes-traslado/estadisticas/areas-origen?limit=", "Áreas origen populares");
        reportesTraslado.put("GET /hm/reportes-traslado/estadisticas/areas-destino?limit=", "Áreas destino populares");
        endpoints.put("reportes_traslado", reportesTraslado);

        // Documentar registros de alta
        Map<String, String> registrosAlta = new HashMap<>();
        registrosAlta.put("GET /hm/registro_alta", "Obtener todos los registros de alta");
        registrosAlta.put("GET /hm/registro_alta/{id}", "Obtener registro por ID");
        registrosAlta.put("POST /hm/registro_alta", "Crear nuevo registro de alta");
        registrosAlta.put("PUT /hm/registro_alta/{id}", "Actualizar registro de alta");
        registrosAlta.put("DELETE /hm/registro_alta/{id}", "Eliminar registro de alta");
        endpoints.put("registros_alta", registrosAlta);

        // Documentar registros de baja
        Map<String, String> registrosBaja = new HashMap<>();
        registrosBaja.put("GET /hm/registro_baja", "Obtener todos los registros de baja");
        registrosBaja.put("GET /hm/registro_baja/{id}", "Obtener registro por ID");
        registrosBaja.put("POST /hm/registro_baja", "Crear nuevo registro de baja");
        registrosBaja.put("PUT /hm/registro_baja/{id}", "Actualizar registro de baja");
        registrosBaja.put("DELETE /hm/registro_baja/{id}", "Eliminar registro de baja");
        endpoints.put("registros_baja", registrosBaja);

        docs.put("endpoints", endpoints);
        docs.put("database", "HUGIN_MUNIN");
        docs.put("port", 7000);
        docs.put("features", Map.of(
                "crud_completo", "Operaciones CRUD para todas las entidades",
                "registro_unificado", "Creación coordinada de especie + especimen + registro + reporte traslado",
                "sistema_reportes", "Sistema completo de reportes con herencia (TipoReporte -> Reporte -> ReporteTraslado)",
                "validaciones", "Validaciones de negocio y datos",
                "estadisticas", "Endpoints de estadísticas y reportes",
                "busquedas_avanzadas", "Búsquedas por múltiples criterios y filtros específicos",
                "gestion_traslados", "Seguimiento completo de traslados de especímenes"
        ));

        ctx.json(docs);
    });

        // Manejo de rutas no encontradas
        app.error(404, ctx -> {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Ruta no encontrada");
            error.put("path", ctx.path());
            error.put("method", ctx.method().toString());
            error.put("message", "La ruta solicitada no existe en esta API");
            error.put("available_docs", "/hm/docs");
            error.put("login_endpoint", "/hm/auth/login");

            ctx.json(error);
        });
    }

    /**
     * Configurar rutas de módulos COMPLETAMENTE ACTUALIZADO
     * Incluye todos los nuevos módulos de reportes
     */
    private static void setupModuleRoutes(Javalin app) {
        try {
            // Configurar middleware de autenticación
            AuthService authService = AppModule.getAuthService();
            AuthMiddleware authMiddleware = new AuthMiddleware(authService);

            // Aplicar middleware de autenticación a todas las rutas /hm/* excepto públicas
            app.before("/hm/*", authMiddleware.handle());

            // Rutas de autenticación (deben ir antes del middleware)
            AppModule.initAuth().defineRoutes(app);

            // Rutas de Roles
            AppModule.initRoles().defineRoutes(app);

            // Rutas de Usuarios
            AppModule.initUsuarios().defineRoutes(app);

            // Rutas de Origen Alta
            AppModule.initOrigenAlta().defineRoutes(app);

            // Rutas de Causa Baja
            AppModule.initCausaBaja().defineRoutes(app);

            // Rutas de Especies
            AppModule.initSpecies().defineRoutes(app);

            // Rutas de Especímenes
            AppModule.initSpecimens().defineRoutes(app);

            // Rutas de Tipos de Reporte
            AppModule.initTipoReporte().defineRoutes(app);

            // Rutas de Reportes (clase padre)
            AppModule.initReporte().defineRoutes(app);

            // Rutas de Reportes de Traslado (clase hija)
            AppModule.initReporteTraslado().defineRoutes(app);

            // Rutas de Registro Unificado
            AppModule.initRegistroUnificado().defineRoutes(app);

            // Rutas de Registros de Alta
            AppModule.initRegistroAlta().defineRoutes(app);

            // Rutas de Registros de Baja
            AppModule.initRegistroBaja().defineRoutes(app);

            System.out.println("✅ Rutas configuradas exitosamente con autenticación:");
            System.out.println("   - Autenticación: /hm/auth/* (NUEVO)");
            System.out.println("   - Middleware aplicado a todas las rutas /hm/*");
            System.out.println("   - Roles: /hm/roles/*");
            System.out.println("   - Usuarios: /hm/usuarios/*");
            System.out.println("   - Origen Alta: /hm/origenes-alta/*");
            System.out.println("   - Causa Baja: /hm/causas-baja/*");
            System.out.println("   - Especies: /hm/especies/*");
            System.out.println("   - Especímenes: /hm/especimenes/*");
            System.out.println("   - Tipos Reporte: /hm/tipos-reporte/*");
            System.out.println("   - Reportes: /hm/reportes/*");
            System.out.println("   - Reportes Traslado: /hm/reportes-traslado/*");
            System.out.println("   - Registro Unificado: /hm/registro-unificado/*");
            System.out.println("   - Registros Alta: /hm/registro_alta/*");
            System.out.println("   - Registros Baja: /hm/registro_baja/*");
            System.out.println("🔐 Sistema de autenticación basado en cookies activado");

        } catch (Exception e) {
            System.err.println("❌ Error al configurar rutas de módulos:");
            e.printStackTrace();
            throw new RuntimeException("Error en configuración de rutas", e);
        }
    }
}