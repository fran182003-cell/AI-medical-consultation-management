package com.example.demo;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import java.util.UUID;
import com.example.demo.AttributeEncryptor;
import jakarta.persistence.*;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @Column(columnDefinition = "UUID", updatable = false)// updatable = false impide que se actualice la columna aunque se Integerente cambiar
    private UUID id = UuidCreator.getTimeOrderedEpoch(); 
    
    @Column(length = 1000)
    private String dni; // Clave primaria 

    @Column(length = 1000)
    private String nombre; 

    @Column(length = 1000)
    private Integer edad; 
    
    @Column(length = 1000)
    private String sexo; 
    
    @Column(length = 1000)
    private Float peso; 
    
    @Column(length = 1000)
    private Float altura; 
    

    
    // Asociación: Un paciente pertenece a un médico (según tu requisito)
    @ManyToOne(optional = false)
    @JoinColumn(name = "medico_id", referencedColumnName = "id") // creamos columna medico_id con la clave primaria de la columna id del medico
    private Medico medico;

    // --- CONSTRUCTORES ---

    public Paciente() {
    }

    public Paciente(UUID id, String dni, String nombre,Integer edad, String sexo, Float peso, Float altura, Medico medico) {
    	this.id= id;
        this.dni = dni;
        this.nombre = nombre;
        this.altura = altura;
        this.edad= edad;
        this.sexo = sexo;
        this.peso=peso;
        this.medico = medico;
    }

    // --- GETTERS Y SETTERS ---

    public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Integer getEdad() {
		return edad;
	}

	public void setEdad(Integer edad) {
		this.edad = edad;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public Float getPeso() {
		return peso;
	}

	public void setPeso(Float peso) {
		this.peso = peso;
	}

	public Float getAltura() {
		return altura;
	}

	public void setAltura(Float altura) {
		this.altura = altura;
	}

	public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

 

    public Medico getMedico() {
        return medico;
    }

    public void setMedico(Medico medico) {
        this.medico = medico;
    }
}
