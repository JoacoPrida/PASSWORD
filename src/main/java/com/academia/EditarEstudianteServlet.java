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
        
        // --- NUEVAS VARIABLES ---
        String colegio = "";
        String gradoAnio = "";
        String descripcion = "";

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
                telefono = rs.getString("telefono"); 
                edad = rs.getInt("edad");            
                idGrupoActual = rs.getInt("id_grupo");
                
                // --- RECUPERAR NUEVOS DATOS ---
                colegio = rs.getString("colegio");
                gradoAnio = rs.getString("grado_anio");
                descripcion = rs.getString("descripcion");
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        // Si los campos opcionales son null, ponemos cadena vacía para que no escriba "null" en la caja
        if (telefono == null) telefono = "";
        if (colegio == null) colegio = "";
        if (gradoAnio == null) gradoAnio = "";
        if (descripcion == null) descripcion = "";

        // DIBUJAR FORMULARIO
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>"); 
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light d-flex justify-content-center align-items-center' style='min-height: 100vh'>");
        
        out.println("<div class='container py-5'>"); // Un contenedor para mejor margen en móviles
        out.println("<div class='card shadow mx-auto' style='max-width: 600px;'>");
        out.println("<div class='card-body p-4'>");
        
        out.println("<h3 class='mb-4 text-center text-primary'>&#9999; Editar Estudiante</h3>");
        
        out.println("<form action='editar' method='post'>");
        out.println("<input type='hidden' name='id' value='" + id + "'>");

        // 1. NOMBRE
        out.println("<div class='mb-3'><label class='form-label'>Nombre:</label>");
        out.println("<input type='text' name='nombre' class='form-control' value='" + nombre + "' required></div>");

        // 2. DNI
        out.println("<div class='mb-3'><label class='form-label'>DNI / Documento:</label>");
        out.println("<input type='text' name='dni' class='form-control' value='" + dni + "' required></div>");

        // 3. TELÉFONO
        out.println("<div class='mb-3'><label class='form-label'>Tel&eacute;fono:</label>");
        out.println("<input type='text' name='telefono' class='form-control' value='" + telefono + "'></div>");

        // 4. EDAD
        out.println("<div class='mb-3'><label class='form-label'>Edad:</label>");
        out.println("<input type='number' name='edad' class='form-control' value='" + edad + "'></div>");
        
        // --- 5. COLEGIO (NUEVO) ---
        out.println("<div class='mb-3'><label class='form-label'>Colegio / Escuela:</label>");
        out.println("<input type='text' name='colegio' class='form-control' value='" + colegio + "'></div>");

        // --- 6. GRADO/AÑO (NUEVO) ---
        out.println("<div class='mb-3'><label class='form-label'>Grado / Año:</label>");
        out.println("<input type='text' name='grado_anio' class='form-control' value='" + gradoAnio + "'></div>");

        // --- 7. DESCRIPCIÓN (NUEVO) ---
        out.println("<div class='mb-3'><label class='form-label'>Descripción / Observaciones:</label>");
        out.println("<textarea name='descripcion' class='form-control' rows='3'>" + descripcion + "</textarea></div>");

        // 8. GRUPO
        out.println("<div class='mb-3'><label class='form-label'>Cambiar de Grupo (Actual ID: " + idGrupoActual + "):</label>");
        out.println("<select name='id_grupo' class='form-select' required>");
        out.println("<option value='' disabled>-- Seleccione Nuevo Grupo --</option>");
        
        // Nota: Idealmente esta lista debería venir de la BD como en el registro, 
        // pero mantengo tu estructura estática original por ahora.
        out.println("<optgroup label='Adultos / Inicial'><option value='1'>Beginners</option><option value='2'>PreIntermediate</option><option value='3'>Intermediate (L)</option><option value='4'>Intermediate (M)</option><option value='5'>Intermediate (J)</option><option value='6'>Upper</option></optgroup>");
        out.println("<optgroup label='Youngsters'><option value='7'>Y1</option><option value='8'>Y2</option><option value='9'>Y3</option><option value='10'>Y4</option></optgroup>");
        out.println("<optgroup label='Intermediate Teens'><option value='11'>Inter 1 (L)</option><option value='12'>Inter 1 (M)</option><option value='13'>Inter 2 (L)</option><option value='14'>Inter 2 (M)</option></optgroup>");
        out.println("<optgroup label='Ex&aacute;menes'><option value='15'>First (L 16:30)</option><option value='16'>First (L 18:30)</option><option value='17'>First (M)</option></optgroup>");
        
        out.println("</select>");
        out.println("</div>");

        out.println("<div class='d-grid gap-2 mt-4'>");
        out.println("<button type='submit' class='btn btn-primary btn-lg'>Guardar Cambios</button>");
        out.println("<a href='lista' class='btn btn-secondary'>Cancelar</a>");
        out.println("</div>");
        
        out.println("</form>");
        out.println("</div></div></div></body></html>");
    }
    

    // 2. GUARDAR LOS CAMBIOS (POST)

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        // Recibimos los datos originales
        int id = Integer.parseInt(request.getParameter("id"));
        String nombre = request.getParameter("nombre");
        String dni = request.getParameter("dni");
        String telefono = request.getParameter("telefono");
        int idGrupo = Integer.parseInt(request.getParameter("id_grupo"));
        
        // --- RECIBIMOS LOS NUEVOS DATOS ---
        String colegio = request.getParameter("colegio");
        String gradoAnio = request.getParameter("grado_anio");
        String descripcion = request.getParameter("descripcion");
        
        // Tratamiento especial para EDAD (por si lo dejan vacío o ponen texto)
        int edad = 0;
        try {
            edad = Integer.parseInt(request.getParameter("edad"));
        } catch (NumberFormatException e) {
            edad = 0;
        }

        try {
            Connection con = Conexion.getConexion();
            
            // SQL ACTUALIZADO: Agregamos colegio, grado_anio y descripcion al UPDATE
            String sql = "UPDATE estudiantes SET nombre=?, dni=?, telefono=?, edad=?, id_grupo=?, colegio=?, grado_anio=?, descripcion=? WHERE id=?";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, dni);
            ps.setString(3, telefono); 
            ps.setInt(4, edad);        
            ps.setInt(5, idGrupo);
            
            // --- ASIGNAMOS LOS NUEVOS VALORES ---
            ps.setString(6, colegio);
            ps.setString(7, gradoAnio);
            ps.setString(8, descripcion);
            
            // El ID ahora es el parámetro número 9 (porque está al final del WHERE)
            ps.setInt(9, id);
            
            ps.executeUpdate();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Al terminar, volvemos a la lista general
        response.sendRedirect("lista");
    }
}