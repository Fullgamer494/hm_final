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
     *
     * Estructura del JSON esperado ACTUALIZADA:
     * {
     *   "especie": {
     *     "genero": "Panthera",
     *     "especie": "leo"
     *   },
     *   "especimen": {
     *     "num_inventario": "PL001",
     *     "nombre_especimen": "León Simba"
     *   },
     *   "registro_alta": {
     *     "id_origen_alta": 1,
     *     "id_responsable": 1,
     *     "procedencia": "Zoológico de la Ciudad",
     *     "observacion": "Especimen adulto en buen estado de salud",
     *     "fecha_ingreso": "2024-01-15"
     *   },
     *   "reporte_traslado": {  // NUEVO - OPCIONAL
     *     "id_tipo_reporte": 1,
     *     "asunto": "Traslado de especimen a nueva ubicación",
     *     "contenido": "Traslado programado por mejoras en el habitat",
     *     "area_origen": "Zona A",
     *     "area_destino": "Zona B",
     *     "ubicacion_origen": "Jaula 15",
     *     "ubicacion_destino": "Jaula 23",
     *     "motivo": "Renovación de instalaciones",
     *     "fecha_reporte": "2024-01-15"  // Opcional
     *   }
     * }
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

            // PROCESAMIENTO ESPECIAL DE FECHAS para reporte_traslado (NUEVO)
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
            Map<String, Object> registroResult = especimenService.createSpecimenWithRegistration(requestData);

            // 2. Si se proporcionó reporte_traslado, crearlo (NUEVO)
            Map<String, Object> reporteResult = null;
            if (incluirReporte) {
                reporteResult = createReporteTraslado(reporteData, registroResult);
            }

            // Preparar respuesta completa ACTUALIZADA
            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", incluirReporte ?
                            "Registro unificado creado exitosamente con reporte de traslado" :
                            "Registro unificado creado exitosamente",
                    "registro_data", registroResult,
                    "reporte_traslado", reporteResult != null ? reporteResult : "No se creó reporte de traslado",
                    "components_created", incluirReporte ?
                            Map.of("especie", "✅", "especimen", "✅", "registro_alta", "✅", "reporte_traslado", "✅") :
                            Map.of("especie", "✅", "especimen", "✅", "registro_alta", "✅", "reporte_traslado", "❌")
            );

            // Responder con éxito
            ctx.status(HttpStatus.CREATED).json(response);

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

            ctx.json(Map.of(
                    "success", true,
                    "message", "Validación completada",
                    "validation_result", validationResult
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(Map.of(
                            "success", false,
                            "validation_result", Map.of(
                                    "valid", false,
                                    "errors", e.getMessage()
                            )
                    ));
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
            Map<String, Object> formData = Map.of(
                    "message", "Para implementar completamente este endpoint se requieren los servicios adicionales",
                    "estructura_esperada", Map.of(
                            "origenes_alta", "Lista de orígenes disponibles",
                            "usuarios_responsables", "Lista de usuarios que pueden ser responsables",
                            "tipos_reporte", "Lista de tipos de reporte disponibles (NUEVO)",
                            "validation_rules", Map.of(
                                    "num_inventario", "Debe ser único, formato alfanumérico",
                                    "nombre_especimen", "Mínimo 2 caracteres, solo letras y espacios",
                                    "genero", "Mínimo 2 caracteres, solo letras",
                                    "especie", "Mínimo 2 caracteres, solo letras",
                                    "procedencia", "Máximo 200 caracteres",
                                    "observacion", "Máximo 500 caracteres, requerida",
                                    "fecha_ingreso", "Formato YYYY-MM-DD o vacío para usar fecha actual",
                                    "reporte_traslado", "OPCIONAL - Todos los campos de traslado son requeridos si se incluye",
                                    "area_origen", "Mínimo 2 caracteres, máximo 100",
                                    "area_destino", "Mínimo 2 caracteres, máximo 100, debe ser diferente a origen",
                                    "ubicacion_origen", "Mínimo 2 caracteres, máximo 100",
                                    "ubicacion_destino", "Mínimo 2 caracteres, máximo 100, debe ser diferente a origen",
                                    "motivo", "Mínimo 5 caracteres, máximo 500",
                                    "asunto_reporte", "Mínimo 5 caracteres, máximo 200",
                                    "contenido_reporte", "Mínimo 10 caracteres, máximo 1000"
                            )
                    )
            );

            ctx.json(Map.of(
                    "success", true,
                    "data", formData
            ));

        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .json(createErrorResponse("Error al obtener datos del formulario", e.getMessage()));
        }
    }

    /**
     * GET /hm/registro-unificado/ejemplo - Obtener ejemplo de estructura JSON ACTUALIZADA
     */
    public void getExampleStructure(Context ctx) {
        Map<String, Object> ejemploCompleto = Map.of(
                "especie", Map.of(
                        "genero", "Panthera",
                        "especie", "leo"
                ),
                "especimen", Map.of(
                        "num_inventario", "PL001",
                        "nombre_especimen", "León Simba"
                ),
                "registro_alta", Map.of(
                        "id_origen_alta", 1,
                        "id_responsable", 1,
                        "procedencia", "Zoológico de la Ciudad",
                        "observacion", "Especimen adulto en buen estado de salud",
                        "fecha_ingreso", "2024-01-15"
                ),
                "reporte_traslado", Map.of(
                        "id_tipo_reporte", 1,
                        "asunto", "Traslado de especimen a nueva ubicación",
                        "contenido", "Traslado programado por mejoras en el habitat original",
                        "area_origen", "Zona A",
                        "area_destino", "Zona B",
                        "ubicacion_origen", "Jaula 15",
                        "ubicacion_destino", "Jaula 23",
                        "motivo", "Renovación de instalaciones en Zona A",
                        "fecha_reporte", "2024-01-15"
                )
        );

        Map<String, Object> ejemploSinReporte = Map.of(
                "especie", Map.of(
                        "genero", "Felis",
                        "especie", "catus"
                ),
                "especimen", Map.of(
                        "num_inventario", "FC002",
                        "nombre_especimen", "Gato Misifú"
                ),
                "registro_alta", Map.of(
                        "id_origen_alta", 2,
                        "id_responsable", 1,
                        "procedencia", "Rescate urbano",
                        "observacion", "Especimen joven encontrado en la calle"
                        // Sin fecha_ingreso - usará fecha actual automáticamente
                        // Sin reporte_traslado - es opcional
                )
        );

        ctx.json(Map.of(
                "success", true,
                "message", "Ejemplos de estructura JSON para registro unificado con reportes de traslado",
                "ejemplo_completo_con_reporte", ejemploCompleto,
                "ejemplo_solo_registro", ejemploSinReporte,
                "notas", Map.of(
                        "especie", "Si ya existe la combinación género+especie, se usará la existente",
                        "especimen", "El número de inventario debe ser único",
                        "registro_alta", "La fecha es opcional, si no se proporciona se usa la fecha actual",
                        "reporte_traslado", "COMPLETAMENTE OPCIONAL - Solo se crea si se proporciona",
                        "validacion_traslado", "Si se incluye reporte_traslado, TODOS sus campos son obligatorios",
                        "formato_fecha", "YYYY-MM-DD (ejemplo: 2024-01-15)",
                        "proceso", "Se crean en orden: especie -> especimen -> registro_alta -> reporte_traslado (si aplica)",
                        "responsable_reporte", "Se usa el mismo responsable del registro_alta para el reporte"
                )
        ));
    }

    // MÉTODOS PRIVADOS DE UTILIDAD

    /**
     * Crear reporte de traslado con los datos del especimen ya creado
     */
    private Map<String, Object> createReporteTraslado(Map<String, Object> reporteData,
                                                      Map<String, Object> registroResult) throws Exception {

        // Obtener el especimen creado del resultado del registro
        Map<String, Object> especimenData = (Map<String, Object>) registroResult.get("especimen");
        Integer idEspecimen = (Integer) especimenData.get("id_especimen");

        // Obtener el responsable del registro de alta
        Map<String, Object> registroAltaData = (Map<String, Object>) registroResult.get("registro_alta");
        Integer idResponsable = (Integer) registroAltaData.get("id_responsable");

        // Crear objeto ReporteTraslado
        ReporteTraslado reporteTraslado = new ReporteTraslado();

        // Datos del reporte padre
        reporteTraslado.setId_tipo_reporte((Integer) reporteData.get("id_tipo_reporte"));
        reporteTraslado.setId_especimen(idEspecimen);
        reporteTraslado.setId_responsable(idResponsable); // Mismo responsable que el registro
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

        return Map.of(
                "id_reporte", reporteCreado.getId_reporte(),
                "asunto", reporteCreado.getAsunto(),
                "traslado_info", reporteCreado.getTrasladoInfo(),
                "fecha_reporte", reporteCreado.getFecha_reporte(),
                "message", "Reporte de traslado creado exitosamente"
        );
    }

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
        Map<String, Object> result = Map.of(
                "valid", true,
                "warnings", new java.util.ArrayList<String>(),
                "checks", Map.of(
                        "especie_data", "Estructura válida",
                        "especimen_data", "Estructura válida",
                        "registro_data", "Estructura válida",
                        "reporte_traslado_data", "Validación completada"
                )
        );

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
        return Map.of(
                "success", false,
                "error", error,
                "details", details,
                "timestamp", System.currentTimeMillis(),
                "help", "Consulte /hm/registro-unificado/ejemplo para ver la estructura correcta con reportes de traslado"
        );
    }
}