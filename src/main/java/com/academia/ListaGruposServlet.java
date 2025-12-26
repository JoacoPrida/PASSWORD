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

@WebServlet("/ver-grupos")
public class ListaGruposServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Vista por Grupos</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js'></script>"); // Necesario para que funcione el click
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-4'>");

        // ENCABEZADO CON BOTONES DE CAMBIO DE VISTA
        out.println("<div class='d-flex justify-content-between align-items-center mb-4'>");
        out.println("<h2>&#128101; Grupos y Alumnos</h2>");
        out.println("<div>");
        // Botón activo (este)
        out.println("<a href='#' class='btn btn-primary disabled'>Ver por Grupos</a>");
        // Botón para ir a la otra vista
        out.println("<a href='lista' class='btn btn-outline-primary ms-2'>Ver Lista Completa</a>");
        out.println("</div>");
        out.println("</div>");

        out.println("<div class='accordion' id='accordionGrupos'>");

        try {
            Connection con = Conexion.getConexion();
            
            // 1. TRAEMOS LOS GRUPOS Y LA CANTIDAD DE ALUMNOS
            String sqlGrupos = "SELECT g.*, a.nombre as nombre_aula, " +
                               "(SELECT COUNT(*) FROM estudiantes e WHERE e.id_grupo = g.id_grupo) as cantidad " +
                               "FROM grupos g JOIN aulas a ON g.id_aula = a.id_aula ORDER BY g.nombre";
            
            PreparedStatement psGrupos = con.prepareStatement(sqlGrupos);
            ResultSet rsGrupos = psGrupos.executeQuery();

            while(rsGrupos.next()) {
                int idGrupo = rsGrupos.getInt("id_grupo");
                String nombreGrupo = rsGrupos.getString("nombre");
                String infoExtra = rsGrupos.getString("dias") + " | " + rsGrupos.getString("horario") + " | " + rsGrupos.getString("nombre_aula");
                int cantidad = rsGrupos.getInt("cantidad");

                // --- INICIO ITEM ACORDEÓN ---
                out.println("<div class='accordion-item shadow-sm mb-2 border-0'>");
                
                // CABECERA DEL GRUPO (LO QUE SE VE ANTES DE HACER CLICK)
                out.println("<h2 class='accordion-header' id='heading"+idGrupo+"'>");
                out.println("<button class='accordion-button collapsed' type='button' data-bs-toggle='collapse' data-bs-target='#collapse"+idGrupo+"' aria-expanded='false'>");
                out.println("<div class='d-flex flex-column'>");
                out.println("<span class='fw-bold fs-5'>" + nombreGrupo + "</span>");
                // Aquí está la letra pequeña con la cantidad
                out.println("<span class='text-muted small'>" + infoExtra + " &bull; <span class='badge bg-info text-dark'>" + cantidad + " Estudiantes</span></span>");
                out.println("</div>");
                out.println("</button></h2>");

                // CUERPO DEL GRUPO (LO QUE SE DESPLIEGA)
                out.println("<div id='collapse"+idGrupo+"' class='accordion-collapse collapse' data-bs-parent='#accordionGrupos'>");
                out.println("<div class='accordion-body bg-white'>");
                
                if (cantidad > 0) {
                    // TABLA DE ALUMNOS DE ESTE GRUPO
                    out.println("<table class='table table-sm table-hover align-middle'>");
                    out.println("<thead><tr><th>Nombre</th><th>Teléfono</th><th class='text-end'>Gestionar</th></tr></thead>");
                    out.println("<tbody>");
                    
                    // CONSULTA INTERNA: BUSCAR ALUMNOS DE ESTE GRUPO ESPECÍFICO
                    String sqlAlumnos = "SELECT * FROM estudiantes WHERE id_grupo = ? ORDER BY nombre";
                    PreparedStatement psAlumnos = con.prepareStatement(sqlAlumnos);
                    psAlumnos.setInt(1, idGrupo);
                    ResultSet rsAlumnos = psAlumnos.executeQuery();
                    
                    while(rsAlumnos.next()){
                        out.println("<tr>");
                        out.println("<td>" + rsAlumnos.getString("nombre") + "</td>");
                        out.println("<td>" + rsAlumnos.getString("telefono") + "</td>");
                        out.println("<td class='text-end'>");
                        // Botones de acción rápida
                        out.println("<a href='pagos?id=" + rsAlumnos.getInt("id") + "' class='btn btn-outline-success btn-sm me-1'>Pagos</a>");
                        out.println("<a href='editar?id=" + rsAlumnos.getInt("id") + "' class='btn btn-outline-secondary btn-sm'>Editar</a>");
                        out.println("</td>");
                        out.println("</tr>");
                    }
                    out.println("</tbody></table>");
                } else {
                    out.println("<p class='text-muted fst-italic mb-0'>No hay estudiantes registrados en este grupo.</p>");
                }

                out.println("</div></div>"); // Fin body y collapse
                out.println("</div>"); // Fin item
                // --- FIN ITEM ACORDEÓN ---
            }
            
            con.close();

        } catch (Exception e) {
            e.printStackTrace();
            out.println("<div class='alert alert-danger'>Error cargando grupos: " + e.getMessage() + "</div>");
        }

        out.println("</div>"); // Fin accordion container
        out.println("<div class='mt-4 text-center'><a href='formulario-registro' class='btn btn-success'>+ Nuevo Alumno</a></div>");
        out.println("</div></body></html>");
    }
}