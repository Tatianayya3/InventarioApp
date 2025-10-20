package com.inventario.facade;

import com.inventario.model.Producto;
import com.inventario.persistence.ProductoDAO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Fachada de negocio para Producto.
 * Recibe el DataSource desde el contenedor (Servlet) y delega en el DAO.
 */
public class ProductoFacade {

    private final DataSource ds;

    public ProductoFacade(DataSource ds) {
        this.ds = Objects.requireNonNull(ds, "DataSource no puede ser null");
    }

    /* ===================== Reglas de negocio / validación ===================== */

    private static final Set<String> CATEGORIAS =
            Set.of("Electronicos", "Accesorios", "Muebles", "Ropa");

    private void validar(Producto p) throws Exception {
        if (p == null) throw new Exception("El producto es requerido.");

        String codigo = p.getCodigo() == null ? "" : p.getCodigo().trim();
        String nombre = p.getNombre() == null ? "" : p.getNombre().trim();
        String categoria = p.getCategoria() == null ? "" : p.getCategoria().trim();

        if (codigo.length() < 3) throw new Exception("El código debe tener al menos 3 caracteres.");
        if (nombre.length() < 5) throw new Exception("El nombre debe tener al menos 5 caracteres.");
        if (!CATEGORIAS.contains(categoria)) {
            throw new Exception("La categoría debe ser una de: " + CATEGORIAS);
        }

        if (p.getPrecio() == null) throw new Exception("El precio es obligatorio.");
        if (p.getPrecio().signum() <= 0) throw new Exception("El precio debe ser mayor a 0.");

        if (p.getStock() == null) throw new Exception("El stock es obligatorio.");
        if (p.getStock() < 0) throw new Exception("El stock no puede ser negativo.");
    }

    /* =============================== Operaciones ============================== */

    /** Lista paginada */
    public List<Producto> listar(int limit, int offset) throws Exception {
        try (Connection con = ds.getConnection()) {
            ProductoDAO dao = new ProductoDAO(con);
            return dao.listar(limit, offset);
        }
    }

    /** Total registros */
    public int contar() throws Exception {
        try (Connection con = ds.getConnection()) {
            ProductoDAO dao = new ProductoDAO(con);
            return dao.contar();
        }
    }

    /** Buscar por código */
    public Optional<Producto> buscarPorCodigo(String codigo) throws Exception {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        try (Connection con = ds.getConnection()) {
            ProductoDAO dao = new ProductoDAO(con);
            return dao.buscarPorCodigo(codigo.trim());
        }
    }

    public void crear(Producto p) throws Exception {
        validar(p);
        try (Connection con = ds.getConnection()) {
            ProductoDAO dao = new ProductoDAO(con);
            // Unicidad por código
            if (dao.buscarPorCodigo(p.getCodigo().trim()).isPresent()) {
                throw new Exception("Ya existe un producto con ese código.");
            }
            dao.insertar(p);
        }
    }

    public void eliminar(int id) throws Exception {
        if (id <= 0) throw new Exception("ID inválido.");
        try (Connection con = ds.getConnection()) {
            ProductoDAO dao = new ProductoDAO(con);
            dao.eliminarPorId(id);
        }
    }
}
