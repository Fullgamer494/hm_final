package com.hugin_munin.service;

import com.hugin_munin.model.Reporte;
import com.hugin_munin.repository.ReporteRepository;
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
 * Servicio para gestionar reportes
 * Contiene la lógica de negocio para reportes
 */
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final TipoReporteRepository tipoReporteRepository;
    private final EspecimenRepository especimenRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteService(ReporteRepository reporteRepository,
                          TipoReporteRepository tipoReporteRepository,
                          EspecimenRepository especimenRepository,
                          UsuarioRepository usuarioRepository) {
        this.reporteRepository = reporteRepository;
        this.tipoReporteRepository = tipoReporteRepository;
        this.especimenRepository = especimenRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * OBTENER todos los reportes
     */
    public List<Reporte> getAllReportes() throws SQLException {
        return reporteRepository.findAll();
    }

    /**
     * OBTENER reporte por ID
     */
    public Reporte getReporteById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        Optional<Reporte> reporte = reporteRepository.findById(id);
        return reporte.orElseThrow(() ->
                new IllegalArgumentException("Reporte no encontrado con ID: " + id));
    }

    /**
     * BUSCAR reportes por tipo
     */
    public List<Reporte> getReportesByTipo(Integer idTipoReporte) throws SQLException {
        if (idTipoReporte == null || idTipoReporte <= 0) {
            throw new IllegalArgumentException("ID de tipo de reporte inválido");
        }

        return reporteRepository.findByTipoReporte(idTipoReporte);
    }

    /**
     * BUSCAR reportes por especimen
     */
    public List<Reporte> getReportesByEspecimen(Integer idEspecimen) throws SQLException {
        if (idEspecimen == null || idEspecimen <= 0) {
            throw new IllegalArgumentException("ID de especimen inválido");
        }

        return reporteRepository.findByEspecimen(idEspecimen);
    }

    /**
     * BUSCAR reportes por responsable
     */
    public List<Reporte> getReportesByResponsable(Integer idResponsable) throws SQLException {
        if (idResponsable == null || idResponsable <= 0) {
            throw new IllegalArgumentException("ID de responsable inválido");
        }

        return reporteRepository.findByResponsable(idResponsable);
    }

    /**
     * BUSCAR reportes por asunto
     */
    public List<Reporte> searchReportesByAsunto(String asunto) throws SQLException {
        if (asunto == null || asunto.trim().isEmpty()) {
            throw new IllegalArgumentException("El asunto no puede estar vacío");
        }

        return reporteRepository.findByAsuntoContaining(asunto.trim());
    }

    /**
     * BUSCAR reportes por contenido
     */
    public List<Reporte> searchReportesByContenido(String contenido) throws SQLException {
        if (contenido == null || contenido.trim().isEmpty()) {
            throw new IllegalArgumentException("El contenido no puede estar vacío");
        }

        return reporteRepository.findByContenidoContaining(contenido.trim());
    }

    /**
     * BUSCAR reportes por rango de fechas
     */
    public List<Reporte> getReportesByDateRange(Date fechaInicio, Date fechaFin) throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }

        if (fechaInicio.after(fechaFin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }

        return reporteRepository.findByDateRange(fechaInicio, fechaFin);
    }

    /**
     * CREAR nuevo reporte
     */
    public Reporte createReporte(Reporte reporte) throws SQLException {
        // Validaciones básicas
        validateReporteData(reporte);

        // Validaciones de referencias foráneas
        validateForeignKeys(reporte);

        // Establecer fecha si no se proporcionó
        if (reporte.getFecha_reporte() == null) {
            reporte.setFecha_reporte(new Date());
        }

        // Guardar reporte
        return reporteRepository.save(reporte);
    }

    /**
     * ACTUALIZAR reporte existente
     */
    public Reporte updateReporte(Reporte reporte) throws SQLException {
        if (reporte.getId_reporte() == null || reporte.getId_reporte() <= 0) {
            throw new IllegalArgumentException("ID del reporte requerido para actualización");
        }

        // Verificar que el reporte existe
        if (!reporteRepository.existsById(reporte.getId_reporte())) {
            throw new IllegalArgumentException("Reporte no encontrado con ID: " + reporte.getId_reporte());
        }

        // Validaciones básicas
        validateReporteData(reporte);

        // Validaciones de referencias foráneas
        validateForeignKeys(reporte);

        // Actualizar reporte
        boolean updated = reporteRepository.update(reporte);
        if (!updated) {
            throw new SQLException("No se pudo actualizar el reporte");
        }

        return reporte;
    }

    /**
     * ELIMINAR reporte
     */
    public boolean deleteReporte(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido");
        }

        // Verificar que el reporte existe
        if (!reporteRepository.existsById(id)) {
            throw new IllegalArgumentException("Reporte no encontrado con ID: " + id);
        }

        // Eliminar reporte
        return reporteRepository.deleteById(id);
    }

    /**
     * OBTENER estadísticas de reportes
     */
    public Map<String, Object> getReporteStatistics() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total_reportes", reporteRepository.countTotal());
        return stats;
    }

    // MÉTODOS PRIVADOS DE VALIDACIÓN

    /**
     * Validar datos básicos del reporte
     */
    private void validateReporteData(Reporte reporte) {
        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser nulo");
        }

        if (!reporte.isValid()) {
            throw new IllegalArgumentException("Los datos del reporte no son válidos");
        }

        // Validar longitudes
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
     * Validar que las referencias foráneas existen
     */
    private void validateForeignKeys(Reporte reporte) throws SQLException {
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
}