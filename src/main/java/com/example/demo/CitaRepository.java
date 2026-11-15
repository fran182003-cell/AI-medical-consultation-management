package com.example.demo;

import com.example.demo.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {
	List<Cita> findByPacienteDni(String dni);

	@Query("SELECT c FROM Cita c WHERE c.palabrasClave LIKE CONCAT('%', :termino, '%')")
    List<Cita> buscarPorTermino(@Param("termino") String termino);
}
