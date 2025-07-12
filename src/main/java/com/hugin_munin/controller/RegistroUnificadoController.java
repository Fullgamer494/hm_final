package com.hugin_munin.controller;

import com.hugin_munin.service.EspecimenService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.Map;

/**
 * Controlador para manejar el registro unificado desde el frontend
 * Este controlador maneja la creación coordinada de especie, especimen y registro de alta
 */
public class RegistroUnificadoController {

    private final EspecimenService especimenService;

    public RegistroUnificadoController(EspecimenService especimenService) {
        this.especimenService = especimenService;
    }

    /**
     * POST /hm/registro-unificado - Crear especie, especimen y registro de alta en una sola operación
     *
     * Estructura del JSON esperado:
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
     *     "fecha_ingreso": "2024-01-15" // Opcional, si no se proporciona usa fecha actual
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

            // Procesar el registro unificado
            Map<String, Object> result = especimenService.createSpecimenWithRegistration(requestData);

            // Responder con éxito
            ctx.status(HttpStatus.CREATED)
                    .json(Map.of(
                            "success", true,
                            "message", "Registro unificado creado exitosamente",
                            "data", result
                    ));

        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST)
                    .json(createErrorResponse("Datos inválidos", e.getMessage()));
        } catch (Exception e) {
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
     * POST /hm/registro-unificado/validar - Validar datos antes de crear el registro
     *
     * Permite validar los datos sin crear el registro, útil para validación en tiempo real
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
     * GET /hm/registro-unificado/formulario-data - Obtener datos necesarios para el formulario
     *
     * Retorna listas de orígenes de alta, usuarios responsables, y otras opciones
     * necesarias para completar el formulario de registro unificado
     */
    public void getFormData(Context ctx) {
        try {
            // Este método requeriría inyectar otros servicios
            // Por ahora retornamos una estructura básica

            Map<String, Object> formData = Map.of(
                    "message", "Para implementar completamente este endpoint se requieren los servicios de OrigenAlta y Usuario",
                    "estructura_esperada", Map.of(
                            "origenes_alta", "Lista de orígenes disponibles",
                            "usuarios_responsables", "Lista de usuarios que pueden ser responsables",
                            "validation_rules", Map.of(
                                    "num_inventario", "Debe ser único, formato alfanumérico",
                                    "nombre_especimen", "Mínimo 2 caracteres, solo letras y espacios",
                                    "genero", "Mínimo 2 caracteres, solo letras",
                                    "especie", "Mínimo 2 caracteres, solo letras",
                                    "procedencia", "Máximo 200 caracteres",
                                    "observacion", "Máximo 500 caracteres, requerida"
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
     * GET /hm/registro-unificado/ejemplo - Obtener ejemplo de estructura JSON
     */
    public void getExampleStructure(Context ctx) {
        Map<String, Object> ejemplo = Map.of(
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
                        "fecha_ingreso", "2024-01-15 (opcional, formato YYYY-MM-DD)"
                )
        );

        ctx.json(Map.of(
                "success", true,
                "message", "Ejemplo de estructura JSON para registro unificado",
                "ejemplo", ejemplo,
                "notas", Map.of(
                        "especie", "Si ya existe la combinación género+especie, se usará la existente",
                        "especimen", "El número de inventario debe ser único",
                        "registro_alta", "La fecha es opcional, si no se proporciona se usa la fecha actual",
                        "proceso", "Se crean en orden: especie (si no existe) -> especimen -> registro de alta"
                )
        ));
    }

    // MÉTODOS PRIVADOS DE UTILIDAD

    /**
     * Validar datos de registro sin crear registros en la base de datos
     */
    private Map<String, Object> validateRegistrationData(Map<String, Object> requestData) {
        Map<String, Object> result = Map.of(
                "valid", true,
                "warnings", new java.util.ArrayList<String>(),
                "checks", Map.of(
                        "especie_data", "Estructura válida",
                        "especimen_data", "Estructura válida",
                        "registro_data", "Estructura válida"
                )
        );

        // Aquí irían las validaciones específicas sin acceso a BD
        // Por ahora solo validamos estructura básica

        if (!requestData.containsKey("especie")) {
            throw new IllegalArgumentException("Faltan datos de especie");
        }

        if (!requestData.containsKey("especimen")) {
            throw new IllegalArgumentException("Faltan datos de especimen");
        }

        if (!requestData.containsKey("registro_alta")) {
            throw new IllegalArgumentException("Faltan datos de registro de alta");
        }

        return result;
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
                "help", "Consulte /hm/registro-unificado/ejemplo para ver la estructura correcta"
        );
    }
}