package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public class CitaRequestDTO {

    // === NUEVO CAMPO PARA LA AGENDA ===
    // Este ID nos permite vincular la cita clínica con el "hueco" del calendario.
    // Es opcional (Long) porque las urgencias pueden no tener reserva previa.
    private Long reservaId;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;

    @Size(max = 4000, message = "El tratamiento excede el límite permitido")
    private String tratamiento;

    @NotBlank(message = "El diagnóstico es obligatorio")
    @Size(max = 4000, message = "El diagnóstico excede el límite permitido")
    private String diagnostico;

    @NotBlank(message = "La anamnesis es obligatoria")
    @Size(max = 4000, message = "La anamnesis excede el límite permitido")
    private String anamesis;

    private String Respuesta_ia; 
    
    private Integer valoracion;
    
    private String comenmed;
    
    private String palabrasClave;

    @NotNull(message = "Los datos del paciente son obligatorios")
    @Valid 
    private PacienteIdDTO paciente;

    // --- NUEVOS GETTER Y SETTER PARA reservaId ---
    public Long getReservaId() { return reservaId; }
    public void setReservaId(Long reservaId) { this.reservaId = reservaId; }

    // --- RESTO DE GETTERS Y SETTERS ---
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getAnamesis() { return anamesis; }
    public void setAnamesis(String anamesis) { this.anamesis = anamesis; }

    public String getRespuesta_ia() { return Respuesta_ia; }
    public void setRespuesta_ia(String respuesta_ia) { Respuesta_ia = respuesta_ia; }

    public Integer getValoracion() { return valoracion; }
    public void setValoracion(Integer valoracion) { this.valoracion = valoracion; }

    public String getComenmed() { return comenmed; }
    public void setComenmed(String comenmed) { this.comenmed = comenmed; }

    public String getPalabrasClave() { return palabrasClave; }
    public void setPalabrasClave(String palabrasClave) { this.palabrasClave = palabrasClave; }

    public PacienteIdDTO getPaciente() { return paciente; }
    public void setPaciente(PacienteIdDTO paciente) { this.paciente = paciente; }

    public static class PacienteIdDTO {
        @NotNull(message = "El ID del paciente no puede ser nulo")
        private UUID id;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
    }
}