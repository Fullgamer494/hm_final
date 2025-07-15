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
import java.util.List;
import java.util.Arrays;

/**
 * HUGIN MUNIN API - CLASE PRINCIPAL INTEGRADA Y MEJORADA
 * Sistema de gestión integral para el manejo de especies, especímenes y registros
 * INTEGRADO: Combina funcionalidad existente con mejoras del sistema de permisos
 * ACTUALIZADO: Incluye módulos completos de reportes y sistema de permisos
 * CORREGIDO: Orden de configuración de rutas y middleware
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

            // Crear aplicación Javalin con configuración CORS corregida
            Javalin app = Javalin.create(config -> {
                // Configuración CORS corregida para versiones recientes de Javalin
                config.bundledPlugins.enableCors(cors -> {
                    cors.addRule(it -> {
                        it.anyHost();
                        it.allowCredentials = true;
                    });
                });

                // Configurar logging
                config.bundledPlugins.enableRouteOverview("/routes");
                config.bundledPlugins.enableDevLogging();
                config.http.defaultContentType = "application/json";
                config.showJavalinBanner = false;
            });

            // Configurar rutas principales
            setupMainRoutes(app);

            // Configurar rutas de módulos COMPLETAMENTE ACTUALIZADAS
            setupModuleRoutes(app);

            // Iniciar servidor
            app.start(7000);

            // Mostrar información de inicio COMPLETA
            showStartupInfo();

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
     * Configurar rutas principales de la API COMPLETAMENTE ACTUALIZADAS
     */
    private static void setupMainRoutes(Javalin app) {

        // Health Check - Ruta raíz MEJORADA
        app.get("/", ctx -> {
            Map<String, Object> health = new HashMap<>();
            health.put("api", API_NAME);
            health.put("version", API_VERSION);
            health.put("status", "OK");
            health.put("timestamp", System.currentTimeMillis());
            health.put("message", "Hugin Munin API funcionando correctamente con autenticación y permisos");
            health.put("auth_available", true);
            health.put("permissions_available", true);
            health.put("login_endpoint", "/hm/auth/login");

            ctx.json(health);
        });

        // Documentación de API COMPLETAMENTE ACTUALIZADA con nuevos módulos
        app.get("/hm/docs", ctx -> {
            Map<String, Object> docs = new HashMap<>();
            docs.put("api", API_NAME);
            docs.put("version", API_VERSION);
            docs.put("description", "Sistema de gestión integral Hugin Munin con módulos completos de reportes y permisos");

            Map<String, Object> endpoints = new HashMap<>();

            // DOCUMENTAR autenticación
            Map<String, String> auth = new HashMap<>();
            auth.put("POST /hm/auth/login", "Iniciar sesión con nombre_usuario y contrasena");
            auth.put("POST /hm/auth/logout", "Cerrar sesión actual");
            auth.put("GET /hm/auth/verify", "Verificar si hay sesión activa");
            auth.put("GET /hm/auth/profile", "Obtener perfil del usuario autenticado");
            auth.put("PUT /hm/auth/change-password", "Cambiar contraseña del usuario");
            endpoints.put("autenticacion", auth);

            // DOCUMENTAR sistema de permisos (NUEVO)
            Map<String, String> permisos = new HashMap<>();
            permisos.put("GET /hm/permisos", "Obtener todos los permisos");
            permisos.put("GET /hm/permisos/categorias", "Agrupar permisos por categoría");
            permisos.put("GET /hm/permisos/rol/{id}", "Obtener permisos de un rol");
            permisos.put("POST /hm/permisos/{id}/rol/{id}", "Asignar permiso a rol");
            permisos.put("PUT /hm/permisos/rol/{id}/sync", "Sincronizar permisos de rol");
            permisos.put("GET /hm/permisos/estadisticas", "Obtener estadísticas de uso de permisos");
            endpoints.put("permisos", permisos);

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

            // DOCUMENTAR tipos de reporte
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

            // DOCUMENTAR reportes (clase padre)
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

            // DOCUMENTAR reportes de traslado (clase hija)
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
                    "sistema_autenticacion", "Sistema de autenticación basado en cookies",
                    "sistema_permisos", "Gestión completa de permisos y roles (NUEVO)",
                    "registro_unificado", "Creación coordinada de especie + especimen + registro + reporte traslado",
                    "sistema_reportes", "Sistema completo de reportes con herencia (TipoReporte -> Reporte -> ReporteTraslado)",
                    "validaciones", "Validaciones de negocio y datos",
                    "estadisticas", "Endpoints de estadísticas y reportes",
                    "busquedas_avanzadas", "Búsquedas por múltiples criterios y filtros específicos",
                    "gestion_traslados", "Seguimiento completo de traslados de especímenes"
            ));

            ctx.json(docs);
        });

        // Test de base de datos
        app.get("/hm/test-db", ctx -> {
            try (Connection conn = DatabaseConfig.getConnection()) {
                Map<String, Object> dbInfo = new HashMap<>();
                dbInfo.put("status", "Connected");
                dbInfo.put("database", conn.getCatalog());
                dbInfo.put("url", conn.getMetaData().getURL());
                dbInfo.put("timestamp", System.currentTimeMillis());
                ctx.json(dbInfo);
            } catch (Exception e) {
                ctx.status(500).json(Map.of(
                        "status", "Error",
                        "error", e.getMessage(),
                        "timestamp", System.currentTimeMillis()
                ));
            }
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
     * Configurar rutas de módulos - ORDEN CORREGIDO
     * PASO 1: Rutas públicas ANTES del middleware
     * PASO 2: Configurar middleware
     * PASO 3: Rutas protegidas DESPUÉS del middleware
     */
    private static void setupModuleRoutes(Javalin app) {
        try {
            System.out.println("\n🚀 INICIANDO CONFIGURACIÓN DE RUTAS - ORDEN CRÍTICO");

            // ===========================================
            // PASO 1: CONFIGURAR RUTAS PÚBLICAS PRIMERO ⭐ CRÍTICO
            // ===========================================
            System.out.println("\n📍 PASO 1: Configurando rutas PÚBLICAS (antes del middleware)");

            // Rutas de autenticación (DEBEN ir ANTES del middleware)
            AppModule.initAuth().defineRoutes(app);
            System.out.println("✅ Rutas de autenticación configuradas (incluye login, logout, verify, profile, change-password)");

            System.out.println("📋 RUTAS PÚBLICAS configuradas:");
            System.out.println("   - POST /hm/auth/login");
            System.out.println("   - POST /hm/auth/logout");
            System.out.println("   - GET /hm/auth/verify");
            System.out.println("📋 RUTAS PROTEGIDAS configuradas:");
            System.out.println("   - GET /hm/auth/profile");
            System.out.println("   - PUT /hm/auth/change-password");

            // ===========================================
            // PASO 2: CONFIGURAR MIDDLEWARE DE AUTENTICACIÓN ⭐ CRÍTICO
            // ===========================================
            System.out.println("\n📍 PASO 2: Configurando middleware de autenticación");

            // Configurar middleware de autenticación
            AuthService authService = AppModule.getAuthService();
            AuthMiddleware authMiddleware = new AuthMiddleware(authService);

            // Aplicar middleware de autenticación a todas las rutas /hm/* excepto públicas
            app.before("/hm/*", authMiddleware.handle());
            System.out.println("✅ Middleware de autenticación configurado para rutas /hm/*");
            System.out.println("🔒 El middleware se ejecutará DESPUÉS de las rutas públicas");
            System.out.println("🔒 /hm/auth/profile pasará por el middleware (requiere autenticación)");

            // ===========================================
            // PASO 3: CONFIGURAR RUTAS PROTEGIDAS ⭐ DESPUÉS DEL MIDDLEWARE
            // ===========================================
            System.out.println("\n📍 PASO 3: Configurando rutas PROTEGIDAS (después del middleware)");

            // Rutas de Roles
            AppModule.initRoles().defineRoutes(app);
            System.out.println("✅ Rutas de roles configuradas");

            // Rutas de Permisos (si están disponibles)
            try {
                AppModule.initPermisos().defineRoutes(app);
                System.out.println("✅ Rutas de permisos configuradas");
            } catch (Exception e) {
                System.err.println("⚠️ Sistema de permisos no disponible: " + e.getMessage());
            }

            // Rutas de Usuarios
            AppModule.initUsuarios().defineRoutes(app);
            System.out.println("✅ Rutas de usuarios configuradas");

            // Rutas de Origen Alta
            AppModule.initOrigenAlta().defineRoutes(app);
            System.out.println("✅ Rutas de origen alta configuradas");

            // Rutas de Causa Baja
            AppModule.initCausaBaja().defineRoutes(app);
            System.out.println("✅ Rutas de causa baja configuradas");

            // Rutas de Especies
            AppModule.initSpecies().defineRoutes(app);
            System.out.println("✅ Rutas de especies configuradas");

            // Rutas de Especímenes
            AppModule.initSpecimens().defineRoutes(app);
            System.out.println("✅ Rutas de especímenes configuradas");

            // Rutas de Tipos de Reporte
            AppModule.initTipoReporte().defineRoutes(app);
            System.out.println("✅ Rutas de tipos de reporte configuradas");

            // Rutas de Reportes (clase padre)
            AppModule.initReporte().defineRoutes(app);
            System.out.println("✅ Rutas de reportes configuradas");

            // Rutas de Reportes de Traslado (clase hija)
            AppModule.initReporteTraslado().defineRoutes(app);
            System.out.println("✅ Rutas de reportes de traslado configuradas");

            // Rutas de Registro Unificado
            AppModule.initRegistroUnificado().defineRoutes(app);
            System.out.println("✅ Rutas de registro unificado configuradas");

            // Rutas de Registros de Alta
            AppModule.initRegistroAlta().defineRoutes(app);
            System.out.println("✅ Rutas de registros de alta configuradas");

            // Rutas de Registros de Baja
            AppModule.initRegistroBaja().defineRoutes(app);
            System.out.println("✅ Rutas de registros de baja configuradas");

            // ===========================================
            // RESUMEN FINAL
            // ===========================================
            System.out.println("\n🎉 CONFIGURACIÓN DE RUTAS COMPLETADA");
            System.out.println("🔐 Sistema de autenticación basado en cookies activado");
            System.out.println("🔒 Middleware aplicado correctamente");
            System.out.println("✅ /hm/auth/profile configurado como ruta PROTEGIDA");
            System.out.println("✅ Orden correcto: Rutas públicas → Middleware → Rutas protegidas\n");

        } catch (Exception e) {
            System.err.println("❌ ERROR CRÍTICO en configuración de rutas:");
            e.printStackTrace();
            throw new RuntimeException("Error en configuración de rutas", e);
        }
    }

    /**
     * Mostrar información completa de inicio del sistema
     */
    private static void showStartupInfo() {
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
        System.out.println("🔑 Permisos: http://localhost:7000/hm/permisos (NUEVO)");
        System.out.println("🔐 AUTENTICACIÓN REQUERIDA para la mayoría de endpoints");

        System.out.println("\n==========================================================");
        System.out.println("📋 ENDPOINTS PRINCIPALES:");
        System.out.println("🔐 Autenticación: /hm/auth/*");
        System.out.println("👥 Roles: /hm/roles/*");
        System.out.println("🔑 Permisos: /hm/permisos/* (NUEVO)");
        System.out.println("👤 Usuarios: /hm/usuarios/*");
        System.out.println("🧬 Especies: /hm/especies/*");
        System.out.println("🐾 Especímenes: /hm/especimenes/*");
        System.out.println("📊 Reportes: /hm/reportes/*");
        System.out.println("🔄 Reportes Traslado: /hm/reportes-traslado/*");
        System.out.println("📝 Registros: /hm/registros/*");

        System.out.println("\n🔑 FUNCIONALIDADES NUEVAS DE PERMISOS:");
        System.out.println("   • GET /hm/permisos - Listar todos los permisos");
        System.out.println("   • GET /hm/permisos/categorias - Permisos por categoría");
        System.out.println("   • GET /hm/permisos/rol/{id} - Permisos de un rol");
        System.out.println("   • POST /hm/permisos/{id}/rol/{id} - Asignar permiso a rol");
        System.out.println("   • PUT /hm/permisos/rol/{id}/sync - Sincronizar permisos");
        System.out.println("   • GET /hm/permisos/estadisticas - Estadísticas de uso");

        System.out.println("\n✨ NUEVAS CARACTERÍSTICAS:");
        System.out.println("   🔒 Sistema completo de permisos");
        System.out.println("   📊 Sistema de reportes con herencia");
        System.out.println("   🔄 Reportes de traslado específicos");
        System.out.println("   🧹 Middleware de autenticación mejorado");
        System.out.println("   🛡️ Validaciones robustas de seguridad");
        System.out.println("   ✅ ORDEN DE CONFIGURACIÓN CORREGIDO");
        System.out.println("==========================================================\n");
    }
}