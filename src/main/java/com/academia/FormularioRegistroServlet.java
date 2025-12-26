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

@WebServlet("/formulario-registro")
public class FormularioRegistroServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // COPIAMOS EL HTML ORIGINAL PERO AHORA DENTRO DE JAVA
        out.println("<!DOCTYPE html><html lang='es'><head>");
        out.println("<meta charset='UTF-8'><title>Registro de Estudiante</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-5' style='max-width: 600px;'>");
        out.println("<div class='card shadow'><div class='card-body p-5'>");
        
        // --- CAMBIO: FOTOS A LOS COSTADOS DEL LOGO ---
        out.println("<div class='d-flex justify-content-center align-items-center mb-4 gap-3'>");
        
        // Foto Izquierda
        out.println("<img src='img/fotoMa.jpg' width='100' class='rounded shadow' alt='Foto'>"); 
        
        // Logo Central
        out.println("<img src='img/logo.jpg' alt='Logo Password' width='200' class='img-fluid'>");
        
        // Foto Derecha
        out.println("<img src='img/fotoMa.jpg' width='100' class='rounded shadow' alt='Foto'>");
        
        out.println("</div>");
        // ---------------------------------------------

        out.println("<h3 class='text-center mb-4 text-primary'>Nuevo Estudiante</h3>");
        
        out.println("<form action='RegistrarEstudianteServlet' method='post'>");
        
        out.println("<div class='mb-3'><label class='form-label'>Nombre Completo:</label>");
        out.println("<input type='text' name='nombre_alumno' class='form-control' required placeholder='Ej: Joaquin Prida'></div>");

        out.println("<div class='mb-3'><label class='form-label'>DNI / Documento:</label>");
        out.println("<input type='text' name='dni_alumno' class='form-control' required placeholder='Sin puntos'></div>");

        // --- AQUÍ OCURRE LA MAGIA DINÁMICA ---
        out.println("<div class='mb-3'><label class='form-label'>Asignar Grupo y Horario:</label>");
        out.println("<select name='id_grupo' class='form-select' required>");
        out.println("<option value='' selected disabled>-- Seleccione el grupo --</option>");
        
        try {
            Connection con = Conexion.getConexion();
            // Traemos los grupos y también el nombre del aula para mostrarlo
            String sql = "SELECT g.*, a.nombre as n_aula FROM grupos g JOIN aulas a ON g.id_aula = a.id_aula ORDER BY g.nombre";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()){
                // Creamos una <option> por cada grupo en la base de datos
                String textoOpcion = rs.getString("nombre") + " - " + rs.getString("dias") + " (" + rs.getString("horario") + ") [" + rs.getString("n_aula") + "]";
                out.println("<option value='" + rs.getInt("id_grupo") + "'>" + textoOpcion + "</option>");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        out.println("</select></div>");
        // ----------------------------------------

        out.println("<div class='mb-3'><label class='form-label'>Teléfono:</label>");
        out.println("<input type='text' name='telefono_alumno' class='form-control' placeholder='Solo números'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Edad (Opcional):</label>");
        out.println("<input type='number' name='edad_alumno' class='form-control' placeholder='Ej: 25'></div>");
        
        out.println("<div class='mb-3'><label class='form-label'>Colegio / Escuela:</label>");
        out.println("<input type='text' name='colegio' class='form-control' placeholder='Nombre del colegio'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Grado / Año:</label>");
        out.println("<input type='text' name='grado_anio' class='form-control' placeholder='Ej: 4to Grado, 5to Año'></div>");

        out.println("<div class='mb-3'><label class='form-label'>Descripción / Observaciones:</label>");
        out.println("<textarea name='descripcion' class='form-control' rows='3' placeholder='Comentarios adicionales...'></textarea></div>");

        out.println("<div class='d-grid gap-2 mt-4'>");
        out.println("<button type='submit' class='btn btn-primary btn-lg'>Guardar Estudiante</button>");
        out.println("</div></form>");

        // --- BOTONES DE PIE DE PÁGINA (AQUÍ ESTÁ TU PEDIDO) ---
        out.println("<div class='mt-4 text-center border-top pt-3 d-flex justify-content-between'>");
        
        // Botón 1: Ver Lista
        out.println("<a href='lista' class='btn btn-outline-dark'>&#128203; Ver Planilla</a>");
        
        // Botón 2: Configurar Grupos (EL NUEVO)
        out.println("<a href='configuracion' class='btn btn-outline-secondary'>&#9881; Parametros / Grupos</a>");
        
        out.println("</div>");
        // -----------------------------------------------------

        out.println("</div></div></div></body></html>");
    }
}