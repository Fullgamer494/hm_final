package com.hugin_munin.di;

import com.hugin_munin.controller.*;
import com.hugin_munin.repository.*;
import com.hugin_munin.routes.*;
import com.hugin_munin.service.*;

/**
 * Contenedor de inyección de dependencias
 * Maneja la inicialización de todos los módulos con sus dependencias
 */
public class AppModule {

    /**
     * Inicializar módulo de roles
     */
    public static RolRoutes initRoles() {
        RolRepository rolRepository = new RolRepository();
        RolService rolService = new RolService(rolRepository);
        RolController rolController = new RolController(rolService);

        return new RolRoutes(rolController);
    }

    /**
     * Inicializar módulo de usuarios
     */
    public static UsuarioRoutes initUsuarios() {
        RolRepository rolRepository = new RolRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        UsuarioService usuarioService = new UsuarioService(usuarioRepository, rolRepository);
        UsuarioController usuarioController = new UsuarioController(usuarioService);

        return new UsuarioRoutes(usuarioController);
    }

    /**
     * Inicializar módulo de origen alta
     */
    public static OrigenAltaRoutes initOrigenAlta() {
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();
        OrigenAltaService origenAltaService = new OrigenAltaService(origenAltaRepository);
        OrigenAltaController origenAltaController = new OrigenAltaController(origenAltaService);

        return new OrigenAltaRoutes(origenAltaController);
    }

    /**
     * Inicializar módulo de causa baja
     */
    public static CausaBajaRoutes initCausaBaja() {
        CausaBajaRepository causaBajaRepository = new CausaBajaRepository();
        CausaBajaService causaBajaService = new CausaBajaService(causaBajaRepository);
        CausaBajaController causaBajaController = new CausaBajaController(causaBajaService);

        return new CausaBajaRoutes(causaBajaController);
    }

    /**
     * Inicializar módulo de especies con CRUD completo
     */
    public static EspecieRoutes initSpecies() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecieService especieService = new EspecieService(especieRepository);
        EspecieController especieController = new EspecieController(especieService);

        return new EspecieRoutes(especieController);
    }

    /**
     * Inicializar módulo de especímenes con todas las dependencias
     */
    public static EspecimenRoutes initSpecimens() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();

        EspecimenService especimenService = new EspecimenService(
                especimenRepository,
                especieRepository,
                registroAltaRepository,
                usuarioRepository,
                origenAltaRepository
        );
        EspecimenController especimenController = new EspecimenController(especimenService);

        return new EspecimenRoutes(especimenController);
    }

    /**
     * Inicializar módulo de registro unificado
     * Utiliza el mismo servicio de especímenes pero con un controlador específico
     */
    public static RegistroUnificadoRoutes initRegistroUnificado() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();

        EspecimenService especimenService = new EspecimenService(
                especimenRepository,
                especieRepository,
                registroAltaRepository,
                usuarioRepository,
                origenAltaRepository
        );
        RegistroUnificadoController unificadoController = new RegistroUnificadoController(especimenService);

        return new RegistroUnificadoRoutes(unificadoController);
    }

    /**
     * Inicializar módulo de registro alta con todas las dependencias
     */
    public static RegistroAltaRoutes initRegistroAlta() {
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();

        RegistroAltaService registroAltaService = new RegistroAltaService(
                registroAltaRepository,
                especimenRepository,
                usuarioRepository
        );

        RegistroAltaController registroAltaController = new RegistroAltaController(registroAltaService);

        return new RegistroAltaRoutes(registroAltaController);
    }

    /**
     * Inicializar módulo de registro baja con todas las dependencias
     */
    public static RegistroBajaRoutes initRegistroBaja() {
        RegistroBajaRepository registroBajaRepository = new RegistroBajaRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        CausaBajaRepository causaBajaRepository = new CausaBajaRepository();

        RegistroBajaService registroBajaService = new RegistroBajaService(
                registroBajaRepository,
                especimenRepository,
                usuarioRepository,
                causaBajaRepository
        );

        RegistroBajaController registroBajaController = new RegistroBajaController(registroBajaService);

        return new RegistroBajaRoutes(registroBajaController);
    }

    /**
     * Información completa del módulo
     */
    public static void printModuleInfo() {
        System.out.println("=== HUGIN MUNIN API - DEPENDENCY INJECTION COMPLETO ===");
        System.out.println("✅ Módulo Rol:");
        System.out.println("   RolRepository -> RolService -> RolController");

        System.out.println("✅ Módulo Usuario:");
        System.out.println("   [RolRepository, UsuarioRepository] -> UsuarioService -> UsuarioController");

        System.out.println("✅ Módulo OrigenAlta:");
        System.out.println("   OrigenAltaRepository -> OrigenAltaService -> OrigenAltaController");

        System.out.println("✅ Módulo CausaBaja:");
        System.out.println("   CausaBajaRepository -> CausaBajaService -> CausaBajaController");

        System.out.println("✅ Módulo Especie:");
        System.out.println("   EspecieRepository -> EspecieService -> EspecieController");

        System.out.println("✅ Módulo Especimen:");
        System.out.println("   [EspecieRepository, EspecimenRepository, RegistroAltaRepository,");
        System.out.println("    UsuarioRepository, OrigenAltaRepository] -> EspecimenService -> EspecimenController");

        System.out.println("✅ Módulo RegistroUnificado:");
        System.out.println("   [Mismas dependencias que Especimen] -> EspecimenService -> RegistroUnificadoController");

        System.out.println("✅ Módulo RegistroAlta:");
        System.out.println("   [RegistroAltaRepository, EspecimenRepository, UsuarioRepository]");
        System.out.println("   -> RegistroAltaService -> RegistroAltaController");

        System.out.println("✅ Módulo RegistroBaja:");
        System.out.println("   [RegistroBajaRepository, EspecimenRepository, UsuarioRepository, CausaBajaRepository]");
        System.out.println("   -> RegistroBajaService -> RegistroBajaController");

        System.out.println("==========================================================");
        System.out.println("📋 Patrón implementado: Repository -> Service -> Controller");
        System.out.println("🔗 Relaciones foráneas manejadas con joins completos");
        System.out.println("✅ CRUD completo para todas las entidades");
        System.out.println("🚀 Registro unificado para formulario único del frontend");
        System.out.println("==========================================================");
    }

    /**
     * Inicializar todos los módulos
     */
    public static class ModuleInitializer {
        private final RolRoutes rolRoutes;
        private final UsuarioRoutes usuarioRoutes;
        private final OrigenAltaRoutes origenAltaRoutes;
        private final CausaBajaRoutes causaBajaRoutes;
        private final EspecieRoutes especieRoutes;
        private final EspecimenRoutes especimenRoutes;
        private final RegistroUnificadoRoutes registroUnificadoRoutes;
        private final RegistroAltaRoutes registroAltaRoutes;
        private final RegistroBajaRoutes registroBajaRoutes;

        public ModuleInitializer() {
            this.rolRoutes = initRoles();
            this.usuarioRoutes = initUsuarios();
            this.origenAltaRoutes = initOrigenAlta();
            this.causaBajaRoutes = initCausaBaja();
            this.especieRoutes = initSpecies();
            this.especimenRoutes = initSpecimens();
            this.registroUnificadoRoutes = initRegistroUnificado();
            this.registroAltaRoutes = initRegistroAlta();
            this.registroBajaRoutes = initRegistroBaja();
        }

        // Getters para todas las rutas
        public RolRoutes getRolRoutes() { return rolRoutes; }
        public UsuarioRoutes getUsuarioRoutes() { return usuarioRoutes; }
        public OrigenAltaRoutes getOrigenAltaRoutes() { return origenAltaRoutes; }
        public CausaBajaRoutes getCausaBajaRoutes() { return causaBajaRoutes; }
        public EspecieRoutes getEspecieRoutes() { return especieRoutes; }
        public EspecimenRoutes getEspecimenRoutes() { return especimenRoutes; }
        public RegistroUnificadoRoutes getRegistroUnificadoRoutes() { return registroUnificadoRoutes; }
        public RegistroAltaRoutes getRegistroAltaRoutes() { return registroAltaRoutes; }
        public RegistroBajaRoutes getRegistroBajaRoutes() { return registroBajaRoutes; }
    }

    /**
     * Validar integridad de dependencias
     */
    public static boolean validateDependencies() {
        try {
            initRoles();
            initUsuarios();
            initOrigenAlta();
            initCausaBaja();
            initSpecies();
            initSpecimens();
            initRegistroUnificado();
            initRegistroAlta();
            initRegistroBaja();

            System.out.println("✅ Todas las dependencias validadas exitosamente");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en la validación de dependencias: " + e.getMessage());
            return false;
        }
    }
}