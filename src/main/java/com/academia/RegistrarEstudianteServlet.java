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
        
        // Configuración para tildes y eñes
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 1. RECIBIMOS LOS DATOS EN BRUTO (TEXTO)
        String nombre = request.getParameter("nombre_alumno");
        String dni = request.getParameter("dni_alumno");
        String telefono = request.getParameter("telefono_alumno");
        String edadTexto = request.getParameter("edad_alumno"); 
        String idGrupoTexto = request.getParameter("id_grupo");

        // --- NUEVOS CAMPOS ---
        String colegio = request.getParameter("colegio");
        String gradoAnio = request.getParameter("grado_anio");
        String descripcion = request.getParameter("descripcion");

        // 2. CONVERSIÓN SEGURA
        int idGrupo = 0;
        int edad = 0; // Por defecto asumimos 0 si no escriben nada

        // A) Validar Grupo (Este SÍ es obligatorio)
        try {
            if (idGrupoTexto == null || idGrupoTexto.isEmpty()) {
                throw new Exception("Grupo vacío");
            }
            idGrupo = Integer.parseInt(idGrupoTexto);
        } catch (Exception e) {
            mostrarError(out, "Faltan Datos", "Por favor selecciona un grupo de la lista.");
            return; // Cortamos aquí si no hay grupo
        }

        // B) Validar Edad (Este es OPCIONAL - Si viene vacío, no pasa nada)
        if (edadTexto != null && !edadTexto.trim().isEmpty()) {
            try {
                edad = Integer.parseInt(edadTexto);
            } catch (NumberFormatException e) {
                edad = 0; // Si escriben letras o error, lo dejamos en 0
            }
        }
        
        // C) Limpiar nulos para los campos de texto opcionales
        if (colegio == null) colegio = "";
        if (gradoAnio == null) gradoAnio = "";
        if (descripcion == null) descripcion = "";

        // 3. EL PORTERO (Validar Cupo)
        if (!hayCupoDisponible(idGrupo)) {
            mostrarError(out, "¡Aula Llena!", "Lo sentimos, el grupo seleccionado ya está completo.");
            return; 
        }

        // 4. GUARDAR EN LA BASE DE DATOS
        try {
            Connection con = Conexion.getConexion();
            
            // SQL ACTUALIZADO CON LOS NUEVOS CAMPOS
            String sql = "INSERT INTO estudiantes (nombre, dni, telefono, edad, id_grupo, colegio, grado_anio, descripcion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono);
            ps.setInt(4, edad); 
            ps.setInt(5, idGrupo);
            
            // Asignamos los nuevos valores
            ps.setString(6, colegio);
            ps.setString(7, gradoAnio);
            ps.setString(8, descripcion);
            
            ps.executeUpdate();
            con.close();
            
            mostrarExito(out, nombre, dni);

        } catch (SQLIntegrityConstraintViolationException e) {
            mostrarError(out, "DNI Duplicado", "El DNI <b>" + dni + "</b> ya está registrado en el sistema.");
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError(out, "Error Técnico", e.getMessage());
        }
    }

    // --- FUNCIONES AUXILIARES ---

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
        out.println("<h1 class='text-success mb-4'>¡Éxito!</h1>");
        out.println("<p class='fs-4'>El estudiante <b>" + nombre + "</b><br>ha sido registrado correctamente.</p>");
        out.println("<a href='formulario-registro' class='btn btn-primary mt-3'>Registrar Nuevo Alumno</a>");
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
        out.println("<a href='formulario-registro' class='btn btn-secondary mt-3'>Volver al formulario</a>");
        out.println("</div></body></html>");
    }
}