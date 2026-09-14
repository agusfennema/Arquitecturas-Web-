package edu.isistan;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory("miUnidadPersistencia");

        EntityManager em = emf.createEntityManager();

        System.out.println("JPA FUNCIONA");

        em.close();
        emf.close();
    }
}