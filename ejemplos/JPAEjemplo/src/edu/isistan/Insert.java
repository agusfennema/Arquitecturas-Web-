package edu.isistan;

import java.sql.Timestamp;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dao.Direccion;
import edu.isistan.dao.Persona;
import edu.isistan.dao.Socio;
import edu.isistan.dao.Turno;

public class Insert {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Example");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		Direccion d = new Direccion("Tandil", "Sarmiento 772");
		em.persist(d);
		Persona j = new Persona(1, "Juan", 25, d);
		Persona a = new Persona(2, "Ana", 22, d);
		em.persist(j);
		em.persist(a);
		Socio sj = new Socio(j, "Basico");
		Socio sa = new Socio(a, "Premium");
		em.persist(sj);
		em.persist(sa);
		Turno t = new Turno(new Timestamp(System.currentTimeMillis()));
		t.getJugadores().add(j);
		em.persist(t);
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

}
