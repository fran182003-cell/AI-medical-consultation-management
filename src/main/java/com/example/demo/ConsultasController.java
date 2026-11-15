package com.example.demo;

import jakarta.persistence.*;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

// === NUEVO: Imports para el manejo de fechas de la Agenda ===
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.CitaRequestDTO;
import com.example.demo.dto.MedicoRequestDTO;
import com.example.demo.dto.MedicoResponseDTO;
import com.example.demo.dto.PromptRequestDTO;
// === NUEVO: Import del DTO que acabamos de crear para cambiar la hora ===
import com.example.demo.dto.ReservaUpdateRequestDTO; 
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ConsultasController {
    
    @Autowired private MedicoRepository medicoRepo;
    @Autowired private PacienteRepository pacienteRepo;
    @Autowired private CitaRepository citaRepo;
    @Autowired private CitaService citaService;
    @Autowired private IAService iaService;
    
    // === NUEVO: Inyectamos el repositorio de Reservas para poder usarlo ===
    @Autowired private ReservaRepository reservaRepo; 

    // ---------------------------------------------------------
    // === NUEVA SECCIÓN: GESTIÓN DE AGENDA (CALENDARIO) ===
    // ---------------------------------------------------------

    // 1. Endpoint para cargar el calendario del día
    // Recibe el 'username' del médico (que React tiene en localStorage)
    @GetMapping("/reservas/agenda")
    public ResponseEntity<List<Reserva>> getAgendaDelDia(@RequestParam String username) {
        
        // Calculamos automáticamente el inicio (00:00) y fin (23:59) del DÍA ACTUAL
        LocalDateTime inicioDia = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime finDia = LocalDateTime.now().with(LocalTime.MAX);
        
        // Buscamos usando la Query que creaste en el ReservaRepository
        List<Reserva> reservas = reservaRepo.findAgendaDelDia(username, inicioDia, finDia);
        
        return ResponseEntity.ok(reservas);
    }

    // 2. Endpoint para el "Drag & Drop" (arrastrar la cita a otra hora)
    // Usamos PATCH porque solo actualizamos un campo (fechaHora), no todo el objeto
    @PatchMapping("/reservas/{id}")
    public ResponseEntity<?> actualizarHoraReserva(@PathVariable Long id, @Valid @RequestBody ReservaUpdateRequestDTO updateDTO) {
        
        // Comprobamos que la reserva existe
        Reserva reserva = reservaRepo.findById(id).orElse(null);
        if (reserva == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Reserva no encontrada"));
        }
        
        // Actualizamos la hora y guardamos
        reserva.setFechaHora(updateDTO.getNuevaFechaHora());
        reservaRepo.save(reserva);
        
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Hora actualizada correctamente"));
    }

    
    
    @PatchMapping("/reservas/{id}/cancelar")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id) {

        Reserva reserva = reservaRepo.findById(id).orElse(null);
        if (reserva == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Reserva no encontrada"));
        }

        // Cambiamos el estado a CANCELADO
        reserva.setEstado("CANCELADO");
        reservaRepo.save(reserva);

        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Cita cancelada correctamente"));
    }
    
    
    // ---------------------------------------------------------
    // SECCIÓN ORIGINAL DE CITAS Y PACIENTES
    // ---------------------------------------------------------

    // Buscar Paciente (Vista 2)
    @GetMapping("/pacientes/{dni}")
    public ResponseEntity<Paciente> getPaciente(@PathVariable String dni) {
        return pacienteRepo.findByDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/citas/buscar-similares")
    public List<Cita> buscarSimilares(@RequestParam String terminos) {
        return citaService.obtenerSugerenciasPro(terminos);
    }

    // --- ENDPOINT VALIDADO Y ACTUALIZADO ---
    @PostMapping("/citas")
    public ResponseEntity<?> crearCita(@Valid @RequestBody CitaRequestDTO citaDTO) {
        
        // Buscamos al paciente con el ID del DTO
        Paciente paciente = pacienteRepo.findById(citaDTO.getPaciente().getId()).orElse(null);
        if (paciente == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "El paciente no existe"));
        }

        // === NUEVO: ACTUALIZAR EL ESTADO DE LA RESERVA ===
        // Si la petición trae un reservaId, marcamos esa reserva como ATENDIDA.
        if (citaDTO.getReservaId() != null) {
            Reserva reserva = reservaRepo.findById(citaDTO.getReservaId()).orElse(null);
            if (reserva != null) {
                reserva.setEstado("ATENDIDO"); 
                reservaRepo.save(reserva);
            }
        }
        // =================================================

        // Mapeamos
        Cita nuevaCita = new Cita();
        nuevaCita.setFechaHora(citaDTO.getFechaHora());
        nuevaCita.setTratamiento(citaDTO.getTratamiento());
        nuevaCita.setDiagnostico(citaDTO.getDiagnostico());
        nuevaCita.setAnamesis(citaDTO.getAnamesis());
        nuevaCita.setRespuesta_ia(citaDTO.getRespuesta_ia());
        nuevaCita.setValoracion(citaDTO.getValoracion());
        nuevaCita.setComenmed(citaDTO.getComenmed());
        nuevaCita.setPalabrasClave(citaDTO.getPalabrasClave());
        nuevaCita.setPaciente(paciente);

        return ResponseEntity.ok(citaRepo.save(nuevaCita));
    }
    
    // Histórico (Vista 3)
    @GetMapping("/citas/historial/{dni}")
    public List<Cita> getHistorial(@PathVariable String dni) {
        return citaRepo.findByPacienteDni(dni);
    }
    
    // --- ENDPOINT SEGURO: Usamos MedicoResponseDTO ---
    @GetMapping("/admin/medicos")
    public List<MedicoResponseDTO> listar() {
        return medicoRepo.findAll().stream().map(medico -> {
            MedicoResponseDTO dto = new MedicoResponseDTO();
            dto.setId(medico.getId());
            dto.setUsername(medico.getUsername());
            return dto;
        }).collect(Collectors.toList());
    }
    
    // --- ENDPOINT VALIDADO ---
    @PostMapping("/admin/medicos/guardar")
    public ResponseEntity<?> guardar(@Valid @RequestBody MedicoRequestDTO medicoDTO) {
        
        if (medicoRepo.findByUsername(medicoDTO.getUsername()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("username", "El nombre de usuario ya está en uso");
            return ResponseEntity.badRequest().body(error);
        }

        Medico nuevoMedico = new Medico();
        nuevoMedico.setUsername(medicoDTO.getUsername());
        nuevoMedico.setPassword(medicoDTO.getPassword());
        
        medicoRepo.save(nuevoMedico);
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Médico guardado correctamente"));
    }

    @DeleteMapping("/admin/medicos/{id}")
    public void eliminar(@PathVariable UUID id) {
        medicoRepo.deleteById(id);
    }
    
    // --- ENDPOINT VALIDADO ---
    @PostMapping("/ia/generar-respuesta")
    public ResponseEntity<?> generarRespuestaIA(@Valid @RequestBody PromptRequestDTO request) {
        
        String respuestaIA = iaService.generarRespuesta(request.getPrompt());
        
        Map<String, String> response = new HashMap<>();
        response.put("respuesta", respuestaIA);
        
        return ResponseEntity.ok(response);
    }
}