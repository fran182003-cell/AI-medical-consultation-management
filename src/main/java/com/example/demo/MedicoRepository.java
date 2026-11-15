package com.example.demo;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.UUID;
import com.example.demo.Medico; // Ajusta si tu modelo está en otro paquete
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, UUID> {
    Optional<Medico> findByUsername(String username); //metodo especifico para buscar por nombre el medico.Optional permite devolver valores null cuando no hay un medico con ese nombre y la funcion
    //findByUsername es como hacer un SELECT * FROM medicos WHERE username = 'username';
}
