package edu.isistan.dao;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.ManyToMany;
import javax.persistence.FetchType;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private Timestamp fecha;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Persona> jugadores;

    public Turno() {
        super();
    }

    public Turno(Timestamp fecha){
        super();
        this.fecha = fecha;
        this.jugadores = new ArrayList<Persona>();  
    }

public Timestamp getFecha() {
    return fecha;
}

public int getId() {
    return id;
}

public List<Persona> getJugadores() {
    return jugadores;
}

public void setFecha(Timestamp fecha) {
    this.fecha = fecha;
}

public void setId(int id) {
    this.id = id;
}

public void setJugadores(List<Persona> jugadores) {
    this.jugadores = jugadores;
}

@Override
public String toString() {
    // TODO Auto-generated method stub
    return super.toString();
}

}
