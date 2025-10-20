# InventarioApp

Aplicación web sencilla para gestión de productos (CRUD parcial) con **Jakarta Servlet + JSP + JSTL**, **GlassFish 7**, **MySQL 8** y **Maven**. Incluye búsqueda por código, paginación, filtro de tiempo de respuesta y mensajes flash.

---

## 1\) Investigación inicial (resumen corto)

* **Servidor**: GlassFish 7 (Jakarta EE 10), admin en `http://localhost:4848`, dominio `domain1`.
* **JDK**: 21 (LTS).
* **DB**: MySQL 8.0 (driver `com.mysql.cj.jdbc.Driver`).
* **JSTL (Jakarta)**: URIs `jakarta.tags.core` y `jakarta.tags.functions`.
* **JNDI**: DataSource `jdbc/inventarioPool` configurado en GlassFish.

---

## 2\) Modelo de datos (ER) y diseño (UML)

### 2.1 ER (Entidad-Relación)

Producto(id, codigo, nombre, categoria, precio, stock, activo)
![Modelo Entidad-Relacion](/images/ER.png)



### 2.2 Diagrama de clases (UML) 

Producto → ProductoDAO → ProductoFacade → ProductoServlet
![Diagrama UML](/images/Diagrama_UML.png)

---

## 3\) Reglas de negocio implementadas

* `codigo`: longitud ≥ 3 y **único**.
* `nombre`: longitud ≥ 5.
* `categoria` ∈ {Electronicos, Accesorios, Muebles, Ropa}.
* `precio` > 0.
* `stock` ≥ 0.

La validación vive en `ProductoFacade.validar(...)`.

---

## 4\) Preparación del entorno

### 4.1 MySQL

1. Ejecutar el script SQL: [`inventario\_productos.sql`](./inventario_productos.sql) o cargarlo desde Workbench.
2. Verificar conexión con el usuario `inv\_user`.

### 4.2 GlassFish 7

* Dominio: `domain1` iniciado (`asadmin start-domain domain1`).
* **Pool**:

```bash
  asadmin create-jdbc-connection-pool \\
    --restype javax.sql.DataSource \\
    --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \\
    --property user=inv\_user:password=inv\_pass:databaseName=inventario\_db:serverName=localhost:portNumber=3306:serverTimezone=America/Bogota:useSSL=false:allowPublicKeyRetrieval=true \\
    InventarioPool
  ```

* **Recurso JNDI**:

```bash
  asadmin create-jdbc-resource --connectionpoolid InventarioPool jdbc/inventarioPool
  ```

* Probar pool: `asadmin ping-connection-pool InventarioPool` (OK).

> Si ves error de zona horaria, usa `serverTimezone=America/Bogota` en el pool.

---

## 5\) Build y despliegue

### 5.1 Maven

```bash
mvn -DskipTests package
```

Genera `target/InventarioApp-1.0.0.war`.

### 5.2 Despliegue

* **Via NetBeans**: Run/Deploy en GlassFish.
* **Via CLI**:

```bash
  asadmin deploy --name InventarioApp --contextroot /InventarioApp target/InventarioApp-1.0.0.war
  ```

### 5.3 URL

* App: `http://localhost:8080/InventarioApp/productos`
* Admin: `http://localhost:4848`

---

## 6\) Funcionalidades visibles

* **Lista** con **paginación** (10 por página) y cabecera fija con scroll.
* **Crear** producto (validación back y HTML5).
* **Eliminar** producto (confirmación).
* **Buscar** por `codigo` (`GET /productos?action=search\&codigo=...`).
* **Mensajes flash** (éxito/error) + autocierre en el JSP.
* **Filtro `@WebFilter`** que mide tiempo de respuesta y lo registra en logs.

---

## 7\) Decisiones y notas

* Se usa **DataSource JNDI** (`jdbc/inventarioPool`) para desacoplar credenciales del código.
* Validación de negocio en **Facade**; operaciones de DB en **DAO**.
* **PRG** (Post/Redirect/Get) tras crear/eliminar para evitar reenvío de formulario.
* **JSTL (Jakarta)**: `jakarta.tags.core` y `jakarta.tags.functions`.
* Paginación por **LIMIT/OFFSET**; `contar()` para total de páginas.
* Seguridad admin GlassFish: `enable-secure-admin` habilitado.

---

## 8\) Problemas comunes (troubleshooting)

* **Access denied (usuario DB)**: recrear usuario/clave y `GRANT` + `FLUSH PRIVILEGES`.
* **Time zone desconocida**: agrega `serverTimezone=America/Bogota` al pool.
* **No inyecta DataSource en servlet**: comprobar `@Resource(lookup="jdbc/inventarioPool")` y que el recurso exista en GlassFish.
* **No aparecen logs del filtro**: revisar `server.log` en `domains/domain1/logs/` o consola GlassFish.

---

## 9\) Estructura mínima del repo

```
InventarioApp/
├─ src/
│  ├─ java/com/inventario/model/Producto.java
│  ├─ java/com/inventario/persistence/ProductoDAO.java
│  ├─ java/com/inventario/facade/ProductoFacade.java
│  ├─ java/com/inventario/controller/ProductoServlet.java
│  └─ java/com/inventario/filter/TiempoRespuestaFilter.java
├─ web/ (JSP, web.xml si aplica)
│  └─ productos.jsp
├─ pom.xml
├─ inventario\_productos.sql
└─ docs/
   ├─ er.png
   └─ uml.png
```

---

## 10\) Investigación guiada 

### ¿Qué es un Managed Bean y en qué se diferencia de un JavaBean?

- Un **JavaBean** es una clase Java con atributos privados, constructor vacío y métodos getters y setters.  
- Un **Managed Bean** es un JavaBean administrado por el contenedor **CDI o JSF**, que gestiona su ciclo de vida y dependencias.  
- Los **JavaBeans** se crean manualmente con `new`, mientras que los **Managed Beans** son instanciados automáticamente por el servidor.  
- Los **Managed Beans** pueden tener diferentes **scopes** que controlan su duración (`@RequestScoped`, `@SessionScoped`, `@ApplicationScoped`, `@Dependent`).  
- Los **JavaBeans** no soportan inyección de dependencias, mientras que los **Managed Beans** sí lo hacen mediante `@Inject`, `@Resource` o `@Produces`.  

---

### ¿Para qué sirven @Named, @ApplicationScoped, @RequestScoped, @SessionScoped y @Dependent?

- **@Named:** permite acceder al bean desde la vista mediante Expression Language (`#{bean}`).  
- **@ApplicationScoped:** mantiene una sola instancia activa durante toda la aplicación, ideal para servicios compartidos.  
- **@RequestScoped:** vive solo durante una petición HTTP, útil para manejar mensajes y formularios.  
- **@SessionScoped:** conserva información del usuario durante toda su sesión, como preferencias o idioma.  
- **@Dependent:** su ciclo de vida depende del bean que lo inyecta, ideal para validadores o utilidades específicas.

---

## 11\) Explicación de cada scope usado y dónde aplica en el flujo.

En esta aplicación se utilizan diferentes **scopes (alcances)** para los *beans* y *controladores*, los cuales determinan el ciclo de vida de los objetos dentro de la aplicación.  
A continuación, se explican los scopes empleados y su función dentro del flujo **“Crear producto”**:

---

### 🔹 `@RequestScoped`

- **Descripción:** Este scope indica que el bean existe únicamente durante una **petición HTTP**.  
  Se crea al iniciar una solicitud y se destruye al devolver la respuesta al cliente.

- **Dónde se usa:** En *servlets* o controladores encargados de manejar acciones específicas, como el `ProductoServlet`.

- **Aplicación en el flujo “Crear producto”:**  
  1. El usuario completa el formulario de creación de producto.  
  2. Al enviar el formulario, se realiza una **petición POST** al `ProductoServlet`.  
  3. El servlet procesa los datos, los envía al *facade* o *DAO* para guardarlos en la base de datos, y devuelve una respuesta.  
  4. Una vez finalizada la petición, el objeto del servlet se destruye.

---

### 🔹 `@SessionScoped`

- **Descripción:** Mantiene el estado del bean mientras dure la **sesión del usuario**.  
  Se crea una sola vez al inicio de la sesión y puede conservar información durante la navegación del usuario.

- **Dónde se usa:** En beans relacionados con la **gestión de sesión del usuario**, **autenticación** o **carrito temporal de productos**.

- **Aplicación en el flujo “Crear producto”:**  
  - Permite conservar información del usuario autenticado mientras se realizan operaciones.  
  - Si el usuario crea varios productos en la misma sesión, el bean puede mantener sus datos sin reiniciarse entre peticiones.

---

### 🔹 `@ApplicationScoped`

- **Descripción:** Define beans que viven mientras la **aplicación esté activa**.  
  Son compartidos entre todas las sesiones y peticiones.

- **Dónde se usa:** En clases que proveen recursos globales, como configuraciones, `EntityManager` o `DataSource`.

- **Aplicación en el flujo “Crear producto”:**  
  - Se utiliza, por ejemplo, para inyectar el `EntityManager` o `DataSource` que maneja la conexión a la base de datos.  
  - Gracias a este scope, los componentes de acceso a datos pueden usar un recurso común sin necesidad de crear uno nuevo por cada solicitud, optimizando el rendimiento.

---

## 12\) Capturas de funcionamiento 

![Interfaz del inventario](/images/Evidencia1.png)
![`codigo`: longitud ≥ 3 y **único**.](/images/Evidencia2.png)
![`nombre`: longitud ≥ 5.](/images/Evidencia3.png)
![Error al no seleccionar categoria](/images/Evidencia4.png)
![`precio` > 0.](/images/Evidencia5.png)
![`precio` > 0.](/images/Evidencia6.png)
![**Filtro `@WebFilter`** que mide tiempo de respuesta y lo registra en logs.](/images/@WebFilter.png)

---

## 13\) Fragmentos de código ilustrativos

### 13.1 ProductoServlet.java

- Scope: @RequestScoped
- Rol: Controlador que recibe la solicitud del formulario y coordina la creación del producto.

```

@WebServlet("/producto")
@RequestScoped
public class ProductoServlet extends HttpServlet {

    @Inject
    private ProductoFacade productoFacade;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre = request.getParameter("nombre");
        double precio = Double.parseDouble(request.getParameter("precio"));
        int cantidad = Integer.parseInt(request.getParameter("cantidad"));

        Producto producto = new Producto(nombre, precio, cantidad);

        productoFacade.crearProducto(producto);

        response.sendRedirect("listaProductos.jsp");
    }
}

```
### 13.2 ProductoFacade.java

- Scope: @ApplicationScoped
- Rol: Capa intermedia que centraliza la lógica de negocio y comunica el servlet con el DAO.

````

@ApplicationScoped
public class ProductoFacade {

    @Inject
    private ProductoDAO productoDAO;

    public void crearProducto(Producto producto) {
        productoDAO.insertar(producto);
    }

    public List<Producto> listarProductos() {
        return productoDAO.obtenerTodos();
    }
}


````
### 13.3 ProductoDAO.java

- Scope: @Dependent (por defecto)
- Rol: Acceso directo a la base de datos (persistencia).

```

@Dependent
public class ProductoDAO {

    @Resource(lookup = "jdbc/inventarioDS")
    private DataSource dataSource;

    public void insertar(Producto producto) {
        String sql = "INSERT INTO producto (nombre, precio, cantidad) VALUES (?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setDouble(2, producto.getPrecio());
            ps.setInt(3, producto.getCantidad());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

```
### 13.4 Producto.java

- Scope: No aplica (es una entidad).
- Rol: Representa la estructura del producto en el sistema.

```

@Entity
@Table(name = "producto")
public class Producto implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String nombre;
    private double precio;
    private int cantidad;

    public Producto() {}

    public Producto(String nombre, double precio, int cantidad) {
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    // Getters y setters
}

```
## 14\) Diagramas

- Mapa conceptual de scopes.

![Mapa conceptual de los scopes y relación con casos del inventario](/images/mapa_conceptual.png)

- Diagrama de secuencia del flujo “Crear producto” con beans y scopes.

![Diagrama de secuencia del flujo “Crear producto” con beans y scopes.](/images/Diagrama_de_secuencia_del_flujo.png)