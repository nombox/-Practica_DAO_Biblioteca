# Práctica DAO - Biblioteca (UD2, parte 1)

Proyecto Maven en Java que implementa el acceso a bases de datos relacionales mediante **JDBC**, aplicando estrictamente el **patrón DAO (Data Access Object)** y una **arquitectura por capas**.

## 🚀 Puesta en marcha

1. En MySQL Workbench, ejecuta el script `sql/01_esquema.sql` (esto creará la base de datos `biblioteca`) y después `sql/02_datos.sql` para cargar los datos de prueba.
2. Configura el archivo `db.properties` en la raíz del proyecto con el usuario y contraseña de tu instalación de MySQL (puedes basarte en `db.properties.example`).
3. Abre el proyecto en tu IDE (NetBeans, IntelliJ, Eclipse) y ejecuta la clase principal `App.java`. Se ejecutarán todas las baterías de pruebas (Bloques A, B, C y D del CRUD).

## 📂 Estructura del Proyecto

El proyecto está organizado en una arquitectura de 3 capas para separar responsabilidades de manera estricta:

* **`sql/`**: Scripts de creación de tablas (`01_esquema.sql`) y carga de datos iniciales (`02_datos.sql`).
* **`src/main/java/gal/dam/biblioteca/`**
  * **`config/`**: Clase `ConexionBD` para cargar las credenciales y aislar la creación de conexiones.
  * **`modelo/`**: Clases de dominio o entidades (`Genero`, `Libro`, `Socio`, `Prestamo`).
  * **`dto/`**: Objetos de transferencia de datos (`Records` de Java) empleados para agrupar resultados de consultas específicas (ej. `FichaSocio`, `LibroDisponible`, `ResumenGenero`).
  * **`dao/`**: Interfaces que definen el contrato de acceso a datos. Incluye una interfaz genérica `DAO<T,K>` y las específicas de cada tabla.
  * **`dao/mysql/`**: Implementaciones concretas del patrón DAO para MySQL usando la API JDBC pura.
  * **`servicio/`**: Lógica de negocio (`CatalogoService`, `SocioService` y `PrestamoService`). Controlan el ciclo de vida de la conexión y las transacciones.
  * **`App.java`**: Punto de entrada de la aplicación. Contiene un set de pruebas completo (con todos los bloques descomentados) que ataca exclusivamente a los servicios.

## 🛠️ Qué se ha implementado

Esta práctica se encuentra **completamente finalizada**, con todas las capas, tablas y funcionalidades operativas:

* **CRUD Completo y Ejecución por Lotes (Batch):** Se ha implementado la inserción, actualización y borrado individual y por lotes en todos los DAO utilizando `addBatch()` y `executeBatch()`, optimizando el rendimiento de las operaciones masivas.
* **Control de Transacciones (TCL):** La capa de servicios gestiona transacciones explícitas (por ejemplo, en `CatalogoService.insertarLibros()`) utilizando `setAutoCommit(false)`, `commit()` para confirmar los lotes, y `rollback()` para revertir los cambios de forma segura ante cualquier violación de integridad (ej. ISBN duplicado).
* **Consultas Avanzadas y Agrupaciones:** Se han resuelto todas las consultas complejas requeridas (Bloque B): disponibilidad de libros calculando ejemplares totales menos no devueltos, filtrado de socios por encima de la media de préstamos mediante subconsultas, y resúmenes estadísticos de puntualidad por género mediante `GROUP BY` y funciones de agregación.
* **Gestión Segura de Recursos:** Uso riguroso de la estructura `try-with-resources` para asegurar el cierre automático de `Connection`, `PreparedStatement` y `ResultSet`.
* **Prevención de Inyección SQL:** Absolutamente todas las consultas a la base de datos emplean `PreparedStatement` con parámetros dinámicos (`?`).
* **Arquitectura Respetada:** 
  * Los **DAO** reciben la conexión ya abierta (`Connection`), ejecutan la sentencia y la devuelven, sin cerrarla en ningún caso.
  * Los **Servicios** solicitan la conexión a `ConexionBD`, llaman a los DAO necesarios (permitiendo reusar la misma conexión para construir DTOs complejos como `FichaSocio`) y la cierran al finalizar su bloque.
  * La clase **App** actúa como un cliente puro, comunicándose única y exclusivamente a través de la capa de Servicios, sin instanciar conexiones ni conocer la base de datos subyacente.

## 💻 Tecnologías Utilizadas
* **Java 17+** (Uso extensivo de `Records`, API de `LocalDate` y `Streams`).
* **Maven** (Gestión del ciclo de vida y dependencias).
* **MySQL Connector/J** (Driver JDBC).
