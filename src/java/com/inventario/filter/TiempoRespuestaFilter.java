package com.inventario.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*") 
public class TiempoRespuestaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        filterConfig.getServletContext()
                .log("[Filtro] TiempoRespuestaFilter inicializado");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        
        if (request instanceof HttpServletRequest req) {
            String uri = req.getRequestURI();
            if (uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png")
             || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif")
             || uri.endsWith(".ico") || uri.startsWith(req.getContextPath() + "/favicon")) {
                chain.doFilter(request, response);
                return;
            }
        }

        long inicio = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long ms = System.currentTimeMillis() - inicio;

            String metodo = "-";
            String uri = "-";
            int status = -1;

            if (request instanceof HttpServletRequest req) {
                metodo = req.getMethod();
                uri = req.getRequestURI();
                String qs = req.getQueryString();
                if (qs != null && !qs.isBlank()) uri += "?" + qs;
            }
            if (response instanceof HttpServletResponse resp) {
                status = resp.getStatus();
            }

            request.getServletContext()
                   .log(String.format("[Filtro] %s %s -> %d (%d ms)", metodo, uri, status, ms));
        }
    }

    @Override
    public void destroy() {}
}
