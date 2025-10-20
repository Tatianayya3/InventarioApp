package com.inventario.persistence;

import com.inventario.model.Producto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object para Producto
 */
public class ProductoDAO {
    private final Connection con;

    public ProductoDAO(Connection con) throws SQLException {
        this.con = con;
        ensureSchema();
    }

    /* Crea la tabla si no existe (MySQL) */
    private void ensureSchema() throws SQLException {
        final String sql = """
            CREATE TABLE IF NOT EXISTS productos (
              id        INT AUTO_INCREMENT PRIMARY KEY,
              codigo    VARCHAR(50)  NOT NULL UNIQUE,
              nombre    VARCHAR(120) NOT NULL,
              categoria VARCHAR(80)  NULL,
              precio    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
              stock     INT NOT NULL DEFAULT 0,
              activo    TINYINT(1) NOT NULL DEFAULT 1
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """;
        try (Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    /* ===== Listado paginado ===== */
    public List<Producto> listar(int limit, int offset) throws SQLException {
        final String sql = """
            SELECT id, codigo, nombre, categoria, precio, stock, activo
            FROM productos
            ORDER BY nombre ASC
            LIMIT ? OFFSET ?
            """;
        List<Producto> out = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            ps.setInt(2, Math.max(0, offset));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        }
        return out;
    }

    public int contar() throws SQLException {
        final String sql = "SELECT COUNT(*) FROM productos";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /* ===== Búsqueda, inserción y eliminación ===== */
    public Optional<Producto> buscarPorCodigo(String codigo) throws SQLException {
        final String sql = """
            SELECT id, codigo, nombre, categoria, precio, stock, activo
            FROM productos
            WHERE codigo = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
                return Optional.empty();
            }
        }
    }

    public void insertar(Producto p) throws SQLException {
        final String sql = """
            INSERT INTO productos (codigo, nombre, categoria, precio, stock, activo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getCategoria());
            ps.setBigDecimal(4, p.getPrecio() != null ? p.getPrecio() : BigDecimal.ZERO);
            ps.setInt(5, p.getStock() != null ? p.getStock() : 0);
            ps.setBoolean(6, p.getActivo() != null ? p.getActivo() : Boolean.TRUE);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        }
    }

    public void eliminarPorId(int id) throws SQLException {
        final String sql = "DELETE FROM productos WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /* ---------- util ---------- */
    private Producto mapRow(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getBoolean("activo"));
        return p;
    }
}
