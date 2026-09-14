package edu.isistan.dao;

import java.lang.annotation.Inherited;
import java.security.Timestamp;

import javax.annotation.processing.Generated;

@Entity
public class Turno {
    @Inherited 
    @GeneratedValue(strategy = GerationType.AUTO)
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
