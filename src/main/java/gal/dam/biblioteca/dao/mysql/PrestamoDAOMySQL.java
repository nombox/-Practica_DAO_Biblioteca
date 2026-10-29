package gal.dam.biblioteca.dao.mysql;

import gal.dam.biblioteca.dao.PrestamoDAO;
import gal.dam.biblioteca.dto.PrestamoDetalle;
import gal.dam.biblioteca.dto.PrestamoVencido;
import gal.dam.biblioteca.modelo.Prestamo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementación con JDBC (MySQL) de PrestamoDAO. Para que desaparezcan los
 * errores: El DAO tiene que hacer el extends
 * <p>
 * Normas: PreparedStatement para cualquier valor que venga de fuera,
 * PreparedStatement y ResultSet en try-with-resources, la Connection NUNCA (la
 * cierra quien la abrió: el servicio). Cada fila se convierte en objeto con
 * mapear().
 */
public class PrestamoDAOMySQL implements PrestamoDAO {

    @Override
    public Optional<Prestamo> buscarPorId(Connection con, Integer id) throws SQLException {
        // TODO
        String sql = "SELECT id, id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion "
                + "FROM prestamo "
                + "where id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }

        }

    }

    @Override
    public List<Prestamo> listarTodos(Connection con) throws SQLException {
        // TODO
        String sql = "SELECT id, id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion "
                + "FROM prestamo "
                + "order by fecha_prestamo, id;";
        List<Prestamo> prestamos = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql); 
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prestamos.add(mapear(rs));
            }
        }
        return prestamos;
    }

    // TODO: consultas específicas
    /**
     * Convierte la fila actual del ResultSet en un Prestamo.
     */
    private Prestamo mapear(ResultSet rs) throws SQLException {
        // TODO
        Prestamo prestamo = new Prestamo(
                rs.getInt("id"),
                rs.getInt("id_socio"),
                rs.getInt("id_libro"),
                rs.getObject("fecha_prestamo", LocalDate.class),
                rs.getObject("fecha_limite", LocalDate.class),
                rs.getObject("fecha_devolucion", LocalDate.class));
        return prestamo;

    }

    @Override
    public List<Prestamo> activosDeSocio(Connection con, int idSocio) throws SQLException {
        String sql = "SELECT id, id_socio, id_libro, fecha_prestamo, fecha_limite, fecha_devolucion "
                + "FROM biblioteca.prestamo "
                + "where fecha_devolucion is null and id_socio = ?";
        List<Prestamo> prestamos = new ArrayList<>();
        
        try(PreparedStatement ps = con.prepareStatement(sql)){
            
            ps.setInt(1, idSocio);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next())
                    prestamos.add(mapear(rs));
            }
        }
        return prestamos;
    }

    @Override
    public List<PrestamoDetalle> historial(Connection con, int idSocio) throws SQLException {
        
        String sql = "SELECT p.id, l.titulo, group_concat(a.nombre order by a.nombre separator \", \"  ) as autores, p.fecha_prestamo, p.fecha_limite, p.fecha_devolucion \n" +
"FROM prestamo p \n" +
"inner join libro l on p.id_libro = l.id \n" +
"inner join libro_autor la on p.id_libro = la.id_libro \n" +
"inner join autor a on la.id_autor = a.id \n" +
"where p.id_socio = ? \n" +
"group by p.id \n" +
"order by p.fecha_prestamo;";
        List<PrestamoDetalle> prestamosDetalles = new ArrayList<>();
        
        try(PreparedStatement ps = con.prepareStatement(sql)){
           ps.setInt(1, idSocio);
           
           try(ResultSet rs = ps.executeQuery()){
                while(rs.next())
                    prestamosDetalles.add(new PrestamoDetalle(
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("autores"),
                            rs.getObject("fecha_prestamo", LocalDate.class),
                            rs.getObject("fecha_limite", LocalDate.class),
                            rs.getObject("fecha_devolucion", LocalDate.class)
                    ));
           }
        }
        return prestamosDetalles;
        
    }

    @Override
    public List<PrestamoVencido> vencidos(Connection con, LocalDate fechaRef) throws SQLException {
        String sql
                = "select p.id, concat(s.nombre, \" \", s.apellidos) as nombre_completo, l.titulo, p.fecha_limite, datediff(?, p.fecha_limite) as dias_retraso \n"
                + "from prestamo p \n"
                + "inner join socio s on p.id_socio = s.id \n"
                + "inner join libro l on p.id_libro = l.id \n"
                + "where fecha_devolucion is null and datediff(?, p.fecha_limite) > 0 \n"
                + "order by dias_retraso desc;";

        List<PrestamoVencido> vencidos = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setObject(1, fechaRef);
            ps.setObject(2, fechaRef);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    vencidos.add(new PrestamoVencido(
                            rs.getInt("id"),
                            rs.getString("nombre_completo"),
                            rs.getString("titulo"),
                            rs.getObject("fecha_limite", LocalDate.class),
                            rs.getInt("dias_retraso")
                    ));
                }
            }
        }
        return vencidos;
    }
}
