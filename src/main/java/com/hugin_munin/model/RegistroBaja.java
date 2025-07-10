package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Objects;

/**
 * Modelo para la entidad RegistroBaja
 * Representa el registro cuando un especimen es dado de baja del sistema
 */
public class RegistroBaja {

    @JsonProperty("id_registro_baja")
    private Integer id_registro_baja;

    @JsonProperty("id_especimen")
    private Integer id_especimen;

    @JsonProperty("especimen")
    private Especimen especimen;

    @JsonProperty("id_causa_baja")
    private Integer id_causa_baja;

    @JsonProperty("causa_baja")
    private CausaBaja causa_baja;

    @JsonProperty("id_responsable")
    private Integer id_responsable;

    @JsonProperty("responsable")
    private Usuario responsable;

    @JsonProperty("fecha_baja")
    private Date fecha_baja;

    @JsonProperty("observacion")
    private String observacion;

    // Constructores
    public RegistroBaja() {
    }

    public RegistroBaja(Integer id_especimen, Integer id_causa_baja, Integer id_responsable,
                        Date fecha_baja, String observacion) {
        this.id_especimen = id_especimen;
        this.id_causa_baja = id_causa_baja;
        this.id_responsable = id_responsable;
        this.fecha_baja = fecha_baja;
        this.observacion = observacion;
    }

    public RegistroBaja(Integer id_registro_baja, Integer id_especimen, Especimen especimen,
                        Integer id_causa_baja, CausaBaja causa_baja, Integer id_responsable,
                        Usuario responsable, Date fecha_baja, String observacion) {
        this.id_registro_baja = id_registro_baja;
        this.id_especimen = id_especimen;
        this.especimen = especimen;
        this.id_causa_baja = id_causa_baja;
        this.causa_baja = causa_baja;
        this.id_responsable = id_responsable;
        this.responsable = responsable;
        this.fecha_baja = fecha_baja;
        this.observacion = observacion;
    }

    // Getters
    public Integer getId_registro_baja() {
        return id_registro_baja;
    }

    public Integer getId_especimen() {
        return id_especimen;
    }

    public Especimen getEspecimen() {
        return especimen;
    }

    public Integer getId_causa_baja() {
        return id_causa_baja;
    }

    public CausaBaja getCausa_baja() {
        return causa_baja;
    }

    public Integer getId_responsable() {
        return id_responsable;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public Date getFecha_baja() {
        return fecha_baja;
    }

    public String getObservacion() {
        return observacion;
    }

    // Setters
    public void setId_registro_baja(Integer id_registro_baja) {
        this.id_registro_baja = id_registro_baja;
    }

    public void setId_especimen(Integer id_especimen) {
        this.id_especimen = id_especimen;
    }

    public void setEspecimen(Especimen especimen) {
        this.especimen = especimen;
    }

    public void setId_causa_baja(Integer id_causa_baja) {
        this.id_causa_baja = id_causa_baja;
    }

    public void setCausa_baja(CausaBaja causa_baja) {
        this.causa_baja = causa_baja;
    }

    public void setId_responsable(Integer id_responsable) {
        this.id_responsable = id_responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public void setFecha_baja(Date fecha_baja) {
        this.fecha_baja = fecha_baja;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroBaja that = (RegistroBaja) o;
        return Objects.equals(id_registro_baja, that.id_registro_baja);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_registro_baja);
    }

    /**
     * Validar si el registro tiene los datos mínimos requeridos
     */
    public boolean isValid() {
        return id_especimen != null &&
                id_causa_baja != null &&
                id_responsable != null &&
                fecha_baja != null &&
                observacion != null && !observacion.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("RegistroBaja{id=%d, especimen=%d, causa=%d, fecha=%s}",
                id_registro_baja, id_especimen, id_causa_baja, fecha_baja);
    }
}