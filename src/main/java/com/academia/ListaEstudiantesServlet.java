package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/lista")
public class ListaEstudiantesServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
                // --- 1. DECLARACIÓN Y CONSULTA (Pegar esto al principio del doGet) ---
            int totalAlumnos = 0;
            try {
                // Abrimos una conexión rápida para el contador
                java.sql.Connection conAux = Conexion.getConexion();
                String sqlCount = "SELECT COUNT(*) FROM estudiantes";
                java.sql.PreparedStatement psCount = conAux.prepareStatement(sqlCount);
                java.sql.ResultSet rsCount = psCount.executeQuery();
                
                if (rsCount.next()) {
                    totalAlumnos = rsCount.getInt(1);
                }
                
                // Cerramos los recursos de esta consulta
                rsCount.close();
                psCount.close();
                conAux.close();
            } catch (Exception e) {
                e.printStackTrace(); // Esto te ayuda a ver si hay error en la consola de Railway
            }
        
        
        // 1. CONFIGURACIÓN BÁSICA
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Fecha actual
        Calendar cal = Calendar.getInstance();
        int mesActual = cal.get(Calendar.MONTH) + 1; 
        int anioActual = cal.get(Calendar.YEAR);
        
        // 2. RECUPERAR BÚSQUEDA
        String busqueda = request.getParameter("busqueda");
        boolean hayBusqueda = (busqueda != null && !busqueda.trim().isEmpty());

        // --- HTML ---
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'><title>Planilla de Alumnos</title>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");

        out.println("<div class='container mt-4'>");
        
        // ENCABEZADO
        out.println("<div class='d-flex justify-content-between align-items-center mb-3'>");
        out.println("<div>");
        out.println("<div class='d-flex align-items-center'>");
        out.println("<h2>&#128176; Planilla General</h2>");
        out.println("<img src='img/fotoMa.jpg' width='60' class='rounded ms-3 shadow-sm' alt='Foto'>");
        out.println("</div>");

        out.println("<div class='btn-group mt-1'>");
        out.println("<a href='lista' class='btn btn-primary btn-sm disabled'>Ver Lista Completa</a>");
        out.println("<a href='ver-grupos' class='btn btn-outline-primary btn-sm'>Ver por Grupos</a>");
        out.println("</div>");
        out.println("</div>");



        
        out.println("<div class='d-flex gap-2 align-items-center'>");
        out.println("<a href='configuracion' class='btn btn-outline-secondary fw-bold'>&#9881; Parámetros / Grupos</a>");
        out.println("<div class='me-2'>");
        out.println("  <span class='badge bg-dark text-white p-2 shadow-sm' style='font-size: 0.9rem;'>");
        out.println("    &#128101; Total: <strong>" + totalAlumnos + "</strong>");
        out.println("  </span>");
        out.println("</div>");
        out.println("<a href='links.jsp' class='btn btn-warning text-dark fw-bold'>&#11088; Links Útiles</a>");
        out.println("<a href='formulario-registro' class='btn btn-success fw-bold'>+ Nuevo Alumno</a>");
        out.println("</div>");
        out.println("</div>");

        // BARRA DE BÚSQUEDA
        out.println("<div class='card mb-3 shadow-sm'>");
        out.println("<div class='card-body py-2'>");
        out.println("<form action='lista' method='get' class='d-flex gap-2'>");
        String valorInput = hayBusqueda ? busqueda : "";
        out.println("<input class='form-control' type='search' name='busqueda' placeholder='Buscar por nombre...' value='" + valorInput + "'>");
        out.println("<button class='btn btn-primary' type='submit'>&#128269; Buscar</button>");
        if(hayBusqueda) {
            out.println("<a href='lista' class='btn btn-outline-secondary'>&#10005;</a>");
        }
        out.println("</form></div></div>");

        out.println("<div class='card shadow'>");
        out.println("<div class='card-body p-0'>"); 
        out.println("<div class='table-responsive'>"); 
        
        out.println("<table class='table table-striped table-hover mb-0 align-middle'>");
        out.println("<thead class='table-dark'><tr>");
        out.println("<th>Nombre</th>");
        out.println("<th>Grupo</th>");
        out.println("<th class='text-center'>Situación</th>");
        out.println("<th class='text-center'>Acciones</th>");
        out.println("</tr></thead><tbody>");


            // SQL CON DOBLE SUBCONSULTA (MESES Y MATRÍCULA)
            Connection con = Conexion.getConexion();
            try {
            String sql = "SELECT e.id, e.nombre, e.fecha_inscripcion, g.nombre AS nombre_grupo, " +
                         "(SELECT COUNT(*) FROM pagos p WHERE p.id_estudiante = e.id AND p.anio = ? AND p.mes >= 1 AND p.mes <= 13) as pagos_hechos, " + 
                         "(SELECT COUNT(*) FROM pagos p WHERE p.id_estudiante = e.id AND p.anio = ? AND p.mes = 0) as matricula_pagada " +
                         "FROM estudiantes e " +
                         "JOIN grupos g ON e.id_grupo = g.id_grupo ";
            
            if (hayBusqueda) {
                sql += " WHERE e.nombre LIKE ? ";
            }
            sql += " ORDER BY e.nombre ASC"; 
            
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, anioActual);
            ps.setInt(2, anioActual);
            if (hayBusqueda) {
                ps.setString(3, "%" + busqueda + "%");
            }

            ResultSet rs = ps.executeQuery();
            boolean hayResultados = false;

            while (rs.next()) {
                hayResultados = true;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String grupo = rs.getString("nombre_grupo");
                int pagosHechos = rs.getInt("pagos_hechos");
                int matriculaPagada = rs.getInt("matricula_pagada");
                
                // --- CÁLCULO DE MESES EXIGIBLES ---
                java.sql.Date sqlFecha = rs.getDate("fecha_inscripcion");
                Calendar calInscripcion = Calendar.getInstance();
                calInscripcion.setTime(sqlFecha);
                
                int mesInscripcion = calInscripcion.get(Calendar.MONTH) + 1;
                int anioInscripcion = calInscripcion.get(Calendar.YEAR);
                
                int mesesExigibles = 0;

                if (mesActual >= 3) {
                    int mesInicioCobro = 3; 
                    if (anioInscripcion == anioActual && mesInscripcion > 3) {
                        mesInicioCobro = mesInscripcion;
                    }
                    // Meses de clase (Marzo a Diciembre = Máximo 10)
                    mesesExigibles = (mesActual <= 12) ? (mesActual - mesInicioCobro + 1) : (12 - mesInicioCobro + 1);
                    if (mesesExigibles < 0) mesesExigibles = 0;
                }

                // Derecho de Examen (Cuota 11) exigible desde Noviembre
                if (mesActual >= 11) {
                    mesesExigibles++;
                }

                int deudaMeses = mesesExigibles - pagosHechos;
                if (deudaMeses < 0) deudaMeses = 0;

                // --- LÓGICA DE BADGE (CARTEL DE ESTADO) ---
                String badgeClass = "bg-success";
                String estado = "AL DÍA";

                if (deudaMeses > 0 || matriculaPagada == 0) {
                    badgeClass = "bg-danger";
                    if (deudaMeses > 0 && matriculaPagada == 0) {
                        estado = "DEBE MATRÍCULA Y " + deudaMeses + " MES(ES)";
                    } else if (matriculaPagada == 0) {
                        estado = "DEBE MATRÍCULA";
                    } else {
                        estado = "DEBE " + deudaMeses + " MES(ES)";
                    }
                }

                if (mesActual < 3) {
                    estado = "VACACIONES";
                    badgeClass = "bg-info text-dark";
                }

                // --- DIBUJAR FILA ---
                out.println("<tr>");
                out.println("<td class='fw-bold'>" + nombre + "</td>");
                out.println("<td><span class='badge bg-secondary'>" + (grupo != null ? grupo : "Sin Grupo") + "</span></td>");
                out.println("<td class='text-center'><span class='badge " + badgeClass + " rounded-pill px-3'>" + estado + "</span></td>");
                out.println("<td class='text-center'>");
                out.println("<a href='pagos?id=" + id + "' class='btn btn-outline-primary btn-sm' title='Pagos'>&#128179;</a>");
                out.println("<a href='editar?id=" + id + "' class='btn btn-outline-secondary btn-sm ms-1' title='Editar'>&#9999;</a>");
                out.println("<a href='eliminar?id=" + id + "' class='btn btn-outline-danger btn-sm ms-1' onclick='return confirm(\"¿Borrar?\")' title='Borrar'>&#128465;</a>");
                out.println("</td></tr>");
            }

            if (!hayResultados) {
                out.println("<tr><td colspan='4' class='text-center py-4 text-muted'>No se encontraron alumnos.</td></tr>");
            }
            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            out.println("<tr><td colspan='4' class='text-danger'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</tbody></table></div></div></div></div></body></html>");
    }

}