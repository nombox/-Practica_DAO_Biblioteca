package gal.dam.biblioteca.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones comunes a todos los DAO.
 *
 * @param <T> tipo de la entidad (Libro, Socio...)
 * @param <K> tipo de su clave primaria (Integer en esta práctica)
 *
 * Todos los métodos reciben la conexión como primer parámetro: el DAO la usa,
 * pero no la abre ni la cierra. Así varias operaciones de varios DAO pueden
 * compartir la misma conexión (y, más adelante, la misma transacción).
 *
 * Los métodos ...Lote trabajan con listas y usan ejecución por lotes: UN solo
 * PreparedStatement, addBatch() por cada elemento y UN executeBatch() al final.
 * Devuelven el array de executeBatch(): las filas afectadas por cada sentencia,
 * en el mismo orden que la lista.
 */
public interface DAO<T, K> {

    // ---------- Consulta ----------

    /** Devuelve la entidad con esa clave, u Optional.empty() si no existe. */
    Optional<T> buscarPorId(Connection con, K id) throws SQLException;

    /** Devuelve todas las entidades, en el orden que indica el enunciado. */
    List<T> listarTodos(Connection con) throws SQLException;

    // ---------- Inserción ----------

    /**
     * Inserta la entidad (su id se ignora: lo genera la base de datos).
     * Asigna al objeto el id generado y lo devuelve.
     */
    K insertar(Connection con, T t) throws SQLException;

    /**
     * Inserta todas las entidades de la lista en un lote.
     * Asigna a cada objeto de la lista su id generado.
     */
    int[] insertarLote(Connection con, List<T> lista) throws SQLException;

    // ---------- Actualización ----------

    /**
     * Actualiza todas las columnas de la fila cuyo id es el del objeto.
     * Devuelve false si no existe ninguna fila con ese id.
     */
    boolean actualizar(Connection con, T t) throws SQLException;

    /** Actualiza todas las entidades de la lista en un lote. */
    int[] actualizarLote(Connection con, List<T> lista) throws SQLException;

    // ---------- Borrado ----------

    /** Elimina la fila con esa clave. Devuelve false si no existía. */
    boolean eliminar(Connection con, K id) throws SQLException;

    /** Elimina en un lote las filas con esas claves (0 en la posición de las que no existían). */
    int[] eliminarLote(Connection con, List<K> ids) throws SQLException;
}
