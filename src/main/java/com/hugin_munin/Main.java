package com.hugin_munin;

import com.hugin_munin.config.DatabaseConfig;
import com.hugin_munin.di.AppModule;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * HUGIN MUNIN API - MAIN CLASS
 * Sistema de gestión integral para el manejo de especies, especímenes y registros
 */
public class Main {

    private static final String API_VERSION = "1.0.0";
    private static final String API_NAME = "Hugin Munin API";

    public static void main(String[] args) {
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

        // Configurar rutas de módulos
        setupModuleRoutes(app);

        // Iniciar servidor
        app.start(7000);

        System.out.println("\n🚀 " + API_NAME + " v" + API_VERSION);
        System.out.println("📡 Servidor iniciado en: http://localhost:7000");
        System.out.println("🔗 Health Check: http://localhost:7000/");
        System.out.println("📊 Test DB: http://localhost:7000/hm/test-db");
        System.out.println("📚 Documentación: http://localhost:7000/hm/docs");
        System.out.println("===============================================\n");
    }

    /**
     * CONFIGURAR RUTAS PRINCIPALES
     */
    private static void setupMainRoutes(Javalin app) {

        // Health Check - Ruta raíz
        app.get("/", ctx -> {
            Map<String, Object> health = new HashMap<>();
            health.put("api", API_NAME);
            health.put("version", API_VERSION);
            health.put("status", "OK");
            health.put("timestamp", System.currentTimeMillis());
            health.put("message", "Hugin Munin API funcionando correctamente");

            ctx.json(health);
        });

        // Test de conexión a base de datos
        app.get("/hm/test-db", ctx -> testDatabaseConnection(ctx));

        // Documentación de API
        app.get("/hm/docs", ctx -> {
            Map<String, Object> docs = new HashMap<>();
            docs.put("api", API_NAME);
            docs.put("version", API_VERSION);
            docs.put("description", "Sistema de gestión integral Hugin Munin");

            Map<String, Object> endpoints = new HashMap<>();

            // Documentar especies
            Map<String, String> especies = new HashMap<>();
            especies.put("GET /hm/especies", "Obtener todas las especies");
            especies.put("GET /hm/especies/search?scientific_name=", "Buscar especies por nombre científico");
            especies.put("POST /hm/especies", "Crear nueva especie");
            endpoints.put("especies", especies);

            // Documentar especímenes
            Map<String, String> especimenes = new HashMap<>();
            especimenes.put("GET /hm/especimenes", "Obtener todos los especímenes");
            especimenes.put("POST /hm/especimenes", "Crear nuevo especimen");
            endpoints.put("especimenes", especimenes);

            // Documentar registros de alta
            Map<String, String> registros = new HashMap<>();
            registros.put("GET /registro_alta", "Obtener todos los registros de alta");
            registros.put("GET /registro_alta/{id}", "Obtener registro por ID");
            registros.put("POST /registro_alta", "Crear nuevo registro de alta");
            registros.put("PUT /registro_alta/{id}", "Actualizar registro de alta");
            registros.put("DELETE /registro_alta/{id}", "Eliminar registro de alta");
            endpoints.put("registros_alta", registros);

            docs.put("endpoints", endpoints);
            docs.put("database", "HUGIN_MUNIN");
            docs.put("port", 7000);

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

            ctx.json(error);
        });
    }

    /**
     * CONFIGURAR RUTAS DE MÓDULOS
     */
    private static void setupModuleRoutes(Javalin app) {

        // Rutas de Especies
        AppModule.initSpecies().defineRoutes(app);

        // Rutas de Especímenes
        AppModule.initSpecimens().defineRoutes(app);

        // Rutas de Registros de Alta
        AppModule.initRegistroAlta().defineRoutes(app);

        System.out.println("✅ Rutas configuradas:");
        System.out.println("   - Especies: /hm/especies/*");
        System.out.println("   - Especímenes: /hm/especimenes/*");
        System.out.println("   - Registros Alta: /registro_alta/*");
    }

    /**
     * TEST DE CONEXIÓN A BASE DE DATOS
     */
    private static void testDatabaseConnection(Context ctx) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            Map<String, Object> result = new HashMap<>();
            result.put("database_status", "CONECTADO");
            result.put("database_name", "HUGIN_MUNIN");
            result.put("connection_valid", conn.isValid(5));
            result.put("timestamp", System.currentTimeMillis());
            result.put("message", "Conexión a base de datos exitosa");
            result.put("success", true);

            ctx.json(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("database_status", "ERROR");
            error.put("error", e.getMessage());
            error.put("success", false);
            error.put("timestamp", System.currentTimeMillis());

            ctx.status(500).json(error);
        }
    }
}