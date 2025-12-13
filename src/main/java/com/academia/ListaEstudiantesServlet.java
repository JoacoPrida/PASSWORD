package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/lista")
public class ListaEstudiantesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Lista de Alumnos</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-4'>");
        out.println("<div class='d-flex justify-content-between align-items-center mb-4'>");
        out.println("<h2>📋 Planilla de Alumnos</h2>");
        out.println("<a href='registro.html' class='btn btn-success'>+ Nuevo Alumno</a>");
        out.println("</div>");

        out.println("<div class='card shadow'>");
        out.println("<div class='card-body p-0'>"); 
        out.println("<div class='table-responsive'>"); 
        
        out.println("<table class='table table-striped table-hover mb-0'>");
        out.println("<thead class='table-dark'><tr>");
        out.println("<th>ID</th>");
        out.println("<th>Nombre</th>");
        out.println("<th>Grupo / Horario</th>");
        out.println("<th>Teléfono</th>");
        out.println("<th class='text-center'>Acciones</th>"); // <--- NUEVA COLUMNA
        out.println("</tr></thead>");
        out.println("<tbody>");

        try {
            Connection con = Conexion.getConexion();
            // Traemos datos
            String sql = "SELECT e.id, e.nombre, e.telefono, g.nombre AS nombre_grupo, g.dias, g.horario " +
                         "FROM estudiantes e " +
                         "JOIN grupos g ON e.id_grupo = g.id_grupo " +
                         "ORDER BY e.id DESC"; 
            
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                
                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td class='fw-bold'>" + nombre + "</td>");
                
                String infoGrupo = rs.getString("nombre_grupo") + " <br><small class='text-muted'>" + rs.getString("dias") + " " + rs.getString("horario") + "</small>";
                out.println("<td>" + infoGrupo + "</td>");
                out.println("<td>" + rs.getString("telefono") + "</td>");
                
                // --- BOTONES DE ACCIÓN ---
                out.println("<td class='text-center'>");
                // Botón Editar (Lleva a la pantalla de edición con el ID del alumno)
                out.println("<a href='editar?id=" + id + "' class='btn btn-sm btn-primary me-2'>✏️ Editar</a>");
                // Botón Eliminar (Pide confirmación antes de borrar)
                out.println("<a href='eliminar?id=" + id + "' class='btn btn-sm btn-danger' onclick='return confirm(\"¿Seguro que quieres borrar a " + nombre + "?\")'>🗑️</a>");
                out.println("</td>");
                out.println("</tr>");
            }
            con.close();

        } catch (SQLException e) {
            out.println("<tr><td colspan='5' class='text-danger'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</tbody></table>");
        out.println("</div></div></div></div></body></html>");
    }
}