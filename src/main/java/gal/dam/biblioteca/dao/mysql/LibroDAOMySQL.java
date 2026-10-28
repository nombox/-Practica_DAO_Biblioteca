package gal.dam.biblioteca.dao.mysql;

import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.dto.FichaSocio;
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
    public List<Libro> buscarPorTitulo(Connection con, String texto) throws SQLException {
        
        String sql = "SELECT * FROM biblioteca.libro where titulo like ?";
        List<Libro> libros = new ArrayList<>();
        
        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setString(1, String.format("%%%s%%", texto));
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next())
                    libros.add(mapear(rs));
            }
        }
        return libros;
    }

    @Override
    public List<Libro> deAutor(Connection con, String nombreAutor) throws SQLException {
        String sql = "select l.id, l.isbn, l.titulo, l.anio, l.ejemplares, l.id_genero "
                + "from libro l inner join libro_autor la on l.id = la.id_libro "
                + "inner join autor a on la.id_autor = a.id "
                + "where a.nombre like ?";
        List<Libro> libros = new ArrayList<>();
        
        try(PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setString(1, String.format("%%%s%%", nombreAutor));
            
            try(ResultSet rs = ps.executeQuery()){
            while(rs.next())
                libros.add(mapear(rs));
            }
        }
        return libros;
    }

    @Override
    public List<String> autoresDe(Connection con, int idLibro) throws SQLException {
        String sql = "select a.nombre "
                + "from libro l "
                + "inner join libro_autor la on l.id = la.id_libro inner join autor a on la.id_autor = a.id "
                + "where l.id like ? "
                + "order by a.nombre";
        List<String> autores = new ArrayList<>();
        try(PreparedStatement ps = con.prepareStatement(sql)){
          ps.setInt(1, idLibro);
          try(ResultSet rs = ps.executeQuery()){
              while(rs.next())
                  autores.add(rs.getString("a.nombre"));
          }
        }
        return autores;
    }

    @Override
    public List<Libro> prestadosASocioDeGenero(Connection con, int idSocio, int idGenero) throws SQLException {
        String sql = "SELECT DISTINCT l.id, l.isbn, l.titulo, l.anio, l.ejemplares, l.id_genero "
                + "FROM libro l inner join prestamo p on l.id = p.id_libro "
                + "where p.id_socio = ? and l.id_genero = ? "
                + "order by l.titulo;";
        List<Libro> libros = new ArrayList<>(); 

        try(PreparedStatement ps = con.prepareStatement(sql)){
            ps.setInt(1, idSocio);
            ps.setInt(2, idGenero);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next())
                libros.add(mapear(rs));
            }
        }
        return libros;
    }

    @Override
    public List<Libro> nuncaPrestados(Connection con) throws SQLException {
        // Alternativa = select l.* from biblioteca.libro l left join biblioteca.prestamo p on l.id = p.id_libro where p.id is null;
        String sql = "SELECT * "
                + "FROM biblioteca.libro "
                + "where not exists("
                + "select * "
                + "from biblioteca.prestamo "
                + "where biblioteca.libro.id = biblioteca.prestamo.id_libro);";
        List<Libro> libros = new ArrayList<>();
        try(PreparedStatement ps = con.prepareStatement(sql)){
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next())
                    libros.add(mapear(rs));   
            }
            
        }
        return libros;
    }
}
