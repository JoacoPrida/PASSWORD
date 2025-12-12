package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException; // Para detectar DNI repetido
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegistrarEstudianteServlet")
public class RegistrarEstudianteServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configuración para leer acentos y caracteres especiales
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 1. RECIBIR DATOS DEL FORMULARIO
        String nombre = request.getParameter("nombre_alumno");
        String dni = request.getParameter("dni_alumno"); // Nuevo campo DNI
        String telefono = request.getParameter("telefono_alumno");
        
        // Convertir textos a números con seguridad
        int edad = 0;
        int idGrupo = 0;
        try {
            edad = Integer.parseInt(request.getParameter("edad_alumno"));
            idGrupo = Integer.parseInt(request.getParameter("id_grupo"));
        } catch (NumberFormatException e) {
            mostrarError(out, "Datos inválidos", "La edad o el grupo seleccionado no son válidos.");
            return; 
        }

        // 2. VALIDACIÓN DE CUPO (EL PORTERO)
        // Antes de guardar, verificamos si hay lugar en el aula
        if (!hayCupoDisponible(idGrupo)) {
            mostrarError(out, "¡Aula Llena!", "Lo sentimos, el aula asignada a este grupo ha alcanzado su capacidad máxima.");
            return; // Cortamos aquí, no guardamos nada.
        }

        // 3. GUARDAR EN BASE DE DATOS
        try {
            Connection con = Conexion.getConexion();
            
            // La consulta ahora incluye 'dni' e 'id_grupo' en vez de 'nivel'
            String sql = "INSERT INTO estudiantes (nombre, dni, telefono, edad, id_grupo) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setInt(4, edad);
            ps.setInt(5, idGrupo);
            
            ps.executeUpdate();
            con.close();
            
            // Si llegamos aquí, todo salió bien
            mostrarExito(out, nombre, dni);

        } catch (SQLIntegrityConstraintViolationException e) {
            // Este error salta específicamente si el DNI ya existe en la base de datos
            mostrarError(out, "DNI Duplicado", "El estudiante con DNI <b>" + dni + "</b> ya está registrado en el sistema.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError(out, "Error de Base de Datos", e.getMessage());
        }
    }

    // --- FUNCIONES AUXILIARES (Para mantener el código ordenado) ---

    // Verifica en la BD si la cantidad de inscritos es menor a la capacidad del aula
    private boolean hayCupoDisponible(int idGrupo) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            String sql = "SELECT a.capacidad, " +
                         "(SELECT COUNT(*) FROM estudiantes e WHERE e.id_grupo = g.id_grupo) as inscritos " +
                         "FROM grupos g " +
                         "JOIN aulas a ON g.id_aula = a.id_aula " +
                         "WHERE g.id_grupo = ?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idGrupo);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int capacidad = rs.getInt("capacidad");
                int inscritos = rs.getInt("inscritos");
                return inscritos < capacidad; // Devuelve TRUE si hay espacio
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return false; 
    }

    // Pantalla Verde (Éxito)
    private void mostrarExito(PrintWriter out, String nombre, String dni) {
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Registro Exitoso</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex align-items-center justify-content-center' style='height: 100vh;'>");
        out.println("<div class='card shadow p-5 text-center' style='max-width: 500px;'>");
        out.println("<img src='img/logo.jpg' width='180' class='d-block mx-auto mb-4'>");
        out.println("<h1 class='text-success mb-4'>&iexcl;&Eacute;xito!</h1>");
        out.println("<p class='fs-4'>El estudiante <b>" + nombre + "</b> (DNI: " + dni + ") ha sido registrado correctamente.</p>");
        out.println("<a href='registro.html' class='btn btn-primary mt-3'>Registrar Nuevo Alumno</a>");
        out.println("</div></body></html>");
    }

    // Pantalla Roja (Error)
    private void mostrarError(PrintWriter out, String titulo, String mensaje) {
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Error</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex align-items-center justify-content-center' style='height: 100vh;'>");
        out.println("<div class='card shadow p-5 text-center border-danger' style='max-width: 500px;'>");
        out.println("<h1 class='display-1 text-danger'>&#9888;</h1>"); 
        out.println("<h2 class='text-danger mb-3'>" + titulo + "</h2>");
        out.println("<p class='fs-5'>" + mensaje + "</p>");
        out.println("<a href='registro.html' class='btn btn-secondary mt-3'>Volver al formulario</a>");
        out.println("</div></body></html>");
    }
}