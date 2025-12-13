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

@WebServlet("/editar")
public class EditarEstudianteServlet extends HttpServlet {

    // 1. MOSTRAR EL FORMULARIO
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        
        // Configuración UTF-8
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String nombre = "";
        String dni = "";
        int idGrupoActual = 0;

        try {
            Connection con = Conexion.getConexion();
            // Solo traemos nombre, dni y grupo (el teléfono no lo editaremos)
            String sql = "SELECT * FROM estudiantes WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                nombre = rs.getString("nombre");
                dni = rs.getString("dni");
                idGrupoActual = rs.getInt("id_grupo");
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>"); // Importante para tildes
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex justify-content-center align-items-center' style='min-height: 100vh'>");
        
        out.println("<div class='card shadow p-4' style='width: 500px'>");
        out.println("<h3 class='mb-4'>✏️ Editar Estudiante</h3>");
        
        out.println("<form action='editar' method='post'>");
        out.println("<input type='hidden' name='id' value='" + id + "'>");

        out.println("<div class='mb-3'><label>Nombre:</label>");
        out.println("<input type='text' name='nombre' class='form-control' value='" + nombre + "'></div>");

        // --- AQUÍ ESTABA EL ERROR ---
        // 1. Cambié la etiqueta visual a "DNI"
        // 2. Cambié el name='Documento' a name='dni' para que coincida con el doPost
        out.println("<div class='mb-3'><label>DNI / Documento:</label>");
        out.println("<input type='text' name='dni' class='form-control' value='" + dni + "'></div>");

        out.println("<div class='mb-3'><label>Cambiar de Grupo:</label>");
        out.println("<select name='id_grupo' class='form-select' required>");
        out.println("<option value='' disabled>-- Seleccione Nuevo Grupo --</option>");
        
        // Tus grupos
        out.println("<optgroup label='Adultos'><option value='1'>Beginners</option><option value='2'>Pre-Intermediate</option><option value='3'>Intermediate (Lun)</option><option value='4'>Intermediate (Mar)</option><option value='5'>Intermediate (Jue)</option><option value='6'>Upper</option></optgroup>");
        out.println("<optgroup label='Youngsters'><option value='7'>Y1</option><option value='8'>Y2</option><option value='9'>Y3</option><option value='10'>Y4</option></optgroup>");
        out.println("<optgroup label='Teens'><option value='11'>Inter 1 (L)</option><option value='12'>Inter 1 (M)</option><option value='13'>Inter 2 (L)</option><option value='14'>Inter 2 (M)</option></optgroup>");
        out.println("<optgroup label='Examenes'><option value='15'>First (L 16:30)</option><option value='16'>First (L 18:30)</option><option value='17'>First (M)</option></optgroup>");
        
        out.println("</select>");
        out.println("<div class='form-text'>Grupo actual ID: " + idGrupoActual + "</div>");
        out.println("</div>");

        out.println("<button type='submit' class='btn btn-primary w-100'>Guardar Cambios</button>");
        out.println("<a href='lista' class='btn btn-secondary w-100 mt-2'>Cancelar</a>");
        out.println("</form></div></body></html>");
    }

    // 2. PROCESAR CAMBIOS
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        int id = Integer.parseInt(request.getParameter("id"));
        String nombre = request.getParameter("nombre");
        // Ahora sí va a funcionar porque en el HTML le pusimos name='dni'
        String dni = request.getParameter("dni"); 
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));

        try {
            Connection con = Conexion.getConexion();
            
            // SQL Correcto: Actualizamos nombre, DNI y grupo. (El teléfono queda igual)
            String sql = "UPDATE estudiantes SET nombre=?, dni=?, id_grupo=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setInt(3, idGrupo);
            ps.setInt(4, id);
            
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        response.sendRedirect("lista");
    }
}