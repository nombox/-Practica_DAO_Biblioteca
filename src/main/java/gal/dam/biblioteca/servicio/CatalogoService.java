package gal.dam.biblioteca.servicio;

import gal.dam.biblioteca.config.ConexionBD;
import gal.dam.biblioteca.dao.DAO;
import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.dto.LibroDisponible;
import gal.dam.biblioteca.dto.LibroPrestamos;
import gal.dam.biblioteca.modelo.Genero;
import gal.dam.biblioteca.modelo.Libro;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones sobre el catálogo: géneros, libros y autores.
 * <p>
 * Patrón de TODOS los métodos de servicio: abrir la conexión en un
 * try-with-resources, llamar a uno o varios DAO pasándoles esa conexión
 * y devolver el resultado. El servicio no escribe SQL.
 * <p>
 * listarGeneros() y buscarGenero() están hechos como ejemplo.
 */
public class CatalogoService {

    private final LibroDAO libroDAO;
    private final DAO<Genero, Integer> generoDAO;

    public CatalogoService(LibroDAO libroDAO, DAO<Genero, Integer> generoDAO) {
        this.libroDAO = libroDAO;
        this.generoDAO = generoDAO;
    }

    // ---------- Ejemplos dados para ver cómo funcionan ----------

    public List<Genero> listarGeneros() throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.listarTodos(con);
        }
    }

    public Optional<Genero> buscarGenero(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.buscarPorId(con, id);
        }
    }

    // ---------- TODO ----------

    public List<Libro> listarLibros() throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.listarTodos(con);
        }
    }

    public Optional<Libro> buscarLibro(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.buscarPorId(con, id);
        }
    }

    public List<Libro> buscarPorTitulo(String texto) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.buscarPorTitulo(con, texto);
        }
    }

    public List<Libro> librosDeAutor(String nombreAutor) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return libroDAO.deAutor(con, nombreAutor);
        }
    }

    public List<String> autoresDe(int idLibro) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return libroDAO.autoresDe(con, idLibro);
        }
    }

    public List<Libro> nuncaPrestados() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return libroDAO.nuncaPrestados(con);
        }
    }

    public List<LibroDisponible> disponibilidad() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return libroDAO.disponibilidad(con);
        }
    }

    public List<LibroPrestamos> masPrestadoPorGenero() throws SQLException {
       try(Connection con = ConexionBD.getConexion()){
           return libroDAO.masPrestadoPorGenero(con);
       }
    }
    
    //------------------------INSERCIONES BLOQUE D------------------------------
    
     public int insertarGenero(Genero genero) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.insertar(con, genero);
        }
    }

    public int[] insertarGeneros(List<Genero> generos) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.insertarLote(con, generos);
        }
    }

    public boolean actualizarGenero(Genero genero) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.actualizar(con, genero);
        }
    }

    public int[] actualizarGeneros(List<Genero> generos) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.actualizarLote(con, generos);
        }
    }

    public boolean eliminarGenero(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.eliminar(con, id);
        }
    }

    public int[] eliminarGeneros(List<Integer> ids) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return generoDAO.eliminarLote(con, ids);
        }
    }

    public int insertarLibro(Libro libro) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.insertar(con, libro);
        }
    }

    public int[] insertarLibros(List<Libro> libros, boolean transaccional) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            if (!transaccional) {
                return libroDAO.insertarLote(con, libros);
            } else {
                con.setAutoCommit(false);
                try {
                    int[] filas = libroDAO.insertarLote(con, libros);
                    con.commit();
                    return filas;
                } catch (SQLException ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }
        }
    }

    public boolean actualizarLibro(Libro libro) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.actualizar(con, libro);
        }
    }

    public int[] actualizarLibros(List<Libro> libros) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.actualizarLote(con, libros);
        }
    }

    public boolean eliminarLibro(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.eliminar(con, id);
        }
    }

    public int[] eliminarLibros(List<Integer> ids) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return libroDAO.eliminarLote(con, ids);
        }
    }
}
