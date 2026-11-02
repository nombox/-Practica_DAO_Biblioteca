package gal.dam.biblioteca.dao.mysql;

import gal.dam.biblioteca.dao.SocioDAO;
import gal.dam.biblioteca.modelo.Socio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación con JDBC (MySQL) de SocioDAO. Para que desaparezcan los
 * errores: El DAO tiene que hacer el extends
 * <p>
 * Normas: PreparedStatement para cualquier valor que venga de fuera,
 * PreparedStatement y ResultSet en try-with-resources, la Connection NUNCA (la
 * cierra quien la abrió: el servicio). Cada fila se convierte en objeto con
 * mapear().
 */
public class SocioDAOMySQL implements SocioDAO {

    @Override
    public Optional<Socio> buscarPorId(Connection con, Integer id) throws SQLException {
        // TODO
        String sql = "SELECT id, nombre, apellidos, email, tipo, fecha_alta from socio where id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }

    }

    @Override
    public List<Socio> listarTodos(Connection con) throws SQLException {
        // TODO
        String sql = "SELECT id, nombre, apellidos, email, tipo, fecha_alta from socio order by apellidos";
        List<Socio> socios = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                socios.add(mapear(rs));
            }
        }
        return socios;

    }

    // TODO: consultas específicas
    /**
     * Convierte la fila actual del ResultSet en un Socio.
     */
    private Socio mapear(ResultSet rs) throws SQLException {
        Socio socio = new Socio(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("apellidos"),
                rs.getString("email"),
                rs.getString("tipo"),
                rs.getObject("fecha_alta", LocalDate.class));
        return socio;
    }

    @Override
    public Optional<Socio> buscarPorEmail(Connection con, String email) throws SQLException {
        String sql = "SELECT * FROM biblioteca.socio where email like ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Socio> conPrestamosVencidos(Connection con, LocalDate fechaRef) throws SQLException {
        String sql = "select distinct s.* \n"
                + "from socio s \n"
                + "inner join prestamo p on s.id = p.id_socio \n"
                + "where p.fecha_devolucion is null and fecha_limite<? order by s.apellidos;";
        List<Socio> socios = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, fechaRef);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    socios.add(mapear(rs));
                }
            }
        }

        return socios;
    }

    @Override
    public List<Socio> lectoresDeTodoElAutor(Connection con, int idAutor) throws SQLException {
        String sql = "select s.id, s.nombre, s.apellidos, s.email, s.tipo, s.fecha_alta \n"
                + "from socio s\n"
                + "where not exists(\n"
                + "	select 1\n"
                + "    from libro_autor la\n"
                + "    where la.id_autor = ? and\n"
                + "    not exists(\n"
                + "	   select 1\n"
                + "        from prestamo p\n"
                + "        where la.id_libro = p.id_libro\n"
                + "        and p.id_socio = s.id\n"
                + "    )\n"
                + ")order by s.apellidos;";
        List<Socio> socios = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAutor);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    socios.add(mapear(rs));
                }
            }
        }
        return socios;
    }

    @Override
    public List<Socio> lectoresSobreLaMedia(Connection con) throws SQLException {
        String sql = "select s.id, s.nombre, s.apellidos, s.email, s.tipo, s.fecha_alta, count(p.id) as prestamos \n"
                + "from prestamo p \n"
                + "inner join socio s on p.id_socio = s.id \n"
                + "group by s.id\n"
                + "having count(p.id)>(\n"
                + "Select avg(promedio_prestamos)\n"
                + "from\n"
                + "(select count(id) as promedio_prestamos\n"
                + "from prestamo\n"
                + "group by id_socio) as tabla_derivada\n"
                + ");";
        List<Socio> socios = new ArrayList<>();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    socios.add(mapear(rs));
                }
            }
        }

        return socios;
    }

    @Override
    public Integer insertar(Connection con, Socio t) throws SQLException {
        String sql = "INSERT INTO socio (nombre, apellidos, email, tipo, fecha_alta) values (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getApellidos());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getTipo());
            ps.setObject(5, t.getFechaAlta());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                t.setId(rs.getInt(1));
            }

        }
        return t.getId();
    }

    @Override
    public int[] insertarLote(Connection con, List<Socio> lista) throws SQLException {
        String sql = "INSERT INTO socio (nombre, apellidos, email, tipo, fecha_alta) values (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Socio t : lista) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getApellidos());
                ps.setString(3, t.getEmail());
                ps.setString(4, t.getTipo());
                ps.setObject(5, t.getFechaAlta());
                ps.addBatch();
            }
            int[] filas = ps.executeBatch();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                int i = 0;

                while (rs.next()) {
                    lista.get(i++).setId(rs.getInt(1));
                }
            }
            return filas;
        }

    }

    @Override
    public boolean actualizar(Connection con, Socio t) throws SQLException {
        String sql = "UPDATE socio set nombre = ?, apellidos = ?, email = ?, tipo = ?, fecha_alta = ? where id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getApellidos());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getTipo());
            ps.setObject(5, t.getFechaAlta());
            ps.setInt(6, t.getId());

            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int[] actualizarLote(Connection con, List<Socio> lista) throws SQLException {
        String sql = "UPDATE socio set nombre = ?, apellidos = ?, email = ?, tipo = ?, fecha_alta = ? where id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Socio t : lista) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getApellidos());
                ps.setString(3, t.getEmail());
                ps.setString(4, t.getTipo());
                ps.setObject(5, t.getFechaAlta());
                ps.setInt(6, t.getId());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    @Override
    public boolean eliminar(Connection con, Integer id) throws SQLException {
        String sql = "DELETE FROM socio where id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int[] eliminarLote(Connection con, List<Integer> ids) throws SQLException {
        String sql = "DELETE FROM socio where id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Integer id : ids) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }
}
