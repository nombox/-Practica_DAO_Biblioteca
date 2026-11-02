package gal.dam.biblioteca.servicio;

import gal.dam.biblioteca.config.ConexionBD;
import gal.dam.biblioteca.dao.LibroDAO;
import gal.dam.biblioteca.dao.PrestamoDAO;
import gal.dam.biblioteca.dao.SocioDAO;
import gal.dam.biblioteca.dto.FichaSocio;
import gal.dam.biblioteca.modelo.Libro;
import gal.dam.biblioteca.modelo.Prestamo;
import gal.dam.biblioteca.modelo.Socio;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Operaciones sobre los socios.
 * <p>
 * Fíjate en que este servicio recibe tres DAO: algunas operaciones sobre un
 * socio necesitan datos de otras tablas (y por tanto de otros DAO).
 */
public class SocioService {

    private final SocioDAO socioDAO;
    private final LibroDAO libroDAO;
    private final PrestamoDAO prestamoDAO;

    public SocioService(SocioDAO socioDAO, LibroDAO libroDAO, PrestamoDAO prestamoDAO) {
        this.socioDAO = socioDAO;
        this.libroDAO = libroDAO;
        this.prestamoDAO = prestamoDAO;
    }

    // ---------- TODO ----------

    public List<Socio> listarSocios() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return socioDAO.listarTodos(con);
        }
    }

    public Optional<Socio> buscarSocio(int id) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return socioDAO.buscarPorId(con, id);
        }
    }

    public Optional<Socio> buscarPorEmail(String email) throws SQLException {
       try(Connection con = ConexionBD.getConexion()){
           return socioDAO.buscarPorEmail(con, email);
       }
    }

    /**
     * El socio con sus préstamos activos. Usa DOS DAO con UNA sola conexión.
     * Si el socio no existe, lanza NoSuchElementException("No existe el socio " + idSocio).
     */
    public FichaSocio fichaSocio(int idSocio) throws SQLException {
        
        try(Connection con = ConexionBD.getConexion()){
            Socio socio = socioDAO.buscarPorId(con, idSocio)
                    .orElseThrow(()-> new NoSuchElementException("No existe el socio " + idSocio));
                    
            return new FichaSocio(socio, prestamoDAO.activosDeSocio(con, idSocio));
        }
        
    }

    public List<Libro> librosLeidosDeGenero(int idSocio, int idGenero) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return libroDAO.prestadosASocioDeGenero(con, idSocio, idGenero);
        }
    }

    public List<Socio> conPrestamosVencidos(LocalDate fechaRef) throws SQLException {
       
       try(Connection con = ConexionBD.getConexion()){
            return socioDAO.conPrestamosVencidos(con, fechaRef);
       }
    }

    public List<Socio> lectoresDeTodoElAutor(int idAutor) throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return socioDAO.lectoresDeTodoElAutor(con, idAutor);
        }
    }

    public List<Socio> lectoresSobreLaMedia() throws SQLException {
        try(Connection con = ConexionBD.getConexion()){
            return socioDAO.lectoresSobreLaMedia(con);
        }
    }
    
    //------------------------INSERCIONES BLOQUE D------------------------------
    // SocioService
    public int insertarSocio(Socio socio) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.insertar(con, socio);
        }
    }

    public int[] insertarSocios(List<Socio> socios) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.insertarLote(con, socios);
        }
    }

    public boolean actualizarSocio(Socio socio) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.actualizar(con, socio);
        }
    }

    public int[] actualizarSocios(List<Socio> socios) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.actualizarLote(con, socios);
        }
    }

    public boolean eliminarSocio(int id) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.eliminar(con, id);
        }
    }

    public int[] eliminarSocios(List<Integer> ids) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            return socioDAO.eliminarLote(con, ids);
        }
    }

}
