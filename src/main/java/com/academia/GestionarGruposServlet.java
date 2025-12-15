package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/configuracion")
public class GestionarGruposServlet extends HttpServlet {

    // MOSTRAR LA PANTALLA (Y AHORA TAMBIÉN PROCESAR ELIMINAR)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // --- CORRECCIÓN: Verificar si hay una orden de eliminar antes de dibujar ---
        String accion = request.getParameter("accion");
        if (accion != null) {
            procesarAcciones(request, response);
            return; // ¡Importante! Detenemos aquí para no dibujar el HTML encima de la redirección
        }
        // --------------------------------------------------------------------------

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Configurar Grupos</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-5'>");
        
        out.println("<div class='d-flex justify-content-between align-items-center mb-4'>");
        out.println("<h2>&#9881; Configuración de Grupos</h2>");
        out.println("<a href='formulario-registro' class='btn btn-outline-secondary'>&larr; Volver al Registro</a>");
        out.println("</div>");

        out.println("<div class='row'>");

        // COLUMNA IZQUIERDA: CREAR
        out.println("<div class='col-md-4'>");
        out.println("<div class='card shadow-sm'>");
        out.println("<div class='card-header bg-primary text-white'>Nuevo Grupo</div>");
        out.println("<div class='card-body'>");
        
        out.println("<form action='configuracion' method='post'>");
        out.println("<input type='hidden' name='accion' value='crear'>");
        
        out.println("<div class='mb-3'><label>Nombre del Grupo:</label>");
        out.println("<input type='text' name='nombre' class='form-control' required placeholder='Ej: Advanced 1'></div>");
        
        out.println("<div class='mb-3'><label>Días:</label>");
        out.println("<input type='text' name='dias' class='form-control' required placeholder='Ej: Lun y Mie'></div>");
        
        out.println("<div class='mb-3'><label>Horario:</label>");
        out.println("<input type='text' name='horario' class='form-control' required placeholder='Ej: 18:00 a 19:30'></div>");
        
        out.println("<div class='mb-3'><label>Asignar Aula:</label>");
        out.println("<select name='id_aula' class='form-select' required>");
        
        try {
            Connection con = Conexion.getConexion();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM aulas");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                out.println("<option value='"+rs.getInt("id_aula")+"'>"+rs.getString("nombre")+" (Cap: "+rs.getInt("capacidad")+")</option>");
            }
            con.close();
        } catch(Exception e) { e.printStackTrace(); }
        
        out.println("</select></div>");
        
        out.println("<button type='submit' class='btn btn-primary w-100'>Crear Grupo</button>");
        out.println("</form>");
        out.println("</div></div></div>");

        // COLUMNA DERECHA: LISTA
        out.println("<div class='col-md-8'>");
        out.println("<div class='card shadow-sm'>");
        out.println("<div class='card-header'>Grupos Activos</div>");
        out.println("<div class='card-body p-0'>");
        out.println("<table class='table table-striped mb-0'>");
        out.println("<thead><tr><th>ID</th><th>Nombre</th><th>Horario</th><th>Aula</th><th>Acción</th></tr></thead>");
        out.println("<tbody>");
        
        try {
            Connection con = Conexion.getConexion();
            // Query para ver grupos y contar alumnos
            String sql = "SELECT g.*, a.nombre as nombre_aula, " +
                         "(SELECT COUNT(*) FROM estudiantes e WHERE e.id_grupo = g.id_grupo) as total_alumnos " +
                         "FROM grupos g JOIN aulas a ON g.id_aula = a.id_aula ORDER BY g.nombre";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                int idGrupo = rs.getInt("id_grupo");
                int totalAlumnos = rs.getInt("total_alumnos");
                
                out.println("<tr>");
                out.println("<td>"+ idGrupo +"</td>");
                out.println("<td>"+rs.getString("nombre")+"</td>");
                out.println("<td><small>"+rs.getString("dias")+"<br>"+rs.getString("horario")+"</small></td>");
                out.println("<td>"+rs.getString("nombre_aula")+"</td>");
                out.println("<td>");
                
                // Mensaje de advertencia inteligente
                String mensaje = "¿Borrar este grupo?";
                if (totalAlumnos > 0) {
                    mensaje = "¡CUIDADO! Hay " + totalAlumnos + " alumnos en este grupo. Si aceptas, quedarán SIN GRUPO (desvinculados). ¿Continuar?";
                }
                
                out.println("<a href='configuracion?accion=eliminar&id="+idGrupo+"' class='btn btn-danger btn-sm' onclick='return confirm(\""+mensaje+"\")'>&#128465;</a>");
                out.println("</td>");
                out.println("</tr>");
            }
            con.close();
        } catch(Exception e) { out.println("<tr><td colspan='5'>Error: "+e.getMessage()+"</td></tr>"); }
        
        out.println("</tbody></table>");
        out.println("</div></div></div>");
        
        out.println("</div></div></body></html>");
    }

    // PROCESAR FORMULARIO (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        procesarAcciones(request, response);
    }
    
    // LÓGICA COMPARTIDA (Aquí ocurre la magia de borrar y crear)
    private void procesarAcciones(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");
        
        try {
            Connection con = Conexion.getConexion();
            
            if ("crear".equals(accion)) {
                String nombre = request.getParameter("nombre");
                String dias = request.getParameter("dias");
                String horario = request.getParameter("horario");
                int idAula = Integer.parseInt(request.getParameter("id_aula"));
                
                String sql = "INSERT INTO grupos (nombre, dias, horario, id_aula) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, nombre);
                ps.setString(2, dias);
                ps.setString(3, horario);
                ps.setInt(4, idAula);
                ps.executeUpdate();
                
            } else if ("eliminar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                
                // PASO 1: Desvincular alumnos (poner id_grupo en NULL)
                // Esto evita el error de "Foreign Key Constraint"
                String sqlLiberar = "UPDATE estudiantes SET id_grupo = NULL WHERE id_grupo = ?";
                PreparedStatement psLib = con.prepareStatement(sqlLiberar);
                psLib.setInt(1, id);
                psLib.executeUpdate();
                
                // PASO 2: Borrar el grupo
                String sqlBorrar = "DELETE FROM grupos WHERE id_grupo = ?";
                PreparedStatement psBorrar = con.prepareStatement(sqlBorrar);
                psBorrar.setInt(1, id);
                psBorrar.executeUpdate();
            }
            con.close();
        } catch (Exception e) { 
            e.printStackTrace(); // Mira la consola si algo falla
        }
        
        // Recargar la página limpia
        response.sendRedirect("configuracion");
    }
}