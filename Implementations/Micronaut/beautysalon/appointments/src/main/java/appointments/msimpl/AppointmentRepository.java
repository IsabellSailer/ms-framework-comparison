package appointments.msimpl;

import io.micronaut.configuration.hibernate.jpa.scope.CurrentSession;
import io.micronaut.spring.tx.annotation.Transactional;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.lang.Exception;

@Singleton
public class AppointmentRepository {

	@PersistenceContext
	private EntityManager entityManager;
	

	public AppointmentRepository(@CurrentSession EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public Appointment save(@NotBlank String customerName, Date date, @NotNull int startTime, @NotNull int endTime,
			@NotBlank String treatmentName, @NotNull int treatmentId) {
		Appointment appointment = new Appointment(customerName, date, startTime, endTime, treatmentId);
		appointment.setTreatmentName(treatmentName);
		appointment.setDuration();
		entityManager.persist(appointment);
		return appointment;
	}

	@Transactional(readOnly = true)
	public Optional<Appointment> findAppointmentById(@NotNull int id) throws Exception {
		return Optional.ofNullable(entityManager.find(Appointment.class, id));
	}

	@Transactional(readOnly = true)
	public List<Appointment> findAllAppointments() throws Exception {
		String qlString = "SELECT a FROM Appointment a";
		TypedQuery<Appointment> query = entityManager.createQuery(qlString, Appointment.class);

		return query.getResultList();
	}

}
