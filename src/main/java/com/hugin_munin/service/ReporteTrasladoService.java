package com.hugin_munin.service;

import com.hugin_munin.model.ReporteTraslado;
import com.hugin_munin.repository.ReporteTrasladoRepository;
import com.hugin_munin.repository.TipoReporteRepository;
import com.hugin_munin.repository.EspecimenRepository;
import com.hugin_munin.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Date;

/**
 * Servicio para gestionar reportes de traslado
 * Contiene la lógica de negocio para reportes de traslado
 */
public class ReporteTrasladoService {

    private final ReporteTrasladoRepository reporteTrasladoRepository;
    private final TipoReporteRepository tipoReporteRepository;
    private final EspecimenRepository especimenRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteTrasladoService(ReporteTrasladoRepository reporteTrasladoRepository,
                                  TipoReporteRepository tipoReporteRepository,
                                  EspecimenRepository especimenRepository,
                                  UsuarioRepository usuarioRepository) {
        this.reporteTrasladoRepository = reporteTrasladoRepository;
        this.tipoReporteRepository = tipoReporteRepository;
        this.especimenRepository = especimenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * OBTENER todos los reportes de traslado
     */
    public List<ReporteTraslado> getAllReportesTraslado() throws SQLException {
        return reporteTrasladoRepository.findAll();
    }

    /**
     * OBTENER reporte de traslado por ID
     */
    public ReporteTraslado getReporteTrasladoById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<ReporteTraslado> reporte = reporteTrasladoRepository.findById(id);
        return reporte.orElseThrow(() ->
                new IllegalArgumentException("Reporte de traslado no encontrado con ID: " + id));
    }

    /**
     * BUSCAR reportes de traslado por área origen
     */
    public List<ReporteTraslado> getReportesByAreaOrigen(String areaOrigen) throws SQLException {
        if (areaOrigen == null || areaOrigen.trim().isEmpty()) {
            throw new IllegalArgumentException("El área origen no puede estar vacía");
        }

        return reporteTrasladoRepository.findByAreaOrigen(areaOrigen.trim());
    }

    /**
     * BUSCAR reportes de traslado por área destino
     */
    public List<ReporteTraslado> getReportesByAreaDestino(String areaDestino) throws SQLException {
        if (areaDestino == null || areaDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("El área destino no puede estar vacía");
        }

        return reporteTrasladoRepository.findByAreaDestino(areaDestino.trim());
    }

    /**
     * BUSCAR reportes de traslado por ubicación origen
     */
    public List<ReporteTraslado> getReportesByUbicacionOrigen(String ubicacionOrigen) throws SQLException {
        if (ubicacionOrigen == null || ubicacionOrigen.trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación origen no puede estar vacía");
        }

        return reporteTrasladoRepository.findByUbicacionOrigen(ubicacionOrigen.trim());
    }

    /**
     * BUSCAR reportes de traslado por ubicación destino
     */
    public List<ReporteTraslado> getReportesByUbicacionDestino(String ubicacionDestino) throws SQLException {
        if (ubicacionDestino == null || ubicacionDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("La ubicación destino no puede estar vacía");
        }

        return reporteTrasladoRepository.findByUbicacionDestino(ubicacionDestino.trim());
    }

    /**
     * BUSCAR reportes de traslado por motivo
     */
    public List<ReporteTraslado> searchReportesByMotivo(String motivo) throws SQLException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo no puede estar vacío");
        }

        return reporteTrasladoRepository.findByMotivoContaining(motivo.trim());
    }

    /**
     * BUSCAR reportes de traslado por especimen
     */
    public List<ReporteTraslado> getReportesByEspecimen(Integer idEspecimen) throws SQLException {
        if (idEspecimen == null || idEspecimen <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        return reporteTrasladoRepository.findByEspecimen(idEspecimen);
    }

    /**
     * BUSCAR reportes de traslado por responsable
     */
    public List<ReporteTraslado> getReportesByResponsable(Integer idResponsable) throws SQLException {
        if (idResponsable == null || idResponsable <= 0) {
            throw new IllegalArgumentException("ID de responsable inválido");
        }

        return reporteTrasladoRepository.findByResponsable(idResponsable);
    }

    /**
     * BUSCAR reportes de traslado por rango de fechas
     */
    public List<ReporteTraslado> getReportesByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        return reporteTrasladoRepository.findByDateRange(fechaInicio, fechaFin);
    }

    /**
     * CREAR nuevo reporte de traslado
     */
    public ReporteTraslado createReporteTraslado(ReporteTraslado reporteTraslado) throws SQLException {
        // Validaciones básicas del reporte padre
        validateReporteData(reporteTraslado);

        // Validaciones específicas de traslado
        validateTrasladoData(reporteTraslado);

        // Validaciones de referencias foráneas
        validateForeignKeys(reporteTraslado);

        // Establecer fecha si no se proporcionó
        if (reporteTraslado.getFecha_reporte() == null) {
            reporteTraslado.setFecha_reporte(new Date());
        }

        // Normalizar datos de traslado
        normalizeTrasladoData(reporteTraslado);

        // Guardar reporte de traslado
        return reporteTrasladoRepository.save(reporteTraslado);
    }

    /**
     * ACTUALIZAR reporte de traslado existente
     */
    public ReporteTraslado updateReporteTraslado(ReporteTraslado reporteTraslado) throws SQLException {
        if (reporteTraslado.getId_reporte() == null || reporteTraslado.getId_reporte() <= 0) {
            throw new IllegalArgumentException("ID del reporte requerido para actualización");
        }

        // Verificar que el reporte existe
        if (!reporteTrasladoRepository.existsById(reporteTraslado.getId_reporte())) {
            throw new IllegalArgumentException("Reporte de traslado no encontrado con ID: " + reporteTraslado.getId_reporte());
        }

        // Validaciones básicas del reporte padre
        validateReporteData(reporteTraslado);

        // Validaciones específicas de traslado
        validateTrasladoData(reporteTraslado);

        // Validaciones de referencias foráneas
        validateForeignKeys(reporteTraslado);

        // Normalizar datos de traslado
        normalizeTrasladoData(reporteTraslado);

        // Actualizar reporte de traslado
        boolean updated = reporteTrasladoRepository.update(reporteTraslado);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el reporte de traslado");
        }

        return reporteTraslado;
    }

    /**
     * ELIMINAR reporte de traslado
     */
    public boolean deleteReporteTraslado(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el reporte existe
        if (!reporteTrasladoRepository.existsById(id)) {
            throw new IllegalArgumentException("Reporte de traslado no encontrado con ID: " + id);
        }

        // Eliminar reporte de traslado (cascada a tabla padre)
        return reporteTrasladoRepository.deleteById(id);
    }

    /**
     * OBTENER estadísticas de reportes de traslado
     */
    public Map<String, Object> getReporteTrasladoStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_traslados", reporteTrasladoRepository.countTotal());

        // Obtener áreas más utilizadas como origen
        List<ReporteTrasladoRepository.AreaEstadistica> areasOrigen =
                reporteTrasladoRepository.getAreasOrigenMasUsadas(10);
        stats.put("areas_origen_populares", areasOrigen);

        // Obtener áreas más utilizadas como destino
        List<ReporteTrasladoRepository.AreaEstadistica> areasDestino =
                reporteTrasladoRepository.getAreasDestinoMasUsadas(10);
        stats.put("areas_destino_populares", areasDestino);

        return stats;
    }

    /**
     * OBTENER áreas más utilizadas como origen
     */
    public List<ReporteTrasladoRepository.AreaEstadistica> getAreasOrigenPopulares(int limit) throws SQLException {
        if (limit <= 0) {
            limit = 10;
        }

        return reporteTrasladoRepository.getAreasOrigenMasUsadas(limit);
    }

    /**
     * OBTENER áreas más utilizadas como destino
     */
    public List<ReporteTrasladoRepository.AreaEstadistica> getAreasDestinoPopulares(int limit) throws SQLException {
        if (limit <= 0) {
            limit = 10;
        }

        return reporteTrasladoRepository.getAreasDestinoMasUsadas(limit);
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos básicos del reporte padre
     */
    private void validateReporteData(ReporteTraslado reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte de traslado no puede ser nulo");
        }

        // Usar validación de la clase padre primero
        if (!reporte.isValid()) {
            throw new IllegalArgumentException("Los datos básicos del reporte no son válidos");
        }

        // Validar longitudes específicas
        if (reporte.getAsunto().length() < 5 || reporte.getAsunto().length() > 200) {
            throw new IllegalArgumentException("El asunto debe tener entre 5 y 200 caracteres");
        }

        if (reporte.getContenido().length() < 10 || reporte.getContenido().length() > 1000) {
            throw new IllegalArgumentException("El contenido debe tener entre 10 y 1000 caracteres");
        }

        // Validar que la fecha no sea futura
        if (reporte.getFecha_reporte() != null && reporte.getFecha_reporte().after(new Date())) {
            throw new IllegalArgumentException("La fecha del reporte no puede ser futura");
        }
    }

    /**
     * Validar datos específicos de traslado
     */
    private void validateTrasladoData(ReporteTraslado reporteTraslado) {
        // Validar que tiene todos los campos de traslado
        if (!reporteTraslado.isValid()) {
            throw new IllegalArgumentException("Faltan datos específicos del traslado");
        }

        // Validar que el traslado tiene sentido (origen diferente a destino)
        if (!reporteTraslado.isValidTraslado()) {
            throw new IllegalArgumentException("El traslado debe ser a una ubicación diferente");
        }

        // Validar longitudes
        if (reporteTraslado.getArea_origen().length() < 2 || reporteTraslado.getArea_origen().length() > 100) {
            throw new IllegalArgumentException("El área origen debe tener entre 2 y 100 caracteres");
        }

        if (reporteTraslado.getArea_destino().length() < 2 || reporteTraslado.getArea_destino().length() > 100) {
            throw new IllegalArgumentException("El área destino debe tener entre 2 y 100 caracteres");
        }

        if (reporteTraslado.getUbicacion_origen().length() < 2 || reporteTraslado.getUbicacion_origen().length() > 100) {
            throw new IllegalArgumentException("La ubicación origen debe tener entre 2 y 100 caracteres");
        }

        if (reporteTraslado.getUbicacion_destino().length() < 2 || reporteTraslado.getUbicacion_destino().length() > 100) {
            throw new IllegalArgumentException("La ubicación destino debe tener entre 2 y 100 caracteres");
        }

        if (reporteTraslado.getMotivo().length() < 5 || reporteTraslado.getMotivo().length() > 500) {
            throw new IllegalArgumentException("El motivo debe tener entre 5 y 500 caracteres");
        }

        // Validar caracteres permitidos en áreas y ubicaciones
        String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-\\.]+$";

        if (!reporteTraslado.getArea_origen().matches(regex)) {
            throw new IllegalArgumentException("El área origen contiene caracteres no válidos");
        }

        if (!reporteTraslado.getArea_destino().matches(regex)) {
            throw new IllegalArgumentException("El área destino contiene caracteres no válidos");
        }

        if (!reporteTraslado.getUbicacion_origen().matches(regex)) {
            throw new IllegalArgumentException("La ubicación origen contiene caracteres no válidos");
        }

        if (!reporteTraslado.getUbicacion_destino().matches(regex)) {
            throw new IllegalArgumentException("La ubicación destino contiene caracteres no válidos");
        }
    }

    /**
     * Validar que las referencias foráneas existen
     */
    private void validateForeignKeys(ReporteTraslado reporte) throws SQLException {
        // Validar que el tipo de reporte existe
        if (!tipoReporteRepository.existsById(reporte.getId_tipo_reporte())) {
            throw new IllegalArgumentException("El tipo de reporte con ID " + reporte.getId_tipo_reporte() + " no existe");
        }

        // Validar que el especimen existe
        if (!especimenRepository.existsById(reporte.getId_especimen())) {
            throw new IllegalArgumentException("El especimen con ID " + reporte.getId_especimen() + " no existe");
        }

        // Validar que el responsable existe
        if (!usuarioRepository.existsById(reporte.getId_responsable())) {
            throw new IllegalArgumentException("El responsable con ID " + reporte.getId_responsable() + " no existe");
        }
    }

    /**
     * Normalizar datos de traslado
     */
    private void normalizeTrasladoData(ReporteTraslado reporteTraslado) {
        // Capitalizar primera letra y limpiar espacios
        reporteTraslado.setArea_origen(capitalizeWords(reporteTraslado.getArea_origen().trim()));
        reporteTraslado.setArea_destino(capitalizeWords(reporteTraslado.getArea_destino().trim()));
        reporteTraslado.setUbicacion_origen(capitalizeWords(reporteTraslado.getUbicacion_origen().trim()));
        reporteTraslado.setUbicacion_destino(capitalizeWords(reporteTraslado.getUbicacion_destino().trim()));
        reporteTraslado.setMotivo(reporteTraslado.getMotivo().trim());
    }

    /**
     * Capitalizar palabras
     */
    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            if (!words[i].isEmpty()) {
                result.append(words[i].substring(0, 1).toUpperCase())
                        .append(words[i].substring(1));
            }
        }

        return result.toString();
    }
}