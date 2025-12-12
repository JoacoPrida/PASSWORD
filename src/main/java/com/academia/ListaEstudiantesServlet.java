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
        
        // Configuración de codificación
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Lista de Alumnos</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-4'>");
        
        // Título y Botón
        out.println("<div class='d-flex justify-content-between align-items-center mb-4'>");
        out.println("<h2>📋 Planilla de Alumnos</h2>");
        out.println("<a href='registro.html' class='btn btn-success'>+ Nuevo Alumno</a>");
        out.println("</div>");

        out.println("<div class='card shadow'>");
        out.println("<div class='card-body p-0'>"); 
        out.println("<div class='table-responsive'>"); 
        
        // --- CORRECCIÓN DE COLUMNAS ---
        out.println("<table class='table table-striped table-hover mb-0'>");
        out.println("<thead class='table-dark'><tr>");
        out.println("<th>ID</th>");
        out.println("<th>Nombre</th>");
        out.println("<th>DNI</th>");
        out.println("<th>Grupo (Info)</th>"); // Aquí va todo junto
        out.println("<th>Tel&eacute;fono</th>"); // CORREGIDO: Teléfono en su lugar
        out.println("</tr></thead>");
        out.println("<tbody>");

        try {
            Connection con = Conexion.getConexion();
            
            // Traemos el nombre del grupo, los días y el horario
            String sql = "SELECT e.id, e.nombre, e.dni, e.telefono, g.nombre AS nombre_grupo, g.dias, g.horario " +
                         "FROM estudiantes e " +
                         "JOIN grupos g ON e.id_grupo = g.id_grupo " +
                         "ORDER BY e.id DESC"; 
            
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("id") + "</td>");
                out.println("<td class='fw-bold'>" + rs.getString("nombre") + "</td>");
                out.println("<td>" + rs.getString("dni") + "</td>");
                
                // Info del Grupo combinada (Nombre + Horario pequeño abajo)
                String infoGrupo = rs.getString("nombre_grupo") + " <br><small class='text-muted'>" + rs.getString("dias") + " " + rs.getString("horario") + "</small>";
                out.println("<td>" + infoGrupo + "</td>");
                
                // El teléfono cae en la columna correcta ahora (la 5ta)
                out.println("<td>" + rs.getString("telefono") + "</td>");
                out.println("</tr>");
            }
            
            con.close();

        } catch (SQLException e) {
            out.println("<tr><td colspan='5' class='text-danger text-center'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</tbody></table>");
        out.println("</div></div></div>"); 
        out.println("</div></body></html>");
    }
}