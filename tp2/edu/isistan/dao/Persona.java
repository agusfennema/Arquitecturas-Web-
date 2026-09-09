package edu.isistan.dao;

import java.lang.annotation.Inherited;
import javax.persistence.GenerationType;
import javax.persistenceId;
import javax.persistence.ManyToOne;

@Entity
public class Persona {
    
    @Id 
    private int id;
    @Column(nullable=false)
    private String nombre;
    @Column(name="anios")
    private int edad;
    @ManyToOne
    private Direccion domicilio;

    public Persona() {
        super();
    }

    public Persona(int id, string nombre, int edad, direccion domicilio ) {
        super();
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.domicilio = domicilio;

    }

public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

public String getNombre() {
    return nombre;
}

public void setNombre(String nombre) {
    this.nombre = nombre;
}

public int getEdad() {
    return edad;
}

public void setEdad(int edad) {
    this.edad = edad;
}

public Direccion getDomicilio() {
    return domicilio;
}

public void setDomicilio(Direccion domicilio) {
    this.domicilio.getHabitante().remove(this);
    domicilio.getHabitante().add(this);
    this.domicilio = domicilio;
}

@Override
public String toString() {
    return "Persona [id=" + id + ", nombre=" + nombre + ", edad=" + edad
            + ", domicilio=" + domicilio + "]";
}

}
