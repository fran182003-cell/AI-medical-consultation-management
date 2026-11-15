package com.example.demo;

import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final MedicoRepository medicoRepo;
    private final PacienteRepository pacienteRepo;
    private final ReservaRepository reservaRepo;

    public DataLoader(MedicoRepository medicoRepo, PacienteRepository pacienteRepo, ReservaRepository reservaRepo) {
        this.medicoRepo = medicoRepo;
        this.pacienteRepo = pacienteRepo;
        this.reservaRepo = reservaRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        
        // 1. Buscamos o creamos al médico "admin"
        Medico medicoGuardado = medicoRepo.findByUsername("doctor").orElseGet(() -> {
            Medico nuevo = new Medico("admin", "1234");
            System.out.println("--- Médico admin creado ---");
            return medicoRepo.save(nuevo);
        });
       
        // 2. Creamos 4 pacientes distintos de forma segura
        Paciente p1 = pacienteRepo.findByDni("11111111A").orElseGet(() -> {
            return pacienteRepo.save(new Paciente(UuidCreator.getTimeOrderedEpoch(), "11111111A", "Juan Pérez", 35, "Masculino", 75.5f, 1.78f, medicoGuardado));
        });

        Paciente p2 = pacienteRepo.findByDni("22222222B").orElseGet(() -> {
            return pacienteRepo.save(new Paciente(UuidCreator.getTimeOrderedEpoch(), "22222222B", "María García", 28, "Femenino", 62.0f, 1.65f, medicoGuardado));
        });

        Paciente p3 = pacienteRepo.findByDni("33333333C").orElseGet(() -> {
            return pacienteRepo.save(new Paciente(UuidCreator.getTimeOrderedEpoch(), "33333333C", "Carlos López", 45, "Masculino", 82.3f, 1.80f, medicoGuardado));
        });

        Paciente p4 = pacienteRepo.findByDni("44444444D").orElseGet(() -> {
            return pacienteRepo.save(new Paciente(UuidCreator.getTimeOrderedEpoch(), "44444444D", "Ana Martínez", 52, "Femenino", 70.1f, 1.68f, medicoGuardado));
        });

        System.out.println("--- 4 Pacientes de prueba cargados correctamente ---");

        // 3. Borramos las reservas antiguas para generar siempre frescas para el día de HOY
        reservaRepo.deleteAll();

        // 4. Creamos las reservas para el día ACTUAL asociadas a cada paciente
        LocalDateTime hoy = LocalDateTime.now();

        // Reserva 1: Juan Pérez (PENDIENTE)
        Reserva r1 = new Reserva();
        r1.setMedico(medicoGuardado);
        r1.setPaciente(p1);
        r1.setEstado("PENDIENTE");
        r1.setFechaHora(hoy.with(LocalTime.of(9, 0))); 
        reservaRepo.save(r1);

        // Reserva 2: María García (CANCELADA - Esta desaparecerá del calendario)
        Reserva r2 = new Reserva();
        r2.setMedico(medicoGuardado);
        r2.setPaciente(p2);
        r2.setEstado("CANCELADO");
        r2.setFechaHora(hoy.with(LocalTime.of(10, 30))); 
        reservaRepo.save(r2);

        // Reserva 3: Carlos López (ATENDIDO - Saldrá en gris)
        Reserva r3 = new Reserva();
        r3.setMedico(medicoGuardado);
        r3.setPaciente(p3);
        r3.setEstado("ATENDIDO");
        r3.setFechaHora(hoy.with(LocalTime.of(11, 45))); 
        reservaRepo.save(r3);

        // Reserva 4: Ana Martínez (PENDIENTE)
        Reserva r4 = new Reserva();
        r4.setMedico(medicoGuardado);
        r4.setPaciente(p4);
        r4.setEstado("PENDIENTE");
        r4.setFechaHora(hoy.with(LocalTime.of(13, 15))); 
        reservaRepo.save(r4);

        System.out.println("--- 📅 4 Reservas asignadas a distintos pacientes para la agenda de HOY ---");
    }
}