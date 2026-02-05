package com.academia;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pass = request.getParameter("password");
        
        // --- AQUÍ DEFINES LA CONTRASEÑA ---
        // Puedes cambiar "admin123" por la clave que quieras usar
        if ("25172517".equals(pass)) {
            
            // Si es correcta, creamos una "Sesión" (le ponemos el sello en la mano)
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", true);
            
            // Lo mandamos a la lista principal
            response.sendRedirect("lista");
            
        } else {
            // Si está mal, volvemos al login con error
            request.setAttribute("error", "si");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Si alguien intenta entrar directo a /login, le mostramos el formulario
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }
}