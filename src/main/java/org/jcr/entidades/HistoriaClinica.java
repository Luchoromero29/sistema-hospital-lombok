package org.jcr.entidades;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriaClinica implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_historia", nullable = false, unique = true, length = 50)
    private String numeroHistoria;

    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
    @ElementCollection
    @CollectionTable(name = "diagnosticos", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "diagnostico", nullable = false, length = 200)
    private List<String> diagnosticos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tratamientos", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "tratamiento", nullable = false, length = 200)
    private List<String> tratamientos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "alergias", joinColumns = @JoinColumn(name = "historia_id"))
    @Column(name = "alergia", nullable = false, length = 200)
    private List<String> alergias = new ArrayList<>();

    public HistoriaClinica(Paciente paciente) {
        this.paciente = Objects.requireNonNull(paciente, "El paciente no puede ser nulo");
        this.fechaCreacion = LocalDateTime.now();
        this.numeroHistoria = generarNumeroHistoria();
    }

    private String generarNumeroHistoria() {
        return "HC-" + paciente.getDni() + "-" + fechaCreacion.getYear();
    }

    public void agregarDiagnostico(String diagnostico) {
        if (diagnostico != null && !diagnostico.trim().isEmpty()) {
            diagnosticos.add(diagnostico);
        }
    }

    public void agregarTratamiento(String tratamiento) {
        if (tratamiento != null && !tratamiento.trim().isEmpty()) {
            tratamientos.add(tratamiento);
        }
    }

    public void agregarAlergia(String alergia) {
        if (alergia != null && !alergia.trim().isEmpty()) {
            alergias.add(alergia);
        }
    }

    public List<String> getDiagnosticos() {
        return Collections.unmodifiableList(diagnosticos);
    }

    public List<String> getTratamientos() {
        return Collections.unmodifiableList(tratamientos);
    }

    public List<String> getAlergias() {
        return Collections.unmodifiableList(alergias);
    }

    @Override
    public String toString() {
        return "HistoriaClinica{" +
                "numeroHistoria='" + numeroHistoria + '\'' +
                ", paciente=" + (paciente != null ? paciente.getNombreCompleto() : "N/A") +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}
