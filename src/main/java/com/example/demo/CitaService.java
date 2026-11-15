package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    public List<Cita> obtenerSugerenciasPro(String terminosCompletos) {
        if (terminosCompletos == null || terminosCompletos.isEmpty()) {
            return new ArrayList<>();
        }

   
        List<String> palabrasBuscadas = Stream.of(terminosCompletos.toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toList());

       

      
        // Traemos todas las citas que tengan alguna de las palabras clave.
        // Usamos un Set para evitar citas duplicadas si coinciden en varias palabras.
        Set<Cita> todasLasPosibles = new HashSet<>();
        for (String palabra : palabrasBuscadas) {
            todasLasPosibles.addAll(citaRepository.buscarPorTermino(palabra));
        }

        
        // Ahora sí, sobre ese grupo de "posibles", exigimos que coincidan al menos 3
        return todasLasPosibles.stream().filter(cita -> {
            String pcCita = (cita.getPalabrasClave() != null) ? cita.getPalabrasClave().toLowerCase() : "";
            
            // Contamos cuántas palabras de la búsqueda están en esta cita de la BD
            long nCoincidencias = palabrasBuscadas.stream()
                    .filter(pcCita::contains)
                    .count();

            return nCoincidencias >= 3; 
        }).collect(Collectors.toList());
    }
}