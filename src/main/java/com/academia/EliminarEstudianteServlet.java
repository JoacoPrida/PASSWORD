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
                String sql = "DELETE FROM estudiantes WHERE id = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, Integer.parseInt(idStr));
                
                ps.executeUpdate();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Al terminar, volvemos a la lista automáticamente
        response.sendRedirect("lista");
    }
}