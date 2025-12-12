package com.academia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    // Configuración de la base de datos
    private static final String URL = "jdbc:mysql://localhost:3306/password_ingles";
    private static final String USUARIO = "root"; 
    private static final String PASSWORD = "root"; 

    public static Connection getConexion() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
            System.out.println("--- CONEXIÓN EXITOSA ---");
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Falta el driver en pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error SQL: Revisa usuario/clave.");
            e.printStackTrace();
        }
        return con;
    }
}