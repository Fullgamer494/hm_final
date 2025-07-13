package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * Modelo para la entidad ReporteTraslado - CORREGIDO
 * Hereda de Reporte y agrega campos específicos para traslados
 * CORREGIDO: Manejo adecuado de fechas heredado de la clase padre
 */
public class ReporteTraslado extends Reporte {

    @JsonProperty("area_origen")
    private String area_origen;

    @JsonProperty("area_destino")
    private String area_destino;

    @JsonProperty("ubicacion_origen")
    private String ubicacion_origen;

    @JsonProperty("ubicacion_destino")
    private String ubicacion_destino;

    @JsonProperty("motivo")
    private String motivo;

    // Constructores
    public ReporteTraslado() {
        super();
    }

    public ReporteTraslado(Integer id_tipo_reporte, Integer id_especimen, Integer id_responsable,
                           String asunto, String contenido, String area_origen, String area_destino,
                           String ubicacion_origen, String ubicacion_destino, String motivo) {
        super(id_tipo_reporte, id_especimen, id_responsable, asunto, contenido);
        this.area_origen = area_origen;
        this.area_destino = area_destino;
        this.ubicacion_origen = ubicacion_origen;
        this.ubicacion_destino = ubicacion_destino;
        this.motivo = motivo;
    }

    public ReporteTraslado(Integer id_reporte, Integer id_tipo_reporte, TipoReporte tipo_reporte,
                           Integer id_especimen, Especimen especimen, Integer id_responsable,
                           Usuario responsable, String asunto, String contenido, Date fecha_reporte,
                           String area_origen, String area_destino,
                           String ubicacion_origen, String ubicacion_destino, String motivo) {
        super(id_reporte, id_tipo_reporte, tipo_reporte, id_especimen, especimen,
                id_responsable, responsable, asunto, contenido, fecha_reporte);
        this.area_origen = area_origen;
        this.area_destino = area_destino;
        this.ubicacion_origen = ubicacion_origen;
        this.ubicacion_destino = ubicacion_destino;
        this.motivo = motivo;
    }

    // Getters específicos de ReporteTraslado
    public String getArea_origen() {
        return area_origen;
    }

    public String getArea_destino() {
        return area_destino;
    }

    public String getUbicacion_origen() {
        return ubicacion_origen;
    }

    public String getUbicacion_destino() {
        return ubicacion_destino;
    }

    public String getMotivo() {
        return motivo;
    }

    // Setters específicos de ReporteTraslado
    public void setArea_origen(String area_origen) {
        this.area_origen = area_origen;
    }

    public void setArea_destino(String area_destino) {
        this.area_destino = area_destino;
    }

    public void setUbicacion_origen(String ubicacion_origen) {
        this.ubicacion_origen = ubicacion_origen;
    }

    public void setUbicacion_destino(String ubicacion_destino) {
        this.ubicacion_destino = ubicacion_destino;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /**
     * Override del método isValid para incluir validaciones específicas de traslado
     */
    @Override
    public boolean isValid() {
        return super.isValid() &&
                area_origen != null && !area_origen.trim().isEmpty() &&
                area_destino != null && !area_destino.trim().isEmpty() &&
                ubicacion_origen != null && !ubicacion_origen.trim().isEmpty() &&
                ubicacion_destino != null && !ubicacion_destino.trim().isEmpty() &&
                motivo != null && !motivo.trim().isEmpty();
    }

    /**
     * Información específica de traslado
     */
    public String getTrasladoInfo() {
        return String.format("Traslado: %s (%s) → %s (%s)",
                area_origen, ubicacion_origen, area_destino, ubicacion_destino);
    }

    /**
     * Validar que el traslado tiene sentido (origen diferente a destino)
     */
    public boolean isValidTraslado() {
        if (area_origen == null || area_destino == null ||
                ubicacion_origen == null || ubicacion_destino == null) {
            return false;
        }

        // El traslado debe ser a un lugar diferente
        return !(area_origen.equals(area_destino) && ubicacion_origen.equals(ubicacion_destino));
    }

    @Override
    public String toString() {
        return String.format("ReporteTraslado{id=%d, origen=%s->%s, destino=%s->%s, motivo='%s'}",
                getId_reporte(), area_origen, ubicacion_origen, area_destino, ubicacion_destino, motivo);
    }
}