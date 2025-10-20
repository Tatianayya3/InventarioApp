package com.inventario.controller;

import com.inventario.facade.ProductoFacade;
import com.inventario.model.Producto;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos"})
public class ProductoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Resource(lookup = "jdbc/inventarioPool")
    private DataSource ds;

    private ProductoFacade facade;

    @Override
    public void init() throws ServletException {
        if (ds == null) {
            throw new ServletException("No se pudo inyectar el DataSource jdbc/inventarioPool. Verifica GlassFish y el web.xml.");
        }
        this.facade = new ProductoFacade(ds);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) action = "list";

        // --- parámetros de paginación ---
        int limit = 10; // ajustable
        int page = 1;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignore) { page = 1; }
        int offset = (page - 1) * limit;

        try {
            switch (action) {
                case "new": {
                    req.getRequestDispatcher("/productos.jsp").forward(req, resp);
                    break;
                }

                case "delete": {
                    String idStr = req.getParameter("id");
                    try {
                        if (idStr != null && !idStr.isBlank()) {
                            int id = Integer.parseInt(idStr);
                            facade.eliminar(id);
                            req.getSession().setAttribute("flashSuccess", "Producto eliminado correctamente.");
                        }
                    } catch (NumberFormatException nfe) {
                        req.getSession().setAttribute("flashError", "ID inválido para eliminar.");
                    } catch (Exception ex) {
                        req.getSession().setAttribute("flashError", "No se pudo eliminar: " + ex.getMessage());
                    }
                    resp.sendRedirect(req.getContextPath() + "/productos");
                    break;
                }

                case "search": {
                    String codigo = req.getParameter("codigo");
                    req.setAttribute("codigoBusqueda", codigo);

                    if (codigo != null && !codigo.isBlank()) {
                        var opt = facade.buscarPorCodigo(codigo);
                        if (opt.isPresent()) {
                            req.setAttribute("resultado", opt.get());
                        } else {
                            req.setAttribute("error", "No se encontró producto con código: " + codigo.trim());
                        }
                    } else {
                        req.setAttribute("error", "Ingresa un código para buscar.");
                    }

                    int total = facade.contar();
                    int totalPages = Math.max(1, (int)Math.ceil(total / (double)limit));
                    if (page > totalPages) { page = totalPages; offset = (page - 1) * limit; }

                    List<Producto> productos = facade.listar(limit, offset);
                    req.setAttribute("productos", productos);
                    req.setAttribute("page", page);
                    req.setAttribute("totalPages", totalPages);
                    req.setAttribute("total", total);
                    req.setAttribute("limit", limit);

                    req.getRequestDispatcher("/productos.jsp").forward(req, resp);
                    break;
                }

                default: { // list
                    int total = facade.contar();
                    int totalPages = Math.max(1, (int)Math.ceil(total / (double)limit));
                    if (page > totalPages) { page = totalPages; offset = (page - 1) * limit; }

                    List<Producto> productos = facade.listar(limit, offset);

                    req.setAttribute("productos", productos);
                    req.setAttribute("page", page);
                    req.setAttribute("totalPages", totalPages);
                    req.setAttribute("total", total);
                    req.setAttribute("limit", limit);

                    req.getRequestDispatcher("/productos.jsp").forward(req, resp);
                    break;
                }
            }
        } catch (Exception e) {
            req.setAttribute("error", "Error al cargar productos: " + e.getMessage());
            req.setAttribute("productos", Collections.emptyList());
            req.setAttribute("page", 1);
            req.setAttribute("totalPages", 1);
            req.setAttribute("total", 0);
            req.setAttribute("limit", limit);
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String codigo    = req.getParameter("codigo");
        String nombre    = req.getParameter("nombre");
        String categoria = req.getParameter("categoria");
        String precioStr = req.getParameter("precio");
        String stockStr  = req.getParameter("stock");
        String activoStr = req.getParameter("activo");

        try {
            if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
            if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");

            int stock;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock < 0) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("El stock debe ser un número entero ≥ 0.");
            }

            java.math.BigDecimal precio;
            try {
                precio = (precioStr == null || precioStr.isBlank())
                        ? java.math.BigDecimal.ZERO
                        : new java.math.BigDecimal(precioStr);
                if (precio.signum() < 0) throw new NumberFormatException();
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("El precio debe ser un número ≥ 0.");
            }

            boolean activo = "on".equalsIgnoreCase(activoStr) || "true".equalsIgnoreCase(activoStr);

            Producto p = new Producto();
            p.setCodigo(codigo.trim());
            p.setNombre(nombre.trim());
            p.setCategoria(categoria != null ? categoria.trim() : null);
            p.setPrecio(precio);
            p.setStock(stock);
            p.setActivo(activo);

            facade.crear(p);

            req.getSession().setAttribute("flashSuccess", "Producto creado correctamente.");
            resp.sendRedirect(req.getContextPath() + "/productos");

        } catch (IllegalArgumentException exVal) {
            req.setAttribute("error", exVal.getMessage());
            req.setAttribute("codigo", codigo);
            req.setAttribute("nombre", nombre);
            req.setAttribute("categoria", categoria);
            req.setAttribute("precio", precioStr);
            req.setAttribute("stock", stockStr);
            req.setAttribute("activo", activoStr);
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);

        } catch (Exception e) {
            req.setAttribute("error", "No se pudo guardar: " + e.getMessage());
            req.setAttribute("codigo", codigo);
            req.setAttribute("nombre", nombre);
            req.setAttribute("categoria", categoria);
            req.setAttribute("precio", precioStr);
            req.setAttribute("stock", stockStr);
            req.setAttribute("activo", activoStr);
            req.getRequestDispatcher("/productos.jsp").forward(req, resp);
        }
    }
}
