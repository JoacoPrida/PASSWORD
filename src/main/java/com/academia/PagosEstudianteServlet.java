package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/pagos")
public class PagosEstudianteServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idEstudiante = request.getParameter("id");
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String nombreAlumno = "";
        String grupoAlumno = "";

        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT e.nombre, g.nombre as n_grupo FROM estudiantes e JOIN grupos g ON e.id_grupo = g.id_grupo WHERE e.id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idEstudiante));
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                nombreAlumno = rs.getString("nombre");
                grupoAlumno = rs.getString("n_grupo");
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("</head><body class='bg-light'>");
        
        out.println("<div class='container mt-5' style='max-width: 800px;'>");
        
        out.println("<div class='card shadow-sm mb-4 border-primary'>");
        out.println("<div class='card-body text-center'>");
        out.println("<h2 class='card-title text-primary'>" + nombreAlumno + "</h2>");
        out.println("<h5 class='text-muted'>" + grupoAlumno + " | Ciclo Lectivo " + anioActual + "</h5>");
        out.println("<a href='lista' class='btn btn-outline-secondary btn-sm mt-2'>&larr; Volver a la Lista</a>");
        out.println("</div></div>");

        out.println("<div class='row g-3'>");

        String[] meses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        try {
            Connection con = Conexion.getConexion();
            
            for (int i = 1; i <= 12; i++) {
                
                // CASO ESPECIAL: Enero y Febrero deshabilitados
                if (i == 1 || i == 2) {
                    out.println("<div class='col-6 col-md-4'>");
                    out.println("<div class='card h-100 bg-secondary bg-opacity-10 border-0'>"); // Gris claro
                    out.println("<div class='card-body text-center text-muted'>");
                    out.println("<h5>" + meses[i] + "</h5>");
                    out.println("<p class='small'><i>Receso / Vacaciones</i></p>");
                    out.println("</div></div></div>");
                    continue; // Saltamos al siguiente mes
                }

                // Lógica normal para Marzo a Diciembre
                String sqlCheck = "SELECT * FROM pagos WHERE id_estudiante=? AND mes=? AND anio=?";
                PreparedStatement ps = con.prepareStatement(sqlCheck);
                ps.setInt(1, Integer.parseInt(idEstudiante));
                ps.setInt(2, i);
                ps.setInt(3, anioActual);
                ResultSet rs = ps.executeQuery();
                
                boolean pagado = rs.next();
                
                out.println("<div class='col-6 col-md-4'>");
                
                if (pagado) {
                    // PAGADO (VERDE)
                    out.println("<div class='card text-white bg-success h-100'>");
                    out.println("<div class='card-body text-center'>");
                    out.println("<h5>" + meses[i] + "</h5>");
                    out.println("<p class='mb-0 fw-bold'>PAGADO &#10004;</p>");
                    
                    // Botón deshacer
                    out.println("<form action='pagos' method='post' class='mt-2'>");
                    out.println("<input type='hidden' name='accion' value='eliminar'>");
                    out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
                    out.println("<input type='hidden' name='mes' value='" + i + "'>");
                    out.println("<button type='submit' class='btn btn-sm btn-outline-light' style='font-size: 0.7em'>Deshacer</button>");
                    out.println("</form>");
                    out.println("</div></div>");
                } else {
                    // PENDIENTE (BLANCO)
                    out.println("<div class='card h-100 border-secondary'>");
                    out.println("<div class='card-body text-center'>");
                    out.println("<h5 class='text-secondary'>" + meses[i] + "</h5>");
                    
                    // Botón cobrar
                    out.println("<form action='pagos' method='post'>");
                    out.println("<input type='hidden' name='accion' value='registrar'>");
                    out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
                    out.println("<input type='hidden' name='mes' value='" + i + "'>");
                    out.println("<button type='submit' class='btn btn-primary w-100'>Registrar Pago</button>");
                    out.println("</form>");
                    
                    out.println("</div></div>");
                }
                out.println("</div>"); 
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        out.println("</div></div></body></html>");
    }

    // ACCIONES (POST) - Igual que antes
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        int idEstudiante = Integer.parseInt(request.getParameter("id_estudiante"));
        int mes = Integer.parseInt(request.getParameter("mes"));
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        
        try {
            Connection con = Conexion.getConexion();
            
            if ("registrar".equals(accion)) {
                String sql = "INSERT INTO pagos (id_estudiante, mes, anio, fecha_pago) VALUES (?, ?, ?, CURRENT_DATE)";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, idEstudiante);
                ps.setInt(2, mes);
                ps.setInt(3, anioActual);
                ps.executeUpdate();
            } else if ("eliminar".equals(accion)) {
                String sql = "DELETE FROM pagos WHERE id_estudiante=? AND mes=? AND anio=?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, idEstudiante);
                ps.setInt(2, mes);
                ps.setInt(3, anioActual);
                ps.executeUpdate();
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        
        response.sendRedirect("pagos?id=" + idEstudiante);
    }
}