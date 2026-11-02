package gal.dam.biblioteca.servicio;

import gal.dam.biblioteca.config.ConexionBD;
import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.dao.PrestamoDAO;
import gal.dam.biblioteca.dao.SocioDAO;
import gal.dam.biblioteca.dto.PrestamoDetalle;
import gal.dam.biblioteca.dto.PrestamoVencido;
import gal.dam.biblioteca.dto.ResumenGenero;
import gal.dam.biblioteca.modelo.Prestamo;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Operaciones sobre los préstamos.
 */
public class PrestamoService {

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;
    private final SocioDAO socioDAO;

    public PrestamoService(PrestamoDAO prestamoDAO, LibroDAO libroDAO, SocioDAO socioDAO) {
        this.prestamoDAO = prestamoDAO;
        this.libroDAO = libroDAO;
        this.socioDAO = socioDAO;
    }

    // ---------- TODO ----------

    public List<Prestamo> listarPrestamos() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return prestamoDAO.listarTodos(con);
        }
    }

    public Optional<Prestamo> buscarPrestamo(int id) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return prestamoDAO.buscarPorId(con,id);
        }
    }

    public List<Prestamo> activosDeSocio(int idSocio) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return prestamoDAO.activosDeSocio(con, idSocio);
        }
    }

    public List<PrestamoDetalle> historial(int idSocio) throws SQLException {
       try(Connection con = ConexionBD.getConexion()){
           return prestamoDAO.historial(con, idSocio);
       }
    }

    public List<PrestamoVencido> vencidos(LocalDate fechaRef) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return prestamoDAO.vencidos(con, fechaRef);
        }
    }

    public List<ResumenGenero> resumenPorGenero() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return prestamoDAO.resumenPorGenero(con);
        }
    }
    
    //------------------------INSERCIONES BLOQUE D------------------------------
    // PrestamoService
    public int insertarPrestamo(Prestamo prestamo) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.insertar(con, prestamo);
        }
    }

    public int[] insertarPrestamos(List<Prestamo> prestamos) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.insertarLote(con, prestamos);
        }
    }

    public boolean actualizarPrestamo(Prestamo prestamo) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.actualizar(con, prestamo);
        }
    }

    public int[] actualizarPrestamos(List<Prestamo> prestamos) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.actualizarLote(con, prestamos);
        }
    }

    public boolean eliminarPrestamo(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.eliminar(con, id);
        }
    }

    public int[] eliminarPrestamos(List<Integer> ids) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return prestamoDAO.eliminarLote(con, ids);
        }
    }

}
