package com.inventario.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Producto {
    private Integer id;
    private String codigo;
    private String nombre;
    private String categoria;
    private BigDecimal precio;
    private Integer stock;
    private Boolean activo;

    public Producto() { }

    public Producto(String codigo, String nombre, String categoria,
                    BigDecimal precio, Integer stock, Boolean activo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
        this.activo = activo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "Producto{" +
               "id=" + id +
               ", codigo='" + codigo + '\'' +
               ", nombre='" + nombre + '\'' +
               ", categoria='" + categoria + '\'' +
               ", precio=" + precio +
               ", stock=" + stock +
               ", activo=" + activo +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto p = (Producto) o;
        
        if (id != null && p.id != null) return Objects.equals(id, p.id);
        return Objects.equals(codigo, p.codigo);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(codigo);
    }
}
