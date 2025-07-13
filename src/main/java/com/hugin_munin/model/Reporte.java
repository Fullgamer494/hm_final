package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Modelo para la entidad Reporte (Clase padre) - CORREGIDO
 * Representa los reportes generales del sistema
 * AGREGADO: Manejo adecuado de fechas con @JsonFormat
 * CORREGIDO: Eliminado campo 'activo' del procesamiento
 */
public class Reporte {

    @JsonProperty("id_reporte")
    private Integer id_reporte;

    @JsonProperty("id_tipo_reporte")
    private Integer id_tipo_reporte;

    @JsonProperty("tipo_reporte")
    private TipoReporte tipo_reporte;

    @JsonProperty("id_especimen")
    private Integer id_especimen;

    @JsonProperty("especimen")
    private Especimen especimen;

    @JsonProperty("id_responsable")
    private Integer id_responsable;

    @JsonProperty("responsable")
    private Usuario responsable;

    @JsonProperty("asunto")
    private String asunto;

    @JsonProperty("contenido")
    private String contenido;

    @JsonProperty("fecha_reporte")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date fecha_reporte;

    // Constructores
    public Reporte() {
        this.fecha_reporte = new Date();
    }

    public Reporte(Integer id_tipo_reporte, Integer id_especimen, Integer id_responsable,
                   String asunto, String contenido) {
        this.id_tipo_reporte = id_tipo_reporte;
        this.id_especimen = id_especimen;
        this.id_responsable = id_responsable;
        this.asunto = asunto;
        this.contenido = contenido;
        this.fecha_reporte = new Date();
    }

    public Reporte(Integer id_reporte, Integer id_tipo_reporte, TipoReporte tipo_reporte,
                   Integer id_especimen, Especimen especimen, Integer id_responsable,
                   Usuario responsable, String asunto, String contenido, Date fecha_reporte) {
        this.id_reporte = id_reporte;
        this.id_tipo_reporte = id_tipo_reporte;
        this.tipo_reporte = tipo_reporte;
        this.id_especimen = id_especimen;
        this.especimen = especimen;
        this.id_responsable = id_responsable;
        this.responsable = responsable;
        this.asunto = asunto;
        this.contenido = contenido;
        this.fecha_reporte = fecha_reporte;
    }

    // Getters
    public Integer getId_reporte() {
        return id_reporte;
    }

    public Integer getId_tipo_reporte() {
        return id_tipo_reporte;
    }

    public TipoReporte getTipo_reporte() {
        return tipo_reporte;
    }

    public Integer getId_especimen() {
        return id_especimen;
    }

    public Especimen getEspecimen() {
        return especimen;
    }

    public Integer getId_responsable() {
        return id_responsable;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public String getAsunto() {
        return asunto;
    }

    public String getContenido() {
        return contenido;
    }

    public Date getFecha_reporte() {
        return fecha_reporte;
    }

    // Setters
    public void setId_reporte(Integer id_reporte) {
        this.id_reporte = id_reporte;
    }

    public void setId_tipo_reporte(Integer id_tipo_reporte) {
        this.id_tipo_reporte = id_tipo_reporte;
    }

    public void setTipo_reporte(TipoReporte tipo_reporte) {
        this.tipo_reporte = tipo_reporte;
    }

    public void setId_especimen(Integer id_especimen) {
        this.id_especimen = id_especimen;
    }

    public void setEspecimen(Especimen especimen) {
        this.especimen = especimen;
    }

    public void setId_responsable(Integer id_responsable) {
        this.id_responsable = id_responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public void setFecha_reporte(Date fecha_reporte) {
        this.fecha_reporte = fecha_reporte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reporte reporte = (Reporte) o;
        return Objects.equals(id_reporte, reporte.id_reporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_reporte);
    }

    /**
     * Validar si el reporte tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return id_tipo_reporte != null && id_especimen != null &&
                id_responsable != null && asunto != null && !asunto.trim().isEmpty() &&
                contenido != null && !contenido.trim().isEmpty() && fecha_reporte != null;
    }

    /**
     * Información resumida para mostrar
     */
    public String getSummaryInfo() {
        return String.format("Reporte #%d - %s", id_reporte, asunto);
    }

    @Override
    public String toString() {
        return String.format("Reporte{id=%d, tipo=%d, asunto='%s', fecha=%s}",
                id_reporte, id_tipo_reporte, asunto, fecha_reporte);
    }
}