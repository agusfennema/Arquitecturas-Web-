package ejemplo.repository.derby;

import java.sql.Connection;

import ejemplo.dao.DetallePedidoDAO;
import ejemplo.dao.PedidoDAO;
import ejemplo.dao.ProductoDAO;
import ejemplo.dao.UsuarioDAO;
import ejemplo.factory.DAOFactory;

/**
 * Fábrica concreta para Derby: la GEMELA de MySQLDAOFactory.
 *
 * Fijate que tiene exactamente la misma forma que la de MySQL. Eso es el Abstract Factory:
 * agregar un motor nuevo = agregar una subclase + un "case" en DAOFactory.getInstance().
 * Ni Main ni los utils cambian una sola línea.
 */
public class DerbyDAOFactory extends DAOFactory {

    /**
     * Implementación Derby del Factory Method de la conexión (opción C).
     * Cada fábrica sabe conseguir SU conexión; la superclase solo sabe que existe una.
     */
    @Override
    protected Connection getConnection() {
        return DerbyConnectionManager.getInstance().getConnection();
    }

    /** Cierre especifico de Derby: apaga ademas el motor embebido. */
    @Override
    protected void doShutdown() {
        DerbyConnectionManager.getInstance().shutdown();
    }

    @Override
    public UsuarioDAO createUsuarioDAO() {
        return new DerbyUsuarioDAO(getConnection());
    }

    @Override
    public ProductoDAO createProductoDAO() {
        return new DerbyProductoDAO(getConnection());
    }

    @Override
    public PedidoDAO createPedidoDAO() {
        return new DerbyPedidoDAO(getConnection());
    }

    @Override
    public DetallePedidoDAO createDetallePedidoDAO() {
        return new DerbyDetallePedidoDAO(getConnection());
    }
}
