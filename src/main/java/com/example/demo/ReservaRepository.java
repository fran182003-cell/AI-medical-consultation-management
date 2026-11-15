package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    
    // Busca las reservas de un médico en un rango de fechas, ordenadas por hora
    @Query("SELECT r FROM Reserva r WHERE r.medico.username = :username " +
            "AND r.estado != 'CANCELADO' " +  
            "AND r.fechaHora BETWEEN :inicio AND :fin " +
            "ORDER BY r.fechaHora ASC")
     List<Reserva> findAgendaDelDia(@Param("username") String username, 
                                    @Param("inicio") LocalDateTime inicio, 
                                    @Param("fin") LocalDateTime fin);
 }
