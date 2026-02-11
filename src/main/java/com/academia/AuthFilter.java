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

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        // 1. Obtenemos la URL actual
        String path = req.getRequestURI();
        
        // 2. Verificamos la sesión
        HttpSession session = req.getSession(false);
        boolean estaLogueado = (session != null && session.getAttribute("usuarioLogueado") != null);

        // --- REGLAS DEL GUARDIA (CORREGIDAS) ---
        
        // CAMBIO IMPORTANTE: Usamos .contains() en vez de .endsWith()
        // Esto evita que el filtro bloquee la URL si Java le agrega ";jsessionid=..."
        boolean esLogin = path.contains("login");
        
        // Dejamos pasar recursos estáticos (imágenes, css, bootstrap)
        boolean esRecurso = path.contains("/img/") || path.contains("/css/") || path.contains("bootstrap");

        // LÓGICA FINAL
        if (estaLogueado || esLogin || esRecurso) {
            // ¡Pase usted!
            chain.doFilter(request, response);
        } else {
            // Si no cumple nada, lo mandamos al login
            res.sendRedirect(req.getContextPath() + "/login.jsp");
        }
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {}

    @Override
    public void destroy() {}
}