package com.hugin_munin.di;

import com.hugin_munin.controller.RegistroAltaController;
import com.hugin_munin.repository.EspecieRepository;
import com.hugin_munin.repository.RegistroAltaRepository;
import com.hugin_munin.routes.RegistroAltaRoutes;
import com.hugin_munin.service.EspecieService;
import com.hugin_munin.controller.EspecieController;
import com.hugin_munin.routes.EspecieRoutes;

import com.hugin_munin.repository.EspecimenRepository;
import com.hugin_munin.service.EspecimenService;
import com.hugin_munin.controller.EspecimenController;
import com.hugin_munin.routes.EspecimenRoutes;

import com.hugin_munin.repository.RegistroAltaRepository;
import com.hugin_munin.service.RegistroAltaService;
import com.hugin_munin.controller.RegistroAltaController;
import com.hugin_munin.routes.RegistroAltaRoutes;

public class AppModule {
    public static EspecieRoutes initSpecies() {
        EspecieRepository especieRepository = new EspecieRepository();
        EspecieService especieService = new EspecieService(especieRepository);
        EspecieController especieController = new EspecieController(especieService);

        return new EspecieRoutes(especieController);
    }

    public static EspecimenRoutes initSpecimens() {
        EspecimenRepository especimenRepository = new EspecimenRepository();
        EspecimenService especimenService = new EspecimenService(especimenRepository);
        EspecimenController especimenController = new EspecimenController(especimenService);

        return new EspecimenRoutes(especimenController);
    }

    public static RegistroAltaRoutes initRegistroAlta() {
        RegistroAltaRepository registroAltaRepository = new RegistroAltaRepository();
        RegistroAltaService registroAltaService = new RegistroAltaService(registroAltaRepository);
        RegistroAltaController registroAltaController = new RegistroAltaController(registroAltaService);

        return new RegistroAltaRoutes(registroAltaController);
    }
}
