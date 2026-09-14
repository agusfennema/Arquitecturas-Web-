package edu.isistan.dao;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

@Entity
public class Socio {
    
    @Id
    private int id;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    private Persona persona;

    @Column
    private string tipo;

    public Socio(){
        super();
    }

    public Socio(Persona persona, string tipo) {
        super();
        this.id = persona.getId();
        this.persona = persona;
        this.tipo = tipo;
    }

    public Persona getPersona(){
        return persona;
    }
}
