package com.academia;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/eliminar")
public class EliminarEstudianteServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        
        if(idStr != null) {
            try {
                Connection con = Conexion.getConexion();
                int idAlumno = Integer.parseInt(idStr);

                // PASO 1: Borrar primero el historial de pagos (para evitar error de Foreign Key)
                String sqlPagos = "DELETE FROM pagos WHERE id_estudiante = ?";
                PreparedStatement psPagos = con.prepareStatement(sqlPagos);
                psPagos.setInt(1, idAlumno);
                psPagos.executeUpdate();

                // PASO 2: Ahora sí, borrar al estudiante
                String sqlEstudiante = "DELETE FROM estudiantes WHERE id = ?";
                PreparedStatement psEst = con.prepareStatement(sqlEstudiante);
                psEst.setInt(1, idAlumno);
                psEst.executeUpdate();

                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Al terminar, volvemos a la lista automáticamente
        response.sendRedirect("lista");
    }
}