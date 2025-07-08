package com.hugin_munin;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;
import com.hugin_munin.di.AppModule;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(CorsPluginConfig.CorsRule::anyHost);
            });
        }).start(7000);

        // Rutas generales
        app.get("/", ctx -> ctx.result("ApoPI Javalin 2"));

        /**
         * INIT ENDPOINT ROUTES
         **/
        AppModule.initSpecies().defineRoutes(app);
        AppModule.initSpecimens().defineRoutes(app);
        AppModule.initRegistroAlta().defineRoutes(app);
    }
}