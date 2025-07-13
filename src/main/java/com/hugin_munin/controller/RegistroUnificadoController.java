package com.hugin_munin.controller;

import com.hugin_munin.service.EspecimenService;
import com.hugin_munin.service.ReporteTrasladoService;
import com.hugin_munin.model.ReporteTraslado;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Controlador para manejar el registro unificado desde el frontend
 * ACTUALIZADO: Ahora incluye la creación de reportes de traslado
 * Este controlador maneja la creación coordinada de:
 * - especie, especimen y registro de alta (funcionalidad original)
 * - reporte de traslado (nueva funcionalidad)
 */
public class RegistroUnificadoController {

    private final EspecimenService especimenService;
    private final ReporteTrasladoService reporteTrasladoService;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public RegistroUnificadoController(EspecimenService especimenService,
                                       ReporteTrasladoService reporteTrasladoService) {
        this.especimenService = especimenService;
        this.reporteTrasladoService = reporteTrasladoService;
    }

    /**
     * POST /hm/registro-unificado - Crear especie, especimen, registro de alta y opcionalmente reporte de traslado
     */
    public void createUnifiedRegistration(Context ctx) {
        try {
            // Obtener datos del cuerpo de la solicitud
            Map<String, Object> requestData = ctx.bodyAsClass(Map.class);

            // Validar que el cuerpo de la solicitud no esté vacío
            if (requestData == null || requestData.isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos requeridos", "El cuerpo de la solicitud no puede estar vacío"));
                return;
            }

            // PROCESAMIENTO ESPECIAL DE FECHAS para registro_alta
            Map<String, Object> registroData = (Map<String, Object>) requestData.get("registro_alta");
            if (registroData != null && registroData.containsKey("fecha_ingreso")) {
                procesarFecha(registroData, "fecha_ingreso");
            } else {
                if (registroData != null) {
                    registroData.put("fecha_ingreso", new Date());
                }
            }

            // PROCESAMIENTO ESPECIAL DE FECHAS para reporte_traslado
            Map<String, Object> reporteData = (Map<String, Object>) requestData.get("reporte_traslado");
            boolean incluirReporte = reporteData != null && !reporteData.isEmpty();

            if (incluirReporte) {
                if (reporteData.containsKey("fecha_reporte")) {
                    procesarFecha(reporteData, "fecha_reporte");
                } else {
                    reporteData.put("fecha_reporte", new Date());
                }
            }

            // 1. Procesar el registro unificado original (especie + especimen + registro_alta)
            // CAMBIO: Manejar el resultado como objeto, no como Map
            Object registroResult = especimenService.createSpecimenWithRegistration(requestData);

            // Convertir el resultado a Map para mantener la estructura de respuesta
            Map<String, Object> registroResultMap = convertirResultadoAMap(registroResult, requestData);

            // 2. Si se proporcionó reporte_traslado, crearlo
            Map<String, Object> reporteResult = null;
            if (incluirReporte) {
                reporteResult = createReporteTraslado(reporteData, registroResultMap);
            }

            // Preparar respuesta completa
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", incluirReporte ?
                    "Registro unificado creado exitosamente con reporte de traslado" :
                    "Registro unificado creado exitosamente");
            response.put("registro_data", registroResultMap);
            response.put("reporte_traslado", reporteResult != null ? reporteResult : "No se creó reporte de traslado");

            // Components created
            Map<String, String> componentsCreated = new HashMap<>();
            componentsCreated.put("especie", "✅");
            componentsCreated.put("especimen", "✅");
            componentsCreated.put("registro_alta", "✅");
            componentsCreated.put("reporte_traslado", incluirReporte ? "✅" : "❌");
            response.put("components_created", componentsCreated);

            // Responder con éxito
            ctx.status(HttpStatus.CREATED).json(response);

        } catch (ClassCastException e) {
            System.err.println("❌ Error de casting: " + e.getMessage());
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error de procesamiento",
                            "Error interno al procesar la respuesta del servicio: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();

            // Determinar el tipo de error para dar una respuesta más específica
            if (e.getMessage().contains("Ya existe")) {
                ctx.status(HttpStatus.CONFLICT)
                        .json(createErrorResponse("Conflicto", e.getMessage()));
            } else if (e.getMessage().contains("no existe")) {
                ctx.status(HttpStatus.NOT_FOUND)
                        .json(createErrorResponse("Referencia no encontrada", e.getMessage()));
            } else {
                ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .json(createErrorResponse("Error interno del servidor", e.getMessage()));
            }
        }
    }

    /**
     * MÉTODO CORREGIDO: Convertir resultado del servicio a Map con datos del request original
     */
    private Map<String, Object> convertirResultadoAMap(Object resultado, Map<String, Object> requestData) {
        Map<String, Object> resultMap = new HashMap<>();

        try {
            // Si ya es un Map, devolverlo tal como está
            if (resultado instanceof Map) {
                return (Map<String, Object>) resultado;
            }

            // Si es un objeto Especimen, extraer la información necesaria
            if (resultado instanceof com.hugin_munin.model.Especimen) {
                com.hugin_munin.model.Especimen especimen = (com.hugin_munin.model.Especimen) resultado;

                // Crear estructura de respuesta similar a la esperada
                Map<String, Object> especimenData = new HashMap<>();
                especimenData.put("id_especimen", especimen.getId_especimen());
                especimenData.put("num_inventario", especimen.getNum_inventario());
                especimenData.put("nombre_especimen", especimen.getNombre_especimen());

                // Si el especimen tiene referencia a especie, incluirla
                if (especimen.getEspecie() != null) {
                    Map<String, Object> especieData = new HashMap<>();
                    especieData.put("id_especie", especimen.getEspecie().getId_especie());
                    especieData.put("genero", especimen.getEspecie().getGenero());
                    especieData.put("especie", especimen.getEspecie().getEspecie());
                    resultMap.put("especie", especieData);
                }

                resultMap.put("especimen", especimenData);

                // CORREGIDO: Crear datos de registro_alta usando el request original
                Map<String, Object> registroAltaData = new HashMap<>();
                Map<String, Object> originalRegistro = (Map<String, Object>) requestData.get("registro_alta");

                if (originalRegistro != null) {
                    // Usar los datos originales del request
                    registroAltaData.put("id_responsable", originalRegistro.get("id_responsable"));
                    registroAltaData.put("id_origen_alta", originalRegistro.get("id_origen_alta"));
                    registroAltaData.put("procedencia", originalRegistro.get("procedencia"));
                    registroAltaData.put("observacion", originalRegistro.get("observacion"));
                    registroAltaData.put("fecha_ingreso", originalRegistro.get("fecha_ingreso"));
                }

                resultMap.put("registro_alta", registroAltaData);

                return resultMap;
            }

            // Si es otro tipo de objeto, intentar una conversión genérica
            System.err.println("⚠️ Tipo de resultado no esperado: " + resultado.getClass().getName());
            resultMap.put("raw_result", resultado.toString());
            resultMap.put("type", resultado.getClass().getSimpleName());

            return resultMap;

        } catch (Exception e) {
            System.err.println("❌ Error al convertir resultado: " + e.getMessage());
            resultMap.put("error", "No se pudo convertir el resultado");
            resultMap.put("original_type", resultado != null ? resultado.getClass().getName() : "null");
            return resultMap;
        }
    }

    /**
     * MÉTODO ÚNICO Y CORREGIDO: Crear reporte de traslado con manejo mejorado de datos
     */
    private Map<String, Object> createReporteTraslado(Map<String, Object> reporteData,
                                                      Map<String, Object> registroResult) throws Exception {

        // Obtener el especimen creado del resultado del registro
        Integer idEspecimen = null;
        Integer idResponsable = null;

        try {
            // Intentar obtener datos del especimen
            Map<String, Object> especimenData = (Map<String, Object>) registroResult.get("especimen");
            if (especimenData != null) {
                idEspecimen = (Integer) especimenData.get("id_especimen");
            }

            // Intentar obtener el responsable del registro de alta
            Map<String, Object> registroAltaData = (Map<String, Object>) registroResult.get("registro_alta");
            if (registroAltaData != null) {
                idResponsable = (Integer) registroAltaData.get("id_responsable");
            }

            // Si no se pueden obtener los IDs de la estructura esperada, usar valores por defecto
            if (idEspecimen == null) {
                System.err.println("⚠️ No se pudo obtener id_especimen");
                throw new IllegalStateException("No se pudo obtener el ID del especimen creado");
            }

            if (idResponsable == null) {
                System.err.println("⚠️ No se pudo obtener id_responsable de registro_alta");
                throw new IllegalStateException("No se pudo obtener el ID del responsable del registro");
            }

        } catch (ClassCastException e) {
            System.err.println("❌ Error al extraer datos para reporte: " + e.getMessage());
            throw new IllegalStateException("Error al procesar datos del registro para crear reporte de traslado");
        }

        // Crear objeto ReporteTraslado
        ReporteTraslado reporteTraslado = new ReporteTraslado();

        // Datos del reporte padre
        reporteTraslado.setId_tipo_reporte((Integer) reporteData.get("id_tipo_reporte"));
        reporteTraslado.setId_especimen(idEspecimen);
        reporteTraslado.setId_responsable(idResponsable);
        reporteTraslado.setAsunto((String) reporteData.get("asunto"));
        reporteTraslado.setContenido((String) reporteData.get("contenido"));
        reporteTraslado.setFecha_reporte((Date) reporteData.get("fecha_reporte"));
        reporteTraslado.setActivo(true);

        // Datos específicos de traslado
        reporteTraslado.setArea_origen((String) reporteData.get("area_origen"));
        reporteTraslado.setArea_destino((String) reporteData.get("area_destino"));
        reporteTraslado.setUbicacion_origen((String) reporteData.get("ubicacion_origen"));
        reporteTraslado.setUbicacion_destino((String) reporteData.get("ubicacion_destino"));
        reporteTraslado.setMotivo((String) reporteData.get("motivo"));

        // Crear el reporte de traslado
        ReporteTraslado reporteCreado = reporteTrasladoService.createReporteTraslado(reporteTraslado);

        Map<String, Object> result = new HashMap<>();
        result.put("id_reporte", reporteCreado.getId_reporte());
        result.put("asunto", reporteCreado.getAsunto());
        result.put("traslado_info", reporteCreado.getTrasladoInfo());
        result.put("fecha_reporte", reporteCreado.getFecha_reporte());
        result.put("message", "Reporte de traslado creado exitosamente");

        return result;
    }

    /**
     * POST /hm/registro-unificado/validar - Validar datos antes de crear el registro ACTUALIZADA
     */
    public void validateUnifiedRegistration(Context ctx) {
        try {
            Map<String, Object> requestData = ctx.bodyAsClass(Map.class);

            if (requestData == null || requestData.isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST)
                        .json(createErrorResponse("Datos requeridos", "El cuerpo de la solicitud no puede estar vacío"));
                return;
            }

            // Realizar validaciones sin crear registros
            Map<String, Object> validationResult = validateRegistrationData(requestData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Validación completada");
            response.put("validation_result", validationResult);

            ctx.json(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> validationError = new HashMap<>();
            validationError.put("valid", false);
            validationError.put("errors", e.getMessage());

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("validation_result", validationError);

            ctx.status(HttpStatus.BAD_REQUEST).json(response);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error en validación", e.getMessage()));
        }
    }

    /**
     * GET /hm/registro-unificado/formulario-data - Obtener datos necesarios para el formulario ACTUALIZADA
     */
    public void getFormData(Context ctx) {
        try {
            // Validation rules
            Map<String, String> validationRules = new HashMap<>();
            validationRules.put("num_inventario", "Debe ser único, formato alfanumérico");
            validationRules.put("nombre_especimen", "Mínimo 2 caracteres, solo letras y espacios");
            validationRules.put("genero", "Mínimo 2 caracteres, solo letras");
            validationRules.put("especie", "Mínimo 2 caracteres, solo letras");
            validationRules.put("procedencia", "Máximo 200 caracteres");
            validationRules.put("observacion", "Máximo 500 caracteres, requerida");
            validationRules.put("fecha_ingreso", "Formato YYYY-MM-DD o vacío para usar fecha actual");
            validationRules.put("reporte_traslado", "OPCIONAL - Todos los campos de traslado son requeridos si se incluye");
            validationRules.put("area_origen", "Mínimo 2 caracteres, máximo 100");
            validationRules.put("area_destino", "Mínimo 2 caracteres, máximo 100, debe ser diferente a origen");
            validationRules.put("ubicacion_origen", "Mínimo 2 caracteres, máximo 100");
            validationRules.put("ubicacion_destino", "Mínimo 2 caracteres, máximo 100, debe ser diferente a origen");
            validationRules.put("motivo", "Mínimo 5 caracteres, máximo 500");
            validationRules.put("asunto_reporte", "Mínimo 5 caracteres, máximo 200");
            validationRules.put("contenido_reporte", "Mínimo 10 caracteres, máximo 1000");

            // Estructura esperada
            Map<String, String> estructuraEsperada = new HashMap<>();
            estructuraEsperada.put("origenes_alta", "Lista de orígenes disponibles");
            estructuraEsperada.put("usuarios_responsables", "Lista de usuarios que pueden ser responsables");
            estructuraEsperada.put("tipos_reporte", "Lista de tipos de reporte disponibles (NUEVO)");
            estructuraEsperada.put("validation_rules", validationRules.toString());

            Map<String, Object> formData = new HashMap<>();
            formData.put("message", "Para implementar completamente este endpoint se requieren los servicios adicionales");
            formData.put("estructura_esperada", estructuraEsperada);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", formData);

            ctx.json(response);

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener datos del formulario", e.getMessage()));
        }
    }

    /**
     * GET /hm/registro-unificado/ejemplo - Obtener ejemplo de estructura JSON ACTUALIZADA
     */
    public void getExampleStructure(Context ctx) {
        // Ejemplo completo con reporte
        Map<String, Object> especie1 = new HashMap<>();
        especie1.put("genero", "Panthera");
        especie1.put("especie", "leo");

        Map<String, Object> especimen1 = new HashMap<>();
        especimen1.put("num_inventario", "PL001");
        especimen1.put("nombre_especimen", "León Simba");

        Map<String, Object> registroAlta1 = new HashMap<>();
        registroAlta1.put("id_origen_alta", 1);
        registroAlta1.put("id_responsable", 1);
        registroAlta1.put("procedencia", "Zoológico de la Ciudad");
        registroAlta1.put("observacion", "Especimen adulto en buen estado de salud");
        registroAlta1.put("fecha_ingreso", "2024-01-15");

        Map<String, Object> reporteTraslado1 = new HashMap<>();
        reporteTraslado1.put("id_tipo_reporte", 1);
        reporteTraslado1.put("asunto", "Traslado de especimen a nueva ubicación");
        reporteTraslado1.put("contenido", "Traslado programado por mejoras en el habitat original");
        reporteTraslado1.put("area_origen", "Zona A");
        reporteTraslado1.put("area_destino", "Zona B");
        reporteTraslado1.put("ubicacion_origen", "Jaula 15");
        reporteTraslado1.put("ubicacion_destino", "Jaula 23");
        reporteTraslado1.put("motivo", "Renovación de instalaciones en Zona A");
        reporteTraslado1.put("fecha_reporte", "2024-01-15");

        Map<String, Object> ejemploCompleto = new HashMap<>();
        ejemploCompleto.put("especie", especie1);
        ejemploCompleto.put("especimen", especimen1);
        ejemploCompleto.put("registro_alta", registroAlta1);
        ejemploCompleto.put("reporte_traslado", reporteTraslado1);

        // Ejemplo sin reporte
        Map<String, Object> especie2 = new HashMap<>();
        especie2.put("genero", "Felis");
        especie2.put("especie", "catus");

        Map<String, Object> especimen2 = new HashMap<>();
        especimen2.put("num_inventario", "FC002");
        especimen2.put("nombre_especimen", "Gato Misifú");

        Map<String, Object> registroAlta2 = new HashMap<>();
        registroAlta2.put("id_origen_alta", 2);
        registroAlta2.put("id_responsable", 1);
        registroAlta2.put("procedencia", "Rescate urbano");
        registroAlta2.put("observacion", "Especimen joven encontrado en la calle");

        Map<String, Object> ejemploSinReporte = new HashMap<>();
        ejemploSinReporte.put("especie", especie2);
        ejemploSinReporte.put("especimen", especimen2);
        ejemploSinReporte.put("registro_alta", registroAlta2);

        // Notas
        Map<String, String> notas = new HashMap<>();
        notas.put("especie", "Si ya existe la combinación género+especie, se usará la existente");
        notas.put("especimen", "El número de inventario debe ser único");
        notas.put("registro_alta", "La fecha es opcional, si no se proporciona se usa la fecha actual");
        notas.put("reporte_traslado", "COMPLETAMENTE OPCIONAL - Solo se crea si se proporciona");
        notas.put("validacion_traslado", "Si se incluye reporte_traslado, TODOS sus campos son obligatorios");
        notas.put("formato_fecha", "YYYY-MM-DD (ejemplo: 2024-01-15)");
        notas.put("proceso", "Se crean en orden: especie -> especimen -> registro_alta -> reporte_traslado (si aplica)");
        notas.put("responsable_reporte", "Se usa el mismo responsable del registro_alta para el reporte");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ejemplos de estructura JSON para registro unificado con reportes de traslado");
        response.put("ejemplo_completo_con_reporte", ejemploCompleto);
        response.put("ejemplo_solo_registro", ejemploSinReporte);
        response.put("notas", notas);

        ctx.json(response);
    }

    // MÉTODOS PRIVADOS DE UTILIDAD

    /**
     * Procesar fechas de manera uniforme
     */
    private void procesarFecha(Map<String, Object> data, String campoFecha) {
        if (data.containsKey(campoFecha)) {
            Object fechaObj = data.get(campoFecha);

            if (fechaObj instanceof Date) {
                // Si ya es un Date, usarlo directamente
                System.out.println("✅ Usando fecha Date existente para " + campoFecha + ": " + fechaObj);
            } else if (fechaObj instanceof String) {
                // Si es String, convertir
                String fechaStr = (String) fechaObj;
                if (!fechaStr.trim().isEmpty()) {
                    try {
                        Date fecha = DATE_FORMAT.parse(fechaStr);
                        data.put(campoFecha, fecha);
                        System.out.println("✅ Fecha convertida de String para " + campoFecha + ": " + fechaStr + " -> " + fecha);
                    } catch (ParseException e) {
                        System.err.println("❌ Error al convertir fecha String para " + campoFecha + ": " + fechaStr);
                        data.put(campoFecha, new Date()); // Usar fecha actual como fallback
                    }
                } else {
                    data.put(campoFecha, new Date());
                    System.out.println("✅ Usando fecha actual por String vacío para " + campoFecha);
                }
            } else if (fechaObj == null) {
                data.put(campoFecha, new Date());
                System.out.println("✅ Usando fecha actual por valor null para " + campoFecha);
            } else {
                System.err.println("⚠️ Tipo de fecha desconocido para " + campoFecha + ": " + fechaObj.getClass());
                data.put(campoFecha, new Date());
            }
        } else {
            // Si no hay campo de fecha, usar fecha actual
            data.put(campoFecha, new Date());
            System.out.println("✅ Usando fecha actual por ausencia de campo " + campoFecha);
        }
    }

    /**
     * Validar datos de solicitud unificada ACTUALIZADA
     */
    private Map<String, Object> validateRegistrationData(Map<String, Object> requestData) {
        ArrayList<String> warnings = new ArrayList<>();

        Map<String, String> checks = new HashMap<>();
        checks.put("especie_data", "Estructura válida");
        checks.put("especimen_data", "Estructura válida");
        checks.put("registro_data", "Estructura válida");
        checks.put("reporte_traslado_data", "Validación completada");

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("warnings", warnings);
        result.put("checks", checks);

        // Validaciones básicas requeridas
        if (!requestData.containsKey("especie")) {
            throw new IllegalArgumentException("Faltan datos de especie");
        }

        if (!requestData.containsKey("especimen")) {
            throw new IllegalArgumentException("Faltan datos de especimen");
        }

        if (!requestData.containsKey("registro_alta")) {
            throw new IllegalArgumentException("Faltan datos de registro de alta");
        }

        // Validar formato de fecha en registro_alta si está presente
        Map<String, Object> registroData = (Map<String, Object>) requestData.get("registro_alta");
        validateDateFormat(registroData, "fecha_ingreso");

        // NUEVA VALIDACIÓN - Reporte de traslado (opcional)
        if (requestData.containsKey("reporte_traslado")) {
            Map<String, Object> reporteData = (Map<String, Object>) requestData.get("reporte_traslado");
            if (reporteData != null && !reporteData.isEmpty()) {
                validateReporteTraslado(reporteData);
            }
        }

        return result;
    }

    /**
     * NUEVA - Validar datos de reporte de traslado
     */
    private void validateReporteTraslado(Map<String, Object> reporteData) {
        // Campos requeridos del reporte padre
        String[] camposRequeridos = {
                "id_tipo_reporte", "asunto", "contenido",
                "area_origen", "area_destino", "ubicacion_origen",
                "ubicacion_destino", "motivo"
        };

        for (String campo : camposRequeridos) {
            if (!reporteData.containsKey(campo) ||
                    reporteData.get(campo) == null ||
                    reporteData.get(campo).toString().trim().isEmpty()) {
                throw new IllegalArgumentException("Campo requerido para reporte de traslado: " + campo);
            }
        }

        // Validar que origen y destino sean diferentes
        String areaOrigen = reporteData.get("area_origen").toString().trim();
        String areaDestino = reporteData.get("area_destino").toString().trim();
        String ubicacionOrigen = reporteData.get("ubicacion_origen").toString().trim();
        String ubicacionDestino = reporteData.get("ubicacion_destino").toString().trim();

        if (areaOrigen.equals(areaDestino) && ubicacionOrigen.equals(ubicacionDestino)) {
            throw new IllegalArgumentException("El traslado debe ser a una ubicación diferente");
        }

        // Validar formato de fecha si está presente
        validateDateFormat(reporteData, "fecha_reporte");
    }

    /**
     * Validar formato de fecha
     */
    private void validateDateFormat(Map<String, Object> data, String campoFecha) {
        if (data.containsKey(campoFecha)) {
            Object fechaObj = data.get(campoFecha);
            if (fechaObj instanceof String && !((String) fechaObj).trim().isEmpty()) {
                try {
                    DATE_FORMAT.parse((String) fechaObj);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Formato de fecha inválido para " + campoFecha + ". Use YYYY-MM-DD");
                }
            }
        }
    }

    /**
     * Método auxiliar para crear respuestas de error consistentes
     */
    private Map<String, Object> createErrorResponse(String error, String details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("details", details);
        response.put("timestamp", System.currentTimeMillis());
        response.put("help", "Consulte /hm/registro-unificado/ejemplo para ver la estructura correcta con reportes de traslado");

        return response;
    }
}