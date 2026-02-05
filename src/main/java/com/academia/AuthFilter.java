package com.academia;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// Este filtro protege TODO (/*)
@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        // Obtenemos la URL que están intentando visitar
        String path = req.getRequestURI();
        
        // Obtenemos la sesión actual (si existe)
        HttpSession session = req.getSession(false);
        boolean estaLogueado = (session != null && session.getAttribute("usuarioLogueado") != null);

        // --- REGLAS DEL GUARDIA ---
        
        // 1. Dejar pasar si quiere entrar al Login (si no, hacemos un bucle infinito)
        boolean esLogin = path.endsWith("login.jsp") || path.endsWith("login");
        
        // 2. Dejar pasar archivos estáticos (imágenes, CSS) para que se vea bonito
        boolean esRecurso = path.contains("/img/") || path.contains("/css/") || path.contains("bootstrap");

        if (estaLogueado || esLogin || esRecurso) {
            // ¡Pase usted!
            chain.doFilter(request, response);
        } else {
            // ¡ALTO! Usted no tiene permiso. Vaya al login.
            res.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {}

    @Override
    public void destroy() {}
}