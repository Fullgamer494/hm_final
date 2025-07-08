package com.hugin_munin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Objects;

public class RegistroAlta {
    @JsonProperty("id_registro_alta")
    private Integer id_registro_alta;

    @JsonProperty("id_especimen")
    private Integer id_especimen;

    @JsonProperty("especimen")
    private Especimen especimen;

    @JsonProperty("id_origen_alta")
    private Integer id_origen_alta;

    @JsonProperty("origen_alta")
    private OrigenAlta origen_alta;

    @JsonProperty("id_responsable")
    private Integer id_responsable;

    @JsonProperty("responsable")
    private Usuario responsable;

    @JsonProperty("fecha_ingreso")
    private Date fecha_ingreso;

    @JsonProperty("procedencia")
    private String procedencia;

    @JsonProperty("observacion")
    private String observacion;

    // Constructores
    public RegistroAlta() {
    }

    public RegistroAlta(Integer id_especimen, Especimen especimen, Integer id_origen_alta, OrigenAlta origen_alta, Integer id_responsable, Usuario responsable, Date fecha_ingreso, String procedencia, String observacion) {
        this.id_especimen = id_especimen;
        this.especimen = especimen;
        this.id_origen_alta = id_origen_alta;
        this.origen_alta = origen_alta;
        this.id_responsable = id_responsable;
        this.responsable = responsable;
        this.fecha_ingreso = fecha_ingreso;
    }

    public RegistroAlta(Integer id_registro_alta, Integer id_especimen, Especimen especimen, Integer id_origen_alta, OrigenAlta origen_alta, Integer id_responsable, Usuario responsable,Date fecha_ingreso, String procedencia, String observacion) {
        this.id_registro_alta = id_registro_alta;
        this.id_especimen = id_especimen;
        this.especimen = especimen;
        this.id_origen_alta = id_origen_alta;
        this.origen_alta = origen_alta;
        this.id_responsable = id_responsable;
        this.responsable = responsable;
        this.fecha_ingreso = fecha_ingreso;
        this.procedencia = procedencia;
        this.observacion = observacion;
    }

    // Getters
    public Integer getId_registro_alta() {
        return id_registro_alta;
    }

    public Integer getId_especimen() {
        return id_especimen;
    }

    public Especimen getEspecimen() {
        return especimen;
    }

    public Integer getId_origen_alta() {
        return id_origen_alta;
    }

    public OrigenAlta getOrigen_alta() {
        return origen_alta;
    }

    public Integer getId_responsable() {
        return id_responsable;
    }

    public Usuario getResponsable() {
        return responsable;
    }

    public Date getFecha_ingreso() {
        return fecha_ingreso;
    }

    public String getProcedencia() {
        return procedencia;
    }

    public String getObservacion() {
        return observacion;
    }

    // Setters
    public void setId_registro_alta(Integer id_registro_alta) {
        this.id_registro_alta = id_registro_alta;
    }

    public void setId_especimen(Integer id_especimen) {
        this.id_especimen = id_especimen;
    }

    public void setEspecimen(Especimen especimen) {
        this.especimen = especimen;
    }

    public void setId_origen_alta(Integer id_origen_alta) {
        this.id_origen_alta = id_origen_alta;
    }

    public void setOrigen_alta(OrigenAlta origen_alta) {
        this.origen_alta = origen_alta;
    }

    public void setId_responsable(Integer id_responsable) {
        this.id_responsable = id_responsable;
    }

    public void setResponsable(Usuario responsable) {
        this.responsable = responsable;
    }

    public void setFecha_ingreso(Date fecha_ingreso) {
        this.fecha_ingreso = fecha_ingreso;
    }

    public void setProcedencia(String procedencia) {
        this.procedencia = procedencia;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RegistroAlta that = (RegistroAlta) o;
        return Objects.equals(id_registro_alta, that.id_registro_alta)
                && Objects.equals(id_especimen, that.id_especimen)
                && Objects.equals(id_origen_alta, that.id_origen_alta)
                && Objects.equals(id_responsable, that.id_responsable)
                && Objects.equals(especimen, that.especimen)
                && Objects.equals(origen_alta, that.origen_alta)
                && Objects.equals(responsable, that.responsable)
                && Objects.equals(fecha_ingreso, that.fecha_ingreso)
                && Objects.equals(procedencia, that.procedencia)
                && Objects.equals(observacion, that.observacion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id_registro_alta, id_especimen, especimen, id_origen_alta,
                origen_alta, id_responsable, responsable, fecha_ingreso,
                procedencia, observacion);
    }

    public boolean isValid() {
        return id_especimen != null && id_origen_alta != null && id_responsable != null && fecha_ingreso != null && procedencia != null && observacion != null;
    }

    @Override
    public String toString() {
        return "RegistroAlta{" +
                "id_registro_alta=" + id_registro_alta +
                ", id_especimen=" + id_especimen +
                ", especimen=" + especimen +
                ", id_origen_alta=" + id_origen_alta +
                ", origen_alta=" + origen_alta +
                ", id_responsable=" + id_responsable +
                ", responsable=" + responsable +
                ", fecha_ingreso=" + fecha_ingreso +
                ", procedencia='" + procedencia + '\'' +
                ", observacion='" + observacion + '\'' +
                '}';
    }
}