package gal.dam.biblioteca.dao.mysql;

import gal.dam.biblioteca.dao.DAO;
import gal.dam.biblioteca.modelo.Genero;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación con JDBC (MySQL) de DAO&lt;Genero, Integer&gt;.
 * <p>
 * CLASE ENTREGADA COMO EJEMPLO: así deben quedar los demás DAO.
 * <ul>
 * <li>Recibe la Connection y NO la cierra (no va en el try-with-resources).</li>
 * <li>PreparedStatement y ResultSet sí van en try-with-resources.</li>
 * <li>Cada fila se convierte en objeto con mapear().</li>
 * <li>Los métodos ...Lote usan UN PreparedStatement, addBatch() y UN executeBatch().</li>
 * </ul>
 * Género no tiene interfaz propia porque no tiene consultas específicas:
 * implementa directamente la interfaz genérica.
 */
public class GeneroDAOMySQL implements DAO<Genero, Integer> {

    // ---------- Consulta ----------

    @Override
    public Optional<Genero> buscarPorId(Connection con, Integer id) throws SQLException {
        String sql = "SELECT id, nombre FROM genero WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public List<Genero> listarTodos(Connection con) throws SQLException {
        String sql = "SELECT id, nombre FROM genero ORDER BY nombre";
        List<Genero> generos = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                generos.add(mapear(rs));
            }
        }
        return generos;
    }

    // ---------- Inserción ----------

    @Override
    public Integer insertar(Connection con, Genero g) throws SQLException {
        String sql = "INSERT INTO genero (nombre) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getNombre());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                g.setId(claves.getInt(1));
            }
        }
        return g.getId();
    }

    @Override
    public int[] insertarLote(Connection con, List<Genero> lista) throws SQLException {
        String sql = "INSERT INTO genero (nombre) VALUES (?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (Genero g : lista) {
                ps.setString(1, g.getNombre());
                ps.addBatch();
            }
            int[] filas = ps.executeBatch();
            // Las claves generadas llegan en el mismo orden que la lista
            try (ResultSet claves = ps.getGeneratedKeys()) {
                int i = 0;
                while (claves.next()) {
                    lista.get(i++).setId(claves.getInt(1));
                }
            }
            return filas;
        }
    }

    // ---------- Actualización ----------

    @Override
    public boolean actualizar(Connection con, Genero g) throws SQLException {
        String sql = "UPDATE genero SET nombre = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, g.getNombre());
            ps.setInt(2, g.getId());
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int[] actualizarLote(Connection con, List<Genero> lista) throws SQLException {
        String sql = "UPDATE genero SET nombre = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Genero g : lista) {
                ps.setString(1, g.getNombre());
                ps.setInt(2, g.getId());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    // ---------- Borrado ----------

    @Override
    public boolean eliminar(Connection con, Integer id) throws SQLException {
        String sql = "DELETE FROM genero WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public int[] eliminarLote(Connection con, List<Integer> ids) throws SQLException {
        String sql = "DELETE FROM genero WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Integer id : ids) {
                ps.setInt(1, id);
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    /** Convierte la fila actual del ResultSet en un Genero. */
    private Genero mapear(ResultSet rs) throws SQLException {
        return new Genero(rs.getInt("id"), rs.getString("nombre"));
    }
}
