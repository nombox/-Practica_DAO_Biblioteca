package gal.dam.biblioteca.dao;

import gal.dam.biblioteca.dto.PrestamoDetalle;

import gal.dam.biblioteca.modelo.Prestamo;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO de la tabla prestamo.
 * <p>
 * Tiene que heredar de DAO&lt;Prestamo, Integer&gt; buscarPorId y listarTodos.
 * <p>
 * TODO: declara aquí las consultas específicas del enunciado que correspondan a
 * este DAO. Todas reciben la Connection como primer parámetro y lanzan SQLException.
 */
public interface PrestamoDAO extends DAO<Prestamo,Integer>{
    //B.5
     List<Prestamo> activosDeSocio(Connection con, int idSocio) throws SQLException;
    //B.9
     List<PrestamoDetalle> historial(Connection con, int idSocio) throws SQLException;
     
}
