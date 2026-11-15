package com.example.demo;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;

import java.util.Optional;
import java.util.UUID;
import com.example.demo.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente,UUID > {
	// Spring traduce esto a: SELECT * FROM pacientes WHERE dni = ?
    Optional<Paciente> findByDni(String dni);
	// JpaRepository importa todas las funciones de jpa para acceder a la base de datos, devuelve objetos paciente y clave primaria uuid
}
