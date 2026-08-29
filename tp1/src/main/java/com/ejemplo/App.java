package com.ejemplo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.List;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("tp1");

        EntityManager em = emf.createEntityManager();

        try {

            // INSERTAR UNA PERSONA

            em.getTransaction().begin();

            Persona persona = new Persona(6, "DAVIS", 30);

            em.persist(persona);

            em.getTransaction().commit();

            System.out.println("Persona insertada");


            // CONSULTAR TODAS LAS PERSONAS

            List<Persona> personas = em
                    .createQuery(
                            "SELECT p FROM Persona p",
                            Persona.class
                    )
                    .getResultList();

            System.out.println("Personas:");

            for (Persona p : personas) {
                System.out.println(p);
            }

            System.out.println("¡CONEXIÓN EXITOSA!");

        } catch (Exception e) {

            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            System.out.println("ERROR");
            e.printStackTrace();

        } finally {

            em.close();
            emf.close();

        }
    }
}