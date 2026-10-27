package gal.dam.biblioteca.dao.mysql;

import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.modelo.Libro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación con JDBC (MySQL) de LibroDAO. Para que desaparezcan los
 * errores: El DAO tiene que hacer el extends
 * <p>
 * Normas: PreparedStatement para cualquier valor que venga de fuera,
 * PreparedStatement y ResultSet en try-with-resources, la Connection NUNCA (la
 * cierra quien la abrió: el servicio). Cada fila se convierte en objeto con
 * mapear().
 */
public class LibroDAOMySQL implements LibroDAO {

    @Override
    public Optional<Libro> buscarPorId(Connection con, Integer id) throws SQLException {
        // TODO
        String sql = "SELECT id, isbn, titulo, anio, ejemplares, id_genero from libro where id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }

    }

    @Override
    public List<Libro> listarTodos(Connection con) throws SQLException {
        String sql = "SELECT id, isbn, titulo, anio, ejemplares, id_genero FROM biblioteca.libro Order by titulo asc";
        List<Libro> libros = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql); 
                ResultSet rs = ps.executeQuery();) {

            while (rs.next()) {
                libros.add(mapear(rs));
            }
        }
        return libros;
    }

    // TODO: consultas específicas
    /**
     * Convierte la fila actual del ResultSet en un Libro.
     */
    private Libro mapear(ResultSet rs) throws SQLException {

        Libro libro = new Libro(
                rs.getInt("id"), 
                rs.getString("isbn"), 
                rs.getString("titulo"), 
                rs.getInt("anio"), 
                rs.getInt("ejemplares"), 
                rs.getInt("id_genero"));
        
        return libro;
    }

    @Override
    public List<Libro> buscarPorTitulo(String texto) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
