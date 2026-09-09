package edu.isistan.dao;

import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import edu.isistan.dao.Persona;

@Entity
public class Direccion {
    @Id 
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
    @Column
    private String ciudad;
    @Column
    private String calle;
    @OneToMany(mappedBy="domicilio", fetch=FetchType.LAZY)
    private List<Persona> habitante;

    public Direccion() {
        super();
        this.habitante = new ArrayList<Persona>();
    }

    public Direccion(string ciudad, string calle){
        super();
        this.ciudad = ciudad;
        this. calle = calle;
    }

    public String getCiudad(){
        return ciudad;
    }

    public Direccion getDomicilio() {
    return domicilio;
}

public void setDomicilio(Direccion domicilio) {
    this.domicilio.getHabitante().remove(this);
    domicilio.getHabitante().add(this);
    this.domicilio = domicilio;
}

public int getId() {
    return id;
}

@Override
public String toString() {
    return "Persona [id=" + id + ", nombre=" + nombre + ", edad=" + edad + ", domicilio="
}
    
}
