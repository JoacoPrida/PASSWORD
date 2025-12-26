package com.academia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    public static Connection getConexion() {
        Connection con = null;
        
        // 1. LEER VARIABLES DE ENTORNO (Railway)
        // Intentamos leer las variables que configuraste en Railway
        String host = System.getenv("DB_HOST");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        String port = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");

        // 2. CONFIGURACIÓN LOCAL (Tu PC)
        // Si las variables son null (estás en tu PC), usamos tus datos locales por defecto
        if (host == null) { host = "localhost"; }
        if (user == null) { user = "root"; }
        if (password == null) { password = "root"; } // Tu contraseña local
        if (port == null) { port = "3306"; }
        if (dbName == null) { dbName = "password_ingles"; } // Tu base de datos local

        // 3. CONSTRUIR LA URL JDBC
        // Formato: jdbc:mysql://HOST:PORT/NOMBRE_DB
        // Agregamos ?useSSL=false para evitar advertencias comunes
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true";

        try {
            // Cargar el Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Intentar conectar con los datos obtenidos (sea Railway o Local)
            con = DriverManager.getConnection(url, user, password);
            System.out.println("--- CONEXIÓN EXITOSA a: " + host + " ---");
            
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Falta el driver en pom.xml");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Error SQL: No se pudo conectar a " + host);
            System.out.println("Revisa usuario, contraseña o si la IP está permitida.");
            e.printStackTrace();
        }
        return con;
    }
}