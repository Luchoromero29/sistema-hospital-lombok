package org.jcr;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.jcr.entidades.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hospital-persistence-unit");
        EntityManager em = emf.createEntityManager();

        try {
            //crerDatos(em, emf);

            mostrarConsultas(em);

        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
        }
    }

    public static void mostrarConsultas(EntityManager em) {
        System.out.println("\n Datos guardados correctamente en la base de datos H2.\n");

        // ========================================
        // SEGUNDA PARTE: CONSULTAS JPQL
        // ========================================
        em.getTransaction().begin();

        System.out.println("--- CONSULTAS JPQL ---");

        //Hospitales
        TypedQuery<Hospital> q1 = em.createQuery("SELECT h FROM Hospital h", Hospital.class);
        q1.getResultList().forEach(h -> System.out.println("Hospital: " + h.getNombre()));

        // Médicos por especialidad
        TypedQuery<Medico> q2 = em.createQuery(
                "SELECT m FROM Medico m WHERE m.especialidad = :esp", Medico.class);
        q2.setParameter("esp", EspecialidadMedica.CARDIOLOGIA);
        q2.getResultList().forEach(m -> System.out.println("Cardiólogo: " + m.getNombreCompleto()));

        // Citas ordenadas por fecha
        TypedQuery<Cita> q3 = em.createQuery("SELECT c FROM Cita c ORDER BY c.fechaHora", Cita.class);
        q3.getResultList().forEach(c ->
                System.out.println("Cita: " + c.getPaciente().getNombreCompleto() +
                        " con " + c.getMedico().getNombreCompleto() + " el " + c.getFechaHora()));

        // Actualizar una cita a COMPLETADA
        Cita citaActualizable = q3.getResultList().get(0);
        citaActualizable.setEstado(EstadoCita.COMPLETADA);
        citaActualizable.setObservaciones("Consulta completada sin complicaciones.");
        em.merge(citaActualizable);

        // Estadísticas (COUNT)
        Long totalPacientes = em.createQuery("SELECT COUNT(p) FROM Paciente p", Long.class).getSingleResult();
        Long totalCitas = em.createQuery("SELECT COUNT(c) FROM Cita c", Long.class).getSingleResult();
        Long totalMedicos = em.createQuery("SELECT COUNT(m) FROM Medico m", Long.class).getSingleResult();

        System.out.println("\n--- ESTADÍSTICAS ---");
        System.out.println("Pacientes registrados: " + totalPacientes);
        System.out.println("Médicos registrados: " + totalMedicos);
        System.out.println("Citas programadas: " + totalCitas);

        em.getTransaction().commit();

        System.out.println("\nSISTEMA EJECUTADO EXITOSAMENTE ");
    }

    public static void crerDatos(EntityManager em, EntityManagerFactory emf) throws Exception {
        try {
            em.getTransaction().begin();

            // Crear hospital (Aggregate Root)
            Hospital hospital = new Hospital("Hospital Central", "Av. Libertador 1234", "011-4567-8901");

            // Crear departamentos
            Departamento cardio = new Departamento("Cardiología", EspecialidadMedica.CARDIOLOGIA);
            Departamento pedia = new Departamento("Pediatría", EspecialidadMedica.PEDIATRIA);
            Departamento trauma = new Departamento("Traumatología", EspecialidadMedica.TRAUMATOLOGIA);
            hospital.agregarDepartamento(cardio);
            hospital.agregarDepartamento(pedia);
            hospital.agregarDepartamento(trauma);

            // Crear salas por departamento
            Sala salaCardio = cardio.crearSala("CARD-101", "Consultorio");
            Sala salaPedia = pedia.crearSala("PED-201", "Consultorio Infantil");
            Sala salaTrauma = trauma.crearSala("TRAU-301", "Sala de Rehabilitación");

            // Crear médicos especialistas
            Medico medicoCardio = new Medico("Carlos", "González", "12345678",
                    LocalDate.of(1975, 5, 15), TipoSangre.A_POSITIVO,
                    "MP-12345", EspecialidadMedica.CARDIOLOGIA);
            Medico medicoPedia = new Medico("Lucía", "Martínez", "23456789",
                    LocalDate.of(1980, 8, 20), TipoSangre.B_POSITIVO,
                    "MP-22346", EspecialidadMedica.PEDIATRIA);
            Medico medicoTrauma = new Medico("Jorge", "Suárez", "34567890",
                    LocalDate.of(1988, 3, 10), TipoSangre.O_NEGATIVO,
                    "MP-32347", EspecialidadMedica.TRAUMATOLOGIA);

            cardio.agregarMedico(medicoCardio);
            pedia.agregarMedico(medicoPedia);
            trauma.agregarMedico(medicoTrauma);

            // Crear pacientes con historias clínicas
            Paciente pac1 = new Paciente("María", "López", "11111111",
                    LocalDate.of(1985, 12, 5), TipoSangre.A_POSITIVO,
                    "011-1111-1111", "Calle Falsa 123");
            Paciente pac2 = new Paciente("Juan", "Pérez", "22222222",
                    LocalDate.of(1992, 7, 22), TipoSangre.B_NEGATIVO,
                    "011-2222-2222", "Av. Siempre Viva 742");
            Paciente pac3 = new Paciente("Sofía", "García", "33333333",
                    LocalDate.of(2000, 4, 10), TipoSangre.O_POSITIVO,
                    "011-3333-3333", "Pasaje Luna 55");

            hospital.agregarPaciente(pac1);
            hospital.agregarPaciente(pac2);
            hospital.agregarPaciente(pac3);

            pac1.getHistoriaClinica().agregarDiagnostico("Hipertensión arterial");
            pac1.getHistoriaClinica().agregarAlergia("Penicilina");
            pac2.getHistoriaClinica().agregarDiagnostico("Alergia estacional");
            pac3.getHistoriaClinica().agregarDiagnostico("Fractura de pierna");

            //Crear 3 citas médicas
            CitaManager manager = new CitaManager();

            Cita cita1 = manager.programarCita(pac1, medicoCardio, salaCardio,
                    LocalDateTime.now().plusDays(2).withHour(9), new BigDecimal("15000"));
            Cita cita2 = manager.programarCita(pac2, medicoPedia, salaPedia,
                    LocalDateTime.now().plusDays(3).withHour(11), new BigDecimal("12000"));
            Cita cita3 = manager.programarCita(pac3, medicoTrauma, salaTrauma,
                    LocalDateTime.now().plusDays(4).withHour(15), new BigDecimal("18000"));

            pac1.addCita(cita1);
            pac2.addCita(cita2);
            pac3.addCita(cita3);

            medicoCardio.addCita(cita1);
            medicoPedia.addCita(cita2);
            medicoTrauma.addCita(cita3);

            salaCardio.addCita(cita1);
            salaPedia.addCita(cita2);
            salaTrauma.addCita(cita3);

            // Persistir todo con cascading
            em.persist(hospital);
            em.getTransaction().commit();

            System.out.println("\n Datos guardados correctamente en la base de datos H2.\n");

        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } finally {
            em.close();
            emf.close();
        }
    }
}
