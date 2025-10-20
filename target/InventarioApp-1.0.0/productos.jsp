<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html>
<head>
    <title>Inventario - Productos</title>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .form-section { margin-top: 30px; padding: 20px; background-color: #f9f9f9; }
        input[type="text"], input[type="number"], select { width: 220px; padding: 6px; }
        button { padding: 10px 20px; background-color: #007cba; color: white; border: none; cursor: pointer; }
        button:hover { background-color: #005a82; }
        .actions a { margin-right: 8px; }

        /* Alerts + autocierre */
        .alert{
          position:relative; padding:12px 36px 12px 12px; margin:10px 0;
          border-radius:8px; border:1px solid #ddd;
          transition:opacity .35s ease, transform .35s ease, max-height .35s ease;
          max-height:200px; overflow:hidden;
        }
        .alert-success{ background:#ecfdf5; border-color:#a7f3d0; }
        .alert-error{ background:#fef2f2; border-color:#fecaca; }
        .alert.hide{ opacity:0; transform:translateY(-6px); max-height:0; padding-top:0; padding-bottom:0; margin:0; }
        .alert .close{
          position:absolute; right:8px; top:4px; border:none; background:transparent;
          font-size:20px; line-height:20px; cursor:pointer;
        }

        /* Scroll vertical + cabecera fija */
        .table-wrap{
          max-height: 360px;
          overflow-y: auto;
          border: 1px solid #ddd;
          border-radius: 6px;
        }
        .table-wrap table{ width: 100%; border-collapse: collapse; }
        .table-wrap thead th{
          position: sticky;
          top: 0;
          background-color: #f2f2f2;
          z-index: 1;
        }

        /* Buscador */
        .search-bar { margin: 10px 0 14px; }
        .search-bar input[type="text"]{ width: 260px; }
    </style>
</head>
<body>
<h1>Sistema de Inventario</h1>
<h2>Lista de Productos</h2>

<!-- Buscador por código -->
<form class="search-bar" method="get" action="${pageContext.request.contextPath}/productos">
  <input type="hidden" name="action" value="search"/>
  <input type="text" name="codigo" placeholder="Buscar por código"
         value="${fn:escapeXml(codigoBusqueda)}"/>
  <button type="submit">Buscar</button>
  <c:if test="${not empty codigoBusqueda}">
    <button type="button"
            onclick="location.href='${pageContext.request.contextPath}/productos'">Limpiar búsqueda</button>
  </c:if>
</form>

<!-- Mensajes flash -->
<c:if test="${not empty sessionScope.flashSuccess}">
  <div class="alert alert-success" data-autoclose="true">
    <span class="msg">${sessionScope.flashSuccess}</span>
    <button type="button" class="close" aria-label="Cerrar">&times;</button>
  </div>
  <c:remove var="flashSuccess" scope="session"/>
</c:if>
<c:if test="${not empty sessionScope.flashError}">
  <div class="alert alert-error" data-autoclose="true">
    <span class="msg">${sessionScope.flashError}</span>
    <button type="button" class="close" aria-label="Cerrar">&times;</button>
  </div>
  <c:remove var="flashError" scope="session"/>
</c:if>

<!-- Error por forward -->
<c:if test="${not empty error}">
  <div class="alert alert-error" data-autoclose="true">
    <span class="msg"><c:out value="${error}"/></span>
    <button type="button" class="close" aria-label="Cerrar">&times;</button>
  </div>
</c:if>

<!-- Resultado de búsqueda (si lo hay) -->
<c:if test="${not empty resultado}">
  <div class="alert alert-success" data-autoclose="true">
    <strong>Resultado:</strong>
    Código: <b>${resultado.codigo}</b>,
    Nombre: <b>${resultado.nombre}</b>,
    Categoría: <b>${resultado.categoria}</b>,
    Precio: <b>${resultado.precio}</b>,
    Stock: <b>${resultado.stock}</b>,
    Activo: <b><c:out value="${resultado.activo ? 'Sí' : 'No'}"/></b>
  </div>
</c:if>

<!-- Tabla con scroll -->
<div class="table-wrap">
  <table>
    <thead>
    <tr>
      <th>ID</th>
      <th>Código</th>
      <th>Nombre</th>
      <th>Categoría</th>
      <th>Precio</th>
      <th>Stock</th>
      <th>Activo</th>
      <th>Acciones</th>
    </tr>
    </thead>
    <tbody>
    <c:choose>
      <c:when test="${not empty productos}">
        <c:forEach var="p" items="${productos}">
          <tr>
            <td>${p.id}</td>
            <td>${p.codigo}</td>
            <td>${p.nombre}</td>
            <td><c:out value="${p.categoria}"/></td>
            <td>${p.precio}</td>
            <td>${p.stock}</td>
            <td><c:out value="${p.activo ? 'Sí' : 'No'}"/></td>
            <td class="actions">
              <a href="${pageContext.request.contextPath}/productos?action=delete&id=${p.id}"
                 onclick="return confirm('¿Eliminar el producto ${p.nombre}?');">Eliminar</a>
            </td>
          </tr>
        </c:forEach>
      </c:when>
      <c:otherwise>
        <tr><td colspan="8">No hay productos cargados.</td></tr>
      </c:otherwise>
    </c:choose>
    </tbody>
  </table>
</div>

<!-- Paginador -->
<div style="margin-top:10px; display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
  <span>
    <c:set var="from" value="${(page - 1) * limit + 1}" />
    <c:set var="to"   value="${(page * limit) > total ? total : (page * limit)}" />
    Mostrando ${from}–${to} de ${total}
  </span>

  <div>
    <c:choose>
      <c:when test="${page > 1}">
        <a href="${pageContext.request.contextPath}/productos?page=${page - 1}">&laquo; Anterior</a>
      </c:when>
      <c:otherwise>
        <span style="color:#999;">&laquo; Anterior</span>
      </c:otherwise>
    </c:choose>

    <span style="margin:0 8px;">Página ${page} de ${totalPages}</span>

    <c:choose>
      <c:when test="${page < totalPages}">
        <a href="${pageContext.request.contextPath}/productos?page=${page + 1}">Siguiente &raquo;</a>
      </c:when>
      <c:otherwise>
        <span style="color:#999;">Siguiente &raquo;</span>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<div class="form-section">
  <h3>Agregar Nuevo Producto</h3>
  <form method="post" action="${pageContext.request.contextPath}/productos" id="frmProducto">
    <table>
      <tr>
        <td><label for="codigo">Código:</label></td>
        <td>
          <input type="text" id="codigo" name="codigo" required minlength="3"
                 placeholder="Mínimo 3 caracteres"
                 value="${fn:escapeXml(codigo)}"/>
        </td>
      </tr>
      <tr>
        <td><label for="nombre">Nombre:</label></td>
        <td>
          <input type="text" id="nombre" name="nombre" required minlength="5"
                 placeholder="Mínimo 5 caracteres"
                 value="${fn:escapeXml(nombre)}"/>
        </td>
      </tr>
      <tr>
        <td><label for="categoria">Categoría:</label></td>
        <td>
          <select id="categoria" name="categoria">
            <option value="" ${empty categoria ? 'selected' : ''}>Seleccione...</option>
            <option value="Electronicos" ${categoria == 'Electronicos' ? 'selected' : ''}>Electrónicos</option>
            <option value="Accesorios"   ${categoria == 'Accesorios'   ? 'selected' : ''}>Accesorios</option>
            <option value="Muebles"      ${categoria == 'Muebles'      ? 'selected' : ''}>Muebles</option>
            <option value="Ropa"         ${categoria == 'Ropa'         ? 'selected' : ''}>Ropa</option>
          </select>
        </td>
      </tr>
      <tr>
        <td><label for="precio">Precio:</label></td>
        <td>
          <input type="number" id="precio" name="precio" step="0.01" min="0.01"
                 placeholder="0.00" value="${precio}"/>
        </td>
      </tr>
      <tr>
        <td><label for="stock">Stock:</label></td>
        <td>
          <input type="number" id="stock" name="stock" min="0" placeholder="0" value="${stock}"/>
        </td>
      </tr>
      <tr>
        <td><label for="activo">Activo:</label></td>
        <td>
          <input type="checkbox" id="activo" name="activo"
                 ${empty activo || activo == 'on' || activo == true ? 'checked' : ''}/>
        </td>
      </tr>
      <tr>
        <td colspan="2">
          <button type="submit">Guardar Producto</button>
          <button type="button" onclick="location.href='${pageContext.request.contextPath}/productos'">
            Limpiar
          </button>
        </td>
      </tr>
    </table>
  </form>
</div>

<script>
  // Cerrar manualmente
  document.addEventListener('click', function(e){
    if (e.target.matches('.alert .close')) {
      e.target.closest('.alert')?.classList.add('hide');
    }
  });
  // Autocerrar a los 4s
  window.addEventListener('DOMContentLoaded', function(){
    document.querySelectorAll('.alert[data-autoclose]').forEach(function(box){
      setTimeout(function(){ box.classList.add('hide'); }, 4000);
    });
  });
</script>
</body>
</html>
