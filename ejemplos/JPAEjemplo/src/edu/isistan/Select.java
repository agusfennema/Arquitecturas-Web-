package edu.isistan;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.isistan.dao.Persona;
import edu.isistan.dao.Turno;

public class Select {
	
	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("Example");
		EntityManager em = emf.createEntityManager();
		em.getTransaction().begin();
		//Persona j = em.find(Persona.class, 1);
		//System.out.println(j);
		@SuppressWarnings("unchecked")
		List<Persona> personas = em.createQuery("SELECT p FROM Persona p").getResultList(); 

		List<Turno> turnos = em.createQuery("SELECT t FROM Turno t JOIN t.jugadores p WHERE p.edad=23").getResultList(); 
//		turnos.forEach(p -> System.out.println(p.getJugadores().getClass()));
//		turnos.forEach(p -> System.out.println(p));

		@SuppressWarnings("unchecked")
		List<Object[]> res = em.createQuery("SELECT p.nombre, p.edad FROM Persona p").getResultList();
		
		res.forEach(d -> System.out.println(Arrays.toString(d)));
		
		System.out.println("************************************************");
		
		for (Object[] d: res) {
			System.out.println(Arrays.toString(d));
		}
		
		em.getTransaction().commit();
		em.close();
		emf.close();
	}

}
