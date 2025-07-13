package com.hugin_munin.di;

import com.hugin_munin.controller.*;
import com.hugin_munin.repository.*;
import com.hugin_munin.routes.*;
import com.hugin_munin.service.*;

/**
 * Contenedor de inyección de dependencias ACTUALIZADO
 * Maneja la inicialización de todos los módulos con sus dependencias
 * INCLUYE: TipoReporte, Reporte y ReporteTraslado
 */
public class AppModule {

    /**
     * Inicializar módulo de autenticación
     */
    public static AuthRoutes initAuth() {
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        AuthService authService = new AuthService(usuarioRepository);
        AuthController authController = new AuthController(authService);

        return new AuthRoutes(authController);
    }

    /**
     * Obtener instancia del servicio de autenticación
     * Para uso en middleware
     */
    public static AuthService getAuthService() {
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        return new AuthService(usuarioRepository);
    }

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
     * Inicializar módulo de tipos de reporte (NUEVO - Catálogo)
     */
    public static TipoReporteRoutes initTipoReporte() {
        TipoReporteRepository tipoReporteRepository = new TipoReporteRepository();
        TipoReporteService tipoReporteService = new TipoReporteService(tipoReporteRepository);
        TipoReporteController tipoReporteController = new TipoReporteController(tipoReporteService);

        return new TipoReporteRoutes(tipoReporteController);
    }

    /**
     * Inicializar módulo de reportes (NUEVO - Clase padre)
     */
    public static ReporteRoutes initReporte() {
        TipoReporteRepository tipoReporteRepository = new TipoReporteRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        ReporteRepository reporteRepository = new ReporteRepository();

        ReporteService reporteService = new ReporteService(
                reporteRepository,
                tipoReporteRepository,
                especimenRepository,
                usuarioRepository
        );
        ReporteController reporteController = new ReporteController(reporteService);

        return new ReporteRoutes(reporteController);
    }

    /**
     * Inicializar módulo de reportes de traslado (NUEVO - Clase hija)
     */
    public static ReporteTrasladoRoutes initReporteTraslado() {
        TipoReporteRepository tipoReporteRepository = new TipoReporteRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        ReporteTrasladoRepository reporteTrasladoRepository = new ReporteTrasladoRepository();

        ReporteTrasladoService reporteTrasladoService = new ReporteTrasladoService(
                reporteTrasladoRepository,
                tipoReporteRepository,
                especimenRepository,
                usuarioRepository
        );
        ReporteTrasladoController reporteTrasladoController = new ReporteTrasladoController(reporteTrasladoService);

        return new ReporteTrasladoRoutes(reporteTrasladoController);
    }

    /**
     * Inicializar módulo de registro unificado ACTUALIZADO
     * Ahora incluye el servicio de ReporteTraslado para creación completa
     */
    public static RegistroUnificadoRoutes initRegistroUnificado() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecimenRepository especimenRepository = new EspecimenRepository();
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        OrigenAltaRepository origenAltaRepository = new OrigenAltaRepository();

        // NUEVAS dependencias para ReporteTraslado
        TipoReporteRepository tipoReporteRepository = new TipoReporteRepository();
        ReporteTrasladoRepository reporteTrasladoRepository = new ReporteTrasladoRepository();

        EspecimenService especimenService = new EspecimenService(
                especimenRepository,
                especieRepository,
                registroAltaRepository,
                usuarioRepository,
                origenAltaRepository
        );

        // NUEVO servicio para reportes de traslado
        ReporteTrasladoService reporteTrasladoService = new ReporteTrasladoService(
                reporteTrasladoRepository,
                tipoReporteRepository,
                especimenRepository,
                usuarioRepository
        );

        // Controlador actualizado con ambos servicios
        RegistroUnificadoController unificadoController = new RegistroUnificadoController(
                especimenService,
                reporteTrasladoService
        );

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
     * Información completa del módulo ACTUALIZADA
     */
    public static void printModuleInfo() {
        System.out.println("=== HUGIN MUNIN API - DEPENDENCY INJECTION COMPLETO + REPORTES ===");
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

        System.out.println("✅ Módulo TipoReporte (NUEVO - Catálogo):");
        System.out.println("   TipoReporteRepository -> TipoReporteService -> TipoReporteController");

        System.out.println("✅ Módulo Reporte (NUEVO - Clase padre):");
        System.out.println("   [ReporteRepository, TipoReporteRepository, EspecimenRepository,");
        System.out.println("    UsuarioRepository] -> ReporteService -> ReporteController");

        System.out.println("✅ Módulo ReporteTraslado (NUEVO - Clase hija):");
        System.out.println("   [ReporteTrasladoRepository, TipoReporteRepository, EspecimenRepository,");
        System.out.println("    UsuarioRepository] -> ReporteTrasladoService -> ReporteTrasladoController");

        System.out.println("✅ Módulo RegistroUnificado (ACTUALIZADO):");
        System.out.println("   [EspecieRepository, EspecimenRepository, RegistroAltaRepository,");
        System.out.println("    UsuarioRepository, OrigenAltaRepository, TipoReporteRepository,");
        System.out.println("    ReporteTrasladoRepository] -> [EspecimenService, ReporteTrasladoService]");
        System.out.println("    -> RegistroUnificadoController (AHORA CON REPORTES DE TRASLADO)");

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
        System.out.println("📊 NUEVOS: Sistema completo de reportes con herencia");
        System.out.println("   - TipoReporte: Catálogo CRUD básico");
        System.out.println("   - Reporte: Clase padre con búsquedas por todos los atributos");
        System.out.println("   - ReporteTraslado: Clase hija con atributos específicos de traslado");
        System.out.println("🔄 PRÓXIMO: Integración de ReporteTraslado al RegistroUnificado");
        System.out.println("==========================================================");
    }

    /**
     * Inicializar todos los módulos ACTUALIZADO
     */
    public static class ModuleInitializer {
        private final RolRoutes rolRoutes;
        private final UsuarioRoutes usuarioRoutes;
        private final OrigenAltaRoutes origenAltaRoutes;
        private final CausaBajaRoutes causaBajaRoutes;
        private final EspecieRoutes especieRoutes;
        private final EspecimenRoutes especimenRoutes;
        private final TipoReporteRoutes tipoReporteRoutes;
        private final ReporteRoutes reporteRoutes;
        private final ReporteTrasladoRoutes reporteTrasladoRoutes;
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
            this.tipoReporteRoutes = initTipoReporte();
            this.reporteRoutes = initReporte();
            this.reporteTrasladoRoutes = initReporteTraslado();
            this.registroUnificadoRoutes = initRegistroUnificado();
            this.registroAltaRoutes = initRegistroAlta();
            this.registroBajaRoutes = initRegistroBaja();
        }

        // Getters para todas las rutas ACTUALIZADO
        public RolRoutes getRolRoutes() { return rolRoutes; }
        public UsuarioRoutes getUsuarioRoutes() { return usuarioRoutes; }
        public OrigenAltaRoutes getOrigenAltaRoutes() { return origenAltaRoutes; }
        public CausaBajaRoutes getCausaBajaRoutes() { return causaBajaRoutes; }
        public EspecieRoutes getEspecieRoutes() { return especieRoutes; }
        public EspecimenRoutes getEspecimenRoutes() { return especimenRoutes; }
        public TipoReporteRoutes getTipoReporteRoutes() { return tipoReporteRoutes; }
        public ReporteRoutes getReporteRoutes() { return reporteRoutes; }
        public ReporteTrasladoRoutes getReporteTrasladoRoutes() { return reporteTrasladoRoutes; }
        public RegistroUnificadoRoutes getRegistroUnificadoRoutes() { return registroUnificadoRoutes; }
        public RegistroAltaRoutes getRegistroAltaRoutes() { return registroAltaRoutes; }
        public RegistroBajaRoutes getRegistroBajaRoutes() { return registroBajaRoutes; }
    }

    /**
     * Validar integridad de dependencias ACTUALIZADO
     */
    public static boolean validateDependencies() {
        try {
            initRoles();
            initUsuarios();
            initOrigenAlta();
            initCausaBaja();
            initSpecies();
            initSpecimens();
            initTipoReporte();
            initReporte();
            initReporteTraslado();
            initRegistroUnificado();
            initRegistroAlta();
            initRegistroBaja();

            System.out.println("✅ Todas las dependencias validadas exitosamente (incluyendo módulos de reportes)");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error en la validación de dependencias: " + e.getMessage());
            return false;
        }
    }
}