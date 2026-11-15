package com.example.demo;

import com.example.demo.AttributeEncryptor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita  {

   

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora; // sin column detecta Localdatetime y crea la columna de tipo date sin problema jpa

    // Cifrado con AES
    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 4000,columnDefinition = "BLOB") // Espacio extra para texto largo cifrado
    private String tratamiento;

    // Cifrado con AES (Dato clave para el LLM)
    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 4000,columnDefinition = "BLOB")
    private String diagnostico;
    
    @Convert(converter = AttributeEncryptor.class)
    @Column(length = 4000,columnDefinition = "BLOB")
    private String anamesis;
    
    @Convert(converter = AttributeEncryptor.class)
    @Lob
    @Column(columnDefinition = "BLOB",nullable = true) // Cambiamos a TEXT para que H2/SQL no limite a 255
    private String Respuesta_ia;
    
   
    @Column(nullable = true)
    private Integer valoracion;
    
    
    @Column(nullable = true)
    private String comenmed;
    
 // Se guardará como: "dolor,fiebre,tos"
    @Column(columnDefinition = "TEXT") 
    private String palabrasClave;
    // Relación con el Paciente
    @ManyToOne //muchas instancias de la clase citas a una de paciente
    @JoinColumn(name = "paciente_id", nullable = false) //nullable hace que no se pueda dejar sin meter un id de paciente para la cita
    private Paciente paciente;

    // --- CONSTRUCTORES ---

    public Cita() {
    }

 

	public Cita(LocalDateTime fechaHora, String tratamiento, String diagnostico,String anamesis, String Respuesta_ia,Integer valoracion, String PalabrasClave ,String comenmed, Paciente paciente) {
        this.fechaHora = fechaHora;
        this.tratamiento = tratamiento;
        this.diagnostico = diagnostico;
        this.anamesis = anamesis;
        this.Respuesta_ia = Respuesta_ia;
        this.valoracion =valoracion;
        this.comenmed = comenmed;
        this.palabrasClave = PalabrasClave;
        this.paciente = paciente;
    }

    // --- GETTERS Y SETTERS ---
	 public String getComenmed() {
			return comenmed;
		}



		public void setComenmed(String comenmed) {
			this.comenmed = comenmed;
		}
		
	   public String getPalabrasClave() {
			return palabrasClave;
		}

		public void setPalabrasClave(String palabrasClave) {
			this.palabrasClave = palabrasClave;
		}
    public String getAnamesis() {
		return anamesis;
	}

	public void setAnamesis(String anamesis) {
		this.anamesis = anamesis;
	}

	public String getRespuesta_ia() {
		return Respuesta_ia;
	}

	public void setRespuesta_ia(String respuesta_ia) {
		Respuesta_ia = respuesta_ia;
	}

	public Integer getValoracion() {
		return valoracion;
	}

	public void setValoracion(Integer valoracion) {
		this.valoracion = valoracion;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }
}
