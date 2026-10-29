package gal.dam.biblioteca.dao;

import gal.dam.biblioteca.dto.LibroDisponible;
import gal.dam.biblioteca.modelo.Libro;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO de la tabla libro.
 * <p>
 * Tiene que heredar de DAO&lt;Libro, Integer&gt; buscarPorId y listarTodos.
 * <p>
 * TODO: declara aquí las consultas específicas del enunciado que correspondan a
 * este DAO. Todas reciben la Connection como primer parámetro y lanzan SQLException.
 */
public interface LibroDAO extends DAO<Libro,Integer>{
    //B.1
    List<Libro> buscarPorTitulo(Connection con, String texto) throws SQLException;
    //B.2
    List<Libro> deAutor(Connection con, String nombreAutor) throws SQLException;
    //B.3
    List<String> autoresDe(Connection con, int idLibro) throws SQLException;
    //B.6
    List<Libro> prestadosASocioDeGenero(Connection con, int idSocio, int idGenero) throws SQLException;
    //B.7
    List<Libro> nuncaPrestados(Connection con) throws SQLException;
    //B.11
    List<LibroDisponible> disponibilidad(Connection con) throws SQLException;
    
    
}
