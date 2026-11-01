package gal.dam.biblioteca;

import gal.dam.biblioteca.dao.DAO;
import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.dao.PrestamoDAO;
import gal.dam.biblioteca.dao.SocioDAO;
import gal.dam.biblioteca.dao.mysql.GeneroDAOMySQL;
import gal.dam.biblioteca.dao.mysql.LibroDAOMySQL;
import gal.dam.biblioteca.dao.mysql.PrestamoDAOMySQL;
import gal.dam.biblioteca.dao.mysql.SocioDAOMySQL;
import gal.dam.biblioteca.modelo.Genero;
import gal.dam.biblioteca.modelo.Libro;
import gal.dam.biblioteca.modelo.Prestamo;
import gal.dam.biblioteca.modelo.Socio;
import gal.dam.biblioteca.servicio.CatalogoService;
import gal.dam.biblioteca.servicio.PrestamoService;
import gal.dam.biblioteca.servicio.SocioService;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Punto de entrada para probar la práctica.
 * <p>
 * 1. Crea los DAO (implementaciones MySQL) y se los pasa a los servicios.
 * 2. Llama a los servicios con los datos del enunciado y muestra el resultado.
 * <p>
 * App solo habla con los servicios: nunca usa un DAO ni una Connection.
 * Lo que aún no está hecho aparece como "(pendiente)". Puedes añadir tus propias pruebas.
 * <p>
 * BLOQUE D (CRUD): está comentado para que el proyecto compile antes de declarar
 * los métodos nuevos. Cuando tengas declarados en los servicios los métodos de
 * una tabla, descomenta su llamada en main() y su método probarCrud...()
 * (en NetBeans: selecciona las líneas y pulsa Ctrl+Shift+C).
 * <p>
 * Las pruebas del bloque D insertan, modifican y borran sus propias filas, así que
 * la base de datos queda como estaba. Los ids esperados son los de la primera
 * ejecución tras cargar 01_esquema.sql y 02_datos.sql: después, AUTO_INCREMENT
 * sigue contando. Si una prueba falla a medias, vuelve a ejecutar los dos scripts.
 */
public class App {

    /** Fecha de referencia del enunciado para las consultas de vencimientos. */
    private static final LocalDate FECHA_REF = LocalDate.of(2026, 10, 15);

    public static void main(String[] args) {
        // 1. DAO: se crea una sola instancia de cada uno (no guardan estado)
        LibroDAO libroDAO = new LibroDAOMySQL();
        SocioDAO socioDAO = new SocioDAOMySQL();
        PrestamoDAO prestamoDAO = new PrestamoDAOMySQL();
        DAO<Genero, Integer> generoDAO = new GeneroDAOMySQL();

        // 2. Servicios: reciben los DAO por el constructor (como interfaces)
        CatalogoService catalogo = new CatalogoService(libroDAO, generoDAO);
        SocioService socios = new SocioService(socioDAO, libroDAO, prestamoDAO);
        PrestamoService prestamos = new PrestamoService(prestamoDAO, libroDAO, socioDAO);

        probarEjemplo(catalogo);
        probarBloqueA(catalogo, socios, prestamos);
        probarBloqueB(catalogo, socios, prestamos);
        probarBloqueC(socios);

        // Bloque D: descomenta cada línea cuando tengas los métodos de esa tabla
        // probarCrudGeneros(catalogo);
        // probarCrudLibros(catalogo);
        // probarCrudSocios(socios);
        // probarCrudPrestamos(prestamos);
    }

    private static void probarEjemplo(CatalogoService catalogo) {
        prueba("Ejemplo · Géneros por nombre", catalogo::listarGeneros);
        prueba("Ejemplo · Género 5", () -> catalogo.buscarGenero(5));
    }

    private static void probarBloqueA(CatalogoService catalogo, SocioService socios, PrestamoService prestamos) {
        prueba("A.1 · Libros por título", catalogo::listarLibros);
        prueba("A.1 · Libro 4", () -> catalogo.buscarLibro(4));
        prueba("A.1 · Libro 99", () -> catalogo.buscarLibro(99));
        prueba("A.2 · Socios por apellidos", socios::listarSocios);
        prueba("A.2 · Socio 10", () -> socios.buscarSocio(10));
        prueba("A.3 · Préstamos por fecha", prestamos::listarPrestamos);
        prueba("A.3 · Préstamo 34", () -> prestamos.buscarPrestamo(34));
    }

    private static void probarBloqueB(CatalogoService catalogo, SocioService socios, PrestamoService prestamos) {
        prueba("B.1 · Título contiene \"fundacion\"", () -> catalogo.buscarPorTitulo("fundacion"));
        prueba("B.1 · Título contiene \"de\"", () -> catalogo.buscarPorTitulo("de"));
        prueba("B.2 · Libros de \"gaiman\"", () -> catalogo.librosDeAutor("gaiman"));
        prueba("B.3 · Autores del libro 8", () -> catalogo.autoresDe(8));
        prueba("B.3 · Autores del libro 13", () -> catalogo.autoresDe(13));
        prueba("B.4 · Socio marta.rey@biblio.gal", () -> socios.buscarPorEmail("marta.rey@biblio.gal"));
        prueba("B.4 · Socio nadie@biblio.gal", () -> socios.buscarPorEmail("nadie@biblio.gal"));
        prueba("B.5 · Préstamos activos del socio 1", () -> prestamos.activosDeSocio(1));
        prueba("B.5 · Préstamos activos del socio 10", () -> prestamos.activosDeSocio(10));
        prueba("B.6 · Libros de ciencia ficción (2) leídos por el socio 1",
                () -> socios.librosLeidosDeGenero(1, 2));
        prueba("B.7 · Libros nunca prestados", catalogo::nuncaPrestados);
        prueba("B.8 · Socios con préstamos vencidos a " + FECHA_REF, () -> socios.conPrestamosVencidos(FECHA_REF));
        prueba("B.9 · Historial del socio 3", () -> prestamos.historial(3));
        prueba("B.10 · Préstamos vencidos a " + FECHA_REF, () -> prestamos.vencidos(FECHA_REF));
        prueba("B.11 · Disponibilidad", catalogo::disponibilidad);
        prueba("B.12 · Más prestado de cada género", catalogo::masPrestadoPorGenero);
        prueba("B.13 · Lectores de todo el autor 3", () -> socios.lectoresDeTodoElAutor(3));
        prueba("B.13 · Lectores de todo el autor 9", () -> socios.lectoresDeTodoElAutor(9));
        prueba("B.14 · Lectores por encima de la media", socios::lectoresSobreLaMedia);
        prueba("B.15 · Resumen por género", prestamos::resumenPorGenero);
    }

    private static void probarBloqueC(SocioService socios) {
        prueba("C.2 · Ficha del socio 1", () -> socios.fichaSocio(1));
        prueba("C.2 · Ficha del socio 99", () -> socios.fichaSocio(99));
    }

    // ------------------------------------------------------------------
    // Bloque D: resto del CRUD (1 fila y listados por lotes)
    // Descomenta cada método entero cuando lo necesites.
    // ------------------------------------------------------------------

//    private static void probarCrudGeneros(CatalogoService catalogo) {
//        // D.1 insertar 1: devuelve el id generado y lo asigna al objeto
//        Genero teatro = new Genero(0, "Teatro");
//        prueba("D.1 · Insertar género Teatro", () -> catalogo.insertarGenero(teatro));
//        // Esperado: 6
//
//        // D.2 insertar listado (lote): asigna a cada objeto su id
//        Genero comic = new Genero(0, "Cómic");
//        Genero biografia = new Genero(0, "Biografía");
//        prueba("D.2 · Insertar lote de géneros", () -> catalogo.insertarGeneros(List.of(comic, biografia)));
//        // Esperado: [1, 1]
//        prueba("D.2 · Objetos tras el lote (ids asignados)", () -> List.of(comic, biografia));
//        // Esperado: 7: Cómic, 8: Biografía
//        prueba("D.2 · Géneros tras insertar", catalogo::listarGeneros);
//        // Esperado: 8 géneros
//
//        // D.3 actualizar 1
//        teatro.setNombre("Teatro clásico");
//        prueba("D.3 · Actualizar Teatro", () -> catalogo.actualizarGenero(teatro));
//        // Esperado: true
//        prueba("D.3 · Actualizar género 99", () -> catalogo.actualizarGenero(new Genero(99, "Nada")));
//        // Esperado: false
//
//        // D.4 actualizar listado (lote)
//        comic.setNombre("Novela gráfica");
//        biografia.setNombre("Biografía y memorias");
//        prueba("D.4 · Actualizar lote de géneros", () -> catalogo.actualizarGeneros(List.of(comic, biografia)));
//        // Esperado: [1, 1]
//        prueba("D.4 · Género " + comic.getId(), () -> catalogo.buscarGenero(comic.getId()));
//        // Esperado: 7: Novela gráfica
//
//        // Errores de integridad
//        prueba("D.1 · Insertar género repetido (Novela)", () -> catalogo.insertarGenero(new Genero(0, "Novela")));
//        // Esperado: ERROR SQLState 23000 / código 1062 (Duplicate entry)
//        prueba("D.5 · Eliminar género 1 (tiene libros)", () -> catalogo.eliminarGenero(1));
//        // Esperado: ERROR SQLState 23000 / código 1451 (foreign key)
//
//        // D.5 eliminar 1
//        prueba("D.5 · Eliminar Teatro clásico", () -> catalogo.eliminarGenero(teatro.getId()));
//        // Esperado: true
//        prueba("D.5 · Eliminar género 99", () -> catalogo.eliminarGenero(99));
//        // Esperado: false
//
//        // D.6 eliminar listado (lote): el 99 no existe -> 0 en su posición
//        prueba("D.6 · Eliminar lote de géneros",
//                () -> catalogo.eliminarGeneros(List.of(comic.getId(), biografia.getId(), 99)));
//        // Esperado: [1, 1, 0]
//        prueba("D.6 · Géneros al final", catalogo::listarGeneros);
//        // Esperado: los 5 iniciales
//    }

//    private static void probarCrudLibros(CatalogoService catalogo) {
//        // D.1 insertar 1 (en Poesía, género 5)
//        Libro cantares = new Libro(0, "978-84-0002-001-0", "Cantares gallegos", 1863, 2, 5);
//        prueba("D.1 · Insertar libro Cantares gallegos", () -> catalogo.insertarLibro(cantares));
//        // Esperado: 14
//
//        // D.2 insertar listado (lote)
//        Libro orillas = new Libro(0, "978-84-0002-002-7", "En las orillas del Sar", 1884, 1, 5);
//        Libro longa = new Libro(0, "978-84-0002-003-4", "Longa noite de pedra", 1962, 1, 5);
//        prueba("D.2 · Insertar lote de libros", () -> catalogo.insertarLibros(List.of(orillas, longa), false));
//        // Esperado: [1, 1]
//        prueba("D.2 · Objetos tras el lote (ids asignados)", () -> List.of(orillas, longa));
//        // Esperado: 15: En las orillas del Sar..., 16: Longa noite de pedra...
//
//        // D.3 actualizar 1
//        cantares.setEjemplares(3);
//        prueba("D.3 · Actualizar ejemplares de Cantares gallegos", () -> catalogo.actualizarLibro(cantares));
//        // Esperado: true
//        prueba("D.3 · Libro " + cantares.getId(), () -> catalogo.buscarLibro(cantares.getId()));
//        // Esperado: 14: Cantares gallegos (1863, 3 ej.)
//
//        // D.4 actualizar listado (lote)
//        orillas.setEjemplares(2);
//        longa.setEjemplares(2);
//        prueba("D.4 · Actualizar lote de libros", () -> catalogo.actualizarLibros(List.of(orillas, longa)));
//        // Esperado: [1, 1]
//
//        // Errores de integridad
//        prueba("D.1 · Insertar libro con ISBN repetido",
//                () -> catalogo.insertarLibro(new Libro(0, "978-84-0001-001-1", "Copia", 2000, 1, 1)));
//        // Esperado: ERROR SQLState 23000 / código 1062
//        prueba("D.1 · Insertar libro con género 99",
//                () -> catalogo.insertarLibro(new Libro(0, "978-84-0002-099-0", "Sin género", 2000, 1, 99)));
//        // Esperado: ERROR SQLState 23000 / código 1452 (foreign key)
//        prueba("D.5 · Eliminar libro 4 (tiene préstamos)", () -> catalogo.eliminarLibro(4));
//        // Esperado: ERROR SQLState 23000 / código 1451
//
//        // D.5 eliminar 1
//        prueba("D.5 · Eliminar Cantares gallegos", () -> catalogo.eliminarLibro(cantares.getId()));
//        // Esperado: true
//
//        // D.6 eliminar listado (lote) con un fallo en medio: sin transacción,
//        // las sentencias correctas se quedan hechas aunque el lote lance excepción
//        prueba("D.6 · Eliminar lote de libros con el 4 en medio",
//                () -> catalogo.eliminarLibros(List.of(orillas.getId(), 4, longa.getId())));
//        // Esperado: ERROR en el lote, código 1451. Resultado por sentencia: [1, -3, 1]
//        prueba("D.6 · Libros tras el lote", catalogo::listarLibros);
//        // Esperado: los 13 iniciales (los dos nuevos se borraron igualmente)
//
//        // D.7 insertar lote con y sin transacción: el segundo libro repite un ISBN
//        // (Cien años de soledad), así que la segunda sentencia del lote falla.
//        // Si el lote falla, los objetos no reciben id: se buscan por título.
//        prueba("D.7 · Lote SIN transacción con un ISBN repetido",
//                () -> catalogo.insertarLibros(List.of(
//                        new Libro(0, "978-84-0002-071-3", "Prueba D.7 sin transacción A", 2026, 1, 4),
//                        new Libro(0, "978-84-0001-001-1", "Prueba D.7 sin transacción B", 2026, 1, 4),
//                        new Libro(0, "978-84-0002-072-0", "Prueba D.7 sin transacción C", 2026, 1, 4)),
//                        false));
//        // Esperado: ERROR en el lote, código 1062. Resultado por sentencia: [1, -3, 1]
//        List<Libro> quedan = prueba("D.7 · Libros \"Prueba D.7\" tras el lote sin transacción",
//                () -> catalogo.buscarPorTitulo("Prueba D.7"));
//        // Esperado: 2 libros (A y C): las sentencias correctas se quedaron hechas
//        if (quedan != null && !quedan.isEmpty()) {
//            prueba("D.7 · Limpieza", () -> catalogo.eliminarLibros(
//                    quedan.stream().map(Libro::getId).toList()));
//            // Esperado: [1, 1]
//        }
//
//        prueba("D.7 · Lote CON transacción con un ISBN repetido",
//                () -> catalogo.insertarLibros(List.of(
//                        new Libro(0, "978-84-0002-073-7", "Prueba D.7 con transacción A", 2026, 1, 4),
//                        new Libro(0, "978-84-0001-001-1", "Prueba D.7 con transacción B", 2026, 1, 4),
//                        new Libro(0, "978-84-0002-074-4", "Prueba D.7 con transacción C", 2026, 1, 4)),
//                        true));
//        // Esperado: la misma excepción: código 1062, [1, -3, 1]
//        prueba("D.7 · Libros \"Prueba D.7\" tras el lote con transacción",
//                () -> catalogo.buscarPorTitulo("Prueba D.7"));
//        // Esperado: lista vacía: el rollback deshizo las dos inserciones correctas
//        prueba("D.7 · Libros al final", catalogo::listarLibros);
//        // Esperado: los 13 iniciales
//    }

//    private static void probarCrudSocios(SocioService socios) {
//        // D.1 insertar 1
//        Socio uxia = new Socio(0, "Uxía", "Castro Pena", "uxia.castro@biblio.gal", "ESTUDIANTE", FECHA_REF);
//        prueba("D.1 · Insertar socia Uxía", () -> socios.insertarSocio(uxia));
//        // Esperado: 11
//
//        // D.2 insertar listado (lote)
//        Socio xoan = new Socio(0, "Xoán", "Mouriño Abal", "xoan.mourino@biblio.gal", "DOCENTE", FECHA_REF);
//        Socio antia = new Socio(0, "Antía", "Soto Leis", "antia.soto@biblio.gal", "ESTUDIANTE", FECHA_REF);
//        prueba("D.2 · Insertar lote de socios", () -> socios.insertarSocios(List.of(xoan, antia)));
//        // Esperado: [1, 1]
//        prueba("D.2 · Objetos tras el lote (ids asignados)", () -> List.of(xoan, antia));
//        // Esperado: 12: Xoán..., 13: Antía...
//
//        // D.3 actualizar 1
//        uxia.setTipo("DOCENTE");
//        prueba("D.3 · Actualizar tipo de Uxía", () -> socios.actualizarSocio(uxia));
//        // Esperado: true
//        prueba("D.3 · Socio " + uxia.getId(), () -> socios.buscarSocio(uxia.getId()));
//        // Esperado: 11: Uxía Castro Pena <uxia.castro@biblio.gal> DOCENTE
//
//        // D.4 actualizar listado (lote)
//        xoan.setEmail("xoan.mourino@edu.biblio.gal");
//        antia.setEmail("antia.soto@edu.biblio.gal");
//        prueba("D.4 · Actualizar lote de socios", () -> socios.actualizarSocios(List.of(xoan, antia)));
//        // Esperado: [1, 1]
//
//        // Errores de integridad
//        prueba("D.1 · Insertar socio con email repetido", () -> socios.insertarSocio(
//                new Socio(0, "Otra", "Alba", "alba.seoane@biblio.gal", "ESTUDIANTE", FECHA_REF)));
//        // Esperado: ERROR SQLState 23000 / código 1062
//        prueba("D.5 · Eliminar socio 1 (tiene préstamos)", () -> socios.eliminarSocio(1));
//        // Esperado: ERROR SQLState 23000 / código 1451
//
//        // D.5 eliminar 1 y D.6 eliminar listado (lote)
//        prueba("D.5 · Eliminar a Uxía", () -> socios.eliminarSocio(uxia.getId()));
//        // Esperado: true
//        prueba("D.6 · Eliminar lote de socios", () -> socios.eliminarSocios(List.of(xoan.getId(), antia.getId())));
//        // Esperado: [1, 1]
//        prueba("D.6 · Socios al final", socios::listarSocios);
//        // Esperado: los 10 iniciales
//    }

//    private static void probarCrudPrestamos(PrestamoService prestamos) {
//        LocalDate limite = FECHA_REF.plusDays(15);
//
//        // D.1 insertar 1: Nuno (10) se lleva Follas novas (12). Sin devolver: fecha null
//        Prestamo p1 = new Prestamo(0, 10, 12, FECHA_REF, limite, null);
//        prueba("D.1 · Insertar préstamo", () -> prestamos.insertarPrestamo(p1));
//        // Esperado: 39
//
//        // D.2 insertar listado (lote)
//        Prestamo p2 = new Prestamo(0, 10, 13, FECHA_REF, limite, null);
//        Prestamo p3 = new Prestamo(0, 9, 2, FECHA_REF, FECHA_REF.plusDays(30), null);
//        prueba("D.2 · Insertar lote de préstamos", () -> prestamos.insertarPrestamos(List.of(p2, p3)));
//        // Esperado: [1, 1]
//        prueba("D.2 · Préstamos activos del socio 10", () -> prestamos.activosDeSocio(10));
//        // Esperado: los préstamos 39 y 40
//
//        // D.3 actualizar 1: devolver el préstamo
//        p1.setFechaDevolucion(LocalDate.of(2026, 10, 20));
//        prueba("D.3 · Devolver préstamo " + p1.getId(), () -> prestamos.actualizarPrestamo(p1));
//        // Esperado: true
//        prueba("D.3 · Préstamo " + p1.getId(), () -> prestamos.buscarPrestamo(p1.getId()));
//        // Esperado: ..., devuelto el 2026-10-20
//
//        // D.4 actualizar listado (lote): devolver los dos
//        p2.setFechaDevolucion(LocalDate.of(2026, 10, 22));
//        p3.setFechaDevolucion(LocalDate.of(2026, 10, 22));
//        prueba("D.4 · Devolver lote de préstamos", () -> prestamos.actualizarPrestamos(List.of(p2, p3)));
//        // Esperado: [1, 1]
//        prueba("D.4 · Préstamos activos del socio 10", () -> prestamos.activosDeSocio(10));
//        // Esperado: lista vacía
//
//        // Errores de integridad
//        prueba("D.1 · Insertar préstamo del socio 99",
//                () -> prestamos.insertarPrestamo(new Prestamo(0, 99, 1, FECHA_REF, limite, null)));
//        // Esperado: ERROR SQLState 23000 / código 1452 (foreign key)
//        prueba("D.1 · Insertar préstamo con fecha límite anterior",
//                () -> prestamos.insertarPrestamo(new Prestamo(0, 10, 1, FECHA_REF, FECHA_REF.minusDays(1), null)));
//        // Esperado: ERROR SQLState HY000 / código 3819 (check constraint)
//
//        // D.5 eliminar 1 y D.6 eliminar listado (lote)
//        prueba("D.5 · Eliminar préstamo " + p1.getId(), () -> prestamos.eliminarPrestamo(p1.getId()));
//        // Esperado: true
//        prueba("D.6 · Eliminar lote de préstamos",
//                () -> prestamos.eliminarPrestamos(List.of(p2.getId(), p3.getId())));
//        // Esperado: [1, 1]
//        prueba("D.6 · Préstamos al final", prestamos::listarPrestamos);
//        // Esperado: los 38 iniciales
//    }

    // ------------------------------------------------------------------
    // Utilidades para mostrar resultados (no hace falta tocarlas)
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface Prueba<R> {
        R ejecutar() throws SQLException;
    }

    /** Ejecuta la prueba, muestra el resultado y lo devuelve (null si falla). */
    private static <R> R prueba(String titulo, Prueba<R> prueba) {
        System.out.println("\n=== " + titulo + " ===");
        try {
            R resultado = prueba.ejecutar();
            mostrar(resultado);
            return resultado;
        } catch (UnsupportedOperationException ex) {
            System.out.println("  (pendiente: " + ex.getMessage() + ")");
        } catch (BatchUpdateException ex) {
            System.out.println("  ERROR en el lote [SQLState " + ex.getSQLState() + " / código "
                    + ex.getErrorCode() + "]: " + ex.getMessage());
            System.out.println("  Resultado por sentencia: " + Arrays.toString(ex.getUpdateCounts())
                    + "  (-3 = la sentencia falló)");
        } catch (SQLException ex) {
            System.out.println("  ERROR de BD [SQLState " + ex.getSQLState() + " / código "
                    + ex.getErrorCode() + "]: " + ex.getMessage());
        } catch (RuntimeException ex) {
            System.out.println("  " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return null;
    }

    private static void mostrar(Object resultado) {
        if (resultado instanceof Collection<?> lista) {
            if (lista.isEmpty()) {
                System.out.println("  (lista vacía)");
            }
            lista.forEach(e -> System.out.println("  " + e));
            System.out.println("  -> " + lista.size() + " resultado(s)");
        } else if (resultado instanceof Optional<?> opt) {
            System.out.println(opt.map(o -> "  " + o).orElse("  (Optional vacío)"));
        } else if (resultado instanceof int[] filas) {
            System.out.println("  Filas afectadas por sentencia: " + Arrays.toString(filas));
        } else {
            System.out.println("  " + resultado);
        }
    }
}
