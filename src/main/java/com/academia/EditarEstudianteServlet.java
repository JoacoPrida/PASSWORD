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

    // 1. MOSTRAR EL FORMULARIO (GET)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Variables para guardar lo que traemos de la BD
        String nombre = "";
        String dni = "";
        String telefono = "";
        int edad = 0;
        int idGrupoActual = 0;
        
        String colegio = "";
        String gradoAnio = "";
        String descripcion = "";

        // --- PASO 1: TRAER LOS DATOS DEL ESTUDIANTE ---
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT * FROM estudiantes WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                nombre = rs.getString("nombre");
                dni = rs.getString("dni");
                telefono = rs.getString("telefono"); 
                edad = rs.getInt("edad");            
                idGrupoActual = rs.getInt("id_grupo");
                
                colegio = rs.getString("colegio");
                gradoAnio = rs.getString("grado_anio");
                descripcion = rs.getString("descripcion");
            }
            con.close(); // Cerramos esta conexión rápida
        } catch (Exception e) { e.printStackTrace(); }

        if (telefono == null) telefono = "";
        if (colegio == null) colegio = "";
        if (gradoAnio == null) gradoAnio = "";
        if (descripcion == null) descripcion = "";

        // --- DIBUJAR FORMULARIO ---
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>"); 
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex justify-content-center align-items-center' style='min-height: 100vh'>");
        
        out.println("<div class='container py-5'>");
        out.println("<div class='card shadow mx-auto' style='max-width: 600px;'>");
        out.println("<div class='card-body p-4'>");
        
        out.println("<h3 class='mb-4 text-center text-primary'>&#9999; Editar Estudiante</h3>");
        
        out.println("<form action='editar' method='post'>");
        out.println("<input type='hidden' name='id' value='" + id + "'>");

        // CAMPOS DEL FORMULARIO
        out.println("<div class='mb-3'><label class='form-label'>Nombre:</label>");
        out.println("<input type='text' name='nombre' class='form-control' value='" + nombre + "' required></div>");

        out.println("<div class='mb-3'><label class='form-label'>DNI / Documento:</label>");
        out.println("<input type='text' name='dni' class='form-control' value='" + dni + "' required></div>");

        out.println("<div class='mb-3'><label class='form-label'>Tel&eacute;fono:</label>");
        out.println("<input type='text' name='telefono' class='form-control' value='" + telefono + "'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Edad:</label>");
        out.println("<input type='number' name='edad' class='form-control' value='" + edad + "'></div>");
        
        out.println("<div class='mb-3'><label class='form-label'>Colegio / Escuela:</label>");
        out.println("<input type='text' name='colegio' class='form-control' value='" + colegio + "'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Grado / Año:</label>");
        out.println("<input type='text' name='grado_anio' class='form-control' value='" + gradoAnio + "'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Descripción / Observaciones:</label>");
        out.println("<textarea name='descripcion' class='form-control' rows='3'>" + descripcion + "</textarea></div>");

        // --- SELECCION DE GRUPO DINÁMICA (CONEXION A BD) ---
        out.println("<div class='mb-3'><label class='form-label'>Grupo Asignado:</label>");
        out.println("<select name='id_grupo' class='form-select' required>");
        out.println("<option value='' disabled>-- Seleccione Grupo --</option>");
        
        try {
            Connection conGrupos = Conexion.getConexion();
            // Misma lógica que en el registro: LEFT JOIN para traer nombre de aula
            String sqlGrupos = "SELECT g.id_grupo, g.nombre, g.dias, g.horario, IFNULL(a.nombre, 'Sin Aula') as n_aula FROM grupos g LEFT JOIN aulas a ON g.id_aula = a.id_aula ORDER BY g.nombre";
            PreparedStatement psGrupos = conGrupos.prepareStatement(sqlGrupos);
            ResultSet rsGrupos = psGrupos.executeQuery();
            
            while(rsGrupos.next()){
                int idG = rsGrupos.getInt("id_grupo");
                String textoOpcion = rsGrupos.getString("nombre") + " - " + rsGrupos.getString("dias") + " (" + rsGrupos.getString("horario") + ") [" + rsGrupos.getString("n_aula") + "]";
                
                // MAGIA: Si el ID de este grupo coincide con el idGrupoActual del alumno, le ponemos 'selected'
                String seleccionado = (idG == idGrupoActual) ? "selected" : "";
                
                out.println("<option value='" + idG + "' " + seleccionado + ">" + textoOpcion + "</option>");
            }
            conGrupos.close();
        } catch (Exception e) {
            e.printStackTrace();
            out.println("<option disabled>Error cargando grupos</option>");
        }
        
        out.println("</select></div>");
        // ----------------------------------------------------

        out.println("<div class='d-grid gap-2 mt-4'>");
        out.println("<button type='submit' class='btn btn-primary btn-lg'>Guardar Cambios</button>");
        out.println("<a href='lista' class='btn btn-secondary'>Cancelar</a>");
        out.println("</div>");
        
        out.println("</form>");
        out.println("</div></div></div></body></html>");
    }

    // 2. GUARDAR LOS CAMBIOS (POST) - (Esto queda igual que antes)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        int id = Integer.parseInt(request.getParameter("id"));
        String nombre = request.getParameter("nombre");
        String dni = request.getParameter("dni");
        String telefono = request.getParameter("telefono");
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));
        
        String colegio = request.getParameter("colegio");
        String gradoAnio = request.getParameter("grado_anio");
        String descripcion = request.getParameter("descripcion");
        
        int edad = 0;
        try {
            edad = Integer.parseInt(request.getParameter("edad"));
        } catch (NumberFormatException e) { edad = 0; }

        try {
            Connection con = Conexion.getConexion();
            String sql = "UPDATE estudiantes SET nombre=?, dni=?, telefono=?, edad=?, id_grupo=?, colegio=?, grado_anio=?, descripcion=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono); 
            ps.setInt(4, edad);        
            ps.setInt(5, idGrupo);
            ps.setString(6, colegio);
            ps.setString(7, gradoAnio);
            ps.setString(8, descripcion);
            ps.setInt(9, id);
            
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        response.sendRedirect("lista");
    }
}