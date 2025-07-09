package com.hugin_munin.di;

import com.hugin_munin.controller.*;
import com.hugin_munin.repository.*;
import com.hugin_munin.routes.*;
import com.hugin_munin.service.*;

/**
 * Contenedor de inyección de dependencias mejorado
 * Maneja la inicialización de todos los módulos con sus dependencias correctas
 */
public class AppModule {

    /**
     * INICIALIZAR MÓDULO DE ROLES
     */
    public static RolRoutes initRoles() {
        RolRepository rolRepository = new RolRepository();
        RolService rolService = new RolService(rolRepository);
        RolController rolController = new RolController(rolService);

        return new RolRoutes(rolController);
    }

    /**
     * INICIALIZAR MÓDULO DE USUARIOS
     */
    public static UsuarioRoutes initUsuarios() {
        RolRepository rolRepository = new RolRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, rolRepository);
        UsuarioController usuarioController = new UsuarioController(usuarioService);

        return new UsuarioRoutes(usuarioController);
    }

    /**
     * INICIALIZAR MÓDULO DE ORIGEN ALTA
     */
    public static OrigenAltaRoutes initOrigenAlta() {
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();
        OrigenAltaService origenAltaService = new OrigenAltaService(origenAltaRepository);
        OrigenAltaController origenAltaController = new OrigenAltaController(origenAltaService);

        return new OrigenAltaRoutes(origenAltaController);
    }

    /**
     * INICIALIZAR MÓDULO DE ESPECIES
     */
    public static EspecieRoutes initSpecies() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecieService especieService = new EspecieService(especieRepository);
        EspecieController especieController = new EspecieController(especieService);

        return new EspecieRoutes(especieController);
    }

    /**
     * INICIALIZAR MÓDULO DE ESPECÍMENES
     */
    public static EspecimenRoutes initSpecimens() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        EspecimenService especimenService = new EspecimenService(especimenRepository, especieRepository);
        EspecimenController especimenController = new EspecimenController(especimenService);

        return new EspecimenRoutes(especimenController);
    }

    /**
     * INICIALIZAR MÓDULO DE REGISTRO ALTA CON TODAS LAS DEPENDENCIAS
     */
    public static RegistroAltaRoutes initRegistroAlta() {
        // Inicializar repositorios
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();

        // Inicializar servicio con todas las dependencias
        RegistroAltaService registroAltaService = new RegistroAltaService(
                registroAltaRepository,
                especimenRepository,
                usuarioRepository
        );

        // Inicializar controlador
        RegistroAltaController registroAltaController = new RegistroAltaController(registroAltaService);

        return new RegistroAltaRoutes(registroAltaController);
    }

    /**
     * INFORMACIÓN COMPLETA DEL MÓDULO
     */
    public static void printModuleInfo() {
        System.out.println("=== HUGIN MUNIN API - DEPENDENCY INJECTION COMPLETO ===");
        System.out.println("✅ Módulo Rol:");
        System.out.println("   RolRepository -> RolService -> RolController");

        System.out.println("✅ Módulo Usuario:");
        System.out.println("   [RolRepository, UsuarioRepository] -> UsuarioService -> UsuarioController");

        System.out.println("✅ Módulo OrigenAlta:");
        System.out.println("   OrigenAltaRepository -> OrigenAltaService -> OrigenAltaController");

        System.out.println("✅ Módulo Especie:");
        System.out.println("   EspecieRepository -> EspecieService -> EspecieController");

        System.out.println("✅ Módulo Especimen:");
        System.out.println("   [EspecieRepository, EspecimenRepository] -> EspecimenService -> EspecimenController");

        System.out.println("✅ Módulo RegistroAlta:");
        System.out.println("   [RegistroAltaRepository, EspecimenRepository, UsuarioRepository]");
        System.out.println("   -> RegistroAltaService -> RegistroAltaController");

        System.out.println("==========================================================");
        System.out.println("📋 Patrón implementado: Repository -> Service -> Controller");
        System.out.println("🔗 Relaciones foráneas manejadas con joins completos");
        System.out.println("✅ CRUD completo para todas las entidades");
        System.out.println("==========================================================");
    }

    /**
     * INICIALIZAR TODOS LOS MÓDULOS Y RETORNAR CONFIGURADOR DE RUTAS
     */
    public static class ModuleInitializer {
        private final RolRoutes rolRoutes;
        private final UsuarioRoutes usuarioRoutes;
        private final OrigenAltaRoutes origenAltaRoutes;
        private final EspecieRoutes especieRoutes;
        private final EspecimenRoutes especimenRoutes;
        private final RegistroAltaRoutes registroAltaRoutes;

        public ModuleInitializer() {
            this.rolRoutes = initRoles();
            this.usuarioRoutes = initUsuarios();
            this.origenAltaRoutes = initOrigenAlta();
            this.especieRoutes = initSpecies();
            this.especimenRoutes = initSpecimens();
            this.registroAltaRoutes = initRegistroAlta();
        }

        public RolRoutes getRolRoutes() { return rolRoutes; }
        public UsuarioRoutes getUsuarioRoutes() { return usuarioRoutes; }
        public OrigenAltaRoutes getOrigenAltaRoutes() { return origenAltaRoutes; }
        public EspecieRoutes getEspecieRoutes() { return especieRoutes; }
        public EspecimenRoutes getEspecimenRoutes() { return especimenRoutes; }
        public RegistroAltaRoutes getRegistroAltaRoutes() { return registroAltaRoutes; }
    }

    /**
     * VALIDAR INTEGRIDAD DE DEPENDENCIAS
     */
    public static boolean validateDependencies() {
        try {
            // Probar inicialización de cada módulo
            initRoles();
            initUsuarios();
            initOrigenAlta();
            initSpecies();
            initSpecimens();
            initRegistroAlta();

            System.out.println("✅ Todas las dependencias validadas correctamente");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en la validación de dependencias: " + e.getMessage());
            return false;
        }
    }
}