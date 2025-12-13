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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String id = request.getParameter("id");
        
        // Configuración de caracteres
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Variables para guardar lo que traemos de la BD
        String nombre = "";
        String dni = "";
        String telefono = "";
        int edad = 0;
        int idGrupoActual = 0;

        try {
            Connection con = Conexion.getConexion();
            // Traemos TODOS los datos (*)
            String sql = "SELECT * FROM estudiantes WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                nombre = rs.getString("nombre");
                dni = rs.getString("dni");
                telefono = rs.getString("telefono"); // Recuperamos teléfono
                edad = rs.getInt("edad");            // Recuperamos edad
                idGrupoActual = rs.getInt("id_grupo");
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        // Si el teléfono es null (vacío en BD), ponemos cadena vacía para que no escriba "null" en la caja
        if (telefono == null) telefono = "";

        // DIBUJAR FORMULARIO
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>"); 
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex justify-content-center align-items-center' style='min-height: 100vh'>");
        
        out.println("<div class='card shadow p-4' style='width: 500px; max-width: 95%;'>");
        out.println("<h3 class='mb-4'>&#9999; Editar Estudiante</h3>");
        
        out.println("<form action='editar' method='post'>");
        out.println("<input type='hidden' name='id' value='" + id + "'>");

        // 1. NOMBRE
        out.println("<div class='mb-3'><label>Nombre:</label>");
        out.println("<input type='text' name='nombre' class='form-control' value='" + nombre + "' required></div>");

        // 2. DNI
        out.println("<div class='mb-3'><label>DNI / Documento:</label>");
        out.println("<input type='text' name='dni' class='form-control' value='" + dni + "' required></div>");

        // 3. TELÉFONO (Ahora editable)
        out.println("<div class='mb-3'><label>Tel&eacute;fono:</label>");
        out.println("<input type='text' name='telefono' class='form-control' value='" + telefono + "'></div>");

        // 4. EDAD (Ahora editable)
        out.println("<div class='mb-3'><label>Edad:</label>");
        out.println("<input type='number' name='edad' class='form-control' value='" + edad + "'></div>");

        // 5. GRUPO
        out.println("<div class='mb-3'><label>Cambiar de Grupo (Actual ID: " + idGrupoActual + "):</label>");
        out.println("<select name='id_grupo' class='form-select' required>");
        out.println("<option value='' disabled>-- Seleccione Nuevo Grupo --</option>");
        
        // --- LISTA DE GRUPOS ---
        out.println("<optgroup label='Adultos / Inicial'><option value='1'>Beginners</option><option value='2'>PreIntermediate</option><option value='3'>Intermediate (L)</option><option value='4'>Intermediate (M)</option><option value='5'>Intermediate (J)</option><option value='6'>Upper</option></optgroup>");
        out.println("<optgroup label='Youngsters'><option value='7'>Y1</option><option value='8'>Y2</option><option value='9'>Y3</option><option value='10'>Y4</option></optgroup>");
        out.println("<optgroup label='Intermediate Teens'><option value='11'>Inter 1 (L)</option><option value='12'>Inter 1 (M)</option><option value='13'>Inter 2 (L)</option><option value='14'>Inter 2 (M)</option></optgroup>");
        out.println("<optgroup label='Ex&aacute;menes'><option value='15'>First (L 16:30)</option><option value='16'>First (L 18:30)</option><option value='17'>First (M)</option></optgroup>");
        
        out.println("</select>");
        out.println("</div>");

        out.println("<button type='submit' class='btn btn-primary w-100'>Guardar Cambios</button>");
        out.println("<a href='lista' class='btn btn-secondary w-100 mt-2'>Cancelar</a>");
        out.println("</form></div></body></html>");
    }

    // 2. GUARDAR LOS CAMBIOS (POST)
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // Recibimos TODOS los datos
        int id = Integer.parseInt(request.getParameter("id"));
        String nombre = request.getParameter("nombre");
        String dni = request.getParameter("dni");
        String telefono = request.getParameter("telefono");
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));
        
        // Tratamiento especial para EDAD (por si lo dejan vacío al editar)
        int edad = 0;
        try {
            edad = Integer.parseInt(request.getParameter("edad"));
        } catch (NumberFormatException e) {
            edad = 0;
        }

        try {
            Connection con = Conexion.getConexion();
            
            // SQL ACTUALIZADO: Ahora incluimos telefono y edad en el UPDATE
            String sql = "UPDATE estudiantes SET nombre=?, dni=?, telefono=?, edad=?, id_grupo=? WHERE id=?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono); // Guardamos teléfono nuevo
            ps.setInt(4, edad);        // Guardamos edad nueva
            ps.setInt(5, idGrupo);
            ps.setInt(6, id);
            
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Volvemos a la lista
        response.sendRedirect("lista");
    }
}