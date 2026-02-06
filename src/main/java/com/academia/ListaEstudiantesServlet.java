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
        
        // 1. CONFIGURACIÓN BÁSICA
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Fecha actual
        Calendar cal = Calendar.getInstance();
        int mesActual = cal.get(Calendar.MONTH) + 1; 
        int anioActual = cal.get(Calendar.YEAR);
        
        // 2. RECUPERAR LO QUE ESCRIBIÓ EL USUARIO (Si es que escribió algo)
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
        
        // --- CAMBIO: TÍTULO CON FOTO AL LADO ---
        out.println("<div class='d-flex align-items-center'>");
        out.println("<h2>&#128176; Planilla General</h2>");
        // Foto pequeña (width='60') y separada un poco a la izquierda (ms-3)
        out.println("<img src='img/fotoMa.jpg' width='60' class='rounded ms-3 shadow-sm' alt='Foto'>");
        out.println("</div>");
        // ---------------------------------------

        out.println("<div class='btn-group mt-1'>");
        out.println("<a href='lista' class='btn btn-primary btn-sm disabled'>Ver Lista Completa</a>");
        out.println("<a href='ver-grupos' class='btn btn-outline-primary btn-sm'>Ver por Grupos</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("<a href='links.jsp' class='btn btn-warning text-dark fw-bold'>&#11088; Links Útiles</a>");
        // Botón Nuevo Alumno (sin cambios)
        out.println("<a href='formulario-registro' class='btn btn-success'>+ Nuevo Alumno</a>");
        out.println("</div>");


        // --- BARRA DE BÚSQUEDA (NUEVO) ---
        out.println("<div class='card mb-3 shadow-sm'>");
        out.println("<div class='card-body py-2'>");
        out.println("<form action='lista' method='get' class='d-flex gap-2'>");
        
        // El input mantiene el valor escrito (value) para que no se borre al buscar
        String valorInput = hayBusqueda ? busqueda : "";
        out.println("<input class='form-control' type='search' name='busqueda' placeholder='Buscar por nombre...' value='" + valorInput + "'>");
        
        out.println("<button class='btn btn-primary' type='submit'>&#128269; Buscar</button>");
        
        // Si hay búsqueda, mostramos un botón 'X' para limpiar
        if(hayBusqueda) {
            out.println("<a href='lista' class='btn btn-outline-secondary'>&#10005;</a>");
        }
        out.println("</form>");
        out.println("</div></div>");
        // ---------------------------------

        out.println("<div class='card shadow'>");
        out.println("<div class='card-body p-0'>"); 
        out.println("<div class='table-responsive'>"); 
        
        out.println("<table class='table table-striped table-hover mb-0 align-middle'>");
        out.println("<thead class='table-dark'><tr>");
        out.println("<th>Nombre</th>");
        out.println("<th>Grupo</th>");
        out.println("<th class='text-center'>Situaci&oacute;n</th>");
        out.println("<th class='text-center'>Acciones</th>");
        out.println("</tr></thead>");
        out.println("<tbody>");

        try {
            Connection con = Conexion.getConexion();
            
            // --- SQL DINÁMICO ---
            // Base de la consulta
            String sql = "SELECT e.id, e.nombre, e.fecha_inscripcion, g.nombre AS nombre_grupo, " +
                         "(SELECT COUNT(*) FROM pagos p WHERE p.id_estudiante = e.id AND p.anio = ?) as pagos_hechos " + 
                         "FROM estudiantes e " +
                         "JOIN grupos g ON e.id_grupo = g.id_grupo ";
            
            // Si el usuario busca algo, agregamos el filtro WHERE
            if (hayBusqueda) {
                sql += " WHERE e.nombre LIKE ? ";
            }
            
            sql += " ORDER BY e.nombre ASC"; // Siempre ordenado alfabéticamente
            
            PreparedStatement ps = con.prepareStatement(sql);
            
            // ASIGNAR PARÁMETROS (?)
            // El primer ? siempre es el AÑO (está en la subconsulta del SELECT)
            ps.setInt(1, anioActual);
            
            // El segundo ? solo existe si hay búsqueda
            if (hayBusqueda) {
                // Usamos % para buscar coincidencias parciales (ej: %Juan% encuentra a "Juan Perez" y "San Juan")
                ps.setString(2, "%" + busqueda + "%");
            }
            
            ResultSet rs = ps.executeQuery();

            boolean hayResultados = false;
            while (rs.next()) {
                hayResultados = true;
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                String grupo = rs.getString("nombre_grupo");
                int pagosHechos = rs.getInt("pagos_hechos");
                
                // --- CÁLCULO DE DEUDA (IGUAL QUE ANTES) ---
                java.sql.Date sqlFecha = rs.getDate("fecha_inscripcion");
                Calendar calInscripcion = Calendar.getInstance();
                calInscripcion.setTime(sqlFecha);
                
                int mesInscripcion = calInscripcion.get(Calendar.MONTH) + 1;
                int anioInscripcion = calInscripcion.get(Calendar.YEAR);
                
                int mesesExigibles = 0;
                String estadoEspecial = "";

                if (mesActual < 3) {
                    estadoEspecial = "VACACIONES";
                } else {
                    int mesInicioCobro = 3; 
                    if (anioInscripcion == anioActual && mesInscripcion > 3) {
                        mesInicioCobro = mesInscripcion;
                    }
                    mesesExigibles = mesActual - mesInicioCobro + 1;
                    if (mesesExigibles < 0) mesesExigibles = 0;
                }
                int mesesQueDebe = mesesExigibles - pagosHechos;

                // --- DIBUJAR FILA ---
                out.println("<tr>");
                
                // Resaltamos el nombre si coincide con la búsqueda (Opcional, detalle visual)
                out.println("<td class='fw-bold'>" + nombre + "</td>");
                
                out.println("<td><span class='badge bg-secondary'>" + grupo + "</span></td>");
                
                out.println("<td class='text-center'>");
                if (estadoEspecial.equals("VACACIONES")) {
                    out.println("<span class='badge bg-info text-dark rounded-pill px-3'>&#127958; VACACIONES</span>");
                } else if (mesesQueDebe <= 0) {
                    out.println("<span class='badge bg-success rounded-pill px-3'>&#10004; AL D&Iacute;A</span>");
                } else if (mesesQueDebe == 1) {
                    out.println("<span class='badge bg-warning text-dark rounded-pill px-3'>DEBE 1 MES</span>");
                } else {
                    out.println("<span class='badge bg-danger rounded-pill px-3'>DEBE " + mesesQueDebe + " MESES</span>");
                }
                out.println("</td>");
                
                out.println("<td class='text-center'>");
                out.println("<a href='pagos?id=" + id + "' class='btn btn-outline-primary btn-sm' title='Pagos'>&#128179;</a>");
                out.println("<a href='editar?id=" + id + "' class='btn btn-outline-secondary btn-sm ms-1' title='Editar'>&#9999;</a>");
                out.println("<a href='eliminar?id=" + id + "' class='btn btn-outline-danger btn-sm ms-1' onclick='return confirm(\"Borrar?\")' title='Borrar'>&#128465;</a>");
                out.println("</td>");
                out.println("</tr>");
            }

            if (!hayResultados) {
                out.println("<tr><td colspan='4' class='text-center py-4 text-muted'>No se encontraron alumnos con ese nombre.</td></tr>");
            }

            con.close();

        } catch (SQLException e) {
            out.println("<tr><td colspan='4' class='text-danger'>Error: " + e.getMessage() + "</td></tr>");
        }

        out.println("</tbody></table>");
        out.println("</div></div></div></div></body></html>");
    }
}