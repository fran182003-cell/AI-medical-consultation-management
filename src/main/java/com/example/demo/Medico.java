package com.example.demo;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.UUID;
import jakarta.persistence.*;

@Entity
@Table(name = "medicos")
public class Medico {


	@Id
    @Column(columnDefinition = "UUID", updatable = false)// updatable = false impide que se actualice la columna aunque se intente cambiar
    private UUID id = UuidCreator.getTimeOrderedEpoch(); 

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // --- CONSTRUCTORES ---
    
    // Constructor vacío (Obligatorio para JPA)
    public Medico() {
    }

    // Constructor con campos (Útil para crear objetos rápido)
    public Medico(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // --- GETTERS Y SETTERS ---

    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- TO STRING (Opcional, para depurar) ---
    @Override
    public String toString() {
        return "Medico [id=" + id + ", username=" + username + "]";
    }
}
