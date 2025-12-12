package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/RegistrarEstudianteServlet")
public class RegistrarEstudianteServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String nombre = request.getParameter("nombre_alumno");
        String dni = request.getParameter("dni_alumno");
        String telefono = request.getParameter("telefono_alumno");
        
        int edad = 0;
        int idGrupo = 0;
        try {
            edad = Integer.parseInt(request.getParameter("edad_alumno"));
            idGrupo = Integer.parseInt(request.getParameter("id_grupo"));
        } catch (NumberFormatException e) {
            mostrarError(out, "Datos inv&aacute;lidos", "Revisa los n&uacute;meros.");
            return; 
        }

        // --- EL PORTERO (Validación) ---
        if (!hayCupoDisponible(idGrupo)) {
            // Mensaje corregido con acentos HTML
            mostrarError(out, "&iexcl;Aula Llena!", "Lo sentimos, el aula asignada alcanz&oacute; su capacidad m&aacute;xima.");
            return; 
        }

        try {
            Connection con = Conexion.getConexion();
            String sql = "INSERT INTO estudiantes (nombre, dni, telefono, edad, id_grupo) VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setInt(4, edad);
            ps.setInt(5, idGrupo);
            
            ps.executeUpdate();
            con.close();
            
            mostrarExito(out, nombre, dni);

        } catch (SQLIntegrityConstraintViolationException e) {
            mostrarError(out, "DNI Duplicado", "El DNI <b>" + dni + "</b> ya est&aacute; registrado.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError(out, "Error de Base de Datos", e.getMessage());
        }
    }

    // --- FUNCIONES AUXILIARES ---

    private boolean hayCupoDisponible(int idGrupo) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            
            // Cuenta cuántos alumnos hay HOY en ese grupo
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
                
                // --- CHIVATO DE CONSOLA ---
                // Mira la terminal negra de VS Code cuando registres, aparecerá esto:
                System.out.println("REVISANDO CUPO -> Grupo ID: " + idGrupo + " | Inscritos: " + inscritos + " | Capacidad: " + capacidad);
                
                // Si hay MENOS inscritos que la capacidad, entra.
                // Ejemplo: Inscritos 6, Capacidad 7 -> (6 < 7) es VERDADERO -> Pasa.
                // Ejemplo: Inscritos 7, Capacidad 7 -> (7 < 7) es FALSO -> Bloquea.
                return inscritos < capacidad;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
        return false; 
    }

    private void mostrarExito(PrintWriter out, String nombre, String dni) {
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Registro Exitoso</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex align-items-center justify-content-center' style='height: 100vh;'>");
        out.println("<div class='card shadow p-5 text-center' style='max-width: 500px;'>");
        out.println("<img src='img/logo.jpg' width='180' class='d-block mx-auto mb-4'>");
        // Acentos corregidos: &iexcl; (¡) &Eacute; (É)
        out.println("<h1 class='text-success mb-4'>&iexcl;&Eacute;xito!</h1>");
        out.println("<p class='fs-4'>El estudiante <b>" + nombre + "</b> (DNI: " + dni + ") fue registrado.</p>");
        out.println("<a href='registro.html' class='btn btn-primary mt-3'>Registrar Nuevo Alumno</a>");
        // Botón extra para ir a la lista
        out.println("<a href='lista' class='btn btn-outline-dark mt-3 d-block'>Ver Planilla</a>");
        out.println("</div></body></html>");
    }

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