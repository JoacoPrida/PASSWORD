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

        // 1. OBTENER DATOS DEL ALUMNO
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

        // 2. DIBUJAR PANTALLA
        out.println("<!DOCTYPE html><html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1'>");
        out.println("<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>");
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
            
            // --- A) TARJETA DE MATRÍCULA (Mes 0) ---
            imprimirTarjeta(out, con, idEstudiante, 0, anioActual, "Matrícula Anual", true);

            // --- B) MESES DEL AÑO (1 al 12) ---
            String[] nombresMeses = {"", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
            
            for (int i = 1; i <= 12; i++) {
                // Caso Vacaciones (Enero/Febrero) - Tarjeta Gris
                if (i == 1 || i == 2) {
                    out.println("<div class='col-6 col-md-4 col-lg-3'>");
                    out.println("<div class='card h-100 bg-secondary bg-opacity-10 border-0'>");
                    out.println("<div class='card-body text-center text-muted'>");
                    out.println("<h5>" + nombresMeses[i] + "</h5>");
                    out.println("<p class='small'><i>Receso / Vacaciones</i></p>");
                    out.println("</div></div></div>");
                } else {
                    // Meses Normales
                    imprimirTarjeta(out, con, idEstudiante, i, anioActual, nombresMeses[i], false);
                }
            }

            // --- C) DERECHO DE EXAMEN (Mes 13) ---
            imprimirTarjeta(out, con, idEstudiante, 13, anioActual, "Derecho de Examen", true);

            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        out.println("</div></div></body></html>");
    }

    // --- MÉTODO AUXILIAR PARA DIBUJAR CADA CUADRADO ---
    // Esto evita repetir el código HTML 14 veces
    private void imprimirTarjeta(PrintWriter out, Connection con, String idEstudiante, int mesCodigo, int anio, String titulo, boolean esEspecial) throws Exception {
        
        // Verificamos si está pagado
        String sqlCheck = "SELECT * FROM pagos WHERE id_estudiante=? AND mes=? AND anio=?";
        PreparedStatement ps = con.prepareStatement(sqlCheck);
        ps.setInt(1, Integer.parseInt(idEstudiante));
        ps.setInt(2, mesCodigo);
        ps.setInt(3, anio);
        ResultSet rs = ps.executeQuery();
        boolean pagado = rs.next();
        
        // Definimos estilos (Si es especial como Matrícula, le damos un borde o color distinto opcional)
        String colClass = "col-6 col-md-4 col-lg-3"; // 4 columnas en pc, 2 en movil
        
        out.println("<div class='" + colClass + "'>");
        
        if (pagado) {
            // TARJETA VERDE (PAGADO)
            // Si es especial (Matrícula/Examen) usamos un verde más oscuro o igual
            String bgClass = esEspecial ? "bg-success border-success" : "bg-success"; 
            
            out.println("<div class='card text-white " + bgClass + " h-100 shadow-sm'>");
            out.println("<div class='card-body text-center'>");
            out.println(esEspecial ? "<h5>&#9733; " + titulo + "</h5>" : "<h5>" + titulo + "</h5>");
            out.println("<p class='mb-0 fw-bold'>PAGADO &#10004;</p>");
            
            // Botón Deshacer
            out.println("<form action='pagos' method='post' class='mt-2'>");
            out.println("<input type='hidden' name='accion' value='eliminar'>");
            out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
            out.println("<input type='hidden' name='mes' value='" + mesCodigo + "'>");
            out.println("<button type='submit' class='btn btn-sm btn-outline-light' style='font-size: 0.7em'>Deshacer</button>");
            out.println("</form>");
            out.println("</div></div>");
            
        } else {
            // TARJETA BLANCA (PENDIENTE)
            String borderClass = esEspecial ? "border-primary border-2" : "border-secondary";
            String textClass = esEspecial ? "text-primary fw-bold" : "text-secondary";
            
            out.println("<div class='card h-100 " + borderClass + "'>");
            out.println("<div class='card-body text-center'>");
            out.println("<h5 class='" + textClass + "'>" + titulo + "</h5>");
            
            // Botón Pagar
            out.println("<form action='pagos' method='post' class='mt-3'>");
            out.println("<input type='hidden' name='accion' value='registrar'>");
            out.println("<input type='hidden' name='id_estudiante' value='" + idEstudiante + "'>");
            out.println("<input type='hidden' name='mes' value='" + mesCodigo + "'>");
            
            String btnClass = esEspecial ? "btn-primary" : "btn-outline-primary";
            out.println("<button type='submit' class='btn " + btnClass + " w-100'>Registrar Pago</button>");
            out.println("</form>");
            out.println("</div></div>");
        }
        
        out.println("</div>");
    }

    // --- PROCESAR EL PAGO (POST) ---
    // No hace falta cambiar casi nada aquí, porque "mes" ahora puede ser 0 o 13 y la base de datos lo aceptará igual.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        int idEstudiante = Integer.parseInt(request.getParameter("id_estudiante"));
        int mes = Integer.parseInt(request.getParameter("mes")); // Recibe 0, 13, o 1-12
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