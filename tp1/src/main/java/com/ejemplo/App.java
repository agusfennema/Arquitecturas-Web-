package com.ejemplo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class App {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/arquitecturas_web";
        String usuario = "root";
        String password = "";

        try {
            Connection conexion = DriverManager.getConnection(url, usuario, password);
    //        String sql = "SELECT * FROM persona";

  String sql = "INSERT INTO persona (id, nombre, edad) VALUES (?, ?, ?)";

PreparedStatement sentencia = conexion.prepareStatement(sql);

sentencia.setInt(1, 6);
sentencia.setString(2, "DAVIS");
sentencia.setInt(3, 30);

sentencia.executeUpdate();

System.out.println("Persona insertada");
String sqlSelect = "SELECT * FROM persona";

PreparedStatement sentenciaSelect = conexion.prepareStatement(sqlSelect);
ResultSet resultado = sentenciaSelect.executeQuery();

while (resultado.next()) {
    int id = resultado.getInt("id");
    String nombre = resultado.getString("nombre");
    int edad = resultado.getInt("edad");

    System.out.println(id + " - " + nombre + " - " + edad);
}
            

        System.out.println("¡CONEXIÓN EXITOSA!");
            conexion.close();

        } catch (Exception e) {
            System.out.println("ERRORRRRRRRRRRRR");
            e.printStackTrace();
        }
    }
}