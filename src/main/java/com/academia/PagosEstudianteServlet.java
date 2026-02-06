package com.academia;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/pagos")
public class PagosEstudianteServlet extends HttpServlet {

    // --- 1. MOSTRAR PANTALLA (GET) ---
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idEstudiante = request.getParameter("id");
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String nombreAlumno = "";
        String grupoAlumno = "";

        // Recuperar datos del alumno
        try {
            Connection con = Conexion.getConexion();
            String sql = "SELECT e.nombre, g.nombre as n_grupo FROM estudiantes e LEFT JOIN grupos g ON e.id_grupo = g.id_grupo WHERE e.id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(idEstudiante));
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                nombreAlumno = rs.getString("nombre");
                grupoAlumno = rs.getString("n_grupo");
                if (grupoAlumno == null) grupoAlumno = "Sin Grupo";
            }
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        // --- HTML ---
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<title>Pagos de " + nombreAlumno + "</title>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
        out.println("<script src='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js'></script>");
        out.println("</head><body class='bg-light'>");
        
        out.println("<div class='container mt-5' style='max-width: 900px;'>");
        
        // Encabezado
        out.println("<div class='card shadow-sm mb-4 border-primary'>");
        out.println("<div class='card-body text-center'>");
        out.println("<h2 class='card-title text-primary'>" + nombreAlumno + "</h2>");
        out.println("<h5 class='text-muted'>" + grupoAlumno + " | Ciclo Lectivo " + anioActual + "</h5>");
        out.println("<a href='lista' class='btn btn-outline-secondary btn-sm mt-2'>&larr; Volver a la Lista</a>");
        out.println("</div></div>");

        out.println("<div class='row g-3'>");

        try {
            Connection con = Conexion.getConexion();
            
            // DIBUJAR TARJETAS
            imprimirTarjeta(out, con, idEstudiante, 0, anioActual, "Matrícula Anual", true);

            String[] nombresMeses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            for (int i = 1; i <= 12; i++) {
                if (i == 1 || i == 2) {
                    out.println("<div class='col-6 col-md-4 col-lg-3'>");
                    out.println("<div class='card h-100 bg-secondary bg-opacity-10 border-0'>");
                    out.println("<div class='card-body text-center text-muted py-4'>");
                    out.println("<h5>" + nombresMeses[i] + "</h5>");
                    out.println("<p class='small m-0'><i>Receso / Vacaciones</i></p>");
                    out.println("</div></div></div>");
                } else {
                    imprimirTarjeta(out, con, idEstudiante, i, anioActual, nombresMeses[i], false);
                }
            }

            imprimirTarjeta(out, con, idEstudiante, 13, anioActual, "Derecho de Examen", true);

            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        out.println("</div>"); // Fin row
        
        // --- MODAL FLEXIBLE ---
        out.println("<div class='modal fade' id='modalPago' tabindex='-1' aria-hidden='true'>");
        out.println("<div class='modal-dialog modal-sm modal-dialog-centered'>");
        out.println("<div class='modal-content'>");
        out.println("<form action='pagos' method='post'>");
        
        out.println("<div class='modal-header bg-primary text-white'>");
        out.println("<h5 class='modal-title'>Registrar Pago</h5>");
        out.println("<button type='button' class='btn-close btn-close-white' data-bs-dismiss='modal'></button>");
        out.println("</div>");
        
        out.println("<div class='modal-body'>");
        out.println("<input type='hidden' name='accion' value='registrar'>");
        out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
        out.println("<input type='hidden' name='mes' id='inputMesModal'>"); 
        
        out.println("<p class='text-center mb-2'>Mes: <strong id='txtMesModal' class='text-primary'></strong></p>");
        
        // CAMPO MONTO (YA NO TIENE 'REQUIRED')
        out.println("<div class='mb-3'>");
        out.println("<label class='form-label small'>Monto Abonado ($):</label>");
        out.println("<input type='number' name='monto' class='form-control form-control-lg' placeholder='0'>");
        out.println("</div>");

        // CAMPO FECHA
        out.println("<div class='mb-2'>");
        out.println("<label class='form-label small'>Fecha del Pago:</label>");
        out.println("<input type='date' name='fecha_pago' class='form-control' value='" + LocalDate.now() + "' required>");
        out.println("</div>");
        
        out.println("</div>"); 
        
        out.println("<div class='modal-footer p-1'>");
        out.println("<button type='submit' class='btn btn-primary w-100'>Confirmar Pago</button>");
        out.println("</div>");
        
        out.println("</form></div></div></div>");
        
        out.println("<script>");
        out.println("function abrirModal(mes, nombreMes) {");
        out.println("   document.getElementById('inputMesModal').value = mes;");
        out.println("   document.getElementById('txtMesModal').innerText = nombreMes;");
        out.println("   new bootstrap.Modal(document.getElementById('modalPago')).show();");
        out.println("}");
        out.println("</script>");

        out.println("</div></body></html>");
    }

    // --- DIBUJAR TARJETAS ---
    private void imprimirTarjeta(PrintWriter out, Connection con, String idEstudiante, int mesCodigo, int anio, String titulo, boolean esEspecial) throws Exception {
        
        String sqlCheck = "SELECT monto, fecha_pago FROM pagos WHERE id_estudiante=? AND mes=? AND anio=?";
        PreparedStatement ps = con.prepareStatement(sqlCheck);
        ps.setInt(1, Integer.parseInt(idEstudiante));
        ps.setInt(2, mesCodigo);
        ps.setInt(3, anio);
        ResultSet rs = ps.executeQuery();
        
        boolean pagado = false;
        double montoPagado = 0;
        String fechaPagado = "";
        
        if (rs.next()) {
            pagado = true;
            montoPagado = rs.getDouble("monto");
            fechaPagado = rs.getDate("fecha_pago").toString(); 
        }
        
        String colClass = "col-6 col-md-4 col-lg-3"; 
        out.println("<div class='" + colClass + "'>");
        
        if (pagado) {
            String bgClass = esEspecial ? "bg-success border-success" : "bg-success"; 
            
            out.println("<div class='card text-white " + bgClass + " h-100 shadow-sm'>");
            out.println("<div class='card-body text-center p-2'>");
            out.println(esEspecial ? "<h6>&#9733; " + titulo + "</h6>" : "<h6>" + titulo + "</h6>");
            
            // Si el monto es 0, no mostramos "$0", solo la fecha
            if (montoPagado > 0) {
                out.println("<h4 class='my-2'>$" + (int)montoPagado + "</h4>");
            } else {
                out.println("<h4 class='my-2'>-</h4>");
            }
            
            out.println("<span class='badge bg-light text-dark bg-opacity-75'>" + fechaPagado + "</span>");
            
            out.println("<form action='pagos' method='post' class='mt-2'>");
            out.println("<input type='hidden' name='accion' value='eliminar'>");
            out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
            out.println("<input type='hidden' name='mes' value='" + mesCodigo + "'>");
            out.println("<button type='submit' class='btn btn-sm btn-outline-light py-0' onclick='return confirm(\"¿Borrar este pago?\")'>Deshacer</button>");
            out.println("</form>");
            out.println("</div></div>");
            
        } else {
            String borderClass = esEspecial ? "border-primary border-2" : "border-secondary";
            String textClass = esEspecial ? "text-primary fw-bold" : "text-secondary";
            
            out.println("<div class='card h-100 " + borderClass + "'>");
            out.println("<div class='card-body text-center d-flex flex-column justify-content-between p-3'>");
            out.println("<h6 class='" + textClass + "'>" + titulo + "</h6>");
            
            out.println("<button type='button' class='btn btn-outline-primary w-100 mt-2' onclick='abrirModal(" + mesCodigo + ", \"" + titulo + "\")'>");
            out.println("Pagar");
            out.println("</button>");
            
            out.println("</div></div>");
        }
        
        out.println("</div>");
    }

    // --- 2. GUARDAR DATOS (POST) ---
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String accion = request.getParameter("accion");
        int idEstudiante = Integer.parseInt(request.getParameter("id_estudiante"));
        int mes = Integer.parseInt(request.getParameter("mes"));
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        
        try {
            Connection con = Conexion.getConexion();
            
            if ("registrar".equals(accion)) {
                
                // LÓGICA DE MONTO OPCIONAL
                String montoStr = request.getParameter("monto");
                double monto = 0;
                
                // Si escribieron algo, lo convertimos a número. Si está vacío, queda en 0.
                if (montoStr != null && !montoStr.isEmpty()) {
                    try {
                        monto = Double.parseDouble(montoStr);
                    } catch (NumberFormatException e) {
                        monto = 0;
                    }
                }
                
                String fechaManual = request.getParameter("fecha_pago"); 
                
                String sql = "INSERT INTO pagos (id_estudiante, mes, anio, monto, fecha_pago) " +
                             "VALUES (?, ?, ?, ?, ?) " + 
                             "ON DUPLICATE KEY UPDATE monto = ?, fecha_pago = ?";
                
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, idEstudiante);
                ps.setInt(2, mes);
                ps.setInt(3, anioActual);
                ps.setDouble(4, monto);
                ps.setString(5, fechaManual); 
                
                ps.setDouble(6, monto);
                ps.setString(7, fechaManual); 
                
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